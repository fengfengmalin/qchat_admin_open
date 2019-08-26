package com.qunar.qtalk.ss.sift.service;

import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.sift.dao.BusiShopMappingDao;
import com.qunar.qtalk.ss.sift.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusiShopMapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusiShopMapService.class);

    @Autowired
    private BusiShopMappingDao busiShopMappingDao;


    /***
     * 根据商铺ID查找机器人
     * @param shopID
     * @return
     */
    public BusiShopMapping queryBusiShopMappingByShopID(long shopID) {
        LOGGER.info("即将查询商铺-{}-的业务线对应信息", shopID);
        BusiShopMapping shopMapping = busiShopMappingDao.queryBusiShopMappingByShopID(shopID);
        LOGGER.info("查询该商铺-{}的业务线对应信息，得到的结果为：{}",
                shopID, JacksonUtil.obj2String(shopMapping));
        return shopMapping;
    }

    public BusiShopMapping queryBusiShopMappingByBusiShopIDAndBusiID(int busiID, String busiSupplierID) {
        LOGGER.info("即将通过业务线id-{}和tts id-{}查询商铺结果", busiID, busiSupplierID);
        BusiShopMapping shopMapping = busiShopMappingDao.
                queryBusiShopMappingByBusiShopIDAndBusiID(busiID, busiSupplierID);
        LOGGER.info("{}，{}的查询结果为：{}", busiID, busiSupplierID, JacksonUtil.obj2String(shopMapping));
        return  shopMapping;
    }

    public boolean saveBusiShopMapping(BusiShopMapping busiShopMapping) {
        int saveResult = busiShopMappingDao.saveBusiShopMapping(busiShopMapping);
        LOGGER.info("saveBusiShopMapping param:{} result:{}", busiShopMapping.getShopID(), saveResult);
        return saveResult == 1;
    }
}
