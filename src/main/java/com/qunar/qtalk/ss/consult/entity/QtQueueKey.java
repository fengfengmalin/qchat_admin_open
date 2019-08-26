package com.qunar.qtalk.ss.consult.entity;

import com.qunar.qtalk.ss.utils.common.CacheHelper;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
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
        this.productId = QtSessionItem.DEFAULT_PRODUCTID;
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
//        if (StringUtils.isNotEmpty(productId)) {
//            String hashString = String.format("%d-%s", shopId, productId);
//            hashCode = hashString.hashCode();
//            this.productId = productId;
//        } else {
//            this.productId = QtSessionItem.DEFAULT_PRODUCTID;
//            hashCode = Long.valueOf(shopId).hashCode();
//        }
    }

    public static String redisPatternKey() {
        return "qtqueuekey:*";
    }

    public static String releasedRedisKey() {
        return "released-qtqueuekey:*";
    }

    public static HashSet<QtQueueKey> parseFromRedisToHashSet(String mumus) {

        String value = CacheHelper.get(CacheHelper.CacheType.SeatCache, mumus, String.class);

        logger.debug("parseFromRedisToHashSet, KEY is {} value is {}", mumus, value);

        if (StringUtils.isNotEmpty(value)) {
            HashSet<QtQueueKey> result = null;

            List objects;
            try {
                objects = JsonUtil.parseJSonArray(value);
                if (CollectionUtils.isNotEmpty(objects)) {
                    result = new HashSet<>();

                    for (Object objectMap : objects) {
                        LinkedHashMap<String, Object> item = (LinkedHashMap<String, Object>) objectMap;

//                        Map mapValue = (Map) item.get("userName");
//                        JID user = null;
//                        if (MapUtils.isNotEmpty(mapValue)) {
//                            user = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
//                        }

                        Integer integerValue = (Integer) item.get("shopId");
                        String productId = (String) item.get("productId");
                        QtQueueKey queueKey = new QtQueueKey(integerValue.longValue(), productId);

//                        mapValue = (Map) item.get("seatName");
//                        if (MapUtils.isNotEmpty(mapValue)) {
//                            user = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
//                        }
                        result.add(queueKey);
                    }
                }
            } catch (IOException e) {
                logger.error("parseFromRedisToHashSet error", e);
                return null;
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


    public static void main(String[] args) {
//        QtQueueKey key1 = new QtQueueKey(323);
//        QtQueueKey key2 = new QtQueueKey(323, "asdfdsf");

//        boolean equal = key1.equals(key2);

//        Map<QtQueueKey, String> hash = new ConcurrentHashMap<>();
//        hash.put(key1, "1");
//        hash.put(key2, "2");
//        String v1 = hash.get(new QtQueueKey(323));
//
//        String v2 = hash.get(new QtQueueKey(323, "asdfdsf"));

//        hash.remove(new QtQueueKey(323));

//        v1 = hash.get(new QtQueueKey(323));
//
//        int i = 10;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public String getReleasedKey() {
        return releasedKey;
    }
}
