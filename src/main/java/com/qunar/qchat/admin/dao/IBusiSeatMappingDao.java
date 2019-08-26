package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.BusiSeatMapping;

/**
 * Created by qyhw on 10/21/15.
 */
public interface IBusiSeatMappingDao {

    long saveBusiSeatMapping(BusiSeatMapping busiSeatMapping);

    int delBusiSeatMapping(long seatId);
}
