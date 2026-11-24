package academy.carol.portal.web;

import academy.carol.portal.repository.AuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditLogController {
    private final AuditLogRepository logs;

    public AuditLogController(AuditLogRepository logs) {
        this.logs = logs;
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", logs.findTop200ByOrderByCreatedAtDesc());
        model.addAttribute("failedLogins", logs.countByAction("LOGIN_FAILED"));
        return "logs";
    }
}
