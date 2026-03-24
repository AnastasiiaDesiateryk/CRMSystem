package org.example.crm.application.dto;

public class PreconditionFailedException extends AppException {
    public PreconditionFailedException(String code) { super(code); }
}
