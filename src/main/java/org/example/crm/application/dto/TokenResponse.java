package org.example.crm.application.dto;

public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
    private final long expiresInSeconds;

    public TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
}
