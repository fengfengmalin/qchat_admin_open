package com.qunar.chat.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import redis.clients.util.Hashing;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class RedisUtil {
    private final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> void set(String key, T value) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String valueStr = obj2String(value);
            valueOperations.set(key, valueStr);
        } catch (Exception e) {
            logger.info("向redis中存数据失败，key:{},configinfo:{}", key, value, e);
        }
    }

    /**
     * 向redis存数据，利用Jackson对对象进行序列化后再存储
     *
     * @param key      key
     * @param value    configinfo
     * @param timeout  过期时间
     * @param timeUnit 过期时间的单位 {@link TimeUnit}
     * @param <T>      value的类型
     */
    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String valueStr = obj2String(value);
            valueOperations.set(key, valueStr, timeout, timeUnit);
        } catch (Exception e) {
            logger.info("向redis中存数据失败，key:{},configinfo:{}", key, value, e);
        }

    }

    /**
     * 从redis获取数据，利用Jackson对json进行反序列化
     *
     * @param key   key
     * @param clazz 需要反序列化成的对象的class对象
     * @param <T>   class对象保留的对象类型
     * @return 取出来并反序列后的对象
     */

    public <T> T get(String key, Class<T> clazz) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String valueStr = valueOperations.get(key);
            return string2Obj(valueStr, clazz);
        } catch (Exception e) {
            logger.info("从redis中取数据失败，key:{}", key, e);
            return null;
        }
    }

    public String get(String key) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            return valueOperations.get(key);
        } catch (Exception e) {
            logger.info("从redis中取数据失败，key:{}", key, e);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        return get(key, typeReference);
    }

    /**
     * 把redis的某个key-value对进行删除
     *
     * @param key key
     */
    public void remove(String key) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.getOperations().delete(key);
        } catch (Exception e) {
            logger.info("从redis中删除数据失败，key:{}", key, e);
        }
    }


//    public <T> void incr(final T redisKey, final long increValue) {
//        redisTemplate.execute(new RedisCallback<T>() {
//            @Override
//            public T doInRedis(RedisConnection connection) throws DataAccessException {
//                RedisSerializer redisSerializer = redisTemplate.getValueSerializer();
//                byte[] key = redisSerializer.serialize(redisKey);
//
//                connection.incrBy(key, increValue);
//                return null;
//            }
//        });
//
//    }


    public Set<String> hkeys(String key) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.keys(key);
        } catch (Exception e) {
            logger.info("向redis中存数据失败，key:{},e:{}", key, e);
        }
        return Sets.newHashSet();
    }


    public String hGet(String key, String hashKey) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.get(key, hashKey);
        } catch (Exception e) {
            logger.info("从redis中获取hash数据失败，key:{},hashKey:{}", key, hashKey, e);
            return null;
        }
    }

    public <T> void hPut(String key, String hashKey, T hashValue, long timeout, TimeUnit timeUnit) {
        hPut(key, hashKey, hashValue);
        redisTemplate.expire(key, timeout, timeUnit);
    }

    public <T> void hPut(String key, String hashKey, T hashValue) {
        try {
            HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
            hashOperations.put(key, hashKey, hashValue);
        } catch (Exception e) {
            logger.info("向redis中插入hash数据失败，key:{},hashKey:{},hashValue:{}", key, hashKey, hashValue, e);
        }
    }

    public <T> void hDel(String key, String hashKey) {
        try {

            HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
            hashOperations.delete(key, hashKey);
        } catch (Exception e) {
            logger.info("向redis中删除hash数据失败，key:{},hashKey:{},e:{}", key, hashKey, e);
        }

    }

    /**
     * redis的key生成器
     *
     * @param prefix  key的前缀
     * @param objects 后面加的一些唯一标示
     * @return key
     */
    public String keyGenerator(String prefix, Object... objects) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object obj : objects) {
            sb.append("_").append(obj.toString());
        }
        return Long.toString(Hashing.MURMUR_HASH.hash(sb.toString()));
    }

    public <T> String obj2String(T obj) {

        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.info("serialize Object to String failed,Object:{}", obj.getClass(), e);
            return null;
        }
    }

    public <T> T string2Obj(String json, Class<T> clazz) {
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

    public <T> T string2Obj(String json, TypeReference<T> typeReference) {
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


    public Map<String, String> hGetAll(String key) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.entries(key);

        } catch (Exception e) {
            logger.error("从redis中获取hGetAll数据失败，key:{},key:{}", key, e);
            return null;
        }
    }

    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);

    }

    public void delete(String key) {
        redisTemplate.delete(key);

    }

    public boolean setNX(String key, Object value) {
        ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
        String resultStr = JacksonUtils.obj2String(value);
        return valueOperations.setIfAbsent(key, resultStr);
    }

    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public void removeKeys(Collection<String> keys) {
        ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
        valueOperations.getOperations().delete(keys);
    }
}
