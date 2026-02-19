package com.healthdashboard.repository;

import com.healthdashboard.entity.Baseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface BaselineRepository extends JpaRepository<Baseline, Long> {

    Optional<Baseline> findByClinicIdAndSymptomCategoryAndBaselineDate(
            Long clinicId, String symptomCategory, LocalDate baselineDate);

    @Query("SELECT b FROM Baseline b WHERE b.clinic.id = :clinicId AND b.symptomCategory = :symptom " +
           "ORDER BY b.baselineDate DESC LIMIT 1")
    Optional<Baseline> findLatestBaseline(
            @Param("clinicId") Long clinicId,
            @Param("symptom") String symptomCategory);

    List<Baseline> findByClinicIdAndSymptomCategoryOrderByBaselineDateDesc(
            Long clinicId, String symptomCategory);
}
