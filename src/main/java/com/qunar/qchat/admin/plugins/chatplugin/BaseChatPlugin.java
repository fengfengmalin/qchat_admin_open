package com.qunar.qchat.admin.plugins.chatplugin;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.annotation.routingdatasource.DataSourceKeyHolder;
import com.qunar.qchat.admin.annotation.routingdatasource.DataSources;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.dao.msg.IMsgDao;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.responce.SessionDetailResponce;
import com.qunar.qchat.admin.service.query.SessionQueryFilter;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseChatPlugin implements IChatPlugin {

    protected final Logger assignSeatAppender = LoggerFactory.getLogger("assignSeatAppender");

    private IMsgDao msgDao;

    private DataSources dataSources = null;

    public IMsgDao getMsgDao() {
        return msgDao;
    }

    public void setMsgDao(IMsgDao msgDao) {
        this.msgDao = msgDao;
    }

    public DataSources getDataSources() {
        return dataSources;
    }

    public void setDataSources(DataSources dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public List<SeatOnlineState> getUsersOnlineStatus(List<String> userids) {
        return null;
    }

    @Override
    public BusiReturnResult checkUserExist(String p) {
        return null;
    }

    @Override
    public SessionListResultVO getChatSessionList(SessionQueryFilter filter, int pageNum, int pageSize) {
        return null;
    }

    @Override
    public SessionDetailResponce getSessionDetail(String visitorName, String seatName, String startTime, String endTime, String timestamp, int limitnum, int direction) {
        return null;
    }

    @Override
    public boolean sendThirdMessage(String from, String to, String message) {
        return false;
    }

    @Override
    public boolean sendThirePresence(String from, String to, String category, String body) {
        return false;
    }


    @Override
    public SeatsResultVO<SeatWithStateVO> getLastChatSeat(BusinessEnum businessEnum, String qunarName, List<SeatWithStateVO> ssList) {
        if (null == msgDao || null == dataSources)
            return null;

        DataSourceKeyHolder.set(dataSources.key());
        SeatsResultVO<SeatWithStateVO> obj = innergetLastChatSeat(businessEnum, qunarName, ssList);
        DataSourceKeyHolder.clear();
        return obj;
    }

    @Override
    public String getLastChatSeat(String qunarName, List<String> shopids, List<String> seatIds, int since) {
        if (null == msgDao || null == dataSources)
            return null;
        DataSourceKeyHolder.set(dataSources.key());
        String ret = innergetLastChatSeat(qunarName, shopids, seatIds, since);
        DataSourceKeyHolder.clear();
        return ret;
    }

    @Override
    public String seatIntervalRobot(List<String> seatQNames, String userQName, List<String> shopids) {
        if (null == msgDao || null == dataSources)
            return null;
        DataSourceKeyHolder.set(dataSources.key());
        String ret = innerseatIntervalRobot(seatQNames, userQName, shopids);
        DataSourceKeyHolder.clear();
        return ret;
    }

    @Override
    public List<Map<String, Object>> getLastConversationTime(String userid, List<String> seatids) {
        if (null == msgDao || null == dataSources)
            return null;
        DataSourceKeyHolder.set(dataSources.key());
        List<Map<String,Object>> ret = innergetLastConversationTime(userid, seatids);
        DataSourceKeyHolder.clear();
        return ret;
    }



    private static final Function<SeatWithStateVO, String> getQunarName = new Function<SeatWithStateVO, String>() {
        @Override
        public String apply(SeatWithStateVO seatWithStateVO) {
            if (seatWithStateVO == null || seatWithStateVO.getSeat() == null)
                return null;
            return EjabdUtil.makeSureUserid(seatWithStateVO.getSeat().getQunarName());
        }
    };

    private static final Function<SeatWithStateVO, String> getShopIds = new Function<SeatWithStateVO, String>() {
        @Override
        public String apply(SeatWithStateVO seatWithStateVO) {
            if (null != seatWithStateVO) {
                if (seatWithStateVO.isSwitchOn()) {
                    if (null != seatWithStateVO.getSupplier())
                        return EjabdUtil.makeSureUserid(seatWithStateVO.getSupplier().getShopId());
                } else {
                    if (null != seatWithStateVO.getSeat())
                        return EjabdUtil.makeSureUserid(seatWithStateVO.getSeat().getQunarName());
                }
            }
            return "";
        }
    };

    private String innergetLastChatSeat(String qunarName, List<String> shopids, List<String> seatIds, int sinceTimeByMins) {
        String lastSeatQunarName = "";

        qunarName = EjabdUtil.makeSureUserid(qunarName);
        if (sinceTimeByMins > 0) {

            lastSeatQunarName = msgDao.getLastSeatNameWithShopIdBefore(
                    qunarName, shopids, seatIds, sinceTimeByMins);
        } else {
            lastSeatQunarName = msgDao.getLastSeatNameWithShopIdEx(
                    qunarName, shopids, seatIds);
        }
        return lastSeatQunarName;
    }

    private SeatsResultVO<SeatWithStateVO> innergetLastChatSeat(BusinessEnum businessEnum, String qunarName, List<SeatWithStateVO> ssList) {

        qunarName = EjabdUtil.makeSureUserid(qunarName);
        String lastSeatQunarName = msgDao.getLastSeatNameWithShopIdEx(
                qunarName,
                Lists.transform(ssList, getShopIds),
                Lists.transform(ssList, getQunarName));

        if (!StringUtils.isEmpty(lastSeatQunarName)) {
            lastSeatQunarName = EjabdUtil.makeSureUserid(lastSeatQunarName);
        }
        else {
            return null;
        }


        Map<OnlineState, SeatWithStateVO> sList = new HashMap<>();
        SeatWithStateVO lastSeatVO = null;
        for (SeatWithStateVO ssVO : ssList) {
            OnlineState s = ssVO.getOnlineState();
            if (!sList.containsKey(s)) {
                sList.put(s, ssVO);
            }
            String seatid = EjabdUtil.makeSureUserid(ssVO.getSeat().getQunarName());
            if (lastSeatQunarName.equalsIgnoreCase(seatid)) {
                lastSeatVO = ssVO;
            }
        }

        // 没找到上一次聊天的客服
        if (lastSeatVO == null) {
            return null;
        }

        int p = OnlineState.getOnlineStatePriority(lastSeatVO.getOnlineState());
        if (p == 1) {
            SeatWithStateVO ssVOTemp = sList.get(OnlineState.ONLINE);
            if (ssVOTemp == null) {
                ssVOTemp = sList.get(OnlineState.BUSY);
            }
            if (ssVOTemp == null) {
                ssVOTemp = sList.get(OnlineState.AWAY);
            }
            if (ssVOTemp != null) {
                lastSeatVO = ssVOTemp;
            }
        }

        if (p == 2) {
            SeatWithStateVO ssVOTemp = sList.get(OnlineState.ONLINE);
            if (ssVOTemp == null) {
                ssVOTemp = sList.get(OnlineState.BUSY);
            }
            if (ssVOTemp != null) {
                lastSeatVO = ssVOTemp;
            }
        }
        if (p == 3) {
            SeatWithStateVO ssVOTemp = sList.get(OnlineState.ONLINE);
            if (ssVOTemp != null) {
                lastSeatVO = ssVOTemp;
            }
        }
        return new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), lastSeatVO);
    }


    private String innerseatIntervalRobot(List<String> seatQNames, String userQName, List<String> virturalids) {
        if (Strings.isNullOrEmpty(userQName) || CollectionUtils.isEmpty(seatQNames)) {
            return null;
        }
        userQName = EjabdUtil.makeSureUserid(userQName);
        return msgDao.getLastSeatNameWithShopIdBefore(
                userQName,
                EjabdUtil.makeSureUseridList(virturalids),
                EjabdUtil.makeSureUseridList(seatQNames),
                RobotConfig.ROBOT_ALLOCATION_INTERVAL_TIME_MIN);
    }

    private List<Map<String,Object>> innergetLastConversationTime(String userid, List<String> seatids) {
        if (Strings.isNullOrEmpty(userid) || CollectionUtils.isEmpty(seatids)) {
            return null;
        }

        userid = EjabdUtil.makeSureUserid(userid);

        return msgDao.getLestConversationTime(userid, EjabdUtil.makeSureUseridList(seatids));

    }
}
