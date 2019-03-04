package com.qunar.chat.common.business;


import com.qunar.chat.common.util.JID;
import com.qunar.chat.common.util.JacksonUtils;
import com.qunar.chat.service.SpringComponents;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.msgpack.annotation.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Message
public class QtSessionKey {
    private static final Logger logger = LoggerFactory.getLogger(QtSessionKey.class);

    private JID userName;
    private long shopId;
    private String productId;
    private JID seatName;

    private int hashcode;

    private String redisKey;

    private String releasedKey;

    public QtSessionKey(JID userName, long shopId) {
        initWityKey(userName, shopId, QtSessionItem.DEFAULT_PRODUCTID);
    }

    public static String redisPatternKey() {
        return "qtsessionkey:*";
    }

    private void initWityKey(JID userName, long shopId, String productId) {
        this.userName = userName;
        if (StringUtils.isEmpty(productId))
            productId = QtSessionItem.DEFAULT_PRODUCTID;
        this.productId = productId;
        this.shopId = shopId;

        String hashString = String.format("%s-%d", userName, shopId);
        hashcode = hashString.hashCode();

        redisKey = String.format("qtsessionkey:%s-%s-%d", hashcode, userName.toBareJID(), shopId);
        releasedKey = String.format("released-qtqueuekey:%s-%s-%d", hashcode, userName.toBareJID(), shopId);
    }

    public void setUserName(JID userName) {
        this.userName = userName;

        String hashString = String.format("%s-%d", this.userName, shopId);
        hashcode = hashString.hashCode();
    }

    public QtSessionKey(JID userName, long shopId, String productId) {
        initWityKey(userName, shopId, productId);
    }

    public JID getUserName() {
        return userName;
    }

    public long getShopId() {
        return shopId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QtSessionKey) {
            QtSessionKey sessionKey = (QtSessionKey) obj;

            if (this.hashCode() == sessionKey.hashCode())
                return true;
            if (this.getUserName().toBareJID().equalsIgnoreCase(sessionKey.getUserName().toBareJID())
                    &&
                    this.getShopId() == sessionKey.getShopId())
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public String getProductId() {
        return productId;
    }

    public JID getSeatName() {
        return seatName;
    }

    @Override
    public String toString() {
//        private JID userName;
//        private long shopId;
//        private String productId;
//        private JID seatName;
//
//        private int hashcode;

        return JacksonUtils.obj2String(this);
    }

    public static LinkedList<QtSessionKey> parseFromRedisToLinkedList(String redisKey) {
        String value = SpringComponents.components.redisUtil.get( redisKey, String.class);
        if (StringUtils.isNotEmpty(value)) {
            LinkedList<QtSessionKey> result = null;

            List objects;
            try {
                objects = JacksonUtils.parseJSonArray(value);
                if (CollectionUtils.isNotEmpty(objects)) {
                    result = new LinkedList<>();

                    for (Object objectMap : objects) {
                        LinkedHashMap<String, Object> item = (LinkedHashMap<String, Object>) objectMap;

                        Map mapValue = (Map) item.get("userName");
                        JID user = null;
                        if (MapUtils.isNotEmpty(mapValue)) {
                            user = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
                        }

                        Integer integerValue = (Integer) item.get("shopId");
                        String productId = (String) item.get("productId");
                        QtSessionKey sessionKey = new QtSessionKey(user, integerValue.longValue(), productId);

                        mapValue = (Map) item.get("seatName");
                        if (MapUtils.isNotEmpty(mapValue)) {
                            user = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
                        }
                        sessionKey.setSeatName(user);
                        result.add(sessionKey);
                    }
                }
            } catch (IOException e) {
                logger.error("parseFromRedisToLinkedList error", e);
            }

            return result;
        }
        return null;
    }

    public void setSeatName(JID seatName) {
        this.seatName = seatName;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public String getReleasedKey() {
        return releasedKey;
    }
}
