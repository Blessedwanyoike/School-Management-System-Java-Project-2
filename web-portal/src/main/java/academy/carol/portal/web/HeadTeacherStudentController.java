package academy.carol.portal.web;

import academy.carol.portal.domain.FeeAccount;
import academy.carol.portal.domain.Student;
import academy.carol.portal.repository.AcademicRecordRepository;
import academy.carol.portal.repository.FeeAccountRepository;
import academy.carol.portal.repository.FeePaymentRepository;
import academy.carol.portal.repository.StudentRepository;
import java.util.Collections;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/head-teacher/students")
public class HeadTeacherStudentController {
    private static final int MAX_SEARCH_LENGTH = 100;
    private static final int MAX_RESULTS = 50;

    private final StudentRepository students;
    private final AcademicRecordRepository academicRecords;
    private final FeeAccountRepository feeAccounts;
    private final FeePaymentRepository payments;

    public HeadTeacherStudentController(StudentRepository students,
                                        AcademicRecordRepository academicRecords,
                                        FeeAccountRepository feeAccounts,
                                        FeePaymentRepository payments) {
        this.students = students;
        this.academicRecords = academicRecords;
        this.feeAccounts = feeAccounts;
        this.payments = payments;
    }

    @GetMapping
    public String search(@RequestParam(required = false) String q, Model model) {
        String query = q == null ? "" : q.trim();
        model.addAttribute("query", query);
        model.addAttribute("results", Collections.emptyList());

        if (query.length() > MAX_SEARCH_LENGTH) {
            model.addAttribute("error", "Search text must be 100 characters or fewer.");
        } else if (!query.isEmpty()) {
            model.addAttribute("results",
                    students.searchByAdmissionOrName(query, PageRequest.of(0, MAX_RESULTS)));
        }
        return "student-search";
    }

    @GetMapping("/report")
    public String report(@RequestParam String admissionNumber, Model model) {
        String normalizedAdmissionNumber = admissionNumber.trim();
        if (normalizedAdmissionNumber.isEmpty() || normalizedAdmissionNumber.length() > MAX_SEARCH_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid admission number is required.");
        }

        Student student = students.findByStudentCode(normalizedAdmissionNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        FeeAccount feeAccount = feeAccounts.findByStudent_Id(student.getId()).orElse(null);
        model.addAttribute("student", student);
        model.addAttribute("records", academicRecords.findForStudentWithStudent(student.getId()));
        model.addAttribute("feeAccount", feeAccount);
        model.addAttribute("payments", feeAccount == null
                ? Collections.emptyList()
                : payments.findByFeeAccount_IdOrderByCreatedAtDesc(feeAccount.getId()));
        return "student-report";
    }
}
