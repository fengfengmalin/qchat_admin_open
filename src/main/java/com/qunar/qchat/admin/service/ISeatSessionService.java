package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.SeatSession;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-11-9
 *
 */
public interface ISeatSessionService {

    void setSeatSessionTime(SeatSession seatSession);

    List<SeatSession> querySeatSessionList(List<Long> seatIdList);
}
