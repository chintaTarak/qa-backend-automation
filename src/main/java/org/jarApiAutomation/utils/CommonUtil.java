package org.jarApiAutomation.utils;

import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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
        return doc != null ? doc.getString(key) : null;
    }

    public static Map<String, String> buildQueryParams(String... keyValues) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = keyValues[i];
            String value = keyValues[i + 1];

            if (value != null) {
                params.put(key, value);
            }
        }
        return params.isEmpty() ? null : params;
    }
}
