package com.healthdashboard.service;

import com.healthdashboard.entity.Baseline;
import com.healthdashboard.entity.Clinic;
import com.healthdashboard.entity.Submission;
import com.healthdashboard.repository.BaselineRepository;
import com.healthdashboard.repository.ClinicRepository;
import com.healthdashboard.repository.SettingRepository;
import com.healthdashboard.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BaselineService {

    @Autowired
    private BaselineRepository baselineRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Transactional
    public Baseline calculateBaseline(Long clinicId, String symptomCategory, LocalDate date) {
        int windowDays = getBaselineWindowDays();
        LocalDate startDate = date.minusDays(windowDays);

        List<Submission> submissions = submissionRepository.findValidSubmissions(
                clinicId, symptomCategory, startDate, date.minusDays(1));

        if (submissions.isEmpty()) {
            return null;
        }

        // Calculate rolling average
        double sum = submissions.stream()
                .mapToInt(Submission::getCaseCount)
                .sum();
        double average = sum / submissions.size();

        // Calculate standard deviation
        double variance = submissions.stream()
                .mapToDouble(s -> Math.pow(s.getCaseCount() - average, 2))
                .sum() / submissions.size();
        double stdDev = Math.sqrt(variance);

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));

        // Upsert baseline
        Optional<Baseline> existing = baselineRepository
                .findByClinicIdAndSymptomCategoryAndBaselineDate(clinicId, symptomCategory, date);

        Baseline baseline;
        if (existing.isPresent()) {
            baseline = existing.get();
            baseline.setRollingAverage(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
            baseline.setStandardDeviation(BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP));
            baseline.setSampleCount(submissions.size());
        } else {
            baseline = Baseline.builder()
                    .clinic(clinic)
                    .symptomCategory(symptomCategory)
                    .baselineDate(date)
                    .rollingAverage(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP))
                    .standardDeviation(BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP))
                    .sampleCount(submissions.size())
                    .windowDays(windowDays)
                    .build();
        }

        return baselineRepository.save(baseline);
    }

    public Optional<Baseline> getLatestBaseline(Long clinicId, String symptomCategory) {
        return baselineRepository.findLatestBaseline(clinicId, symptomCategory);
    }

    @Transactional
    public void calculateAllBaselines() {
        LocalDate today = LocalDate.now();
        List<Clinic> clinics = clinicRepository.findByIsActiveTrue();
        List<String> symptoms = submissionRepository.findDistinctSymptomCategories();

        for (Clinic clinic : clinics) {
            for (String symptom : symptoms) {
                calculateBaseline(clinic.getId(), symptom, today);
            }
        }
    }

    private int getBaselineWindowDays() {
        return settingRepository.findBySettingKey("anomaly.baseline.window.days")
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(14);
    }
}
