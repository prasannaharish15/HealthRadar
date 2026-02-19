package com.healthdashboard.service;

import com.healthdashboard.dto.SubmissionRequest;
import com.healthdashboard.entity.Clinic;
import com.healthdashboard.entity.Submission;
import com.healthdashboard.entity.User;
import com.healthdashboard.repository.ClinicRepository;
import com.healthdashboard.repository.SettingRepository;
import com.healthdashboard.repository.SubmissionRepository;
import com.healthdashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DataIngestionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private AuditService auditService;

    @Transactional
    public Submission ingestSubmission(SubmissionRequest request) {
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new RuntimeException("Clinic not found: " + request.getClinicId()));

        // Enforce minimum group-size privacy rule
        int minGroupSize = getMinGroupSize();
        if (request.getGroupSize() < minGroupSize) {
            throw new RuntimeException("Group size (" + request.getGroupSize() +
                    ") is below minimum required (" + minGroupSize + ") for privacy protection");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User submitter = userRepository.findByUsername(username).orElse(null);

        Submission submission = Submission.builder()
                .clinic(clinic)
                .submissionDate(request.getSubmissionDate() != null ? request.getSubmissionDate() : LocalDate.now())
                .symptomCategory(request.getSymptomCategory())
                .caseCount(request.getCaseCount())
                .groupSize(request.getGroupSize())
                .notes(request.getNotes())
                .submittedBy(submitter)
                .isValid(true)
                .build();

        submission = submissionRepository.save(submission);

        auditService.log(submitter, "SUBMISSION_CREATED", "Submission", submission.getId(),
                "New submission for clinic " + clinic.getName() + " - " + request.getSymptomCategory());

        return submission;
    }

    public List<Submission> getSubmissionsByClinic(Long clinicId) {
        return submissionRepository.findByClinicId(clinicId);
    }

    public List<Submission> getSubmissionsByDateRange(LocalDate start, LocalDate end) {
        return submissionRepository.findBySubmissionDateBetween(start, end);
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public List<String> getSymptomCategories() {
        return submissionRepository.findDistinctSymptomCategories();
    }

    private int getMinGroupSize() {
        return settingRepository.findBySettingKey("privacy.min.group.size")
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(5);
    }
}
