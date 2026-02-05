package org.jarApiAutomation.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

@Slf4j
public class CommonUtil {

    public static void executeShellCmd(String shellCmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", shellCmd});
            process.waitFor();
        } catch (Exception e) {
            log.error("Error executing command: {}", shellCmd, e);
        }
    }

    public static String getApiEndPoint(String baseUri, String version, String endpoint) {
        return MessageFormat.format("{0}{1}{2}", baseUri, version, endpoint);
    }

    public static String generateMongoId() {
        return new ObjectId().toHexString();
    }

    public static String getValueFromDocument(Document doc, String key) {
        Object value = doc != null ? doc.get(key) : null;
        return value != null ? value.toString() : null;
    }

    public static double getDoubleValueFromDocument(Document doc, String key) {
        if (doc == null || key == null) return 0.0;

        Object value = doc.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return value != null ? Double.parseDouble(value.toString()) : 0.0;
    }

    public static Map<String, String> buildQueryParams(Object... keyValues) {
        Map<String, String> params = new HashMap<>();

        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];

            if (value != null) {
                params.put(key, String.valueOf(value));
            }
        }
        return params.isEmpty() ? null : params;
    }
}
