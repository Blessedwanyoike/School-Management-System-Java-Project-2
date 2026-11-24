package academy.carol.portal.web;

import academy.carol.portal.domain.Role;
import academy.carol.portal.domain.UserAccount;
import academy.carol.portal.repository.UserAccountRepository;
import academy.carol.portal.service.AuditLogService;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StaffRegistrationController {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogs;

    public StaffRegistrationController(UserAccountRepository users, PasswordEncoder passwordEncoder,
                                       AuditLogService auditLogs) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.auditLogs = auditLogs;
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    @Transactional
    public String register(@RequestParam String username,
                           @RequestParam String displayName,
                           @RequestParam String role,
                           @RequestParam(required = false) String subject,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirect) {
        String normalizedUsername = username.trim();
        String normalizedName = displayName.trim();
        Role accountRole;
        if ("TEACHER".equals(role)) {
            accountRole = Role.TEACHER;
        } else if ("BUS_DRIVER".equals(role)) {
            accountRole = Role.BUS_DRIVER;
        } else {
            redirect.addFlashAttribute("error", "Choose Teacher or Bus Driver for staff sign-up.");
            return "redirect:/signup";
        }

        if (!normalizedUsername.matches("[A-Za-z0-9._-]{3,30}")) {
            redirect.addFlashAttribute("error", "Username must be 3–30 characters using letters, numbers, dots, underscores, or hyphens.");
            return "redirect:/signup";
        }
        if (normalizedName.isEmpty() || normalizedName.length() > 100) {
            redirect.addFlashAttribute("error", "Enter a name of up to 100 characters.");
            return "redirect:/signup";
        }
        if (accountRole == Role.TEACHER && (subject == null || subject.trim().isEmpty())) {
            redirect.addFlashAttribute("error", "Enter the teacher's subject.");
            return "redirect:/signup";
        }
        int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (password.length() < 10 || passwordBytes > 72) {
            redirect.addFlashAttribute("error", "Password must be at least 10 characters and no more than 72 UTF-8 bytes.");
            return "redirect:/signup";
        }
        if (!password.equals(confirmPassword)) {
            redirect.addFlashAttribute("error", "The passwords do not match.");
            return "redirect:/signup";
        }
        if (users.findByUsername(normalizedUsername).isPresent()) {
            redirect.addFlashAttribute("error", "That username is already in use.");
            return "redirect:/signup";
        }

        String teacherSubject = accountRole == Role.TEACHER ? subject.trim() : null;
        users.save(new UserAccount(normalizedUsername, passwordEncoder.encode(password),
                normalizedName, accountRole, null, teacherSubject, false));
        auditLogs.record(normalizedUsername, "STAFF_SIGNUP_REQUESTED", "Staff management",
                "Sign-up request submitted for " + accountRole + ".");
        redirect.addFlashAttribute("message", "Sign-up request sent. An administrator must approve your account before you can sign in.");
        return "redirect:/login";
    }

    @PostMapping("/admin/users/approve")
    @Transactional
    public String approve(@RequestParam Long userId,
                          org.springframework.security.core.Authentication authentication,
                          RedirectAttributes redirect) {
        UserAccount account = users.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Account not found."));
        if (account.isEnabled() || (account.getRole() != Role.TEACHER && account.getRole() != Role.BUS_DRIVER)) {
            redirect.addFlashAttribute("error", "Only pending teacher and bus-driver sign-ups can be approved.");
            return "redirect:/admin";
        }
        account.enable();
        auditLogs.record(authentication.getName(), "STAFF_SIGNUP_APPROVED", "Staff management",
                "Approved account " + account.getUsername() + ".");
        redirect.addFlashAttribute("message", "Staff sign-up approved.");
        return "redirect:/admin";
    }
}
