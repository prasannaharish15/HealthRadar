package com.healthdashboard.controller;

import com.healthdashboard.dto.DashboardStats;
import com.healthdashboard.entity.Alert;
import com.healthdashboard.repository.ClinicRepository;
import com.healthdashboard.repository.SubmissionRepository;
import com.healthdashboard.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        Long pendingAlerts = alertService.countByStatus(Alert.Status.PENDING);
        Long acknowledgedAlerts = alertService.countByStatus(Alert.Status.ACKNOWLEDGED);
        Long resolvedAlerts = alertService.countByStatus(Alert.Status.RESOLVED);

        Map<String, Long> alertsBySeverity = new HashMap<>();
        for (Alert.Severity severity : Alert.Severity.values()) {
            long count = alertService.getAlertsByStatus(Alert.Status.PENDING).stream()
                    .filter(a -> a.getSeverity() == severity).count();
            alertsBySeverity.put(severity.name(), count);
        }

        DashboardStats stats = DashboardStats.builder()
                .totalClinics(clinicRepository.count())
                .totalSubmissions(submissionRepository.count())
                .pendingAlerts(pendingAlerts)
                .acknowledgedAlerts(acknowledgedAlerts)
                .resolvedAlerts(resolvedAlerts)
                .criticalAlerts(alertsBySeverity.getOrDefault("CRITICAL", 0L))
                .alertsBySeverity(alertsBySeverity)
                .build();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-alerts")
    public ResponseEntity<?> getRecentAlerts(@RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(alertService.getRecentAlerts(hours));
    }
}
