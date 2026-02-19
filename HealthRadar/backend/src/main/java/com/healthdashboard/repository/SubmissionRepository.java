package com.healthdashboard.repository;

import com.healthdashboard.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByClinicIdAndSubmissionDateBetween(Long clinicId, LocalDate start, LocalDate end);

    List<Submission> findBySubmissionDateBetween(LocalDate start, LocalDate end);

    List<Submission> findByClinicId(Long clinicId);

    @Query("SELECT s FROM Submission s WHERE s.clinic.id = :clinicId AND s.symptomCategory = :symptom " +
           "AND s.submissionDate BETWEEN :startDate AND :endDate AND s.isValid = true")
    List<Submission> findValidSubmissions(
            @Param("clinicId") Long clinicId,
            @Param("symptom") String symptomCategory,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT s.symptomCategory FROM Submission s")
    List<String> findDistinctSymptomCategories();

    @Query("SELECT s FROM Submission s WHERE s.submissionDate = :date AND s.isValid = true")
    List<Submission> findValidSubmissionsByDate(@Param("date") LocalDate date);
}
