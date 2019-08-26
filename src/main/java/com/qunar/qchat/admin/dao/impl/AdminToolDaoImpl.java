package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.controller.admin.ASeatGroupVO;
import com.qunar.qchat.admin.controller.admin.ASeatVO;
import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yhw on 12/01/2016.
 */
@Repository("adminToolDao")
public class AdminToolDaoImpl extends BaseSqlSessionDao{

    public List<ASeatVO> getSeatList(String qunarName) {
        if (StringUtils.isEmpty(qunarName)) return null;
        return getReadSqlSession().selectList("AdminTool.getSeatList",  qunarName.toLowerCase());
    }

    public List<ASeatGroupVO> getSeatGroupList(String qunarName) {
        if (StringUtils.isEmpty(qunarName)) return null;
        return getReadSqlSession().selectList("AdminTool.getSeatGroupList",  qunarName.toLowerCase());
    }

    public List<ASeatGroupVO> getGroupProductList(Integer groupId) {
        if (groupId == null) return null;
        return getReadSqlSession().selectList("AdminTool.getGroupProductList",  groupId);
    }

    public List<ASeatVO> getSeatByBusiSupplier(String busiSupplierId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(busiSupplierId) && supplierId == null) return null;

        if (StringUtils.isNotEmpty(busiSupplierId)) {
            map.put("busiSupplierId", busiSupplierId);
        }

        if (supplierId != null && supplierId > 0) {
            map.put("supplierId", supplierId);
        }

        return getReadSqlSession().selectList("AdminTool.getSeatByBusiSupplier",  map);
    }
}
