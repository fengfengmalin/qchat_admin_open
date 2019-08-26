package com.qunar.qchat.admin.util;

import com.qunar.qchat.admin.common.ApplicationContextHelper;
import com.qunar.qchat.admin.model.LogEntity;
import com.qunar.qchat.admin.service.LogOperationService;

/**
 * Created by yinmengwang on 17-3-24.
 */
public class LogUtil {

    public static void doLog(LogEntity logEntity) {
        if (logEntity == null) {
            return;
        }
        LogOperationService logOperationService = ApplicationContextHelper.popBean(LogOperationService.class);
        logOperationService.insertLog(logEntity);
    }

    public static void doLog(String operateType, String itemType, Integer itemId, String itemStr, String operator,
            String content) {
        LogEntity logEntity = LogEntity.builder().operateType(operateType).itemType(itemType).itemId(itemId)
                .itemStr(itemStr).operator(operator).content(content).build();
        doLog(logEntity);
    }
}
