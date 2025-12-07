package com.central.authentication_service.service;

import com.central.authentication_service.exception.*;
import com.central.authentication_service.repository.UserRepository;
import com.central.authentication_service.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static com.central.authentication_service.utils.ServiceUtils.constructLoginResponse;

/**
 * Implementation of the {@link AuthService} interface.
 * Handles the core authentication logic including user login and token validation.
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Constructs a new AuthServiceImpl with the required dependencies.
     *
     * @param repository The user repository for database operations
     * @param passwordEncoder The password encoder for hashing and verifying passwords
     * @param jwtUtil The JWT utility for token operations
     */
    @Autowired
    public AuthServiceImpl(UserRepository repository, 
                          PasswordEncoder passwordEncoder, 
                          JwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and generates a JWT token upon successful authentication.
     *
     * @param loginRequest The login request containing user credentials
     * @return ResponseEntity containing the JWT token if authentication is successful
     * @throws IllegalArgumentException if the request is null or missing required fields
     * @throws UserDoesNotExistException if no user is found with the provided email
     * @throws InvalidJWTTokenException if the credentials are invalid or authentication fails
     */
    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        // Validate input parameters
        if (loginRequest == null) {
            log.warn("Login attempt with null request");
            throw new InvalidInputException("Login request cannot be null. Please provide valid login credentials.");
        }
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            log.warn("Login attempt with empty email");
            throw new InvalidInputException("Email address is required. Please enter a valid email address.");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            log.warn("Login attempt with empty password for email: {}", loginRequest.getEmail());
            throw new InvalidInputException("Password is required. Please enter your password.");
        }

        try {
            log.debug("Attempting to authenticate user with email: {}", loginRequest.getEmail());
            
            // Verify user exists
            var user = repository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.info("Login failed - User not found for email: {}", loginRequest.getEmail());
                        return new UserDoesNotExistException("Invalid email or password");
                    });

            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Login failed - Invalid password for email: {}", loginRequest.getEmail());
                throw new InvalidInputException("Authentication failed. The email address or password you entered is incorrect. Please check your credentials and try again.");
            }

            // Generate and return JWT token
            String token = jwtUtil.generateToken(user.getUserCode(), String.valueOf(user.getRole()));
            LoginResponse response = constructLoginResponse(token);
            log.info("User authenticated successfully: {}", user.getUserCode());
            return response;

        } catch (UserDoesNotExistException | InvalidInputException e) {
            // Re-throw specific exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for email: {}", loginRequest.getEmail(), e);
            throw new InvalidInputException("Authentication service is currently unavailable. Please try again later. If the problem persists, please contact support.");
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token to validate (with or without 'Bearer ' prefix)
     * @return ResponseEntity with HTTP 200 OK if token is valid
     * @throws InvalidJWTTokenException if the token is invalid, expired, or malformed
     */
    @Override
    public Boolean validateToken(String token) {
        log.debug("Validating authentication token");
        
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Invalid or missing Bearer token in Authorization header");
            throw new InvalidJWTTokenException("Missing or invalid Authorization header");
        }

        try {
            String jwtToken = token.substring(7); // Remove 'Bearer ' prefix
            if (jwtUtil.validateToken(jwtToken)) {
                log.debug("Token validation successful");
                return true;
            }
            log.warn("Token validation failed - Invalid token");
            throw new InvalidJWTTokenException("Invalid JWT token");
        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new InvalidJWTTokenException("Invalid JWT token: " + e.getMessage());
        }
    }
}
