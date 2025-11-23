package com.central.authentication_service.utils;

import com.central.authentication_service.model.User;
import org.openapitools.model.LoginResponse;
import org.openapitools.model.UserResponse;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility class providing helper methods for service layer operations.
 * Contains methods for converting between domain models and API response objects.
 */
public final class ServiceUtils {

    // Private constructor to prevent instantiation
    private ServiceUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a User domain object to a UserResponse DTO.
     *
     * @param user The User entity to convert
     * @return UserResponse containing the user's data
     * @throws IllegalArgumentException if the user parameter is null
     */
    public static UserResponse constructUserResponse(User user) {
        if (Objects.isNull(user)) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        UserResponse userResponse = UserResponse.builder()
                // Copy matching properties from User to UserResponse
                .username(user.getUsername())
                .email(user.getEmail())
                // Convert role to RoleEnum
                .role(UserResponse.RoleEnum.valueOf(user.getRole().toString()))
                .build();
        return userResponse;
    }

    /**
     * Converts an Optional<User> to a UserResponse DTO.
     *
     * @param user Optional containing the User entity to convert
     * @return UserResponse containing the user's data
     * @throws IllegalArgumentException if the Optional parameter is null or empty
     */
    public static UserResponse constructUserResponse(Optional<User> user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("User cannot be null or empty");
        }
        
        return constructUserResponse(user.get());
    }

    /**
     * Creates a LoginResponse DTO with the provided JWT token.
     * Sets a default expiration time of 10 hours.
     *
     * @param token The JWT token to include in the response
     * @return LoginResponse containing the token and expiration information
     * @throws IllegalArgumentException if the token is null or empty
     */
    public static LoginResponse constructLoginResponse(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(token)
                .expiresIn("10 Hrs")
                .build();
        return loginResponse;
    }
}
