package com.healthdashboard.controller;

import com.healthdashboard.entity.Setting;
import com.healthdashboard.entity.User;
import com.healthdashboard.repository.SettingRepository;
import com.healthdashboard.repository.UserRepository;
import com.healthdashboard.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @GetMapping
    public ResponseEntity<List<Setting>> getAllSettings() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Setting>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(settingRepository.findByCategory(category.toUpperCase()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<Setting> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key)));
    }

    @PutMapping("/{key}")
    public ResponseEntity<Setting> updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        Setting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));

        String oldValue = setting.getSettingValue();
        setting.setSettingValue(body.get("value"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        setting.setUpdatedBy(user);

        setting = settingRepository.save(setting);

        auditService.log(user, "SETTING_UPDATED", "Setting", setting.getId(),
                "Setting " + key + " changed from " + oldValue + " to " + body.get("value"));

        return ResponseEntity.ok(setting);
    }
}
