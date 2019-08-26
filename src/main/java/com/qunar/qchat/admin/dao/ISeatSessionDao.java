package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.SeatSession;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-11-9
 *
 */
public interface ISeatSessionDao {

    SeatSession querySeatSession(long seatId);

    List<SeatSession> querySeatSessionList(List<Long> seatIdList);

    int updateSeatSession(SeatSession seatSession);

    int insertSeatSession(SeatSession seatSession);
}
