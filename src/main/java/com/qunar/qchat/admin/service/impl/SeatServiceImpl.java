package com.qunar.qchat.admin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.common.ApplicationContextHelper;
import com.qunar.qchat.admin.constants.*;
import com.qunar.qchat.admin.controller.seatselect.ISeatSelectorEvents;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.controller.seatselect.impl.RobotSelectorNoticeEvents;
import com.qunar.qchat.admin.controller.seatselect.impl.SeatDefaultSelector;
import com.qunar.qchat.admin.controller.seatselect.impl.SeatNoRobotSelector;
import com.qunar.qchat.admin.controller.seatselect.impl.SeatSelectorBaseNoticeEvents;
import com.qunar.qchat.admin.dao.*;
import com.qunar.qchat.admin.dao.msg.IMsgDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.plugins.chatplugin.BaseChatPlugin;
import com.qunar.qchat.admin.plugins.chatplugin.ChatPluginInstance;
import com.qunar.qchat.admin.plugins.chatplugin.IChatPlugin;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISeatSortFactory;
import com.qunar.qchat.admin.service.sortstrategy.PollingASeatStrategy;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.utils.common.CacheHelper;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.consult.ConsultUtils;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.consult.QueueMappingDao;
import com.qunar.qtalk.ss.consult.SpringComponents;
import com.qunar.qtalk.ss.consult.entity.QtSessionItem;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.QueueMapping;
import com.qunar.qtalk.ss.sift.enums.csr.BindWxStatus;
import com.qunar.qtalk.ss.sift.model.DistributedInfo;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qtalk.ss.sift.service.SiftStrategyService;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import com.qunar.qtalk.ss.utils.SendMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
@Service("seatService")
public class SeatServiceImpl implements ISeatService {
    private static final Logger logger = LoggerFactory.getLogger(SeatServiceImpl.class);

//    private final Logger assignSeatAppender = LoggerFactory.getLogger("assignSeatAppender");
    @Resource(name = "seatDao")
    private ISeatDao seatDao;
    @Resource(name = "seatGroupDao")
    private ISeatGroupDao seatGroupDao;
    @Resource(name = "onlineSeatSortFactory")
    private ISeatSortFactory seatSortFactory;

//    @Resource(name = "sessionV2Service")
//    private ISessionV2Service sessionV2Service;

    @Autowired
    ISeatGroupMappingDao seatGroupMappingDao;
    @Autowired
    IBusiSupplierMappingDao busiSupplierMappingDao;

    @Autowired
    IBusiSeatMappingDao busiSeatMappingDao;

    @Resource
    private IRobotService robotService;
    @Resource
    private PollingASeatStrategy pollingASeatStrategy;

    @Resource(name = "redisAgent")
    private RedisAgentUtil redisAgent;

    @Resource
    private ISessionDao sessionDao;

    @Autowired
    private ISupplierDao supplierDao;
    @Autowired
    public SiftStrategyService siftStrategyService;
    @Resource
    private IMsgDao msgDao;

    @Autowired
    ShopService shopService;
    @Autowired
    QueueMappingDao queueMappingDao;

//    private static final String DOMAIN1_NAME = "ejabhost1";
//    private static final String HOTEL_SETTLEMENT_SUPPLIERID = "0";
//    private static final long DEFAULT_FOR_NULL_TIME = new Date(0).getTime();

//    private static final Function<SeatAndGroup, String> getQunarName = new Function<SeatAndGroup, String>() {
//        @Override
//        public String apply(SeatAndGroup seatAndGroup) {
//            return seatAndGroup.getQunarName();
//        }
//    };

    private static final Function<SeatWithStateVO, String> getSeatName = new Function<SeatWithStateVO, String>() {
        @Override
        public String apply(SeatWithStateVO seatWithStateVO) {
            if (seatWithStateVO == null || seatWithStateVO.getSeat() == null)
                return null;
            return seatWithStateVO.getSeat().getQunarName();
        }
    };

    private static final Predicate<String> isShop = new Predicate<String>() {
        @Override
        public boolean apply(String s) {
            if (StringUtils.isEmpty(s))
                return false;
            return s.startsWith(Supplier.SHOPID_PREFIX);
        }
    };

    private static final Function<String, Long> getShopId = new Function<String, Long>() {
        @Override
        public Long apply(String s) {
            try {
                return Long.valueOf(s.replace(Supplier.SHOPID_PREFIX, ""));
            } catch (Exception e) {
                logger.warn("输入的店铺id出错{}", s);
            }
            return 0l;
        }
    };


    public void clearRedisCache(String key) {
        redisAgent.del(key);
    }


    @Override
    public Map<String, List<GroupAndSeatVO>> getBatchBusiSupplierSeatsWithOnlineStatus(
            List<String> busiSupplierIds, BusinessEnum businessEnum, String pid) {

        Map<String, List<GroupAndSeatVO>> seatAndGroupMap = Maps.newHashMap();

        for (String busiSupplierId : busiSupplierIds) {

            List<SeatAndGroup> sag = getSeatAndGroup(busiSupplierId, businessEnum);
            if (CollectionUtil.isEmpty(sag)) {
                continue;
            }


            List<GroupAndSeatVO> groupAndSeatVOList = assembleAndFilterSeat(sag, pid);
            seatAndGroupMap.put(busiSupplierId, groupAndSeatVOList);

        }


        return seatAndGroupMap;
    }

    @Override
    public List<GroupAndSeatVO> getSeatWithOnlineStateList(String busiSupplierId, BusinessEnum businessEnum, String pid) {

        List<SeatAndGroup> seatAndGroups = getSeatAndGroup(busiSupplierId, businessEnum);
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            return null;
        }


        List<GroupAndSeatVO> groupAndSeatVOList = assembleAndFilterSeat(seatAndGroups, pid);
        return groupAndSeatVOList;
    }

    @Override
    public SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineState(String busiSupplierId, BusinessEnum businessEnum,
                                                                       String qunarName, String pid) {
        return getSingleSeatWithOnlineState(busiSupplierId, businessEnum, qunarName, pid, null, true);
    }

    private SeatsResultVO<SeatWithStateVO> getOneSeatWithOnlineState(
            String busiSupplierId,
            BusinessEnum businessEnum,
            String qunarName,
            String pid,
            String groupType,
            boolean forRobot
    ) {
        List<SeatAndGroup> seatAndGroups;
        if (Strings.isNullOrEmpty(groupType)) {
            logger.info("getOneSeatWithOnlineState ! groupType isNullOrEmpty");
            seatAndGroups = seatDao.getAllSeatsWithGroup(busiSupplierId, businessEnum.getId());
        } else {
            logger.info("getOneSeatWithOnlineState  groupType is not empty or NULL");
            seatAndGroups = getSeatAndGroupWithGroupType(busiSupplierId, businessEnum, groupType);
        }
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            logger.info("getOneSeatWithOnlineState ! collectionUtil is empty");
            return null;
        }

        // 设置一个全局策略  TODO  应该配置化
        for (SeatAndGroup sg : seatAndGroups) {
            sg.setStrategy(SeatSortStrategyEnum.POLLING_STRATEGY.getStrategyId());
        }

        return getSingleSeatWithOnlineStateCommon(seatAndGroups, busiSupplierId, businessEnum, qunarName, pid, forRobot);
    }

//    @Override
//    public SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineStateNotRobot(String busiSupplierId, BusinessEnum businessEnum, String qunarName, String pid) {
//        return getOneSeatWithOnlineState(busiSupplierId, businessEnum, qunarName, pid, null, false);
//    }


    // 只影响 one.qunar 这个接口
//    @Override
//    public SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineState(
//            String busiSupplierId,
//            BusinessEnum businessEnum,
//            String qunarName,
//            String pid,
//            String groupType,
//            CustomArgs customArgs) {
//        return getOneSeatWithOnlineState(busiSupplierId, businessEnum, qunarName, pid, groupType, true);
//    }

    @Override
    public SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineState(String busiSupplierId, BusinessEnum businessEnum,
                                                                       String qunarName, String pid, String groupType, boolean forRobot) {
        List<SeatAndGroup> seatAndGroups;
        if (Strings.isNullOrEmpty(groupType)) {
            logger.info("getSingleSeatWithOnlineState中 ! groupType isNullOrEmpty");
            seatAndGroups = getSeatAndGroup(busiSupplierId, businessEnum);
        } else {
            logger.info("getSingleSeatWithOnlineState中  groupType is not empty or NULL");
            seatAndGroups = getSeatAndGroupWithGroupType(busiSupplierId, businessEnum, groupType);
        }
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            logger.info("getSingleSeatWithOnlineState中 ! collectionUtil is empty");
            return null;
        }

        // 设置一个全局策略  TODO  应该配置化
        for (SeatAndGroup sg : seatAndGroups) {
            sg.setStrategy(SeatSortStrategyEnum.POLLING_STRATEGY.getStrategyId());
        }

        return getSingleSeatWithOnlineStateCommon(seatAndGroups, busiSupplierId, businessEnum, qunarName, pid, forRobot);
    }

    private String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString();
    }

    /**
     * 将getSeatAndGroup之后的操作抽出来，支持qtalk
     */
    private SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineStateCommon(
            List<SeatAndGroup> seatAndGroups,
            String busiSupplierId,
            BusinessEnum businessEnum,
            String qunarName,
            String pid,
            boolean forRobot) {

        String conversationHandleKey = RedisConstants.KEY_CONVERSATIONHANDLE + busiSupplierId + businessEnum.getEnName() + qunarName;

        logger.info(" getSingleSeatWithOnlineStateCommon函数中 qunarName：{}, pid:{}, busiSupplierId:{}", qunarName, pid, busiSupplierId);
        List<GroupAndSeatVO> groupAndSeatVOList = assembleAndFilterSeat(seatAndGroups, pid);
        // 过滤组之后 所有客服按照在线状态&上次会话时间排序
        List<SeatWithStateVO> ssList = Lists.newArrayList();
        for (GroupAndSeatVO groupAndSeatVO : groupAndSeatVOList) {
            ssList.addAll(groupAndSeatVO.getSeatWithStateVOList());
        }
        ///
        List<String> onlinelist = Lists.newArrayList();
        List<String> offlinelist = Lists.newArrayList();
        List<String> awaylist = Lists.newArrayList();
        List<String> buylist = Lists.newArrayList();

        for (SeatWithStateVO seate : ssList) {

            if (seate != null && seate.getSeat() != null && seate.getOnlineState() != null && seate.getSeat().getQunarName() != null) {
                String id = seate.getSeat().getQunarName();
                OnlineState st = seate.getOnlineState();
                if (st == OnlineState.ONLINE) {
                    onlinelist.add(id);
                }
                if (st == OnlineState.OFFLINE) {
                    offlinelist.add(id);
                }
                if (st == OnlineState.AWAY) {
                    awaylist.add(id);
                }
                if (st == OnlineState.BUSY) {
                    buylist.add(id);
                }
            } else {
                logger.info(" getSingleSeatWithOnlineStateCommon函数中 SeatWithStateVO seate is null qunarName：{}, pid:{}", qunarName, pid);
            }
        }
        logger.info(" getSingleSeatWithOnlineStateCommon函数中  qunarName：{}, pid:{}, onlineList：{},offlineList：{},awaylist：{},buylist：{}"
                , qunarName, pid, listToString(onlinelist, ','), listToString(offlinelist, ','), listToString(awaylist, ','), listToString(buylist, ','));

        if (CollectionUtil.isEmpty(groupAndSeatVOList) || CollectionUtil.isEmpty(ssList)) {
            logger.info(" getSingleSeatWithOnlineStateCommon函数中 CollectionUtil isEmpty qunarName：{}, pid:{}", qunarName, pid);
            return null;
        }
        // 重新排序 one.qunar使用，按轮询排序
        ssList = pollingASeatStrategy.sortSeatVOList(ssList);
        SeatsResultVO<SeatWithStateVO> result = null;
        // 判断是否开通机器人并是否分配机器人
        if (RobotConfig.ROBOT_SWITCH && forRobot) {

            if (businessEnum.getId() == BusinessEnum.INTERTRAIN.getId()) {
                result = getRobotSeat(businessEnum, ssList, qunarName);
                if (result != null) {
                    logger.info(" getSingleSeatWithOnlineStateCommon函数中 getRobotSeat !=NULL");
                    return result;
                }
            }
        }


        // 会话保持功能
        if (StringUtils.isNotEmpty(qunarName)) {

            String lastSeatid = redisAgent.get(conversationHandleKey);
            boolean hondlesuccess = false;
            if (!Strings.isNullOrEmpty(lastSeatid)) {

                for (SeatWithStateVO seat : ssList) {
                    if (lastSeatid.equalsIgnoreCase(seat.getSeat().getQunarName())) {
                        hondlesuccess = true;
                        result = new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), seat);
                        logger.info(" getSingleSeatWithOnlineStateCommon函数中 会话保持功能 成功 busisupplied:{}, line:{}, user:{}, seat:{} ",
                                busiSupplierId, businessEnum.getEnName(), qunarName, result.getData().getSeat().getQunarName());
                    }
                }
            }

            if (!hondlesuccess) {
                logger.info(" getSingleSeatWithOnlineStateCommon函数中 会话保持功能 失败 busisupplied:{}, line:{}, user:{}, seat:{} ",
                        busiSupplierId, businessEnum.getEnName(), qunarName, "");
            }

//            String host = EjabdUtil.getUserDomain(qunarName, QChatConstant.QCHAR_HOST);
//            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(host);
//            if (null != plugin && plugin instanceof BaseChatPlugin) {
//                ((BaseChatPlugin) plugin).setMsgDao(msgDao);
//                result = plugin.getLastChatSeat(businessEnum, qunarName, ssList);
//            }
//
//            if (result != null && result.getData() != null && result.getData().getSeat() != null && result.getData().getSeat().getQunarName() != null) {
//                logger.info(" getSingleSeatWithOnlineStateCommon函数中 会话保持功能 pid:{}, qunarName:{}, resultId:{}", pid, qunarName, result.getData().getSeat().getQunarName());
//            } else {
//                logger.info(" getSingleSeatWithOnlineStateCommon函数中 会话保持功能 pid:{},qunarName:{},  resultId==null", pid, qunarName);
//            }
        } else {
            logger.error(" getSingleSeatWithOnlineStateCommon 无法会话保持 qunarName：{}, pid:{}, busiSupplierId:{}", qunarName, pid, busiSupplierId);
        }

        if (result == null) {
            result = new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), ssList.get(0));
            if (null != result.getData()
                    && null != result.getData().getSeat()
                    && !Strings.isNullOrEmpty(result.getData().getSeat().getQunarName()))
                redisAgent.set(conversationHandleKey, result.getData().getSeat().getQunarName(), 30, TimeUnit.DAYS);
        }
        return result;
    }


//    private SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineStateCommon(List<SeatAndGroup> seatAndGroups, String busiSupplierId, BusinessEnum businessEnum, String qunarName, String pid) {
//        return getSingleSeatWithOnlineStateCommon(seatAndGroups, busiSupplierId, businessEnum, qunarName, pid, true);
//    }

    @Override
    public List<SupplierAndSeatVO> getMoreSuSeatWithOnStList(List<SupplierRequestVO> suList) {
        if (CollectionUtil.isEmpty(suList)) {
            return null;
        }
        // 根据业务线供应商信息获取客服信息
        List<SeatAndBuSupplier> seatAndBuSupplier = getSeatBySupplier(suList);
        if (CollectionUtil.isEmpty(seatAndBuSupplier)) {
            return null;
        }

        // 获取客服状态
        List<SeatOnlineState> sos = getSeatOnlineState(seatAndBuSupplier);
        if (CollectionUtil.isEmpty(sos)) {
            return null;
        }

        // 根据客服状态进行排序
        Map<String, OnlineState> m = buildSeatStateMap(sos);
        List<SupplierAndSeatVO> bsasVOList = seatSortFactory.getSeatsWithOSListByBuSupplier(seatAndBuSupplier, m);

        // 过滤有关产品的客服
        List<SupplierAndSeatVO> newBsasVOList = filterSeatByPid(suList, bsasVOList);
        if (CollectionUtils.isNotEmpty(newBsasVOList)) {
            bsasVOList = newBsasVOList;
        }

        // 会话保持
        List<SupplierAndSeatVO> newBsasVOListV2 = filterSeatBySessionKeep(suList, bsasVOList);
        if (CollectionUtils.isNotEmpty(newBsasVOListV2)) {
            bsasVOList = newBsasVOListV2;
        }

        return bsasVOList;
    }

    private List<SeatOnlineState> getSeatOnlineState(List<SeatAndBuSupplier> seatAndBuSupplier) {
        Map<Long, List<String>> seatsGroupSupplierid = Maps.newHashMap();

        for (SeatAndBuSupplier seat : seatAndBuSupplier) {
            Long supplierId = seat.getSupplierId();
            if (seatsGroupSupplierid.containsKey(supplierId)) {
                List<String> seats = seatsGroupSupplierid.get(supplierId);
                seats.add(seat.getSeatName());
            } else {
                List<String> seats = Lists.newArrayList();
                seats.add(seat.getSeatName());
                seatsGroupSupplierid.put(supplierId, seats);
            }
        }

        List<SeatOnlineState> retList = Lists.newArrayList();
        for (Long key : seatsGroupSupplierid.keySet()) {
            List<SeatOnlineState> userInSuppliers = getSeatOnlineFixedStatie(key, seatsGroupSupplierid.get(key));
            if (!CollectionUtil.isEmpty(userInSuppliers)) {
                retList.addAll(userInSuppliers);
            }

        }
        return retList;
    }

    private List<SeatAndBuSupplier> getSeatBySupplier(List<SupplierRequestVO> suList) {
        List<String> bSuIdAndTypeList = new ArrayList<>();
        for (SupplierRequestVO su : suList) {
            bSuIdAndTypeList.add(su.getbSuId() + String.valueOf(su.getbType()));
        }

        return seatDao.getSeatsByBuSuIdAndType(bSuIdAndTypeList);
    }

    private List<SupplierAndSeatVO> filterSeatBySessionKeep(List<SupplierRequestVO> suList, List<SupplierAndSeatVO> bsasVOList) {
        if (CollectionUtils.isEmpty(bsasVOList)) {
            return null;
        }
        Map<String, SupplierAndSeatVO> suIdTypeAndVOMap = new HashedMap();
        for (SupplierAndSeatVO ssVO : bsasVOList) {
            suIdTypeAndVOMap.put(ssVO.getbSuId() + ssVO.getbType() + ssVO.getpId(), ssVO);
        }
        List<SupplierAndSeatVO> newBsasVOListV2 = null;
        for (SupplierRequestVO sReqVO : suList) {
            SupplierAndSeatVO sasVO = suIdTypeAndVOMap.get(sReqVO.getbSuId() + sReqVO.getbType() + sReqVO.getpId());
            if (sasVO == null) {
                continue;
            }
            if (CollectionUtils.isEmpty(newBsasVOListV2)) {
                newBsasVOListV2 = new ArrayList<>();
            }
            if (StringUtils.isEmpty(sReqVO.getuName())) {
                newBsasVOListV2.add(sasVO);
                continue;
            }

            String host = EjabdUtil.getUserDomain(sReqVO.getuName(), QChatConstant.DEFAULT_HOST);
            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(host);
            if (null != plugin && plugin instanceof BaseChatPlugin) {
                ((BaseChatPlugin) plugin).setMsgDao(msgDao);
                SeatsResultVO<SeatWithStateVO> srVO = plugin.getLastChatSeat(BusinessEnum.of(sReqVO.getbType()), sReqVO.getuName(), sasVO.getSeatWithStateVOList());
                if (srVO == null) {
                    newBsasVOListV2.add(sasVO);
                } else {
                    sasVO.setSeatWithStateVOList(Arrays.asList(srVO.getData()));
                    newBsasVOListV2.add(sasVO);
                }
            }
        }
        return newBsasVOListV2;
    }

    private List<SupplierAndSeatVO> filterSeatByPid(List<SupplierRequestVO> suList, List<SupplierAndSeatVO> bsasVOList) {
        boolean isFilter = Boolean.parseBoolean(Config.getPropertyInQConfig(ConfigConstants.FILTER_GROUP_BY_PRODUCT_SWITCH, "true"));
        if (!isFilter || CollectionUtils.isEmpty(suList)) {
            return bsasVOList;
        }
        Map<String, List<String>> suIdBTypeAndPIdMap = buildBsuIdTypeAndPidMap(suList);
        List<String> pIdList = buildPidList(suList);

        List<SupplierAndSeatVO> newBsasVOList = doFilterSeat(bsasVOList, suIdBTypeAndPIdMap, pIdList);
        return newBsasVOList;
    }

    private List<SupplierAndSeatVO> doFilterSeat(List<SupplierAndSeatVO> bsasVOList, Map<String, List<String>> suIdBTypeAndPIdMap, List<String> pIdList) {
        // 获取每个产品关联的所有客服
        Map<String, List<String>> pidAndSeatMap = getSeatNameList(pIdList);
        List<SupplierAndSeatVO> newBsasVOList = new ArrayList<>();
        for (SupplierAndSeatVO ssVO : bsasVOList) {
            List<String> pIdListFromMapv2 = suIdBTypeAndPIdMap.get(ssVO.getbSuId() + ssVO.getbType());
            if (CollectionUtils.isEmpty(pIdListFromMapv2)) {
                newBsasVOList.add(ssVO);
                continue;
            }

            for (String pId : pIdListFromMapv2) {
                SupplierAndSeatVO ssVONew = buildNewObject(ssVO);
                if (ssVONew == null)
                    continue;
                ssVONew.setpId(pId);
                List<SeatWithStateVO> ssVOList = ssVO.getSeatWithStateVOList();
                if (CollectionUtils.isEmpty(ssVOList)) {
                    newBsasVOList.add(ssVONew);
                    continue;
                }

                filterPidSeat(pidAndSeatMap, ssVONew, pId, ssVOList);
                newBsasVOList.add(ssVONew);
            }
        }
        return newBsasVOList;
    }

    private List<String> buildPidList(List<SupplierRequestVO> suList) {
        List<String> pIdList = new ArrayList<>();
        for (SupplierRequestVO sRequest : suList) {
            if (StringUtils.isNotEmpty(sRequest.getpId())) {
                pIdList.add(sRequest.getpId()); // 所有产品编号，供后续查询客服使用
            }
        }
        return pIdList;
    }

    private Map<String, List<String>> buildBsuIdTypeAndPidMap(List<SupplierRequestVO> suList) {
        Map<String, List<String>> suIdBTypeAndPIdMap = new HashedMap();
        for (SupplierRequestVO sRequest : suList) {
            String bSuId = sRequest.getbSuId();
            int bType = sRequest.getbType();
            String pId = sRequest.getpId();
            String key = bSuId + bType;
            List<String> pIdListFromMap = suIdBTypeAndPIdMap.get(key);
            if (CollectionUtils.isEmpty(pIdListFromMap)) {
                pIdListFromMap = new ArrayList<>();
            }
            pIdListFromMap.add(pId);
            suIdBTypeAndPIdMap.put(key, pIdListFromMap);
        }
        return suIdBTypeAndPIdMap;
    }

    private SupplierAndSeatVO buildNewObject(SupplierAndSeatVO ssVO) {
        SupplierAndSeatVO ssVONew = null;
        try {
            ssVONew = (SupplierAndSeatVO) ssVO.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("CloneNotSupportedException error", e);
            return null;
        }
//        if (ssVONew == null) {
//            ssVONew = new SupplierAndSeatVO();
//            ssVONew.setSeatWithStateVOList(ssVO.getSeatWithStateVOList());
//            ssVONew.setbSuId(ssVO.getbSuId());
//            ssVONew.setbType(ssVO.getbType());
//            ssVONew.setStrategy(ssVO.getStrategy());
//            ssVONew.setSuId(ssVO.getSuId());
//        }
        return ssVONew;
    }

    private Map<String, List<String>> getSeatNameList(List<String> pIdList) {
        if (CollectionUtils.isEmpty(pIdList)) {
            return null;
        }
        List<Seat> seatList = seatDao.getSeatListByPid(pIdList);
        if (CollectionUtils.isEmpty(seatList)) {
            return null;
        }
        Map<String, List<String>> pidAndSeatMap = new HashMap();
        for (Seat s : seatList) {
            List<String> qunarNameList = pidAndSeatMap.get(s.getPid());
            if (CollectionUtils.isEmpty(qunarNameList)) {
                qunarNameList = new ArrayList<>();
            }
            qunarNameList.add(s.getQunarName());
            pidAndSeatMap.put(s.getPid(), qunarNameList);
        }
        return pidAndSeatMap;
    }

    private void filterPidSeat(Map<String, List<String>> pidAndSeatMap, SupplierAndSeatVO ssVO, String pId, List<SeatWithStateVO> ssVOList) {
        if (pidAndSeatMap == null || pidAndSeatMap.size() <= 0) {
            return;
        }
        List<String> seatNameList = pidAndSeatMap.get(pId);
        if (CollectionUtils.isEmpty(seatNameList)) {
            return;
        }
        List<SeatWithStateVO> swsVOList = new ArrayList<>();
        for (SeatWithStateVO ssVO2 : ssVOList) {
            if (seatNameList.contains(ssVO2.getSeat().getQunarName())) {
                swsVOList.add(ssVO2);
            }
        }
        // 如果没有关联客服，则使用默认的客服信息
        if (CollectionUtils.isNotEmpty(swsVOList)) {
            ssVO.setSeatWithStateVOList(swsVOList);
        }
    }

    private Map<String, OnlineState> buildSeatStateMap(List<SeatOnlineState> sos) {
        return CollectionUtil.uniqueIndex(sos,
                new Function<SeatOnlineState, String>() {
                    @Override
                    public String apply(SeatOnlineState input) {
                        return input.getStrId();
                    }
                },
                new Function<SeatOnlineState, OnlineState>() {
                    @Override
                    public OnlineState apply(SeatOnlineState input) {
                        return input.getOnlineState();
                    }
                });
    }


    private List<GroupAndSeatVO> assembleAndFilterSeat(List<SeatAndGroup> seatAndGroups, String pid) {
        List<GroupAndSeatVO> gsList = assemble(seatAndGroups);
        if (StringUtils.isEmpty(pid)) {
            return gsList;
        }

        // 根据产品关联组过滤返回结果
        boolean isFilter = Boolean.parseBoolean(Config.getPropertyInQConfig(ConfigConstants.FILTER_GROUP_BY_PRODUCT_SWITCH, "true"));
        if (isFilter) {
            gsList = filterGroupByPid(pid, gsList);
        }
        return gsList;
    }


    private List<GroupAndSeatVO> filterGroupByPid(String pid, List<GroupAndSeatVO> gsList) {
        // 获取已关联的组和客服
        List<SeatAndGroup> sgList = seatGroupDao.getSeatAndGroupListByPid(pid);
        if (CollectionUtils.isNotEmpty(sgList)) {  // 如果已关联客服
            Map<Long, List<String>> groupSeatListMap = buildGroupSeatListMap(sgList);  // 构造group--SeatList对应关系

            // 构造过滤后的集合
            List<GroupAndSeatVO> gsListNew = buildNewSeatList(gsList, groupSeatListMap);
            if (CollectionUtils.isNotEmpty(gsListNew)) {  // 如果没有关联组则返回默认数据
                gsList = gsListNew;
            }
        }
        return gsList;
    }

    private List<GroupAndSeatVO> buildNewSeatList(List<GroupAndSeatVO> gsList, Map<Long, List<String>> groupSeatListMap) {
        List<GroupAndSeatVO> gsListNew = null;
        for (GroupAndSeatVO gs : gsList) {
            Long groupId = gs.getGroupId();
            List<String> seatNameList = groupSeatListMap.get(groupId);
            if (CollectionUtils.isEmpty(seatNameList)) {
                continue;
            }
            if (CollectionUtils.isEmpty(gsListNew)) {
                gsListNew = new ArrayList<>();
            }
            List<SeatWithStateVO> seatList = gs.getSeatWithStateVOList();
            if (CollectionUtils.isNotEmpty(seatList)) {
                gsListNew.add(gs);
            }
        }
        return gsListNew;
    }

    private Map<Long, List<String>> buildGroupSeatListMap(List<SeatAndGroup> sgList) {
        Map<Long, List<String>> groupSeatListMap;
        groupSeatListMap = new HashMap();
        for (SeatAndGroup sg : sgList) {
            Long groupId = sg.getGroupId();
            List<String> seatNameList = groupSeatListMap.get(groupId);
            if (CollectionUtils.isEmpty(seatNameList)) {
                seatNameList = new ArrayList<>();
            }
            seatNameList.add(sg.getQunarName());
            groupSeatListMap.put(groupId, seatNameList);
        }
        return groupSeatListMap;
    }

    private List<GroupAndSeatVO> assemble(List<SeatAndGroup> seatAndGroups) {
        Map<String, OnlineState> seatOnlineStateMap = warpSeatOnlineStateListGetter(seatAndGroups);
        return seatSortFactory.getSeatsWithOnlineStateList(seatAndGroups, seatOnlineStateMap);
    }

    @Override
    public List<SupplierQunarNameMappingVO> getQunarNamesByBusiSupplierIds(List<String> busiSupplierIds, BusinessEnum businessEnum) {
        if (CollectionUtil.isEmpty(busiSupplierIds)) {
            return null;
        }

        Map<String, List<SeatAndGroup>> supplierSeatMap = new HashMap<>();
        List<SeatAndGroup> allSeats = new ArrayList<>();
        for (String busiSupplierId : busiSupplierIds) {
            List<SeatAndGroup> temp = getSeatAndGroup(busiSupplierId, businessEnum);
            supplierSeatMap.put(busiSupplierId, temp);
            allSeats.addAll(temp);
        }
        Map<String, OnlineState> onlineStateMap = warpSeatOnlineStateListGetter(allSeats);
        List<SupplierQunarNameMappingVO> res = new ArrayList<>();
        for (String supplierId : supplierSeatMap.keySet()) {
            List<GroupAndSeatVO> gasList = seatSortFactory.getSeatsWithOnlineStateList(supplierSeatMap.get(supplierId), onlineStateMap);
            String seatQunarName;
            try {
                seatQunarName = gasList.get(0).getSeatWithStateVOList().get(0).getSeat().getQunarName();
            } catch (Exception e) {
                seatQunarName = null;
            }
            res.add(new SupplierQunarNameMappingVO(supplierId, seatQunarName));
        }

        return res;
    }


//    private List<SeatAndGroup> getSeatAndWithoutGroup(String busiSupplierId, BusinessEnum businessEnum) {
//        List<SeatAndGroup> seatAndGroups = seatDao.getSeatsWithoutGroupIds(busiSupplierId, businessEnum.getId());
//        return seatAndGroups;
//    }

    private List<SeatAndGroup> getSeatAndGroupWithGroupType(String busiSupplierId, BusinessEnum businessEnum,
                                                            String groupType) {
        return seatDao.getSeatsWithGroupType(busiSupplierId, businessEnum.getId(), groupType);
    }

//    @Override
//    public List<SeatGroup> getSeatGroupList(int supplierId, BusinessEnum businessEnum) {
//        return seatGroupDao.getSeatGroup(supplierId, businessEnum.getId());
//    }

    @Override
    public BusiReturnResult saveOrUpdateSeat(SeatVO seatVO) {
        if (seatVO == null) {
            logger.warn("saveOrUpdateSeat --- 参数不正确, seatVO == null");
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, false);
        }

        BusiReturnResult result = null;
        if (CollectionUtil.isNotEmpty(seatVO.getSuIdList())) {
            for (Long suId : seatVO.getSuIdList()) {
                seatVO.setSupplierId(suId);
                result = this.saveOrUpdateSingleSeat(seatVO);
            }
        } else {
            result = this.saveOrUpdateSingleSeat(seatVO);
        }

        BusiSupplierMapping bsm = busiSupplierMappingDao.getBusiSupplierMappingBySuId(seatVO.getSupplierId());
        return result;
    }

    private BusiReturnResult saveOrUpdateSingleSeat(SeatVO seatVO) {
        if (seatVO == null) {
            logger.warn("saveOrUpdateSeat --- 参数不正确, seatVO == null");
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, false);
        }
        logger.info("saveOrUpdateSeat ,seat info: {}", JacksonUtil.obj2String(seatVO));

        long supplierId = seatVO.getSupplierId();
        if (!SessionUtils.checkInputSuIdIsValid(supplierId)) {
            logger.error("saveOrUpdateSeat -- 不能操作其他供应商客服,当前登陆用户:{}, 操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), supplierId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_AUTH, false);
        }
        String operator = SessionUtils.getUserName();
        long seatId = seatVO.getId();
        List<Seat> seatList = seatDao.getSeat(seatVO.getQunarName());
        boolean isUpdate = seatId > 0 && CollectionUtils.isNotEmpty(seatList);
        if (isUpdate) {  // 编辑流程
            BusiReturnResult updateResult = updateSeatInfo(seatVO);
            if (!updateResult.isRet()) {
                return updateResult;
            }
            LogUtil.doLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_SEAT, (int) seatId, seatVO.getQunarName(), operator,
                    JacksonUtils.obj2String(seatVO));
        } else { // 添加流程
            BusiReturnResult checkResult = saveSeat(seatVO);
            if (!checkResult.isRet()) {
                return checkResult;
            }
            seatId = (Long) checkResult.getData();
            logger.info("saveOrUpdateSeat ,success to save seat, from DB seatId: {}", seatId);
            LogUtil.doLog(LogEntity.OPERATE_INSERT, LogEntity.ITEM_SEAT, (int) seatId, seatVO.getQunarName(), operator,
                    JacksonUtils.obj2String(seatVO));
        }

        saveSeatRelation(seatVO, seatId);

        return BusiReturnResultUtil.buildReturnResult(isUpdate ? BusiResponseCodeEnum.SUCCESS_UPDATE : BusiResponseCodeEnum.SUCCESS, true, seatId);
    }

    private void saveSeatRelation(SeatVO seatVO, long seatId) {
        // 添加客服与业务关联关系
        List<Business> busiList = seatVO.getBusiList();
        saveBusiSeatMapping(seatId, busiList);
        // 添加客服与组关联关系
        List<SeatGroup> sgList = seatVO.getGroupList();
        saveSeatGroupMapping(seatId, sgList);
    }

    private BusiReturnResult saveSeat(SeatVO seatVO) {
        String qunarName = StringUtils.trim(seatVO.getQunarName());
        // 判断用户中心是否存在
        if (needUserCenterCheck(seatVO.getSupplierId())) {
            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(EjabdUtil.getUserDomain(qunarName, QChatConstant.DEFAULT_HOST));
            if (null == plugin)
                return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS, "域名错误", false, null);
            BusiReturnResult result = plugin.checkUserExist(qunarName);
            if (!result.isRet())
                return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS, "用户中心不存在", false, null);
        }
        // 判断已否已经添加过
        String seatNames = seatDao.getSeatNameStrBySupplierId(seatVO.getSupplierId());
        if (StringUtils.isNotEmpty(seatNames) && seatNames.toLowerCase().contains(qunarName.toLowerCase())) {
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_REPEAT, "客服已经存在", false, null);
        }

        Seat s = buildSeat(seatVO, qunarName);
        long seatId = seatDao.saveSeat(s);
        return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.SUCCESS, true, seatId);
    }

    private boolean needUserCenterCheck(long supplierId) {
        Integer sid = Integer.valueOf(String.valueOf(supplierId));

        return true;
    }

    private Seat buildSeat(SeatVO seatVO, String qunarName) {
        Integer maxPriority = seatDao.getMaxSeatPriority(seatVO.getSupplierId());
        if (maxPriority == null) {
            maxPriority = 0;
        }

        Seat s = new Seat();
        s.setQunarName(qunarName.toLowerCase());
        s.setWebName(seatVO.getWebName());
        s.setNickName(seatVO.getNickName());
        s.setFaceLink(seatVO.getFaceLink());
        s.setPriority(maxPriority + 1);
        s.setSupplierId(seatVO.getSupplierId());
        s.setMaxSessions(seatVO.getMaxSessions());
        return s;
    }

    private void saveBusiSeatMapping(long seatId, List<Business> busiList) {
        if (seatId <= 0 || CollectionUtil.isEmpty(busiList)) {
            return;
        }
        for (Business busi : busiList) {
            BusiSeatMapping busiSeatMapping = new BusiSeatMapping();
            busiSeatMapping.setSeatId(seatId);
            busiSeatMapping.setBusiId(busi.getId());
            busiSeatMappingDao.saveBusiSeatMapping(busiSeatMapping);
            logger.info("saveOrUpdateSeat - saveBusiSeatMapping, seatId: {} ,busiId: {}", seatId, busi.getId());
        }
    }

    private void saveSeatGroupMapping(long seatId, List<SeatGroup> sgList) {
        if (seatId <= 0 || CollectionUtil.isEmpty(sgList)) {
            return;
        }
        for (SeatGroup sg : sgList) {
            SeatGroupMapping sgMapping = new SeatGroupMapping();
            sgMapping.setSeatId(seatId);
            sgMapping.setGroupId(sg.getId());
            seatGroupMappingDao.saveSeatGroupMapping(sgMapping);
            logger.info("saveOrUpdateSeat - saveBusiSeatMapping, seatId: {} ,groupId: {}", seatId, sg.getId());
        }
    }

    private BusiReturnResult updateSeatInfo(SeatVO seatVO) {
        long seatId = seatVO.getId();
        Seat seatDB = seatDao.getSeatBySeatId(seatId);
        if (seatDB == null) {
            logger.error("saveOrUpdateSeat -- 系统不存在该客服 seatId:{}", seatId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS, false);
        }

        busiSeatMappingDao.delBusiSeatMapping(seatId);
        SeatGroupMapping sgMapping = new SeatGroupMapping();
        sgMapping.setSeatId(seatId);
        seatGroupMappingDao.delSeatGroupMapping(sgMapping);

        seatDB.setWebName(seatVO.getWebName());
        seatDB.setNickName(seatVO.getNickName());
        seatDB.setFaceLink(seatVO.getFaceLink());
        seatDB.setSupplierId(seatVO.getSupplierId());
        seatDB.setMaxSessions(seatVO.getMaxSessions());
        int num = seatDao.updateSeat(seatDB);
        return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.SUCCESS_UPDATE, true, num);
    }

    public int updateSeat(SeatVO seatVO) {
        long seatId = seatVO.getId();
        Seat seatDB = seatDao.getSeatBySeatId(seatId);
        if (seatDB == null) {
            return 0;
        }

        seatDB.setWebName(seatVO.getWebName());
        seatDB.setNickName(seatVO.getNickName());
        seatDB.setFaceLink(seatVO.getFaceLink());
        seatDB.setSupplierId(seatVO.getSupplierId());
        seatDB.setServiceStatus(seatVO.getServiceStatus());

        int num = seatDao.updateSeat(seatDB);
        return num;
    }


    @Override
    public List<SeatVO> getSeatByQunarName(String qunarName) {
        List<Seat> sList = seatDao.getSeat(StringUtils.lowerCase(qunarName));
        List<SeatVO> sVOList = modelToVOList(sList);
        return sVOList;
    }

    @Override
    public Seat getSeatBySeatId(long seatId) {
        return seatDao.getSeatBySeatId(seatId);
    }

    private SeatVO seatConvertToVO(Seat s) {
        if (s == null) {
            return null;
        }
        SeatVO sVO = new SeatVO();
        sVO.setId(s.getId());
        sVO.setQunarName(s.getQunarName());
        sVO.setWebName(s.getWebName());
        sVO.setNickName(s.getNickName());
        sVO.setFaceLink(s.getFaceLink());
        sVO.setPriority(s.getPriority() == null ? 0 : s.getPriority());
        sVO.setSupplierId(s.getSupplierId());
        sVO.setBusinessId(s.getBusinessId() == null ? 0 : s.getBusinessId());
        sVO.setSupplierName(s.getSupplierName());
        sVO.setServiceStatus(s.getServiceStatus());
        return sVO;
    }

//    private List<Integer> getSeatGroupIds(int supplierId, BusinessEnum businessEnum) {
//        List<SeatGroup> seatGroups = getSeatGroupList(supplierId, businessEnum);
//
//        List<Integer> seatGroupIds = new ArrayList<>();
//        for (SeatGroup seatGroup : seatGroups) {
//            seatGroupIds.add(seatGroup.getId());
//        }
//        return seatGroupIds;
//    }

    /**
     * 用坐席和组去请求坐席的在线状态
     *
     * @param seatAndGroupList 　坐席和组
     * @return 在线状态
     */
    private Map<String, OnlineState> warpSeatOnlineStateListGetter(List<SeatAndGroup> seatAndGroupList) {
        if (CollectionUtil.isEmpty(seatAndGroupList)) {
            return null;
        }

        Map<Long, List<String>> seatsGroupBySupplierid = Maps.newHashMap();

//        List<String> strings = new ArrayList<>();
        for (SeatAndGroup seat : seatAndGroupList) {
            Long supplierId = seat.getSupplierId();
            if (seatsGroupBySupplierid.containsKey(supplierId)) {
                List<String> usernames = seatsGroupBySupplierid.get(supplierId);
                usernames.add(seat.getQunarName());
            } else {
                List<String> usernames = Lists.newArrayList();
                usernames.add(seat.getQunarName());
                seatsGroupBySupplierid.put(supplierId, usernames);
            }
        }

        List<SeatOnlineState> sos = Lists.newArrayList();
        for (Long key : seatsGroupBySupplierid.keySet()) {
            List<SeatOnlineState> sosPerSupplier = getSeatOnlineFixedStatie(key, seatsGroupBySupplierid.get(key));
            if (!CollectionUtil.isEmpty(sosPerSupplier))
                sos.addAll(sosPerSupplier);
        }
        if (CollectionUtil.isEmpty(sos)) {
            return null;
        }

        // serviceStatus
        return CollectionUtil.uniqueIndex(sos,
                new Function<SeatOnlineState, String>() {
                    @Override
                    public String apply(SeatOnlineState input) {
                        return input.getStrId();
                    }
                },
                new Function<SeatOnlineState, OnlineState>() {
                    @Override
                    public OnlineState apply(SeatOnlineState input) {
                        return input.getOnlineState();
                    }
                });
    }


    @Override
    public List<SeatVO> getSeatListBySupplierId(long supplierId) {
        List<Seat> seatList = seatDao.getSeatList(supplierId);
        List<SeatVO> seatVOList = modelToVOList(seatList);
        return seatVOList;
    }

    @Override
    public List<SeatVO> getSeatListBySupplierName(String supplierName) {
        List<Seat> seatList = seatDao.getSeatListBySupplierName(supplierName);
        List<SeatVO> seatVOList = modelToVOList(seatList);
        return seatVOList;
    }

    private List<SeatVO> modelToVOList(List<Seat> seatList) {
        if (CollectionUtil.isEmpty(seatList)) return null;
        List<SeatVO> seatVOList = new ArrayList<>();
        for (Seat s : seatList) {
            seatVOList.add(seatConvertToVO(s));
        }
        return seatVOList;
    }

    @Override
    public List<Seat> getSeatListByQunarNames(List<String> qunarName) {
        if (CollectionUtil.isNotEmpty(qunarName)) {
//            for (String name : qunarName) {
//                name.toLowerCase();
//            }
            return seatDao.getSeatListByQunarNames(qunarName);
        }
        return null;
    }

    @Override
    public String getSeatNameList(List<String> supplierIds, BusinessEnum busiType) {
        return seatDao.getSeatNameList(supplierIds, busiType.getId());
    }

    @Override
    public Map<String, ?> getUserAndSeatInfo(List<String> qunarNames, String fields) {
        // 过滤店铺id
        List<Map<String, Object>> userInfoList = Lists.newArrayList();

        List<Map<String, Object>> shops = Lists.newArrayList();
        List<Map<String, Object>> robots = Lists.newArrayList();

        List<String> shopIdsStr = Lists.newArrayList(Collections2.filter(qunarNames, isShop));
        if (CollectionUtils.isNotEmpty(shopIdsStr)) {
            List<Long> shopIds = Lists.transform(shopIdsStr, getShopId);
            shops = SupplierServiceUtil.buildSupplierInfo(shopIds);
            if (!CollectionUtil.isEmpty(shops))
                userInfoList.addAll(shops);
        }

        // 过滤店铺机器人信息
        List<String> robotids = Lists.newArrayList(Collections2.filter(qunarNames, new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.endsWith("_robot");
            }
        }));
        if (!CollectionUtil.isEmpty(robotids)) {
            robots = SupplierServiceUtil.buildRobotInfo(robotids);
            if (!CollectionUtil.isEmpty(robots))
                userInfoList.addAll(robots);
        }

        Map<String, ?> UserInfoMap = getUserInfoByQunarNames(qunarNames, fields);
        if (CollectionUtil.isEmpty(UserInfoMap)) {
            if (CollectionUtil.isEmpty(userInfoList)) {
                return null;
            } else {
                return JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(userInfoList)));
            }
        }


        try {
            @SuppressWarnings("unchecked") List<Map<String, Object>> userInfos
                    = (List<Map<String, Object>>) UserInfoMap.get("data");
            if (!CollectionUtil.isEmpty(userInfos)) {

                for (Map<String, Object> userinfo : userInfos) {
                    if (userinfo.containsKey("nickname") && userinfo.containsKey("username")) {
                        String nickname = userinfo.get("nickname").toString();
                        String username = userinfo.get("username").toString();
                        // 识别 nickname 为  a***z 这种类型的显示，用username代替
                        if (!Strings.isNullOrEmpty(username)) {
                            username = username.charAt(0) + "***" + username.charAt(username.length() - 1);
                            if (username.equalsIgnoreCase(nickname)) {
                                userinfo.put("nickname", userinfo.get("username"));
                            }
                        }
                    }
                }

                userInfoList.addAll(userInfos);
            }


            CollectionUtil.filterNull(userInfoList);

            if (CollectionUtil.isEmpty(userInfoList)) {
                return null;
            }

            Map<String, Seat> seatIndexMap = getSeatIndex(getSeatListByQunarNames(qunarNames));
            if (CollectionUtil.isEmpty(seatIndexMap)) {
                if (!CollectionUtil.isEmpty(userInfoList)) {
                    return JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(userInfoList)));
                }
                return null;
            }
            for (Map<String, Object> map : userInfoList) {
                String username = (String) map.get("username");
                Seat seat = seatIndexMap.get(username);
                if (seat != null) {
                    map.put("webname", seat.getWebName());
                    map.put("suppliername", seat.getSupplierName());
                }
                if (null != map.get("username") && !Strings.isNullOrEmpty(map.get("username").toString()))
                    map.put("username", map.get("username").toString().toLowerCase());
            }
            return JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(userInfoList)));
        } catch (Exception e) {
            logger.info("get user info error", e);
        }

        return null;
    }

    @Override
    public Map<String, ?> getNewUserAndSeatInfo(List<String> qunarNames, String fields) {
        // 过滤店铺id
        List<Map<String, Object>> userInfoList = Lists.newArrayList();

        List<Map<String, Object>> shops = Lists.newArrayList();
        List<Map<String, Object>> robots = Lists.newArrayList();
        List<Map<String, Object>> bnbs = Lists.newArrayList();

        List<String> shopIdsStr = Lists.newArrayList(Collections2.filter(qunarNames, isShop));
        if (CollectionUtils.isNotEmpty(shopIdsStr)) {
            List<Long> shopIds = Lists.transform(shopIdsStr, getShopId);
            shops = SupplierServiceUtil.buildSupplierInfo(shopIds);
            if (!CollectionUtil.isEmpty(shops))
                userInfoList.addAll(shops);
        }
        //过滤途家bnb_信息
        List<String> bnbids = Lists.newArrayList(Collections2.filter(qunarNames, new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.startsWith("demo_");
            }
        }));
        if(!CollectionUtil.isEmpty(bnbids)) {
            bnbs = getBnbUserInfoByQunarNames(bnbids);
            if(!CollectionUtil.isEmpty(bnbs)) {
                userInfoList.addAll(bnbs);
            }
        }


        // 过滤店铺机器人信息
        List<String> robotids = Lists.newArrayList(Collections2.filter(qunarNames, new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.endsWith("_robot") || s.startsWith("third_");
            }
        }));
        if (!CollectionUtil.isEmpty(robotids)) {
            robots = SupplierServiceUtil.buildRobotInfoWithConfig(robotids);
            if (!CollectionUtil.isEmpty(robots))
                userInfoList.addAll(robots);
        }

        Map<String, ?> UserInfoMap = getUserInfoByQunarNames(qunarNames, fields);
        if (CollectionUtil.isEmpty(UserInfoMap)) {
            if (CollectionUtil.isEmpty(userInfoList)) {
                return null;
            } else {
                return parseData(qunarNames, userInfoList);//JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(userInfoList)));
            }
        }


        try {
            @SuppressWarnings("unchecked") List<Map<String, Object>> userInfos
                    = (List<Map<String, Object>>) UserInfoMap.get("data");
            if (!CollectionUtil.isEmpty(userInfos)) {

                for (Map<String, Object> userinfo : userInfos) {
                    if(userinfo != null) {
                        if (userinfo.containsKey("nickname") && userinfo.containsKey("username")) {
                            String nickname = userinfo.get("nickname") == null ? "" : userinfo.get("nickname").toString();
                            String username = userinfo.get("username") == null ? "" : userinfo.get("username").toString();
                            // 识别 nickname 为  a***z 这种类型的显示，用username代替
                            if (!Strings.isNullOrEmpty(username)) {
                                username = username.charAt(0) + "***" + username.charAt(username.length() - 1);
                                if (username.equalsIgnoreCase(nickname)) {
                                    userinfo.put("nickname", userinfo.get("username"));
                                }
                            }
                        }
                    }
                }

                userInfoList.addAll(userInfos);
            }


            CollectionUtil.filterNull(userInfoList);

            if (CollectionUtil.isEmpty(userInfoList)) {
                return null;
            }

            Map<String, Seat> seatIndexMap = getSeatIndex(getSeatListByQunarNames(qunarNames));
            if (CollectionUtil.isEmpty(seatIndexMap)) {
                if (!CollectionUtil.isEmpty(userInfoList)) {
                    return parseData(qunarNames, userInfoList);//JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(userInfoList)));
                }
                return null;
            }
            for (Map<String, Object> map : userInfoList) {
                String username = (String) map.get("username");
                Seat seat = seatIndexMap.get(username);
                if (seat != null) {
                    map.put("webname", seat.getWebName());
                    map.put("suppliername", seat.getSupplierName());
                }
                if (null != map.get("username") && !Strings.isNullOrEmpty(map.get("username").toString()))
                    map.put("username", map.get("username").toString().toLowerCase());
            }
            return parseData(qunarNames, userInfoList);//JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(userInfoList)));
        } catch (Exception e) {
            logger.info("get user info error", e);
        }

        return null;
    }

    public Map<String, Object> parseData(List<String> qunarNames, List<Map<String, Object>> userInfoList) {
        if(CollectionUtil.isEmpty(userInfoList)) {
            return null;
        }
        List<String> names = new ArrayList<>();
        names.addAll(qunarNames);
        List<Map<String, Object>> resultDatas = Lists.newArrayList();
        Map<String, Map<String, Object>> serverDatas = new HashMap<>();
        for(Map userinfo : userInfoList) {
            if(!CollectionUtil.isEmpty(userinfo)) {
                Map<String, Object> data = new HashMap<>();
                if(userinfo.containsKey("uType") && Supplier.SHOP.equals(userinfo.get("uType"))){
                    data.put("username", userinfo.get("shopId"));
                    data.put("type", 4);
                    if(userinfo.containsKey("logoUrl") && userinfo.get("logoUrl") != null && !TextUtils.isEmpty(userinfo.get("logoUrl").toString())){
                        data.put("imageurl", userinfo.get("logoUrl"));
                    } else {
                        data.put("imageurl", "http://xxx");
                    }
                    data.put("email", "");
                    data.put("gender", 0);
                    data.put("loginName", "");
                    data.put("mobile", "");
                    data.put("nickname", userinfo.get("name"));
                    data.put("webname", "");
                    data.put("displaytype", (userinfo.get("displaytype") != null) ? userinfo.get("displaytype") : 0);
                    resultDatas.add(data);
                    names.remove(userinfo.get("shopId").toString());
                } else if(userinfo.containsKey("uType") && Supplier.BNB.equals(userinfo.get("uType"))) {
                    if(userinfo.get("username") != null){
                        data.put("username", userinfo.get("username"));
                        data.put("type", 0);
                        if(userinfo.containsKey("avatar") && userinfo.get("avatar") != null && !TextUtils.isEmpty(userinfo.get("avatar").toString())){
                            data.put("imageurl", userinfo.get("avatar"));
                        } else {
                            data.put("imageurl", "http://xxx");
                        }
                        data.put("email", "");
                        data.put("gender", 0);
                        data.put("loginName", userinfo.get("username"));
                        data.put("mobile", "");
                        data.put("nickname", userinfo.get("nickName"));
                        data.put("webname", userinfo.get("nickName"));
                        data.put("bu", userinfo.get("bnb"));
                        data.put("displaytype", (userinfo.get("displaytype") != null) ? userinfo.get("displaytype") : 0);
                        serverDatas.put(userinfo.get("username").toString(), data);
                    }
                } else if (userinfo.containsKey("uType") && Supplier.ROBOT.equals(userinfo.get("uType"))) {
                    if(userinfo.get("username") != null){
                        data.put("username", userinfo.get("username"));
                        data.put("type", 2);
                        if(userinfo.containsKey("imageurl")
                                && userinfo.get("imageurl") != null
                                && !TextUtils.isEmpty(userinfo.get("imageurl").toString())){
                            data.put("imageurl", userinfo.get("imageurl"));
                        } else {
                            data.put("imageurl", "http://xxx");
                        }
                        data.put("email", "");
                        data.put("gender", 0);
                        data.put("loginName", userinfo.get("loginName"));
                        data.put("mobile", "");
                        data.put("nickname", userinfo.get("nickname"));
                        data.put("webname", userinfo.get("webname"));
                        data.put("displaytype", (userinfo.get("displaytype") != null) ? userinfo.get("displaytype") : 0);
                        serverDatas.put(userinfo.get("username").toString(), data);
                    }
                } else if (userinfo.containsKey("uType") && Supplier.HOTEL_PRE_SALE.equals(userinfo.get("uType"))) {
                    if(userinfo.get("username") != null){
                        data.put("username", userinfo.get("username"));
                        data.put("type", 0);
                        if(userinfo.containsKey("icon")
                                && userinfo.get("icon") != null
                                && !TextUtils.isEmpty(userinfo.get("icon").toString())){
                            data.put("imageurl", userinfo.get("icon"));
                        } else {
                            data.put("imageurl", "http://xxx");
                        }
                        data.put("email", "");
                        data.put("gender", 0);
                        data.put("loginName", userinfo.get("uid"));
                        data.put("mobile", "");
                        data.put("nickname", userinfo.get("name"));
                        data.put("webname", userinfo.get("name"));
                        data.put("displaytype", (userinfo.get("displaytype") != null) ? userinfo.get("displaytype") : 0);
                        serverDatas.put(userinfo.get("username").toString(), data);
                    }
                } else {
                    if(userinfo.get("username") != null){
                        data.put("username", userinfo.get("username"));
                        if(userinfo.containsKey("webname") && !TextUtils.isEmpty(userinfo.get("webname").toString())) {
                            data.put("type", 1);
                        } else {
                            data.put("type", 0);
                        }
                        data.put("imageurl", "https://xxx");
                        data.put("email", userinfo.get("email"));
                        data.put("gender", userinfo.get("gender"));
                        data.put("loginName", userinfo.get("loginName"));
                        data.put("mobile", userinfo.get("mobile"));
                        data.put("nickname", userinfo.get("nickname"));
                        data.put("webname", userinfo.get("webname"));
                        data.put("displaytype", (userinfo.get("displaytype") != null) ? userinfo.get("displaytype") : 0);
                        serverDatas.put(userinfo.get("username").toString(), data);
                    }
                }
            }
        }
        for(String user : names) {
            if(serverDatas.containsKey(user)) {
                Map<String, Object> data = serverDatas.get(user);
                data.put("extentInfo", Lists.newArrayList());
                resultDatas.add(data);
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("username", user);
                data.put("type", -1);
                resultDatas.add(data);
            }
        }
        if(CollectionUtil.isEmpty(resultDatas)) {
            return null;
        }
        return JacksonUtil.string2Map(JacksonUtil.obj2String(JsonData.success(resultDatas)));
    }


    @Override
    public Map<String, ?> getUserInfoByQunarNames(List<String> qunarNames, String fields) {
        if (CollectionUtil.isEmpty(qunarNames)) {
            return null;
        }
        Map<String, String> formParams = new HashMap<>();
        formParams.put("username", Joiner.on(',').skipNulls().join(qunarNames));
        formParams.put("outEncrypt", "true");
        if (StringUtils.isNotBlank(fields)) {
            formParams.put("fields", fields);
        }
        String jsonRes = HttpClientUtils.post(Config.USER_CENTER_INFO_URL, formParams);
        return JacksonUtil.string2Map(jsonRes);
    }

    @Override
    public List<Map<String,Object>> getBnbUserInfoByQunarNames(List<String> qunarNames) {
        if(CollectionUtil.isEmpty(qunarNames)) {
            return null;
        }
        List<Map<String,Object>> infos = Lists.newArrayList();
        for(String name : qunarNames) {
            Map<String, String> params = new HashMap<>();
            params.put("userId", name);
            String jsonRes = HttpClientUtils.get(Config.BNB_USER_INFO_URL, params);
            Map<String, Object> res = JacksonUtil.string2Map(jsonRes);
            if(!CollectionUtil.isEmpty(res) && res.containsKey("status") && Integer.valueOf(res.get("status").toString()) == 0) {
                Map<String, Object> map = (Map<String, Object>) res.get("users");
                map.put("uType", Supplier.BNB);
                map.put("username", name);
                infos.add(map);
            }
        }
        if(CollectionUtil.isEmpty(infos)) {
            return null;
        }
        return infos;
    }



    @Override
    public int updateSeatByQunarName(String qunarName, String newWebName) {
        if (StringUtils.isEmpty(qunarName) || StringUtils.isEmpty(newWebName)) {
            return 0;
        }
        return seatDao.updateSeatByQunarName(qunarName, newWebName);
    }

    @Override
    public SeatListVO pageQuerySeatList(SeatQueryFilter filter, int pageNum, int pageSize) {
        // 根据供应商编号\qunarName\webName\业务类型\  获取客服列表
        long totalCount = seatDao.pageQuerySeatListCount(filter);
        if (totalCount <= 0) {
            return null;
        }
        logger.info("pageQuerySeatList SeatQueryFilter:{}", JacksonUtil.obj2String(filter));
        List<Seat> seatList = seatDao.pageQuerySeatList(filter, pageNum, pageSize);

        List<Long> seatIds = buildSeatIds(seatList);

        List<BusiSeatMapping> bsList = null;
        List<SeatGroupBusiMapping> sgbList = null;
        if (CollectionUtil.isNotEmpty(seatIds)) {
            // 获取每个客服所属的业务
            bsList = seatDao.getSeatBusiListBySeatId(seatIds);
            // 获取每个客服所属组
            sgbList = seatDao.getGroupAndBusiListBySeatId(seatIds);
        }

        SeatListVO slVO = new SeatListVO();
        slVO.setTotalCount(totalCount);
        slVO.setPageNum(pageNum);
        slVO.setPageSise(pageSize);

        Map<Long, List<BusiSeatMapping>> bsMap = null;
        if (CollectionUtil.isNotEmpty(bsList)) {
            bsMap = new HashMap<>();
            for (BusiSeatMapping bs : bsList) {
                List<BusiSeatMapping> bsmList = bsMap.get(bs.getSeatId());
                if (bsmList == null) {
                    bsmList = new ArrayList<>();
                }
                bsmList.add(bs);
                bsMap.put(bs.getSeatId(), bsmList);
            }
        }


        Map<String, List<SeatGroupBusiMapping>> sgbMap = null;
        if (CollectionUtil.isNotEmpty(sgbList)) {
            sgbMap = new HashMap<>();
            for (SeatGroupBusiMapping sgbm : sgbList) {
                String key = String.valueOf(sgbm.getSeatId()) + String.valueOf(sgbm.getBusiId());
                List<SeatGroupBusiMapping> sgbmList = sgbMap.get(key);
                if (sgbmList == null) {
                    sgbmList = new ArrayList<>();
                }
                sgbmList.add(sgbm);
                sgbMap.put(key, sgbmList);
            }
        }

        List<SeatListSubVO> slsVOList = new ArrayList<>();
        for (Seat s : seatList) {
            long seatId = s.getId();
            SeatListSubVO seatListSubVO = new SeatListSubVO();

            // 构造客服自身属性
            seatListSubVO.setId(s.getId());
            seatListSubVO.setQunarName(s.getQunarName());
            seatListSubVO.setWebName(s.getWebName());
            seatListSubVO.setPriority(s.getPriority() == null ? 0 : s.getPriority());
            seatListSubVO.setFaceLink(s.getFaceLink());
            seatListSubVO.setNickName(s.getNickName());
            seatListSubVO.setCreateTime(s.getCreateTime().getTime());
            seatListSubVO.setSupplierId(s.getSupplierId());
            seatListSubVO.setSupplierName(s.getSupplierName());
            seatListSubVO.setMaxSessions(s.getMaxSessions());
            seatListSubVO.setServiceStatus(s.getServiceStatus());
            seatListSubVO.setBindWx(s.getBindWx() == BindWxStatus.BIND_WX.code);
            seatListSubVO.setHost(s.getHost());


            // 构造客服所属业务
            if (CollectionUtil.isNotEmpty(bsMap)) {
                List<BusinessVO> busiList = new ArrayList<>();
                List<BusiSeatMapping> bsmList = bsMap.get(seatId);
                if (bsmList != null) {
                    for (BusiSeatMapping bsm : bsmList) {
                        int busiId = bsm.getBusiId();
                        BusinessVO bVO = new BusinessVO();
                        bVO.setId(busiId);
                        bVO.setName(bsm.getBusiName());
                        if (CollectionUtil.isNotEmpty(sgbMap)) {
                            String key = String.valueOf(seatId) + String.valueOf(busiId);
                            List<SeatGroupBusiMapping> sgbmList = sgbMap.get(key);
                            if (sgbmList != null) {
                                List<SeatGroup> sgList = new ArrayList<SeatGroup>();
                                for (SeatGroupBusiMapping sgbm : sgbmList) {
                                    SeatGroup sg = new SeatGroup();
                                    sg.setId(sgbm.getGroupId());
                                    sg.setName(sgbm.getGroupName());
                                    sgList.add(sg);
                                }
                                bVO.setGroupList(sgList);
                            }
                        }
                        busiList.add(bVO);
                    }
                    seatListSubVO.setBusiList(busiList);
                }
            }
            slsVOList.add(seatListSubVO);
        }
        slVO.setSeatList(slsVOList);

        return slVO;
    }

    @Override
    public int delSeatById(long seatId) {
        Seat seatDB = seatDao.getSeatBySeatId(seatId);
        if (seatDB == null) {
            return 0;
        }

        if (!SessionUtils.checkInputSuIdIsValid(seatDB.getSupplierId())) {
            logger.error("deleteSeat -- 不能删除其他供应商的客服, 当前登陆供应商编号:{},操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), seatDB.getSupplierId());
            return BusinessResponseCodeConstants.FAIL_AUTH_OWNER;
        }

        int num = seatDao.delSeatById(seatId);
        if (num > 0) {
            busiSeatMappingDao.delBusiSeatMapping(seatId);
            SeatGroupMapping sgMapping = new SeatGroupMapping();
            sgMapping.setSeatId(seatId);
            seatGroupMappingDao.delSeatGroupMapping(sgMapping);
        }

        BusiSupplierMapping bsm = busiSupplierMappingDao.getBusiSupplierMappingBySuId(seatDB.getSupplierId());
        return num;
    }

    @Override
    public boolean sortSeat(long preSeatId, long curSeatId, long supplierId) {
        logger.info("sortSeat --- preSeatId: {}, curSeatId: {}", preSeatId, curSeatId);
        Seat preSeat = this.getSeatBySeatId(preSeatId);
        Seat curSeat = this.getSeatBySeatId(curSeatId);

        if (curSeat == null) {
            return false;
        }
        Integer prePriority = 0;
        if (preSeatId > 0) {
            prePriority = preSeat.getPriority();
            if (prePriority == null) {
                logger.error("sortSeat --- 客服优先级不存在,客服编号: {}", preSeatId);
                return false;
            }
        }

        int curPriority = prePriority + 1;
        seatDao.updateAfterSeatPriority(supplierId, prePriority);
        seatDao.updateSeatPriority(curSeatId, curPriority);

        logger.info("sortSeat --- 客服排序成功,客服编号: {},优先级:{}", curSeatId, curPriority);
        return true;

    }

    private List<Long> buildSeatIds(List<Seat> seatList) {
        List<Long> seatIdList = new ArrayList<>();
        for (Seat s : seatList) {
            seatIdList.add(s.getId());
        }
        return seatIdList;
    }

    public Map<String, Seat> getSeatIndex(List<Seat> seatList) {
        if (CollectionUtil.isEmpty(seatList)) {
            return null;
        }
        Map<String, Seat> index = new HashMap<>();
        for (Seat seat : seatList) {
            if (index.get(seat.getQunarName()) == null) {
                index.put(seat.getQunarName(), seat);
            }
        }
        return index;
    }


    /**
     * 如果qunarName存在，则判断是否在线，如果在线返回，不在线，重新分配客服
     *
     * @param supplierId
     * @param seatQName
     * @param userQName
     * @return
     */
    @Override
    public SeatsResultVO<SeatWithStateVO> judgeOrRedistribute(long supplierId, String seatQName, String userQName) {


        if (supplierId <= 0) {
            logger.info("judgeOrRedistribute 函数中，seatQName:{},userQName:{}", seatQName, userQName);
            return null;
        }

        List<SeatAndGroup> seatAndGroups = Lists.newArrayList();
        if (StringUtils.isNotBlank(seatQName)) {
            logger.info("judgeOrRedistribute 函数中 seatQName is not blank，seatQName:{},userQName:{}", seatQName, userQName);
            List<SeatOnlineState> seatOnlineStateList = getSeatOnlineFixedStatie(supplierId, Lists.<String>newArrayList(seatQName));

            if (CollectionUtils.isNotEmpty(seatOnlineStateList)
                    && (seatOnlineStateList.get(0).getOnlineState() == OnlineState.ONLINE
                    || seatOnlineStateList.get(0).getOnlineState() == OnlineState.BUSY)) {
                seatAndGroups = Lists.newArrayList(seatDao.getSeatAndGroupByQName(seatQName, supplierId));
            }
        }
        //获取坐席ids

        Function<SeatAndGroup, String> getQuanrId = new Function<SeatAndGroup, String>() {
            @Override
            public String apply(SeatAndGroup seatWithStateVO) {
                if (null != seatWithStateVO) {
                    return seatWithStateVO.getQunarName();
                }
                return "";
            }
        };

        List<String> ids = Lists.transform(seatAndGroups, getQuanrId);
        logger.info("judgeOrRedistribute 函数中 seatAndGroups，seatQName:{},userQName:{}, online or busy ids:{}"
                , seatQName, userQName, listToString(ids, ','));
        // seatAndGroups说明这个客服不在线，按照分配逻辑获取一在线客服信息
        if (CollectionUtils.isEmpty(seatAndGroups)) {
            logger.info("judgeOrRedistribute 函数中，seatQName:{},userQName:{}, seatAndGroups is Empty"
                    , seatQName, userQName);
            seatAndGroups = getSeatAndGroup(supplierId, seatQName);

            ids = Lists.transform(seatAndGroups, getQuanrId);
            logger.info("judgeOrRedistribute 函数中 通过函数getSeatAndGroup再次获取 seatQName:{},userQName:{}, reids:{}"
                    , seatQName, userQName, listToString(ids, ','));
        }
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            logger.info("judgeOrRedistribute 函数中 通过函数getSeatAndGroup再次获取仍为空 seatQName:{},userQName:{}"
                    , seatQName, userQName);
            return null;
        }
        // 设置一个全局策略 TODO 应该配置化
        for (SeatAndGroup sg : seatAndGroups) {
            if (null != sg)
                sg.setStrategy(SeatSortStrategyEnum.POLLING_STRATEGY.getStrategyId());
        }
        String busiSupplierId = seatAndGroups.get(0).getBusiSupplierId();
        BusinessEnum businessEnum = BusinessEnum.of(seatAndGroups.get(0).getBusiId());
        return getSingleSeatWithOnlineStateCommon(seatAndGroups, busiSupplierId, businessEnum, userQName, null, false);
    }

    /**
     * 从数据库中获取坐席和组的映射关系
     *
     * @param busiSupplierId 　业务线的供应商id
     * @param businessEnum   业务线id
     * @return 坐席和坐席的映射关系
     */
    private List<SeatAndGroup> getSeatAndGroup(String busiSupplierId, BusinessEnum businessEnum) {
        List<SeatAndGroup> seatAndGroups = seatDao.getSeatsByGroupIds(busiSupplierId, businessEnum.getId());
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            seatAndGroups = seatDao.getSeatsWithoutGroupIds(busiSupplierId, businessEnum.getId());
        }
        return seatAndGroups;
    }

    private List<SeatAndGroup> getSeatAndGroup(long supplierId, String seatQName) {
        List<SeatAndGroup> seatAndGroups;
        if (Strings.isNullOrEmpty(seatQName)) {
            seatAndGroups = seatDao.getSeatAndGroupBySupplierId(supplierId);
        } else {
            seatAndGroups = seatDao.getSeatBySeatQNameGroup(supplierId, seatQName);
        }
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            seatAndGroups = seatDao.getSeatsWithoutGroupBySupplierId(supplierId);
        }
        return seatAndGroups;
    }

    @Override
    public Map<Long, List<Seat>> getSeatListBySupplierIds(List<Long> supplierIds) {
        Map<Long, List<Seat>> result = Maps.newHashMap();
        List<Seat> seats = seatDao.querySeatListBySupplierIds(supplierIds);
        if (CollectionUtil.isEmpty(seats)) {
            return result;
        }
        for (Seat seat : seats) {
            Long supplierId = seat.getSupplierId();
            List<Seat> seatlist = result.get(supplierId);
            if (seatlist == null) {
                result.put(supplierId, Lists.newArrayList(seat));
            } else {
                seatlist.add(seat);
            }
        }
        return result;
    }

    private static final Function<SupplierVO, Long> getSupplierId = new Function<SupplierVO, Long>() {
        @Override
        public Long apply(SupplierVO supplierVO) {
            if (supplierVO == null) {
                return null;
            }
            return supplierVO.getId();
        }
    };

    @Override
    public List<SupplierVO> filterSeatList(List<SupplierVO> supplierVOs) {
        if (CollectionUtil.isEmpty(supplierVOs)) {
            return supplierVOs;
        }
        Map<Long, List<Seat>> seats = getSeatListBySupplierIds(Lists.transform(supplierVOs, getSupplierId));
        if (MapUtils.isEmpty(seats)) {
            return supplierVOs;
        }
        for (SupplierVO supplierVO : supplierVOs) {
            long supplierId = supplierVO.getId();
            supplierVO.setSeatList(seats.get(supplierId));
        }
        return supplierVOs;
    }

    private static final Predicate<SeatWithStateVO> filterOnlineSeat = new Predicate<SeatWithStateVO>() {
        @Override
        public boolean apply(SeatWithStateVO seatWithStateVO) {
            if (seatWithStateVO == null)
                return false;
            OnlineState onlineState = seatWithStateVO.getOnlineState();
            if (onlineState != null && (onlineState == OnlineState.ONLINE || onlineState == OnlineState.BUSY)) {
                return true;
            }
            return false;
        }
    };

    @Override
    public SeatsResultVO<SeatWithStateVO> getRobotSeat(BusinessEnum businessEnum, List<SeatWithStateVO> seatList,
                                                       String userName) {
        if (businessEnum == null || businessEnum == BusinessEnum.EMPTY) {
            return null;
        }
        Robot robot = robotService.getRobotByBusiness(businessEnum);
        // 没有配置机器人，返回null，走原来逻辑
        if (robot == null) {
            return null;
        }
        // 如果配置了机器人，但在线客服中，一定时间段有咨询过的，返回客服
        // 过滤在线客服

        Function<SeatWithStateVO, String> getShopIds = new Function<SeatWithStateVO, String>() {
            @Override
            public String apply(SeatWithStateVO seatWithStateVO) {
                if (null != seatWithStateVO) {
                    if (seatWithStateVO.isSwitchOn()) {
                        if (null != seatWithStateVO.getSupplier())
                            return seatWithStateVO.getSupplier().getShopId();
                    } else {
                        if (null != seatWithStateVO.getSeat())
                            return seatWithStateVO.getSeat().getQunarName();
                    }
                }
                return "";
            }
        };


        List<SeatWithStateVO> onlineSeats = Lists.newArrayList(Collections2.filter(seatList, filterOnlineSeat));

        String lastSeatName = "";
        String domain = EjabdUtil.getUserDomain(userName, QChatConstant.DEFAULT_HOST);
        IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(domain);
        if (null != plugin && plugin instanceof BaseChatPlugin) {
            ((BaseChatPlugin) plugin).setMsgDao(msgDao);
            lastSeatName = plugin.seatIntervalRobot(
                    Lists.transform(onlineSeats, getSeatName),
                    userName,
                    Lists.transform(onlineSeats, getShopIds)
            );
        }

        if (!Strings.isNullOrEmpty(lastSeatName)) {
            for (SeatWithStateVO seatWithStateVO : seatList) {
                if (StringUtils.equals(seatWithStateVO.getSeat().getQunarName(), lastSeatName)) {
                    return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), seatWithStateVO);
                }
            }
        }
        // 返回机器人id
        Long supplierId = seatList.get(0).getSeat().getSupplierId();
        SeatWithStateVO robotSeat = new SeatWithStateVO();
        robotSeat.setOnlineState(OnlineState.ONLINE);
        robotSeat.setSeat(robot.toSeat(supplierId));
        robotSeat.setSwitchOn(seatList.get(0).isSwitchOn());
        robotSeat.setSupplier(seatList.get(0).getSupplier());
        return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), robotSeat);
    }


    @Override
    public String getBusiSupplieridBySupplierID(String supplierID, BusinessEnum businessEnum) {
        if (TextUtils.isEmpty(supplierID))
            return null;
        if (null == businessEnum)
            return null;
        return seatDao.getBusiSupplieridBySupplierID(Integer.valueOf(supplierID), businessEnum.getId());
    }

    @Override
    public List<SeatOnlineState> getSeatOnlineFixedStatie(long lSupplierid, List<String> qunarNameList) {
        if (CollectionUtil.isEmpty(qunarNameList)) {
            return Lists.newArrayList();
        }

        // qunarNameList 可能包含各种域名的人
        // 将他们根据域名区分，没有域名的按照 qchat处理
        List<SeatOnlineState> stateList = Lists.newArrayList();
        Map<String, List<String>> domainedUsers = EjabdUtil.spliteUsersByDomain(qunarNameList);
        for (String key : domainedUsers.keySet()) {
            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(key);
            if (null != plugin) {
                List<SeatOnlineState> sublist = plugin.getUsersOnlineStatus(domainedUsers.get(key));
                stateList.removeAll(sublist);
                stateList.addAll(sublist);
            }
        }

        if (CollectionUtil.isEmpty(stateList))
            return Lists.newArrayList();

        Map<String, ServiceStatusEnum> userServiceStatus = Maps.newHashMap();

        // 查找这几个人的最后一次修改信息
        List<Seat> seats = getSeatListByQunarNames(qunarNameList);


        for (Seat seat : seats) {
            if (lSupplierid > 0) {
                if (seat.getSupplierId() == lSupplierid) {
                    // 不是这家人，不做考虑,只考虑这家
                    if (ServiceStatusEnum.SUPER_MODE.getKey() == seat.getServiceStatus() || seat.getBindWx() == BindWxStatus.BIND_WX.code) {
                        userServiceStatus.put(seat.getQunarName(), ServiceStatusEnum.SUPER_MODE);
                    } else {
                        userServiceStatus.put(seat.getQunarName(), ServiceStatusEnum.of(seat.getServiceStatus()));
                    }
                }
            } else {
                // 没有什么依据，随便找一个
                userServiceStatus.put(seat.getQunarName(), ServiceStatusEnum.of(seat.getServiceStatus()));
            }
        }

        for (SeatOnlineState sos : stateList) {
            String username = sos.getStrId();
            //  方便查找问题
            sos.setRawo(sos.getOnlineState());


            if (userServiceStatus.containsKey(username)) {
                sos.setServicestate(userServiceStatus.get(username).getKey());
                ServiceStatusEnum serviceStatusEnum = userServiceStatus.get(username);
                if (ServiceStatusEnum.SUPER_MODE == serviceStatusEnum) {
                    sos.setOnlineState(OnlineState.ONLINE);
                } else if (ServiceStatusEnum.DND_MODE == serviceStatusEnum) {
                    sos.setOnlineState(OnlineState.OFFLINE);
                }
            } else {
                sos.setServicestate(ServiceStatusEnum.STANDARD_MODE.getKey());
            }

        }

        return stateList;

    }

    @Override
    public void transferReadSeat(String userName, String shopid, String fromSeatName, String toSeatName) {

        String shopName = EjabdUtil.makeSureUserid(shopid);

        // old session
        List<Session> sessionList = sessionDao.getSession(userName, shopName, "");
        if (!CollectionUtil.isEmpty(sessionList)) {
            Session lastSession = sessionList.get(0);
            for (Session ss : sessionList) {
                if (!Strings.isNullOrEmpty(fromSeatName) && fromSeatName.equalsIgnoreCase(ss.getSeat_name()) && ss.getSession_state() == SessionStateEnum.STATE_ASSIGNED.getState()) {
                    ss.setSession_state(SessionStateEnum.STATE_TRANSED.getState());
                    sessionDao.updateSession(ss);
                    RedisBizUtil.updateSession(ss);
                }
            }

            if (!Strings.isNullOrEmpty(toSeatName)) {
                sessionList = sessionDao.getSession(userName, shopName, "");
                boolean isFind = false;
                for (Session ss : sessionList) {
                    if (toSeatName.equalsIgnoreCase(ss.getSeat_name())
                            && SessionStateEnum.STATE_FINISHED.getState() != ss.getSession_state()) {

                        if (SessionStateEnum.STATE_ASSIGNED.getState() != ss.getSession_state()) {
                            ss.setSession_state(SessionStateEnum.STATE_ASSIGNED.getState());
                            ss.setSeat_name(toSeatName);
                            ss.setIsrobot_seat(toSeatName.indexOf(QChatConstant.SEATROBOTPOSTFIX) == 0 ? 1 : 0);
                            sessionDao.updateSession(ss);
                            RedisBizUtil.updateSession(ss);
                        }

                        isFind = true;
                    }

                }
                if (!isFind) {
                    Session newSession = new Session();
                    newSession.setUser_name(userName);
                    newSession.setSession_state(SessionStateEnum.STATE_ASSIGNED.getState());
                    newSession.setShop_name(lastSession.getShop_name());
                    newSession.setSeat_name(toSeatName);
                    newSession.setIsrobot_seat(toSeatName.indexOf(QChatConstant.SEATROBOTPOSTFIX) == 0 ? 1 : 0);
                    newSession.setProduct_id(lastSession.getProduct_id());
                    newSession.setSession_id(String.valueOf(System.currentTimeMillis()));
                    sessionDao.insertSession(newSession);
                    RedisBizUtil.updateSession(newSession);
                }
            }
        }
    }

//    @Override
//    public SeatsResultVO<SeatWithStateVO> turnOnRealSeat(String pid, long supplierid, String userName) {
//        // 获取业务线信息
//
//        Supplier supplier = supplierDao.getSupplier(0, "", supplierid);
//        if (null == supplier)
//            return null;
//
//        SeatNoRobotSelector ss = ApplicationContextHelper.popBean("seatNoRobotSelector", SeatNoRobotSelector.class);
//        SeatsResultVO<SeatWithStateVO> resultVO = null;
//
//        if (null != ss) {
//            ISeatSelectorEvents events = ApplicationContextHelper.popBean("robotSelectorNoticeEvents", RobotSelectorNoticeEvents.class);
//
//            SelectorConfigration configration =
//                    new SelectorConfigration.Builder()
//                            .businessEnum(null)
//                            .busiSupplierId(null)
//                            .groupType(null)
//                            .productID(pid)
//                            .qunarName(userName) // 会话保持用得到，一定要的
//                            .supplierId(supplierid)
//                            .lastSeatName(null) // 这个一定是个空，因为由机器人过到人，不会有优先分配的逻辑
//                            .events(events)
//                            .build();
//
//            // 先分出一个来在说
//            resultVO = ss.getOneSeat(configration);
//
//            List<Session> sessionList = sessionDao.getSession(userName, QChatConstant.SEATSHOPPREFIX + supplierid, pid);
//
//            if (!CollectionUtil.isEmpty(sessionList)) {
//                for (Session session : sessionList) {
//                    if (SessionStateEnum.STATE_ASSIGNED == SessionStateEnum.valueOf(session.getSession_state())) {
//                        // 如果开启排队，变成排队，没开启排队，状态不变
//                        if (null != resultVO) {
//                            session.setSeat_name(resultVO.getData().getSeat().getQunarName());
//                            session.setIsrobot_seat(resultVO.getData().getSeat().isIsrobot() ? 1 : 0);
//                        }
//
//                        if (supplier.getBQueue() != 0) {
//                            session.setSession_state(SessionStateEnum.STATE_QUEUE.getState());
//                        }
//
//                        sessionDao.updateSession(session);
//                        RedisBizUtil.updateSession(session);
//                        break;
//                    }
//                }
//            }
//
//
//        }
//        return resultVO;
//    }


    @Override
    public SeatsResultVO<SeatWithStateVO> assignedOneSeat(String shopid, String qunarName) {

        String supplierid = shopid.replace(QChatConstant.QCHAT_HOST_POSTFIX, "")
                .replace(QChatConstant.QTALK_DOMAIN_POSTFIX, "")
                .replace(QChatConstant.SEATSHOPPREFIX, "");

        long lSupplierId = Long.valueOf(supplierid);

        Supplier supplier = supplierDao.getSupplier(0, "", lSupplierId);
        if (null == supplier)
            return null;

        // 创建选择器
        SeatDefaultSelector selector = ApplicationContextHelper.popBean(
                "seatDefaultSelector", SeatDefaultSelector.class);
        String shopNameWithDomain =
                EjabdUtil.makeSureUserJid(QChatConstant.SEATSHOPPREFIX + String.valueOf(lSupplierId),QChatConstant.DEFAULT_HOST);
        // 获取对应session 的pid
        String pid = RedisUtil.hGet(14,RedisBizUtil.makeQCAdminKey(qunarName,
                shopNameWithDomain), RedisConstants.KEY_QCADMIN_PID);
        String groupType = RedisUtil.hGet(14,RedisBizUtil.makeQCAdminKey(qunarName,
                shopNameWithDomain), RedisConstants.KEY_QCADMIN_GROUPTYPE);
        if (null != selector) {
            ISeatSelectorEvents events = ApplicationContextHelper.popBean("seatSelectorBaseNoticeEvents", SeatSelectorBaseNoticeEvents.class);

            SelectorConfigration configration =
                    new SelectorConfigration.Builder()
                            .businessEnum(BusinessEnum.of(supplier.getbType()))
                            .busiSupplierId(supplier.getBusiSupplierId())
                            .groupType(groupType)
                            .productID(pid)
                            .qunarName(qunarName)
                            .events(events)
                            .build();

            SeatsResultVO<SeatWithStateVO> svo = selector.getOneSeat(configration);

            // 更新session表，如果相关session不存在或者已经关闭，进行创建
            String shopName = EjabdUtil.makeShopName(supplier.getId());

            Session session = RedisBizUtil.lastSession(qunarName,shopName);
            // 终结了的会话和转移之后的会话都重新路由
            if (null!=session &&
                    (SessionStateEnum.STATE_FINISHED.getState() == session.getSession_state()
                    || SessionStateEnum.STATE_TRANSED.getState() == session.getSession_state()
                    )){
                session = null;
            }

            SessionStateEnum sse = null;
            if (supplier.getBQueue() != 0) {
                // 开启排队
                if (null == svo) {
                    // 分不到人给排队
                    sse = SessionStateEnum.STATE_QUEUE;
                } else {
                    // 分到人了，看看是不是可用的人
                    if (OnlineState.OFFLINE != svo.getData().getOnlineState()) {
                        // 看是不是开启排队，且不超载
                        if (null != svo.getData().getSeat().getCurSessions()
                                && null != svo.getData().getSeat().getMaxSessions()
                                && OnlineState.OFFLINE != svo.getData().getOnlineState()
                                && svo.getData().getSeat().getCurSessions() >= svo.getData().getSeat().getMaxSessions()
                                 ) {
                            // 人员超载给排队
                            if (svo.getData().getSeat().getCurSessions().equals(svo.getData().getSeat().getMaxSessions())
                                && (session != null && ( session.getSession_state() == SessionStateEnum.STATE_STOPED.getState()
                                    || session.getSession_state().equals(SessionStateEnum.STATE_ASSIGNED.getState())) ) ) {
                                sse = SessionStateEnum.STATE_ASSIGNED;
                            }else{
                                if (session != null && (session.getSession_state().equals(SessionStateEnum.STATE_STOPED.getState())
                                        || session.getSession_state().equals(SessionStateEnum.STATE_ASSIGNED.getState()))){
                                    sse = SessionStateEnum.STATE_ASSIGNED;
                                }else {
                                    sse = SessionStateEnum.STATE_QUEUE;
                                }
                            }
                        } else {
                            if (null!=session
                                    //预分配处理?
                                    && SessionStateEnum.STATE_PREASSIGN.getState() != session.getSession_state()
                                    && SessionStateEnum.STATE_STOPED.getState() != session.getSession_state()
                                    && SessionStateEnum.STATE_ASSIGNED.getState() != session.getSession_state()){
                                sse = SessionStateEnum.STATE_QUEUE;
                            } else {
                                // 从断开过来的人，直接给溢出
                                // 正常给分配
                                sse = SessionStateEnum.STATE_ASSIGNED;
                            }

                        }
                    } else {
                        // 人员离线给排队

                        sse = SessionStateEnum.STATE_QUEUE;
                    }
                }
            } else {
                // 没有开启排队
                if (null == svo) {
                    sse = SessionStateEnum.STATE_ASSIGNED;
                } else {
                    // 看看是不是可用的人
                    if (OnlineState.ONLINE == svo.getData().getOnlineState()) {
                        sse = SessionStateEnum.STATE_ASSIGNED;
                    } else {
                        sse = SessionStateEnum.STATE_ASSIGNED;
                    }
                }
            }



            // 需要判断该店铺下是否开启了排队且全忙/是否可以分配到人/
            if (null == session) {
                //  新增一个session
                session = new Session();
                session.setUser_name(qunarName);
                session.setSession_state(sse.getState());
                session.setShop_name(shopName);
                if (null != svo) {
                    session.setSeat_name(svo.getData().getSeat().getQunarName());
                    session.setIsrobot_seat(svo.getData().getSeat().isIsrobot() ? 1 : 0);
                }
                if (pid ==null) {
                    pid = "";
                }
                session.setProduct_id(pid);
                session.setSession_id(String.valueOf(System.currentTimeMillis()));
                sessionDao.insertSession(session);
                RedisBizUtil.updateSession(session);

            } else {
                if (null != svo ){
                    if (session.getSeat_name().equals(svo.getData().getSeat().getQunarName()) && session.getSession_id() !=  null) {
                        session.setSession_state(sse.getState());
                        session.setSeat_name(svo.getData().getSeat().getQunarName());
                        session.setIsrobot_seat(svo.getData().getSeat().isIsrobot() ? 1 : 0);
                        sessionDao.updateSession(session);
                        RedisBizUtil.updateSession(session);
                    }else{

                        if (session.getSession_id() == null) {
                            session.setSession_id(String.valueOf(System.currentTimeMillis()));
                        }
                        if (!session.getSeat_name().equals(svo.getData().getSeat().getQunarName())){
                            sessionDao.closeInvaildSession(session,shopid,qunarName,pid);
                        }

                        String seatName = svo.getData().getSeat().getQunarName();
                        session.setSeat_name(seatName);
                        session.setSession_state(sse.getState());
                        sessionDao.updateSession(session);
                        RedisBizUtil.updateSession(session);
                    }
                }else{
                    session.setSession_state(sse.getState());
                    sessionDao.updateSession(session);
                    RedisBizUtil.updateSession(session);
                }
/*                session.setSession_state(sse.getState());
                if (null != svo) {
                    session.setSeat_name(svo.getData().getSeat().getQunarName());
                    session.setIsrobot_seat(svo.getData().getSeat().isIsrobot() ? 1 : 0);
                }
                sessionDao.updateSession(session);
                RedisBizUtil.updateSession(session);*/

        }


            // 回报率给各个业务线
            String url = Config.urlOfSessionBusiCallback(BusinessEnum.of(supplier.getbType()).getEnName());
            if (SessionStateEnum.STATE_ASSIGNED.getState() == session.getSession_state() && !Strings.isNullOrEmpty(url)){
                HttpClientUtils.newPostJson(url, JacksonUtil.obj2String(session));
            }

            if (null != svo){
                svo.getData().setSession(session);
            } else {
                logger.error("assignedOneSeat  selector select user fail");
            }

            return svo;
        } else {
            logger.error("AssignedOneSeat  selector init fail");
        }
        return null;
    }

    @Override
    public SeatsResultVO<SeatWithStateVO> preAssignedOneSeat(
            String busiSupplierId,
            BusinessEnum businessEnum,
            String qunarName,
            String pid,
            String host) {

        Supplier supplier = supplierDao.getSupplier(businessEnum.getId(), busiSupplierId, 0);
        if (null == supplier)
            return null;

        String hotline = shopService.selectHotlineByShopId(supplier.getId());
        supplier.setHotline(hotline);

        SeatWithStateVO seatWithStateVO = new SeatWithStateVO();
        DistributedInfo distributedInfo = siftStrategyService.siftCsr(pid, supplier.getId(), null, host, false);
        logger.info("preAssignedOneSeat DistributedInfo:{}", JacksonUtil.obj2String(distributedInfo));
        if (distributedInfo != null && distributedInfo.getCsr() != null) {
            Seat seat = csrTransformSeat(distributedInfo.getCsr(), supplier.getName(), qunarName, pid);
            seatWithStateVO.setSeat(seat);
            seatWithStateVO.setOnlineState(OnlineState.ONLINE);
        } else {
            seatWithStateVO.setOnlineState(OnlineState.OFFLINE);
        }
        String shopName = "shop_" + supplier.getId();
        supplier.setShopId(shopName);
        seatWithStateVO.setSupplier(supplier);
        seatWithStateVO.setSwitchOn(true);

        SeatsResultVO<SeatWithStateVO> resultVO = new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), seatWithStateVO);
        return resultVO;

//        SeatDefaultSelector selector = ApplicationContextHelper.popBean("seatDefaultSelector", SeatDefaultSelector.class);
//        if (null != selector) {
//            SelectorConfigration configration =
//                    new SelectorConfigration.Builder()
//                            .businessEnum(businessEnum)
//                            .busiSupplierId(busiSupplierId)
//                            .groupType(groupType)
//                            .productID(pid)
//                            .qunarName(qunarName)
//                            .build();
//
//            SeatsResultVO<SeatWithStateVO> svo = selector.getOneSeat(configration);
//            if (svo == null) {
//                return null;
//            }
//            // 更新session表，如果相关session不存在或者已经关闭，进行创建
//            String shopName = svo.getData().getSupplier().getShopId();
//
//
//            List<Session> sessionList = sessionDao.getSessionAndPid(qunarName, shopName, pid);
//            Session session = null;
//            if (!CollectionUtil.isEmpty(sessionList)) {
//                for (Session ss : sessionList) {
//                    if (SessionStateEnum.STATE_FINISHED != SessionStateEnum.valueOf(ss.getSession_state())) {
//                        session = ss;
//                        break;
//                    }
//                }
//            }
//
//
//            if (null == session) {
//
//                sessionV2Service.processClose(shopName,qunarName,pid);
//                //  新增一个session
//                Session newSession = new Session();
//                newSession.setUser_name(qunarName);
//                newSession.setSession_state(SessionStateEnum.STATE_PREASSIGN.getState());
//                newSession.setShop_name(svo.getData().getSupplier().getShopId());
//                newSession.setProduct_id(pid == null? "" :pid);
//                newSession.setSeat_name("");
//                newSession.setIsrobot_seat(0);
//                newSession.setSession_id(String.valueOf(System.currentTimeMillis()));
//                sessionDao.insertSession(newSession);
//                RedisBizUtil.updateSession(newSession);
//                svo.getData().setSession(newSession);
//                if (Strings.isNullOrEmpty(busi_session_id))
//                    busi_session_id = RedisBizUtil.makeBusiSessionId(newSession.getUser_name(),newSession.getShop_name(),newSession.getProduct_id());
//                sessionDao.insertBusiSessionMapping(busi_session_id,newSession.getSession_id());
//            } else {
//                sessionV2Service.processClose(shopName,qunarName,pid);
//
//                session.setSession_state(SessionStateEnum.STATE_PREASSIGN.getState());
//                session.setSeat_name("");
//                session.setIsrobot_seat(0);
//                sessionDao.updateSession(session);
//                RedisBizUtil.updateSession(session);
//                svo.getData().setSession(session);
//            }
//
//            // 更新 busi_session_id 的与session的对应关系
//            return svo;
//        } else {
//            logger.error("preAssignedOneSeat  selector init fail");
//        }
//        return null;
    }

    @Override
    public List<SeatWithStateVO> onlineSeats(String busiSupplierId, BusinessEnum businessEnum, String pid, String host) {
        List<SeatWithStateVO> seatWithStateVOS = new ArrayList<>();

        Supplier supplier = supplierDao.getSupplier(businessEnum.getId(), busiSupplierId, 0);
        if (null == supplier)
            return null;

        String shopName = "shop_" + supplier.getId();
        supplier.setShopId(shopName);
        List<CSR> onlineCsrs = siftStrategyService.getOnlineCsrs(pid, supplier.getId(), host,false, false);
        if (CollectionUtils.isEmpty(onlineCsrs)) {
            SeatWithStateVO seatWithStateVO = new SeatWithStateVO();
            seatWithStateVO.setOnlineState(OnlineState.OFFLINE);
            seatWithStateVO.setSupplier(supplier);
            seatWithStateVO.setSwitchOn(true);
            seatWithStateVOS.add(seatWithStateVO);
            return seatWithStateVOS;
        }

        List<Seat> seatList = onlineCsrs.stream().map(csr -> csrTransformSeat(csr, supplier.getName(), null, pid)).collect(Collectors.toList());
        for (Seat seat : seatList) {
            SeatWithStateVO seatWithStateVO = new SeatWithStateVO();
            seatWithStateVO.setSeat(seat);
            seatWithStateVO.setOnlineState(OnlineState.ONLINE);
            seatWithStateVO.setSupplier(supplier);
            seatWithStateVO.setSwitchOn(true);
            seatWithStateVOS.add(seatWithStateVO);
        }

        return seatWithStateVOS;
    }

    @Override
    public JsonData redistributionEx(long shopId, JID userQName, String pdtId, String seatQName, String host) {
        logger.debug("judgmentOrRedistribution, {} - {} - {}", userQName, shopId, pdtId);
        Supplier supplier = supplierDao.getSupplier(0, null, shopId);

        if (supplier == null || BusinessEnum.of(supplier.getbType()) == null) {
            return JsonData.error("业务线错误");
        }

        Robot robot = robotService.getRobotByBusiness(BusinessEnum.of(supplier.getbType()));
        QueueMapping queueMapping = queueMappingDao.selectByCustomerNameAndShopId(userQName.toBareJID(), shopId);
        if (queueMapping == null || robot == null || !queueMapping.getSeatName().startsWith(robot.getRobotId())) {
            return JsonData.error("当前坐席不是机器人");
        }
//
//        if (robot != null && !robot.getRobotId().equalsIgnoreCase(seatQName)) {
//            return JsonData.error("当前坐席不是机器人");
//        }

        QtSessionItem sessionItem = QtQueueManager.getInstance().judgmentOrRedistribution(userQName, shopId, pdtId, host,true, true);

        CSR csr = null;
        SeatWithStateVO seatWithStateVO = new SeatWithStateVO();
        if (sessionItem != null && sessionItem.getSeatId() > 0) {
            logger.info("seatid is {}", sessionItem.getSeatId());
            List<CSR> scrs = SpringComponents.components.csrService.queryCsrsByCsrIDs(Arrays.asList(sessionItem.getSeatId()));
            if (CollectionUtils.isNotEmpty(scrs)) {
                csr = scrs.get(0);
            } else {
                logger.info("seatid is {}, and scrs is empty", sessionItem.getSeatId());
            }
        }
        String shopName = "shop_" + supplier.getId();
        supplier.setShopId(shopName);
        seatWithStateVO.setSupplier(supplier);
        if (csr != null) {

            Seat seat = csrTransformSeat(csr, String.valueOf(shopId), userQName.toBareJID(), pdtId);
            seatWithStateVO.setSeat(seat);
            seatWithStateVO.setOnlineState(OnlineState.ONLINE);
            seatWithStateVO.setSwitchOn(true);

            String showName = StringUtils.isNotEmpty(seat.getWebName()) ? seat.getWebName()
                    : (StringUtils.isNotEmpty(seat.getNickName()) ? seat.getNickName() : JID.parseAsJID(seat.getQunarName()).getNode());

            sendSeatMsg(shopName, userQName, robot.getRobotId(), seat.getQunarName(), host, showName);
        } else {
            String robotId = SendMessage.appendQCDomain(robot.getRobotId(), host);
            shopName = SendMessage.appendQCDomain(shopName, host);
            String toUserMsg = "当前店铺没有可服务客服，请稍后再试";
            ConsultUtils.sendMessage(JID.parseAsJID(shopName), userQName, JID.parseAsJID(robotId), userQName, toUserMsg, false, false, true);

            if (sessionItem != null)
                logger.info("sessionItem id = {}", sessionItem.getSeatId());
            else
                logger.info("sessionItem is null");
            return JsonData.error("没有分配到客服，请稍候再试");
        }
        return JsonData.success(seatWithStateVO);
    }

    private void sendSeatMsg(String shopName, JID userQName, String robotId, String newSeat, String domain, String showName) {
        String url = Config.QCHAT_HISTORY_MSG_SEARCH_URL;
        shopName = SendMessage.appendQCDomain(shopName, domain);
        newSeat = SendMessage.appendQCDomain(newSeat, domain);
        robotId = SendMessage.appendQCDomain(robotId, domain);
        JID shop = JID.parseAsJID(shopName);
        JID newSeatJid = JID.parseAsJID(newSeat);

        CacheHelper.set(CacheHelper.CacheType.SeatCache, userQName.toBareJID() + "_" + shop.toBareJID(), newSeatJid.toBareJID(), 1, TimeUnit.DAYS);

        url = String.format(url, shopName, userQName.getNode(), robotId, System.currentTimeMillis());
        String toNewCsrMsg = "用户已转人工，<a href='%s'>查看机器人历史消息</a>。";
        toNewCsrMsg = String.format(toNewCsrMsg, url);
        // 通知转人工客服
        ConsultUtils.sendMessage(shop, newSeatJid, userQName, newSeatJid, toNewCsrMsg, true, false, true);

        String toUserMsg = "您已转人工，客服 %s 为您服务。";
        toUserMsg = String.format(toUserMsg, showName);
        ConsultUtils.sendMessage(shop, userQName, newSeatJid, userQName, toUserMsg, false, false, true);
    }


    private Seat csrTransformSeat(CSR csr, String supplierName, String qunarName, String pid) {
        String csrName = csr.getQunarName().getNode();
        Seat seat = new Seat();
        seat.setId(csr.getId());
        seat.setQunarName(csrName);
        seat.setWebName(csr.getWebName());
        seat.setSupplierId(csr.getSupplierID());
        seat.setFaceLink(csr.getFaceLink());
        seat.setNickName(csr.getNickName());
        seat.setPriority(csr.getPriority());
        seat.setMaxSessions(csr.getMaxServiceCount());
        seat.setServiceStatus(csr.getServiceStatus());
        if (csrName.endsWith("_robot"))
            seat.setIsrobot(true);
        else
            seat.setIsrobot(false);
        seat.setCreateTime(csr.getCreateTime());
        seat.setSupplierName(supplierName);
        seat.setCustomerName(qunarName);
        seat.setPid(pid);
        seat.setBindWx(csr.getBindWx());
        seat.setHost(csr.getHost());
        return seat;
    }

    @Override
    public JsonData updateWxStatus(String csrName, Integer bindWx) {
        List<Seat> seatDB = seatDao.getSeatListByQunarName(csrName);

        if (CollectionUtils.isEmpty(seatDB) || BindWxStatus.of(bindWx) == null) {
            logger.warn("csrName:{}  or bindWx:{} not exist", csrName, bindWx);
            return JsonData.error("待更新客服不存在", 500);
        }
        seatDao.updateSeatWxStatus(csrName, bindWx);
        LogUtil.doLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_SEAT, 0, csrName, SessionUtils.getUserName(),
                "update wxBind before:" + seatDB.get(0).getBindWx() + " after:" + bindWx);
        return JsonData.success("success");
    }

}
