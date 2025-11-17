package com.central.authentication_service.service;

import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service interface for handling authentication-related operations.
 * Defines the contract for user authentication and token validation.
 */
@Service
public interface AuthService {

    /**
     * Authenticates a user and generates a JWT token upon successful authentication.
     *
     * @param loginRequest The login request containing user credentials (email and password)
     * @return ResponseEntity containing the JWT token and user details if authentication is successful
     * @throws com.central.authentication_service.exception.UserDoesNotExistException if user is not found
     * @throws com.central.authentication_service.exception.InvalidJWTTokenException if credentials are invalid
     */
    ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest);

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token to validate (with or without 'Bearer ' prefix)
     * @return ResponseEntity with HTTP 200 OK if token is valid
     * @throws com.central.authentication_service.exception.InvalidJWTTokenException if token is invalid or expired
     */
    ResponseEntity<Void> validateToken(String token);
}
