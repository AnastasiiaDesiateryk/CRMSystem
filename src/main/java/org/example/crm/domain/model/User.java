package org.example.crm.domain.model;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private String name;
    private Set<String> roles;
    private boolean hasAccess;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public boolean isHasAccess() { return hasAccess; }
    public void setHasAccess(boolean hasAccess) { this.hasAccess = hasAccess; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
