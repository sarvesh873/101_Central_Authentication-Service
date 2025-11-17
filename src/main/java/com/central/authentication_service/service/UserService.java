package com.central.authentication_service.service;

import com.central.authentication_service.model.CentralRequest;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service interface for user management operations.
 * Defines the contract for user-related business logic.
 */
@Service
public interface UserService {

    /**
     * Creates a new user with the provided details.
     *
     * @param request The request containing user details to be created
     * @return ResponseEntity containing the created user's details and HTTP status
     * @throws com.central.authentication_service.exception.UserAlreadyExistsException if a user with the same email already exists
     * @throws jakarta.validation.ValidationException if the request contains invalid data
     */
    ResponseEntity<UserResponse> createUser(CentralRequest<CreateUserRequest> request);

    /**
     * Retrieves a user by their unique user code.
     *
     * @param userCode The unique identifier of the user to retrieve
     * @return ResponseEntity containing the user's details if found
     * @throws com.central.authentication_service.exception.UserDoesNotExistException if no user is found with the given user code
     */
    ResponseEntity<UserResponse> getUserByUserCode(String userCode);

    /**
     * Searches for users based on username and/or email.
     * At least one search parameter must be provided.
     *
     * @param username The username to search for (can be null)
     * @param email The email to search for (can be null)
     * @return ResponseEntity containing a list of matching users
     * @throws IllegalArgumentException if both username and email are null
     * @throws com.central.authentication_service.exception.UserDoesNotExistException if no users match the search criteria
     */
    ResponseEntity<List<UserResponse>> searchUsers(String username, String email);
}
