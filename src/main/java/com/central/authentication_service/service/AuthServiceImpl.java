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
    public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest) {
        // Validate input parameters
        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            log.warn("Login attempt with missing credentials");
            throw new IllegalArgumentException("Email and password are required");
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
                log.info("Login failed - Invalid password for email: {}", loginRequest.getEmail());
                throw new InvalidJWTTokenException("Invalid email or password");
            }

            // Generate and return JWT token
            String token = jwtUtil.generateToken(user.getUserCode(), String.valueOf(user.getRole()));
            LoginResponse response = constructLoginResponse(token);
            log.info("User authenticated successfully: {}", user.getUserCode());
            return ResponseEntity.ok(response);

        } catch (UserDoesNotExistException | InvalidJWTTokenException e) {
            // Re-throw specific exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for email: {}", loginRequest.getEmail(), e);
            throw new InvalidJWTTokenException("Authentication failed: " + e.getMessage());
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
    public ResponseEntity<Void> validateToken(String token) {
        log.debug("Validating authentication token");
        
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Invalid or missing Bearer token in Authorization header");
            throw new InvalidJWTTokenException("Missing or invalid Authorization header");
        }

        try {
            String jwtToken = token.substring(7); // Remove 'Bearer ' prefix
            if (jwtUtil.validateToken(jwtToken)) {
                log.debug("Token validation successful");
                return ResponseEntity.ok().build();
            }
            log.warn("Token validation failed - Invalid token");
            throw new InvalidJWTTokenException("Invalid JWT token");
        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new InvalidJWTTokenException("Invalid JWT token: " + e.getMessage());
        }
    }
}
