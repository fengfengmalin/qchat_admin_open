package com.qunar.chat.service;



import com.qunar.chat.common.business.JsonResultVO;
import com.qunar.chat.common.util.JsonResultUtil;
import com.qunar.chat.config.Config;
import com.qunar.chat.dao.SeatDao;
import com.qunar.chat.dao.ShopDao;
import com.qunar.chat.entity.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiService {
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    @Autowired
    SeatDao seatDao;
    @Autowired
    ShopDao shopDao;

    public JsonResultVO insertSeat(String qunarName, long supplierId, String webName, int serviceStatus, int maxSessions) {

        Shop shop = shopDao.selectShopById(supplierId);
        if (shop == null) {
            return JsonResultUtil.buildFailedJsonResult("supplier not exist");
        }

        int i = seatDao.saveSeat(qunarName, supplierId, Config.QCHAT_DEFAULT_HOST, webName, serviceStatus, maxSessions);
        logger.info("ApiService/insertSeat qunarName:{} result:{}", qunarName, i);
        return JsonResultUtil.buildSucceedJsonResult("success");
    }

    public JsonResultVO insertSupplier(String shopName) {

        Shop shop = shopDao.selectShopByName(shopName);
        if (shop != null) {
            return JsonResultUtil.buildSucceedJsonResult(shop);
        }

        int i = shopDao.insertShop(shopName);
        logger.info("ApiService/insertShop shopName:{} result:{}", shopName, i);
        Shop shopInsert = shopDao.selectShopByName(shopName);
        return JsonResultUtil.buildSucceedJsonResult(shopInsert);
    }

    public JsonResultVO selectSupplierByName(String searchKey) {

        List<String> stringList = shopDao.selectShopId(searchKey);
        logger.info("selectSupplierByName result:{}", stringList.size());
        return JsonResultUtil.buildSucceedJsonResult(stringList);
    }


}
