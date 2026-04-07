package org.example.crm.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "organizations",
        indexes = {
                @Index(name = "idx_org_name", columnList = "name"),
                @Index(name = "idx_org_category", columnList = "category"),
                @Index(name = "idx_org_status", columnList = "status"),
                @Index(name = "idx_org_country_region", columnList = "country_region")
        }
)
public class OrganizationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "website", length = 500)
    private String website;

    // working / not-working (можно потом заменить на enum + @Enumerated)
    @Column(name = "website_status", length = 50)
    private String websiteStatus;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "country_region", length = 200)
    private String countryRegion;

    @Column(name = "email", length = 320)
    private String email;

    // additive-manufacturing, ...
    @Column(name = "category", length = 120)
    private String category;

    // active, ...
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "preferred_language", length = 5)
    private String preferredLanguage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "organization",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<ContactEntity> contacts = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // getters/setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getWebsiteStatus() { return websiteStatus; }
    public void setWebsiteStatus(String websiteStatus) { this.websiteStatus = websiteStatus; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getCountryRegion() { return countryRegion; }
    public void setCountryRegion(String countryRegion) { this.countryRegion = countryRegion; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ContactEntity> getContacts() { return contacts; }
    public void setContacts(List<ContactEntity> contacts) { this.contacts = contacts; }
}
