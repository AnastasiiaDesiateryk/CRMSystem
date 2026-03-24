package org.example.crm.application.port.out;



public interface RefreshTokenGenerator {
    /** cryptographically secure random token for client storage */
    String generateRaw();
}
