package com.qunar.qchat.admin.dao;

import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.LogEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-3-24.
 */
@Repository
public class LogOperationDao extends BaseSqlSessionDao {

    public int insertLog(LogEntity logEntity) {
        return getWriteSqlSession().insert("LogOperateMapper.insertLog", logEntity);
    }

    public List<LogEntity> queryOperationLog(String operateType, String itemType, String itemId, String itemStr) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("operateType", operateType);
        param.put("itemType", itemType);
        param.put("itemId", itemId);
        param.put("itemStr", itemStr);
        return getReadSqlSession().selectList("LogOperateMapper.queryLogs", param);
    }
}
