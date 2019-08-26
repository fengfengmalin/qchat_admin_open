package com.qunar.qchat.admin.dao.seat;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yinmengwang on 17-5-31.
 */
@Repository
public interface GroupNewDao {

    List<Long> queryGroupIdsBySupplierIds(@Param(value = "supplierIds") List<Long> supplierIds);

    int deleteGroupBySupplierIds(@Param(value = "groupIds") List<Long> groupIds);
}
