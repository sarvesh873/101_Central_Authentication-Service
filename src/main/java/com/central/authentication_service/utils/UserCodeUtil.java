package com.central.authentication_service.utils;

import static com.central.authentication_service.constants.Constants.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating secure, unique user codes.
 * Uses HMAC-based generation with a secret key and random salt to ensure
 * both uniqueness and unpredictability of generated codes.
 */
@Slf4j
@Component
public class UserCodeUtil {

    private static String SECRET;
    private static SecureRandom RANDOM = new SecureRandom();

    /**
     * Constructor for UserCodeUtil.
     *
     * @param secret The secret key for HMAC generation, injected from application properties
     */
    public UserCodeUtil(@Value("${central.usercode.secret}") String secret) {
        SECRET = secret;
    }
    /**
     * Character set used for generating alphanumeric user codes (A-Z, 0-9).
     */
    private static final char[] ALPHANUM_CHARS = ALPHANUM.toCharArray();

    public static String generateUserCode(String username) {

        try {
            log.info("Generating user code for username: {}", username);
            // 1. Generate random salt (secure)
            byte[] salt = new byte[16]; // 128-bit crypto salt
            RANDOM.nextBytes(salt);

            // 2. Compute HMAC(username + randomSalt)
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(keySpec);

            mac.update(username.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(salt);

            // 3. Convert HMAC bytes → A-Z + 0-9 only
            //    Use modulus to map bytes to 36-character alphabet safely
            StringBuilder result = new StringBuilder(7);
            for (int i = 0; i < 7; i++) {
                int index = (rawHmac[i] & 0xFF) % ALPHANUM_CHARS.length;
                result.append(ALPHANUM_CHARS[index]);
            }
            log.info("Generated user code successfully for username {}", username);
            return result.toString();

        } catch (Exception e) {
            log.error("Error generating user code for username {}", username, e);
            throw new RuntimeException("Error generating user code", e);
        }
    }
}

