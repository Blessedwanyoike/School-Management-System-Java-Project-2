package academy.carol.portal.config;

import academy.carol.portal.domain.AcademicRecord;
import academy.carol.portal.domain.BusAssignment;
import academy.carol.portal.domain.FeeAccount;
import academy.carol.portal.domain.Role;
import academy.carol.portal.domain.SchoolAsset;
import academy.carol.portal.domain.Student;
import academy.carol.portal.domain.UserAccount;
import academy.carol.portal.repository.AcademicRecordRepository;
import academy.carol.portal.repository.BusAssignmentRepository;
import academy.carol.portal.repository.FeeAccountRepository;
import academy.carol.portal.repository.SchoolAssetRepository;
import academy.carol.portal.repository.StudentRepository;
import academy.carol.portal.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class SeedData implements ApplicationRunner {
    private static final String LEGACY_STUDENT_CODE = "WEB-ST-001";
    private static final String[] SUBJECTS = {
        "Mathematics", "English", "Science", "History", "Geography"
    };
    private static final String[] FIRST_NAMES = {
        "Amina", "Brian", "Grace", "Daniel", "Lucy",
        "Joseph", "Wanjiku", "Peter", "Mary", "Samuel",
        "Faith", "James", "Esther", "David", "Naomi"
    };
    private static final String[] LAST_NAMES = {
        "Njeri", "Otieno", "Mwangi", "Achieng", "Kamau", "Wambui"
    };

    private final UserAccountRepository users;
    private final StudentRepository students;
    private final AcademicRecordRepository academicRecords;
    private final FeeAccountRepository feeAccounts;
    private final BusAssignmentRepository busAssignments;
    private final SchoolAssetRepository schoolAssets;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed-data.enabled:true}")
    private boolean enabled;

    @Value("${app.seed-data.password}")
    private String seedPassword;

    public SeedData(UserAccountRepository users, StudentRepository students,
                    AcademicRecordRepository academicRecords, FeeAccountRepository feeAccounts,
                    BusAssignmentRepository busAssignments, SchoolAssetRepository schoolAssets,
                    PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.users = users;
        this.students = students;
        this.academicRecords = academicRecords;
        this.feeAccounts = feeAccounts;
        this.busAssignments = busAssignments;
        this.schoolAssets = schoolAssets;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        migrateUserRoles();
        String staffPasswordHash = passwordEncoder.encode(seedPassword);
        Map<String, UserAccount> accountsByUsername = new HashMap<String, UserAccount>();
        for (UserAccount account : users.findAll()) {
            accountsByUsername.put(account.getUsername(), account);
        }

        users.deleteByUsername("parent");
        accountsByUsername.remove("parent");
        seedStaff(accountsByUsername, staffPasswordHash);

        List<Student> roster = seedStudents();
        for (Student student : roster) {
            String username = student.getStudentCode();
            upsertUser(accountsByUsername, username, "Parent of " + student.getFullName(),
                    Role.PARENT, username, null, true, staffPasswordHash);
            seedFeeAccount(student);
        }

        seedAcademicRecords(roster.get(0));
        seedDrivers(accountsByUsername, staffPasswordHash);
        seedSchoolAssets();
    }

    private void migrateUserRoles() {
        try {
            List<Map<String, Object>> constraints = jdbcTemplate.queryForList(
                    "select constraint_name from information_schema.table_constraints "
                            + "where upper(table_name) = 'PORTAL_USERS' and constraint_type = 'CHECK'");
            for (Map<String, Object> constraint : constraints) {
                Object name = constraint.get("CONSTRAINT_NAME");
                if (name == null) {
                    continue;
                }
                jdbcTemplate.execute("alter table portal_users drop constraint \"" + name + "\"");
            }
            jdbcTemplate.execute("alter table portal_users alter column role varchar(30)");
        } catch (Exception ignored) {
            // Some databases do not expose H2 constraint metadata.
        }
    }

    private void seedStaff(Map<String, UserAccount> accounts, String passwordHash) {
        upsertUser(accounts, "headteacher", "Head Teacher", Role.HEAD_TEACHER,
                null, null, true, passwordHash);
        upsertUser(accounts, "deputy", "Deputy Head Teacher", Role.DEPUTY_HEAD_TEACHER,
                null, null, true, passwordHash);
        upsertUser(accounts, "secretary", "School Secretary", Role.SECRETARY,
                null, null, true, passwordHash);
        upsertUser(accounts, "bursar", "School Bursar", Role.BURSAR,
                null, null, true, passwordHash);

        for (int i = 1; i <= 60; i++) {
            String username = i == 1 ? "teacher" : "teacher" + String.format(Locale.ROOT, "%02d", i);
            upsertUser(accounts, username, staffName(i - 1), Role.TEACHER,
                    null, SUBJECTS[(i - 1) % SUBJECTS.length], true, passwordHash);
        }
    }

    private List<Student> seedStudents() {
        Map<String, Student> existing = new HashMap<String, Student>();
        for (Student student : students.findAll()) {
            existing.put(student.getStudentCode(), student);
        }

        if (!existing.containsKey("B/001/26")) {
            Student legacyStudent = existing.remove(LEGACY_STUDENT_CODE);
            if (legacyStudent != null) {
                legacyStudent.changeAdmissionNumber("B/001/26");
                existing.put("B/001/26", students.save(legacyStudent));
            }
        }

        List<Student> roster = new ArrayList<Student>(1500);
        for (int i = 1; i <= 1500; i++) {
            String admissionNumber = String.format(Locale.ROOT, "B/%03d/26", i);
            Student student = existing.get(admissionNumber);
            if (student == null) {
                String name = i == 1 ? "Amina Njeri" : String.format(Locale.ROOT, "Student %04d", i);
                student = students.save(new Student(admissionNumber, name, "Grade " + (((i - 1) % 12) + 1)));
                existing.put(admissionNumber, student);
            }
            roster.add(student);
        }
        return roster;
    }

    private void seedAcademicRecords(Student student) {
        if (!academicRecords.existsByStudent_Id(student.getId())) {
            academicRecords.save(new AcademicRecord(student, "Term 1, 2026", "Mathematics",
                    new BigDecimal("84.00"), "Excellent progress in problem solving."));
            academicRecords.save(new AcademicRecord(student, "Term 1, 2026", "English",
                    new BigDecimal("78.00"), "Good effort; continue reading regularly."));
            academicRecords.save(new AcademicRecord(student, "Term 1, 2026", "Science",
                    new BigDecimal("91.00"), "Outstanding class participation."));
        }
    }

    private void seedFeeAccount(Student student) {
        if (!feeAccounts.findByStudent_Id(student.getId()).isPresent()) {
            feeAccounts.save(new FeeAccount(student, new BigDecimal("120000.00"), new BigDecimal("80000.00")));
        }
    }

    private void seedDrivers(Map<String, UserAccount> accounts, String passwordHash) {
        String[] routes = { "Town", "West", "East", "North", "South" };
        for (int i = 1; i <= 10; i++) {
            String username = i == 1 ? "driver" : "driver" + String.format(Locale.ROOT, "%02d", i);
            upsertUser(accounts, username, staffName(59 + i), Role.BUS_DRIVER,
                    null, null, true, passwordHash);

            if (!busAssignments.findByDriverUsername(username).isPresent()) {
                BusAssignment bus = busAssignments.save(new BusAssignment(
                        username,
                        String.format(Locale.ROOT, "BU-%03d", i),
                        String.format(Locale.ROOT, "CA-BUS-%02d", i),
                        routes[(i - 1) % routes.length] + " route",
                        true));
                bus.checkIn(LocalTime.of(7, 0));
            }
        }
    }

    private void upsertUser(Map<String, UserAccount> accounts, String username, String displayName,
                            Role role, String studentCode, String subject, boolean accountEnabled,
                            String passwordHash) {
        UserAccount account = accounts.get(username);
        if (account == null) {
            account = new UserAccount(username, passwordHash, displayName, role,
                    studentCode, subject, accountEnabled);
        } else {
            account.updateSeedAccount(displayName, role, studentCode, subject, accountEnabled, passwordHash);
        }
        accounts.put(username, users.save(account));
    }

    private String staffName(int index) {
        return FIRST_NAMES[index % FIRST_NAMES.length] + " " + LAST_NAMES[index / FIRST_NAMES.length];
    }

    private void seedSchoolAssets() {
        if (schoolAssets.count() > 0) {
            return;
        }
        schoolAssets.save(new SchoolAsset("Library", "Library", "Things Fall Apart",
                12, 12, "Available", "Literature stock."));
        schoolAssets.save(new SchoolAsset("Library", "Library", "Introduction to Biology",
                8, 8, "Available", "Science stock."));
        schoolAssets.save(new SchoolAsset("Library", "Library", "Algebra Basics",
                12, 0, "Out of stock", "Replacement copies are required."));
        schoolAssets.save(new SchoolAsset("Dormitory", "Girls' Dormitory", "Bed",
                80, 78, "Missing 2", "Two beds are not present."));
        schoolAssets.save(new SchoolAsset("Dormitory", "Boys' Dormitory", "Mattress",
                100, 100, "Complete", ""));
        schoolAssets.save(new SchoolAsset("Dormitory", "Girls' Dormitory", "Study desk",
                40, 37, "Missing 3", "Three desks are not present."));
        schoolAssets.save(new SchoolAsset("Kitchen", "Main Kitchen", "Cooking pot",
                20, 18, "Missing 2", "Two pots need replacement."));
        schoolAssets.save(new SchoolAsset("Kitchen", "Main Kitchen", "Water heater",
                2, 1, "Broken", "One water heater is not working."));
    }
}
