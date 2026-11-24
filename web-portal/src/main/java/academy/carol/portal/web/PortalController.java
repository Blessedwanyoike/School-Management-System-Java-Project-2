package academy.carol.portal.web;

import academy.carol.portal.domain.Role;
import academy.carol.portal.domain.UserAccount;
import academy.carol.portal.repository.AcademicRecordRepository;
import academy.carol.portal.repository.BusAssignmentRepository;
import academy.carol.portal.repository.SchoolAssetRepository;
import academy.carol.portal.repository.SchoolRecordRepository;
import academy.carol.portal.repository.FeeAccountRepository;
import academy.carol.portal.repository.FeePaymentRepository;
import academy.carol.portal.repository.StudentRepository;
import academy.carol.portal.repository.UserAccountRepository;
import academy.carol.portal.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalTime;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PortalController {
    private final UserAccountRepository users;
    private final StudentRepository students;
    private final AcademicRecordRepository academicRecords;
    private final BusAssignmentRepository buses;
    private final SchoolAssetRepository schoolAssets;
    private final SchoolRecordRepository schoolRecords;
    private final FeeAccountRepository feeAccounts;
    private final FeePaymentRepository feePayments;
    private final AuditLogService auditLogs;

    public PortalController(UserAccountRepository users, StudentRepository students,
                            AcademicRecordRepository academicRecords, BusAssignmentRepository buses,
                            SchoolAssetRepository schoolAssets, SchoolRecordRepository schoolRecords,
                            FeeAccountRepository feeAccounts, FeePaymentRepository feePayments,
                            AuditLogService auditLogs) {
        this.users = users;
        this.students = students;
        this.academicRecords = academicRecords;
        this.buses = buses;
        this.schoolAssets = schoolAssets;
        this.schoolRecords = schoolRecords;
        this.feeAccounts = feeAccounts;
        this.feePayments = feePayments;
        this.auditLogs = auditLogs;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/portal";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/portal")
    public String portal(org.springframework.security.core.Authentication authentication) {
        UserAccount account = currentAccount(authentication.getName());
        if (account.getRole() == Role.PARENT) return "redirect:/parent";
        if (account.getRole() == Role.BUS_DRIVER) return "redirect:/driver";
        if (account.getRole() == Role.TEACHER) return "redirect:/teacher";
        if (account.getRole() == Role.BURSAR) return "redirect:/bursar";
        return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String admin(Model model, org.springframework.security.core.Authentication authentication) {
        UserAccount account = currentAccount(authentication.getName());
        model.addAttribute("displayName", account.getDisplayName());
        model.addAttribute("roleName", roleLabel(account.getRole()));
        model.addAttribute("studentCount", students.count());
        model.addAttribute("teacherCount", users.countByRole(Role.TEACHER));
        model.addAttribute("driverCount", users.countByRole(Role.BUS_DRIVER));
        model.addAttribute("busCount", buses.count());
        model.addAttribute("teachers", users.findByRoleOrderByDisplayNameAsc(Role.TEACHER));
        model.addAttribute("drivers", users.findByRoleOrderByDisplayNameAsc(Role.BUS_DRIVER));
        model.addAttribute("recentResults", academicRecords.findAllWithStudent());
        model.addAttribute("schoolAssets", schoolAssets.findAllByOrderByCategoryAscLocationAscItemNameAsc());
        model.addAttribute("busAssignments", buses.findAll());
        model.addAttribute("pendingStaffSignups", users.findByEnabledFalseAndRoleInOrderByDisplayNameAsc(
                java.util.Arrays.asList(Role.TEACHER, Role.BUS_DRIVER)));
        return "admin";
    }

    @GetMapping("/teacher")
    public String teacher(Model model, org.springframework.security.core.Authentication authentication) {
        UserAccount account = currentAccount(authentication.getName());
        model.addAttribute("displayName", account.getDisplayName());
        model.addAttribute("records", account.getSubject() == null
                ? java.util.Collections.emptyList()
                : academicRecords.findForSubjectWithStudent(account.getSubject()));
        model.addAttribute("subject", account.getSubject() == null ? "No subject assigned" : account.getSubject());
        model.addAttribute("checkIn", account.getCheckIn());
        model.addAttribute("checkOut", account.getCheckOut());
        return "teacher";
    }

    @PostMapping("/teacher/check-in")
    @Transactional
    public String teacherCheckIn(org.springframework.security.core.Authentication authentication,
                                 RedirectAttributes redirect) {
        UserAccount account = currentAccount(authentication.getName());
        account.checkIn(LocalTime.now());
        users.save(account);
        auditLogs.record(authentication.getName(), "TEACHER_CHECK_IN", "Teacher attendance",
                "Teacher checked in at " + account.getCheckIn() + ".");
        redirect.addFlashAttribute("message", "Check-in recorded.");
        return "redirect:/teacher";
    }

    @PostMapping("/teacher/check-out")
    @Transactional
    public String teacherCheckOut(org.springframework.security.core.Authentication authentication,
                                  RedirectAttributes redirect) {
        UserAccount account = currentAccount(authentication.getName());
        if (account.getCheckIn() == null) {
            redirect.addFlashAttribute("error", "Check in before checking out.");
            return "redirect:/teacher";
        }
        account.checkOut(LocalTime.now());
        users.save(account);
        auditLogs.record(authentication.getName(), "TEACHER_CHECK_OUT", "Teacher attendance",
                "Teacher checked out at " + account.getCheckOut() + ".");
        redirect.addFlashAttribute("message", "Check-out recorded.");
        return "redirect:/teacher";
    }

    @PostMapping("/admin/assets/update")
    @Transactional
    public String updateSchoolAsset(@RequestParam Long assetId,
                                    @RequestParam int expectedQuantity,
                                    @RequestParam int presentQuantity,
                                    @RequestParam String status,
                                    @RequestParam(required = false) String notes,
                                    org.springframework.security.core.Authentication authentication,
                                    RedirectAttributes redirect) {
        if (expectedQuantity < 0 || presentQuantity < 0) {
            redirect.addFlashAttribute("error", "Quantities cannot be negative.");
            return "redirect:/admin";
        }
        academy.carol.portal.domain.SchoolAsset asset = schoolAssets.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found."));
        asset.update(expectedQuantity, presentQuantity, status.trim(), notes == null ? "" : notes.trim());
        auditLogs.record(authentication.getName(), "ASSET_UPDATED", "Inventory",
                "Updated " + asset.getItemName() + " at " + asset.getLocation() + ".");
        redirect.addFlashAttribute("message", "School asset updated.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/buses/maintenance")
    @Transactional
    public String updateBusMaintenance(@RequestParam Long busId,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String notes,
                                       org.springframework.security.core.Authentication authentication,
                                       RedirectAttributes redirect) {
        academy.carol.portal.domain.BusAssignment bus = buses.findById(busId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bus not found."));
        bus.updateMaintenance(status.trim(), notes == null ? "" : notes.trim());
        auditLogs.record(authentication.getName(), "BUS_MAINTENANCE_UPDATED", "Transport",
                "Updated maintenance for bus " + bus.getBusCode() + " to " + status.trim() + ".");
        redirect.addFlashAttribute("message", "Bus maintenance status updated.");
        return "redirect:/admin";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("studentCount", students.count());
        model.addAttribute("teacherCount", users.countByRole(Role.TEACHER));
        model.addAttribute("bursarCount", users.countByRole(Role.BURSAR));
        model.addAttribute("feeAccountCount", feeAccounts.count());
        model.addAttribute("paymentCount", feePayments.count());
        model.addAttribute("academicRecordCount", academicRecords.count());
        model.addAttribute("moduleRecordCount", schoolRecords.count());
        model.addAttribute("busCount", buses.count());
        return "reports";
    }

    private UserAccount currentAccount(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    private String roleLabel(Role role) {
        switch (role) {
            case HEAD_TEACHER: return "Head Teacher";
            case DEPUTY_HEAD_TEACHER: return "Deputy Head Teacher";
            case SECRETARY: return "School Secretary";
            case BURSAR: return "Bursar";
            default: throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
