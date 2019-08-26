package com.qunar.qchat.admin.dao;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.TimeUnit;

/**
 * Created by qyhw on 2/18/16.
 */
public interface IRedisCacheDao {

    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit);

    public String get(String key);

    public <T> T get(String key, Class<T> clazz);

    public <T> T get(String key, TypeReference<T> typeReference);

    public void del(String key);

}
