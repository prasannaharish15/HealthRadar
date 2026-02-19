package com.healthdashboard.repository;

import com.healthdashboard.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    Optional<Clinic> findByCode(String code);
    List<Clinic> findByRegion(String region);
    List<Clinic> findByIsActiveTrue();
}
