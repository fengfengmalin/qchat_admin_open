package com.qunar.chat.service;



import com.qunar.chat.common.business.*;
import com.qunar.chat.common.util.JID;
import com.qunar.chat.config.Config;
import com.qunar.chat.dao.QueueMappingDao;
import com.qunar.chat.entity.QueueMapping;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueueMappingService {

    private static final Logger logger = LoggerFactory.getLogger(QueueMappingService.class);
    @Autowired
    QueueMappingDao queueMappingDao;

    public QtQueueItem getQueue(QtQueueKey qtQueueKey) {
        Set<QueueMapping> mappings = queueMappingDao.selectNoSeatByShopId(qtQueueKey.getShopId());
        QtQueueItem queueItem = new QtQueueItem(qtQueueKey);
        mappings.stream().forEach(queueMapping -> {
            QueueUser qu = new QueueUser();
            String customerName = queueMapping.getCustomerName();
            if (StringUtils.isNotEmpty(customerName) && !customerName.contains("@")) {
                customerName = String.format("%s@%s", customerName, Config.QCHAT_DEFAULT_HOST);
            }
            qu.setJid(JID.parseAsJID(customerName));
            qu.setSessionId(queueMapping.getSessionId());

            queueItem.addSessionItem(qu);
        });
        return queueItem;
    }

    public QueueUser addNewQueue(QtSessionKey key) {
        QueueUser queue = queueMappingDao.addQueue(key.getUserName().toBareJID(), key.getShopId(), key.getProductId());
        if (queue != null) {
            queue.setJid(key.getUserName());
        }
        return queue;
    }

    public int updateInServiceSeatInfo(long seatId, String seatName, String pid, String customerName, long shopId) {
        return queueMappingDao.updateSeatName(seatId, seatName, pid, customerName, shopId);
    }

    public QtSessionItem getConfig(QtSessionKey qtSessionKey) {
        QueueMapping queueMapping = queueMappingDao.selectByCustomerNameAndShopId(qtSessionKey.getUserName().toBareJID(), qtSessionKey.getShopId());
        QtSessionItem sessionItem = new QtSessionItem(qtSessionKey.getUserName(), qtSessionKey.getShopId());
        if (queueMapping != null) {
            sessionItem.setLastAckTime(queueMapping.getLastAckTime());
            sessionItem.setInQueueTime(queueMapping.getInqueueTime());
            sessionItem.setRequestCount(queueMapping.getRequestCount());
            sessionItem.setStatus(queueMapping.getStatus());
            sessionItem.setSeatId(queueMapping.getSeatId());
            sessionItem.setSessionId(queueMapping.getSessionId());
            sessionItem.setSeatQunarName(JID.parseAsJID(queueMapping.getSeatName()));
        }

        return sessionItem;
    }

    public int releseSessionItems(List<String> sessionIds) {
        return queueMappingDao.updateStatusBySessionIds(QtQueueStatus.SeatReleased.code, sessionIds);
    }


    public Collection<QtSessionItem> getValidSessions(Timestamp timeout) {

        List<QueueMapping> queueMappings = queueMappingDao.selectByAckTime(timeout);

        return buildSessionList(queueMappings);
    }

    public Collection<QtSessionItem> finishSessions(Timestamp timeout) {
        List<QueueMapping> queueMappings = queueMappingDao.finishedSession(timeout);
        return buildSessionList(queueMappings);
    }
    public Collection<QtSessionItem> getValidSessions(Timestamp startTime, Timestamp endTime) {
        List<QueueMapping> queueMappings = queueMappingDao.selectBetweenTime(startTime, endTime);
        return buildSessionList(queueMappings);
    }

    public Collection<QtSessionItem> getTimeoutSessions(Timestamp startTime) {
        List<QueueMapping> queueMappings = queueMappingDao.selectTimeoutByStatus(startTime, QtQueueStatus.SeatLast.code);
        return buildSessionList(queueMappings);
    }

    public void removeTimeoutSessions(Collection<QtSessionItem> sessionList) {
        List<String> sessionIds = new ArrayList<>();
        sessionList.forEach(qtSessionItem -> sessionIds.add(qtSessionItem.getSessionId()));
        queueMappingDao.deleteBySessionIds(sessionIds);
    }

    public void removeTimeoutQueues(Collection<QtQueueItem> queueList) {
        List<String> sessionIds = new ArrayList<>();
        queueList.forEach(qtQueueItem -> {
            QueueUser queueUser = qtQueueItem.popQueueUser();
            if (queueUser != null) {
                sessionIds.add(queueUser.getSessionId());
            }
        });
        queueMappingDao.deleteBySessionIds(sessionIds);
    }



    public Collection<QtQueueItem> getTimeoutQtQueueItem(Timestamp timeout) {
        List<QueueMapping> queueMappings = queueMappingDao.selectTimeoutSession(timeout);
        return buildQueueList(queueMappings);
    }

    public Collection<QtQueueItem> getValidQueues(Timestamp timeout) {

        List<QueueMapping> queueMappings = queueMappingDao.selectValidQueue(timeout);
        return buildQueueList(queueMappings);
    }

    private Collection<QtQueueItem> buildQueueList(List<QueueMapping> queueMappings) {
        Map<QtQueueKey, QtQueueItem> maps = new ConcurrentHashMap<>();
        queueMappings.stream().forEach(queueMapping -> {
            QtQueueKey key = new QtQueueKey(queueMapping.getShopId(), queueMapping.getProductId());
            QtQueueItem item = maps.get(key);
            if (item == null) {
                item = new QtQueueItem(key);
                maps.put(key, item);
            }
            QueueUser qu = new QueueUser();
            qu.setJid(JID.parseAsJID(queueMapping.getCustomerName()));
            qu.setSessionId(queueMapping.getSessionId());
            qu.setInQueueTime(queueMapping.getInqueueTime());
            qu.setLastAckTime(queueMapping.getLastAckTime());
            qu.setRequestCount(queueMapping.getRequestCount());
            item.addSessionItem(qu);

        });
        return maps.values();
    }




    private Collection<QtSessionItem> buildSessionList( List<QueueMapping> queueMappings){

        List<QtSessionItem> sessionList = new ArrayList<>();

        queueMappings.stream().forEach(queueMapping -> {
            QtSessionItem sessionItem = new QtSessionItem(JID.parseAsJID(queueMapping.getCustomerName()), queueMapping.getShopId());
            sessionItem.setLastAckTime(queueMapping.getLastAckTime());
            sessionItem.setInQueueTime(queueMapping.getInqueueTime());
            sessionItem.setRequestCount(queueMapping.getRequestCount());
            sessionItem.setStatus(queueMapping.getStatus());
            sessionItem.setSeatId(queueMapping.getSeatId());
            sessionItem.setSessionId(queueMapping.getSessionId());
            sessionItem.setSeatQunarName(JID.parseAsJID(queueMapping.getSeatName()));
            sessionList.add(sessionItem);

        });

        return sessionList;
    }

    public QtSessionKey closeSession(JID user, long shopId, JID seatName) {
        QtSessionKey key = new QtSessionKey(user, shopId);
        QueueMapping queueMapping = queueMappingDao.closeSession(user.toBareJID(), shopId, seatName.toBareJID());
        if (queueMapping != null) {
            key.setProductId(queueMapping.getProductId());

            int deleteResult = queueMappingDao.deleteBySessionIds(Arrays.asList(queueMapping.getSessionId()));
            logger.info("closeSession deleteBySessionIds result:{}", deleteResult);
        }
        return key;
    }

}
