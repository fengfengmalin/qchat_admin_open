package com.qunar.chat.common.business;

import com.qunar.chat.common.Cont;
import com.qunar.chat.common.util.JacksonUtils;
import com.qunar.chat.service.SpringComponents;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class QtQueueKey {
    private final long shopId;
    private final String productId;
    private final int hashCode;
    private final String redisKey;
    private final String releasedKey;

    private static final String RedisPatternKey = "qtqueuekey:";
    private static final String ReleasedRedisKey = "released-qtqueuekey:";

    private static final Logger logger = LoggerFactory.getLogger(QtQueueKey.class);

    public QtQueueKey(long shopId) {
        this.shopId = shopId;
        this.productId = Cont.DEFAULT_PRODUCTID;
        hashCode = Long.valueOf(shopId).hashCode();
        redisKey = String.format("%s%s", RedisPatternKey, hashCode);
        releasedKey = String.format("%s%s", ReleasedRedisKey, hashCode);
    }

    public QtQueueKey(long shopId, String productId) {
        this.shopId = shopId;
        this.productId = productId;

        hashCode = Long.valueOf(shopId).hashCode();
        redisKey = String.format("%s%s", RedisPatternKey, hashCode);
        releasedKey = String.format("%s%s", ReleasedRedisKey, hashCode);
    }

    public static String redisPatternKey() {
        return "qtqueuekey:*";
    }

    public static String releasedRedisKey() {
        return "released-qtqueuekey:*";
    }

    public static HashSet<QtQueueKey> parseFromRedisToHashSet(String redisKey) {

        String value = SpringComponents.components.redisUtil.get(redisKey, String.class);

        logger.debug("parseFromRedisToHashSet, KEY is {} value is {}", redisKey, value);

        if (StringUtils.isNotEmpty(value)) {
            HashSet<QtQueueKey> result = null;

            List objects;
            try {
                objects = JacksonUtils.parseJSonArray(value);
                if (CollectionUtils.isNotEmpty(objects)) {
                    result = new HashSet<>();
                    for (Object objectMap : objects) {
                        LinkedHashMap<String, Object> item = (LinkedHashMap<String, Object>) objectMap;

                        Integer integerValue = (Integer) item.get("shopId");
                        String productId = (String) item.get("productId");
                        QtQueueKey queueKey = new QtQueueKey(integerValue.longValue(), productId);
                        result.add(queueKey);
                    }
                }
            } catch (IOException e) {
                logger.error("parseFromRedisToHashSet error", e);
            }
            return result;
        }
        return null;
    }

    public long getShopId() {
        return shopId;
    }

    public String getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QtQueueKey) {
            QtQueueKey queueKey = (QtQueueKey) obj;
            return this.getShopId() == queueKey.getShopId();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("shopId :%d productId: %s hashcode:%d", shopId, productId, hashCode);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public String getReleasedKey() {
        return releasedKey;
    }
}
