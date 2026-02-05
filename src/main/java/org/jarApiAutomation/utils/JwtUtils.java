package org.jarApiAutomation.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;

public class JwtUtils {

    @SuppressWarnings("unchecked")
    public static String extractUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);

            Map<String, Object> claims =
                    (Map<String, Object>)
                            payload.get(
                                    "https://myjar.app/jwt/claims"); // JWT claim ka “namespace key”

            return (String) claims.get("userId");

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract userId from token", e);
        }
    }
}
