package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.service.impl.SeatGroupServiceImpl;
import com.qunar.qchat.admin.util.GroupQueryFilter;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SeatGroupListVO;
import com.qunar.qchat.admin.vo.SeatGroupVO;

import java.util.List;
import java.util.Map;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
public interface ISeatGroupService {

    BusiReturnResult saveOrUpdateSeatGroup(SeatGroupVO seatVO);

    SeatGroupListVO pageQueryGroupList(GroupQueryFilter filter, int pageNum, int pageSize);

    int delGroupById(int groupId);

    boolean assignProducts(int groupId, String pIds);

    SeatGroupServiceImpl.GROUPERRORCODE assignProductsInner(int groupId, List<String> pids );

    List<String> queryProducts(int groupId);
    List<String> queryProductsInner(int groupId);

    Map<Integer, List<String>> queryProductsByGroupIds(List<Integer> groupIdList);

}
