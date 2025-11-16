package com.central.authentication_service.exception;

public class UserDoesNotExistException extends RuntimeException {
    
    public UserDoesNotExistException(String message) {
        super(message);
    }

}
