package com.central.authentication_service.exception;

/**
 * Exception thrown when invalid input is provided that violates business rules or constraints.
 * This is typically used for user-facing validation errors.
 */
public class InvalidInputException extends RuntimeException {

    /**
     * Constructs a new InvalidInputException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidInputException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}