package com.qunar.qchat.admin.service.supplier;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.vo.conf.JsonData;

import java.util.List;

/**
 * Created by yinmengwang on 17-5-26.
 */
public interface SupplierNewService {

    /**
     * 获取所有店铺的id和name，禁止修改
     */
    List<Supplier> getAllSupplierInfo();

    JsonData supplierSuggest(String qunarName, String query);

    JsonData seatSuggest(String qunarName, String query);

    JsonData queryOrganization(String qunarName, long supplierId);

    List<Seat> querySeatsBySupplierId(long supplierId);

    boolean deleteSuppliersByBusiIds(List<String> busiSupplierIds, BusinessEnum busiEnum);

    boolean deleteSuppliersByIds(List<Long> supplierIds);
}
