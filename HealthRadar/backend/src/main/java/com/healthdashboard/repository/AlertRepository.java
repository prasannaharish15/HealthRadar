package com.healthdashboard.repository;

import com.healthdashboard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatusOrderByCreatedAtDesc(Alert.Status status);

    List<Alert> findByClinicIdOrderByCreatedAtDesc(Long clinicId);

    List<Alert> findBySeverityOrderByCreatedAtDesc(Alert.Severity severity);

    @Query("SELECT a FROM Alert a WHERE a.status = 'PENDING' AND a.escalationDeadline < :now AND a.isEscalated = false")
    List<Alert> findUnescalatedPastDeadline(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Alert a ORDER BY a.createdAt DESC")
    List<Alert> findAllOrderByCreatedAtDesc();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = :status")
    Long countByStatus(@Param("status") Alert.Status status);

    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);
}
