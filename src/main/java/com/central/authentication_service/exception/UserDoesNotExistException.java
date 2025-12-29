package com.central.authentication_service.exception;

/**
 * Exception thrown when a requested user cannot be found in the system.
 * This exception is typically thrown when:
 * - Looking up a user by ID, email, or username that doesn't exist
 * - Attempting to update or delete a non-existent user
 * - Authenticating with invalid credentials
 * 
 * This exception results in an HTTP 404 Not Found response when handled by
 * the {@link GlobalExceptionHandler}.
 */
public class UserDoesNotExistException extends RuntimeException {
    
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message that describes which user was not found
     */
    public UserDoesNotExistException(String message) {
        super(message);
    }
}
