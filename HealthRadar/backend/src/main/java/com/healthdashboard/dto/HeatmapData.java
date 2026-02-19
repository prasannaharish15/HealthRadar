package com.healthdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapData {
    private List<HeatmapZone> zones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapZone {
        private Long clinicId;
        private String clinicName;
        private String region;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Double radiusKm;
        private Integer totalCases;
        private String intensityLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private String dominantSymptom;
        private List<SymptomBreakdown> symptoms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymptomBreakdown {
        private String category;
        private Integer count;
        private Boolean isAnomaly;
    }
}
