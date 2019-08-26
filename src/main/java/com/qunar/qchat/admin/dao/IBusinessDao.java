package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.vo.BusinessVO;

import java.util.List;

/**
 * Created by qyhw on 10/19/15.
 */
public interface IBusinessDao {

    int getIdByName(String name);

    List<Business> getBusinessesBySupplierId(long supplierId);

    List<BusinessVO> getBusiGroupMappingBySupplierId(long supplierId);

    List<Business> getAllBusiness();
}
