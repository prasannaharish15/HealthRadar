package com.healthdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {
    private Long clinicId;
    private LocalDate submissionDate;
    private String symptomCategory;
    private Integer caseCount;
    private Integer groupSize;
    private String notes;
}
