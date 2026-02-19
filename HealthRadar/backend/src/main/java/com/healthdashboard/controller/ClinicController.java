package com.healthdashboard.controller;

import com.healthdashboard.entity.Clinic;
import com.healthdashboard.repository.ClinicRepository;
import com.healthdashboard.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clinics")
public class ClinicController {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AuditService auditService;

    @GetMapping
    public ResponseEntity<List<Clinic>> getAllClinics() {
        return ResponseEntity.ok(clinicRepository.findByIsActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinic> getClinic(@PathVariable Long id) {
        return ResponseEntity.ok(clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found")));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<Clinic>> getByRegion(@PathVariable String region) {
        return ResponseEntity.ok(clinicRepository.findByRegion(region));
    }

    @PostMapping
    public ResponseEntity<Clinic> createClinic(@RequestBody Clinic clinic) {
        clinic = clinicRepository.save(clinic);
        return ResponseEntity.ok(clinic);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clinic> updateClinic(@PathVariable Long id, @RequestBody Clinic updated) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));
        clinic.setName(updated.getName());
        clinic.setRegion(updated.getRegion());
        clinic.setDistrict(updated.getDistrict());
        clinic.setAddress(updated.getAddress());
        clinic.setContactPhone(updated.getContactPhone());
        clinic.setContactEmail(updated.getContactEmail());
        clinic.setLatitude(updated.getLatitude());
        clinic.setLongitude(updated.getLongitude());
        clinic.setIsActive(updated.getIsActive());
        return ResponseEntity.ok(clinicRepository.save(clinic));
    }
}
