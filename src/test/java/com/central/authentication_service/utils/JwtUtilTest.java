package com.central.authentication_service.utils;

import com.central.authentication_service.exception.InvalidJWTTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private static final String TEST_SECRET = "testSecretKey12345678901234567890123456789012";
    private static final String TEST_USER_CODE = "USR123";
    private static final String TEST_ROLE = "USER";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        // Encode the test secret in base64
        String base64Secret = Base64.getEncoder()
                .encodeToString(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        // Initialize JwtUtil with the test secret
        jwtUtil = new JwtUtil(base64Secret);

        // Set the access token expiry to 1 hour for testing (using int value)
        setAccessTokenExpiry(3600 * 1000);  // 1 hour in milliseconds
    }
    private void setAccessTokenExpiry(int expiry) throws Exception {
        Field field = JwtUtil.class.getDeclaredField("accessTokenExpiry");
        field.setAccessible(true);
        field.setInt(jwtUtil, expiry);  // Changed from set() to setInt()
    }




    private void setAccessTokenExpiry(long expiry) throws Exception {
        Field field = JwtUtil.class.getDeclaredField("accessTokenExpiry");
        field.setAccessible(true);
        field.set(jwtUtil, expiry);
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_USER_CODE, TEST_ROLE);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Parse the token to verify its structure
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(TEST_USER_CODE, claims.getSubject());
        assertEquals(TEST_USER_CODE, claims.get("userCode", String.class));
        assertEquals(TEST_ROLE, claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_CODE, TEST_ROLE);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldThrowInvalidJWTTokenException() throws Exception {
        // Arrange
        setAccessTokenExpiry(-1000); // Set token to expire in the past
        String expiredToken = jwtUtil.generateToken(TEST_USER_CODE, TEST_ROLE);
        setAccessTokenExpiry(3600000); // Reset for other tests

        // Act & Assert
        assertThrows(InvalidJWTTokenException.class, () -> jwtUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_WithInvalidSignature_ShouldThrowInvalidJWTTokenException() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USER_CODE, TEST_ROLE);

        // Create a JwtUtil with a different secret
        String otherSecret = Base64.getEncoder()
                .encodeToString("differentSecret123456789012345678901234567890".getBytes());
        JwtUtil otherJwtUtil = new JwtUtil(otherSecret);

        // Act & Assert
        assertThrows(InvalidJWTTokenException.class, () -> otherJwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithMalformedToken_ShouldThrowInvalidJWTTokenException() {
        // Act & Assert
        assertThrows(InvalidJWTTokenException.class,
                () -> jwtUtil.validateToken("malformed.token.here"));
    }

    @Test
    void validateToken_WithNullToken_ShouldThrowInvalidJWTTokenException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_WithEmptyToken_ShouldThrowInvalidJWTTokenException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> jwtUtil.validateToken(""));
    }

    @Test
    void generateToken_WithEmptyRole_ShouldGenerateValidToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_USER_CODE, "");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_WithNullRole_ShouldGenerateValidToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_USER_CODE, null);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}