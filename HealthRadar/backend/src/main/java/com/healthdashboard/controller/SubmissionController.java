package com.healthdashboard.controller;

import com.healthdashboard.dto.SubmissionRequest;
import com.healthdashboard.entity.Submission;
import com.healthdashboard.service.AnomalyDetectionService;
import com.healthdashboard.service.DataIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    @Autowired
    private DataIngestionService dataIngestionService;

    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    @PostMapping
    public ResponseEntity<Submission> createSubmission(@RequestBody SubmissionRequest request) {
        Submission submission = dataIngestionService.ingestSubmission(request);
        // Trigger anomaly detection for the new submission
        anomalyDetectionService.detectAnomaly(submission);
        return ResponseEntity.ok(submission);
    }

    @GetMapping
    public ResponseEntity<List<Submission>> getAllSubmissions() {
        return ResponseEntity.ok(dataIngestionService.getAllSubmissions());
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<Submission>> getByClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(dataIngestionService.getSubmissionsByClinic(clinicId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Submission>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dataIngestionService.getSubmissionsByDateRange(start, end));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(dataIngestionService.getSymptomCategories());
    }
}
