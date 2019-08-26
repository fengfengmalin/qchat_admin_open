package com.qunar.qchat.admin.dao.welcomes;

import com.qunar.qchat.admin.model.Supplier;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by yinmengwang on 17-5-19.
 */
@Repository
public interface SupplierWelcomesDao {

    List<Supplier> getWelcomesBySupplierIds(@Param(value = "supplierIds") List<Long> supplierIds);

    int updateWelcomesBySupplierId(@Param(value = "suppliers") List<Supplier> suppliers);

    String queryWelcomesBySeatQName(@Param(value = "seatQName") String seatQName);

    String queryWelcomesBySeatId(@Param(value = "seatId") long seatId);

    String queryWelcomesById(@Param(value = "supplierId") long supplierId);
}
