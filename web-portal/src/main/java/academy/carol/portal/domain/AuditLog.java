package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 120)
    private String section;

    @Column(nullable = false, length = 2000)
    private String details;

    @Column(length = 64)
    private String clientAddress;

    protected AuditLog() {
    }

    public AuditLog(String actor, String action, String section, String details, String clientAddress) {
        this.createdAt = LocalDateTime.now();
        this.actor = actor;
        this.action = action;
        this.section = section;
        this.details = details;
        this.clientAddress = clientAddress;
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getSection() { return section; }
    public String getDetails() { return details; }
    public String getClientAddress() { return clientAddress; }
}
