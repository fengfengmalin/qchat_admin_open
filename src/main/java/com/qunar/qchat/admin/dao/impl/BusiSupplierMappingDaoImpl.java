package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IBusiSupplierMappingDao;
import com.qunar.qchat.admin.model.BusiSupplierMapping;
import com.qunar.qchat.admin.vo.third.SupplierOperatorInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by qyhw on 10/19/15.
 */
@Repository("busiSupplierMappingDao")
public class BusiSupplierMappingDaoImpl extends BaseSqlSessionDao implements IBusiSupplierMappingDao {

    @Override
    public long saveBusiSupplierMapping(BusiSupplierMapping busiSupplierMapping) {
        this.getWriteSqlSession().insert("BusiSupplierMapping.saveBusiSupplierMapping", busiSupplierMapping);
        return busiSupplierMapping.getId();
    }

    @Override
    public BusiSupplierMapping getBusiSupplierMappingBySuId(long suId) {
        return this.getReadSqlSession().selectOne("BusiSupplierMapping.getBusiSupplierMappingBySuId", suId);
    }

    @Override
    public List<SupplierOperatorInfo> getOperatorsBySeatQunarName(String seatQunarName) {
        return this.getReadSqlSession().selectList("BusiSupplierMapping.getOperatorsBySeatQunarName", seatQunarName);
    }

    @Override
    public int saveSupplierOperator(SupplierOperatorInfo supplierOperatorInfo) {
        return this.getWriteSqlSession().insert("BusiSupplierMapping.saveSupplierOperator", supplierOperatorInfo);
    }
}
