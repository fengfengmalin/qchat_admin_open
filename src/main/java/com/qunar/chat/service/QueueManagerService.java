package com.qunar.chat.service;

import com.google.common.eventbus.Subscribe;
import com.qunar.chat.common.Cont;
import com.qunar.chat.common.business.*;
import com.qunar.chat.common.util.ConsultUtils;
import com.qunar.chat.common.util.JID;
import com.qunar.chat.common.util.JacksonUtils;
import com.qunar.chat.common.util.RedisUtil;
import com.qunar.chat.config.Config;
import com.qunar.chat.dao.QueueMappingDao;
import com.qunar.chat.dao.ShopDao;
import com.qunar.chat.entity.Seat;
import com.qunar.chat.entity.Shop;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class QueueManagerService {

    private static final Logger logger = LoggerFactory.getLogger(QueueManagerService.class);


    @Autowired
    QueueMappingDao queueMappingDao;
    @Autowired
    SeatService seatService;
    @Autowired
    QueueMappingService queueMappingService;
    @Autowired
    QtSessionWatcherService qtSessionWatcherService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ShopDao shopDao;


    public void closeSession(JID jid, JID seatName, long shopId) {

        try {
            QtSessionKey sessionKey = queueMappingService.closeSession(jid, shopId, seatName);

            QtSessionItem var = QtSessionItem.parseFromRedis(sessionKey);

            logger.debug("will remove session: {}", var);
            JID from = Shop.parseLong(shopId, Config.QCHAT_DEFAULT_HOST);
            ConsultUtils.sendMessage(from, seatName, jid, seatName, "会话已关闭", true, false, true);
            if (var == null)
                logger.debug("impossable this is no session in my cache!{}", JacksonUtils.obj2String(sessionKey));
            else {
                redisUtil.remove(sessionKey.getRedisKey());
                redisUtil.remove(sessionKey.getReleasedKey());

                LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", seatName.toBareJID()));

                logger.debug("close session, seatName={}, list is {}", JacksonUtils.obj2String(seatName), JacksonUtils.obj2String(lists));

                if (CollectionUtils.isNotEmpty(lists)) {
                    lists.remove(sessionKey);
                    redisUtil.set(String.format("fromtoMapping:%s", seatName.toBareJID()), JacksonUtils.obj2String(lists), 1, TimeUnit.DAYS);
                    logger.debug("close session removed, seatName={}\nlist is {}\n sessionKey is{}\n",
                            JacksonUtils.obj2String(seatName), JacksonUtils.obj2String(lists), JacksonUtils.obj2String(sessionKey));
                }
                redistribution(sessionKey.getShopId(), sessionKey.getProductId(), null, seatName.getDomain(), false);
            }
        } catch (Exception e) {
            logger.error("closeSession failed, {} {} {}", jid, seatName, shopId, e);
        }
    }


    /***
     * 重新分配客服和客人的对应关系
     * @param shopId 商铺id
     * @param productId 商品id
     * @param userName 请求的客人的id
     * @param sendQueueInfo 是否发送排队信息，如果本次调用是客人说话引起的，那么会给客人提示当前队列信息，否则不提示
     * @return
     */
    private QtSessionItem redistribution(long shopId, String productId, JID userName, String host, boolean sendQueueInfo) {

        logger.info("redistribution : shop:{}\n pid:{}\n username:{}\n", shopId, productId, userName);

        QtQueueKey queueKey = new QtQueueKey(shopId, productId);

        QtQueueItem queueItem = QtQueueItem.parseFromRedis(new QtQueueKey(queueKey.getShopId()));

        logger.debug("redistributioning and queueItem is {}", queueItem == null ? null : queueItem.toJsonString());
        QtSessionKey sessionKey = null;
        if (userName != null) {
            // 如果用户正在聊天中，直接返回
            sessionKey = new QtSessionKey(userName, shopId, productId);
            QtSessionItem configValue = QtSessionItem.parseFromRedis(sessionKey);
            if (configValue != null) {
                return configValue;
            }

            // 如果用户当前未分配，新建队列
            if (queueItem == null) {
                queueItem = queueMappingService.getQueue(queueKey);
            }

            QueueUser queueUser;
            queueUser = queueItem.getSessionItem(userName);

            if (queueUser == null) {
                queueUser = queueMappingService.addNewQueue(new QtSessionKey(userName, shopId, productId));
                queueItem.addSessionItem(queueUser);
                logger.debug("queueItem is {}", queueItem.toJsonString());
            }

            QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);
        } else {
            if (queueItem == null) {
                queueItem = queueMappingService.getQueue(queueKey);

                QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);
            }
        }

        boolean next;
        QtSessionItem value = null;
        do {
            next = false;
            QueueUser currentUser = queueItem.popQueueUser();

            if (currentUser != null) {

                logger.debug("{} judgmentOrRedistributionEx on {} and sift failed.", currentUser.getJid(), shopId);

                Seat csr = seatService.siftSeat(shopId);

                logger.info("shopId:{} jid:{} productId:{}  siftStrategyService done. {}",
                        queueKey.getShopId(), currentUser.getJid(), queueKey.getProductId(), JacksonUtils.obj2String(csr));
                if (csr != null) {
                    next = true;
                    QtSessionItem localValue;

                    try {

                        JID shopIdJid = JID.parseAsJID(String.format("shop_%d@%s", queueKey.getShopId(), host));

                        ConsultUtils.sendWelcome(shopIdJid, currentUser.getJid(), csr.getQunarName());

                        QtSessionKey newKey = new QtSessionKey(currentUser.getJid(), queueKey.getShopId(), queueKey.getProductId());

                        localValue = new QtSessionItem(currentUser.getJid(), queueKey.getShopId(), queueKey.getProductId());
                        localValue.setSeatQunarName(csr.getQunarName());
                        localValue.setSessionId(currentUser.getSessionId());
                        localValue.setSeatId(csr.getId());
                        localValue.setLastAckTime(new Timestamp(System.currentTimeMillis()));

                        // 分配成功！更新一下客服in service时间
                        // 这里设置一下客服的信息，并且要更新数据库
                        queueMappingService.updateInServiceSeatInfo(csr.getId(), csr.getQunarName().toBareJID(), newKey.getProductId(), currentUser.getJid().toBareJID(), shopId);


                        boolean succeeded = redisUtil.setNX(newKey.getRedisKey(), localValue);

                        if (succeeded) {
                            if (userName != null && StringUtils.equalsIgnoreCase(userName.toBareJID(), localValue.getUserName().toBareJID())) {
                                value = localValue;
                            }

                            LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", csr.getQunarName().toBareJID()));
                            if (lists == null)
                                lists = new LinkedList<>();
                            lists.add(newKey);
                            redisUtil.set(String.format("fromtoMapping:%s", csr.getQunarName().toBareJID()), JacksonUtils.obj2String(lists), 1, TimeUnit.DAYS);

                            HashSet<QtQueueKey> sets = QtQueueKey.parseFromRedisToHashSet(String.format("predistributionMapping:%s", currentUser.getJid().toBareJID()));
                            if (sets != null) {
                                sets.remove(queueKey);
                                redisUtil.set(String.format("predistributionMapping:%s", currentUser.getJid().toBareJID()), JacksonUtils.obj2String(sets), 1, TimeUnit.DAYS);
                            }

                            // 获取历史消息，扔到队列中
                            ConsultUtils.resendUnsentMesasge(queueKey.getShopId(), currentUser.getJid());
                        } else {
                            QtSessionItem item = QtSessionItem.parseFromRedis(newKey);
                            if (item != null)
                                value = item;
                        }


                    } catch (Exception e) {

                        queueItem.pushFrontItem(currentUser);
                        QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);

                        throw e;
                    }
                } else {
                    logger.info("siftStrategyService sift a null., {} {} {}", queueKey.getProductId(), queueKey.getShopId(), currentUser.getJid());

                    if (sendQueueInfo) {

                        int pos = queueItem.getSessionIndex(QueueUser.asUser(userName));
                        if (pos <= 0)
                            pos = 1;
                        else
                            pos += 1;

                        String content;
                        Shop shop = shopDao.selectShopById(shopId);
                        if (shop != null && shop.getOpenQueueStatus() != 0) {
                            content = String.format("咨询人数过多需排队，当前排号%d位，您可以继续咨询，小驼看到后将第一时间答复", pos);
                        } else {
                            content = shop != null && StringUtils.isNotEmpty(shop.getNoServiceWelcomes()) ? shop.getNoServiceWelcomes() : "当前没有可用客服，请稍候咨询";
                        }
//                        String supplierId = hotlineSupplierService.selectHotlineBySupplierId(shopId);
                        JID from = Shop.parseLong(shopId, Config.QCHAT_DEFAULT_HOST);
                        ConsultUtils.sendMessage(from, userName, Shop.parseLong(shopId, host), userName, content, false, false, true);
                    }


                    queueItem.pushFrontItem(currentUser);
                    QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);

                    return null;
                }
            }
            if (next) {
                QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);
            }
        } while (next);

        return value;
    }


    private QtSessionItem customerDistributionFromDB(QtSessionKey key) {
        QtSessionItem value = queueMappingService.getConfig(key);

        if (value != null && value.isValid()) {
            redisUtil.setNX(key.getRedisKey(), value);
            logger.info("customerDistributionFromDB QtSessionItem:{}", value);
            return value;
        }
        return null;
    }

    /***
     * 搞一下当前客服曾经服务过的Session列表(商铺和产品纬度)
     * @param userName 用户名称
     * @param shopId 商铺id
     * @param productId 商品id
     * @param isEx 是否强制重新分配
     * @return
     */
    public QtSessionItem judgmentOrRedistribution(JID userName, long shopId, String productId, String host, boolean isEx) {

        QtSessionKey key = new QtSessionKey(userName, shopId, productId);
        QtSessionItem result = customerDistribution(key, false);

        if (result == null || !result.isValid() || isEx || invalidCsr(shopId, host, result.getSeatQunarName())) {
            redisUtil.remove(key.getRedisKey());
            result = redistribution(shopId, productId, userName, host, true);

        }
        return result;
    }


    public JID getRealTo(JID from, long shopId, String productId, String host, boolean noUpdate) {
        logger.info("record a real to :{} - {}", from, shopId);
        JID result = null;

        QtSessionItem queueItem = judgmentOrRedistribution(from, shopId, productId, host, false);
        if (queueItem != null) {
            result = queueItem.getSeatQunarName();
            if (!noUpdate)
                saveMsgLog(from, queueItem.getShopId(), System.currentTimeMillis(), true);
        }
        logger.info("get real to:{}", result);
        return result;
    }

    public void saveMsgLog(JID customer, long shopId, long time, boolean isCustomerMsg) {

        int status = isCustomerMsg ? QtQueueStatus.CustomerLast.getCode() : QtQueueStatus.SeatLast.getCode();
        queueMappingDao.updateByNameAndShopId(new Date(time), status, customer.toBareJID(), shopId);
    }

    private QtSessionItem customerDistribution(QtSessionKey sessionKey, boolean readFromDB) {
        QtSessionItem configValue = QtSessionItem.parseFromRedis(sessionKey);
        logger.info("customerDistribution QtSessionItem{}", JacksonUtils.obj2String(configValue));
        if (configValue == null && readFromDB)
            return customerDistributionFromDB(sessionKey);
        return configValue;
    }


    public void initialize() {
        qtSessionWatcherService.eventBus.register(this);
        logger.info("QtSessionManager is initialized!");
    }

    @Subscribe
    public void onMessage(SessionAction action) {

        if (action.getAction().equals(Action.Add)) {
            for (QtSessionItem item : action.getSessionIds()) {
                if (item.isValid()) {
                    QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId(), item.getProductId());
                    redisUtil.setNX(sessionKey.getRedisKey(), item);
                }
            }
        } else if (action.getAction().equals(Action.RemoveAll)) {
            Set<String> keys = redisUtil.keys(QtSessionKey.redisPatternKey());
            redisUtil.removeKeys(keys);
        } else if (action.getAction().equals(Action.Remove)) {
            for (QtSessionItem item : action.getSessionIds()) {
                QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId(), item.getProductId());
                redisUtil.remove(sessionKey.getRedisKey());
            }
        } else {
            logger.info("i don't know what you want{}", action.getAction());
        }
    }

    @Subscribe
    public void onMessage(QueueAction action) {

        if (action.getAction().equals(Action.Add)) {

            for (QtQueueItem item : action.getQueueList()) {
                QtQueueKey queueKey = item.getQueueKey();

                QtQueueItem queueItem = QtQueueItem.parseFromRedis(new QtQueueKey(queueKey.getShopId()));


                if (queueItem == null) {
                    QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), item);
                } else {
                    QueueUser user;
                    while ((user = item.popQueueUser()) != null) {
                        queueItem.addSessionItem(user);
                    }
                }
            }
        } else if (action.getAction().equals(Action.RemoveAll)) {


            Set<String> keys = redisUtil.keys(QtQueueKey.redisPatternKey());
            redisUtil.removeKeys(keys);

        } else if (action.getAction().equals(Action.Remove)) {
            for (QtQueueItem item : action.getQueueList()) {
                QtQueueKey queueKey = item.getQueueKey();

                QtQueueItem queueItem = QtQueueItem.parseFromRedis(new QtQueueKey(queueKey.getShopId()));

                if (queueItem != null) {
                    QueueUser user;
                    while ((user = item.popQueueUser()) != null) {
                        queueItem.removeItem(user);
                    }
                }
            }
        } else {
            logger.info("i don't know what you want{}", action.getAction());
        }
    }

    @Subscribe
    public void onMessage(SessionTimeoutAction action) {
        logger.debug("received SessionTimeoutAction:{}", action.getActionItems().toArray());

        if (action.getAction().equals(Action.RemoveAll)) {
            Set<String> sets = redisUtil.keys("fromtoMapping:*");
            redisUtil.removeKeys(sets);

        } else if (action.getAction().equals(Action.Remove)) {
            List<String> sessionList = new ArrayList<>();

            for (QtSessionItem item : action.getActionItems()) {

                try {
                    JID seatName = item.getSeatQunarName();

                    LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", seatName.toBareJID()));

                    if (lists != null) {
                        logger.info("ready to remove seatName:{} 's queue, queue size :{}", seatName, lists.size());
                        QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId());
                        if (lists.contains(sessionKey)) {
                            lists.remove(sessionKey);
                            redisUtil.set(String.format("fromtoMapping:%s", seatName.toBareJID()), JacksonUtils.obj2String(lists), 1, TimeUnit.DAYS);
                        }
                    }

                    QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId(), item.getProductId());

                    QtSessionItem var = QtSessionItem.parseFromRedis(sessionKey);
                    if (var != null) {
//                        QtQueueKey queueKey = new QtQueueKey(item.getShopId(), item.getProductId());
                        redisUtil.set(sessionKey.getReleasedKey(), var, Config.Redis_Released_Session_Time, TimeUnit.MINUTES);
                        redisUtil.remove(sessionKey.getRedisKey());
                        sessionList.add(item.getSessionId());

                    }
                    redistribution(item.getShopId(), item.getProductId(), null, seatName.getDomain(), false);

                } catch (Exception e) {
                    logger.error("ON EVENT: SessionTimeoutAction failed.", e);
                }
            }

            if (CollectionUtils.isNotEmpty(sessionList)) {
                logger.info("remove {} sessions from db", sessionList.size());
                queueMappingService.releseSessionItems(sessionList);
            }
        }
    }

    @Subscribe
    public void onMessage(ClearAction action) {
        logger.info("received Clear All request");
        Set<String> keys = redisUtil.keys("predistributionMapping:*");
        if (CollectionUtils.isNotEmpty(keys))
            redisUtil.removeKeys(keys);
        keys = redisUtil.keys(QtQueueKey.releasedRedisKey());
        if (CollectionUtils.isNotEmpty(keys))
            redisUtil.removeKeys(keys);
    }

    private boolean invalidCsr(long shopId, String host, JID csrJid) {
        // 校验 session 中的 客服是否已被删除
        String seatName = csrJid != null ? csrJid.getNode() : "";
        List<Seat> csrList = seatService.queryCsrByQunarNameAndShopId(seatName, shopId, host);
        return CollectionUtils.isEmpty(csrList);
    }

}
