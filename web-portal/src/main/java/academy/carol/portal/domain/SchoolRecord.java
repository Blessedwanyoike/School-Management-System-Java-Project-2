package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_records")
public class SchoolRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String reference;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected SchoolRecord() {
    }

    public SchoolRecord(String section, String title, String reference, String details, String status) {
        this.section = section;
        this.title = title;
        this.reference = reference;
        this.details = details;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSection() { return section; }
    public String getTitle() { return title; }
    public String getReference() { return reference; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
