package com.clinic.exception;

/**
 * Exception used to indicate a business rule violation.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}