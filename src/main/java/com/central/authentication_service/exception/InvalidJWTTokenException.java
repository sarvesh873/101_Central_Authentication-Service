package com.central.authentication_service.exception;

/**
 * Exception thrown when an invalid or expired JWT token is encountered.
 * This exception is typically thrown during token validation when:
 * - The token signature is invalid
 * - The token has expired
 * - The token format is incorrect
 * - The token is missing required claims
 * 
 * This exception results in an HTTP 401 Unauthorized response when handled by
 * the {@link GlobalExceptionHandler}.
 */
public class InvalidJWTTokenException extends RuntimeException {
    
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message that describes the specific error
     */
    public InvalidJWTTokenException(String message) {
        super(message);
    }
}
