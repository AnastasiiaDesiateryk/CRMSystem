package org.example.crm.application.dto;


public class RefreshRequest {
    private String refreshToken; // format: "<uuid>.<raw>"

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
