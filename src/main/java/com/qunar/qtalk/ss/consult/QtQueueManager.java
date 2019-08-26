package com.qunar.qtalk.ss.consult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.Subscribe;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.constants.Config;
import com.qunar.qtalk.ss.consult.entity.*;
import com.qunar.qtalk.ss.sift.entity.BusiShopMapping;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.model.DistributedInfo;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.common.CacheHelper;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class QtQueueManager {

    //
    // 客人和客服的对应关系， 这个访问量是最大的
//    private ConcurrentHashMap<QtSessionKey, QtSessionItem> sessionMapping = new ConcurrentHashMap<>();

    //
    // 当前商铺排队的队列
    private ConcurrentHashMap<QtQueueKey, QtQueueItem> queueMapping = new ConcurrentHashMap<>();

    //
    // 已经释放了的mapping，但是需要考虑24小时的有效性
//    private ConcurrentHashMap<QtQueueKey, QtSessionItem> releasedMapping = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(QtQueueManager.class);

    //
    // 当前客服服务的客人列表
    private ConcurrentHashMap<String, LinkedList<QtSessionKey>> fromtoMapping = new ConcurrentHashMap<>();

    //
    // 预分配的表
//    private Map<JID, HashSet<QtQueueKey>> predistributionMapping = new ConcurrentHashMap<>();

    public void closeSession(JID jid, JID seatName, long shopId) {

        try {
            QtSessionKey sessionKey = QtQueueDao.getInstance().closeSession(jid, shopId, seatName);

            QtSessionItem var = QtSessionItem.parseFromRedis(sessionKey);

            logger.debug("will remove session: {}", var);
            String supplierId = SpringComponents.components.hotlineSupplierService.selectHotlineBySupplierId(shopId);
            JID from = StringUtils.isNotEmpty(supplierId) ? JID.parseAsJID(supplierId) : Shop.parseLong(shopId, seatName.getDomain());
            ConsultUtils.sendMessage(from, seatName, jid, seatName, "会话已关闭", true, false, true);
            if (var == null)
                logger.debug("impossable this is no session in my cache!{}", JacksonUtil.obj2String(sessionKey));
            else {
                CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey());
                CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getReleasedKey());

                LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", seatName.toBareJID()));

                logger.debug("close session, seatName={}, list is {}", JacksonUtil.obj2String(seatName), JacksonUtil.obj2String(lists));

                if (CollectionUtils.isNotEmpty(lists)) {
                    lists.remove(sessionKey);
                    CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("fromtoMapping:%s", seatName.toBareJID()), JsonUtil.obj2String(lists), 1, TimeUnit.DAYS);
                    logger.debug("close session removed, seatName={}\nlist is {}\n map is {}\nsessionKey is{}\n",
                            JacksonUtil.obj2String(seatName), JacksonUtil.obj2String(lists), JacksonUtil.obj2String(fromtoMapping), JacksonUtil.obj2String(sessionKey));
                }
                redistribution(sessionKey.getShopId(), sessionKey.getProductId(), null,seatName.getDomain(), false, false);
            }
        } catch (Exception e) {
            logger.error("closeSession failed, {} {} {}", jid, seatName, shopId, e);
        }
    }

    public void judgmentForOne(String busiSupplierId, Integer businessId, String productId, JID qunarName) {
        BusiShopMapping shopMapping = SpringComponents.components.busiShopMapService.queryBusiShopMappingByBusiShopIDAndBusiID(businessId.intValue(), busiSupplierId);

        logger.debug("shopMapping is {}, shopId is {}", shopMapping, shopMapping.getShopID());

        long shopId = shopMapping.getShopID();

        HashSet<QtQueueKey> keys = QtQueueKey.parseFromRedisToHashSet(String.format("predistributionMapping:%s", qunarName.toBareJID()));

        logger.info("record a judgmentforone, {} {} {} {} keys{}", busiSupplierId, businessId, productId, qunarName, JacksonUtil.obj2String(keys));
        QtQueueKey key = new QtQueueKey(shopId, productId);
        if (keys == null) {
            keys = new HashSet<>();
        } else {
            if (keys.contains(key) && !"*".equals(productId)) {
                keys.remove(key);
            }
        }

        keys.add(key);
        CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("predistributionMapping:%s", qunarName.toBareJID()), JsonUtil.obj2String(keys), 1, TimeUnit.DAYS);
    }

    public void goOnline(long shopId) {
        // redistribution(shopId, "*", null);
        redistribution(shopId, "*", null, QChatConstant.DEFAULT_HOST,  true, false);
    }

    private static class Holder {
        private static final QtQueueManager INSTANCE = new QtQueueManager();
    }

    public static QtQueueManager getInstance() {
        return Holder.INSTANCE;
    }

    /***
     * 进入排队
     * @param key sessionKey
     * @return 返回QtQueueItem，制定当前排队位置
     */
//    private QtSessionItem customerRedistribution(QtSessionKey key) {
//        return customerRedistribution(key, true);
//    }

    private QtSessionItem customerRedistribution(QtSessionKey key, String host, boolean isTranAs) {
        if (key == null)
            return null;
        long shopId = key.getShopId();
        String productId = key.getProductId();
        JID userName = key.getUserName();
        return redistribution(shopId, productId, userName, host, true, isTranAs);
    }

    /***
     * 搞一下现在有多少个人正在被客服服务
     * @param qunarName 客服名(其实也可以支持seatsid)
     * @return
     */
    public LinkedList<QtSessionKey> workingQueueForSeats(JID qunarName) {

        LinkedList<QtSessionKey> c = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", qunarName.toBareJID()));
        logger.debug("c is {}, qunarname is {}, mapping is {}", c == null ? "" : JacksonUtil.obj2String(c), JacksonUtil.obj2String(qunarName), JacksonUtil.obj2String(fromtoMapping));
        return c;
    }

    public LinkedList<Map.Entry<String, Timestamp>> lastDistributeTime(List<JID> users, long shopId) {
        return QtQueueDao.getInstance().lastDistributeTime(users, shopId);
    }

    /***
     * 搞一下当前客服曾经服务过的Session列表(商铺和产品纬度)
     * @param qunarName 客服名(其实也可以支持seatsid)
     * @return
     */
//    public List<QtSessionKey> seatServiceHistory(String qunarName) {
//        return QtQueueDao.getInstance().getSeatsServiceHistory(qunarName);
//    }

//    public List<QtSessionItem> serviceSessionHistory(String userName, long shopId) {
//        return serviceSessionHistory(userName, shopId, QtSessionItem.DEFAULT_PRODUCTID);
//    }

//    private QtSessionItem redistribution(long shopId, String productId, JID userName) {
//        return redistribution(shopId, productId, userName, true);
//    }

    /***
     * 重新分配客服和客人的对应关系
     * @param shopId 商铺id
     * @param productId 商品id
     * @param userName 请求的客人的id
     * @param sendQueueInfo 是否发送排队信息，如果本次调用是客人说话引起的，那么会给客人提示当前队列信息，否则不提示
     * @return
     */
    private QtSessionItem redistribution(long shopId, String productId, JID userName,String host, boolean sendQueueInfo, boolean isTranAs) {

        logger.info("redistribution : shop:{}\n pid:{}\n username:{}\nququeMapping:{}\n", shopId, productId, userName, JacksonUtil.obj2String(queueMapping));


        QtQueueKey queueKey = new QtQueueKey(shopId, productId);

        QtQueueItem queueItem = QtQueueItem.parseFromRedis(new QtQueueKey(queueKey.getShopId()));

        logger.debug("redistributioning and queueItem is {}", queueItem == null ? null : queueItem.toJsonString());
        QtSessionKey sessionKey = null;
        if (userName != null) {
            //
            // 如果用户正在聊天中，直接返回
            sessionKey = new QtSessionKey(userName, shopId, productId);
            QtSessionItem configValue = QtSessionItem.parseFromRedis(sessionKey);
            if (configValue != null) {
                return configValue;
            }

            //
            // 如果用户当前未分配，新建队列
            if (queueItem == null) {
                queueItem = QtQueueDao.getInstance().getQueue(queueKey);
            }

            QueueUser queueUser;

            queueUser = queueItem.getSessionItem(userName);

            if (queueUser == null) {
                queueUser = QtQueueDao.getInstance().addNewQueue(new QtSessionKey(userName, shopId, productId));
                queueItem.addSessionItem(queueUser);
                logger.debug("queueItem is {}", queueItem.toJsonString());
            }

            QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);
        } else {
            if (queueItem == null) {
                queueItem = QtQueueDao.getInstance().getQueue(queueKey);

                QtQueueItem.putItemToRedis(new QtQueueKey(queueKey.getShopId()), queueItem);
            }
        }

        boolean next;
        QtSessionItem value = null;
        do {
            next = false;
            QueueUser currentUser = queueItem.popQueueUser();

            logger.debug("redistribution and getuser:{}\ncurrnetUser = {}\nmap is{}", JacksonUtil.obj2String(queueItem), JacksonUtil.obj2String(currentUser),
                    JacksonUtil.obj2String(queueMapping));

            if (currentUser != null) {
                //
                // TODO 这里实际上队列有更新，可以产生新的消息

                logger.debug("{} judgmentOrRedistributionEx on {} and sift failed.", currentUser.getJid(), shopId);

                CSR csr = null;
                String noServiceWelcomeContent = null;
                int serviceStatus = 0;
                try {
//                    QtSessionItem oldsession = releasedMapping.get(new QtQueueKey(queueKey.getShopId()));
                    JID oldSeat = null;
                    if (sessionKey != null) {
                        QtSessionItem oldsession = QtSessionItem.parseFromRedis(sessionKey.getReleasedKey());

                        if (oldsession != null)
                            oldSeat = oldsession.getSeatQunarName();
                    }
                    DistributedInfo distributedInfo =
                            SpringComponents.components.siftStrategyService.siftCsr(queueKey.getProductId(), Long.valueOf(queueKey.getShopId()), oldSeat, host, isTranAs);

                    serviceStatus = distributedInfo == null ? -1 : distributedInfo.getQueueStatus();

                    csr = distributedInfo == null ? null : distributedInfo.getCsr();

                    noServiceWelcomeContent = distributedInfo == null ? null : distributedInfo.getNoServiceWelcomes();

                } catch (NullPointerException e) {
                    logger.error("siftCsr failed.", e);
                }
                logger.info("shopId:{} jid:{} productId:{}  siftStrategyService done. {}",
                        queueKey.getShopId(), currentUser.getJid(), queueKey.getProductId(), JacksonUtil.obj2String(csr));
                if (csr != null) {
                    next = true;

                    try {
                        logger.info("redistribution succeeded at shopId:{} productId:{} userName:{} sendQueueInfo:{} csr:{}", shopId, productId, userName, sendQueueInfo, JacksonUtil.obj2String(csr));
                    } catch (Exception e) {
                        logger.error("redistribution succeeded and log failed.");
                    }

                    QtSessionItem localValue;

                    try {
                        //
                        // TODO:发欢迎语

                        JID shopIdJid = JID.parseAsJID(String.format("shop_%d@%s", queueKey.getShopId(), host));

                        logger.debug("begin to send welcome:{} {} {}", shopIdJid, currentUser.getJid(), csr.getQunarName());

                        ConsultUtils.sendWelcome(shopIdJid, currentUser.getJid(), csr.getQunarName());

                        QtSessionKey newKey = new QtSessionKey(currentUser.getJid(), queueKey.getShopId(), queueKey.getProductId());

                        localValue = new QtSessionItem(currentUser.getJid(), queueKey.getShopId(), queueKey.getProductId());
                        localValue.setSeatQunarName(csr.getQunarName());
                        localValue.setSessionId(currentUser.getSessionId());
                        localValue.setSeatId(csr.getId());
                        localValue.setLastAckTime(new Timestamp(System.currentTimeMillis()));
                        // 分配成功！更新一下客服in service时间
                        // 这里设置一下客服的信息，并且要更新数据库

//                        QtQueueDao.getInstance().updateInServiceSeatInfo(csr, newKey);
                        SpringComponents.components.queueMappingService.updateInServiceSeatInfo(csr, newKey);
                        final String csrName = csr.getQunarName().getNode();
//                        CompletableFuture.runAsync(() -> SpringComponents.components.dataStatisticsService.notifyBusinessLine(currentUser.getJid().getNode(), queueKey.getShopId(), queueKey.getProductId(), csrName));

//                        sessionMapping.put(newKey, localValue);
                        boolean succeeded = CacheHelper.setNX(CacheHelper.CacheType.SeatCache, newKey.getRedisKey(), localValue);

                        if (succeeded) {
                            if (userName != null && StringUtils.equalsIgnoreCase(userName.toBareJID(), localValue.getUserName().toBareJID())) {
                                value = localValue;
                            }

                            LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", csr.getQunarName().toBareJID()));
                            if (lists == null)
                                lists = new LinkedList<>();
                            lists.add(newKey);
                            CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("fromtoMapping:%s", csr.getQunarName().toBareJID()), JsonUtil.obj2String(lists), 1, TimeUnit.DAYS);

                            HashSet<QtQueueKey> sets = QtQueueKey.parseFromRedisToHashSet(String.format("predistributionMapping:%s", currentUser.getJid().toBareJID()));
                            if (sets != null) {
                                sets.remove(queueKey);
                                CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("predistributionMapping:%s", currentUser.getJid().toBareJID()), JsonUtil.obj2String(sets), 1, TimeUnit.DAYS);
                            }

                            //
                            // TODO:获取历史消息，扔到队列中，注意，这条方法一定要放在最后，不然就sb了
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
                    logger.info("siftStrategyService sift a null., {} {} {}", queueKey.getProductId(), Long.valueOf(queueKey.getShopId()), currentUser.getJid());


                    if (sendQueueInfo) {

                        logger.debug("queue Mapping is \n{}", JacksonUtil.obj2String(queueMapping));

                        int pos = queueItem.getSessionIndex(QueueUser.asUser(userName));
                        if (pos <= 0)
                            pos = 1;
                        else
                            pos += 1;

                        String content;

                        if (serviceStatus != 0) {
                            content = String.format("咨询人数过多需排队，当前排号%d位，您可以继续咨询，小驼看到后将第一时间答复", pos);
                        } else {
                            content = noServiceWelcomeContent;
                        }
                        String supplierId = SpringComponents.components.hotlineSupplierService.selectHotlineBySupplierId(shopId);
                        JID from = StringUtils.isNotEmpty(supplierId) ? JID.parseAsJID(supplierId) : Shop.parseLong(shopId, host);
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


//    /***
//     * 搞一下当前客人以前被哪个客服服务过
//     * @return
//     */
//    public List<QtSessionItem> serviceSessionHistory(String userName, long shopId, String productId) {
//        return QtQueueDao.getInstance().getServiceSessionHistory(userName, shopId, productId);
//
//    }

    public QtSessionItem customerDistribution(QtSessionKey key) {
        return customerDistribution(key, false);
    }

    public void saveMessageWithoutRealto(JID userId, long shopId, Map<String, Object> consultMessage) {

        try {
            QtUnSentMessage message = new QtUnSentMessage();
            message.setMessage(JsonUtil.toJSONString(consultMessage));
            message.setShopId(shopId);
            message.setUserName(userId);

            QtQueueDao.getInstance().saveNoneRealtoMessage(Arrays.asList(message));
        } catch (JsonProcessingException e) {
            logger.error("failed to ");
        }

    }

    private QtSessionItem customerDistributionFromDB(QtSessionKey key) {
        QtSessionItem value = QtQueueDao.getInstance().getConfig(key);

        if (value != null && value.isValid()) {
            CacheHelper.setNX(CacheHelper.CacheType.SeatCache, key.getRedisKey(), value);
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
    public QtSessionItem judgmentOrRedistribution(JID userName, long shopId, String productId,String host, boolean isEx, boolean isTranAs) {

        QtSessionKey key = new QtSessionKey(userName, shopId, productId);

        QtSessionItem result = customerDistribution(key);

        if (result == null || !result.isValid() || isEx || invalidCsr(shopId, host, result.getSeatQunarName())) {
            CacheHelper.remove(CacheHelper.CacheType.SeatCache, key.getRedisKey());
            result = customerRedistribution(key, host, isTranAs);
        }
        return result;
    }


    public QtSessionItem availableSessionWithUserAndStop(JID user, long shopId) {
        QtSessionKey sessionKey = new QtSessionKey(user, shopId);
//        QtSessionItem var = sessionMapping.get(sessionKey);
        QtSessionItem var = QtSessionItem.parseFromRedis(sessionKey);
        return var;
    }

//    public QtSessionItem judgmentOrRedistribution(JID userName, long shopId) {
//        logger.info("received judgmentOrRedistribution username:{}, shopId:{}", userName, shopId);
//        return judgmentOrRedistribution(userName, shopId, QtSessionItem.DEFAULT_PRODUCTID, false);
//    }

    public JID getRealTo(JID from, long shopId, String productId, String host, boolean noUpdate) {
        logger.info("record a real to :{} - {}", from, shopId);
        JID result = null;

//        HashSet<QtQueueKey> keys = QtQueueKey.parseFromRedisToHashSet(String.format("predistributionMapping:%s", from.toBareJID()));
//        String productId = "*";
//
//        if (keys != null) {
//
//            List<QtQueueKey> list = new ArrayList(keys);
//
//            ListIterator<QtQueueKey> iter = list.listIterator();
//
//            while (iter.hasNext()) {
//                QtQueueKey key = iter.next();
//
//                if (key.getShopId() == shopId) {
//                    productId = key.getProductId();
//                    logger.debug("catch a shopId, pid is {}", productId);
//                    break;
//                }
//
//            }
//        }

        QtSessionItem queueItem = judgmentOrRedistribution(from, shopId, productId, host, false, false);
        if (queueItem != null) {
            result = queueItem.getSeatQunarName();
            if (!noUpdate)
                // saveMsgLog(from, queueItem.getShopId(), System.currentTimeMillis(), true);
                SpringComponents.components.consultMessageService.saveMsgLog(from, shopId, System.currentTimeMillis(), true);
        }
        logger.info("get real to:{}", result);
        return result;
    }

    public void saveMsgLog(JID customer, long shopId, long time, boolean customerMsg) {
        // TODO 这里需要把 db 操作减少一下

        QtMessageLog log = new QtMessageLog();
        log.setUser(customer);
        log.setShopId(shopId);
        log.setTime(time);
        log.setIsCustomerMsg(customerMsg);
        logger.info("updateMsgLog:{}", JacksonUtil.obj2String(log));
        QtQueueDao.getInstance().updateMsgLog(log);

        SpringComponents.components.consultMessageService.saveMsgLog(customer, shopId, time, customerMsg);
    }

    private QtSessionItem customerDistribution(QtSessionKey sessionKey, boolean readFromDB) {
        QtSessionItem configValue = QtSessionItem.parseFromRedis(sessionKey);
        logger.info("customerDistribution QtSessionItem{}", JacksonUtil.obj2String(configValue));
        if (configValue == null && readFromDB)
            return customerDistributionFromDB(sessionKey);
        return configValue;
    }

    public void initialize() {
        QtSessionWatcher.getInstance().eventBus.register(this);
        logger.info("QtSessionManager is initialized!");
    }

    @Subscribe
    public void onMessage(SessionAction action) {
        // logger.info("received Session action:{} - {}", action.getAction(), GeneralToString.toString(action.getSessionIds()));

        if (action.getAction().equals(Action.Add)) {
            for (QtSessionItem item : action.getSessionIds()) {
                if (item.isValid()) {
                    QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId(), item.getProductId());
                    CacheHelper.setNX(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey(), item);
                }
            }
        } else if (action.getAction().equals(Action.RemoveAll)) {
            Set<String> keys = CacheHelper.keys(CacheHelper.CacheType.SeatCache, QtSessionKey.redisPatternKey());
            CacheHelper.removeKeys(CacheHelper.CacheType.SeatCache, keys);
        } else if (action.getAction().equals(Action.Remove)) {
            for (QtSessionItem item : action.getSessionIds()) {
                QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId(), item.getProductId());
                CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey());
            }
        } else {
            logger.info("i don't know what you want{}", action.getAction());
        }
    }

    @Subscribe
    public void onMessage(QueueAction action) {
        // logger.info("received queue action:{} - {}", action.getAction(), GeneralToString.toString(action.getQueueList()));

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


            Set<String> keys = CacheHelper.keys(CacheHelper.CacheType.SeatCache, QtQueueKey.redisPatternKey());
            CacheHelper.removeKeys(CacheHelper.CacheType.SeatCache, keys);

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
            Set<String> sets = CacheHelper.keys(CacheHelper.CacheType.SeatCache, "fromtoMapping:*");
            CacheHelper.removeKeys(CacheHelper.CacheType.SeatCache, sets);

        } else if (action.getAction().equals(Action.Remove)) {
            List<String> sessionList = new ArrayList<>();

            for (QtSessionItem item : action.getActionItems()) {

                try {
                    JID seatName = item.getSeatQunarName();

                    LinkedList<QtSessionKey> lists = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", seatName.toBareJID()));

                    if (lists != null) {
                        logger.info("ready to remove seatName:{} 's queue, queue size :{}", seatName, lists.size());

                        logger.debug("lists {} remove {}", JacksonUtil.obj2String(lists), JacksonUtil.obj2String(item));

                        lists.remove(new QtSessionKey(item.getUserName(), item.getShopId()));

                        logger.debug("lists remove item,result:{}", JacksonUtil.obj2String(lists));

                        CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("fromtoMapping:%s", seatName.toBareJID()), JsonUtil.obj2String(lists), 1, TimeUnit.DAYS);

                        logger.debug("fromtoMapping is {}", JacksonUtil.obj2String(fromtoMapping));
                    }

                    QtSessionKey sessionKey = new QtSessionKey(item.getUserName(), item.getShopId(), item.getProductId());

                    QtSessionItem var = QtSessionItem.parseFromRedis(sessionKey);
                    if (var != null) {
//                        QtQueueKey queueKey = new QtQueueKey(item.getShopId(), item.getProductId());
                        CacheHelper.set(CacheHelper.CacheType.SeatCache, sessionKey.getReleasedKey(), var, Config.Redis_Released_Session_Time, TimeUnit.MINUTES);
                        CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey());
                        sessionList.add(item.getSessionId());

                    }
                    redistribution(item.getShopId(), item.getProductId(), null, seatName.getDomain(), false, false);

                } catch (Exception e) {
                    logger.error("ON EVENT: SessionTimeoutAction failed.", e);
                }
            }

            if (CollectionUtils.isNotEmpty(sessionList)) {
                logger.info("remove {} sessions from db", sessionList.size());
                QtQueueDao.getInstance().releseSessionItems(sessionList);
            }
        }
    }

    @Subscribe
    public void onMessage(ClearAction action) {
        logger.info("received Clear All request");
        Set<String> keys = CacheHelper.keys(CacheHelper.CacheType.SeatCache, String.format("predistributionMapping:*"));
        if (CollectionUtils.isNotEmpty(keys))
            CacheHelper.removeKeys(CacheHelper.CacheType.SeatCache, keys);
        keys = CacheHelper.keys(CacheHelper.CacheType.SeatCache, QtQueueKey.releasedRedisKey());
        if (CollectionUtils.isNotEmpty(keys))
            CacheHelper.removeKeys(CacheHelper.CacheType.SeatCache, keys);
    }

    private boolean invalidCsr(long shopId, String host, JID csrJid) {
        // 校验 session 中的 客服是否已被删除
        String seatName = csrJid != null ? csrJid.getNode() : "";
        // 机器人的话正常服务
        if (seatName.endsWith("_robot")) {
            return false;
        }
        List<CSR> csrList = SpringComponents.components.csrService.queryCsrByQunarNameAndShopId(seatName, shopId, host);
        return CollectionUtils.isEmpty(csrList);
    }






}
