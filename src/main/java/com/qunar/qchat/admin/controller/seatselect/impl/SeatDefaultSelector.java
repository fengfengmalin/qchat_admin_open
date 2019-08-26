package com.qunar.qchat.admin.controller.seatselect.impl;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.ConfigConstants;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.controller.seatselect.ISeatSelector;
import com.qunar.qchat.admin.controller.seatselect.SeatSelectorOperatorCode;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.dao.ISeatDao;
import com.qunar.qchat.admin.dao.ISeatGroupDao;
import com.qunar.qchat.admin.dao.ISupplierDao;
import com.qunar.qchat.admin.dao.msg.IMsgDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.plugins.chatplugin.BaseChatPlugin;
import com.qunar.qchat.admin.plugins.chatplugin.ChatPluginInstance;
import com.qunar.qchat.admin.plugins.chatplugin.IChatPlugin;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.impl.OnlineSeatSortFactory;
import com.qunar.qchat.admin.service.sortstrategy.ASeatSortStrategy;
import com.qunar.qchat.admin.service.util.SeatUtil;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.GroupAndSeatVO;
import com.qunar.qchat.admin.vo.SeatOnlineState;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SeatsResultVO;
import com.qunar.qtalk.ss.sift.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "seatDefaultSelector")
public class SeatDefaultSelector implements ISeatSelector {
    private static final Logger logger = LoggerFactory.getLogger(SeatDefaultSelector.class);
    @Resource
    protected IMsgDao msgDao;
    @Resource
    protected ISeatDao seatDao;
    @Resource
    protected ISupplierDao supplierDao;
    @Resource
    protected IRobotService robotService;
    @Resource
    protected ISeatGroupDao seatGroupDao;

    @Resource(name = "onlineSeatSortFactory")
    protected OnlineSeatSortFactory seatSortFactory;

    @Autowired
    ShopService shopService;
/*
    // 分配参数
    protected SelectorConfigration d;

    // 中间值
    private List<SeatAndGroup> allSeat;
    private Supplier supplier;
    */

    public SeatDefaultSelector() {

    }


    @Override
    public SeatsResultVO<SeatWithStateVO> getOneSeat(SelectorConfigration d) {
       /* this.d = d;
        if (this.d == null)
            return null;*/
        if (d == null)
            return null;

        SeatInfoUtil.setSelectorConfigragtion(d);

        return getOneSeatSkeleton();
    }

    /**
     * 在这个状态之上的进行分配，这样可以由子类定制分配级别，默认离线不分配，在线／忙碌／离开都分配
     *
     * @return 最低的过滤级别（不包括参数）
     */
    protected OnlineState getSelectFilterLevelAbove() {
        return OnlineState.AWAY;
    }


    /**
     * 主流程，是个主干流程，其中很多挂载点，可以进行扩展定制
     *
     * @return 分配之后的座席
     */
    private SeatsResultVO<SeatWithStateVO> getOneSeatSkeleton() {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        SeatsResultVO<SeatWithStateVO> resultVO = null;
        if (d == null){
            return resultVO;
        }

        if (null == d.getBusinessEnum())
            d.setBusinessEnum(BusinessEnum.EMPTY);
        if (Strings.isNullOrEmpty(d.getBusiSupplierId()))
            d.setBusiSupplierId("");

        // 先查出店铺的信息
        //if (null == supplier)
        Supplier supplier = supplierDao.getSupplier(d.getBusinessEnum().getId(), d.getBusiSupplierId(), d.getSupplierId());
        SeatInfoUtil.setSupplier(supplier);

        d.setSupplierId(supplier.getId());
        d.setBusinessEnum(BusinessEnum.of(supplier.getbType()));
        d.setBusiSupplierId(supplier.getBusiSupplierId());
        // 获取全部的座席id,优先获取座席是因为，在会话保持的时候，需要根据座席的集合去做sub,在
        SeatInfoUtil.setSelectorConfigragtion(d);

        List<SeatAndGroup> allSeat = getAllSeat();
        SeatInfoUtil.setAllSeat(allSeat);
        if (null == allSeat || allSeat.isEmpty()) {
            // 查不到客服尝试分配机器人
            resultVO = getPostRobotSeat();
            if (null != resultVO) {
                logOperation(SeatSelectorOperatorCode.POSTROBOTSELECT, resultVO);
                if (null != d.getEvents())
                    d.getEvents().onPostRobotSelect(d,resultVO.getData());
                return resultVO;
            } else {
                logOperation(SeatSelectorOperatorCode.NOTSEAT, null);
                if (null != d.getEvents()) {
                    d.getEvents().onRealSeatNotSelect(d);
/*
                    if (null!=supplier && 0 != supplier.getBQueue()){
                        // d.getEvents().onQueue(d,null);
                    } else {
                        d.getEvents().onLeaveMessage(d);
                    }*/
                }
                return null;
            }
        }



        // 遍历所有的客服id集合
        List<String> seatIds = Lists.transform(allSeat, new Function<SeatAndGroup, String>() {
            @Override
            public String apply(SeatAndGroup seatWithStateVO) {
                if (null != seatWithStateVO) {
                    return seatWithStateVO.getQunarName();
                }
                return "";
            }
        });

        // 获取到所有人的状态
        Map<String, OnlineState> onlineStateMap = getSeatOnlineState(seatIds);


        // 将状态merge到客服列表
        List<GroupAndSeatVO> gsList = seatSortFactory.getSeatsWithOnlineStateList(allSeat, onlineStateMap);
        if (null == gsList || gsList.isEmpty()) {
            logger.error("gsList iserror qunarname - {} ", d.getQunarName());
            logOperation(SeatSelectorOperatorCode.SEATCOLLECTEMPTY, null);
            if (null != d.getEvents()) {
                d.getEvents().onRealSeatNotSelect(d);

           /*     if (null!=supplier && 0 != supplier.getBQueue()){
                    //   d.getEvents().onQueue(d,null);
                } else {
                    d.getEvents().onLeaveMessage(d);
                }*/
            }
            return null;
        }
        // 根据产品id过滤一下客服组呗
        if (StringUtils.isNotEmpty(d.getProductID())) {
            List<Long> groupByProductId = getProductBindGroup();
            if (null != groupByProductId && !groupByProductId.isEmpty())
                gsList = filterByGroupIds(gsList, groupByProductId);
        }

        if (CollectionUtil.isEmpty(gsList)) {
            logger.error("gsList iserror qunarname - {},productid 0 {} ", d.getQunarName(), d.getProductID());
            logOperation(SeatSelectorOperatorCode.SEATCOLLECTEMPTY, String.format("product filter error"));
            if (null != d.getEvents()) {
                d.getEvents().onRealSeatNotSelect(d);

           /*     if (null!=supplier && 0 != supplier.getBQueue()){
                    //d.getEvents().onQueue(d,null);
                } else {
                    d.getEvents().onLeaveMessage(d);
                }*/
            }
            return null;
        }

        // 过滤组之后 所有客服按照在线状态&上次会话时间排序
        List<SeatWithStateVO> ssList = sumAllGroupSeatVO(gsList);
        // 重新标记客服的在线状态
        for (SeatWithStateVO vo : ssList) {
            // 被修正的online status 业务线可能会判定这个字段的意思。将原始的在线状态保存在 座席状态信息里，和服务模式放一起，有益于排查问题
            vo.getSeat().setOnlineState(vo.getOnlineState());
            if (!RobotConfig.ALLSEAT_OFFLINE_BYFORCE) {
                if (ServiceStatusEnum.SUPER_MODE.getKey() == vo.getSeat().getServiceStatus())
                    vo.setOnlineState(OnlineState.ONLINE);
                if (ServiceStatusEnum.DND_MODE.getKey() == vo.getSeat().getServiceStatus())
                    vo.setOnlineState(OnlineState.OFFLINE);
            }
        }


        // 人前机器人分配
        SeatsResultVO<SeatWithStateVO> preRobotSeat = getPreRobotSeat();
        // 人前机器人逻辑
        if (null != preRobotSeat) {
            resultVO = processOnPreRobotSelect(ssList, preRobotSeat);
        } else {
            // 重新排序座席顺序，而且遵循客服的在线状态，与服务状态
            Map<OnlineState, SeatWithStateVO> qosSeatList = sortSeatVOList(ssList);
            // 分人
            if (supplier.getBQueue() == 0) {
                resultVO = processOnRealSeatSelect(ssList, qosSeatList);
            }else{
                resultVO = processOnSessionRealSeatSelect(ssList,qosSeatList);
            }
            if (null == resultVO) {
                // 人为空，看看人后机器人
                resultVO = processOnPostRobotSelect(qosSeatList);
            }
        }

        if (null != d.getEvents()) {

            if (supplier.getBQueue() != 0) {
                // 开启排队
            /*    if (null == resultVO) {
                    // 分不到人给排队
                    d.getEvents().onQueue(d,resultVO.getData());
                } else {
                    // 分到人了，看看是不是可用的人
                    if (OnlineState.ONLINE == resultVO.getData().getOnlineState()) {
                        // 看是不是开启排队，且不超载
                        if (null != resultVO.getData().getSeat().getCurSessions()
                                && null != resultVO.getData().getSeat().getMaxSessions()
                                && OnlineState.ONLINE == resultVO.getData().getOnlineState()
                                && resultVO.getData().getSeat().getCurSessions() >= resultVO.getData().getSeat().getMaxSessions()
                                ) {
                            // 人员超载给排队
                                    if (resultVO.getData().getSeat().getCurSessions() == resultVO.getData().getSeat().getMaxSessions()){
                                        Session session = RedisBizUtil.lastSession(d.getQunarName(),supplier.getShopId());
                                        if (session != null && session.getSession_state() != SessionStateEnum.STATE_ASSIGNED.getState()
                                                && session.getSession_state() != SessionStateEnum.STATE_STOPED.getState()){
                                            d.getEvents().onQueue(d, resultVO.getData());
                                        }
                                    }else {
                                        d.getEvents().onQueue(d, resultVO.getData());
                                    }
                        }
                    } else {
                        // 人员离线给排队
                        d.getEvents().onQueue(d,resultVO.getData());
                    }
                }*/
            } else {
                // 没有开启排队
      /*          if (null == resultVO) {
                    d.getEvents().onLeaveMessage(d);
                } else {
                    // 看看是不是可用的人
                    if (OnlineState.ONLINE != resultVO.getData().getOnlineState()) {
                        d.getEvents().onLeaveMessage(d);
                    }
                }*/
            }
        }

        return resultVO;


    }

    protected SeatsResultVO<SeatWithStateVO> processOnPreRobotSelect(List<SeatWithStateVO> ssList, SeatsResultVO<SeatWithStateVO> robotseat) {

        // 如果配置了机器人那么判定一下需不需要会话保持逻辑
        SeatsResultVO<SeatWithStateVO> lastSeat = getLastRealSeatQName(ssList, RobotConfig.ROBOT_ALLOCATION_INTERVAL_TIME_MIN);

        if (null != lastSeat
                && null != lastSeat.getData()
                && SeatUtil.isSeatServiceable(lastSeat.getData(), lastSeat.getData().getOnlineState())) {
            // 保持座席成功
            logOperation(SeatSelectorOperatorCode.CONVESATIONHOLDSECCESS, lastSeat);
            return lastSeat;
        }
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        // 返回机器人
        if (null != d.getEvents()) {
            d.getEvents().onPreRobotSelectd(d, null == lastSeat ? null : lastSeat.getData(), robotseat.getData());
            SeatInfoUtil.setSelectorConfigragtion(d);
        }
        logOperation(SeatSelectorOperatorCode.PREROBOTSELECTD, lastSeat);
        return robotseat;
    }

    protected SeatsResultVO<SeatWithStateVO> processOnRealSeatSelect(List<SeatWithStateVO> ssList, Map<OnlineState, SeatWithStateVO> qosSeatList) {
        // 没有分派到机器人，进行座席分配，同样会话保持一下，这次获取的时间要更大谢
        //session 修改暂未想好
        //获取lastseat后判断session连接数量，>maxSession，并需要判断坐席在线状态，还需要修正session状态
        SeatsResultVO<SeatWithStateVO> lastRealSeat = getLastRealSeatQName(ssList, -1);
        //   int bQueue = supplier == null ? 0 : supplier.getBQueue();
        int bQueue = 0;
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        if (null != lastRealSeat) {
            SeatsResultVO<SeatWithStateVO> fixSeat = filterPriorityByOnlineStatus(bQueue,
                    qosSeatList, OnlineState.getOnlineStatePriority(lastRealSeat.getData().getOnlineState()));
            if (null != fixSeat) {
                // 保持座席失败，分配了哥新的客服
                if (null != d.getEvents())
                    d.getEvents().onRealChanged(d,lastRealSeat.getData(), fixSeat.getData());
                logOperation(SeatSelectorOperatorCode.NEWREALSEATSELECT, lastRealSeat);
                return fixSeat;
            } else {
                if (OnlineState.compare(lastRealSeat.getData().getOnlineState(), getSelectFilterLevelAbove()) > 0) {
                    // 保持会话成功，继续为你服务
                    logOperation(SeatSelectorOperatorCode.CONVESATIONHOLDSECCESS, lastRealSeat);
                    if (null != d.getEvents()) {
                        d.getEvents().onConversationHolded(d, lastRealSeat.getData());
                        SeatInfoUtil.setSelectorConfigragtion(d);
                    }
                    return lastRealSeat;
                } else {
                    return null;
                }
            }
        } else {

            SeatsResultVO<SeatWithStateVO> fixSeat = filterPriorityByOnlineStatus(bQueue,
                    qosSeatList, OnlineState.getOnlineStatePriority(getSelectFilterLevelAbove()));
            if (null != fixSeat) {
                // 新分配了个客服
                if (null != d.getEvents()) {
                    d.getEvents().onRealSeatSelect(d, fixSeat.getData());
                    SeatInfoUtil.setSelectorConfigragtion(d);                }

                    logOperation(SeatSelectorOperatorCode.REALSEATSELECTED, fixSeat);
                return fixSeat;
            }
        }
        return null;
    }


    protected SeatsResultVO<SeatWithStateVO> processOnSessionRealSeatSelect(List<SeatWithStateVO> ssList, Map<OnlineState, SeatWithStateVO> qosSeatList) {
        // 没有分派到机器人，进行座席分配，同样会话保持一下，这次获取的时间要更大谢
        //session 修改暂未想好
        //获取lastseat后判断session连接数量，>maxSession，并需要判断坐席在线状态，还需要修正session状态
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        SeatsResultVO<SeatWithStateVO> lastRealSeat = getLastRealSeatQName(ssList, -1);

        if (null != lastRealSeat) {

            SeatsResultVO<SeatWithStateVO> fixSeat = filterPriorityByOnlineStatusAndSession(
                    qosSeatList,lastRealSeat );
            if (null != fixSeat) {
                // 保持座席失败，分配了哥新的客服
                if (null != d.getEvents()) {
                    d.getEvents().onRealChanged(d, lastRealSeat.getData(), fixSeat.getData());
                    SeatInfoUtil.setSelectorConfigragtion(d);
                }
                logOperation(SeatSelectorOperatorCode.NEWREALSEATSELECT, lastRealSeat);
                return fixSeat;
            } else {
                if (OnlineState.compare(lastRealSeat.getData().getOnlineState(), getSelectFilterLevelAbove()) > 0 &&
                        lastRealSeat.getData().getSeat().getServiceStatus() != ServiceStatusEnum.DND_MODE.getKey()) {
                    // 保持会话成功，继续为你服务
                    logOperation(SeatSelectorOperatorCode.CONVESATIONHOLDSECCESS, lastRealSeat);
                    if (null != d.getEvents()) {
                        d.getEvents().onConversationHolded(d, lastRealSeat.getData());
                        SeatInfoUtil.setSelectorConfigragtion(d);
                    }
                    return lastRealSeat;
                } else {
                    return null;
                }
            }
        } else {

            SeatsResultVO<SeatWithStateVO> fixSeat = filterPriorityByOnlineStatusAndSession(
                    qosSeatList, null);
            if (null != fixSeat) {
                // 新分配了个客服
                if (null != d.getEvents()) {
                    d.getEvents().onRealSeatSelect(d, fixSeat.getData());
                    SeatInfoUtil.setSelectorConfigragtion(d);
                }
                logOperation(SeatSelectorOperatorCode.REALSEATSELECTED, fixSeat);
                return fixSeat;
            }
        }
        return null;
    }

    protected SeatsResultVO<SeatWithStateVO> processOnPostRobotSelect(Map<OnlineState, SeatWithStateVO> qosSeatList) {
        // 没有客服可分，看看有没有事后机器人支持
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        SeatsResultVO<SeatWithStateVO> postRobotSeat = getPostRobotSeat();
        if (null != postRobotSeat) {
            logOperation(SeatSelectorOperatorCode.POSTROBOTSELECT, postRobotSeat);
            if (null != d.getEvents()) {
                d.getEvents().onPostRobotSelect(d, postRobotSeat.getData());
             //   SeatInfoUtil.setSelectorConfigragtion(d);
            }
            return postRobotSeat;
        } else {
            if (null != d.getEvents()) {
                d.getEvents().onRealSeatNotSelect(d);
             //   SeatInfoUtil.setSelectorConfigragtion(d);

            }
            logOperation(SeatSelectorOperatorCode.NOREALSEATSELECTABLE, qosSeatList, getSelectFilterLevelAbove());
            return null;
        }
    }

    /**
     * 获取改店铺下所有的客服信息
     * 如果是C2B 业务线需要区分A，B岗客服
     *
     * @return 所有座席
     */
    protected List<SeatAndGroup> getAllSeat() {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        if (null == d.getBusinessEnum()) {
            logOperation(SeatSelectorOperatorCode.BUSINOTEXIST, null);
            return null;
        }

        List<SeatAndGroup> seatAndGroups;
        if (Strings.isNullOrEmpty(d.getGroupType())) {
            // 一键分配不考虑用户组的情况
            logger.info("getSingleSeatWithOnlineState中 ! groupType isNullOrEmpty");
            seatAndGroups = seatDao.getAllSeatsWithGroup(d.getBusiSupplierId(), d.getBusinessEnum().getId());
        } else {
            logger.info("getSingleSeatWithOnlineState中  groupType is not empty or NULL");
            seatAndGroups = seatDao.getSeatsWithGroupType(d.getBusiSupplierId(), d.getBusinessEnum().getId(), d.getGroupType());
        }
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            logger.info("getSingleSeatWithOnlineState中 ! collectionUtil is empty");
            return null;
        }

        return seatAndGroups;
    }

    /**
     * 获取在线状态
     *
     * @param strIdList 目标ID列表
     * @return ID 和与其对应的在线状态
     */
    protected Map<String, OnlineState> getSeatOnlineState(List<String> strIdList) {
        if (CollectionUtil.isEmpty(strIdList)) {
            return null;
        }

        if (RobotConfig.ALLSEAT_OFFLINE_BYFORCE)
            return null;

        // qunarNameList 可能包含各种域名的人
        // 将他们根据域名区分，没有域名的按照 qchat处理
        List<SeatOnlineState> stateList = Lists.newArrayList();
        Map<String, List<String>> domainedUsers = EjabdUtil.spliteUsersByDomain(strIdList);
        for (String key : domainedUsers.keySet()) {
            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(key);
            if (null != plugin) {
                List<SeatOnlineState> sublist = plugin.getUsersOnlineStatus(domainedUsers.get(key));
                if (null != sublist) {
                    stateList.removeAll(sublist);
                    stateList.addAll(sublist);
                }
            }
        }

        if (CollectionUtils.isEmpty(stateList)) {
            return null;
        }

        return CollectionUtil.uniqueIndex(stateList,
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


    private List<Long> getProductBindGroup() {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        List<Long> gsList = null;
        if (StringUtils.isEmpty(d.getProductID()))
            return null;

        boolean isFilter = Boolean.parseBoolean(Config.getPropertyInQConfig(ConfigConstants.FILTER_GROUP_BY_PRODUCT_SWITCH, "true"));
        if (isFilter) {
            List<SeatAndGroup> sgList = seatGroupDao.getSeatAndGroupListByPid(d.getProductID());
            gsList = Lists.transform(sgList, new Function<SeatAndGroup, Long>() {
                @Override
                public Long apply(SeatAndGroup input) {
                    return input.getGroupId();
                }
            });
            return gsList;
        }
        return gsList;
    }

    private List<GroupAndSeatVO> filterByGroupIds(List<GroupAndSeatVO> gasList, List<Long> gropids) {

        if (null == gropids)
            return gasList;

        List<GroupAndSeatVO> groupAndSeatVOList = new ArrayList<>();
        for (GroupAndSeatVO groupAndSeatVO : gasList) {
            if (gropids.contains(groupAndSeatVO.getGroupId())) {
                groupAndSeatVOList.add(groupAndSeatVO);
            }
        }
        return groupAndSeatVOList;
    }

    protected Map<OnlineState, SeatWithStateVO> sortSeatVOList(List<SeatWithStateVO> seatList) {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();

        ASeatSortStrategy aSeatSortStrategy = seatSortFactory.getASeatSortStrategy(SeatSortStrategyEnum.POLLING_STRATEGY);
        Supplier supplier = SeatInfoUtil.getSupplier();
        if (null == supplier)
            supplier = supplierDao.getSupplier(d.getBusinessEnum().getId(), d.getBusiSupplierId(), d.getSupplierId());
        if (null != supplier && supplier.getBQueue() != 0) {
            aSeatSortStrategy = seatSortFactory.getASeatSortStrategy(SeatSortStrategyEnum.QUEUE_STRATEGY);

        }

        seatList = aSeatSortStrategy.sortSeatVOList(seatList);

        Map<OnlineState, SeatWithStateVO> sList = new HashMap<>();
        for (SeatWithStateVO ssVO : seatList) {
            OnlineState s = ssVO.getOnlineState();
            if (!sList.containsKey(s)) {
                sList.put(s, ssVO);
            }
        }
        return sList;
    }

    private List<SeatWithStateVO> sumAllGroupSeatVO(List<GroupAndSeatVO> groupList) {
        List<SeatWithStateVO> ssList = Lists.newArrayList();
        if (null != groupList) {
            for (GroupAndSeatVO groupAndSeatVO : groupList) {
                ssList.addAll(groupAndSeatVO.getSeatWithStateVOList());
            }
        }
        return ssList;
    }

    protected SeatsResultVO<SeatWithStateVO> getPreRobotSeat() {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        Supplier supplier = SeatInfoUtil.getSupplier();

        if (d.getBusinessEnum() == null || d.getBusinessEnum() == BusinessEnum.EMPTY) {
            return null;
        }

        if (null == supplier)
            supplier = supplierDao.getSupplier(d.getBusinessEnum().getId(), d.getBusiSupplierId(), d.getSupplierId());

        if (null == supplier) {
            logger.info(" getPreRobotSeat can not find supplier businessEnum：{}, busiSupplierId:{}",
                    d.getBusinessEnum().getEnName(), d.getBusinessEnum());
            return null;
        }



        supplier.setShopId(Supplier.SHOPID_PREFIX + supplier.getId());
        supplier.setBusiSupplierId(d.getBusiSupplierId());
        supplier.setBusiName(d.getBusinessEnum().getEnName());

        if (!RobotConfig.robotEnabel(d.getBusinessEnum().getEnName(), supplier.getId())) {
            logger.info(" getPreRobotSeat robot is not enable not support businessEnum:{} SupplierId：{}",
                    d.getBusinessEnum().getEnName(), supplier.getId());
            return null;
        }

        Robot robot = robotService.getRobotByBusiness(d.getBusinessEnum());
        if (robot == null) {
            logger.info(" getPreRobotSeat robot is not find businessEnum：{}",
                    d.getBusinessEnum().getId());
            return null;
        }

        // 获取这个机器人的生效策略
        SupplierWithRobot supplierWithRobot = robotService.getRobotConfig(robot.getRobotId(), supplier.getId());

        RobotStrategyEnum robotStrategyEnum = null == supplierWithRobot ?
                RobotStrategyEnum.RSE_DEFAULT :
                RobotStrategyEnum.of(supplierWithRobot.getStrategy());
        if (RobotStrategyEnum.RSE_ROBOT_ADVANCED.getValue() == robotStrategyEnum.getValue()) {
            // 返回机器人id
            SeatWithStateVO robotSeat = new SeatWithStateVO();
            robotSeat.setOnlineState(OnlineState.ONLINE);
            robotSeat.setSeat(robot.toSeat(supplier.getId()));
            robotSeat.setSwitchOn(true);
            robotSeat.setSupplier(supplier);
            return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(), robotSeat);
        }
        logger.info(" getPreRobotSeat robot work style not match：{},robot : {}",
                robotStrategyEnum.getValue(), robot.getRobotId());
        return null;
    }

    protected SeatsResultVO<SeatWithStateVO> getPostRobotSeat() {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        Supplier supplier = SeatInfoUtil.getSupplier();

        if (d.getBusinessEnum() == null || d.getBusinessEnum() == BusinessEnum.EMPTY) {
            return null;
        }

        if (null == supplier)
            supplier = supplierDao.getSupplier(d.getBusinessEnum().getId(), d.getBusiSupplierId(), d.getSupplierId());

        if (null == supplier)
            return null;



        supplier.setShopId(Supplier.SHOPID_PREFIX + supplier.getId());

        if (!RobotConfig.robotEnabel(d.getBusinessEnum().getEnName(), supplier.getId())) {
            return null;
        }

        Robot robot = robotService.getRobotByBusiness(d.getBusinessEnum());
        if (robot == null) {
            return null;
        }

        // 获取这个机器人的生效策略
        SupplierWithRobot supplierWithRobot = robotService.getRobotConfig(robot.getRobotId(), supplier.getId());

        RobotStrategyEnum robotStrategyEnum = null == supplierWithRobot ?
                RobotStrategyEnum.RSE_DEFAULT :
                RobotStrategyEnum.of(supplierWithRobot.getStrategy());
        if (RobotStrategyEnum.RSE_SEAT_ADVANCED.getValue() == robotStrategyEnum.getValue()) {
            // 返回机器人id
            SeatWithStateVO robotSeat = new SeatWithStateVO();
            robotSeat.setOnlineState(OnlineState.ONLINE);
            robotSeat.setSeat(robot.toSeat(supplier.getId()));
            robotSeat.setSwitchOn(true);
            robotSeat.setSupplier(supplier);
            return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(), robotSeat);
        }
        return null;
    }

    protected SeatsResultVO<SeatWithStateVO> getLastRealSeatQName(List<SeatWithStateVO> ssList, final int sinceTimeByMins) {
        String lastSeatQunarName = null;
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        if (d.getBusinessEnum() == null || d.getBusinessEnum() == BusinessEnum.EMPTY) {
            return null;
        }

        List<String> shopIds = Lists.transform(ssList, new Function<SeatWithStateVO, String>() {
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
        });

        List<String> seatNames = Lists.transform(ssList, new Function<SeatWithStateVO, String>() {
            @Override
            public String apply(SeatWithStateVO seatWithStateVO) {
                return seatWithStateVO.getSeat().getQunarName();
            }
        });
        if (!StringUtils.isEmpty(d.getQunarName()) && !shopIds.isEmpty() && !seatNames.isEmpty()) {
            String domain = EjabdUtil.getUserDomain(d.getQunarName(), QChatConstant.DEFAULT_HOST);
            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(domain);
            if (null != plugin && plugin instanceof BaseChatPlugin) {
                ((BaseChatPlugin) plugin).setMsgDao(msgDao);
                lastSeatQunarName = plugin.getLastChatSeat(d.getQunarName(), shopIds, seatNames, sinceTimeByMins);
            }
        }


        if (StringUtils.isNotEmpty(lastSeatQunarName)) {
            SeatWithStateVO lastSeatVO = null;
            for (SeatWithStateVO ssVO : ssList) {
                if (ssVO.getSeat().getQunarName().equals(lastSeatQunarName)) {
                    lastSeatVO = ssVO;
                    break;
                }
            }
            return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(), lastSeatVO);
        }
        return null;
    }

    protected SeatsResultVO<SeatWithStateVO> filterPriorityByOnlineStatus(int bQueue,
                                                                          Map<OnlineState, SeatWithStateVO> qosSeatLists,
                                                                          int lastqos) {
        /*
        if (bQueue != 0 && lastqos > 1){
            lastqos = 4;
        }*/
        logger.debug("bQueue:{}", bQueue);
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();

        if (null != qosSeatLists) {
            for (int qos = OnlineState.maxPriority; qos > 0; qos--) {
                if (qos > lastqos) {
                    OnlineState onlineState = OnlineState.of(qos);
                    if (null != onlineState && qosSeatLists.containsKey(onlineState)) {
                        return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(), qosSeatLists.get(onlineState));
                    }
                    continue;
                }
                break;
            }
        }
        return null;
    }

    protected SeatsResultVO<SeatWithStateVO> filterPriorityByOnlineStatusAndSession(
            Map<OnlineState, SeatWithStateVO> qosSeatLists,
            SeatsResultVO<SeatWithStateVO> lastRealSeat) {
        int lastqos;
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();
        Supplier supplier = SeatInfoUtil.getSupplier();

        if (lastRealSeat != null) {
            lastqos = OnlineState.getOnlineStatePriority(lastRealSeat.getData().getOnlineState());
        }else{
            lastqos = OnlineState.getOnlineStatePriority(getSelectFilterLevelAbove());
        }

        if(lastRealSeat != null &&
                lastRealSeat.getData().getSeat().getCurSessions() <= lastRealSeat.getData().getSeat().getMaxSessions()
                && lastqos >= (OnlineState.maxPriority - 2) &&
                    lastRealSeat.getData().getSeat().getServiceStatus() != ServiceStatusEnum.DND_MODE.getKey()){

            if (lastRealSeat.getData().getSeat().getCurSessions() < lastRealSeat.getData().getSeat().getMaxSessions()){
                return null;
            }
            if (lastRealSeat.getData().getSeat().getCurSessions().equals(lastRealSeat.getData().getSeat().getMaxSessions())
                    && d != null & supplier != null){
                Session ss = RedisBizUtil.lastSession(d.getQunarName(),supplier.getShopId());
                if ( ss != null &&
                        ss.getSeat_name().equals(EjabdUtil.makeSureUserJid(lastRealSeat.getData().getSeat().getQunarName(),QChatConstant.DEFAULT_HOST))
                     /*        && (ss.getSession_state().equals(SessionStateEnum.STATE_PREASSIGN)
                                    || ss.getSession_state().equals(SessionStateEnum.STATE_ASSIGNED))
                                    || ss.getSession_state().equals(SessionStateEnum.STATE_STOPED)*/
                        ){
                    return null;
                }
            }
        }

        if (null != qosSeatLists) {
            for (int qos = OnlineState.maxPriority; qos > 0; qos--) {
                OnlineState onlineState = OnlineState.of(qos);
                if (null != onlineState && qosSeatLists.containsKey(onlineState)
                        && qosSeatLists.get(onlineState).getSeat().getCurSessions() <= qosSeatLists.get(onlineState).getSeat().getMaxSessions()) {
                    if (qosSeatLists.get(onlineState).getSeat().getCurSessions() < qosSeatLists.get(onlineState).getSeat().getMaxSessions()) {
                        return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(), qosSeatLists.get(onlineState));
                    }
                    if (qosSeatLists.get(onlineState).getSeat().getCurSessions().equals(
                            qosSeatLists.get(onlineState).getSeat().getMaxSessions())
                            && d != null & supplier != null ){
                        Session ss = RedisBizUtil.lastSession(d.getQunarName(),supplier.getShopId());
                        if ( ss != null && ss.getSeat_name().equals(EjabdUtil.makeSureUserJid(
                                qosSeatLists.get(onlineState).getSeat().getQunarName(),QChatConstant.DEFAULT_HOST))){
                            return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(),
                                    qosSeatLists.get(onlineState));
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Map<String, Object> getChildsLogParam() {
        List<SeatAndGroup> allSeat = SeatInfoUtil.getAllSeat();
        Map<String, Object> subShortCut = Maps.newHashMap();
        String allids = "";
        if (null != allSeat) {
            List<String> ids = Lists.transform(allSeat, new Function<SeatAndGroup, String>() {
                @Override
                public String apply(SeatAndGroup input) {
                    return input.getQunarName();
                }
            });
            allids = Joiner.on(".").join(ids);
        }
        subShortCut.put("ids", allids);
        return subShortCut;
    }

    protected void logOperation(SeatSelectorOperatorCode code, Object param) {
        logOperation(code, param, null);
    }

    protected void logOperation(SeatSelectorOperatorCode code, Object param, OnlineState onlineState) {
//        if (null == code)
//            return;
//
//        Map<String, Object> errornote = new HashMap<>();
//        errornote.put("code", code.getCode());
//        errornote.put("desc", code.getDesc());
//        if (null != onlineState) {
//            errornote.put("filterlevel", OnlineState.getOnlineStatePriority(onlineState));
//        }
//        Map<String, Object> shortcut = new HashMap<>();
//        shortcut.put("busiSupplierId", d.getBusiSupplierId());
//        shortcut.put("busi", null == d.getBusinessEnum() ? "" : d.getBusinessEnum().getEnName());
//        shortcut.put("qunarName", d.getQunarName());
//        shortcut.put("productID", d.getProductID());
//        shortcut.put("groupType", d.getGroupType());
//
//        Map<String, Object> subShortCut = getChildsLogParam();
//        if (null != subShortCut)
//            shortcut.putAll(subShortCut);
//
//        errornote.put("shortcut", shortcut);
//        if (null != param)
//            errornote.put("subparam", param);
//
//        String logData = JacksonUtils.obj2StringPretty(errornote);
//        String pid = d.getProductID();
//        if (!Strings.isNullOrEmpty(pid) && pid.length() > 20) {
//            pid = pid.substring(0, 20);
//        }
//        LogUtil.doLog(LogEntity.OPERATOR_ONESEAT,
//                LogEntity.ITEM_SEAT,
//                code.getCode(),
//                d.getQunarName(),
//                pid,
//                logData
//        );
//        System.out.println("logOperation->" + logData);
    }


}
