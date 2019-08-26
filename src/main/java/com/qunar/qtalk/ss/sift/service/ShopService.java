package com.qunar.qtalk.ss.sift.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.util.IDEncryptor;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.sift.dao.ShopDao;
import com.qunar.qtalk.ss.sift.entity.Shop;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ShopService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShopService.class);

    @Autowired
    private ShopDao shopDao;

    public Shop selectShopById(long shopID) {
        LOGGER.info("通过shop ID-{}-查询商铺。", shopID);
        Shop shop = shopDao.selectShopById(shopID);
        LOGGER.info("shop ID-{}-查询到的结果为：{}",
                shopID, JacksonUtil.obj2String(shop));
        return shop;
    }

    public Shop selectShopByBsiIdAndBusiSupplierId(int busiId, String busiSupplierId) {

        Shop shop = shopDao.selectShopByBsiIdAndBusiSupplierId(busiId, busiSupplierId);
        LOGGER.info("busiId:{} busiSupplierId:{} 查询到的结果为：{}", busiId,
                busiSupplierId, JacksonUtil.obj2String(shop));
        return shop;
    }

    public JsonData selectBusiSupplierIds(int busiId, List<Integer> shopIds) {

        Map<String, Integer> mapping = Maps.newConcurrentMap();
        List<String> busiSupplierIds = Lists.newArrayList();

        for (Integer originalId : shopIds) {
            // 原始supplierId encode后对应数据库busi_supplier_id
            String busiSupId = IDEncryptor.encode(originalId);
            mapping.put(busiSupId, originalId);
            busiSupplierIds.add(busiSupId);
        }
        List<String> busiSupplierIdList = shopDao.selectBusiSupplierIds(busiId, busiSupplierIds);
        Map<Integer, Boolean> mapResult = Maps.newConcurrentMap();
        for (String busiSupplierId : busiSupplierIdList) {
            if (mapping.containsKey(busiSupplierId)) {
                mapResult.put(mapping.get(busiSupplierId), true);
            } else {
                mapResult.put(mapping.get(busiSupplierId), false);
            }
        }
        busiSupplierIds.removeAll(busiSupplierIdList);
        if (CollectionUtils.isNotEmpty(busiSupplierIds)) {
            for (String busiSupplierId : busiSupplierIds) {
                if (mapping.containsKey(busiSupplierId)) {
                    mapResult.put(mapping.get(busiSupplierId), false);
                }
            }
        }

        LOGGER.debug("busiId:{} busiSupplierIds:{} 查询到的结果为：{}", busiId,
                JacksonUtil.obj2String(busiSupplierIds), JacksonUtil.obj2String(busiSupplierIdList));
        return JsonData.success(mapResult);
    }


    public String selectShopsByBusiIds(List<Integer> list) {
        String s = shopDao.selectShopsByBusiIds(list);
        LOGGER.debug("selectShopsByBusiId result:{}", s);
        return s == null ? "" : s;
    }

    public List<Shop> selectShopByBsiId(int busiId) {

        List<Shop> shops = shopDao.selectShopByBsiId(busiId);
        LOGGER.info("busiId:{}  查询到的结果为：{}", busiId, JacksonUtil.obj2String(shops));
        return shops;
    }

    public Long selectShopByBsiId(String hotline) {
        Long shopId = shopDao.selectShopIdByHotline(hotline);
        LOGGER.info("hotline:{}  查询到的结果为：{}", hotline, shopId);
        return shopId;
    }

    public String selectHotlineByShopId(long shopId) {
        String hotline = shopDao.selectHotlineByShopId(shopId);
        LOGGER.info("shopId:{}  查询到的结果为：{}", shopId, hotline);
        return hotline;
    }

    public List<Long> selectOtherSupplier(Long supplierId) {

        List<Long> longList = shopDao.selectOtherShopIdsByIdAndQunarName(supplierId);
        LOGGER.debug("selectOtherSupplier param:{} result:{}", supplierId, JacksonUtil.obj2String(longList));
        return CollectionUtils.isNotEmpty(longList) ? longList : new ArrayList<>();
    }

    public boolean saveShop(Shop shop) {

        int saveResult = shopDao.saveShop(shop);
        LOGGER.info("saveShop param:{} result:{}", shop.getName(), saveResult);
        return saveResult == 1;
    }
}
