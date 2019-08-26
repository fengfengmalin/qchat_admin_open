package com.qunar.qchat.admin.service.sortstrategy;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatSession;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.model.ServiceStatusEnum;
import com.qunar.qchat.admin.service.ISeatSessionService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Author : mingxing.shao
 * Date : 15-10-28
 *
 */

@Component("pollingWithServiceStatusASeatStrategy")
public class PollingWithServiceStatusASeatStrategy extends ASeatSortStrategy {
//    private static final Logger LOGGER = LoggerFactory.getLogger(PollingWithServiceStatusASeatStrategy.class);
    private static final long DEFAULT_FOR_NULL_TIME = new Date(0).getTime();//当取不到上次会话的时间时，默认是unix开始时间
    @Resource(name = "seatSessionService")
    private ISeatSessionService seatSessionService;


    @Override
    protected Map<OnlineState, List<SeatWithStateVO>> classifySeatVOList(List<SeatWithStateVO> seatWithStateVOList) {
        Map<OnlineState, List<SeatWithStateVO>> classify = new HashMap<>();
        for (SeatWithStateVO seatWithStateVO : seatWithStateVOList) {
            // 优先判定服务状态
            OnlineState onlineState = seatWithStateVO.getOnlineState();
            if (ServiceStatusEnum.SUPER_MODE.getKey() == seatWithStateVO.getSeat().getServiceStatus())
                onlineState = OnlineState.ONLINE;
            if (ServiceStatusEnum.DND_MODE.getKey() == seatWithStateVO.getSeat().getServiceStatus())
                onlineState = OnlineState.OFFLINE;

            List<SeatWithStateVO> sl = classify.get(onlineState);
            if (sl != null) {
                sl.add(seatWithStateVO);
            } else {
                classify.put(onlineState, Lists.newArrayList(seatWithStateVO));
            }
        }
        return classify;
    }

    /**
     * <note>
     * 平均轮询功能目前已支持
     * </note>
     *
     * @param seatWithStateVOList 需要进行排序的列表
     * @return 排序好的列表
     */
    @Override
    public List<SeatWithStateVO> sortSeatVOList(List<SeatWithStateVO> seatWithStateVOList) {

        //客服上一次开始会话的时间
        getSeatLastSessionTime(seatWithStateVOList);


        //把客服按照在线、忙碌、离开和离线进行分类
        Map<OnlineState, List<SeatWithStateVO>> classifiedMap = classifySeatVOList(seatWithStateVOList);

        List<SeatWithStateVO> resList = new ArrayList<>();//结果集存的List

        for (OnlineState onlineState : getOnlineMaxPriorityArray()) {
            List<SeatWithStateVO> temp = classifiedMap.get(onlineState);//分别获取在线,忙碌,离开,或者离线的坐席
            if (CollectionUtil.isNotEmpty(temp)) {
                //按照客服上次开始会话的时间进行排序，如果客服没有上次开始会话的时间，则优先级最高
                Collections.sort(temp, new Comparator<SeatWithStateVO>() {
                    @Override
                    public int compare(SeatWithStateVO s1, SeatWithStateVO s2) {
                        Date s1Date = s1.getLastStartTime();
                        Date s2Date = s2.getLastStartTime();
                        long s1Time = s1Date == null ? DEFAULT_FOR_NULL_TIME : s1Date.getTime();
                        long s2Time = s2Date == null ? DEFAULT_FOR_NULL_TIME : s2Date.getTime();
                        return s1Time - s2Time > 0 ? 1 : s1Time - s2Time == 0 ? 0 : -1;
                    }
                });
                resList.addAll(temp);
            }
        }
        return resList;
    }

    @Override
    public SeatSortStrategyEnum supportStrategy() {
        return SeatSortStrategyEnum.POLLING_STRATEGY;
    }

    /**
     * 获取每个坐席的上一次开会话的时间
     *
     * @param seatWithStateVOList 客服信息列表
     * @return 坐席的上一次开始会话的时间
     */
    private void getSeatLastSessionTime(List<SeatWithStateVO> seatWithStateVOList) {
        final List<Long> seatIdList = new ArrayList<>();
        for (SeatWithStateVO seatWithState : seatWithStateVOList) {
            seatIdList.add(seatWithState.getSeat().getId());
        }
        List<SeatSession> seatSessionList = seatSessionService.querySeatSessionList(seatIdList);
        final ImmutableMap<Long, SeatSession> sessionMap = Maps.uniqueIndex(seatSessionList,
                new Function<SeatSession, Long>() {
                    @Override
                    public Long apply(SeatSession seatSession) {
                        return seatSession.getSeatId();
                    }
                });
        for (SeatWithStateVO seatWithStateVO : seatWithStateVOList) {
            SeatSession seatSession = null;
            if (MapUtils.isEmpty(sessionMap)
                    || (seatSession = sessionMap.get(seatWithStateVO.getSeat().getId())) == null) {
                seatWithStateVO.setLastStartTime(new Date(0));
            } else {
                seatWithStateVO.setLastStartTime(seatSession.getLastStartTime());
            }
        }
    }
}
