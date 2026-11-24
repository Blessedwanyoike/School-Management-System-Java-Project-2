package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "academic_records")
public class AcademicRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 600)
    private String teacherComment;

    protected AcademicRecord() {
    }

    public AcademicRecord(Student student, String term, String subject, BigDecimal score, String teacherComment) {
        this.student = student;
        this.term = term;
        this.subject = subject;
        this.score = score;
        this.teacherComment = teacherComment;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getTerm() { return term; }
    public String getSubject() { return subject; }
    public BigDecimal getScore() { return score; }
    public String getTeacherComment() { return teacherComment; }
}
