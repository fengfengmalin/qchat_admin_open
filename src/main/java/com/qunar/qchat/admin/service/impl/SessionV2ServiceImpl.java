package com.qunar.qchat.admin.service.impl;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.dao.ISessionDao;
import com.qunar.qchat.admin.model.SeatSessionsDetail;
import com.qunar.qchat.admin.model.SessionStateEnum;
import com.qunar.qchat.admin.service.ISessionV2Service;
import com.qunar.qchat.admin.service.third.INoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("sessionV2Service")
public class SessionV2ServiceImpl implements ISessionV2Service {

    private static final Logger logger = LoggerFactory.getLogger(ISessionV2Service.class);
    @Resource(name = "sessionDao")
    private ISessionDao sessionDao;


    @Resource
    private INoticeService noticeService;



    @Override
    public Map<String, SeatSessionsDetail> getSeatSessionsDetail(List<String> seatids, String shop_name) {
        List<SessionStateEnum> sessionStateEnums = Lists.newArrayList();
        sessionStateEnums.add(SessionStateEnum.STATE_ASSIGNED);
       // sessionStateEnums.add(SessionStateEnum.STATE_STOPED);
        return sessionDao.getSessionCounts(seatids, shop_name, sessionStateEnums);
    }

}
