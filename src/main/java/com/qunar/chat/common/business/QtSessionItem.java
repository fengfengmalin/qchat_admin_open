package com.qunar.chat.common.business;


import com.qunar.chat.common.util.JID;
import com.qunar.chat.service.SpringComponents;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;

public class QtSessionItem {

    private static final Logger logger = LoggerFactory.getLogger(QtSessionItem.class);
    public static final String DEFAULT_PRODUCTID = "*";
    public static final long SESSION_TIME_OUT = 24 * 60 * 60 * 1000;

    private JID userName;
    private long shopId;
    private String productId;
    private Timestamp lastAckTime;
    private long seatId = 0;
    private String webName = "";
    private JID seatQunarName = null;
    private Timestamp inQueueTime;
    private long requestCount;
    private String sessionId;
    private JID shopJid;
   // private int status;
    private QtQueueStatus queueStatus;


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QtSessionItem)) {
            return false;
        }
        QtSessionItem value = (QtSessionItem) obj;
        return (this.shopId == value.shopId &&
//                this.productId.equalsIgnoreCase(value.productId) &&
                this.userName.toBareJID().equalsIgnoreCase(value.userName.toBareJID()));
    }

    public JID getShopJid() {
        return shopJid;
    }

    public QtSessionItem(JID userName, long shopId) {
        this.userName = userName;
        this.shopId = shopId;
        String jid = String.format("shop_%d@%s", shopId, userName.getDomain());
        this.shopJid = JID.parseAsJID(jid);
    }

    public QtSessionItem(JID userName, long shopId, String productId) {
        this.userName = userName;
        this.shopId = shopId;
        String jid = String.format("shop_%d@%s", shopId, userName.getDomain());
        this.shopJid = JID.parseAsJID(jid);

        if (StringUtils.isNotEmpty(productId))
            this.productId = productId;
        else
            this.productId = DEFAULT_PRODUCTID;
    }

    public Timestamp getTime() {
        return lastAckTime;
    }

    public boolean isValid() {

        try {
            if (getSeatId() > 0 && System.currentTimeMillis() - lastAckTime.getTime() < SESSION_TIME_OUT)
                return true;
            return false;

        } catch (Exception e) {
            logger.error("QtSessionItem get isValid failed.", e);
        }
        return false;
    }


    public void setLastAckTime(Timestamp lastAckTime) {
        this.lastAckTime = lastAckTime;
    }

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public JID getSeatQunarName() {
        return seatQunarName;
    }

    public void setSeatQunarName(JID seatQunarName) {
        this.seatQunarName = seatQunarName;
    }

    public long getShopId() {
        return shopId;
    }

    public void setInQueueTime(Timestamp inQueueTime) {
        this.inQueueTime = inQueueTime;
    }

    public Timestamp getInQueueTime() {
        return inQueueTime;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public String getProductId() {
        return productId;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public JID getUserName() {
        return this.userName;
    }

    public void setUserName(JID stringValue) {
        this.userName = stringValue;
    }

    public QtQueueStatus getStatus() {
        return queueStatus;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setStatus(int status) {
        // this.status = status;
        queueStatus = QtQueueStatus.valueOf(status);
    }


    public static QtSessionItem parseFromRedis(String key) {
        Map<String, Object> cacheValue = SpringComponents.components.redisUtil.get( key, Map.class);
        JID from = null;
        if (MapUtils.isNotEmpty(cacheValue)) {
            String stringValue;

            Map<String, Object> mapValue = (Map<String, Object>) cacheValue.get("userName");

//            Map mapValue = cacheValue.get("userName");
//
            if (MapUtils.isNotEmpty(mapValue)) {
                from = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
            }

            Integer integerValue = (Integer) cacheValue.get("shopId");
            stringValue = (String) cacheValue.get("productId");

            QtSessionItem item = new QtSessionItem(from, integerValue.longValue(), stringValue);


            stringValue = (String) cacheValue.get("status");

            if (StringUtils.isNotEmpty(stringValue)) {
                QtQueueStatus status = QtQueueStatus.valueOf(stringValue);
                item.setStatus(status.code);
            }

            integerValue = (Integer) cacheValue.get("seatId");
            item.setSeatId(integerValue.longValue());

            mapValue = (Map<String, Object>) cacheValue.get("seatQunarName");
            if (MapUtils.isNotEmpty(mapValue))
                item.setSeatQunarName(new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true));

            stringValue = (String) cacheValue.get("sessionId");
            if (StringUtils.isNotEmpty(stringValue))
                item.setSessionId(stringValue);

            Long longValue = (Long) cacheValue.get("inQueueTime");
            if (longValue != null)
                item.setInQueueTime(new Timestamp(longValue.longValue()));

            longValue = (Long) cacheValue.get("time");
            if (longValue != null)
                item.setLastAckTime(new Timestamp(longValue.longValue()));

            integerValue = (Integer) cacheValue.get("requestCount");
            if (integerValue != null)
                item.setRequestCount(integerValue.longValue());

            stringValue = (String) cacheValue.get("webName");
            if (StringUtils.isNotEmpty(stringValue))
                item.setWebName(stringValue);

            return item;
        }

        return null;
    }


    public static QtSessionItem parseFromRedis(QtSessionKey key) {
        return parseFromRedis(key.getRedisKey());
    }
}
