package academy.carol.portal.web;

import academy.carol.portal.domain.BusAssignment;
import academy.carol.portal.domain.UserAccount;
import academy.carol.portal.repository.BusAssignmentRepository;
import academy.carol.portal.repository.UserAccountRepository;
import academy.carol.portal.service.AuditLogService;
import java.time.LocalTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BusDriverController {
    private final UserAccountRepository users;
    private final BusAssignmentRepository buses;
    private final AuditLogService auditLogs;

    public BusDriverController(UserAccountRepository users, BusAssignmentRepository buses,
                               AuditLogService auditLogs) {
        this.users = users;
        this.buses = buses;
        this.auditLogs = auditLogs;
    }

    @GetMapping("/driver")
    public String dashboard(Model model, Authentication authentication) {
        UserAccount account = currentAccount(authentication.getName());
        BusAssignment bus = currentBus(authentication.getName());
        model.addAttribute("displayName", account.getDisplayName());
        model.addAttribute("bus", bus);
        return "driver";
    }

    @PostMapping("/driver/check-in")
    @Transactional
    public String checkIn(Authentication authentication, RedirectAttributes redirect) {
        BusAssignment bus = currentBus(authentication.getName());
        bus.checkIn(LocalTime.now());
        auditLogs.record(authentication.getName(), "DRIVER_CHECK_IN", "Transport attendance",
                "Driver checked in for bus " + bus.getBusCode() + ".");
        redirect.addFlashAttribute("message", "Check-in recorded.");
        return "redirect:/driver";
    }

    @PostMapping("/driver/check-out")
    @Transactional
    public String checkOut(Authentication authentication, RedirectAttributes redirect) {
        BusAssignment bus = currentBus(authentication.getName());
        if (bus.getCheckIn() == null) {
            redirect.addFlashAttribute("error", "Check in before checking out.");
            return "redirect:/driver";
        }
        bus.checkOut(LocalTime.now());
        auditLogs.record(authentication.getName(), "DRIVER_CHECK_OUT", "Transport attendance",
                "Driver checked out for bus " + bus.getBusCode() + ".");
        redirect.addFlashAttribute("message", "Check-out recorded.");
        return "redirect:/driver";
    }

    private UserAccount currentAccount(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    private BusAssignment currentBus(String username) {
        return buses.findByDriverUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No bus is assigned to this driver."));
    }
}
