package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.BusiSupplierMapping;
import com.qunar.qchat.admin.vo.third.SupplierOperatorInfo;

import java.util.List;

/**
 * Created by qyhw on 10/19/15.
 */
public interface IBusiSupplierMappingDao {

    public long saveBusiSupplierMapping(BusiSupplierMapping busiSupplierMapping);

    public BusiSupplierMapping getBusiSupplierMappingBySuId(long suId);

    List<SupplierOperatorInfo> getOperatorsBySeatQunarName(String seatQunarName);

    int saveSupplierOperator(SupplierOperatorInfo supplierOperatorInfo);
}
