package com.qunar.qtalk.ss.utils.common;

import org.apache.http.util.TextUtils;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/6/1
 */
public class RedisConfig {

    private JedisConnectionFactory jedisConnectionFactory;
    private boolean isLog = false;

    private RedisConfig(Builder builder) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(builder.maxIdle > 0 ? builder.maxIdle : Config.REDIS_POOL_MAXIDLE);
        jedisPoolConfig.setMaxWaitMillis(builder.maxWaitMillis > 0 ? builder.maxWaitMillis : Config.REDIS_POOL_MAXWAITMILLIS);
        jedisPoolConfig.setMaxTotal(builder.maxTotal > 0 ? builder.maxTotal : Config.REDIS_POOL_MAXACTIVE);
        jedisPoolConfig.setTestOnReturn(builder.isTestOnReturn);
        jedisPoolConfig.setTestOnBorrow(builder.isTestOnBorrow);

        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setMaster(TextUtils.isEmpty(builder.masterName) ? Config.REDIS_SENTINEL_MASTER : builder.masterName);
        if (builder.redisNodes.size() == 0) {
            builder.redisNodes.add(new RedisNode(Config.REDIS_SENTINEL_HOST1, Config.REDIS_SENTINEL_PORT));
            builder.redisNodes.add(new RedisNode(Config.REDIS_SENTINEL_HOST2, Config.REDIS_SENTINEL_PORT));
        }
        redisSentinelConfiguration.setSentinels(builder.redisNodes);

        jedisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration, jedisPoolConfig);
        jedisConnectionFactory.setPassword(TextUtils.isEmpty(builder.password) ? Config.REDIS_SENTINEL_PASS : builder.password);
        jedisConnectionFactory.afterPropertiesSet();
    }

    public JedisConnectionFactory getJedisConnectionFactory() {
        return jedisConnectionFactory;
    }

    public boolean isLog() {
        return isLog;
    }

    public static class Builder {

        private int maxIdle;
        private int maxTotal;
        private int maxWaitMillis;
        private boolean isTestOnBorrow = false;
        private boolean isTestOnReturn = false;

        private String masterName;
        private List<RedisNode> redisNodes;

        private String password;

        private boolean isLog;

        public Builder() {
            redisNodes = new ArrayList<RedisNode>();
        }

        public RedisConfig build() {
            return new RedisConfig(this);
        }

        public Builder setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
            return this;
        }

        public Builder setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        public Builder setMaxWaitMillis(int maxWaitMillis) {
            this.maxWaitMillis = maxWaitMillis;
            return this;
        }

        public Builder testOnBorrow(boolean isTestOnBorrow) {
            this.isTestOnBorrow = isTestOnBorrow;
            return this;
        }

        public Builder testOnReturn(boolean isTestOnReturn) {
            this.isTestOnReturn = isTestOnReturn;
            return this;
        }

        public Builder setMasterName(String name) {
            this.masterName = name;
            return this;
        }

        public Builder addRedisNode(String host, int port) {
            RedisNode redisNode = new RedisNode(host, port);
            redisNodes.add(redisNode);
            return this;
        }

        public Builder setRedisSentinelConfiguration(List<RedisNode> redisNodes) {
            this.redisNodes = redisNodes;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder isLog(boolean islog) {
            this.isLog = islog;
            return this;
        }

    }
}
