package com.qunar.qchat.admin.service.sortstrategy;

import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Author : mingxing.shao
 * Date : 15-10-21
 *
 */
@Component("randomSeatStrategy")
public class RandomASeatStrategy extends ASeatSortStrategy {
//    private static final Logger LOGGER = LoggerFactory.getLogger(RandomASeatStrategy.class);

    @Override
    public List<SeatWithStateVO> sortSeatVOList(List<SeatWithStateVO> seatWithStateVOList) {
        Map<OnlineState, List<SeatWithStateVO>> classifiedMap = classifySeatVOList(seatWithStateVOList);

        List<SeatWithStateVO> resList = new ArrayList<>();

        for (OnlineState onlineState : getOnlineMaxPriorityArray()) {
            List<SeatWithStateVO> temp = classifiedMap.get(onlineState);
            if (CollectionUtil.isNotEmpty(temp)) {
                Collections.sort(temp, new Comparator<SeatWithStateVO>() {
                    @Override
                    @SuppressWarnings("all")
                    public int compare(SeatWithStateVO o1, SeatWithStateVO o2) {
                        return RandomUtils.nextInt(0, 100) - RandomUtils.nextInt(0, 100);
                    }
                });
                resList.addAll(temp);
            }
        }
        return resList;
    }

    @Override
    public SeatSortStrategyEnum supportStrategy() {
        return SeatSortStrategyEnum.RANDOM_STRATEGY;
    }
}
