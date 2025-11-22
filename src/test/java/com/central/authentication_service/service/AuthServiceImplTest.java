package com.central.authentication_service.service;

import com.central.authentication_service.exception.InvalidJWTTokenException;
import com.central.authentication_service.exception.UserDoesNotExistException;
import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import com.central.authentication_service.repository.UserRepository;
import com.central.authentication_service.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private User testUser;
    private final String testToken = "test.jwt.token";    private final String testUserCode = "USR123";
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String encodedPassword = "$2a$10$testencodedpassword";

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
            .email(testEmail)
            .password(testPassword)
            .build();

        testUser = User.builder()
            .userCode(testUserCode)
            .email(testEmail)
            .password(encodedPassword)
            .role(Role.USER)
            .build();
    }

    @Test
    void loginUser_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(testToken);

        // Act
        ResponseEntity<LoginResponse> response = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testToken, response.getBody().getAccessToken());
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
        verify(jwtUtil, times(1)).generateToken(anyString(), anyString());
    }

    @Test
    void loginUser_WithNonExistentEmail_ShouldThrowUserDoesNotExistException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserDoesNotExistException.class, () -> {
            authService.loginUser(loginRequest);
        });
        verify(userRepository, times(1)).findByEmail(testEmail);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void loginUser_WhenUnexpectedErrorOccurs_ShouldThrowInvalidJWTTokenException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        InvalidJWTTokenException exception = assertThrows(InvalidJWTTokenException.class, () -> {
            authService.loginUser(loginRequest);
        });
        
        assertTrue(exception.getMessage().contains("Authentication failed"));
        verify(userRepository, times(1)).findByEmail(testEmail);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void validateToken_WithNullToken_ShouldThrowInvalidJWTTokenException() {
        // Act & Assert
        InvalidJWTTokenException exception = assertThrows(InvalidJWTTokenException.class, () -> {
            authService.validateToken(null);
        });
        
        assertEquals("Missing or invalid Authorization header", exception.getMessage());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void validateToken_WithInvalidTokenFormat_ShouldThrowInvalidJWTTokenException() {
        // Arrange
        String invalidToken = "InvalidToken";

        // Act & Assert
        InvalidJWTTokenException exception = assertThrows(InvalidJWTTokenException.class, () -> {
            authService.validateToken(invalidToken);
        });
        
        assertEquals("Missing or invalid Authorization header", exception.getMessage());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void validateToken_WhenJwtExceptionOccurs_ShouldThrowInvalidJWTTokenException() {
        // Arrange
        String token = "Bearer invalid.token";
        when(jwtUtil.validateToken(anyString())).thenThrow(new JwtException("Invalid token"));

        // Act & Assert
        InvalidJWTTokenException exception = assertThrows(InvalidJWTTokenException.class, () -> {
            authService.validateToken(token);
        });
        
        assertTrue(exception.getMessage().contains("Invalid JWT token"));
        verify(jwtUtil, times(1)).validateToken("invalid.token");
    }

    @Test
    void loginUser_WithInvalidPassword_ShouldThrowInvalidJWTTokenException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidJWTTokenException.class, () -> {
            authService.loginUser(loginRequest);
        });
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void loginUser_WithNullRequest_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.loginUser(null);
        });
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void loginUser_WithNullEmail_ShouldThrowIllegalArgumentException() {
        // Arrange
        loginRequest.setEmail(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.loginUser(loginRequest);
        });
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void loginUser_WithNullPassword_ShouldThrowIllegalArgumentException() {
        // Arrange
        loginRequest.setPassword(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.loginUser(loginRequest);
        });
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnOk() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = authService.validateToken("Bearer "+testToken);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil, times(1)).validateToken(testToken);
    }

    @Test
    void validateToken_WithBearerToken_ShouldStripBearerPrefix() {
        // Arrange
        String bearerToken = "Bearer " + testToken;
        when(jwtUtil.validateToken(testToken)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = authService.validateToken(bearerToken);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtUtil, times(1)).validateToken(testToken);
    }
}
