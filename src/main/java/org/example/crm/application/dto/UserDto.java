package org.example.crm.application.dto;



import java.util.Set;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private Set<String> roles;
    public UserDto() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}