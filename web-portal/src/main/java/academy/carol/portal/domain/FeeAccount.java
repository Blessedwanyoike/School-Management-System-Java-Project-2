package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "fee_accounts")
public class FeeAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFees;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Version
    private long version;

    protected FeeAccount() {
    }

    public FeeAccount(Student student, BigDecimal totalFees, BigDecimal amountPaid) {
        this.student = student;
        this.totalFees = totalFees;
        this.amountPaid = amountPaid;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public BigDecimal getTotalFees() { return totalFees; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getBalance() { return totalFees.subtract(amountPaid).max(BigDecimal.ZERO); }

    public void addPayment(BigDecimal amount) {
        amountPaid = amountPaid.add(amount);
    }
}
