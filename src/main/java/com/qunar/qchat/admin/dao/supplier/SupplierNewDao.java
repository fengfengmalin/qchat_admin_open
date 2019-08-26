package com.qunar.qchat.admin.dao.supplier;

import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.model.Supplier;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by yinmengwang on 17-5-26.
 */
@Repository
public interface SupplierNewDao {

    /**
     * 禁止修改
     */
    List<Supplier> getAllSupplierInfo();

    List<Long> querySupplierIds(@Param(value = "busiSupplierIds") List<String> busiSupplierIds,
            @Param(value = "busiId") int busiId);

    int deleteSuppliers(@Param(value = "supplierIds") List<Long> supplierIds);

    List<Long> filterOnlineSuppliers(@Param(value = "supplierIds") List<Long> supplierIds);
}
