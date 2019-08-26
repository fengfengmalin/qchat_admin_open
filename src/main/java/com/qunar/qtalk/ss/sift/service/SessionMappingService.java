package com.qunar.qtalk.ss.sift.service;


import com.google.common.collect.Maps;
import com.qunar.qtalk.ss.sift.dao.SessionMappingDao;
import com.qunar.qchat.admin.vo.conf.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SessionMappingService {
    @Autowired
    SessionMappingDao sessionMappingDao;

    public JsonData queryYesterdaySessionCount(Long shopId, String dateString) {
        int sessionCount = sessionMappingDao.selectYesterdaySessionCount(shopId, dateString);
        int queueCount = sessionMappingDao.selectYesterdayQueueCount(shopId, dateString);
        Map<String, Object> result = Maps.newConcurrentMap();
        result.put("sessionCount", sessionCount + queueCount);
        return JsonData.success(result);
    }

}
