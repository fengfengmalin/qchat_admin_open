package com.qunar.qtalk.ss.utils.common;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.qunar.qtalk.ss.utils.common.RedisUtil.defaultRedis;

public class CacheHelper {
    public enum CacheType {
        SeatCache(8, "SeatCache");

        private final String desc;
        private final int code;

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        CacheType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static CacheType valueOf(int code) {
            for (CacheType e : CacheType.values()) {
                if (e.code == code) {
                    return e;
                }
            }
            throw new RuntimeException("CacheType 不支持这种类型,【code: " + code + "】");
        }
    }

    public static <T> boolean setNX(CacheType type, String key, T value) {
        return defaultRedis().setNX(type.getCode(), key, value);
    }

    public static <T> void set(CacheType type, String key, T value, long timeout, TimeUnit timeUnit) {
        defaultRedis().set(type.getCode(), key, value, timeout, timeUnit);
    }

    public static <T> T get(CacheType type, String key, Class<T> clazz) {
        return defaultRedis().get(type.getCode(), key, clazz);
    }

    public static <T> T get(CacheType type, String key, TypeReference<T> typeReference) {
        return defaultRedis().get(type.getCode(), key, typeReference);
    }

    public static void remove(CacheType type, String key) {
        defaultRedis().remove(type.getCode(), key);
    }

    public static void removeKeys(CacheType type, Collection<String> key) {
        defaultRedis().remove(type.getCode(), key);
    }

    public static String hGet(CacheType type, String key, String hashKey) {
        return defaultRedis().hGet(type.getCode(), key, hashKey);
    }

    public static Set<String> keys(CacheType type, String pattern) {
        return defaultRedis().keys(type.getCode(), pattern);
    }

    public static Map<String, String> hGetAll(CacheType type, String key) {
        return defaultRedis().hGetAll(type.getCode(), key);
    }

    public static <T> void hPut(CacheType type, String key, String hashKey, T hashValue) {
        defaultRedis().hPut(type.getCode(), key, hashKey, hashValue);
    }

    public static <T> void hDel(CacheType type, String key, String hashKey) {
        defaultRedis().hDel(type.getCode(), key, hashKey);
    }

}
