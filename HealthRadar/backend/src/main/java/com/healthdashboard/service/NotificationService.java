package com.healthdashboard.service;

import com.healthdashboard.entity.Alert;
import com.healthdashboard.repository.SettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SettingRepository settingRepository;

    public void sendAlertNotification(Alert alert) {
        if (isEmailEnabled()) {
            sendEmail(alert, "New Health Alert",
                    String.format("Alert: %s at %s - Severity: %s",
                            alert.getSymptomCategory(),
                            alert.getClinic().getName(),
                            alert.getSeverity()));
        }

        if (isSmsEnabled()) {
            sendSms(alert);
        }

        if (isPushEnabled()) {
            sendPush(alert);
        }
    }

    public void sendEscalationNotification(Alert alert) {
        log.warn("ESCALATION: Alert {} has not been acknowledged within the deadline. Clinic: {}, Symptom: {}",
                alert.getId(), alert.getClinic().getName(), alert.getSymptomCategory());

        if (isEmailEnabled()) {
            sendEmail(alert, "URGENT: Escalated Health Alert",
                    String.format("ESCALATED ALERT: %s at %s requires immediate attention. " +
                            "This alert was not acknowledged within the required timeframe.",
                            alert.getSymptomCategory(),
                            alert.getClinic().getName()));
        }
    }

    private void sendEmail(Alert alert, String subject, String body) {
        // In production, use JavaMailSender to send actual emails
        log.info("EMAIL NOTIFICATION - To: admin@healthdashboard.com, Subject: {}, Body: {}", subject, body);
        // mailSender.send(message);
    }

    private void sendSms(Alert alert) {
        // Integration point for SMS provider (Twilio, AWS SNS, etc.)
        log.info("SMS NOTIFICATION - Alert {} for clinic {}", alert.getId(), alert.getClinic().getName());
    }

    private void sendPush(Alert alert) {
        // Integration point for push notification service (Firebase, etc.)
        log.info("PUSH NOTIFICATION - Alert {} for clinic {}", alert.getId(), alert.getClinic().getName());
    }

    private boolean isEmailEnabled() {
        return settingRepository.findBySettingKey("notification.email.enabled")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(true);
    }

    private boolean isSmsEnabled() {
        return settingRepository.findBySettingKey("notification.sms.enabled")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);
    }

    private boolean isPushEnabled() {
        return settingRepository.findBySettingKey("notification.push.enabled")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);
    }
}
