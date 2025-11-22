package com.central.authentication_service.utils;

import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import org.junit.jupiter.api.Test;
import org.openapitools.model.LoginResponse;
import org.openapitools.model.UserResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ServiceUtilsTest {

    private static final String TEST_USER_CODE = "USR123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test.jwt.token";

    @Test
    void constructUserResponse_WithValidUser_ShouldReturnUserResponse() {
        // Arrange
        User user = User.builder()
                .userCode(TEST_USER_CODE)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .role(Role.USER)
                .build();

        // Act
        UserResponse response = ServiceUtils.constructUserResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(UserResponse.RoleEnum.USER, response.getRole());
    }

    @Test
    void constructUserResponse_WithNullUser_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceUtils.constructUserResponse((User) null);
        });
    }

    @Test
    void constructUserResponse_WithValidUserOptional_ShouldReturnUserResponse() {
        // Arrange
        User user = User.builder()
                .userCode(TEST_USER_CODE)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .role(Role.SUPERUSER)
                .build();

        // Act
        UserResponse response = ServiceUtils.constructUserResponse(Optional.of(user));

        // Assert
        assertNotNull(response);
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(UserResponse.RoleEnum.SUPERUSER, response.getRole());
    }

    @Test
    void constructUserResponse_WithEmptyOptional_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceUtils.constructUserResponse(Optional.empty());
        });
    }

    @Test
    void constructUserResponse_WithNullOptional_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceUtils.constructUserResponse((Optional<User>) null);
        });
    }

    @Test
    void constructLoginResponse_WithValidToken_ShouldReturnLoginResponse() {
        // Act
        LoginResponse response = ServiceUtils.constructLoginResponse(TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getAccessToken());
        assertEquals("10 Hrs", response.getExpiresIn());
    }

    @Test
    void constructLoginResponse_WithNullToken_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceUtils.constructLoginResponse(null);
        });
    }

    @Test
    void constructLoginResponse_WithEmptyToken_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceUtils.constructLoginResponse("");
        });
    }

    @Test
    void constructLoginResponse_WithBlankToken_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ServiceUtils.constructLoginResponse("   ");
        });
    }
}
