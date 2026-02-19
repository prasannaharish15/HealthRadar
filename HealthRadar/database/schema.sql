-- =============================================
-- Regional Health Trend Dashboard - MySQL Schema
-- =============================================

CREATE DATABASE IF NOT EXISTS health_dashboard;
USE health_dashboard;

-- =============================================
-- 1. Users Table
-- =============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'ANALYST', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 2. Clinics Table
-- =============================================
CREATE TABLE clinics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    region VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    address TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_clinics_region (region),
    INDEX idx_clinics_coords (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 3. Submissions Table
-- =============================================
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id BIGINT NOT NULL,
    submission_date DATE NOT NULL,
    symptom_category VARCHAR(100) NOT NULL,
    case_count INT NOT NULL DEFAULT 0,
    group_size INT NOT NULL DEFAULT 0,
    notes TEXT,
    submitted_by BIGINT,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_submissions_date (submission_date),
    INDEX idx_submissions_clinic_date (clinic_id, submission_date),
    INDEX idx_submissions_symptom (symptom_category),
    CONSTRAINT chk_case_count CHECK (case_count >= 0),
    CONSTRAINT chk_group_size CHECK (group_size >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 4. Baselines Table
-- =============================================
CREATE TABLE baselines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id BIGINT NOT NULL,
    symptom_category VARCHAR(100) NOT NULL,
    baseline_date DATE NOT NULL,
    rolling_average DECIMAL(10, 2) NOT NULL DEFAULT 0,
    standard_deviation DECIMAL(10, 2) NOT NULL DEFAULT 0,
    sample_count INT NOT NULL DEFAULT 0,
    window_days INT NOT NULL DEFAULT 14,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    INDEX idx_baselines_clinic_symptom (clinic_id, symptom_category),
    INDEX idx_baselines_date (baseline_date),
    UNIQUE KEY uk_baseline (clinic_id, symptom_category, baseline_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 5. Alerts Table
-- =============================================
CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id BIGINT NOT NULL,
    submission_id BIGINT,
    baseline_id BIGINT,
    symptom_category VARCHAR(100) NOT NULL,
    alert_type ENUM('THRESHOLD_EXCEEDED', 'STATISTICAL_DEVIATION', 'MANUAL') NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('PENDING', 'ACKNOWLEDGED', 'RESOLVED') NOT NULL DEFAULT 'PENDING',
    observed_value DECIMAL(10, 2) NOT NULL,
    baseline_value DECIMAL(10, 2) NOT NULL,
    deviation_factor DECIMAL(10, 2),
    description TEXT,
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMP NULL,
    resolved_by BIGINT,
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT,
    escalation_deadline TIMESTAMP NULL,
    is_escalated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE SET NULL,
    FOREIGN KEY (baseline_id) REFERENCES baselines(id) ON DELETE SET NULL,
    FOREIGN KEY (acknowledged_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_alerts_status (status),
    INDEX idx_alerts_severity (severity),
    INDEX idx_alerts_clinic (clinic_id),
    INDEX idx_alerts_created (created_at),
    INDEX idx_alerts_escalation (escalation_deadline, is_escalated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 6. Audit Logs Table
-- =============================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details JSON,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 7. Settings Table
-- =============================================
CREATE TABLE settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_settings_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- Seed Data
-- =============================================

-- Default admin user (password: admin123 - BCrypt hash)
INSERT INTO users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@healthdashboard.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', 'ADMIN');

-- Default settings
INSERT INTO settings (setting_key, setting_value, description, category) VALUES
('anomaly.threshold.percentage', '50', 'Percentage increase over baseline to trigger alert', 'ANOMALY_DETECTION'),
('anomaly.threshold.deviation', '2.0', 'Standard deviations above baseline to trigger alert', 'ANOMALY_DETECTION'),
('anomaly.detection.window.hours', '6', 'Maximum hours for anomaly detection after submission', 'ANOMALY_DETECTION'),
('anomaly.baseline.window.days', '14', 'Number of days for rolling baseline calculation', 'ANOMALY_DETECTION'),
('alert.escalation.hours', '12', 'Hours before unacknowledged alert is escalated', 'ALERTS'),
('privacy.min.group.size', '5', 'Minimum group size to display data (privacy protection)', 'PRIVACY'),
('heatmap.zone.radius.km', '10', 'Radius in km for heatmap zones', 'VISUALIZATION'),
('notification.email.enabled', 'true', 'Enable email notifications', 'NOTIFICATIONS'),
('notification.sms.enabled', 'false', 'Enable SMS notifications', 'NOTIFICATIONS'),
('notification.push.enabled', 'false', 'Enable push notifications', 'NOTIFICATIONS');

-- Sample clinics
INSERT INTO clinics (name, code, latitude, longitude, region, district, address) VALUES
('Central Health Clinic', 'CHC001', 12.9716, 77.5946, 'Bangalore Urban', 'Bangalore South', '123 MG Road, Bangalore'),
('North District Hospital', 'NDH002', 13.0358, 77.5970, 'Bangalore Urban', 'Bangalore North', '45 Hebbal Main Road'),
('Whitefield Medical Center', 'WMC003', 12.9698, 77.7500, 'Bangalore Urban', 'Bangalore East', '78 ITPL Road, Whitefield'),
('Jayanagar Community Health', 'JCH004', 12.9250, 77.5938, 'Bangalore Urban', 'Bangalore South', '34 Jayanagar 4th Block'),
('Electronic City Clinic', 'ECC005', 12.8440, 77.6630, 'Bangalore Urban', 'Anekal', 'Electronic City Phase 1');
