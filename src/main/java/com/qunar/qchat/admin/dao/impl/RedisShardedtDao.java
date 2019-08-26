package com.qunar.qchat.admin.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qunar.qchat.admin.dao.IRedisCacheDao;
import com.qunar.qchat.admin.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.concurrent.TimeUnit;

/**
 * Created by qyhw on 02/02/16.
 */
// @Repository("redisSharedDao")
public class RedisShardedtDao implements IRedisCacheDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisShardedtDao.class);
    @Autowired
    private ShardedJedisPool shardedJedisPool;

    /**
     * 向redis存数据，利用Jackson将对象序列化后再存储
     *
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     * @param <T>
     */
    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        ShardedJedis shardedJedis = null;
        try {
            String valueStr = JacksonUtil.obj2String(value);
            // long time = TimeoutUtils.toMillis(timeout, timeUnit);
            shardedJedis = shardedJedisPool.getResource();
            shardedJedis.set(key, valueStr);
        } catch (Exception e) {
            logger.error("向redis中存数据失败，key:{}", key, e);
        } finally {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        }

    }

    /**
     * 从redis获取数据，利用Jackson对json进行反序列化
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(String key, Class<T> clazz) {
        ShardedJedis shardedJedis = null;
        String valueStr;

        try {
            shardedJedis = shardedJedisPool.getResource();
            valueStr = shardedJedis.get(key);

            T value = JacksonUtil.string2Obj(valueStr, clazz);
            return value;
        } catch (Exception e) {
            logger.error("从redis中取数据失败，key:{}", key, e);
            return null;
        } finally {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        }
    }
    
    public String get(String key) {
        ShardedJedis shardedJedis = null;
        String valueStr;

        try {
            shardedJedis = shardedJedisPool.getResource();
            valueStr = shardedJedis.get(key);

            return valueStr;
        } catch (Exception e) {
            logger.error("从redis中取数据失败，key:{}", key, e);
            return null;
        } finally {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        }
    }

    /**
     * 从redis获取数据，利用Jackson对json进行反序列化
     *
     * @param key
     * @param typeReference
     * @param <T>
     * @return
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        ShardedJedis shardedJedis = null;
        String valueStr;

        try {
                shardedJedis = shardedJedisPool.getResource();
                valueStr = shardedJedis.get(key);

            T value = JacksonUtil.string2Obj(valueStr, typeReference);
            return value;
        } catch (Exception e) {
            logger.info("从redis中取数据失败，key:{}", key, e);
            return null;
        } finally {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        }
    }

    /**
     * 把redis的某个key-value对进行删除
     * @param key
     */
    public void del(String key) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = shardedJedisPool.getResource();
            shardedJedis.del(key);
        } catch (Exception e) {
            logger.info("从redis中删除数据失败，key:{}", key, e);
        } finally {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        }

    }

}
