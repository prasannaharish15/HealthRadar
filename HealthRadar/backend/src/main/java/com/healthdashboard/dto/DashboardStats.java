package com.healthdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalClinics;
    private Long totalSubmissions;
    private Long pendingAlerts;
    private Long acknowledgedAlerts;
    private Long resolvedAlerts;
    private Long criticalAlerts;
    private Map<String, Long> submissionsByCategory;
    private Map<String, Long> alertsBySeverity;
}
