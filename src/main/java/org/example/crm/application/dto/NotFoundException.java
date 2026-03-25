package org.example.crm.application.dto;

public class NotFoundException extends AppException {
    public NotFoundException(String code) { super(code); }
}
