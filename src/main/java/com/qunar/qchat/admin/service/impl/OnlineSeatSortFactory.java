package com.qunar.qchat.admin.service.impl;

import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.service.ASeatSortFactory;
import com.qunar.qchat.admin.service.sortstrategy.ASeatSortStrategy;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.vo.GroupAndSeatVO;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SupplierAndSeatVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
@Component("onlineSeatSortFactory")
public class OnlineSeatSortFactory extends ASeatSortFactory {
//    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineSeatSortFactory.class);
    @Resource(name = "randomSeatStrategy")
    private ASeatSortStrategy randomSort;
    @Resource(name = "orderSeatStrategy")
    private ASeatSortStrategy orderSort;
    @Resource(name = "pollingSeatStrategy")
    private ASeatSortStrategy pollingSort;
    @Resource(name = "defaultSeatStrategy")
    private ASeatSortStrategy defaultSort;
    @Resource(name = "queueASeatSortStrategy")
    private ASeatSortStrategy queueASeatSortStrategy;
    @Resource(name = "unQueueASeatSortStrategy")
    private ASeatSortStrategy unQueueASeatSortStrategy;

    /**
     * 当增加新的规则的时候请在上方先注入进来
     *
     * @see ASeatSortStrategy
     */

    private EnumMap<SeatSortStrategyEnum, ASeatSortStrategy> strategyEnumMap;

    public OnlineSeatSortFactory() {
    }

    public ASeatSortStrategy getASeatSortStrategy(SeatSortStrategyEnum sortStrategyEnum) {
        init();
        if (strategyEnumMap.containsKey(sortStrategyEnum))
            return strategyEnumMap.get(sortStrategyEnum);
        return unQueueASeatSortStrategy; // default one
    }

    @Override
    protected List<GroupAndSeatVO> sortGroupAndSeatVO(List<GroupAndSeatVO> groupAndSeatVOs) {
        init();
        for (GroupAndSeatVO gas : groupAndSeatVOs) {
            SeatSortStrategyEnum strategyEnum = gas.getStrategy();
            //将未排序的列表取出来进行排序
            List<SeatWithStateVO> sortedList = strategyEnumMap.get(strategyEnum).sortSeatVOList(gas.getSeatWithStateVOList());
            //替换未排序的列表成排序好的列表
            gas.setSeatWithStateVOList(sortedList);
        }

        //将组和组之间进行排序，拥有在线客服的组优先级比较高
        Collections.sort(groupAndSeatVOs, new Comparator<GroupAndSeatVO>() {
            @Override
            public int compare(GroupAndSeatVO g1, GroupAndSeatVO g2) {
                return getGroupPriority(g2) - getGroupPriority(g1);
            }
        });
        return groupAndSeatVOs;
    }

    @Override
    protected void sortBuSupplierAndSeatVO(List<SupplierAndSeatVO> buSupplierAndSeatVOList) {
        init();
        for (SupplierAndSeatVO sas : buSupplierAndSeatVOList) {
            SeatSortStrategyEnum strategyEnum = sas.getStrategy();
            //将未排序的列表取出来进行排序
            List<SeatWithStateVO> sortedList = strategyEnumMap.get(strategyEnum).sortSeatVOList(sas.getSeatWithStateVOList());
            //替换未排序的列表成排序好的列表
            sas.setSeatWithStateVOList(sortedList);
        }
    }

    private void init() {
        if (CollectionUtil.isNotEmpty(strategyEnumMap)) {
            return;
        }

        /**
         * 当增加新的策略的时候在上面用Spring注入进来一个属性，然后在这里把规则放进去就可以,{@link #putStrategy(ASeatSortStrategy)}
         */
        putStrategy(randomSort).putStrategy(orderSort).putStrategy(pollingSort).putStrategy(defaultSort);
        putStrategy(queueASeatSortStrategy);
        putStrategy(unQueueASeatSortStrategy);
    }

    public ASeatSortStrategy getDefaultSort() {
        return defaultSort;
    }

    private OnlineSeatSortFactory putStrategy(ASeatSortStrategy sortStrategy) {
        if (strategyEnumMap == null) {
            strategyEnumMap = new EnumMap<>(SeatSortStrategyEnum.class);
        }
        if (strategyEnumMap.containsKey(sortStrategy.supportStrategy())) {
            StringBuilder sb = new StringBuilder(String.format("同一个排序策略枚举只能对应一个策略,请检查是否对应了两个或两个以上的策略!,重复的策略为:%s。", sortStrategy.supportStrategy().getName()));
            sb.append("已放入的支持的策略有:");
            for (SeatSortStrategyEnum strategy : strategyEnumMap.keySet()) {
                sb.append(strategy.getName()).append("--").append(strategyEnumMap.get(strategy).getClass().getName()).append("|");
            }
            throw new IllegalArgumentException(sb.toString());
        }

        strategyEnumMap.put(sortStrategy.supportStrategy(), sortStrategy);
        return this;
    }

    private int getGroupPriority(GroupAndSeatVO gas) {
        try {
            return OnlineState.getOnlineStatePriority(gas.getSeatWithStateVOList().get(0).getOnlineState());
        } catch (NullPointerException e) {
            return OnlineState.getOnlineStatePriority(null);
        }
    }

}
