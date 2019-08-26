package com.qunar.qchat.admin.service.sortstrategy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.model.SeatSessionsDetail;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component(value = "unQueueASeatSortStrategy")
public class UnQueueASeatSortStrategy extends QueueASeatSortStrategy {

    @Override
    protected void getSeatSessionTimeAndCount(List<SeatWithStateVO> seatWithStateVOList) {
        if (CollectionUtil.isEmpty(seatWithStateVOList))
            return;

        String shop_name = seatWithStateVOList.get(0).getSupplier().getShopId();

        List<String> seatids = Lists.transform(seatWithStateVOList, new Function<SeatWithStateVO, String>() {
            @Override
            public String apply(SeatWithStateVO seatWithStateVO) {
                return seatWithStateVO.getSeat().getQunarName();
            }
        });

        Map<String,SeatSessionsDetail> seatSessionsDetailMap = sessionV2Service.getSeatSessionsDetail(seatids,shop_name);
        for (SeatWithStateVO seatWithStateVO:seatWithStateVOList){
            String seat_name = seatWithStateVO.getSeat().getQunarName();
            if (seatSessionsDetailMap.containsKey(seat_name)){
                seatWithStateVO.setLastStartTime(seatSessionsDetailMap.get(seat_name).getLast_session_time());
                seatWithStateVO.getSeat().setCurSessions(0);
            }
            else {
                seatWithStateVO.setLastStartTime(new Date(0));
                seatWithStateVO.getSeat().setCurSessions(0);
            }
        }
    }

    @Override
    public SeatSortStrategyEnum supportStrategy() {
        return SeatSortStrategyEnum.UNQUEUE_STRATEGY;
    }
}
