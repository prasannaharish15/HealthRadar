package com.healthdashboard.service;

import com.healthdashboard.dto.HeatmapData;
import com.healthdashboard.entity.Alert;
import com.healthdashboard.entity.Clinic;
import com.healthdashboard.entity.Submission;
import com.healthdashboard.repository.AlertRepository;
import com.healthdashboard.repository.ClinicRepository;
import com.healthdashboard.repository.SettingRepository;
import com.healthdashboard.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VisualizationService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SettingRepository settingRepository;

    public HeatmapData getHeatmapData(LocalDate startDate, LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().minusDays(7);
        if (endDate == null)
            endDate = LocalDate.now();

        double radiusKm = getZoneRadiusKm();
        int minGroupSize = getMinGroupSize();

        List<Clinic> clinics = clinicRepository.findByIsActiveTrue();
        List<HeatmapData.HeatmapZone> zones = new ArrayList<>();

        for (Clinic clinic : clinics) {
            List<Submission> submissions = submissionRepository
                    .findByClinicIdAndSubmissionDateBetween(clinic.getId(), startDate, endDate);

            // Privacy filter: skip clinics that don't meet minimum group size
            boolean meetsPrivacy = submissions.stream()
                    .allMatch(s -> s.getGroupSize() >= minGroupSize);
            if (!meetsPrivacy && !submissions.isEmpty())
                continue;

            // Aggregate by symptom category
            Map<String, Integer> symptomCounts = submissions.stream()
                    .collect(Collectors.groupingBy(
                            Submission::getSymptomCategory,
                            Collectors.summingInt(Submission::getCaseCount)));

            int totalCases = symptomCounts.values().stream().mapToInt(Integer::intValue).sum();

            // Determine which symptoms have active alerts
            List<Alert> clinicAlerts = alertRepository.findByClinicIdOrderByCreatedAtDesc(clinic.getId());
            Set<String> alertedSymptoms = clinicAlerts.stream()
                    .filter(a -> a.getStatus() != Alert.Status.RESOLVED)
                    .map(Alert::getSymptomCategory)
                    .collect(Collectors.toSet());

            List<HeatmapData.SymptomBreakdown> breakdowns = symptomCounts.entrySet().stream()
                    .map(e -> HeatmapData.SymptomBreakdown.builder()
                            .category(e.getKey())
                            .count(e.getValue())
                            .isAnomaly(alertedSymptoms.contains(e.getKey()))
                            .build())
                    .collect(Collectors.toList());

            String dominantSymptom = symptomCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("None");

            String intensity = calculateIntensity(totalCases);

            zones.add(HeatmapData.HeatmapZone.builder()
                    .clinicId(clinic.getId())
                    .clinicName(clinic.getName())
                    .region(clinic.getRegion())
                    .latitude(clinic.getLatitude())
                    .longitude(clinic.getLongitude())
                    .radiusKm(radiusKm)
                    .totalCases(totalCases)
                    .intensityLevel(intensity)
                    .dominantSymptom(dominantSymptom)
                    .symptoms(breakdowns)
                    .build());
        }

        return HeatmapData.builder().zones(zones).build();
    }

    public HeatmapData getClinicDrilldown(Long clinicId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null)
            startDate = LocalDate.now().minusDays(30);
        if (endDate == null)
            endDate = LocalDate.now();

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));

        List<Submission> submissions = submissionRepository
                .findByClinicIdAndSubmissionDateBetween(clinicId, startDate, endDate);

        Map<String, Integer> symptomCounts = submissions.stream()
                .collect(Collectors.groupingBy(
                        Submission::getSymptomCategory,
                        Collectors.summingInt(Submission::getCaseCount)));

        List<Alert> alerts = alertRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);
        Set<String> alertedSymptoms = alerts.stream()
                .filter(a -> a.getStatus() != Alert.Status.RESOLVED)
                .map(Alert::getSymptomCategory)
                .collect(Collectors.toSet());

        List<HeatmapData.SymptomBreakdown> breakdowns = symptomCounts.entrySet().stream()
                .map(e -> HeatmapData.SymptomBreakdown.builder()
                        .category(e.getKey())
                        .count(e.getValue())
                        .isAnomaly(alertedSymptoms.contains(e.getKey()))
                        .build())
                .collect(Collectors.toList());

        int totalCases = symptomCounts.values().stream().mapToInt(Integer::intValue).sum();

        HeatmapData.HeatmapZone zone = HeatmapData.HeatmapZone.builder()
                .clinicId(clinic.getId())
                .clinicName(clinic.getName())
                .region(clinic.getRegion())
                .latitude(clinic.getLatitude())
                .longitude(clinic.getLongitude())
                .radiusKm(getZoneRadiusKm())
                .totalCases(totalCases)
                .intensityLevel(calculateIntensity(totalCases))
                .dominantSymptom(symptomCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).orElse("None"))
                .symptoms(breakdowns)
                .build();

        return HeatmapData.builder().zones(List.of(zone)).build();
    }

    private String calculateIntensity(int totalCases) {
        if (totalCases >= 100)
            return "CRITICAL";
        if (totalCases >= 50)
            return "HIGH";
        if (totalCases >= 20)
            return "MEDIUM";
        return "LOW";
    }

    private double getZoneRadiusKm() {
        return settingRepository.findBySettingKey("heatmap.zone.radius.km")
                .map(s -> Double.parseDouble(s.getSettingValue()))
                .orElse(10.0);
    }

    private int getMinGroupSize() {
        return settingRepository.findBySettingKey("privacy.min.group.size")
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(5);
    }
}
