package com.labjb.cms.shared.errors.exception;

public class EntityAlreadyException extends RuntimeException{
    public EntityAlreadyException(String message) {
        super(message);
    }
}
