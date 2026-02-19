package com.healthdashboard.service;

import com.healthdashboard.entity.*;
import com.healthdashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnomalyDetectionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private BaselineService baselineService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    /**
     * Scheduled anomaly detection — runs every 6 hours by default.
     */
    @Scheduled(cron = "${app.anomaly.detection.cron}")
    @Transactional
    public void runScheduledDetection() {
        LocalDate today = LocalDate.now();
        // First, recalculate all baselines
        baselineService.calculateAllBaselines();

        // Then check today's submissions against baselines
        List<Submission> todaySubmissions = submissionRepository.findValidSubmissionsByDate(today);
        for (Submission submission : todaySubmissions) {
            detectAnomaly(submission);
        }
    }

    @Transactional
    public void detectAnomaly(Submission submission) {
        Optional<Baseline> latestBaseline = baselineService.getLatestBaseline(
                submission.getClinic().getId(), submission.getSymptomCategory());

        if (latestBaseline.isEmpty()) {
            // No baseline yet — calculate one
            Baseline newBaseline = baselineService.calculateBaseline(
                    submission.getClinic().getId(),
                    submission.getSymptomCategory(),
                    submission.getSubmissionDate());
            if (newBaseline == null)
                return;
            latestBaseline = Optional.of(newBaseline);
        }

        Baseline baseline = latestBaseline.get();
        double observed = submission.getCaseCount();
        double baselineAvg = baseline.getRollingAverage().doubleValue();
        double stdDev = baseline.getStandardDeviation().doubleValue();

        if (baselineAvg == 0)
            return; // Cannot compare against zero baseline

        // Check threshold-based anomaly
        double thresholdPct = getThresholdPercentage();
        double percentIncrease = ((observed - baselineAvg) / baselineAvg) * 100;

        if (percentIncrease >= thresholdPct) {
            createAlert(submission, baseline, Alert.AlertType.THRESHOLD_EXCEEDED,
                    observed, baselineAvg, percentIncrease / 100);
            return;
        }

        // Check statistical deviation
        double deviationThreshold = getDeviationThreshold();
        if (stdDev > 0) {
            double zScore = (observed - baselineAvg) / stdDev;
            if (zScore >= deviationThreshold) {
                createAlert(submission, baseline, Alert.AlertType.STATISTICAL_DEVIATION,
                        observed, baselineAvg, zScore);
            }
        }
    }

    private void createAlert(Submission submission, Baseline baseline,
            Alert.AlertType type, double observed, double baselineValue, double deviation) {
        Alert.Severity severity = calculateSeverity(deviation, type);

        int escalationHours = getEscalationHours();

        alertService.createAlert(
                submission.getClinic(),
                submission,
                baseline,
                submission.getSymptomCategory(),
                type,
                severity,
                BigDecimal.valueOf(observed),
                BigDecimal.valueOf(baselineValue),
                BigDecimal.valueOf(deviation),
                String.format("%s detected: observed=%,.0f, baseline=%,.2f, deviation=%.2f",
                        type.name(), observed, baselineValue, deviation),
                LocalDateTime.now().plusHours(escalationHours));
    }

    private Alert.Severity calculateSeverity(double deviation, Alert.AlertType type) {
        if (type == Alert.AlertType.THRESHOLD_EXCEEDED) {
            if (deviation >= 2.0)
                return Alert.Severity.CRITICAL;
            if (deviation >= 1.0)
                return Alert.Severity.HIGH;
            return Alert.Severity.MEDIUM;
        } else {
            if (deviation >= 4.0)
                return Alert.Severity.CRITICAL;
            if (deviation >= 3.0)
                return Alert.Severity.HIGH;
            if (deviation >= 2.0)
                return Alert.Severity.MEDIUM;
            return Alert.Severity.LOW;
        }
    }

    private double getThresholdPercentage() {
        return settingRepository.findBySettingKey("anomaly.threshold.percentage")
                .map(s -> Double.parseDouble(s.getSettingValue()))
                .orElse(50.0);
    }

    private double getDeviationThreshold() {
        return settingRepository.findBySettingKey("anomaly.threshold.deviation")
                .map(s -> Double.parseDouble(s.getSettingValue()))
                .orElse(2.0);
    }

    private int getEscalationHours() {
        return settingRepository.findBySettingKey("alert.escalation.hours")
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(12);
    }
}
