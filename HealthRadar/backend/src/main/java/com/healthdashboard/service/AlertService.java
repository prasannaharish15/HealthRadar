package com.healthdashboard.service;

import com.healthdashboard.entity.*;
import com.healthdashboard.repository.AlertRepository;
import com.healthdashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    public Alert createAlert(Clinic clinic, Submission submission, Baseline baseline,
            String symptomCategory, Alert.AlertType type, Alert.Severity severity,
            BigDecimal observedValue, BigDecimal baselineValue,
            BigDecimal deviationFactor, String description,
            LocalDateTime escalationDeadline) {
        Alert alert = Alert.builder()
                .clinic(clinic)
                .submission(submission)
                .baseline(baseline)
                .symptomCategory(symptomCategory)
                .alertType(type)
                .severity(severity)
                .status(Alert.Status.PENDING)
                .observedValue(observedValue)
                .baselineValue(baselineValue)
                .deviationFactor(deviationFactor)
                .description(description)
                .escalationDeadline(escalationDeadline)
                .isEscalated(false)
                .build();

        alert = alertRepository.save(alert);

        // Send notification for new alert
        notificationService.sendAlertNotification(alert);

        return alert;
    }

    @Transactional
    public Alert acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        alert.setStatus(Alert.Status.ACKNOWLEDGED);
        alert.setAcknowledgedBy(user);
        alert.setAcknowledgedAt(LocalDateTime.now());

        alert = alertRepository.save(alert);

        auditService.log(user, "ALERT_ACKNOWLEDGED", "Alert", alertId,
                "Alert acknowledged for " + alert.getSymptomCategory() + " at " + alert.getClinic().getName());

        return alert;
    }

    @Transactional
    public Alert resolveAlert(Long alertId, String resolutionNotes) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        alert.setStatus(Alert.Status.RESOLVED);
        alert.setResolvedBy(user);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(resolutionNotes);

        alert = alertRepository.save(alert);

        auditService.log(user, "ALERT_RESOLVED", "Alert", alertId,
                "Alert resolved: " + resolutionNotes);

        return alert;
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAllOrderByCreatedAtDesc();
    }

    public List<Alert> getAlertsByStatus(Alert.Status status) {
        return alertRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Alert> getAlertsByClinic(Long clinicId) {
        return alertRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);
    }

    public List<Alert> getRecentAlerts(int hours) {
        return alertRepository.findRecentAlerts(LocalDateTime.now().minusHours(hours));
    }

    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }

    public Long countByStatus(Alert.Status status) {
        return alertRepository.countByStatus(status);
    }

    /**
     * Escalation check — runs every 30 minutes to flag overdue alerts.
     */
    @Scheduled(cron = "${app.alert.escalation.cron}")
    @Transactional
    public void checkEscalations() {
        List<Alert> overdue = alertRepository.findUnescalatedPastDeadline(LocalDateTime.now());
        for (Alert alert : overdue) {
            alert.setIsEscalated(true);
            alert.setSeverity(Alert.Severity.CRITICAL);
            alertRepository.save(alert);

            notificationService.sendEscalationNotification(alert);

            auditService.log(null, "ALERT_ESCALATED", "Alert", alert.getId(),
                    "Alert escalated - not acknowledged within deadline");
        }
    }
}
