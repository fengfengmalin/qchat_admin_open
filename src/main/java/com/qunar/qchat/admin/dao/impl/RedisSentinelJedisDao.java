package com.qunar.qchat.admin.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qunar.qchat.admin.dao.IRedisCacheDao;
import com.qunar.qchat.admin.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by qyhw on 2/2/16.
 */
// @Service("redisSentinelDao")
public class RedisSentinelJedisDao implements IRedisCacheDao {

    private Logger logger = LoggerFactory.getLogger(RedisSentinelJedisDao.class);

    @Resource
    private JedisSentinelPool jedisSentinelPool;

    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();

            String valueStr = JacksonUtil.obj2String(value);
            jedis.set(key, valueStr);
            long time = TimeoutUtils.toSeconds(timeout, timeUnit);
            jedis.expire(key, (int)time);
        } catch (Exception ex) {
            logger.error("向redis存储数据失败，key:{}", key, ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();
            String valueStr = jedis.get(key);
            T value = JacksonUtil.string2Obj(valueStr, clazz);
            return value;
        } catch (Exception ex) {
            logger.error("从redis中取数据失败，key:{}", key, ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();
            return jedis.get(key);
        } catch (Exception ex) {
            logger.error("从redis中取数据失败，key:{}", key, ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();
            String valueStr = jedis.get(key);
            T value = JacksonUtil.string2Obj(valueStr, typeReference);
            return value;
        } catch (Exception e) {
            logger.error("从redis中取数据失败，key:{}", key, e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public void del(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();
            jedis.del(key);
        } catch (Exception ex) {
            logger.error("删除redis数据失败，key:{}", key, ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}
