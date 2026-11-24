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
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_payments")
public class FeePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_account_id", nullable = false)
    private FeeAccount feeAccount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, unique = true)
    private String reference;

    protected FeePayment() {
    }

    public FeePayment(FeeAccount feeAccount, BigDecimal amount, LocalDateTime createdAt, String reference) {
        this.feeAccount = feeAccount;
        this.amount = amount;
        this.createdAt = createdAt;
        this.reference = reference;
    }

    public Long getId() { return id; }
    public FeeAccount getFeeAccount() { return feeAccount; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getReference() { return reference; }
}
