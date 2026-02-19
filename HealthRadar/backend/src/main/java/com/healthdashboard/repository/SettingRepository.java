package com.healthdashboard.repository;

import com.healthdashboard.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findBySettingKey(String settingKey);
    List<Setting> findByCategory(String category);
}
