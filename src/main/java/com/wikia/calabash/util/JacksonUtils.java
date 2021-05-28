package com.wikia.calabash.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author wikia
 * @since 7/13/2020 5:46 PM
 */
@Slf4j
public class JacksonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String writeValueAsString(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static <T> T readValue(byte[] json, TypeReference<T> reference) {
        try {
            return OBJECT_MAPPER.readValue(json, reference);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static <T> T readValue(String json, TypeReference<T> reference) {
        try {
            return OBJECT_MAPPER.readValue(json, reference);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static <T> T readValue(String json, Class<T> clz) {
        try {
            return OBJECT_MAPPER.readValue(json, clz);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static <T> T readValue(byte[] json, Class<T> clz) {
        try {
            return OBJECT_MAPPER.readValue(json, clz);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Map<String, Object> readForMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static List<Map<String, Object>> readForMapList(String json) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
            if (jsonNode.isArray()) {
                return OBJECT_MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {
                });
            } else {
                List<Map<String, Object>> r = new ArrayList<>();
                Map<String, Object> map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
                });
                r.add(map);
                return r;
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static List<Map<String, Object>> readForFlattenMapList(String json) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
            for (JsonNode node : jsonNode) {
                Map<String, Object> map = new HashMap<>();
                toFlattenMap("", node, map);
                result.add(map);
            }
            return result;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Map<String, Object> readForFlattenMap(String json) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(json);

        Map<String, Object> result = new HashMap<>();
        toFlattenMap("", root, result);

        return result;
    }

    private static void toFlattenMap(String key, JsonNode node, Map<String, Object> result) {
        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                toFlattenMap(key + "[" + i + "]", arrayNode.get(i), result);
            }
        }
        if (node instanceof ObjectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> next = fields.next();
                toFlattenMap(key + "." + next.getKey(), next.getValue(), result);
            }
        }
        if (node instanceof ValueNode) {
            result.put(key, node.toString());
        }
    }
}
