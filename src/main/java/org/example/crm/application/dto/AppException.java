package org.example.crm.application.dto;

/**
 * Базовое исключение application layer.
 * code — стабильный машинный код для фронта/логов.
 */
public class AppException extends RuntimeException {
    private final String code;

    public AppException(String code) {
        super(code);
        this.code = code;
    }

    public AppException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
