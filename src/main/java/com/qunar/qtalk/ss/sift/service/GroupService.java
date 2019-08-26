package com.qunar.qtalk.ss.sift.service;

import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.sift.dao.SeatGroupDao;
import com.qunar.qtalk.ss.sift.entity.SeatGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

    @Resource(name = "ssSeatGroupDao")
    private SeatGroupDao seatGroupDao;

//    @Autowired
//    private SeatGroupMappingDao seatGroupMappingDao;

    public List<SeatGroup> querySeatGroupsByShopAndProductID(long shopID, String productID) {
        LOGGER.debug("即将通过商铺-{}和产品-{}筛选出座席组。", shopID, productID);
        List<SeatGroup> seatGroups = seatGroupDao.selectGroupListByShopIDAndProductID(shopID, productID);
        LOGGER.info("商铺-{}， 产品-{}，筛选出来的座席组结果为：{}",
                shopID, productID, JacksonUtil.obj2String(seatGroups));
        return seatGroups;
    }

    public List<SeatGroup> queryDefaultGroupsByShopID(long shopID) {
        LOGGER.debug("即将通过商铺-{}筛选出默认组", shopID);
        List<SeatGroup> seatGroups = seatGroupDao.selectDefaultGroupsByShopID(shopID);
        LOGGER.info("该商铺-{}的默认组结果为：{}", shopID, JacksonUtil.obj2String(seatGroups));
        return seatGroups;
    }

//    public List<SeatGroupMapping> queryMappingsByGroupIDs(List<Long> groupIDs) {
//        LOGGER.debug("即将通过组IDs-{}筛选出座席对应表", JacksonUtil.obj2String(groupIDs));
//        List<SeatGroupMapping> mappings = seatGroupMappingDao.selectMappingsByGroupIDs(groupIDs);
//        LOGGER.info("通过组的IDs-{},筛选出的结果为：{}",
//                JacksonUtil.obj2String(groupIDs), JacksonUtil.obj2String(mappings));
//        return mappings;
//    }

//    public List<Long> querySeatIDsByGroupIDs(List<Long> groupIDs) {
//        List<Long> seatIDs = Lists.newArrayList();
//        LOGGER.debug("即将通过组IDs-{}筛选出座席对应表", JacksonUtil.obj2String(groupIDs));
//        List<SeatGroupMapping> mappings = seatGroupMappingDao.selectMappingsByGroupIDs(groupIDs);
//        LOGGER.info("即将通过组IDs-{}筛选出座席对应表", JacksonUtil.obj2String(groupIDs));
//        if (CollectionUtils.isNotEmpty(mappings)) {
//            for (SeatGroupMapping mapping : mappings) {
//                seatIDs.add(mapping.getSeatID());
//            }
//        }
//        LOGGER.info("通过组IDs查询到的座席列表有-{}",
//                JacksonUtil.obj2String(groupIDs), JacksonUtil.obj2String(seatIDs));
//        return seatIDs;
//    }

    public List<Long> querySeatGroupIdByShopIdAndProductId(Long shopId, String productId) {
        LOGGER.debug("即将通过shopId:{} productId:{}筛选客服组列表", shopId, productId);
        List<Long> seatGroupList = null;
        if (StringUtils.isNotEmpty(productId)) {
            seatGroupList = seatGroupDao.selectSeatGroupIdByShopIdAndProductId(shopId, productId);
        }
        LOGGER.info("查询到的客服组列表为：{}", JacksonUtil.obj2String(seatGroupList));

        return seatGroupList;
    }

    public List<Long> queryDefaultSeatGroupIdByShopId(Long shopId) {
        List<Long> seatGroupList = seatGroupDao.selectGroupIdByShopId(shopId);
        LOGGER.info("通过shopId:{} 查询到到客服组为:{}", shopId, JacksonUtil.obj2String(seatGroupList));
        return seatGroupList;
    }

    public List<Long> queryShopIdByProductId(String productId) {
        List<Long> shopIdList = seatGroupDao.selectShopIdByProductId(productId);
        LOGGER.info("productId:{} 查询到到店铺列表为:{}", productId, JacksonUtil.obj2String(shopIdList));
        return shopIdList;
    }
}
