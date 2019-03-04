package com.qunar.chat.service;


import com.qunar.chat.common.util.RedisUtil;
import com.qunar.chat.dao.ShopDao;
import com.qunar.chat.dao.UnSentMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SpringComponents {

    public static SpringComponents components;

    @PostConstruct
    public void init() {
        components = this;
    }

    @Autowired
    public RedisUtil redisUtil;

    @Autowired
    public ShopDao shopDao;

    @Autowired
    public ConsultMessageService consultMessageService;

    @Autowired
    public UnSentMessageDao unSentMessageDao;

    @Autowired
    public QueueMappingService queueMappingService;
}
