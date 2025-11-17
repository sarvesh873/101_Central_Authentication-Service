package com.central.authentication_service.utils;

import com.central.authentication_service.exception.InvalidJWTTokenException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for JWT (JSON Web Token) operations including token generation and validation.
 * This class handles all JWT-related operations using the jjwt library.
 */
@Component
public class JwtUtil {

    /**
     * The secret key used for signing and verifying JWT tokens.
     * Injected from application properties and base64 decoded.
     */
    private final Key secretKey;

    /**
     * Expiration time for the access token in milliseconds.
     * Configured in application properties.
     */
    @Value("${jwt.accessTokenExpiry}")
    private int accessTokenExpiry;

    /**
     * Constructs a new JwtUtil with the provided secret key.
     * The secret is expected to be a base64-encoded string.
     *
     * @param secret The base64-encoded secret key for JWT signing/verification
     */
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Decode the base64 secret and create a secure key
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new JWT token for the specified user and role.
     * The token includes the following claims:
     * - sub (subject): The user's unique identifier
     * - userCode: The user's code
     * - role: The user's role
     * - iat (issued at): Timestamp when the token was issued
     * - exp (expiration): Timestamp when the token will expire
     *
     * @param userCode The unique identifier for the user
     * @param role The role assigned to the user
     * @return A signed JWT token as a compact URL-safe string
     */
    public String generateToken(String userCode, String role) {
        return Jwts.builder()
                .subject(userCode)  // Standard JWT claim: subject (identifies the principal)
                .claim("userCode", userCode)  // Custom claim: user's unique code
                .claim("role", role)  // Custom claim: user's role
                .issuedAt(new Date())  // Standard JWT claim: issued at time
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))  // Expiration time
                .signWith(secretKey)  // Sign the token with our secret key
                .compact();  // Convert to compact string
    }

    /**
     * Validates the provided JWT token.
     * This method verifies the token's signature and checks if it has expired.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid, false otherwise
     * @throws InvalidJWTTokenException if the token is invalid or expired
     */
    public Boolean validateToken(String token) {
        try {
            // Parse and verify the token's signature
            Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            // The token's signature is invalid
            throw new InvalidJWTTokenException("Invalid JWT signature");
        } catch (JwtException e) {
            // Other JWT validation errors (expired, malformed, etc.)
            throw new InvalidJWTTokenException("Invalid or expired JWT token: " + e.getMessage());
        }
    }
}