package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.SeatSessionsDetail;

import java.util.List;
import java.util.Map;

public interface ISessionV2Service {
    /**
     * 扫描会话的状态，做适当的变更
     */



    Map<String,SeatSessionsDetail> getSeatSessionsDetail(List<String> seatids,String shop_name);


}
