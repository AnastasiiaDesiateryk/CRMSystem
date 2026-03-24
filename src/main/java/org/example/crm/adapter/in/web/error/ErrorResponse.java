package org.example.crm.adapter.in.web.error;

/**
 * Standard HTTP error payload.
 *
 * Enterprise rules:
 * - immutable (no setters)
 * - stable machine-readable "code"
 * - safe human-readable "message"
 * - "status" duplicated in body for client consistency
 */
public final class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;

    public ErrorResponse(int status, String code, String message) {
        this.status = status;
        this.code = normalize(code, "internal_error");
        this.message = normalize(message, "Unexpected error");
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }

    private static String normalize(String v, String fallback) {
        if (v == null) return fallback;
        String t = v.trim();
        return t.isEmpty() ? fallback : t;
    }
}
