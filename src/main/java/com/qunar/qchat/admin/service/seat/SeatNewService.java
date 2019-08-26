package com.qunar.qchat.admin.service.seat;

/**
 * Created by yinmengwang on 17-7-3.
 */
public interface SeatNewService {

    /**
     * 判断一个用户名是否为客服
     */
    boolean isSeat(String qunarName,String seatHost);
}
