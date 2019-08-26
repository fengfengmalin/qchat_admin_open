package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.model.SupplierInfo;
import com.qunar.qchat.admin.model.SupplierWithRobot;
import com.qunar.qchat.admin.vo.SupplierGroupVO;

import java.util.List;

/**
 * Created by qyhw on 10/15/15.
 */
public interface ISupplierDao {

    public List<Supplier> getSupplierList();

    public long saveSupplier(Supplier supplier);

    public long saveSupplierEx(Supplier supplier);


    public int updateSupplier(Supplier supplier);

    public int updateFullSupplier(Supplier supplier);

    public List<Supplier> getSupplierByQunarName(String name);

    public List<SupplierWithRobot> getSupplierWithRobotByQunarName(String name);

    public List<SupplierWithRobot> getPageSupplierWithRobotByQunarName(String qunarName, int pageNum, int pageSize, int businessId);

    public Long getPageCountSupplier(String qunarName, int businessId);

    Supplier getSupplierByBusiSupplierId(String busiSupplierId,int busiType);

    Supplier getSupplierByBusiSupplierIdEx(String busiSupplierId,int busiType);

    List<SupplierGroupVO> getSuGroupList(List<Long> suIdList);

    List<Supplier> getSupplierBySeatQName(String qName, int bType);
    
    List<Supplier> getSupplierByIds(List<Long> ids);

    int saveSupplierInfo(SupplierInfo supplierInfo);

    List<Supplier> getAllSupplierBySeatQName(String qName);

    Supplier getSupplier(int busi,String busisupplier,long supplier);
}
