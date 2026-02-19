package com.healthdashboard.controller;

import com.healthdashboard.dto.HeatmapData;
import com.healthdashboard.service.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/visualization")
public class VisualizationController {

    @Autowired
    private VisualizationService visualizationService;

    @GetMapping("/heatmap")
    public ResponseEntity<HeatmapData> getHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(visualizationService.getHeatmapData(startDate, endDate));
    }

    @GetMapping("/heatmap/clinic/{clinicId}")
    public ResponseEntity<HeatmapData> getClinicDrilldown(
            @PathVariable Long clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(visualizationService.getClinicDrilldown(clinicId, startDate, endDate));
    }
}
