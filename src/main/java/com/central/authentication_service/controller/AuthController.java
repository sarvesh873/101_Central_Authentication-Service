package com.central.authentication_service.controller;

import com.central.authentication_service.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.AuthApi;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling authentication-related HTTP requests.
 * Implements the AuthApi interface generated from OpenAPI specification.
 * Delegates business logic to the AuthService.
 */
@RestController
@Slf4j
public class AuthController implements AuthApi {

    private final AuthService authService;

    /**
     * Constructs a new AuthController with the specified AuthService.
     *
     * @param authService The authentication service to delegate business logic to
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user login requests.
     *
     * @param loginRequest The login request containing user credentials
     * @return ResponseEntity containing JWT token and user details if authentication is successful
     */
    @Override
    public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest) {
        log.debug("Received login request for email: {}", loginRequest.getEmail());
        return authService.loginUser(loginRequest);
    }

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token to validate (with or without 'Bearer ' prefix)
     * @return ResponseEntity with HTTP 200 OK if token is valid
     */
    @Override
    public ResponseEntity<Void> validateToken(String token) {
        log.debug("Validating authentication token");
        return authService.validateToken(token);
    }
}
