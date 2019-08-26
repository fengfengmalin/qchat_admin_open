package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.GroupProductMapping;
import com.qunar.qchat.admin.model.SeatAndGroup;
import com.qunar.qchat.admin.model.SeatGroup;
import com.qunar.qchat.admin.util.GroupQueryFilter;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
public interface ISeatGroupDao {

    List<SeatGroup> getSeatGroup(int supplierId,int businessId);

    int saveSeatGroup(SeatGroup sg);

    int updateSeatGroup(SeatGroup sg);

    List<SeatGroup> pageQueryGroupList(GroupQueryFilter filter, int pageNum, int pageSize);

    int pageQueryGroupListCount(GroupQueryFilter filter);

    int delGroupById(int groupId);

    SeatGroup getGroup(String groupName,long supplierId);

    SeatGroup getGroupById(int id);

    int saveGroupProductMapping(GroupProductMapping gpMapping);

    int delGroupProductMappingByGroupId(int groupId);

    List<GroupProductMapping> getProductListByGroupId(int groupId);

    List<GroupProductMapping> getProductListByGroupIds(List<Integer> groupIdList);

    List<SeatAndGroup> getSeatAndGroupListByPid(String pid);
}
