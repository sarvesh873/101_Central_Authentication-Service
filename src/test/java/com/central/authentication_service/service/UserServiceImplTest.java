package com.central.authentication_service.service;

import com.central.authentication_service.exception.InvalidInputException;
import com.central.authentication_service.exception.UserDoesNotExistException;
import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import com.central.authentication_service.repository.UserRepository;
import com.central.authentication_service.utils.UserCodeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.central.authentication_service.utils.UserCodeUtil.generateUserCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;


    private CreateUserRequest createUserRequest;
    private User testUser;
    private final String testUserCode = "USR123";
    private final String testEmail = "test@example.com";
    private final String testUsername = "testuser";
    private final String testPassword = "password123";
    private final String encodedPassword = "$2a$10$testencodedpassword";

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        createUserRequest = CreateUserRequest.builder()
                .email(testEmail)
                .username(testUsername)
                .password(testPassword)
                .role(Role.USER.name())
                .build();

        testUser = User.builder()
            .userCode(testUserCode)
            .email(testEmail)
            .username(testUsername)
            .password(encodedPassword)
            .role(Role.USER)
            .build();

        Field secretField = UserCodeUtil.class.getDeclaredField("SECRET");
        secretField.setAccessible(true);
        secretField.set(null, "testSecretKey12345678901234567890123456789012");
    }

    @Test
    void createUser_WithValidRequest_ShouldCreateUser() {
        // Arrange
        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
            .t(createUserRequest)
            .build();

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<UserResponse> response = userService.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testEmail, response.getBody().getEmail());
        assertEquals(testUsername, response.getBody().getUsername());
        
        verify(userRepository, times(1)).existsByEmail(testEmail);
        verify(passwordEncoder, times(1)).encode(testPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowInvalidInputException() {
        // Arrange
        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
            .t(createUserRequest)
            .build();

        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> {
            userService.createUser(request);
        });
        verify(userRepository, times(1)).existsByEmail(testEmail);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenDatabaseErrorOccurs_ShouldThrowRuntimeException() {
        // Arrange
        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
            .t(createUserRequest)
            .build();

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(request);
        });
        
        verify(userRepository, times(1)).existsByEmail(testEmail);
        verify(passwordEncoder, times(1)).encode(testPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithInvalidRole_ShouldThrowRuntimeException() {
        // Arrange
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
            .email("new@example.com")
            .username("newuser")
            .password("password123")
            .role("INVALID_ROLE")
            .build();

        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
            .t(invalidRequest)
            .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(request);
        });
        
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUser_WithNullRequest_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(null);
        });
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUserCode_WithExistingUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUserCode(testUserCode)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserResponse> response = userService.getUserByUserCode(testUserCode);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userRepository, times(1)).findByUserCode(testUserCode);
    }

    @Test
    void getUserByUserCode_WithNonExistentUser_ShouldThrowUserDoesNotExistException() {
        // Arrange
        when(userRepository.findByUserCode(testUserCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserDoesNotExistException.class, () -> {
            userService.getUserByUserCode(testUserCode);
        });
        
        verify(userRepository, times(1)).findByUserCode(testUserCode);
    }
    
    @Test
    void getUserByUserCode_WhenUnexpectedErrorOccurs_ShouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByUserCode(testUserCode)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByUserCode(testUserCode);
        });
        
        // Verify the exception message contains the expected error
        assertTrue(exception.getMessage().contains("Failed to fetch user"));
        verify(userRepository, times(1)).findByUserCode(testUserCode);
    }

    @Test
    void searchUsers_ByUsername_ShouldReturnMatchingUsers() {
        // Arrange
        String username = "test";
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByUsername(username)).thenReturn(users);

        // Act
        ResponseEntity<List<UserResponse>> response = userService.searchUsers(username, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void searchUsers_ByEmail_ShouldReturnMatchingUsers() {
        // Arrange
        String email = "test";
        Optional<User> users = Optional.of(testUser);
        when(userRepository.findByEmail(email)).thenReturn(users);

        // Act
        ResponseEntity<List<UserResponse>> response = userService.searchUsers(null, email);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
        assertEquals(testEmail, response.getBody().get(0).getEmail());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void searchUsers_ByBothUsernameAndEmail_ShouldReturnMatchingUser() {
        // Arrange
        when(userRepository.findByUsernameAndEmail(testUsername, testEmail))
            .thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<List<UserResponse>> response = userService.searchUsers(testUsername, testEmail);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
        assertEquals(testEmail, response.getBody().get(0).getEmail());
        verify(userRepository, times(1)).findByUsernameAndEmail(testUsername, testEmail);
    }

    @Test
    void searchUsers_ByNonExistentUsername_ShouldThrowUserDoesNotExistException() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(UserDoesNotExistException.class, () -> {
            userService.searchUsers(nonExistentUsername, null);
        });
        
        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByUsernameAndEmail(anyString(), anyString());
    }

    @Test
    void searchUsers_ByNonExistentEmail_ShouldThrowUserDoesNotExistException() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserDoesNotExistException.class, () -> {
            userService.searchUsers(null, nonExistentEmail);
        });
        
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByUsernameAndEmail(anyString(), anyString());
    }

    @Test
    void searchUsers_ByNonExistentUsernameAndEmail_ShouldThrowUserDoesNotExistException() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByUsernameAndEmail(nonExistentUsername, nonExistentEmail))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserDoesNotExistException.class, () -> {
            userService.searchUsers(nonExistentUsername, nonExistentEmail);
        });
        
        verify(userRepository, times(1)).findByUsernameAndEmail(nonExistentUsername, nonExistentEmail);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void searchUsers_WhenUnexpectedErrorOccurs_ShouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.searchUsers("test", null);
        });
        
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void searchUsers_WithNoSearchCriteria_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.searchUsers(null, null);
        });
        verifyNoInteractions(userRepository);
    }
}
