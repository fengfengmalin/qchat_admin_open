package com.qunar.qchat.admin.service.sortstrategy;

import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Author : mingxing.shao
 * Date : 15-10-22
 *
 */
@Component("defaultSeatStrategy")
public class DefaultASeatStrategy extends ASeatSortStrategy {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultASeatStrategy.class);

    @Override
    public List<SeatWithStateVO> sortSeatVOList(List<SeatWithStateVO> seatWithStateVOList) {
        Map<OnlineState, List<SeatWithStateVO>> classifiedMap = classifySeatVOList(seatWithStateVOList);

        List<SeatWithStateVO> resList = new ArrayList<>();
        for (OnlineState onlineState : getOnlineMaxPriorityArray()) {
            List<SeatWithStateVO> temp = classifiedMap.get(onlineState);
            if (CollectionUtil.isNotEmpty(temp)) {
                Collections.sort(temp, new Comparator<SeatWithStateVO>() {
                    @Override
                    public int compare(SeatWithStateVO o1, SeatWithStateVO o2) {
                        return getIntPriority(getIntPriority(o1.getSeat().getPriority() - o2.getSeat().getPriority()));
                    }
                });
                resList.addAll(temp);
            }
        }
        return resList;
    }

    @Override
    public SeatSortStrategyEnum supportStrategy() {
        return SeatSortStrategyEnum.DEFAULT_STRATEGY;
    }

    private int getIntPriority(Integer priority) {
        return priority != null ? priority : 0;
    }
}
