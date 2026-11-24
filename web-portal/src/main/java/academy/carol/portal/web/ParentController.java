package academy.carol.portal.web;

import academy.carol.portal.domain.FeeAccount;
import academy.carol.portal.domain.FeePayment;
import academy.carol.portal.domain.Student;
import academy.carol.portal.domain.UserAccount;
import academy.carol.portal.repository.AcademicRecordRepository;
import academy.carol.portal.repository.FeeAccountRepository;
import academy.carol.portal.repository.FeePaymentRepository;
import academy.carol.portal.repository.StudentRepository;
import academy.carol.portal.repository.UserAccountRepository;
import academy.carol.portal.service.AuditLogService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ParentController {
    private final UserAccountRepository users;
    private final StudentRepository students;
    private final AcademicRecordRepository academicRecords;
    private final FeeAccountRepository feeAccounts;
    private final FeePaymentRepository payments;
    private final AuditLogService auditLogs;

    public ParentController(UserAccountRepository users, StudentRepository students,
                            AcademicRecordRepository academicRecords, FeeAccountRepository feeAccounts,
                            FeePaymentRepository payments, AuditLogService auditLogs) {
        this.users = users;
        this.students = students;
        this.academicRecords = academicRecords;
        this.feeAccounts = feeAccounts;
        this.payments = payments;
        this.auditLogs = auditLogs;
    }

    @GetMapping("/parent")
    public String dashboard(Model model, Authentication authentication) {
        Student student = linkedStudent(authentication.getName());
        FeeAccount feeAccount = feeAccounts.findByStudent_Id(student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee account not found."));
        model.addAttribute("student", student);
        model.addAttribute("records", academicRecords.findForStudentWithStudent(student.getId()));
        model.addAttribute("feeAccount", feeAccount);
        model.addAttribute("payments", payments.findByFeeAccount_IdOrderByCreatedAtDesc(feeAccount.getId()));
        return "parent";
    }

    @PostMapping("/parent/pay")
    @Transactional
    public String recordPayment(@RequestParam BigDecimal amount, Authentication authentication,
                                   RedirectAttributes redirect) {
        Student student = linkedStudent(authentication.getName());
        FeeAccount feeAccount = feeAccounts.findByStudent_Id(student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee account not found."));

        if (amount == null || amount.signum() <= 0) {
            redirect.addFlashAttribute("error", "Enter an amount greater than zero.");
            return "redirect:/parent";
        }
        if (amount.scale() > 2 || amount.compareTo(feeAccount.getBalance()) > 0) {
            redirect.addFlashAttribute("error", "Amount must be in KES, up to 2 decimal places, and no greater than the balance.");
            return "redirect:/parent";
        }

        feeAccount.addPayment(amount);
        String reference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        FeePayment payment = new FeePayment(feeAccount, amount, LocalDateTime.now(), reference);
        payments.save(payment);
        auditLogs.record(authentication.getName(), "PAYMENT_RECORDED", "Fees",
                "Parent payment for " + student.getStudentCode() + ": KES " + amount + "; receipt " + reference + ".");
        redirect.addFlashAttribute("message", "Payment recorded.");
        return "redirect:/parent";
    }

    private Student linkedStudent(String username) {
        UserAccount parent = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (parent.getStudentCode() == null || parent.getStudentCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No student is linked to this parent account.");
        }
        return students.findByStudentCode(parent.getStudentCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linked student was not found."));
    }
}
