package com.qunar.qchat.admin.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 这个类封装了Jackson（2.0以后的版本）的一些方法，{@link com.fasterxml.jackson}
 * Note: Jackson在2.0版本以前的包是org.codehaus.jackson,2.0版本以后包名叫com.fastxml.jackson
 */
public class JacksonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JacksonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    @SuppressWarnings(value = {"unchecked", "unused"})
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

    @SuppressWarnings(value = {"unchecked", "unused"})
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

    @SuppressWarnings("all")
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

    /**
     * string to map
     *
     * @param json json format
     * @return map object
     */
    @SuppressWarnings(value = {"unchecked", "unused"})
    public static Map<String, Object> string2Map(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            if (!isJSONValid(json)) {
                logger.info("string2Map/need decode:{}", json);
                json = URLDecoder.decode(json, "utf-8");
            }
            return (Map<String, Object>) objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.info("read the json error,json:{}", json, e);
            return null;
        }
    }

    /**
     * string to non_null map
     *
     * @param json json format
     * @return non_null map
     */
    @SuppressWarnings("unused")
    public static Map<String, Object> string2MapWithoutNull(String json) {
        Map<String, Object> resultMap = string2Map(json);
        if (resultMap == null) {
            return Collections.emptyMap();
        }
        return resultMap;
    }

    /**
     * read the node in json,but the node should not have children.
     *
     * @param json      json format string
     * @param paramPath the path should be join with '/',URI.
     * @return node
     */
    @SuppressWarnings("unused")
    public static String getNodeText(String json, String paramPath) {
        if (Strings.isNullOrEmpty(json) || Strings.isNullOrEmpty(paramPath)) {
            return null;
        }
        List<String> path = Lists.newArrayList(Splitter.on('/').omitEmptyStrings().trimResults().split(paramPath));
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(json);
            JsonNode node = rootNode;
            for (String nodeStr : path) {
                node = node.path(nodeStr);
            }
            return node.asText();
        } catch (IOException e) {
            logger.info("read the json node failed,json:{},paramPath:{}", json, paramPath, e);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static String getJacksonVersion() {
        return objectMapper.version().toString();
    }

    public static boolean isJSONValid(String jsonInString ) {
        try {
            objectMapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

//    public static void main(String[] args) {
//        String json = "%7B%22unitId%22%3A2015454%2C%22priceUnit%22%3A%22%C2%A5%22%2C%22roomName%22%3A%22%E3%80%90%E5%8F%A4%E9%87%8C%E2%80%A2%E8%A7%82%E3%80%91%E5%A4%AA%E5%8F%A4%E9%87%8C%2F%E6%98%A5%E7%86%99%E8%B7%AF%2F%E4%B9%9D%E7%9C%BC%E6%A1%A5%E4%B9%90%E4%B8%80%E5%B1%85%22%2C%22price%22%3A258%2C%22ImgUrl%22%3A%22https%3A%2F%2Fpic.tujia.com%2Fupload%2Fqualifiedpics%2Fday_180606%2Fthumb%2F201806062112352885_300_200.jpg%22%7D";

//        Map<String, Object> stringObjectMap = string2Map(json);
//        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
//            System.out.println(entry.getKey() + "---->" + entry.getValue());
//        }
//        String json2 = "{\"unitId\":\"1113723\",\"price\":588,\"priceUnit\":\"¥\",\"roomName\":\"惠州十里银滩·花园别墅四房6床\"}";
//        Map<String, Object> stringObjectMap2 = string2Map(json2);
//        for (Map.Entry<String, Object> entry : stringObjectMap2.entrySet()) {
//            System.out.println(entry.getKey() + "---->" + entry.getValue());
//        }
//    }
}
