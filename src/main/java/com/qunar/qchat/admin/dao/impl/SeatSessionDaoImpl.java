package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISeatSessionDao;
import com.qunar.qchat.admin.model.SeatSession;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-11-9
 *
 */
@Repository("seatSessionDao")
public class SeatSessionDaoImpl extends BaseSqlSessionDao implements ISeatSessionDao {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SeatSessionDaoImpl.class);

    @Override
    public SeatSession querySeatSession(long seatId) {
        return getReadSqlSession().selectOne("seatSessionMapping.querySeatSession", seatId);
    }

    @Override
    public List<SeatSession> querySeatSessionList(List<Long> seatIdList) {
        return getReadSqlSession().selectList("seatSessionMapping.querySeatSessionList", seatIdList);
    }

    @Override
    public int updateSeatSession(SeatSession seatSession) {
        return getWriteSqlSession().update("seatSessionMapping.updateSeatSession", seatSession);
    }

    @Override
    public int insertSeatSession(SeatSession seatSession) {
        return getWriteSqlSession().insert("seatSessionMapping.insertSeatSession", seatSession);
    }
}
