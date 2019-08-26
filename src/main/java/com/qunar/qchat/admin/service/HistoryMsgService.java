package com.qunar.qchat.admin.service;

import com.qunar.qtalk.ss.session.dao.IConsultDao;
import com.qunar.qtalk.ss.session.dao.model.ConsultMsgInfo;
import org.apache.commons.collections.CollectionUtils;
import java.util.stream.Collectors;
import java.util.Comparator;
import com.qunar.qchat.admin.vo.conf.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class HistoryMsgService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryMsgService.class);
    @Autowired
    IConsultDao consultDao;

    public JsonData historyMsgSearch(String shopJid, String realFrom, String realTo, int limit, String direction, Timestamp timestamp) {

        List<ConsultMsgInfo> consultMsgs = consultDao.selectHistoryMsgByCondition(shopJid, realFrom, realTo, timestamp, limit, direction);

        if (CollectionUtils.isNotEmpty(consultMsgs)) {
            consultMsgs = consultMsgs.stream().sorted(Comparator.comparing(ConsultMsgInfo::getCreateTime)).collect(Collectors.toList());
        }
//        logger.info("historyMsgSearch result:{}", JacksonUtil.obj2String(consultMsgs));
        return JsonData.success(consultMsgs);

    }
}
