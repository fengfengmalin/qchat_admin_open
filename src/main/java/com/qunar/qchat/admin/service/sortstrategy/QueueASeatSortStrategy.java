package com.qunar.qchat.admin.service.sortstrategy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatSessionsDetail;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.service.ISessionV2Service;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component(value = "queueASeatSortStrategy")
public class QueueASeatSortStrategy extends ASeatSortStrategy {

    private static final long DEFAULT_FOR_NULL_TIME = new Date(0).getTime();//当取不到上次会话的时间时，默认是unix开始时间

    @Resource(name = "sessionV2Service")
    protected ISessionV2Service sessionV2Service;

    protected void getSeatSessionTimeAndCount(List<SeatWithStateVO> seatWithStateVOList){
        if (CollectionUtil.isEmpty(seatWithStateVOList))
            return;

        String shop_name = seatWithStateVOList.get(0).getSupplier().getShopId();

        List<String> seatids = Lists.transform(seatWithStateVOList, new Function<SeatWithStateVO, String>() {
            @Override
            public String apply(SeatWithStateVO seatWithStateVO) {
              // return seatWithStateVO.getSeat().getQunarName();
                return  EjabdUtil.makeSureUserJid(seatWithStateVO.getSeat().getQunarName(), QChatConstant.DEFAULT_HOST);
            }
        });

        Map<String,SeatSessionsDetail> seatSessionsDetailMap = sessionV2Service.getSeatSessionsDetail(seatids,shop_name);
        for (SeatWithStateVO seatWithStateVO:seatWithStateVOList){
            String seat_name = EjabdUtil.makeSureUserJid(seatWithStateVO.getSeat().getQunarName(), QChatConstant.DEFAULT_HOST);
            if (seatSessionsDetailMap.containsKey(seat_name)){
                seatWithStateVO.setLastStartTime(seatSessionsDetailMap.get(seat_name).getLast_session_time());
                seatWithStateVO.getSeat().setCurSessions(seatSessionsDetailMap.get(seat_name).getSession_counts());

            }
            else {
                seatWithStateVO.setLastStartTime(new Date(0));
                seatWithStateVO.getSeat().setCurSessions(0);
            }
        }
    }
    @Override
    public List<SeatWithStateVO> sortSeatVOList(List<SeatWithStateVO> seatWithStateVOList) {
        // 没人的时候排个P
        if (CollectionUtil.isEmpty(seatWithStateVOList))
            return seatWithStateVOList;

        // 获取每个客服的最新的服务时间和最近服务的数
        getSeatSessionTimeAndCount(seatWithStateVOList);

        // 1个人还排个P
        if (1 == seatWithStateVOList.size())
            return seatWithStateVOList;


        //把客服按照在线、忙碌、离开和离线进行分类
        Map<OnlineState, List<SeatWithStateVO>> classifiedMap = classifySeatVOList(seatWithStateVOList);

        final List<SeatWithStateVO> resList = new ArrayList<>();//结果集存的List

        for (OnlineState onlineState : getOnlineMaxPriorityArray()) {
            List<SeatWithStateVO> temp = classifiedMap.get(onlineState);//分别获取在线,忙碌,离开,或者离线的坐席
            if (CollectionUtil.isNotEmpty(temp)) {
                //按照客服上次开始会话的时间进行排序，如果客服没有上次开始会话的时间，则优先级最高
                //扩展修改，根据当前活动的session数量进行排列
                Collections.sort(temp, new Comparator<SeatWithStateVO>() {
                    @Override
                    public int compare(SeatWithStateVO s1, SeatWithStateVO s2) {

                        if (null == s1.getSeat().getMaxSessions())
                            s1.getSeat().setMaxSessions(0);
                        if (null == s2.getSeat().getMaxSessions())
                            s2.getSeat().setMaxSessions(0);
                        if (null == s1.getSeat().getCurSessions())
                            s1.getSeat().setCurSessions(0);
                        if (null == s2.getSeat().getCurSessions())
                            s2.getSeat().setCurSessions(0);

                        if ( 0!=s1.getSeat().getMaxSessions() &&
                                0!=s2.getSeat().getMaxSessions()){
                      //      float businessRate1 = s1.getSeat().getCurSessions()/s1.getSeat().getMaxSessions();
                       //     float businessRate2 = s2.getSeat().getCurSessions()/s2.getSeat().getMaxSessions();
                            float businessRate1 =s1.getSeat().getMaxSessions() - s1.getSeat().getCurSessions(); ;
                            float businessRate2 = s2.getSeat().getMaxSessions() - s2.getSeat().getCurSessions();;
                            if (businessRate1!=businessRate2){
                                return businessRate1 < businessRate2 ? 1 : -1;
                            }
                        }

                        Date s1Date = s1.getLastStartTime();
                        Date s2Date = s2.getLastStartTime();
                        long s1Time = s1Date == null ? DEFAULT_FOR_NULL_TIME : s1Date.getTime();
                        long s2Time = s2Date == null ? DEFAULT_FOR_NULL_TIME : s2Date.getTime();
                        return s1Time - s2Time < 0 ? 1 : s1Time - s2Time == 0 ? 0 : -1;
                    }
                });
                resList.addAll(temp);
            }
        }
        return resList;
    }

    @Override
    public SeatSortStrategyEnum supportStrategy() {
        return SeatSortStrategyEnum.QUEUE_STRATEGY;
    }
}
