package com.qunar.qchat.admin.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qunar.qchat.admin.constants.RedisConstants;
import com.qunar.qchat.admin.dao.IRedisCacheDao;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by qyhw on 2/18/16.
 */
@Component("redisAgent")
public class RedisAgentUtil {

    private IRedisCacheDao redisDao;

    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {

        redisDao.set(RedisConstants.KEY_GLOBAL_RRE + key, value, timeout, timeUnit);
    }

    public String get(String key){

        return redisDao.get(RedisConstants.KEY_GLOBAL_RRE + key);
    }

    public <T> T get(String key, Class<T> clazz){

        return redisDao.get(RedisConstants.KEY_GLOBAL_RRE + key, clazz);
    }

    public <T> T get(String key, TypeReference<T> typeReference){

        return redisDao.get(RedisConstants.KEY_GLOBAL_RRE + key, typeReference);
    }

    public void del(String key){

        redisDao.del(RedisConstants.KEY_GLOBAL_RRE + key);
    }

}
