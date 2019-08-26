package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IBusinessDao;
import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.vo.BusinessVO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/19/15.
 */
@Repository("businessDao")
public class BusinessDaoImpl extends BaseSqlSessionDao implements IBusinessDao {

    @Override
    public int getIdByName(String name) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        Integer id = this.getReadSqlSession().selectOne("BusinessMapping.getIdByName", params);
        return id == null ? 0 : id;
    }

    @Override
    public List<Business> getBusinessesBySupplierId(long supplierId) {
        return getReadSqlSession().selectList("BusinessMapping.getBusinessesBySupplierId", supplierId);
    }

    @Override
    public List<BusinessVO> getBusiGroupMappingBySupplierId(long supplierId) {
        return getReadSqlSession().selectList("BusinessMapping.getBusiGroupMappingBySupplierId", supplierId);
    }

    @Override
    public List<Business> getAllBusiness() {
        return getReadSqlSession().selectList("BusinessMapping.getAllBusiness");
    }
}
