package com.qunar.qtalk.ss.sift.service;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.consult.ConsultUtils;
import com.qunar.qtalk.ss.consult.QtQueueDao;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.consult.entity.QtSessionItem;
import com.qunar.qtalk.ss.consult.entity.QtSessionKey;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.RobotShopRelation;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.enums.csr.BindWxStatus;
import com.qunar.qtalk.ss.sift.enums.csr.CsrServiceStatus;
import com.qunar.qtalk.ss.sift.enums.shop.QueueOpenStatus;
import com.qunar.qtalk.ss.sift.enums.shop.RobotStrategy;
import com.qunar.qtalk.ss.sift.enums.shop.ShopStatus;
import com.qunar.qtalk.ss.sift.model.DistributedInfo;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.common.CacheHelper;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
import com.qunar.qtalk.ss.utils.common.UserHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SiftStrategyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiftStrategyService.class);

    @Resource
    private ShopService shopService;

    @Resource(name = "ssRobotService")
    private RobotService robotService;

    @Resource
    private GroupService groupService;

    @Resource
    private CsrService csrService;

    @Resource
    private StrageryService strageryService;


    /***
     * 客服分配的筛选策略
     * @param productID 用户咨询的产品ID
     * @param shopID 用户咨询的商铺ID
     * @param csrQunarName 上次该店铺服务的客服名称
     * @param isTranAs 是否为转人工客服，true为是，false为否
     * @return isSpillover 是否可以溢出（业务含义为:
     *  true: 只要这个客服在线，就分配这个客服
     *  false: 这个客服在线且不忙的时候，分配这个客服）
     */
    public DistributedInfo siftCsr(String productID, Long shopID, JID csrQunarName, String host, boolean isTranAs, boolean isSpillover) {
        LOGGER.info("请求siftCsr, 输入参数为：{}，{}，{}，{}，{}",
                productID, shopID, JacksonUtil.obj2String(csrQunarName), isTranAs, isSpillover);
        DistributedInfo distributedInfo = new DistributedInfo();
        CSR csr = null;
        long startTime = System.currentTimeMillis();
        try {
            // 获取商铺信息
            Shop shop = getShopInfo(shopID);
            if (shop == null || shop.getStatus() == ShopStatus.offline.code) {
                LOGGER.warn("该店铺不存在或者已下线-{}，{}", shopID, JacksonUtil.obj2String(shop));
                return null;
            } else {
                distributedInfo.setQueueStatus(shop.getOpenQueueStatus());
                distributedInfo.setNoServiceWelcomes(shop.getNoServiceWelcomes());
            }

            // 筛选机器人配置（如果是转人工客服，则忽略这一步）
            RobotShopRelation robotShopRelation = null;
            if (!isTranAs) {
                robotShopRelation = robotService.siftRobot(shop);
                if (robotShopRelation != null
                        && robotShopRelation.getStrategy() == RobotStrategy.RSE_ROBOT_ADVANCED.code) {
                    LOGGER.info("使用机器人客服");
                    csr = robotService.buildCSRByRobot(robotShopRelation, host);
                    return distributedInfo;
                }
            }

            // 筛选客服列表
            List<CSR> csrList = findAvailableCSRs(productID, shop, host);

            if (!isSpillover) {
                // 判定是否开启排队，过滤掉忙的客服
                filterFreeCsrs(csrList, shop, productID);
            }

            //如果seatQunarName不为空，先判断这个坐席是否可用
            csr = containTheCsr(csrList, csrQunarName, shop);
            if (csr != null) {
                return distributedInfo;
            }

            // 筛选客服
            csr = sift(csrList, shop, robotShopRelation, host);
        } catch (Exception e) {
            LOGGER.error("商铺-{}，产品-{}，筛选客服异常。{}", shopID, productID, e);
        } finally {
            long time = System.currentTimeMillis() - startTime;
            LOGGER.info("商铺-{}，产品-{}，筛选结果：{}，筛选客服结束。用时：{}",
                    shopID, productID, JacksonUtil.obj2String(csr), time);
            if (csr == null) {
             LOGGER.warn("csr not exist");
            };
            distributedInfo.setCsr(csr);
        }
        return distributedInfo;
    }

    public List<CSR> getOnlineCsrs(String productID, Long shopID, String host, boolean isTranAs, boolean isSpillover) {

        List<CSR> csrList = Lists.newArrayList();
        // 获取商铺信息
        Shop shop = getShopInfo(shopID);
        if (shop == null || shop.getStatus() == ShopStatus.offline.code) {
            LOGGER.warn("该店铺不存在或者已下线-{}，{}", shopID, JacksonUtil.obj2String(shop));
            return null;
        }
        // 筛选机器人配置（如果是转人工客服，则忽略这一步）
        RobotShopRelation robotShopRelation = null;
        if (!isTranAs) {
            robotShopRelation = robotService.siftRobot(shop);
            if (robotShopRelation != null
                    && robotShopRelation.getStrategy() == RobotStrategy.RSE_ROBOT_ADVANCED.code) {
                LOGGER.info("使用机器人客服");
                CSR csr = robotService.buildCSRByRobot(robotShopRelation, host);
                csrList.add(csr);
                return csrList;
            }
        }
        // 筛选客服列表
        csrList = findAvailableCSRs(productID, shop, host);

        if (!isSpillover) {
            // 判定是否开启排队，过滤掉忙的客服
            filterFreeCsrs(csrList, shop, productID);
        }
        return csrList;
    }
    /***
     * 重载上一个函数
     */
    public DistributedInfo siftCsr(String productID, Long shopID, JID csrQunarName, String host,  boolean isTranAs) {
        LOGGER.info("请求siftCsr, 输入参数为：{}，{}，{}，{}",
                productID, shopID, JacksonUtil.obj2String(csrQunarName), isTranAs);
        return siftCsr(productID, shopID, csrQunarName, host, isTranAs, false);
    }

    /***
     * 重载上一个函数
     */
//    public DistributedInfo siftCsr(Long shopID, JID csrQunarName, boolean isTranAs) {
//        return siftCsr(null, shopID, csrQunarName, isTranAs);
//    }


    /***
     * 获取店铺信息
     * @param shopID
     * @return
     */
    private Shop getShopInfo(Long shopID) {
        LOGGER.debug("开始获取店铺信息，shop id-{}", shopID);
        Shop shop = shopService.selectShopById(shopID);
        LOGGER.info("获取到店铺信息， shop id -{}, {}", shopID, JacksonUtil.obj2String(shop));
        return shop;
    }

    /***
     * 判定该商铺是否已经分配了客服正在服务这个客人
     * @param csrQunarName
     * @param shopID
     * @return
     */
//    private CSR findAvailableSession(JID csrQunarName, Long shopID) {
//        LOGGER.info("{}，{}-开始查找该客人是否已经在该店铺分配了客服，服务之。",
//                JacksonUtil.obj2String(csrQunarName), shopID);
//        CSR csr = null;
//        QtSessionItem item = QtQueueManager.getInstance().availableSessionWithUserAndStop(csrQunarName, shopID);
//        LOGGER.info("根据-{}-{}-获取到的结果为：{}",
//                JacksonUtil.obj2String(csrQunarName), shopID, JacksonUtil.obj2String(item));
//        if (item != null) {
//            List<Long> seatIds = Lists.newArrayList();
//            seatIds.add(item.getSeatId());
//            List<CSR> csrs = csrService.queryCsrsByCsrIDs(seatIds);
//            if (CollectionUtils.isNotEmpty(csrs)) {
//                csr = csrs.get(0);
//            }
//        }
//        return csr;
//    }

    /***
     * 找到可以为该产品服务的客服列表
     * @param productID
     * @param shop
     * @return
     */
    private List<CSR> findAvailableCSRs(String productID, Shop shop, String host) {
        List<CSR> csrList = null;
        List<Long> groupIDs = groupService.querySeatGroupIdByShopIdAndProductId(shop.getId(), productID);
        if (CollectionUtils.isNotEmpty(groupIDs)) {
            // 产品分组逻辑
            csrList = csrService.queryCsrsByGroupIDs(groupIDs, host);
            csrList = filterOnlineCSRs(csrList, shop.getId(), productID);
            return csrList;
        } else {
            // 店铺默认分组逻辑
            List<Long> defaultSeatGroup = groupService.queryDefaultSeatGroupIdByShopId(shop.getId());
            if (CollectionUtils.isNotEmpty(defaultSeatGroup)) {
                csrList = csrService.queryCsrsByGroupIDs(defaultSeatGroup, host);
                // 过滤出在线客服
                csrList = filterOnlineCSRs(csrList, shop.getId(), productID);
            }
        }
        LOGGER.info("通过客服组:{} 过滤后，客服列表为:{}", JacksonUtil.obj2String(groupIDs), JacksonUtil.obj2String(csrList));

//        /** 如果能通过默认分组，找到客服ID，则直接分配，如果不可以，则筛选出全部客服 **/
        if (CollectionUtils.isEmpty(csrList)) {
            csrList = csrService.queryOnlineCsrsByShopID(shop.getId(), host);
            csrList = filterOnlineCSRs(csrList, shop.getId(), productID);
            LOGGER.info("通过店铺id:{} 过滤后，客服列表为:{}", shop.getId(), JacksonUtil.obj2String(csrList));
        }

        return csrList;

    }

    /***
     * 过滤出在线的客服
     * @param csrList
     * @return
     */
    public List<CSR> filterOnlineCSRs(List<CSR> csrList, Long shopID, String productID) {
        LOGGER.debug("开始过滤出在线的客服，商铺-{}, 产品-{}", shopID, productID);
        if (CollectionUtil.isEmpty(csrList)) {
            LOGGER.error("商铺-{}，找不到可以服务于该产品-{}的客服", shopID, productID);
            return csrList;
        }

        /******************** 需要添加 调取接口 0需要换成函数 ***************/

        List<CSR> assiginableCsrs = Lists.newArrayList();
        for (CSR csr : csrList) {
            LOGGER.debug("当前的csr为：{}", JacksonUtil.obj2String(csr));
            int statusCode = csr.getServiceStatus();
            //如果开启了勿扰模式，直接过滤
            if (statusCode == CsrServiceStatus.DND_MODE.code) {
                LOGGER.info("该csr-{}设置了勿扰模式，过滤", JacksonUtil.obj2String(csr));
                continue;
            }

            //如果开启了超人模式，则不过滤
            if (statusCode == CsrServiceStatus.SUPER_MODE.code || csr.getBindWx() == BindWxStatus.BIND_WX.code) {
                LOGGER.debug("{}-该csr设置超人模式", JacksonUtil.obj2String(csr));
                assiginableCsrs.add(csr);
                continue;
            }

            //如果开启了正常模式，并且在工作日内，10：00-19：00，且在线，则不过滤
//            Date currentDate = new Date();
//            String hourString = DateUtil.date2str(currentDate, "HH");
//            int hour = Integer.parseInt(org.apache.commons.lang.StringUtils.isBlank(hourString) ? "0" : hourString);
//            boolean isWorkDay = DateUtil.isWorkDay(currentDate);
//            LOGGER.info("{}-当前是否为工作日：{}， 现在的小时数为：{}",
//                    JacksonUtil.obj2String(csr.getQunarName()), isWorkDay, hourString);
            if (statusCode == CsrServiceStatus.STANDARD_MODE.code) {
                LOGGER.debug("{}-该csr设置了标准模式", JacksonUtil.obj2String(csr));
                if (/*isWorkDay && (hour > 9) && (hour < 19) && */judgeOnline(csr.getQunarName())) {
                    assiginableCsrs.add(csr);
                    continue;
                }
            }

        }
        LOGGER.info("商铺-{}, 产品-{}, 过滤在先后的结果为：{}",
                shopID, productID, JacksonUtil.obj2String(assiginableCsrs));
        return assiginableCsrs;
    }

    /***
     * 过滤出闲的客服
     * @param csrList
     * @param shop
     * @param productID
     * @return
     */
    private void filterFreeCsrs(List<CSR> csrList, Shop shop, String productID) {

        LOGGER.info("开始过滤出排队中已达到最大值的客服，商铺-{}, 产品-{}", shop.getId(), productID);
        if (CollectionUtil.isEmpty(csrList)) {
            LOGGER.error("商铺-{}，找不到可以服务于该产品-{}的客服", shop.getId(), productID);
            return;
        }
        if (shop.getOpenQueueStatus() == QueueOpenStatus.close.code) {
            LOGGER.info("当前商铺：{}，未开启排队。", JacksonUtil.obj2String(shop));
            return;
        }

        csrList.removeIf(csr -> {
            LOGGER.debug("即将查询该客服的csr-{}的session数量", JacksonUtil.obj2String(csr));
            LinkedList<QtSessionKey> sessionKeys = QtQueueManager.getInstance().workingQueueForSeats(csr.getQunarName());
            LOGGER.info("通过该客服-{}，获取到的session数量为：{} size大小：{}",
                    JacksonUtil.obj2String(csr), JacksonUtil.obj2String(sessionKeys), CollectionUtils.isEmpty(sessionKeys) ? 0 : sessionKeys.size());
            return CollectionUtils.isNotEmpty(sessionKeys) && sessionKeys.size() >= csr.getMaxServiceCount();
        });
    }

    /***
     * 判定优先客服是否可服务，如果在客服务列表中，返回该客服
     * @param csrList
     * @param csrQunarName
     * @return
     */
    private CSR containTheCsr(List<CSR> csrList, JID csrQunarName, Shop shop) {

        LOGGER.info("商铺-{}, 判断该列表中-{}是否包含此客服-{}.",
                shop.getId(), JacksonUtil.obj2String(csrList), JacksonUtil.obj2String(csrQunarName));
        if (csrQunarName == null) {
            LOGGER.info("此次咨询没有传递优先客服，直接return.");
            return null;
        }
        if (CollectionUtil.isEmpty(csrList)) {
            LOGGER.error("商铺-{}的可用客服列表中，不包含优先客服", shop.getId());
            return null;
        }
        for (CSR csr : csrList) {
//            LOGGER.info("csr is {}, csrQunarName is {}", GeneralToString.toString(csr), csrQunarName);
            if (csr.getQunarName().toBareJID().contains(csrQunarName.toBareJID())) {
                return csr;
            }
        }
        return null;
    }

    /***
     * 根据分配策略，筛选一个适当的客服
     * @param csrList
     * @param shop
     * @return
     */
    private CSR sift(List<CSR> csrList, Shop shop, RobotShopRelation robotShopRelation, String host) {
        LOGGER.info("开始根据商铺-{}的分配策略，机器人配置-{}，可筛选的客服列表-{}筛选终极客服",
                JacksonUtil.obj2String(shop),
                JacksonUtil.obj2String(robotShopRelation),
                JacksonUtil.obj2String(csrList));
        if (CollectionUtils.isEmpty(csrList)
                && (robotShopRelation != null
                && robotShopRelation.getStrategy() == RobotStrategy.RSE_SEAT_ADVANCED.code)) {
            LOGGER.info("{}-客服全部下线，开始分配机器人", JacksonUtil.obj2String(shop));
            return robotService.buildCSRByRobot(robotShopRelation, host);
        }

        if (CollectionUtil.isEmpty(csrList)) {
            return null;
        }
        return strageryService.siftByStragery(csrList, shop);
    }

    public boolean judgeOnline(JID csr) {
        Map<String, Boolean> mapper = UserHelper.getAllPlatOnlineStatus(csr);
        LOGGER.info("获取userName-{}的在线结果-{}",
                JacksonUtil.obj2String(csr), JacksonUtil.obj2String(mapper));
        if (MapUtils.isNotEmpty(mapper)) {
            for (Map.Entry<String, Boolean> item : mapper.entrySet()) {
                if (item.getValue() && (item.getKey().contains("iOS") || item.getKey().contains("PC")
                        || item.getKey().contains("Mac") || item.getKey().contains("Andro")))
                    return true;
            }

        }
        return false;
    }

    public JsonData transformCsr(String shopJid, String oldCsrName, String newCsrName, String customName, String reason, String domain) {

        if (StringUtils.isNumeric(shopJid)) {
            shopJid = "shop_" + shopJid;
        }
        if (!shopJid.contains("@")) {
            shopJid = String.format("%s@%s", shopJid, domain);
        }
        JID shop = JID.parseAsJID(shopJid);
        Long shopId = shopService.selectShopByBsiId(shopJid);
        if (shopId == null || shopId < 1)
            return JsonData.error("参数错误", 500);
        List<CSR> oldCsrList = csrService.queryCsrByQunarNameAndShopId(oldCsrName, shopId, domain);
        List<CSR> newCsrList = csrService.queryCsrByQunarNameAndShopId(newCsrName, shopId, domain);
        if (CollectionUtils.isNotEmpty(oldCsrList) && CollectionUtils.isNotEmpty(newCsrList)) {
            String realCustomName = customName.contains("@") ? customName : String.format("%s@%s", customName, domain);
            CSR oldCsr = oldCsrList.get(0);
            CSR newCsr = newCsrList.get(0);
            // 更新数据库排队列表
            QtQueueDao.getInstance().transformCsrUpdateSeat(shopId, newCsr.getId(), oldCsr.getQunarName().toBareJID(), newCsr.getQunarName().toBareJID(), realCustomName);

            QtSessionKey sessionKey = null;
            try {
                // 更新redis缓存
                sessionKey = new QtSessionKey(JID.parseAsJID(realCustomName), newCsr.getSupplierID());
                updateRedisCache(oldCsr, newCsr, sessionKey);
                msgNotify(shop, oldCsr, newCsr, realCustomName, oldCsr.getWebName(), newCsr.getWebName(), reason);
                CacheHelper.set(CacheHelper.CacheType.SeatCache, realCustomName + "_" + shopJid, newCsr.getQunarName().toBareJID(), 1, TimeUnit.DAYS);
                LOGGER.info("transformCsr success customer:{} from:{} to:{}", customName, oldCsrName, newCsrName);
                return JsonData.success("会话转移成功");
            } catch (Exception e) {
                // 更新缓存失败 客服数据回滚
                QtQueueDao.getInstance().transformCsrUpdateSeat(shopId, oldCsr.getId(), newCsr.getQunarName().toBareJID(), oldCsr.getQunarName().toBareJID(), realCustomName);
                if (sessionKey != null) {
                    CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey());
                    CacheHelper.remove(CacheHelper.CacheType.SeatCache, sessionKey.getReleasedKey());
                }
                LOGGER.error("updateRedisCache customName:{} oldCsr:{} newCsr:{} error", customName, JacksonUtil.obj2String(oldCsr), JacksonUtil.obj2String(newCsr), e);

                return JsonData.error("会话转移失败，请稍候再试", 500);
            }
        }
        return JsonData.error("会话转移失败，客服错误", 500);
    }

    private void updateRedisCache(CSR oldCsr, CSR newCsr, QtSessionKey sessionKey) {
        LOGGER.info("会话转移开始更新 redis oldCsr:{} newCsr:{} sessionKey:{}", JacksonUtil.obj2String(oldCsr), JacksonUtil.obj2String(newCsr), JacksonUtil.obj2String(sessionKey));
        LinkedList<QtSessionKey> oldCsrFromtolist = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", oldCsr.getQunarName().toBareJID()));

        if (CollectionUtils.isNotEmpty(oldCsrFromtolist)) {
            oldCsrFromtolist.remove(sessionKey);
            CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("fromtoMapping:%s", oldCsr.getQunarName().toBareJID()), JsonUtil.obj2String(oldCsrFromtolist), 1, TimeUnit.DAYS);
        }

        LinkedList<QtSessionKey> newCsrFromtoList = QtSessionKey.parseFromRedisToLinkedList(String.format("fromtoMapping:%s", newCsr.getQunarName().toBareJID()));
        if (newCsrFromtoList == null)
            newCsrFromtoList = new LinkedList<>();
        newCsrFromtoList.add(sessionKey);
        CacheHelper.set(CacheHelper.CacheType.SeatCache, String.format("fromtoMapping:%s", newCsr.getQunarName().toBareJID()), JsonUtil.obj2String(newCsrFromtoList), 1, TimeUnit.DAYS);

        QtSessionItem sessionItem = QtSessionItem.parseFromRedis(sessionKey);
        if (sessionItem == null) {
            return;
        }
        LOGGER.info("updateRedisCache sessionKey:{} sessionItem:{}", JacksonUtil.obj2String(sessionKey), JacksonUtil.obj2String(sessionItem));
        sessionItem.setSeatId(newCsr.getId());
        sessionItem.setSeatQunarName(newCsr.getQunarName());
        CacheHelper.set(CacheHelper.CacheType.SeatCache, sessionKey.getRedisKey(), sessionItem, 1, TimeUnit.DAYS);
        CacheHelper.set(CacheHelper.CacheType.SeatCache, sessionKey.getReleasedKey(), sessionItem, 1, TimeUnit.DAYS);
    }

    private void msgNotify(JID shop, CSR oldCsr, CSR newCsr, String realCustomName,String oldCsrName, String newCsrName, String reason) {
        String toCustomerMsg = "客服-%s 已将您的咨询转移给 客服-%s，接下来将由 客服-%s 继续为您服务哦~";
        toCustomerMsg = String.format(toCustomerMsg, oldCsrName, newCsrName, newCsrName);
        // 他通知客人
        ConsultUtils.sendMessage(shop, JID.parseAsJID(realCustomName), newCsr.getQunarName(), JID.parseAsJID(realCustomName), toCustomerMsg, false, false, true);

        String toOldCsrMsg = "您的咨询已转移给 客服-%s，请勿回复。";
        toOldCsrMsg = String.format(toOldCsrMsg, newCsrName);
        // 通知当前客服
        ConsultUtils.sendMessage(shop, oldCsr.getQunarName(), JID.parseAsJID(realCustomName), oldCsr.getQunarName(), toOldCsrMsg, true, true, true);

        String url = Config.QCHAT_HISTORY_MSG_SEARCH_URL;
        url = String.format(url, shop.getNode(), JID.parseAsJID(realCustomName).getNode(), oldCsr.getQunarName().getNode(), System.currentTimeMillis());
        String toNewCsrMsg = "由于‘%s’的原因，您收到来自 客服-%s 的会话转移，<a href='%s'>查看历史消息</a>。";
        toNewCsrMsg = String.format(toNewCsrMsg, reason, oldCsrName, url);
        // 通知转移客服
        ConsultUtils.sendMessage(shop, newCsr.getQunarName(), JID.parseAsJID(realCustomName), newCsr.getQunarName(), toNewCsrMsg, true, false, true);

    }
}
