package com.qunar.qtalk.ss.consult.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qunar.qtalk.ss.utils.common.CacheHelper;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class QtQueueItem {

    private final QtQueueKey queueKey;
    private static final Logger logger = LoggerFactory.getLogger(QtQueueItem.class);

    private LinkedList<QueueUser> queueItems = new LinkedList<>();

    public QtQueueItem(QtQueueKey queueKey) {
        this.queueKey = queueKey;
    }

    public static void putItemToRedis(QtQueueKey key, QtQueueItem queueItem) {
        String jsonString = queueItem.toJsonString();

        if (StringUtils.isNotEmpty(jsonString)) {
            logger.debug("put QtQueueItem to redis:{} with key: {}", jsonString, JsonUtil.obj2String(key));
            CacheHelper.set(CacheHelper.CacheType.SeatCache, new QtQueueKey(key.getShopId()).getRedisKey(), jsonString, 1, TimeUnit.DAYS);
        } else {
            logger.error("putItemToRedis failed! {} {} {}", "jsonString is null", JsonUtil.obj2String(key), JsonUtil.obj2String(queueItem));
        }
    }

    protected LinkedList<QueueUser> getQueueItems() {
        return queueItems;
    }

    public QueueUser getSessionItem(JID userName) {
        int pos = queueItems.indexOf(QueueUser.asUser(userName));
        return pos >= 0 ? queueItems.get(pos) : null;
    }

    public int getSessionIndex(QueueUser user) {

        int index = queueItems.indexOf(user);

        logger.debug("queue pos is {}\nQtQueueItem: {} \nqueue:{} \nuser:{}\n", index, JsonUtil.obj2String(queueKey), JsonUtil.obj2String(queueItems), JsonUtil.obj2String(user));

        return index;
    }

    public void addSessionItem(QueueUser sessionItem) {
        logger.debug("before addsessionitem\n{}", JsonUtil.obj2String(queueItems));
        if (!queueItems.contains(sessionItem))
            queueItems.addLast(sessionItem);
        logger.debug("QtQueueItem {}\naddsessionitem{}\n", toJsonString(), JsonUtil.obj2String(sessionItem));
    }

    public QueueUser popQueueUser() {
        if (queueItems.size() <= 0)
            return null;
        return queueItems.pop();
    }

    public void pushFrontItem(QueueUser item) {
        queueItems.addFirst(item);
    }

    public boolean isEmpty() {
        return queueItems.size() <= 0;
    }

    public void removeItem(QueueUser queueUser) {
        queueItems.remove(queueUser);
    }

    public QtQueueKey getQueueKey() {
        return queueKey;
    }


//    private static QtQueueItem loadQtQueueItemWithJsonString(String s) {

//        Map<String, Object> cacheValue = JsonUtil.parseJSONObject(s);
//
//        try {
//            if (MapUtils.isNotEmpty(cacheValue)) {
//                QtQueueItem item = null;
//
//                String stringValue = (String) cacheValue.get("queueKey");
//                if (StringUtils.isNotEmpty(stringValue)) {
//                    QtQueueKey queueKey;
//                    Map<String, Object> mapValue = JsonUtil.parseJSONObject(stringValue);
//                    if (MapUtils.isNotEmpty(mapValue)) {
//                        Integer integerValue = (Integer) mapValue.get("shopId");
//                        String productId = (String) mapValue.get("productId");
//                        queueKey = new QtQueueKey(integerValue.longValue(), productId);
//                        item = new QtQueueItem(queueKey);
//                    }
//                }
//                if (item != null) {
//                    stringValue = (String) cacheValue.get("queueItem");
//                    logger.debug("record a cache items {}", stringValue);
//                    if (StringUtils.isNotEmpty(stringValue)) {
//                        List<Map<String, Object>> objectList = JsonUtil.parseJSonArray(stringValue);
//                        if (CollectionUtils.isNotEmpty(objectList)) {
//                            for (Map<String, Object> objectMap : objectList) {
//                                QueueUser user = new QueueUser();
//
//                                Map<String, Object> mapValue = (Map<String, Object>) objectMap.get("jid");
//                                if (MapUtils.isNotEmpty(mapValue)) {
//                                    JID from = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
//                                    user.setJid(from);
//                                }
//
//                                stringValue = (String) objectMap.get("sessionId");
//                                if (StringUtils.isNotEmpty(stringValue))
//                                    user.setSessionId(stringValue);
//
//                                Long longValue = (Long) objectMap.get("inQueueTime");
//                                if (longValue != null)
//                                    user.setInQueueTime(new Timestamp(longValue.longValue()));
//
//                                longValue = (Long) objectMap.get("lastAckTime");
//                                if (longValue != null)
//                                    user.setLastAckTime(new Timestamp(longValue.longValue()));
//
//                                Integer integerValue = (Integer) objectMap.get("requestCount");
//                                if (integerValue != null)
//                                    user.setRequestCount(integerValue.intValue());
//                                item.addSessionItem(user);
//                            }
//                        }
//                    }
//                }
//                return item;
//            }
//        } catch (IOException e) {
//            logger.error("parseFromRedis failed!", e);
//        }
//        return null;
//    }


    public static QtQueueItem parseFromRedis(QtQueueKey key) {
//        String content = CacheHelper.get(CacheHelper.CacheType.SeatCache, key.getRedisKey(), String.class);
//
//        QtQueueItem item = null;
//        try {
//            item = QtQueueItem.loadQtQueueItemWithJsonString(content);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return item;

        Map<String, Object> cacheValue = CacheHelper.get(CacheHelper.CacheType.SeatCache, key.getRedisKey(), Map.class);

        try {
            if (MapUtils.isNotEmpty(cacheValue)) {
                QtQueueItem item = null;

                String stringValue = (String) cacheValue.get("queueKey");
                if (StringUtils.isNotEmpty(stringValue)) {
                    QtQueueKey queueKey;
                    Map<String, Object> mapValue = JsonUtil.parseJSONObject(stringValue);
                    if (MapUtils.isNotEmpty(mapValue)) {
                        Integer integerValue = (Integer) mapValue.get("shopId");
                        String productId = (String) mapValue.get("productId");
                        queueKey = new QtQueueKey(integerValue.longValue(), productId);
                        item = new QtQueueItem(queueKey);
                    }
                }
                if (item != null) {
                    stringValue = (String) cacheValue.get("queueItem");
                    logger.debug("record a cache items {}", stringValue);
                    if (StringUtils.isNotEmpty(stringValue)) {
                        List<Map<String, Object>> objectList = JsonUtil.parseJSonArray(stringValue);
                        if (CollectionUtils.isNotEmpty(objectList)) {
                            for (Map<String, Object> objectMap : objectList) {
                                QueueUser user = new QueueUser();

                                Map<String, Object> mapValue = (Map<String, Object>) objectMap.get("jid");
                                if (MapUtils.isNotEmpty(mapValue)) {
                                    JID from = new JID((String) mapValue.get("node"), (String) mapValue.get("domain"), (String) mapValue.get("resources"), true);
                                    user.setJid(from);
                                }

                                stringValue = (String) objectMap.get("sessionId");
                                if (StringUtils.isNotEmpty(stringValue))
                                    user.setSessionId(stringValue);

                                Long longValue = (Long) objectMap.get("inQueueTime");
                                if (longValue != null)
                                    user.setInQueueTime(new Timestamp(longValue.longValue()));

                                longValue = (Long) objectMap.get("lastAckTime");
                                if (longValue != null)
                                    user.setLastAckTime(new Timestamp(longValue.longValue()));

                                Integer integerValue = (Integer) objectMap.get("requestCount");
                                if (integerValue != null)
                                    user.setRequestCount(integerValue.intValue());
                                item.addSessionItem(user);
                            }
                        }
                    }
                }
                return item;
            }
        } catch (Exception e) {
            logger.error("parseFromRedis failed!", e);
        }
        return null;
    }



    public String toJsonString() {

        Map<String, Object> resultMapping = new HashedMap();
        resultMapping.put("queueKey", JsonUtil.obj2String(queueKey));
        String queueItem = CollectionUtils.isEmpty(queueItems) ? "" : JsonUtil.obj2String(queueItems);
        resultMapping.put("queueItem", queueItem);
        String result = null;
        try {
            result = JsonUtil.toJSONString(resultMapping);
        } catch (JsonProcessingException e) {
            logger.error("QtQueueItem toJsonString failed!", e);
        }
        return result;
    }
}
