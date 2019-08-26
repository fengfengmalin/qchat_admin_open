package com.qunar.qchat.admin.model;

import java.util.Objects;

/**
 * Author : mingxing.shao
 * Date : 15-10-21
 *
 */
public enum SeatSortStrategyEnum {

    DEFAULT_STRATEGY(null,"无"),
    ORDER_STRATEGY(1,"顺序"),
    RANDOM_STRATEGY(2,"随机"),
    POLLING_STRATEGY(3,"轮询"),
    QUEUE_STRATEGY(4,"队列"),
    UNQUEUE_STRATEGY(5,"无队列");

    private Integer strategyId;

    private String name;

    SeatSortStrategyEnum(Integer strategyId,String name) {
        this.strategyId = strategyId;
        this.name = name;
    }

    public Integer getStrategyId() {
        return strategyId;
    }

    public static SeatSortStrategyEnum getStrategy(Integer strategyId) {
        for (SeatSortStrategyEnum seatSortStrategyEnum : SeatSortStrategyEnum.values()) {
            if (Objects.equals(seatSortStrategyEnum.strategyId, strategyId)) {
                return seatSortStrategyEnum;
            }
        }
        return null;
    }

    public String getName() {
        return this.name;
    }
}
