package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "portal_users")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String studentCode;
    private String subject;

    private LocalTime checkIn;
    private LocalTime checkOut;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;

    protected UserAccount() {
    }

    public UserAccount(String username, String passwordHash, String displayName, Role role,
                       String studentCode, String subject) {
        this(username, passwordHash, displayName, role, studentCode, subject, true);
    }

    public UserAccount(String username, String passwordHash, String displayName, Role role,
                       String studentCode, String subject, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.studentCode = studentCode;
        this.subject = subject;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public Role getRole() { return role; }
    public String getStudentCode() { return studentCode; }
    public String getSubject() { return subject; }
    public boolean isEnabled() { return enabled; }
    public LocalTime getCheckIn() { return checkIn; }
    public LocalTime getCheckOut() { return checkOut; }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void enable() {
        this.enabled = true;
    }

    public void updateSeedAccount(String displayName, Role role, String studentCode, String subject,
                                  boolean enabled, String passwordHash) {
        this.displayName = displayName;
        this.role = role;
        this.studentCode = studentCode;
        this.subject = subject;
        this.enabled = enabled;
        this.passwordHash = passwordHash;
    }

    public void checkIn(LocalTime time) {
        this.checkIn = time;
        this.checkOut = null;
    }

    public void checkOut(LocalTime time) {
        this.checkOut = time;
    }
}
