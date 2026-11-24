package academy.carol.portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "school_assets")
public class SchoolAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int expectedQuantity;

    @Column(nullable = false)
    private int presentQuantity;

    @Column(nullable = false)
    private String status;

    private String notes;

    protected SchoolAsset() {
    }

    public SchoolAsset(String category, String location, String itemName,
                       int expectedQuantity, int presentQuantity, String status, String notes) {
        this.category = category;
        this.location = location;
        this.itemName = itemName;
        this.expectedQuantity = expectedQuantity;
        this.presentQuantity = presentQuantity;
        this.status = status;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getItemName() { return itemName; }
    public int getExpectedQuantity() { return expectedQuantity; }
    public int getPresentQuantity() { return presentQuantity; }
    public int getMissingQuantity() { return Math.max(0, expectedQuantity - presentQuantity); }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }

    public void update(int expectedQuantity, int presentQuantity, String status, String notes) {
        this.expectedQuantity = expectedQuantity;
        this.presentQuantity = presentQuantity;
        this.status = status;
        this.notes = notes;
    }
}
