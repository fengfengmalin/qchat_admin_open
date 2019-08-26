package com.qunar.qtalk.ss.sift.service;

import com.qunar.qtalk.ss.consult.QueueMappingDao;
import com.qunar.qtalk.ss.consult.entity.QtSessionKey;
import com.qunar.qtalk.ss.sift.entity.CSR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueueMappingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueMappingService.class);

    @Autowired
    private QueueMappingDao queueMappingDao;

    public void updateInServiceSeatInfo(CSR csr, QtSessionKey key) {

        String customerName = key.getUserName().toBareJID();
        long shopId = key.getShopId();
        int result = queueMappingDao.updateInServiceSeat(csr.getId(), csr.getQunarName().toBareJID(), key.getProductId(), customerName, shopId);

        LOGGER.info("updateInServiceSeatInfo customerName:{} shopId:{} result:{}", customerName, shopId, result);
    }
}
