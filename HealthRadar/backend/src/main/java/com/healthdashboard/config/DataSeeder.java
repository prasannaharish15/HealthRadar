package com.healthdashboard.config;

import com.healthdashboard.entity.*;
import com.healthdashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private SettingRepository settingRepository;

        @Autowired
        private ClinicRepository clinicRepository;

        @Autowired
        private SubmissionRepository submissionRepository;

        @Autowired
        private BaselineRepository baselineRepository;

        @Autowired
        private AlertRepository alertRepository;

        @Autowired
        private AuditLogRepository auditLogRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private final Random random = new Random(42); // fixed seed for reproducibility

        @Override
        public void run(String... args) {
                // 1. Seed users
                User admin = seedUsers();

                // 2. Seed settings
                seedSettings();

                // 3. Seed clinics
                List<Clinic> clinics = seedClinics();

                // 4. Seed submissions (20 days of data)
                if (submissionRepository.count() == 0) {
                        List<Submission> allSubmissions = seedSubmissions(clinics, admin);

                        // 5. Seed baselines
                        List<Baseline> baselines = seedBaselines(clinics);

                        // 6. Seed alerts
                        seedAlerts(clinics, allSubmissions, baselines, admin);

                        // 7. Seed audit logs
                        seedAuditLogs(admin);
                }
        }

        // ========== USERS ==========
        private User seedUsers() {
                User admin;
                if (!userRepository.existsByUsername("admin")) {
                        admin = userRepository.save(User.builder()
                                        .username("admin")
                                        .email("admin@healthdashboard.com")
                                        .passwordHash(passwordEncoder.encode("admin123"))
                                        .fullName("System Administrator")
                                        .role(User.Role.ADMIN)
                                        .isActive(true)
                                        .build());
                } else {
                        admin = userRepository.findByUsername("admin").orElse(null);
                }

                if (!userRepository.existsByUsername("analyst1")) {
                        userRepository.save(User.builder()
                                        .username("analyst1")
                                        .email("analyst@healthdashboard.com")
                                        .passwordHash(passwordEncoder.encode("analyst123"))
                                        .fullName("Dr. Priya Sharma")
                                        .role(User.Role.ANALYST)
                                        .phone("+91-9876543210")
                                        .isActive(true)
                                        .build());
                }

                if (!userRepository.existsByUsername("viewer1")) {
                        userRepository.save(User.builder()
                                        .username("viewer1")
                                        .email("viewer@healthdashboard.com")
                                        .passwordHash(passwordEncoder.encode("viewer123"))
                                        .fullName("Rahul Mehta")
                                        .role(User.Role.VIEWER)
                                        .isActive(true)
                                        .build());
                }

                return admin;
        }

        // ========== SETTINGS ==========
        private void seedSettings() {
                seedSetting("anomaly.threshold.percentage", "50", "Percentage increase over baseline to trigger alert",
                                "ANOMALY_DETECTION");
                seedSetting("anomaly.threshold.deviation", "2.0", "Standard deviations above baseline to trigger alert",
                                "ANOMALY_DETECTION");
                seedSetting("anomaly.detection.window.hours", "6",
                                "Maximum hours for anomaly detection after submission",
                                "ANOMALY_DETECTION");
                seedSetting("anomaly.baseline.window.days", "14", "Number of days for rolling baseline calculation",
                                "ANOMALY_DETECTION");
                seedSetting("alert.escalation.hours", "12", "Hours before unacknowledged alert is escalated", "ALERTS");
                seedSetting("privacy.min.group.size", "5", "Minimum group size to display data", "PRIVACY");
                seedSetting("heatmap.zone.radius.km", "10", "Radius in km for heatmap zones", "VISUALIZATION");
                seedSetting("notification.email.enabled", "true", "Enable email notifications", "NOTIFICATIONS");
                seedSetting("notification.sms.enabled", "false", "Enable SMS notifications", "NOTIFICATIONS");
                seedSetting("notification.push.enabled", "false", "Enable push notifications", "NOTIFICATIONS");
        }

        private void seedSetting(String key, String value, String description, String category) {
                if (settingRepository.findBySettingKey(key).isEmpty()) {
                        settingRepository.save(Setting.builder()
                                        .settingKey(key)
                                        .settingValue(value)
                                        .description(description)
                                        .category(category)
                                        .build());
                }
        }

        // ========== CLINICS ==========
        private List<Clinic> seedClinics() {
                if (clinicRepository.count() > 0) {
                        return clinicRepository.findAll();
                }

                List<Clinic> clinics = new ArrayList<>();

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Central Health Clinic")
                                .code("CHC001")
                                .latitude(new BigDecimal("12.9716000"))
                                .longitude(new BigDecimal("77.5946000"))
                                .region("Bangalore Urban")
                                .district("Bangalore South")
                                .address("123 MG Road, Bangalore")
                                .contactPhone("+91-80-25551234")
                                .contactEmail("central@healthclinic.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("North District Hospital")
                                .code("NDH002")
                                .latitude(new BigDecimal("13.0358000"))
                                .longitude(new BigDecimal("77.5970000"))
                                .region("Bangalore Urban")
                                .district("Bangalore North")
                                .address("45 Hebbal Main Road")
                                .contactPhone("+91-80-25559876")
                                .contactEmail("north@districthospital.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Whitefield Medical Center")
                                .code("WMC003")
                                .latitude(new BigDecimal("12.9698000"))
                                .longitude(new BigDecimal("77.7500000"))
                                .region("Bangalore Urban")
                                .district("Bangalore East")
                                .address("78 ITPL Road, Whitefield")
                                .contactPhone("+91-80-25554567")
                                .contactEmail("whitefield@medcenter.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Jayanagar Community Health")
                                .code("JCH004")
                                .latitude(new BigDecimal("12.9250000"))
                                .longitude(new BigDecimal("77.5938000"))
                                .region("Bangalore Urban")
                                .district("Bangalore South")
                                .address("34 Jayanagar 4th Block")
                                .contactPhone("+91-80-25557890")
                                .contactEmail("jayanagar@communityhealth.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Electronic City Clinic")
                                .code("ECC005")
                                .latitude(new BigDecimal("12.8440000"))
                                .longitude(new BigDecimal("77.6630000"))
                                .region("Bangalore Urban")
                                .district("Anekal")
                                .address("Electronic City Phase 1")
                                .contactPhone("+91-80-25552345")
                                .contactEmail("ecity@clinic.in")
                                .isActive(true)
                                .build()));
                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("T. Nagar Health Center")
                                .code("TNG006")
                                .latitude(new BigDecimal("13.0418000"))
                                .longitude(new BigDecimal("80.2341000"))
                                .region("Chennai")
                                .district("Chennai South")
                                .address("15 Usman Road, T. Nagar, Chennai")
                                .contactPhone("+91-44-24341234")
                                .contactEmail("tnagar@healthcenter.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Anna Nagar General Hospital")
                                .code("ANG007")
                                .latitude(new BigDecimal("13.0850000"))
                                .longitude(new BigDecimal("80.2101000"))
                                .region("Chennai")
                                .district("Chennai North")
                                .address("2nd Avenue, Anna Nagar, Chennai")
                                .contactPhone("+91-44-26201567")
                                .contactEmail("annanagar@generalhospital.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Tambaram Community Clinic")
                                .code("TCC008")
                                .latitude(new BigDecimal("12.9249000"))
                                .longitude(new BigDecimal("80.1000000"))
                                .region("Chennai")
                                .district("Chengalpattu")
                                .address("45 GST Road, Tambaram, Chennai")
                                .contactPhone("+91-44-22261890")
                                .contactEmail("tambaram@communityclinic.in")
                                .isActive(true)
                                .build()));

                clinics.add(clinicRepository.save(Clinic.builder()
                                .name("Adyar Medical Institute")
                                .code("AMI009")
                                .latitude(new BigDecimal("13.0012000"))
                                .longitude(new BigDecimal("80.2565000"))
                                .region("Chennai")
                                .district("Chennai South")
                                .address("78 Sardar Patel Road, Adyar, Chennai")
                                .contactPhone("+91-44-24412345")
                                .contactEmail("adyar@medicalinstitute.in")
                                .isActive(true)
                                .build()));

                return clinics;
        }

        // ========== SUBMISSIONS ==========
        private List<Submission> seedSubmissions(List<Clinic> clinics, User admin) {
                List<Submission> allSubmissions = new ArrayList<>();
                LocalDate today = LocalDate.now();

                // Base case counts per symptom category (daily normal range)
                Map<String, int[]> normalRanges = new LinkedHashMap<>();
                normalRanges.put("Respiratory Infection", new int[] { 8, 18 });
                normalRanges.put("Gastroenteritis", new int[] { 5, 12 });
                normalRanges.put("Dengue-like Fever", new int[] { 3, 8 });
                normalRanges.put("Skin Infection", new int[] { 2, 7 });
                // Chennai-specific symptom categories
                normalRanges.put("Typhoid", new int[] { 4, 10 });
                normalRanges.put("Chikungunya", new int[] { 2, 6 });
                normalRanges.put("Leptospirosis", new int[] { 1, 5 });
                normalRanges.put("Conjunctivitis", new int[] { 3, 9 });

                // Generate 20 days of submissions for each clinic and each symptom category
                for (int dayOffset = 20; dayOffset >= 0; dayOffset--) {
                        LocalDate date = today.minusDays(dayOffset);

                        for (Clinic clinic : clinics) {
                                for (Map.Entry<String, int[]> entry : normalRanges.entrySet()) {
                                        String symptom = entry.getKey();
                                        int minCount = entry.getValue()[0];
                                        int maxCount = entry.getValue()[1];

                                        int caseCount;
                                        // Inject anomaly spikes for recent days at specific clinics
                                        // --- Bangalore anomaly spikes ---
                                        if (dayOffset <= 2 && clinic.getCode().equals("CHC001")
                                                        && symptom.equals("Dengue-like Fever")) {
                                                caseCount = 25 + random.nextInt(15); // Spike: 25-39 vs normal 3-8
                                        } else if (dayOffset <= 1 && clinic.getCode().equals("NDH002")
                                                        && symptom.equals("Respiratory Infection")) {
                                                caseCount = 40 + random.nextInt(20); // Spike: 40-59 vs normal 8-18
                                        } else if (dayOffset == 0 && clinic.getCode().equals("WMC003")
                                                        && symptom.equals("Gastroenteritis")) {
                                                caseCount = 30 + random.nextInt(10); // Spike: 30-39 vs normal 5-12
                                                // --- Chennai anomaly spikes ---
                                        } else if (dayOffset <= 2 && clinic.getCode().equals("TNG006")
                                                        && symptom.equals("Typhoid")) {
                                                caseCount = 28 + random.nextInt(12); // Spike: 28-39 vs normal 4-10
                                        } else if (dayOffset <= 1 && clinic.getCode().equals("ANG007")
                                                        && symptom.equals("Chikungunya")) {
                                                caseCount = 20 + random.nextInt(15); // Spike: 20-34 vs normal 2-6
                                        } else if (dayOffset == 0 && clinic.getCode().equals("TCC008")
                                                        && symptom.equals("Leptospirosis")) {
                                                caseCount = 18 + random.nextInt(10); // Spike: 18-27 vs normal 1-5
                                        } else if (dayOffset <= 3 && clinic.getCode().equals("AMI009")
                                                        && symptom.equals("Conjunctivitis")) {
                                                caseCount = 22 + random.nextInt(8); // Spike: 22-29 vs normal 3-9
                                        } else {
                                                caseCount = minCount + random.nextInt(maxCount - minCount + 1);
                                        }

                                        int groupSize = caseCount + 10 + random.nextInt(20); // group always > case
                                                                                             // count, ensures privacy

                                        Submission submission = submissionRepository.save(Submission.builder()
                                                        .clinic(clinic)
                                                        .submissionDate(date)
                                                        .symptomCategory(symptom)
                                                        .caseCount(caseCount)
                                                        .groupSize(groupSize)
                                                        .notes("Daily aggregated submission for " + symptom)
                                                        .submittedBy(admin)
                                                        .isValid(true)
                                                        .build());

                                        allSubmissions.add(submission);
                                }
                        }
                }

                return allSubmissions;
        }

        // ========== BASELINES ==========
        private List<Baseline> seedBaselines(List<Clinic> clinics) {
                List<Baseline> baselines = new ArrayList<>();
                LocalDate today = LocalDate.now();

                // Realistic rolling averages for each symptom category
                Map<String, double[]> baselineValues = new LinkedHashMap<>();
                // [rolling_average, standard_deviation]
                baselineValues.put("Respiratory Infection", new double[] { 13.0, 3.2 });
                baselineValues.put("Gastroenteritis", new double[] { 8.5, 2.4 });
                baselineValues.put("Dengue-like Fever", new double[] { 5.5, 1.8 });
                baselineValues.put("Skin Infection", new double[] { 4.5, 1.6 });
                // Chennai-specific symptoms
                baselineValues.put("Typhoid", new double[] { 7.0, 2.1 });
                baselineValues.put("Chikungunya", new double[] { 4.0, 1.4 });
                baselineValues.put("Leptospirosis", new double[] { 3.0, 1.2 });
                baselineValues.put("Conjunctivitis", new double[] { 6.0, 1.9 });

                for (Clinic clinic : clinics) {
                        for (Map.Entry<String, double[]> entry : baselineValues.entrySet()) {
                                String symptom = entry.getKey();
                                double avg = entry.getValue()[0];
                                double stdDev = entry.getValue()[1];

                                Baseline baseline = baselineRepository.save(Baseline.builder()
                                                .clinic(clinic)
                                                .symptomCategory(symptom)
                                                .baselineDate(today)
                                                .rollingAverage(BigDecimal.valueOf(avg).setScale(2,
                                                                RoundingMode.HALF_UP))
                                                .standardDeviation(BigDecimal.valueOf(stdDev).setScale(2,
                                                                RoundingMode.HALF_UP))
                                                .sampleCount(14)
                                                .windowDays(14)
                                                .build());

                                baselines.add(baseline);
                        }
                }

                return baselines;
        }

        // ========== ALERTS ==========
        private void seedAlerts(List<Clinic> clinics, List<Submission> allSubmissions, List<Baseline> allBaselines,
                        User admin) {
                if (alertRepository.count() > 0)
                        return;

                // Find the relevant clinics
                Clinic chc001 = clinics.stream().filter(c -> c.getCode().equals("CHC001")).findFirst()
                                .orElse(clinics.get(0));
                Clinic ndh002 = clinics.stream().filter(c -> c.getCode().equals("NDH002")).findFirst()
                                .orElse(clinics.get(1));
                Clinic wmc003 = clinics.stream().filter(c -> c.getCode().equals("WMC003")).findFirst()
                                .orElse(clinics.get(2));
                Clinic jch004 = clinics.stream().filter(c -> c.getCode().equals("JCH004")).findFirst()
                                .orElse(clinics.get(3));
                Clinic ecc005 = clinics.stream().filter(c -> c.getCode().equals("ECC005")).findFirst()
                                .orElse(clinics.get(4));

                // Find baselines for these clinics
                Baseline chcDengueBaseline = findBaseline(allBaselines, chc001, "Dengue-like Fever");
                Baseline ndhRespiratoryBaseline = findBaseline(allBaselines, ndh002, "Respiratory Infection");
                Baseline wmcGastroBaseline = findBaseline(allBaselines, wmc003, "Gastroenteritis");
                Baseline jchSkinBaseline = findBaseline(allBaselines, jch004, "Skin Infection");
                Baseline eccRespiratoryBaseline = findBaseline(allBaselines, ecc005, "Respiratory Infection");

                // Find recent submissions for linking
                Submission chcDengueSub = findRecentSubmission(allSubmissions, chc001, "Dengue-like Fever");
                Submission ndhRespSub = findRecentSubmission(allSubmissions, ndh002, "Respiratory Infection");
                Submission wmcGastroSub = findRecentSubmission(allSubmissions, wmc003, "Gastroenteritis");

                // --- Alert 1: CRITICAL PENDING - Dengue spike at Central Health Clinic
                // (recent) ---
                alertRepository.save(Alert.builder()
                                .clinic(chc001)
                                .submission(chcDengueSub)
                                .baseline(chcDengueBaseline)
                                .symptomCategory("Dengue-like Fever")
                                .alertType(Alert.AlertType.STATISTICAL_DEVIATION)
                                .severity(Alert.Severity.CRITICAL)
                                .status(Alert.Status.PENDING)
                                .observedValue(BigDecimal.valueOf(32))
                                .baselineValue(BigDecimal.valueOf(5.5))
                                .deviationFactor(BigDecimal.valueOf(4.72))
                                .description("Dengue-like Fever cases at Central Health Clinic surged to 32 — 481% above the 14-day rolling average of 5.5. Immediate investigation recommended.")
                                .escalationDeadline(LocalDateTime.now().plusHours(12))
                                .isEscalated(false)
                                .build());

                // --- Alert 2: HIGH PENDING - Respiratory spike at North District Hospital ---
                alertRepository.save(Alert.builder()
                                .clinic(ndh002)
                                .submission(ndhRespSub)
                                .baseline(ndhRespiratoryBaseline)
                                .symptomCategory("Respiratory Infection")
                                .alertType(Alert.AlertType.THRESHOLD_EXCEEDED)
                                .severity(Alert.Severity.HIGH)
                                .status(Alert.Status.PENDING)
                                .observedValue(BigDecimal.valueOf(48))
                                .baselineValue(BigDecimal.valueOf(13.0))
                                .deviationFactor(BigDecimal.valueOf(3.46))
                                .description("Respiratory Infection cases at North District Hospital reached 48 — 269% above the baseline of 13.0. Threshold of 50% exceeded.")
                                .escalationDeadline(LocalDateTime.now().plusHours(10))
                                .isEscalated(false)
                                .build());

                // --- Alert 3: MEDIUM PENDING - Gastro spike at Whitefield ---
                alertRepository.save(Alert.builder()
                                .clinic(wmc003)
                                .submission(wmcGastroSub)
                                .baseline(wmcGastroBaseline)
                                .symptomCategory("Gastroenteritis")
                                .alertType(Alert.AlertType.THRESHOLD_EXCEEDED)
                                .severity(Alert.Severity.MEDIUM)
                                .status(Alert.Status.PENDING)
                                .observedValue(BigDecimal.valueOf(33))
                                .baselineValue(BigDecimal.valueOf(8.5))
                                .deviationFactor(BigDecimal.valueOf(2.88))
                                .description("Gastroenteritis cases at Whitefield Medical Center reached 33 — 288% above the baseline of 8.5.")
                                .escalationDeadline(LocalDateTime.now().plusHours(8))
                                .isEscalated(false)
                                .build());

                // --- Alert 4: HIGH ACKNOWLEDGED - previous respiratory issue at Jayanagar (2
                // days ago) ---
                alertRepository.save(Alert.builder()
                                .clinic(jch004)
                                .baseline(jchSkinBaseline)
                                .symptomCategory("Skin Infection")
                                .alertType(Alert.AlertType.STATISTICAL_DEVIATION)
                                .severity(Alert.Severity.HIGH)
                                .status(Alert.Status.ACKNOWLEDGED)
                                .observedValue(BigDecimal.valueOf(18))
                                .baselineValue(BigDecimal.valueOf(4.5))
                                .deviationFactor(BigDecimal.valueOf(3.0))
                                .description("Skin Infection cases at Jayanagar Community Health spiked to 18. Admin acknowledged and is monitoring.")
                                .acknowledgedBy(admin)
                                .acknowledgedAt(LocalDateTime.now().minusHours(6))
                                .escalationDeadline(LocalDateTime.now().minusHours(2))
                                .isEscalated(false)
                                .build());

                // --- Alert 5: MEDIUM ACKNOWLEDGED - Gastro at Electronic City (3 days ago) ---
                alertRepository.save(Alert.builder()
                                .clinic(ecc005)
                                .baseline(eccRespiratoryBaseline)
                                .symptomCategory("Respiratory Infection")
                                .alertType(Alert.AlertType.THRESHOLD_EXCEEDED)
                                .severity(Alert.Severity.MEDIUM)
                                .status(Alert.Status.ACKNOWLEDGED)
                                .observedValue(BigDecimal.valueOf(22))
                                .baselineValue(BigDecimal.valueOf(13.0))
                                .deviationFactor(BigDecimal.valueOf(1.69))
                                .description("Respiratory Infection at Electronic City Clinic exceeded threshold. Under observation.")
                                .acknowledgedBy(admin)
                                .acknowledgedAt(LocalDateTime.now().minusDays(1))
                                .escalationDeadline(LocalDateTime.now().minusDays(1))
                                .isEscalated(false)
                                .build());

                // --- Alert 6: LOW RESOLVED - Old Dengue issue at North District (5 days ago)
                // ---
                alertRepository.save(Alert.builder()
                                .clinic(ndh002)
                                .baseline(findBaseline(allBaselines, ndh002, "Dengue-like Fever"))
                                .symptomCategory("Dengue-like Fever")
                                .alertType(Alert.AlertType.STATISTICAL_DEVIATION)
                                .severity(Alert.Severity.LOW)
                                .status(Alert.Status.RESOLVED)
                                .observedValue(BigDecimal.valueOf(12))
                                .baselineValue(BigDecimal.valueOf(5.5))
                                .deviationFactor(BigDecimal.valueOf(1.18))
                                .description("Mild Dengue-like Fever increase at North District Hospital. Resolved after enhanced sanitation measures.")
                                .acknowledgedBy(admin)
                                .acknowledgedAt(LocalDateTime.now().minusDays(4))
                                .resolvedBy(admin)
                                .resolvedAt(LocalDateTime.now().minusDays(3))
                                .resolutionNotes(
                                                "Enhanced sanitation and vector control measures implemented. Cases returned to normal range.")
                                .escalationDeadline(LocalDateTime.now().minusDays(4))
                                .isEscalated(false)
                                .build());

                // --- Alert 7: MEDIUM RESOLVED - old Gastro at Central (7 days ago) ---
                alertRepository.save(Alert.builder()
                                .clinic(chc001)
                                .baseline(findBaseline(allBaselines, chc001, "Gastroenteritis"))
                                .symptomCategory("Gastroenteritis")
                                .alertType(Alert.AlertType.THRESHOLD_EXCEEDED)
                                .severity(Alert.Severity.MEDIUM)
                                .status(Alert.Status.RESOLVED)
                                .observedValue(BigDecimal.valueOf(19))
                                .baselineValue(BigDecimal.valueOf(8.5))
                                .deviationFactor(BigDecimal.valueOf(1.24))
                                .description("Gastroenteritis spike at Central Health Clinic. Source identified as contaminated water supply.")
                                .acknowledgedBy(admin)
                                .acknowledgedAt(LocalDateTime.now().minusDays(6))
                                .resolvedBy(admin)
                                .resolvedAt(LocalDateTime.now().minusDays(5))
                                .resolutionNotes(
                                                "Water supply contamination identified and fixed. Boil-water advisory issued to nearby residents.")
                                .escalationDeadline(LocalDateTime.now().minusDays(6))
                                .isEscalated(false)
                                .build());

                // --- Alert 8: CRITICAL RESOLVED - escalated Respiratory at Whitefield (10 days
                // ago) ---
                alertRepository.save(Alert.builder()
                                .clinic(wmc003)
                                .baseline(findBaseline(allBaselines, wmc003, "Respiratory Infection"))
                                .symptomCategory("Respiratory Infection")
                                .alertType(Alert.AlertType.STATISTICAL_DEVIATION)
                                .severity(Alert.Severity.CRITICAL)
                                .status(Alert.Status.RESOLVED)
                                .observedValue(BigDecimal.valueOf(45))
                                .baselineValue(BigDecimal.valueOf(13.0))
                                .deviationFactor(BigDecimal.valueOf(4.06))
                                .description("Major respiratory infection outbreak at Whitefield Medical Center. Was escalated due to delayed acknowledgment.")
                                .acknowledgedBy(admin)
                                .acknowledgedAt(LocalDateTime.now().minusDays(9))
                                .resolvedBy(admin)
                                .resolvedAt(LocalDateTime.now().minusDays(7))
                                .resolutionNotes(
                                                "Outbreak contained. Traced to industrial air pollution event. Air quality advisory issued.")
                                .escalationDeadline(LocalDateTime.now().minusDays(10))
                                .isEscalated(true)
                                .build());

                // ========== CHENNAI ALERTS ==========
                Clinic tng006 = clinics.stream().filter(c -> c.getCode().equals("TNG006")).findFirst().orElse(null);
                Clinic ang007 = clinics.stream().filter(c -> c.getCode().equals("ANG007")).findFirst().orElse(null);
                Clinic tcc008 = clinics.stream().filter(c -> c.getCode().equals("TCC008")).findFirst().orElse(null);
                Clinic ami009 = clinics.stream().filter(c -> c.getCode().equals("AMI009")).findFirst().orElse(null);

                if (tng006 != null) {
                        // --- Alert 9: CRITICAL PENDING - Typhoid outbreak at T. Nagar ---
                        alertRepository.save(Alert.builder()
                                        .clinic(tng006)
                                        .submission(findRecentSubmission(allSubmissions, tng006, "Typhoid"))
                                        .baseline(findBaseline(allBaselines, tng006, "Typhoid"))
                                        .symptomCategory("Typhoid")
                                        .alertType(Alert.AlertType.STATISTICAL_DEVIATION)
                                        .severity(Alert.Severity.CRITICAL)
                                        .status(Alert.Status.PENDING)
                                        .observedValue(BigDecimal.valueOf(35))
                                        .baselineValue(BigDecimal.valueOf(7.0))
                                        .deviationFactor(BigDecimal.valueOf(4.0))
                                        .description("Typhoid cases at T. Nagar Health Center surged to 35 — 400% above the 14-day baseline of 7.0. Possible contaminated water source in the locality.")
                                        .escalationDeadline(LocalDateTime.now().plusHours(6))
                                        .isEscalated(false)
                                        .build());
                }

                if (ang007 != null) {
                        // --- Alert 10: HIGH PENDING - Chikungunya at Anna Nagar ---
                        alertRepository.save(Alert.builder()
                                        .clinic(ang007)
                                        .submission(findRecentSubmission(allSubmissions, ang007, "Chikungunya"))
                                        .baseline(findBaseline(allBaselines, ang007, "Chikungunya"))
                                        .symptomCategory("Chikungunya")
                                        .alertType(Alert.AlertType.THRESHOLD_EXCEEDED)
                                        .severity(Alert.Severity.HIGH)
                                        .status(Alert.Status.PENDING)
                                        .observedValue(BigDecimal.valueOf(28))
                                        .baselineValue(BigDecimal.valueOf(4.0))
                                        .deviationFactor(BigDecimal.valueOf(6.0))
                                        .description("Chikungunya cases at Anna Nagar General Hospital spiked to 28 — 600% above baseline. Mosquito breeding grounds suspected in nearby construction sites.")
                                        .escalationDeadline(LocalDateTime.now().plusHours(8))
                                        .isEscalated(false)
                                        .build());
                }

                if (tcc008 != null) {
                        // --- Alert 11: MEDIUM ACKNOWLEDGED - Leptospirosis at Tambaram ---
                        alertRepository.save(Alert.builder()
                                        .clinic(tcc008)
                                        .submission(findRecentSubmission(allSubmissions, tcc008, "Leptospirosis"))
                                        .baseline(findBaseline(allBaselines, tcc008, "Leptospirosis"))
                                        .symptomCategory("Leptospirosis")
                                        .alertType(Alert.AlertType.STATISTICAL_DEVIATION)
                                        .severity(Alert.Severity.MEDIUM)
                                        .status(Alert.Status.ACKNOWLEDGED)
                                        .observedValue(BigDecimal.valueOf(22))
                                        .baselineValue(BigDecimal.valueOf(3.0))
                                        .deviationFactor(BigDecimal.valueOf(6.33))
                                        .description("Leptospirosis cases at Tambaram Community Clinic surged after recent flooding. Post-monsoon contamination suspected.")
                                        .acknowledgedBy(admin)
                                        .acknowledgedAt(LocalDateTime.now().minusHours(4))
                                        .escalationDeadline(LocalDateTime.now().plusHours(4))
                                        .isEscalated(false)
                                        .build());
                }

                if (ami009 != null) {
                        // --- Alert 12: HIGH RESOLVED - Conjunctivitis outbreak at Adyar (escalated)
                        // ---
                        alertRepository.save(Alert.builder()
                                        .clinic(ami009)
                                        .baseline(findBaseline(allBaselines, ami009, "Conjunctivitis"))
                                        .symptomCategory("Conjunctivitis")
                                        .alertType(Alert.AlertType.THRESHOLD_EXCEEDED)
                                        .severity(Alert.Severity.HIGH)
                                        .status(Alert.Status.RESOLVED)
                                        .observedValue(BigDecimal.valueOf(26))
                                        .baselineValue(BigDecimal.valueOf(6.0))
                                        .deviationFactor(BigDecimal.valueOf(3.33))
                                        .description("Conjunctivitis outbreak at Adyar Medical Institute. Likely spread through schools in the area. Was escalated due to rapid spread.")
                                        .acknowledgedBy(admin)
                                        .acknowledgedAt(LocalDateTime.now().minusDays(3))
                                        .resolvedBy(admin)
                                        .resolvedAt(LocalDateTime.now().minusDays(1))
                                        .resolutionNotes(
                                                        "School closures enforced. Hygiene kits distributed. Eye clinics set up in affected wards.")
                                        .escalationDeadline(LocalDateTime.now().minusDays(4))
                                        .isEscalated(true)
                                        .build());
                }
        }

        // ========== AUDIT LOGS ==========
        private void seedAuditLogs(User admin) {
                if (auditLogRepository.count() > 0)
                        return;

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("USER_LOGIN")
                                .entityType("User")
                                .entityId(admin.getId())
                                .details("{\"username\":\"admin\",\"success\":true}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("ALERT_ACKNOWLEDGED")
                                .entityType("Alert")
                                .entityId(4L)
                                .details("{\"alertId\":4,\"symptom\":\"Skin Infection\",\"clinic\":\"Jayanagar Community Health\"}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("ALERT_RESOLVED")
                                .entityType("Alert")
                                .entityId(6L)
                                .details("{\"alertId\":6,\"resolution\":\"Enhanced sanitation and vector control measures implemented.\"}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("SETTINGS_UPDATED")
                                .entityType("Setting")
                                .entityId(1L)
                                .details("{\"key\":\"anomaly.threshold.percentage\",\"oldValue\":\"40\",\"newValue\":\"50\"}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("ALERT_ACKNOWLEDGED")
                                .entityType("Alert")
                                .entityId(5L)
                                .details("{\"alertId\":5,\"symptom\":\"Respiratory Infection\",\"clinic\":\"Electronic City Clinic\"}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("DATA_EXPORT")
                                .entityType("Report")
                                .entityId(null)
                                .details("{\"exportType\":\"CSV\",\"dateRange\":\"last_30_days\"}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("ALERT_RESOLVED")
                                .entityType("Alert")
                                .entityId(7L)
                                .details("{\"alertId\":7,\"resolution\":\"Water supply contamination identified and fixed.\"}")
                                .ipAddress("192.168.1.10")
                                .build());

                auditLogRepository.save(AuditLog.builder()
                                .user(admin)
                                .action("ALERT_ESCALATED")
                                .entityType("Alert")
                                .entityId(8L)
                                .details("{\"alertId\":8,\"reason\":\"Not acknowledged within 12-hour deadline\"}")
                                .ipAddress("10.0.0.1")
                                .build());
        }

        // ========== HELPERS ==========
        private Baseline findBaseline(List<Baseline> baselines, Clinic clinic, String symptom) {
                return baselines.stream()
                                .filter(b -> b.getClinic().getId().equals(clinic.getId())
                                                && b.getSymptomCategory().equals(symptom))
                                .findFirst()
                                .orElse(null);
        }

        private Submission findRecentSubmission(List<Submission> submissions, Clinic clinic, String symptom) {
                return submissions.stream()
                                .filter(s -> s.getClinic().getId().equals(clinic.getId())
                                                && s.getSymptomCategory().equals(symptom))
                                .reduce((first, second) -> second) // get last (most recent)
                                .orElse(null);
        }
}
