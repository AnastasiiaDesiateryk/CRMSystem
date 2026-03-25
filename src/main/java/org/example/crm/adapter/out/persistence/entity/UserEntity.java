package org.example.crm.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity mapped to table "users".
 *
 * TECHNICAL ROLE:
 * - represents database structure
 * - used ONLY inside persistence adapter
 *
 * IMPORTANT:
 * - MUST NOT leak into application or domain layers
 * - contains JPA annotations and persistence concerns
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(name = "has_access", nullable = false)
    private boolean hasAccess;

    /**
     * Roles are stored in separate table: user_roles
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", nullable = false)
    private Set<String> roles = new HashSet<>();

    @Column(nullable = true, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = true, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    /* =======================
       Getters / setters
       ======================= */

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isHasAccess() { return hasAccess; }

    public void setHasAccess(boolean hasAccess) { this.hasAccess = hasAccess; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
