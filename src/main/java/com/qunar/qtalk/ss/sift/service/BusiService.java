package com.qunar.qtalk.ss.sift.service;

import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.sift.dao.BusiDao;
import com.qunar.qtalk.ss.sift.entity.Busi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusiService.class);

    @Autowired
    private BusiDao busiDao;

    public Busi queryBusiByID(long id) {
        LOGGER.info("即将通过业务线ID-{}-查询业务线信息。", id);
        Busi busi = busiDao.selectBusiByID(id);
        LOGGER.info("业务线ID-{},查询出来的结果为：{}", id, JacksonUtil.obj2String(busi));
        return busi;
    }
}
