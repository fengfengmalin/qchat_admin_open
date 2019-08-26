package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.SeatAndBuSupplier;
import com.qunar.qchat.admin.model.SeatAndGroup;
import com.qunar.qchat.admin.vo.GroupAndSeatVO;
import com.qunar.qchat.admin.vo.SupplierAndSeatVO;

import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-10-21
 *
 */
public interface ISeatSortFactory {

    /**
     * 排序，附加在线状态，分组
     * @param seatAndGroupList　坐席
     * @param onlineStateMap　坐席的在线在线状态，key为坐席的qunarName,value为在线状态
     * @return .
     */
    List<GroupAndSeatVO> getSeatsWithOnlineStateList(List<SeatAndGroup> seatAndGroupList, Map<String, OnlineState> onlineStateMap);

    /**
     * 对供应商下直属客服进行排序,附加在线状态
     * @param seatAndSupplierList  客服列表
     * @param onlineStateMap 客服在线状态
     * @return
     */
    List<SupplierAndSeatVO> getSeatsWithOSListByBuSupplier(List<SeatAndBuSupplier> seatAndSupplierList, Map<String, OnlineState> onlineStateMap);
}
