package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISeatGroupMappingDao;
import com.qunar.qchat.admin.model.SeatGroup;
import com.qunar.qchat.admin.model.SeatGroupMapping;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by qyhw on 10/21/15.
 */
@Repository("seatGroupMappingDao")
public class SeatGroupMappingDaoImpl extends BaseSqlSessionDao implements ISeatGroupMappingDao {

    @Override
    public List<SeatGroup> getSeatGroup(int supplierId, int businessId) {
        return null;
    }

    @Override
    public long saveSeatGroupMapping(SeatGroupMapping sgMapping) {
        this.getWriteSqlSession().insert("SeatGroupMapping.saveSeatGroupMapping",sgMapping);
        return sgMapping.getId();
    }

    @Override
    public int delSeatGroupMapping(SeatGroupMapping sgMapping) {
        return this.getWriteSqlSession().delete("SeatGroupMapping.delSeatGroupMapping",sgMapping);
    }
}
