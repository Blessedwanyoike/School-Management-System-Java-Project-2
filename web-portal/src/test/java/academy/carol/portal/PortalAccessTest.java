package academy.carol.portal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import academy.carol.portal.config.SeedData;
import academy.carol.portal.domain.AcademicRecord;
import academy.carol.portal.domain.Role;
import academy.carol.portal.domain.Student;
import academy.carol.portal.domain.UserAccount;
import academy.carol.portal.repository.AcademicRecordRepository;
import academy.carol.portal.repository.AuditLogRepository;
import academy.carol.portal.repository.FeeAccountRepository;
import academy.carol.portal.repository.SchoolAssetRepository;
import academy.carol.portal.repository.StudentRepository;
import academy.carol.portal.repository.UserAccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carol-portal-test;DB_CLOSE_DELAY=-1",
        "app.seed-data.enabled=true",
        "app.seed-data.password=test-only-password"
})
@Transactional
class PortalAccessTest {
    private static final String TEST_SEED_PASSWORD = "test-only-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private StudentRepository students;

    @Autowired
    private AcademicRecordRepository academicRecords;

    @Autowired
    private FeeAccountRepository feeAccounts;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SeedData seedData;
    @Autowired
    private SchoolAssetRepository schoolAssets;
    @Autowired
    private AuditLogRepository auditLogs;

    @Test
    void seedsAdmissionNumbersAndStaffCounts() {
        assertEquals(1500L, students.count());
        assertEquals(60L, users.countByRole(Role.TEACHER));
        assertEquals(10L, users.countByRole(Role.BUS_DRIVER));
        assertEquals(1500L, users.countByRole(Role.PARENT));
        assertEquals(1500L, feeAccounts.count());
        assertEquals("B/1500/26", students.findByStudentCode("B/1500/26").orElseThrow().getStudentCode());
        assertTrue(users.findByUsername("B/001/26").orElseThrow().isEnabled());
    }

    @Test
    void parentCanViewLinkedStudentAndFeeBalance() throws Exception {
        mvc.perform(get("/parent").with(user("B/001/26").roles("PARENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Amina Njeri")))
                .andExpect(content().string(containsString("KES 40,000.00")));
    }

    @Test
    void parentCanViewDashboardForStudentB150026() throws Exception {
        mvc.perform(get("/parent").with(user("B/1500/26").roles("PARENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Student 1500")))
                .andExpect(content().string(containsString("B/1500/26")))
                .andExpect(content().string(containsString("KES 40,000.00")));
    }

    @Test
    void parentCannotSeeAnotherStudentsAcademicRecords() throws Exception {
        Student otherStudent = students.save(new Student("WEB-ST-999", "Unlinked Student", "Grade 9"));
        academicRecords.save(new AcademicRecord(otherStudent, "Term 1, 2026", "Private subject",
                new BigDecimal("99.00"), "Not this parent's record."));

        String page = mvc.perform(get("/parent").with(user("B/001/26").roles("PARENT")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(page.contains("Unlinked Student"));
        assertFalse(page.contains("Private subject"));
    }

    @Test
    void parentCannotOpenAdministrationPortal() throws Exception {
        mvc.perform(get("/admin").with(user("B/001/26").roles("PARENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorsCanReviewLogsButParentsCannot() throws Exception {
        mvc.perform(get("/logs").with(user("headteacher").roles("HEAD_TEACHER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Activity and system logs")));

        mvc.perform(get("/logs").with(user("B/001/26").roles("PARENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void signInsAndFailuresAreAddedToAuditLogs() throws Exception {
        mvc.perform(SecurityMockMvcRequestBuilders.formLogin()
                        .user("B/1500/26")
                        .password(TEST_SEED_PASSWORD))
                .andExpect(status().is3xxRedirection());

        mvc.perform(SecurityMockMvcRequestBuilders.formLogin()
                        .user("B/1500/26")
                        .password("incorrect-password"))
                .andExpect(status().is3xxRedirection());

        assertEquals(1L, auditLogs.countByAction("LOGIN_SUCCESS"));
        assertEquals(1L, auditLogs.countByAction("LOGIN_FAILED"));
    }

    @Test
    void parentCannotSearchHeadTeacherStudentReports() throws Exception {
        mvc.perform(get("/head-teacher/students")
                        .with(user("B/001/26").roles("PARENT"))
                        .param("q", "Amina"))
                .andExpect(status().isForbidden());
    }

    @Test
    void headTeacherCanSearchByStudentName() throws Exception {
        mvc.perform(get("/head-teacher/students")
                        .with(user("headteacher").roles("HEAD_TEACHER"))
                        .param("q", "Student 0010"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("B/010/26")));
    }

    @Test
    void headTeacherCanViewAcademicAndFeeReports() throws Exception {
        mvc.perform(get("/head-teacher/students/report")
                        .with(user("headteacher").roles("HEAD_TEACHER"))
                        .param("admissionNumber", "B/001/26"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Amina Njeri")))
                .andExpect(content().string(containsString("Mathematics")))
                .andExpect(content().string(containsString("KES 40,000.00")));
    }

    @Test
    void administrationListsSeededTeachersAndDrivers() throws Exception {
        String page = mvc.perform(get("/admin").with(user("headteacher").roles("HEAD_TEACHER")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(page.contains("teacher60"));
        assertTrue(page.contains("driver10"));
    }

    @Test
    void teacherSeesOnlyAssignedSubjectRecords() throws Exception {
        String page = mvc.perform(get("/teacher").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(page.contains("Mathematics"));
        assertFalse(page.contains("Outstanding class participation."));
    }

    @Test
    void deputyAndSecretaryCanOpenAdministrationPortalAndStudentSearch() throws Exception {
        mvc.perform(get("/admin").with(user("deputy").roles("DEPUTY_HEAD_TEACHER")))
                .andExpect(status().isOk());
        mvc.perform(get("/admin").with(user("secretary").roles("SECRETARY")))
                .andExpect(status().isOk());
        mvc.perform(get("/head-teacher/students")
                        .with(user("deputy").roles("DEPUTY_HEAD_TEACHER"))
                        .param("q", "Amina"))
                .andExpect(status().isOk());
        mvc.perform(get("/head-teacher/students")
                        .with(user("secretary").roles("SECRETARY"))
                        .param("q", "Amina"))
                .andExpect(status().isOk());
    }

    @Test
    void administratorsCanSeeAndUpdateFacilityAndBusStatus() throws Exception {
        String page = mvc.perform(get("/admin").with(user("headteacher").roles("HEAD_TEACHER")))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        assertTrue(page.contains("Algebra Basics"));
        assertTrue(page.contains("Water heater"));
        assertTrue(page.contains("Broken"));

        academy.carol.portal.domain.SchoolAsset heater = schoolAssets.findAll().stream()
                        .filter(asset -> asset.getItemName().equals("Water heater"))
                        .findFirst().orElseThrow();

        mvc.perform(post("/admin/assets/update")
                                .with(user("secretary").roles("SECRETARY"))
                                .with(csrf())
                                .param("assetId", heater.getId().toString())
                                .param("expectedQuantity", "2")
                                .param("presentQuantity", "2")
                                .param("status", "Repaired")
                                .param("notes", "Both heaters are working."))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/admin"));

        academy.carol.portal.domain.SchoolAsset updated = schoolAssets.findById(heater.getId()).orElseThrow();
        assertEquals("Repaired", updated.getStatus());
        assertEquals(0, updated.getMissingQuantity());
    }

    @Test
    void teacherCanCheckInAndCheckOut() throws Exception {
        mvc.perform(post("/teacher/check-in")
                        .with(user("teacher").roles("TEACHER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher"));

        UserAccount teacher = users.findByUsername("teacher").orElseThrow();
        assertTrue(teacher.getCheckIn() != null);
        assertTrue(teacher.getCheckOut() == null);

        mvc.perform(post("/teacher/check-out")
                        .with(user("teacher").roles("TEACHER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher"));

        UserAccount updatedTeacher = users.findByUsername("teacher").orElseThrow();
        assertTrue(updatedTeacher.getCheckOut() != null);
        assertEquals(1L, auditLogs.countByAction("TEACHER_CHECK_IN"));
        assertEquals(1L, auditLogs.countByAction("TEACHER_CHECK_OUT"));
    }

    @Test
    void parentCannotRecordPaymentAboveOwnBalance() throws Exception {
        Student student = students.findByStudentCode("B/001/26").orElseThrow();

        mvc.perform(post("/parent/pay")
                        .with(user("B/001/26").roles("PARENT"))
                        .with(csrf())
                        .param("amount", "50000.00"))
                .andExpect(status().is3xxRedirection());

        assertEquals(0, new BigDecimal("40000.00").compareTo(
                feeAccounts.findByStudent_Id(student.getId()).orElseThrow().getBalance()));
    }

    @Test
    void parentCanRecordPaymentAgainstLinkedBalance() throws Exception {
        Student student = students.findByStudentCode("B/001/26").orElseThrow();

        mvc.perform(post("/parent/pay")
                        .with(user("B/001/26").roles("PARENT"))
                        .with(csrf())
                        .param("amount", "5000.00"))
                .andExpect(status().is3xxRedirection());

        assertEquals(0, new BigDecimal("35000.00").compareTo(
                feeAccounts.findByStudent_Id(student.getId()).orElseThrow().getBalance()));
        assertEquals(1L, auditLogs.countByAction("PAYMENT_RECORDED"));
    }

    @Test
    void bursarCanRecordFeePaymentAndCreateReceipt() throws Exception {
        Student student = students.findByStudentCode("B/001/26").orElseThrow();
        var initialBalance = feeAccounts.findByStudent_Id(student.getId()).orElseThrow().getBalance();

        mvc.perform(get("/bursar").with(user("bursar").roles("BURSAR"))
                        .param("q", "B/001/26"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Student fees and receipts")));

        mvc.perform(post("/bursar/payments")
                        .with(user("bursar").roles("BURSAR"))
                        .with(csrf())
                        .param("admissionNumber", "B/001/26")
                        .param("amount", "2500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bursar?q=B/001/26"));

        var feeAccount = feeAccounts.findByStudent_Id(student.getId()).orElseThrow();
        assertEquals(0, initialBalance.subtract(new BigDecimal("2500.00")).compareTo(feeAccount.getBalance()));
        assertEquals(1L, auditLogs.countByAction("PAYMENT_RECORDED"));
    }

    @Test
    void administratorCanOpenModulesAndSaveOperationalRecords() throws Exception {
        mvc.perform(get("/modules").with(user("headteacher").roles("HEAD_TEACHER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Departments and operations")));

        mvc.perform(post("/modules/library/add")
                        .with(user("headteacher").roles("HEAD_TEACHER"))
                        .with(csrf())
                        .param("title", "Grade 6 textbook set")
                        .param("reference", "BK-600")
                        .param("details", "Issued to classroom block")
                        .param("status", "Issued"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/modules/library"));
        assertEquals(1L, auditLogs.countByAction("MODULE_RECORD_CREATED"));

        mvc.perform(get("/modules/library").with(user("headteacher").roles("HEAD_TEACHER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Grade 6 textbook set")));
    }

    @Test
    void attendanceEntriesAreAddedToTheAuditTrail() throws Exception {
        mvc.perform(post("/modules/attendance/add")
                        .with(user("headteacher").roles("HEAD_TEACHER"))
                        .with(csrf())
                        .param("title", "B/001/26")
                        .param("reference", "Present")
                        .param("details", "Morning roll call")
                        .param("status", "Recorded"))
                .andExpect(status().is3xxRedirection());

        assertEquals(1L, auditLogs.countByAction("ATTENDANCE_RECORDED"));
    }

    @Test
    void administratorCanOpenAnalyticsReports() throws Exception {
        mvc.perform(get("/reports").with(user("headteacher").roles("HEAD_TEACHER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("School reports and analytics")));
    }

    @Test
    void parentCanSignInWithAdmissionNumberAndSeedPassword() throws Exception {
        mvc.perform(SecurityMockMvcRequestBuilders.formLogin()
                        .user("B/001/26")
                        .password(TEST_SEED_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal"));
    }

    @Test
    void parentCanSignInAfterIndividualInvitation() throws Exception {
        UserAccount parent = users.findByUsername("B/001/26").orElseThrow();
        parent.updateSeedAccount(parent.getDisplayName(), Role.PARENT, "B/001/26", null,
                true, passwordEncoder.encode("individual-parent-password"));
        users.save(parent);

        mvc.perform(SecurityMockMvcRequestBuilders.formLogin()
                        .user("B/001/26")
                        .password("individual-parent-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal"));
    }

    @Test
    void signupPageHidesSeedCredentialsAndAllowsStaffRequestOnly() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Request sign-up")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(TEST_SEED_PASSWORD))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Local seed accounts only"))));

        mvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Request a staff account")))
                .andExpect(content().string(containsString("administrator must approve")));

        mvc.perform(post("/signup")
                        .with(csrf())
                        .param("displayName", "New Teacher")
                        .param("username", "new.teacher")
                        .param("role", "TEACHER")
                        .param("subject", "Science")
                        .param("password", "new-teacher-password")
                        .param("confirmPassword", "new-teacher-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        UserAccount pending = users.findByUsername("new.teacher").orElseThrow();
        assertEquals(Role.TEACHER, pending.getRole());
        assertFalse(pending.isEnabled());
        assertTrue(passwordEncoder.matches("new-teacher-password", pending.getPasswordHash()));

        mvc.perform(post("/signup")
                        .with(csrf())
                        .param("displayName", "Unauthorized Admin")
                        .param("username", "new.admin")
                        .param("role", "HEAD_TEACHER")
                        .param("password", "another-secure-password")
                        .param("confirmPassword", "another-secure-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup"));

        assertFalse(users.findByUsername("new.admin").isPresent());
    }

    @Test
    void administratorCanApproveStaffSignUp() throws Exception {
        users.save(new UserAccount("pending.teacher", passwordEncoder.encode("secure-password-123"),
                "Pending Teacher", Role.TEACHER, null, "English", false));

        mvc.perform(post("/admin/users/approve")
                        .with(user("headteacher").roles("HEAD_TEACHER"))
                        .with(csrf())
                        .param("userId", users.findByUsername("pending.teacher").orElseThrow().getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        assertTrue(users.findByUsername("pending.teacher").orElseThrow().isEnabled());
    }

    @Test
    void configuredPasswordUpdatesExistingSeedAccountsOnRestart() throws Exception {
        UserAccount headTeacher = users.findByUsername("headteacher").orElseThrow();
        headTeacher.updatePasswordHash(passwordEncoder.encode("previous seed password"));
        users.save(headTeacher);

        seedData.run(new DefaultApplicationArguments(new String[0]));

        UserAccount updatedHeadTeacher = users.findByUsername("headteacher").orElseThrow();
        assertTrue(passwordEncoder.matches(TEST_SEED_PASSWORD, updatedHeadTeacher.getPasswordHash()));
    }
}
