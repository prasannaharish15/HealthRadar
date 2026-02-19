package com.healthdashboard.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "baselines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Baseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Clinic clinic;

    @Column(name = "symptom_category", nullable = false, length = 100)
    private String symptomCategory;

    @Column(name = "baseline_date", nullable = false)
    private LocalDate baselineDate;

    @Column(name = "rolling_average", nullable = false, precision = 10, scale = 2)
    private BigDecimal rollingAverage = BigDecimal.ZERO;

    @Column(name = "standard_deviation", nullable = false, precision = 10, scale = 2)
    private BigDecimal standardDeviation = BigDecimal.ZERO;

    @Column(name = "sample_count", nullable = false)
    private Integer sampleCount = 0;

    @Column(name = "window_days", nullable = false)
    private Integer windowDays = 14;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
