package org.jarApiAutomation.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CommonSerializationUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectMapper xmlObjectMapper = new XmlMapper();


    public static String writeString(Object value) {
        try {
            return (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error("Failed read write string: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Convert map to object
     */
    public static <T> T convertMapToObject(Map<String, Object> map, Class<T> cls) {
        return objectMapper.convertValue(map, cls);
    }

    /**
     * Helper to get a Map<String, Object> from String
     */
    public static Map<String, Object> readMapFromString(String str) {
        return readMapFromString(str, true);
    }

    /**
     * Helper to convert Object to Map
     */
    public static Map<String, Object> convertObjectToMap(Object object) {
        if (object instanceof Map) {
            return (Map<String, Object>) object;
        } else {
            return objectMapper.convertValue(object, TypeFactory.defaultInstance().constructMapLikeType(HashMap.class, String.class, Object.class));
        }
    }

    /**
     * Helper to get a Map<String, Object> from String - with option to suppress error
     */
    public static Map<String, Object> readMapFromString(String str, boolean suppressError) {
        try {
            return objectMapper.readValue(str, TypeFactory.defaultInstance().constructMapLikeType(HashMap.class, String.class, Object.class));
        } catch (IOException e) {
            log.error("Failed read map from string: ", e);
            if (suppressError) {
                return new HashMap<>();
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Read a object from string
     */
    public static <T> T readObject(String str, Class<T> cls){
        T res = null;
        if (!Strings.isNullOrEmpty(str)) {
            try {
                res = (cls == String.class) ? (T) str : objectMapper.readValue(str, cls);
            } catch (IOException e) {
                log.error("Failed read Object to class: ", e);
                throw new RuntimeException(e.getMessage());
            }
        }
        return res;
    }

    /**
     * Read parameterised object from string
     */
    public static <T> T readObject(String str, TypeReference<T> cls) {
        T res = null;
        if (!Strings.isNullOrEmpty(str)) {
            try {
                res = objectMapper.readValue(str, cls);
            } catch (IOException e) {
                log.error("Failed read Object to class: ", e);
                throw new RuntimeException(e.getMessage());
            }
        }
        return res;
    }

    /**
     * Read a xml object from string
     */
    public static <T> T readXmlObject(String str, Class<T> cls) {
        try {
            return (cls == String.class) ? (T) str : xmlObjectMapper.readValue(str, cls);
        } catch (IOException e) {
            log.error("Failed read Object to class: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Read a object from string for java types
     */
    public static <T> T readObject(String str, JavaType cls) {
        T res = null;
        if (!Strings.isNullOrEmpty(str)) {
            try {
                res = objectMapper.readValue(str, cls);
            } catch (IOException e) {
                log.error("Failed read Object to JavaType: ", e);
                throw new RuntimeException(e.getMessage());
            }
        }
        return res;
    }

    /**
     * Read list from string
     */
    public static <T> List<T> readListFromString(String str, Class<T> cls) {
        try {
            return objectMapper.readValue(str, TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, cls));
        } catch (IOException e) {
            log.error("Failed to read list from string: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Convert list from TypedList
     */
    public static <T> List<T> convertListToTypedList(List input, Class<T> cls) {
        return objectMapper.convertValue(input, TypeFactory.defaultInstance().constructCollectionLikeType(List.class, cls));
    }


    public static Map<String, String> convertToFlatMap(Object request) {
        Map<String, Object> objectMap = JsonFlattener.flattenAsMap(CommonSerializationUtil.writeString(request));
        Map<String, String> flatMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            if (entry.getValue() != null) {
                flatMap.put(entry.getKey(), entry.getValue().toString());
            } else {
                log.warn("No Value present for key : {}. Hence skipping the key in flatMap", entry.getKey());
            }
        }
        return flatMap;
    }

}