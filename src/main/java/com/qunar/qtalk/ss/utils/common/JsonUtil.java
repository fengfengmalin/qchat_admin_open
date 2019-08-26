package com.qunar.qtalk.ss.utils.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //序列化时的配置
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //反序列化时的配置
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        //其他配置
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    }

    public static String toJSONString(Map<String, Object> jsonObject) throws JsonProcessingException {
        return objectMapper.writeValueAsString(jsonObject);
    }

    public static Map<String, Object> parseJSONObject(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, Map.class);
    }

    public static List parseJSonArray(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, List.class);
    }

    public static <T> String obj2String(T obj) {

        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("serialize Object to String failed,Object:{}", obj.getClass(), e);
            return null;
        }
    }

    public static <T> T string2Obj(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || clazz == null) {
            return null;
        }
        try {
            return String.class.equals(clazz) ? (T) json : objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.info(" deserialize String to Object failed,json:{},class:{}", json, clazz.getName(), e);
            return null;
        }
    }

    public static <T> T string2Obj(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json) || typeReference == null) {
            return null;
        }
        try {
            return (T) (String.class.equals(typeReference.getType()) ? json : objectMapper.readValue(json, typeReference));
        } catch (IOException e) {
            logger.info(" deserialize String to Object failed,json:{},type:{}", json, typeReference.getType(), e);
            return null;
        }
    }

    public static void main(String[] args) {

        try {

            String jsonString = "{\"asdf\":234, \"bbb\":\"fff\"}";

            Map<String, Object> var = JsonUtil.parseJSONObject(jsonString);

            jsonString = "[\"asdf\", \"asdfff\"]";

            List<String> vall = JsonUtil.parseJSonArray(jsonString);

            System.out.println("asdf");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
