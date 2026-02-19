package com.labjb.cms.shared.errors.exception;

public class JwtTokenMissingException extends RuntimeException {
    
    public JwtTokenMissingException(String message) {
        super(message);
    }
}
