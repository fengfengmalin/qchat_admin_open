package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IBusiSeatGroupMappingDao;
import com.qunar.qchat.admin.model.BusiSeatGroupMapping;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by qyhw on 10/26/15.
 */
@Repository("busiSeatGroupMappingDao")
public class BusiSeatGroupMappingDaoImpl extends BaseSqlSessionDao implements IBusiSeatGroupMappingDao{

    @Override
    public long saveBusiSeatGroupMapping(BusiSeatGroupMapping busiSeatGroupMapping) {
        getWriteSqlSession().insert("BusiSeatGroupMapping.saveBusiSeatGroupMapping",busiSeatGroupMapping);
        return busiSeatGroupMapping.getId();
    }

    @Override
    public int delBusiSeatGroupMapping(BusiSeatGroupMapping busiSeatGroupMapping) {
        return getWriteSqlSession().delete("BusiSeatGroupMapping.delBusiSeatGroupMapping",busiSeatGroupMapping);
    }

    @Override
    public List<BusiSeatGroupMapping> getGroupBusiListByGroupId(List<Integer> groupIds) {
        return getWriteSqlSession().selectList("BusiSeatGroupMapping.getGroupBusiListByGroupId",groupIds);
    }
}
