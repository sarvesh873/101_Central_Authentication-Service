package com.central.authentication_service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCodeUtilTest {

    private static final String TEST_SECRET = "testSecretKey1234567890";
    private static final String TEST_USERNAME = "testuser";

    @InjectMocks
    private UserCodeUtil userCodeUtil;

    @BeforeEach
    void setUp() {
        // Initialize UserCodeUtil with test secret
        userCodeUtil = new UserCodeUtil(TEST_SECRET);
    }

    @Test
    void generateUserCode_ShouldReturnValidCode() {
        // Act
        String userCode = UserCodeUtil.generateUserCode(TEST_USERNAME);

        // Assert
        assertNotNull(userCode);
        assertEquals(7, userCode.length());
        assertTrue(userCode.matches("^[A-Z0-9]{7}$")); // Should be 7 alphanumeric characters
    }

    @Test
    void generateUserCode_WithDifferentUsernames_ShouldReturnDifferentCodes() {
        // Act
        String code1 = UserCodeUtil.generateUserCode("user1");
        String code2 = UserCodeUtil.generateUserCode("user2");

        // Assert
        assertNotEquals(code1, code2);
    }

    @Test
    void generateUserCode_WithSameUsername_ShouldReturnDifferentCodes() {
        // Act
        String code1 = UserCodeUtil.generateUserCode(TEST_USERNAME);
        String code2 = UserCodeUtil.generateUserCode(TEST_USERNAME);

        // Assert
        assertNotEquals(code1, code2); // Due to random salt, even same username gives different codes
    }

    @Test
    void generateUserCode_WithEmptyUsername_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            String code = UserCodeUtil.generateUserCode("");
            assertNotNull(code);
            assertEquals(7, code.length());
        });
    }

    @Test
    void generateUserCode_WithNullUsername_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            UserCodeUtil.generateUserCode(null);
        });
    }

    @Test
    void generateUserCode_ShouldOnlyContainAlphanumericChars() {
        // Act
        String code = UserCodeUtil.generateUserCode(TEST_USERNAME);

        // Assert
        assertTrue(code.matches("^[A-Z0-9]+$"));
    }

    @Test
    void generateUserCode_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            String code = UserCodeUtil.generateUserCode("user@domain.com!@#");
            assertNotNull(code);
            assertEquals(7, code.length());
        });
    }
}
