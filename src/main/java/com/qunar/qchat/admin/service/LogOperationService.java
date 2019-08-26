package com.qunar.qchat.admin.service;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.dao.LogOperationDao;
import com.qunar.qchat.admin.model.LogEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by yinmengwang on 17-3-24.
 */
@Service
public class LogOperationService {

    @Resource
    private LogOperationDao logOperationDao;

    public void insertLog(LogEntity logEntity) {
        if (logEntity == null) {
            return;
        }
        logOperationDao.insertLog(logEntity);
    }

    public List<LogEntity> queryOperationLog(String operateType, String itemType, String itemId, String itemStr) {
        if (Strings.isNullOrEmpty(operateType) && Strings.isNullOrEmpty(itemId) && Strings.isNullOrEmpty(itemType)
                && Strings.isNullOrEmpty(itemStr)) {
            return null;
        }
        return logOperationDao.queryOperationLog(operateType, itemType, itemId, itemStr);
    }

    public List<LogEntity> querySeatWorkModelLogs(String qunarName) {
        if (Strings.isNullOrEmpty(qunarName)) {
            return null;
        }
        List<LogEntity> logEntities = queryOperationLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_SEAT, null, qunarName);
        for (LogEntity logEntity : logEntities) {
            logEntity.setOperateTimeStr(logEntity.getOperateTime().toString());
        }
        return logEntities;
    }
}
