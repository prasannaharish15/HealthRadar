package com.healthdashboard.controller;

import com.healthdashboard.entity.Alert;
import com.healthdashboard.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Alert>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(Alert.Status.valueOf(status.toUpperCase())));
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<Alert>> getByClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(alertService.getAlertsByClinic(clinicId));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledge(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(id));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String notes = body.getOrDefault("resolutionNotes", "");
        return ResponseEntity.ok(alertService.resolveAlert(id, notes));
    }
}
