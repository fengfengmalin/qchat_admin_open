package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.SeatGroup;
import com.qunar.qchat.admin.model.SeatGroupMapping;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
public interface ISeatGroupMappingDao {

    List<SeatGroup> getSeatGroup(int supplierId, int businessId);

    long saveSeatGroupMapping(SeatGroupMapping sgMapping);

    int delSeatGroupMapping(SeatGroupMapping sgMapping);
}
