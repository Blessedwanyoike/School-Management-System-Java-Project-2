package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "bus_assignments")
public class BusAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String driverUsername;

    @Column(nullable = false)
    private String busCode;

    @Column(nullable = false)
    private String registration;

    @Column(nullable = false)
    private String route;

    @Column(nullable = false)
    private boolean active;

    private LocalTime checkIn;
    private LocalTime checkOut;
    private LocalTime expectedBack;
    private String maintenanceStatus = "Serviceable";
    private String maintenanceNotes;

    protected BusAssignment() {
    }

    public BusAssignment(String driverUsername, String busCode, String registration, String route, boolean active) {
        this.driverUsername = driverUsername;
        this.busCode = busCode;
        this.registration = registration;
        this.route = route;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getDriverUsername() { return driverUsername; }
    public String getBusCode() { return busCode; }
    public String getRegistration() { return registration; }
    public String getRoute() { return route; }
    public boolean isActive() { return active; }
    public LocalTime getCheckIn() { return checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public LocalTime getExpectedBack() { return expectedBack; }
    public String getMaintenanceStatus() { return maintenanceStatus; }
    public String getMaintenanceNotes() { return maintenanceNotes; }

    public void checkIn(LocalTime time) {
        checkIn = time;
        checkOut = null;
    }

    public void checkOut(LocalTime time) {
        checkOut = time;
    }

    public void updateMaintenance(String status, String notes) {
        maintenanceStatus = status;
        maintenanceNotes = notes;
    }
}
