package com.labjb.cms.shared.errors.exception;

public class JwtTokenInvalidException extends RuntimeException {
    
    public JwtTokenInvalidException(String message) {
        super(message);
    }
}
