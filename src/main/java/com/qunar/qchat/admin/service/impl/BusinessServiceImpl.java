package com.qunar.qchat.admin.service.impl;

import com.qunar.qchat.admin.dao.IBusinessDao;
import com.qunar.qchat.admin.model.Business;
import com.qunar.qchat.admin.service.IBusinessService;
import com.qunar.qchat.admin.vo.BusinessVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-26
 *
 */
@Service("businessService")
public class BusinessServiceImpl implements IBusinessService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessServiceImpl.class);
    @Resource(name = "")
    private IBusinessDao businessDao;

    @Override
    public List<Business> getBusinessesBySupplierId(long supplierId) {
        if (supplierId <= 0) {
            return null;
        }
        return businessDao.getBusinessesBySupplierId(supplierId);
    }

    @Override
    public List<BusinessVO> getBusiGroupMappingBySupplierId(long supplierId) {
        if (supplierId <= 0) {
            return null;
        }
        return businessDao.getBusiGroupMappingBySupplierId(supplierId);
    }

    public List<Business> getAllBusiness() {
        return businessDao.getAllBusiness();
    }
}
