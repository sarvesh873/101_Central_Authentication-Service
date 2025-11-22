package com.central.authentication_service.controller;

import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;
    private final String testUserCode = "USR123";

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("password123")
            .build();

        userResponse = UserResponse.builder()
            .email("test@example.com")
            .username("testuser")
            .build();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        // Arrange
        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
            .t(createUserRequest)
            .build();

        when(userService.createUser(any(CentralRequest.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(userResponse));

        // Act
        ResponseEntity<UserResponse> response = userController.createUser(createUserRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).createUser(any(CentralRequest.class));
    }

    @Test
    void getUserByUserCode_ShouldReturnUser() {
        // Arrange
        when(userService.getUserByUserCode(testUserCode))
            .thenReturn(ResponseEntity.ok(userResponse));

        // Act
        ResponseEntity<UserResponse> response = userController.getUserByUserCode(testUserCode);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).getUserByUserCode(testUserCode);
    }

    @Test
    void searchUsers_ByUsername_ShouldReturnMatchingUsers() {
        // Arrange
        String username = "testuser";
        List<UserResponse> userList = Arrays.asList(userResponse);
        
        when(userService.searchUsers(eq(username), isNull()))
            .thenReturn(ResponseEntity.ok(userList));

        // Act
        ResponseEntity<List<UserResponse>> response = userController.searchUsers(username, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).searchUsers(eq(username), isNull());
    }

    @Test
    void searchUsers_ByEmail_ShouldReturnMatchingUsers() {
        // Arrange
        String email = "test@example.com";
        List<UserResponse> userList = Arrays.asList(userResponse);
        
        when(userService.searchUsers(isNull(), eq(email)))
            .thenReturn(ResponseEntity.ok(userList));

        // Act
        ResponseEntity<List<UserResponse>> response = userController.searchUsers(null, email);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).searchUsers(isNull(), eq(email));
    }
}
