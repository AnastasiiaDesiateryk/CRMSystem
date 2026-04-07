package org.example.crm.adapter.in.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.example.crm.application.dto.AppException;
import org.example.crm.application.dto.ConflictException;
import org.example.crm.application.dto.NotFoundException;
import org.example.crm.application.dto.PreconditionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inbound Web Adapter concern:
 * - translate exceptions -> HTTP status + JSON payload
 * - log in a consistent, audit-friendly way
 *
 * IMPORTANT:
 * - application layer throws AppException (business errors)
 * - web layer decides HTTP mapping + payload shape
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest req) {
        int status = HttpStatus.METHOD_NOT_ALLOWED.value();
        LOG.log(Level.FINE, () -> "METHOD_NOT_ALLOWED path=" + safePath(req) + " msg=" + safeMsg(ex.getMessage()));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "method_not_allowed", "Method not allowed"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        int status = HttpStatus.NOT_FOUND.value();
        LOG.log(Level.FINE, () -> "NOT_FOUND path=" + safePath(req));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "not_found", "Not found"));
    }
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestPart(MissingServletRequestPartException ex,
                                                                  HttpServletRequest req) {
        int status = HttpStatus.BAD_REQUEST.value();
        LOG.log(Level.FINE, () -> "BAD_REQUEST path=" + safePath(req)
                + " msg=Missing request part: " + safeMsg(ex.getRequestPartName()));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "bad_request",
                        "Missing request part: " + safeMsg(ex.getRequestPartName())));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex,
                                                                  HttpServletRequest req) {
        int status = HttpStatus.BAD_REQUEST.value();
        LOG.log(Level.FINE, () -> "BAD_REQUEST path=" + safePath(req)
                + " msg=" + safeMsg(ex.getMessage()));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "bad_request", "Invalid multipart request"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                                                     HttpServletRequest req) {
        int status = HttpStatus.PAYLOAD_TOO_LARGE.value();
        LOG.log(Level.FINE, () -> "PAYLOAD_TOO_LARGE path=" + safePath(req));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "payload_too_large", "Uploaded file is too large"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest req) {
        int status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        LOG.log(Level.FINE, () -> "UNSUPPORTED_MEDIA_TYPE path=" + safePath(req)
                + " msg=" + safeMsg(ex.getMessage()));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "unsupported_media_type", "Unsupported media type"));
    }

    /**
     * Centralized business exception mapping.
     * This is the only place where business error -> HTTP status mapping happens.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        int status = HttpStatus.BAD_REQUEST.value();
        LOG.log(Level.FINE, () -> "BAD_REQUEST path=" + safePath(req) + " msg=" + safeMsg(ex.getMessage()));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "bad_request", safeMsg(ex.getMessage())));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(IllegalStateException ex, HttpServletRequest req) {
        int status = HttpStatus.UNAUTHORIZED.value();
        LOG.log(Level.FINE, () -> "UNAUTHORIZED path=" + safePath(req) + " msg=" + safeMsg(ex.getMessage()));
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "unauthorized", safeMsg(ex.getMessage())));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest req) {
        int status = mapStatus(ex);

        LOG.log(Level.WARNING, () -> String.format(
                "APP_ERROR status=%d code=%s path=%s msg=%s",
                status, ex.code(), safePath(req), safeMsg(ex.getMessage())
        ));

        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, ex.code(), safeMsg(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        LOG.log(Level.SEVERE, "UNEXPECTED_ERROR path=" + safePath(req), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "internal_error", "Unexpected internal error"));
    }

    private static int mapStatus(AppException ex) {
        if (ex instanceof NotFoundException) return HttpStatus.NOT_FOUND.value();
        if (ex instanceof PreconditionFailedException) return HttpStatus.PRECONDITION_FAILED.value();
        if (ex instanceof ConflictException) return HttpStatus.CONFLICT.value();
        return HttpStatus.BAD_REQUEST.value();
    }

    private static String safePath(HttpServletRequest req) {
        return req == null ? "<n/a>" : req.getRequestURI();
    }

    private static String safeMsg(String msg) {
        return msg == null ? "<null>" : msg;
    }
}
