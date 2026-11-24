package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "portal_students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentCode;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String grade;

    protected Student() {
    }

    public Student(String studentCode, String fullName, String grade) {
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public String getStudentCode() { return studentCode; }
    public String getFullName() { return fullName; }
    public String getGrade() { return grade; }

    public void changeAdmissionNumber(String admissionNumber) {
        this.studentCode = admissionNumber;
    }
}
