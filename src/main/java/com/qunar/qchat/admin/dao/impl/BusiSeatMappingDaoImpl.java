package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IBusiSeatMappingDao;
import com.qunar.qchat.admin.model.BusiSeatMapping;
import org.springframework.stereotype.Repository;

/**
 * Created by qyhw on 10/21/15.
 */
@Repository("busiSeatMappingDao")
public class BusiSeatMappingDaoImpl extends BaseSqlSessionDao implements IBusiSeatMappingDao {

    @Override
    public long saveBusiSeatMapping(BusiSeatMapping busiSeatMapping) {
        this.getWriteSqlSession().insert("BusiSeatMapping.saveBusiSeatMapping", busiSeatMapping);
        return busiSeatMapping.getId();
    }

    @Override
    public int delBusiSeatMapping(long seatId) {
        return this.getWriteSqlSession().delete("BusiSeatMapping.delBusiSeatMapping",seatId);
    }

}
