package com.qunar.qchat.admin.service.impl;

import com.qunar.qchat.admin.dao.ISeatSessionDao;
import com.qunar.qchat.admin.model.SeatSession;
import com.qunar.qchat.admin.service.ISeatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-11-9
 *
 */
@Service("seatSessionService")
public class SeatSessionServiceImpl implements ISeatSessionService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SeatSessionServiceImpl.class);
    @Resource(name = "seatSessionDao")
    private ISeatSessionDao seatSessionDao;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void setSeatSessionTime(SeatSession seatSession) {
        if (seatSession == null) {
            return;
        }
        SeatSession session = seatSessionDao.querySeatSession(seatSession.getSeatId());

        if (session == null) {
            seatSessionDao.insertSeatSession(seatSession);
        } else {
            seatSessionDao.updateSeatSession(seatSession);
        }
    }

    @Override
    public List<SeatSession> querySeatSessionList(List<Long> seatIdList) {
        return seatSessionDao.querySeatSessionList(seatIdList);
    }


}
