package org.example.crm.application.port.in;

import org.example.crm.application.dto.*;

public interface AuthServicePort {
    TokenResponse register(RegisterRequest request, String userAgent, String ip);
    TokenResponse login(LoginRequest request, String userAgent, String ip);
    TokenResponse refresh(RefreshRequest request, String userAgent, String ip);
    void logout(LogoutRequest request, String userAgent, String ip);
}
