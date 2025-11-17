package com.central.authentication_service.controller;

import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.api.UserApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * REST controller for handling user-related HTTP requests.
 * Implements the UserApi interface generated from OpenAPI specification.
 * Delegates business logic to the UserService.
 */
@RestController
@Slf4j
public class UserController implements UserApi {

    private final UserService userService;

    /**
     * Constructs a new UserController with the specified UserService.
     *
     * @param userService The user service to delegate business logic to
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles user creation requests.
     *
     * @param createUserRequest The request containing user details for creation
     * @return ResponseEntity containing the created user's details and HTTP status
     */
    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
        log.info("Processing create user request for username: {}, email: {}",
                createUserRequest.getUsername(), createUserRequest.getEmail());

        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
                .t(createUserRequest)
                .build();

        return userService.createUser(request);
    }

    /**
     * Retrieves a user by their unique user code.
     *
     * @param userCode The unique identifier of the user to retrieve
     * @return ResponseEntity containing the user's details if found
     */
    @Override
    public ResponseEntity<UserResponse> getUserByUserCode(String userCode) {
        log.info("Fetching user with userCode: {}", userCode);
        return userService.getUserByUserCode(userCode);
    }

    /**
     * Searches for users based on username and/or email.
     * At least one search parameter must be provided.
     *
     * @param username The username to search for (optional)
     * @param email The email to search for (optional)
     * @return ResponseEntity containing a list of matching users
     */
    @Override
    public ResponseEntity<List<UserResponse>> searchUsers(String username, String email) {
        log.info("Searching users with username: {}, email: {}", username, email);
        return userService.searchUsers(username, email);
    }
}
