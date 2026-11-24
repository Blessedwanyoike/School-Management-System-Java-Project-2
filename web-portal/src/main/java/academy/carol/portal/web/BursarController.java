package academy.carol.portal.web;

import academy.carol.portal.domain.FeeAccount;
import academy.carol.portal.domain.FeePayment;
import academy.carol.portal.domain.Student;
import academy.carol.portal.repository.FeeAccountRepository;
import academy.carol.portal.repository.FeePaymentRepository;
import academy.carol.portal.repository.StudentRepository;
import academy.carol.portal.service.AuditLogService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BursarController {
    private static final int MAX_RESULTS = 20;
    private static final int MAX_SEARCH_LENGTH = 100;

    private final StudentRepository students;
    private final FeeAccountRepository feeAccounts;
    private final FeePaymentRepository payments;
    private final AuditLogService auditLogs;

    public BursarController(StudentRepository students, FeeAccountRepository feeAccounts,
                             FeePaymentRepository payments, AuditLogService auditLogs) {
        this.students = students;
        this.feeAccounts = feeAccounts;
        this.payments = payments;
        this.auditLogs = auditLogs;
    }

    @GetMapping("/bursar")
    public String dashboard(@RequestParam(required = false) String q, Model model) {
        String query = q == null ? "" : q.trim();
        model.addAttribute("query", query);
        model.addAttribute("results", query.isEmpty()
                ? Collections.emptyList()
                : students.searchByAdmissionOrName(query, PageRequest.of(0, MAX_RESULTS)));
        model.addAttribute("selectedStudent", null);
        model.addAttribute("feeAccount", null);
        model.addAttribute("payments", Collections.emptyList());

        if (!query.isEmpty() && query.length() <= MAX_SEARCH_LENGTH) {
            students.findByStudentCode(query).ifPresent(student -> loadStudent(model, student));
        }
        return "bursar";
    }

    @PostMapping("/bursar/payments")
    @Transactional
    public String recordPayment(@RequestParam String admissionNumber,
                                @RequestParam BigDecimal amount,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        String normalizedAdmissionNumber = admissionNumber == null ? "" : admissionNumber.trim();
        if (normalizedAdmissionNumber.isEmpty() || normalizedAdmissionNumber.length() > MAX_SEARCH_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid admission number is required.");
        }

        Student student = students.findByStudentCode(normalizedAdmissionNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        FeeAccount feeAccount = feeAccounts.findByStudent_Id(student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee account not found."));

        if (amount == null || amount.signum() <= 0) {
            redirect.addFlashAttribute("error", "Enter an amount greater than zero.");
            return "redirect:/bursar?q=" + normalizedAdmissionNumber;
        }
        if (amount.scale() > 2 || amount.compareTo(feeAccount.getBalance()) > 0) {
            redirect.addFlashAttribute("error",
                    "Amount must be in KES, up to 2 decimal places, and no greater than the balance.");
            return "redirect:/bursar?q=" + normalizedAdmissionNumber;
        }

        feeAccount.addPayment(amount);
        String reference = "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payments.save(new FeePayment(feeAccount, amount, LocalDateTime.now(), reference));
        auditLogs.record(authentication.getName(), "PAYMENT_RECORDED", "Fees",
                "Bursar payment for " + student.getStudentCode() + ": KES " + amount + "; receipt " + reference + ".");
        redirect.addFlashAttribute("message", "Fee payment recorded and receipt created.");
        return "redirect:/bursar?q=" + normalizedAdmissionNumber;
    }

    private void loadStudent(Model model, Student student) {
        FeeAccount feeAccount = feeAccounts.findByStudent_Id(student.getId()).orElse(null);
        model.addAttribute("selectedStudent", student);
        model.addAttribute("feeAccount", feeAccount);
        model.addAttribute("payments", feeAccount == null
                ? Collections.emptyList()
                : payments.findByFeeAccount_IdOrderByCreatedAtDesc(feeAccount.getId()));
    }
}
