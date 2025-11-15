package com.central.authentication_service.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class UserCodeUtil {


    private static final String SECRET = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCoCRIIJZ2yelWRekUPh/bgVZAOYcB4F7MVRAuQ5ZszLVgWlZBaUBuvF9hrO2+Wv3VlDkPWWgZyVdWI1ObAiFQAWRSWSU4yT+1bGpEK9Z42mZSoHLB+UOLm24W3a5V9iNV26wS0Oaza3ANROSwBmRJOgTlOsSE3DNl1cKUhyPsd4QIDAQAB" +
            "";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public static String generateUserCode(String username) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        try {
            // 1. Generate random salt (secure)
            byte[] salt = new byte[16]; // 128-bit crypto salt
            RANDOM.nextBytes(salt);

            // 2. Compute HMAC(username + randomSalt)
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);

            mac.update(username.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(salt);

            // 3. Convert HMAC bytes → A-Z + 0-9 only
            //    Use modulus to map bytes to 36-character alphabet safely
            StringBuilder result = new StringBuilder(7);
            for (int i = 0; i < 7; i++) {
                int index = (rawHmac[i] & 0xFF) % ALPHANUM.length;
                result.append(ALPHANUM[index]);
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generating user code", e);
        }
    }
}

