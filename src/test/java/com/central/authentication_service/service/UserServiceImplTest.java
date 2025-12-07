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
    private final String testPhoneNumber = "+1234567890";
    private final String encodedPassword = "$2a$10$testencodedpassword";

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        createUserRequest = CreateUserRequest.builder()
                .email(testEmail)
                .username(testUsername)
                .password(testPassword)
                .phoneNumber(testPhoneNumber)
                .role(CreateUserRequest.RoleEnum.USER)
                .build();

        testUser = User.builder()
                .userCode(testUserCode)
                .email(testEmail)
                .username(testUsername)
                .password(encodedPassword)
                .phoneNumber(testPhoneNumber)
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
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUserCode(testUserCode);
            return savedUser;
        });

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(testEmail, response.getEmail());
        assertEquals(testUsername, response.getUsername());

        verify(userRepository, times(1)).existsByEmail(testEmail);
        verify(passwordEncoder, times(1)).encode(testPassword);

        // Verify the saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(testUsername, savedUser.getUsername());
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
    void searchUsers_WithMatchingUsername_ShouldReturnMatchingUsers() {
        // Arrange
        String searchUsername = "testuser";
        when(userRepository.findByUsername(searchUsername))
                .thenReturn(Collections.singletonList(testUser));

        // Act
        List<UserResponse> response = userService.searchUsers(searchUsername, null);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(searchUsername, response.get(0).getUsername());
    }

    @Test
    void searchUsers_WithNoSearchCriteria_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.searchUsers(null, null);
        });
    }
}