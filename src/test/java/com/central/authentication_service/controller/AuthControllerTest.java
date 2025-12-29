package com.central.authentication_service.controller;

import com.central.authentication_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private final String testToken = "testToken";

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
            .email("test@example.com")
            .password("password123")
            .build();

        loginResponse = LoginResponse.builder()
            .accessToken("testuser")
            .build();
    }

    @Test
    void loginUser_ShouldReturnLoginResponse() {
        // Arrange
        when(authService.loginUser(any(LoginRequest.class)))
            .thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.loginUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        verify(authService, times(1)).loginUser(loginRequest);
    }

    @Test
    void validateToken_ShouldReturnOkWhenTokenIsValid() {
        // Arrange
        when(authService.validateToken(testToken)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = authController.validateToken(testToken);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).validateToken(testToken);
    }

    @Test
    void validateToken_ShouldHandleTokenWithBearerPrefix() {
        // Arrange
        String tokenWithBearer = "Bearer " + testToken;
        when(authService.validateToken(tokenWithBearer)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = authController.validateToken(tokenWithBearer);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).validateToken(tokenWithBearer);
    }
}
