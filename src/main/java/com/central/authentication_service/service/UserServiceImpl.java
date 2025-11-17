package com.central.authentication_service.service;

import com.central.authentication_service.exception.InvalidInputException;
import com.central.authentication_service.exception.UserDoesNotExistException;
import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import com.central.authentication_service.repository.UserRepository;
import com.central.authentication_service.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.central.authentication_service.utils.ServiceUtils.constructUserResponse;
import static com.central.authentication_service.utils.UserCodeUtil.generateUserCode;

/**
 * Implementation of the {@link UserService} interface.
 * Provides business logic for user management operations including creation, retrieval, and search.
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserServiceImpl with the required dependencies.
     *
     * @param repository The user repository for database operations
     * @param passwordEncoder The password encoder for hashing passwords
     */
    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with the provided details.
     *
     * @param request The request containing user details to be created
     * @return ResponseEntity containing the created user's details and HTTP status 201 (Created)
     * @throws IllegalArgumentException if the request is null or contains invalid data
     */
    @Override
    @Transactional
    public ResponseEntity<UserResponse> createUser(CentralRequest<CreateUserRequest> request) {
        if (request == null || request.getT() == null) {
            throw new IllegalArgumentException("User creation request cannot be null");
        }

        String email = request.getT().getEmail();
        if (repository.existsByEmail(email)) {
            log.warn("Attempted to create user with existing email: {}", email);
            throw new InvalidInputException("A user with this email already exists");
        }

        String username = request.getT().getUsername();
        log.info("Creating new user with username: {}", username);
        
        try {
            User user = User.builder()
                    .userCode(generateUserCode(username))
                    .username(username)
                    .email(email)
                    .role(Role.valueOf(request.getT().getRole()))
                    .password(passwordEncoder.encode(request.getT().getPassword()))
                    .build();

            User savedUser = repository.save(user);
            log.info("User created successfully with userCode: {}", savedUser.getUserCode());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(constructUserResponse(savedUser));
                    
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a user by their unique user code.
     *
     * @param userCode The unique identifier of the user to retrieve
     * @return ResponseEntity containing the user's details if found
     * @throws UserDoesNotExistException if no user is found with the provided userCode
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getUserByUserCode(String userCode) {
        log.debug("Fetching user with userCode: {}", userCode);
        
        try {
            User user = repository.findByUserCode(userCode)
                    .orElseThrow(() -> {
                        log.warn("User not found with userCode: {}", userCode);
                        return new UserDoesNotExistException("User not found with userCode: " + userCode);
                    });
                    
            log.debug("Successfully retrieved user with userCode: {}", userCode);
            return ResponseEntity.ok(constructUserResponse(user));
            
        } catch (UserDoesNotExistException e) {
            throw e; // Re-throw specific exception
        } catch (Exception e) {
            log.error("Error fetching user with userCode: {}", userCode, e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for users based on username and/or email.
     * At least one search parameter must be provided.
     *
     * @param username The username to search for (optional)
     * @param email The email to search for (optional)
     * @return ResponseEntity containing a list of matching users
     * @throws IllegalArgumentException if both username and email are null
     * @throws UserDoesNotExistException if no users match the search criteria
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponse>> searchUsers(String username, String email) {
        log.debug("Searching users with username: {}, email: {}", username, email);

        if (username == null && email == null) {
            log.warn("Search attempted without any search parameters");
            throw new IllegalArgumentException("At least one search parameter (username or email) must be provided");
        }

        try {
            List<User> users;
            
            if (username != null && email != null) {
                // Search by both username and email (exact match required for both)
                users = repository.findByUsernameAndEmail(username, email)
                        .map(List::of)
                        .orElseThrow(() -> {
                            log.info("No user found with username: '{}' and email: '{}'", username, email);
                            return new UserDoesNotExistException(
                                    String.format("User not found with username: '%s' and email: '%s'", username, email)
                            );
                        });
            } else if (username != null) {
                // Search by username (partial match)
                users = repository.findByUsername(username);
                if (users.isEmpty()) {
                    log.info("No users found with username: '{}'", username);
                    throw new UserDoesNotExistException(
                            String.format("No users found with username: '%s'", username)
                    );
                }
            } else {
                // Search by email (exact match)
                users = repository.findByEmail(email)
                        .map(List::of)
                        .orElseThrow(() -> {
                            log.info("No user found with email: '{}'", email);
                            return new UserDoesNotExistException(
                                    String.format("User not found with email: '%s'", email)
                            );
                        });
            }

            log.debug("Found {} users matching search criteria", users.size());
            List<UserResponse> response = users.stream()
                    .map(ServiceUtils::constructUserResponse)
                    .toList();

            return ResponseEntity.ok(response);
            
        } catch (UserDoesNotExistException e) {
            throw e; // Re-throw specific exception
        } catch (Exception e) {
            log.error("Error searching users with username: '{}', email: '{}'", username, email, e);
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }
}
