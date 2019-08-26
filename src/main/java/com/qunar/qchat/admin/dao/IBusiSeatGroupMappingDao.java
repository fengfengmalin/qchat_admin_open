package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.BusiSeatGroupMapping;

import java.util.List;

/**
 * Created by qyhw on 10/26/15.
 */
public interface IBusiSeatGroupMappingDao {

    long saveBusiSeatGroupMapping(BusiSeatGroupMapping busiSeatGroupMapping);

    int delBusiSeatGroupMapping(BusiSeatGroupMapping busiSeatGroupMapping);

    List<BusiSeatGroupMapping> getGroupBusiListByGroupId(List<Integer> groupIds);
}
