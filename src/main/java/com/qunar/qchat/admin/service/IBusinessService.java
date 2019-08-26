package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.vo.BusinessVO;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-26
 *
 */
public interface IBusinessService {

    /**
     * 获取一个供应商的所有业务
     * @param supplierId　在我们数据库里的供应商ID
     * @return 业务
     */
    List<Business> getBusinessesBySupplierId(long supplierId);

    /**
     * 获取供应商所属业务及包含的所有组
     * @param supplierId
     * @return
     */
    List<BusinessVO> getBusiGroupMappingBySupplierId(long supplierId);
}
