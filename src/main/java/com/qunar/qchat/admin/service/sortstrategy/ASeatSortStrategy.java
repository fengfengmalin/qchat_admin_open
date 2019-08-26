package com.qunar.qchat.admin.service.sortstrategy;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.vo.SeatWithStateVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-10-21
 *
 */
public abstract class ASeatSortStrategy {


    public abstract List<SeatWithStateVO> sortSeatVOList(List<SeatWithStateVO> seatWithStateVOList);

    public abstract SeatSortStrategyEnum supportStrategy();

    protected Map<OnlineState, List<SeatWithStateVO>> classifySeatVOList(List<SeatWithStateVO> seatWithStateVOList) {
        Map<OnlineState, List<SeatWithStateVO>> classify = new HashMap<>();
        for (SeatWithStateVO seatWithStateVO : seatWithStateVOList) {
            List<SeatWithStateVO> sl = classify.get(seatWithStateVO.getOnlineState());
            if (sl != null) {
                sl.add(seatWithStateVO);
            } else {
                classify.put(seatWithStateVO.getOnlineState(), Lists.newArrayList(seatWithStateVO));
            }
        }
        return classify;
    }

    /**
     * 按照{@link OnlineState#getOnlineStatePriority(OnlineState)}的方法返回值的大小按照从大到小排列成一个数组返回
     * @return 排好序的数组
     */
    protected OnlineState[] getOnlineMaxPriorityArray() {
        OnlineState[] onlineStates = OnlineState.values();
        for (int index = 0; index < onlineStates.length - 1; index++) {
            if (OnlineState.getOnlineStatePriority(onlineStates[index]) < OnlineState.getOnlineStatePriority(onlineStates[index + 1])) {
                OnlineState temp = onlineStates[index + 1];
                onlineStates[index + 1] = onlineStates[index];
                onlineStates[index] = temp;
            }
        }
        return onlineStates;
    }
}
