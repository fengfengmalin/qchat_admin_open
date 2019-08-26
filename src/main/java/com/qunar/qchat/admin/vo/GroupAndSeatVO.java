package com.qunar.qchat.admin.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
public class GroupAndSeatVO {
    private Long groupId;
    private String groupName;
    private SeatSortStrategyEnum strategy;

    private List<SeatWithStateVO> seatWithStateVOList;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @JsonSerialize(using = SeatSortStrategyJsonAdapter.Serializer.class)
    public SeatSortStrategyEnum getStrategy() {
        return strategy;
    }

//    public void setStrategy(Integer strategy) {
//        this.strategy = SeatSortStrategyEnum.getStrategy(strategy);
//    }

    @JsonDeserialize(using = SeatSortStrategyJsonAdapter.Deserializer.class)
    public void setStrategy(SeatSortStrategyEnum strategy) {
        this.strategy = strategy;
    }

    public List<SeatWithStateVO> getSeatWithStateVOList() {
        return seatWithStateVOList;
    }

    public void setSeatWithStateVOList(List<SeatWithStateVO> seatWithStateVOList) {
        this.seatWithStateVOList = seatWithStateVOList;
    }

}
