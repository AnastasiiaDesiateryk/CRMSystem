package org.example.crm.domain.model;


import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class CustomField {
    private UUID id;
    private String entityType;
    private String key;
    private String fieldType;
    private String optionsJson;
    private boolean required;
    private Instant createdAt;

    public CustomField() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomField that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}