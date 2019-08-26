package com.qunar.qchat.admin.dao.impl;

import com.google.common.collect.Maps;
import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISupplierDao;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.model.SupplierInfo;
import com.qunar.qchat.admin.model.SupplierWithRobot;
import com.qunar.qchat.admin.vo.SupplierGroupVO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/15/15.
 */
@Repository("supplierDao")
public class SupplierDaoImpl extends BaseSqlSessionDao implements ISupplierDao{

    @Override
    public long saveSupplier(Supplier supplier) {
        this.getWriteSqlSession().insert("SupplierMapping.saveSupplier", supplier);
        return supplier.getId();
    }

    @Override
    public long saveSupplierEx(Supplier supplier) {
        this.getWriteSqlSession().insert("SupplierMapping.saveSupplierEx", supplier);
        return supplier.getId();
    }

    @Override
    public int updateSupplier(Supplier supplier) {
        int num = this.getWriteSqlSession().update("SupplierMapping.updateSupplier", supplier);
        return num;
    }

    @Override
    public int updateFullSupplier(Supplier supplier) {
        int num = this.getWriteSqlSession().update("SupplierMapping.updateFullSupplier", supplier);
        return num;
    }


    @Override
    public List<Supplier> getSupplierList() {
        return this.getReadSqlSession().selectList("SupplierMapping.getSupplierList");
    }

    @Override
    public List<Supplier> getSupplierByQunarName(String qunarName) {
        return this.getReadSqlSession().selectList("SupplierMapping.getSupplierByQunarName",qunarName);
    }


    @Override
    public List<SupplierWithRobot> getSupplierWithRobotByQunarName(String qunarName) {
       return this.getReadSqlSession().selectList("SupplierMapping.getSupplierWithRobotByQunarName",qunarName);
    }

    @Override
    public List<SupplierWithRobot> getPageSupplierWithRobotByQunarName(String qunarName, int pageNum, int pageSize, int businessId) {
        Map<String, Object> map = new HashMap<>();
        map.put("qunarName", qunarName);
        map.put("businessId", businessId);
        map.put("pageSize", pageSize);
        map.put("offset", (pageNum - 1) * pageSize);
        return this.getReadSqlSession().selectList("SupplierMapping.getPageSupplierWithRobotByQunarName",map);
    }
    @Override
    public Long getPageCountSupplier(String qunarName, int businessId) {
        Map<String, Object> map = new HashMap<>();
        map.put("qunarName", qunarName);
        map.put("businessId", businessId);
        return this.getReadSqlSession().selectOne("SupplierMapping.getPageCountSupplier",map);
    }

    @Override
    public List<Supplier> getSupplierBySeatQName(String qName, int bType) {
        Map<String, Object> map = new HashMap<>();
        map.put("qName", qName);
        map.put("bType", bType);
        return this.getReadSqlSession().selectList("SupplierMapping.getSupplierBySeatQName", map);
    }

    @Override
    public List<Supplier> getAllSupplierBySeatQName(String qName) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("qName", qName);
        return this.getReadSqlSession().selectList("SupplierMapping.getAllSupplierBySeatQName", map);
    }

    @Override
    public Supplier getSupplierByBusiSupplierId(String busiSupplierId, int busiType) {
        Map<String, Object> map = new HashMap<>();
        map.put("busiSupplierId", busiSupplierId);
        map.put("busiType", busiType);
        return this.getReadSqlSession().selectOne("SupplierMapping.getSupplierByBusiSupplierId",map);
    }

    @Override
    public Supplier getSupplierByBusiSupplierIdEx(String busiSupplierId, int busiType) {
        Map<String, Object> map = new HashMap<>();
        map.put("busiSupplierId", new Long(busiSupplierId));
        map.put("busiType", busiType);
        return this.getReadSqlSession().selectOne("SupplierMapping.getSupplierByBusiSupplierIdEx",map);
    }

    @Override
    public List<SupplierGroupVO> getSuGroupList(List<Long> suIdList) {
        Map<String, Object> map = new HashMap<>();
        map.put("suIdList", suIdList);
        return getReadSqlSession().selectList("SupplierMapping.getSuGroupList", map);
    }

    @Override
    public List<Supplier> getSupplierByIds(List<Long> ids) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("ids", ids);
        return getReadSqlSession().selectList("SupplierMapping.getSupplierByIds", map);
    }

    @Override
    public int saveSupplierInfo(SupplierInfo supplierInfo) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("supplierInfo", supplierInfo);
        return getWriteSqlSession().update("SupplierMapping.saveSupplierInfo", map);
    }

    @Override
    public Supplier getSupplier(int busi, String busisupplier, long supplier) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("busisupplier", busisupplier);
        map.put("busi", busi);
        map.put("supplier", supplier);
        return getReadSqlSession().selectOne("SupplierMapping.getSupplier", map);
    }
}
