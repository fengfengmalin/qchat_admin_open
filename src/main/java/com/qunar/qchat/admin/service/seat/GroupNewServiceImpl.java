package com.qunar.qchat.admin.service.seat;

import com.qunar.qchat.admin.dao.seat.GroupNewDao;
import com.qunar.qchat.admin.util.CollectionUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by yinmengwang on 17-5-31.
 */
@Service
public class GroupNewServiceImpl implements GroupNewService {

    @Resource
    private GroupNewDao groupNewDao;

    @Override
    public boolean deleteGroupBySupplierIds(List<Long> supplierIds) {
        if (CollectionUtil.isEmpty(supplierIds)) {
            return false;
        }
        List<Long> groupIds = groupNewDao.queryGroupIdsBySupplierIds(supplierIds);
        if (CollectionUtil.isEmpty(groupIds)) {
            return true;
        }
        return groupNewDao.deleteGroupBySupplierIds(groupIds) > 0;
    }
}
