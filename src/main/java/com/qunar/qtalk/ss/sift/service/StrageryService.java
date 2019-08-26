package com.qunar.qtalk.ss.sift.service;

import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.enums.shop.AssignStragegy;
import com.qunar.qtalk.ss.sift.service.sortstragery.IdleFirstSift;
import com.qunar.qtalk.ss.sift.service.sortstragery.PollingSift;
import com.qunar.qtalk.ss.sift.service.sortstragery.RandomSift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StrageryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrageryService.class);

    @Resource
    private IdleFirstSift idleFirstSift;

    @Resource
    private PollingSift pollingSift;

    @Resource
    private RandomSift randomSift;

    public CSR siftByStragery(List<CSR> csrList, Shop shop) {
        LOGGER.info("开始根据店铺配置-{}-从-{}中筛选策略",
                JacksonUtil.obj2String(shop), JacksonUtil.obj2String(csrList));
        int assignStrategy = shop.getAssignStrategy();
        if (assignStrategy == AssignStragegy.IDLE.code) {
            return idleFirstSift.sift(csrList, shop);
        } else if (assignStrategy == AssignStragegy.POLLING.code) {
            return pollingSift.sift(csrList, shop);
        }
        return randomSift.sift(csrList, shop);
    }
}
