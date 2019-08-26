package com.qunar.qtalk.ss.utils.common;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.util.Hashing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * create by hubo.hu (lex) at 2018/5/29
 */

public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    public volatile StringRedisTemplate redisTemplate;
//    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static class Holder {
        private static final RedisUtil defaultInstance = new RedisUtil(null);

    }

    private RedisUtil(RedisConfig config) {
        try {
            if (config == null) {
                config = new RedisConfig.Builder().build();
            }
            if (redisTemplate == null) {
                redisTemplate = new StringRedisTemplate(config.getJedisConnectionFactory());
                redisTemplate.afterPropertiesSet();
            }
        } catch (Exception e) {
            logger.error("RedisUtil initialize failed {}", JsonUtil.obj2String(config), e);
        }
    }

    public static final RedisUtil defaultRedis() {
        return Holder.defaultInstance;
    }


    public <T> boolean setNX(int table, String key, T value) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            //   byte[] valueStr = redisTemplate.getValueSerializer().serialize(value);
            //    String valueStr = JacksonUtil.obj2String(value);
            String valueStr = JsonUtil.obj2String(value);
            return valueOperations.setIfAbsent(key, valueStr);
        } catch (Exception e) {
            logger.info("向redis中存数据失败，key:{},value:{}", key, value, e);
        }
        return false;
    }

    public <T> void set(int table, String key, T value, long timeout, TimeUnit timeUnit) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        set(key, value, timeout, timeUnit);
    }

    /**
     * 向redis存数据，利用Jackson对对象进行序列化后再存储
     *
     * @param key      key
     * @param value    value
     * @param timeout  过期时间
     * @param timeUnit 过期时间的单位 {@link TimeUnit}
     * @param <T>      value的类型
     */
    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            //   byte[] valueStr = redisTemplate.getValueSerializer().serialize(value);
            //    String valueStr = JacksonUtil.obj2String(value);
            String valueStr = JsonUtil.obj2String(value);
            valueOperations.set(key, valueStr, timeout, timeUnit);
        } catch (Exception e) {
            logger.info("向redis中存数据失败，key:{},value:{}", key, value, e);
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

    public <T> T get(int table, String key, Class<T> clazz) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return get(key, clazz);
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String valueStr = valueOperations.get(key);
            return JsonUtil.string2Obj(valueStr, clazz);
        } catch (Exception e) {
            logger.info("从redis中取数据失败，key:{}", key, e);
            return null;
        }
    }

    public <T> T get(int table, String key, TypeReference<T> typeReference) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return get(key, typeReference);
    }

    /**
     * 从redis获取数据，利用Jackson对json进行反序列化
     *
     * @param key           key
     * @param typeReference 需要反序列成的对象，针对有泛型的对象
     * @param <T>           保留的对象类型
     * @return 取出来并反序列化后的对象
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            return JsonUtil.string2Obj(valueOperations.get(key), typeReference);
        } catch (Exception e) {
            logger.info("从redis中取数据失败，key:{}", key, e);
            return null;
        }
    }

    public void remove(int table, String key) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        remove(key);
    }

    public void remove(int table, Collection<String> keys) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        remove(keys);
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

    /**
     * 把redis的某一堆key-value对进行删除
     *
     * @param keys key
     */
    public void remove(Collection<String> keys) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.getOperations().delete(keys);
        } catch (Exception e) {
            logger.info("从redis中删除数据失败，key:{}", keys, e);
        }
    }

    /**
     * @param redisKey   key
     * @param increValue increValue
     * @param <T>        类型
     */
    @SuppressWarnings("unchecked")
    public <T> void incr(final T redisKey, final long increValue) {
        redisTemplate.execute(new RedisCallback<T>() {
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer redisSerializer = redisTemplate.getValueSerializer();
                byte[] key = redisSerializer.serialize(redisKey);
                connection.incrBy(key, increValue);
                return null;
            }
        });

    }

    public String hGet(int table, String key, String hashKey) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return hGet(key, hashKey);
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

    /**
     * 获取key列表
     *
     * @param table
     * @param key
     * @return
     */
    public Map<String, String> hGetAll(int table, String key) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return hGetAll(key);
    }

    public Map<String, String> hGetAll(String key) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.entries(key);

        } catch (Exception e) {
            logger.info("从redis中获取hGetAll数据失败，key:{},key:{}", key, e);
            return null;
        }
    }

    public <T> void hPut(int table, String key, String hashKey, T hashValue) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        hPut(key, hashKey, hashValue);
    }

    public <T> void hPut(String key, String hashKey, T hashValue) {
        try {
            if (null == redisTemplate) {
                int a = 10;
                a = 20;
            }

            HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
            hashOperations.put(key, hashKey, hashValue);
        } catch (Exception e) {
            logger.info("向redis中插入hash数据失败，key:{},hashKey:{},hashValue:{}", key, hashKey, hashValue, e);
        }
    }

    public <T> void hDel(int table, String key, String hashKey) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        hDel(key, hashKey);
    }

    public <T> void hDel(String key, String hashKey) {
        try {
            if (null == redisTemplate) {
                int a = 10;
                a = 20;
            }
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
    public static String keyGenerator(String prefix, Object... objects) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object obj : objects) {
            sb.append("_").append(obj.toString());
        }
        return Long.toString(Hashing.MURMUR_HASH.hash(sb.toString()));
    }

    public Set<String> keys(int table, String pattern) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return keys(pattern);
    }

    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            logger.info("从redis中取keys失败，pattern:{}", pattern, e);
            return null;
        }
    }

    /**
     * 提供给xml注入使用，请不要调用该方法,为了防止该方法混入静态方法，请把该方法始终放在该类最后
     *
     * @param _redisTemplate StringRedisTemplate
     */
    public void setRedisTemplate(StringRedisTemplate _redisTemplate) {
        redisTemplate = _redisTemplate;
    }
}
