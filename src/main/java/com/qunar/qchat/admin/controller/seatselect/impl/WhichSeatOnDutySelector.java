package com.qunar.qchat.admin.controller.seatselect.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.controller.seatselect.SeatSelectorOperatorCode;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatAndGroup;
import com.qunar.qchat.admin.service.util.SeatUtil;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.SeatInfoUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SeatsResultVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service(value = "whichSeatOnDutySelector")
public class WhichSeatOnDutySelector extends SeatDefaultSelector {
   // private static final Logger logger = LoggerFactory.getLogger(WhichSeatOnDutySelector.class);

    @Override
    protected Map<String, Object> getChildsLogParam() {
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();

        Map<String, Object> subShortCut = super.getChildsLogParam();
//        if (null == subShortCut)
//            subShortCut = Maps.newHashMap();
        subShortCut.put("supplierId", d.getSupplierId());
        subShortCut.put("seatQName", d.getLastSeatName());
        return subShortCut;
    }

    @Override
    protected List<SeatAndGroup> getAllSeat() {
        List<SeatAndGroup> seatAndGroups = Lists.newArrayList();

        // 获取全量的客服列表，这样seatQName之外的人才能被会话保持
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();

        if (!Strings.isNullOrEmpty(d.getLastSeatName())) {
            seatAndGroups = seatDao.getSeatBySeatQNameGroup(d.getSupplierId(), d.getLastSeatName());
        }

        if (CollectionUtil.isEmpty(seatAndGroups)) {
            seatAndGroups = seatDao.getAllSeatsWithGroupBySupplierId(d.getSupplierId());
        }
        if (CollectionUtil.isEmpty(seatAndGroups)) {
            seatAndGroups = seatDao.getSeatsWithoutGroupBySupplierId(d.getSupplierId());
        }

        if (!seatAndGroups.isEmpty())
            d.setBusinessEnum(BusinessEnum.of(seatAndGroups.get(0).getBusiId()));

        return seatAndGroups;
    }

    /**
     * 比默认的多一个一个环节
     *
     * @param ssList
     * @param robotseat
     * @return
     */
    @Override
    protected SeatsResultVO<SeatWithStateVO> processOnPreRobotSelect(List<SeatWithStateVO> ssList, SeatsResultVO<SeatWithStateVO> robotseat) {

        SeatsResultVO<SeatWithStateVO> lastSeat = null;
        if (null != ssList && !ssList.isEmpty()) {

            // 如果配置了机器人那么判定一下需不需要会话保持逻辑，
            // 会话保持最优先
            lastSeat = getLastRealSeatQName(ssList, RobotConfig.ROBOT_ALLOCATION_INTERVAL_TIME_MIN);

            if (null != lastSeat
                    && null != lastSeat.getData()
                    && SeatUtil.isSeatServiceable(lastSeat.getData(), lastSeat.getData().getOnlineState())) {
                // 保持座席成功
                logOperation(SeatSelectorOperatorCode.CONVESATIONHOLDSECCESS, lastSeat);
                return lastSeat;
            }
            SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();

            // 查看所期望的座席是不是在客服列表中，如果在，且为可接客状态，那么就返回期望客服
            if (!Strings.isNullOrEmpty(d.getLastSeatName())) {
                for (SeatWithStateVO ss : ssList) {
                    if (null != ss.getSeat()
                            && !Strings.isNullOrEmpty(ss.getSeat().getQunarName())
                            && ss.getSeat().getQunarName().equalsIgnoreCase(d.getLastSeatName())) {
                        if (SeatUtil.isSeatServiceable(ss, getSelectFilterLevelAbove())) {
                            // 返回机器人id
                            return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, d.getBusinessEnum().getEnName(), ss);
                        }
                    }
                }
            }

            if (null == lastSeat) {
                // 没有客服聊天记录的，分配给机器人
                if (null != d.getEvents())
                    d.getEvents().onPreRobotSelectd(d,null, robotseat.getData());
                logOperation(SeatSelectorOperatorCode.PREROBOTSELECTD, null);
                return robotseat;
            }

            // 在其他好友中分配一个
            // 重新排序座席顺序，而且遵循客服的在线状态，与服务状态
            Map<OnlineState, SeatWithStateVO> qosSeatList = sortSeatVOList(ssList);
            SeatsResultVO<SeatWithStateVO> fixSeat = filterPriorityByOnlineStatus(0,
                    qosSeatList, OnlineState.getOnlineStatePriority(getSelectFilterLevelAbove()));
            if (null != fixSeat) {
                // 新分配了个客服
                if (null != d.getEvents())
                    d.getEvents().onRealSeatSelect(d,fixSeat.getData());
                logOperation(SeatSelectorOperatorCode.REALSEATSELECTED, fixSeat);
                return fixSeat;
            }
        }
        SelectorConfigration d = SeatInfoUtil.getSelectorConfigragtion();

        // 最不济，返回机器人
        if (null != d.getEvents())
            d.getEvents().onPreRobotSelectd(d,null == lastSeat ? null : lastSeat.getData(), robotseat.getData());
        logOperation(SeatSelectorOperatorCode.PREROBOTSELECTD, lastSeat);
        return robotseat;
    }


}
