package academy.carol.portal.web;

import academy.carol.portal.domain.SchoolRecord;
import academy.carol.portal.repository.SchoolRecordRepository;
import academy.carol.portal.service.AuditLogService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SchoolModuleController {
    private static final Map<String, String> MODULE_NAMES = new LinkedHashMap<>();

    static {
        MODULE_NAMES.put("profiles", "Student Profiles & Guardian Contacts");
        MODULE_NAMES.put("cbc", "CBC Assessment & Competency Rubrics");
        MODULE_NAMES.put("attendance", "Daily Student & Teacher Attendance");
        MODULE_NAMES.put("communication", "Parent-Teacher Communication & Bulk SMS");
        MODULE_NAMES.put("transport", "School Transport & Route Tracking");
        MODULE_NAMES.put("boarding", "Boarding & Dormitory Management");
        MODULE_NAMES.put("library", "Library & Textbook Issuing");
        MODULE_NAMES.put("health", "Health, Allergy & Sickbay Records");
        MODULE_NAMES.put("staff", "Staff Management & Payroll");
        MODULE_NAMES.put("kitchen", "Kitchen, Uniform & Store Inventory");
        MODULE_NAMES.put("timetable", "Class & Lesson Timetabling");
        MODULE_NAMES.put("accounting", "Accounting & Expense Management");
        MODULE_NAMES.put("admissions", "Student Admissions & NEMIS/UPI Tracking");
    }

    private final SchoolRecordRepository records;
    private final AuditLogService auditLogs;

    public SchoolModuleController(SchoolRecordRepository records, AuditLogService auditLogs) {
        this.records = records;
        this.auditLogs = auditLogs;
    }

    @GetMapping("/modules")
    public String modules(Model model) {
        model.addAttribute("modules", MODULE_NAMES);
        model.addAttribute("totalRecords", records.count());
        return "modules";
    }

    @GetMapping("/modules/{section}")
    public String module(@PathVariable String section, Model model) {
        requireSection(section);
        model.addAttribute("section", section);
        model.addAttribute("moduleName", MODULE_NAMES.get(section));
        model.addAttribute("records", records.findBySectionOrderByCreatedAtDesc(section));
        model.addAttribute("modules", MODULE_NAMES);
        return "module";
    }

    @PostMapping("/modules/{section}/add")
    @Transactional
    public String add(@PathVariable String section,
                      @RequestParam String title,
                      @RequestParam(required = false) String reference,
                      @RequestParam(required = false) String details,
                      @RequestParam(required = false) String status,
                      Authentication authentication,
                      RedirectAttributes redirect) {
        requireSection(section);
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty()) {
            redirect.addFlashAttribute("error", "A title is required.");
            return "redirect:/modules/" + section;
        }
        String cleanReference = nullToEmpty(reference);
        records.save(new SchoolRecord(section, cleanTitle,
                cleanReference, nullToEmpty(details),
                status == null || status.trim().isEmpty() ? "Active" : status.trim()));
        auditLogs.record(authentication.getName(),
                "attendance".equals(section) ? "ATTENDANCE_RECORDED" : "MODULE_RECORD_CREATED",
                MODULE_NAMES.get(section), cleanTitle
                        + (cleanReference.isEmpty() ? "" : " [" + cleanReference + "]"));
        redirect.addFlashAttribute("message", "Record saved.");
        return "redirect:/modules/" + section;
    }

    private void requireSection(String section) {
        if (!MODULE_NAMES.containsKey(section)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found.");
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
