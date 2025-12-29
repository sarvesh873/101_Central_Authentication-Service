package com.central.authentication_service.service;

import com.central.authentication_service.exception.InvalidInputException;
import com.central.authentication_service.exception.UserDoesNotExistException;
import com.central.authentication_service.grpc.WalletServiceGrpcClient;
import com.central.authentication_service.kafka.KafkaUserEventProducer;
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
    private final WalletServiceGrpcClient walletServiceGrpcClient;
    private final KafkaUserEventProducer kafkaUserEventProducer;

    /**
     * Constructs a new UserServiceImpl with the required dependencies.
     *
     * @param repository The user repository for database operations
     * @param passwordEncoder The password encoder for hashing passwords
     */
    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, WalletServiceGrpcClient walletServiceGrpcClient, KafkaUserEventProducer kafkaUserEventProducer) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.walletServiceGrpcClient = walletServiceGrpcClient;
        this.kafkaUserEventProducer = kafkaUserEventProducer;
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
    public UserResponse createUser(CentralRequest<CreateUserRequest> request) {
        // Validate request object
        if (request == null || request.getT() == null) {
            log.warn("User creation request is null");
            throw new InvalidInputException("User creation request cannot be empty");
        }

        CreateUserRequest userRequest = request.getT();
        // Check for existing user
        if (repository.existsByEmail(userRequest.getEmail())) {
            log.warn("User creation failed - Email already exists: {}", userRequest.getEmail());
            throw new InvalidInputException("A user with this email already exists");
        }

        // Validate username format (alphanumeric with underscores, 3-20 chars)
        if (!userRequest.getUsername().matches("^[a-zA-Z0-9_]{3,20}$")) {
            log.warn("User creation failed - Invalid username format: {}", userRequest.getUsername());
            throw new InvalidInputException("Username must be 3-20 characters long and can only contain letters, numbers, and underscores");
        }

        try {
            log.info("Creating new user with username: {}", userRequest.getUsername());
            
            User user = User.builder()
                    .userCode(generateUserCode(userRequest.getUsername()))
                    .username(userRequest.getUsername())
                    .email(userRequest.getEmail())
                    .phoneNumber(userRequest.getPhoneNumber())
                    .role(userRequest.getRole() != null ? Role.valueOf(userRequest.getRole().getValue()) : Role.USER)
                    .password(passwordEncoder.encode(userRequest.getPassword()))
                    .build();

            User savedUser = repository.save(user);
            log.info("User created successfully with userCode: {}", savedUser.getUserCode());

            try {
                walletServiceGrpcClient.createWallet(savedUser.getUserCode(), "INR");
            } catch (Exception e) {
                repository.delete(savedUser);
                log.error("Error creating wallet for user: {}", savedUser.getUserCode(), e);
                throw new RuntimeException("Failed to create wallet for user. Please try again later.", e);
            }

            try {
                kafkaUserEventProducer.sendUserEvent(savedUser);
            } catch (Exception e) {
                log.error("Failed to send user event to Kafka for user {}: {}",
                        savedUser.getUserCode(), e.getMessage(), e);
            }

            return constructUserResponse(savedUser);
                    
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user. Please try again later.", e);
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
    public UserResponse getUserByUserCode(String userCode) {
        log.debug("Fetching user with userCode: {}", userCode);
        
        try {
            User user = repository.findByUserCode(userCode)
                    .orElseThrow(() -> {
                        log.warn("User not found with userCode: {}", userCode);
                        return new UserDoesNotExistException("User not found with userCode: " + userCode);
                    });
                    
            log.debug("Successfully retrieved user with userCode: {}", userCode);
            return constructUserResponse(user);
            
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
    public List<UserResponse> searchUsers(String username, String email) {
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

            return response;
            
        } catch (UserDoesNotExistException e) {
            throw e; // Re-throw specific exception
        } catch (Exception e) {
            log.error("Error searching users with username: '{}', email: '{}'", username, email, e);
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }
}
