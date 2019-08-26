package com.qunar.qtalk.ss.sift.service;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.sift.dao.HotlineSupplierMappingDao;
import com.qunar.qtalk.ss.sift.entity.BusiShopMapping;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.HotlineSupplierMapping;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qchat.admin.vo.conf.JsonData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotlineSupplierService {

    private static final Logger logger = LoggerFactory.getLogger(HotlineSupplierService.class);

    @Autowired
    HotlineSupplierMappingDao hotlineSupplierMappingDao;

    @Autowired
    ShopService shopService;
    @Autowired
    BusiShopMapService busiShopMapService;
    @Autowired
    CsrService csrService;

    public JsonData insertHotlineSeat(HotlineSupplierMapping hotlineSupplierMapping) {

        if (hotlineSupplierMappingDao.insertHotlineSeat(hotlineSupplierMapping) == 1) {
            logger.info("insertHotlineSeat success");
        }

        return JsonData.success("success");
    }

    public JsonData updateHotlineSeat(String hotline, long id) {
        if (hotlineSupplierMappingDao.updateHotlineSeat(hotline, id) == 1) {
            logger.info("updateHotlineSeat success");
        }
        return JsonData.success("success");
    }

    public JsonData deleteHotlineSeat(long id) {
        if (hotlineSupplierMappingDao.deleteById(id) == 1) {
            logger.info("deleteHotlineSeat success");
        }

        return JsonData.success("success");
    }

    public String selectHotlineBySupplierId(JID shopJid) {
        if(shopJid == null || StringUtils.isEmpty(shopJid.getNode()))
            return null;
        String node = shopJid.getNode();
        if (node.startsWith("shop_"))
            node = node.replace("shop_", "");
        long supplierId = StringUtils.isNumeric(node) ? Long.parseLong(node) : 0;
        String hotline = hotlineSupplierMappingDao.selectHotlineBySupplierId(supplierId);
        logger.debug("selectHotlineBySupplierId param:{} result:{}", shopJid, hotline);
        return hotline;
    }
    public List<String> selectHotlineByQunarNameAndHost(String qunarName, String host) {

        List<String> hotline = hotlineSupplierMappingDao.selectHotlineByQunarNameAndHost(qunarName, host);
        logger.debug("selectHotlineBySupplierId param:{} result:{}", qunarName, hotline);
        hotline = CollectionUtils.isNotEmpty(hotline) ? hotline : new ArrayList<>();
        return hotline;
    }

    public String selectHotlineBySupplierId(long supplierId) {
        String hotline = hotlineSupplierMappingDao.selectHotlineBySupplierId(supplierId);
        logger.debug("selectHotlineBySupplierId result:{}", hotline);
        return hotline;
    }

    public JsonData saveNewHotlineMapping(String hotline, String supplierName, String seatName, String webName) {
        Shop shop = new Shop();
        shop.setName(supplierName);
        shop.setWelcomes("您好");
        boolean saveShopSuccess = shopService.saveShop(shop);
        if (saveShopSuccess) {
            Long shopId = shop.getId();
            logger.info(shopId + "--------");
            BusiShopMapping busiShopMap = new BusiShopMapping();
            busiShopMap.setShopID(shopId);
            busiShopMap.setBusiID(BusinessEnum.QTALK.getId());
            boolean saveBusiShopMapping = busiShopMapService.saveBusiShopMapping(busiShopMap);
            HotlineSupplierMapping hotlineSupplierMap = new HotlineSupplierMapping();
            if (StringUtils.isEmpty(hotline)) {
                hotline = shopId.toString();
            }
            if (!hotline.endsWith(QChatConstant.QTALK_DOMAIN_POSTFIX)) {
                hotline += QChatConstant.QTALK_DOMAIN_POSTFIX;
            }
            hotlineSupplierMap.setHotline(hotline);
            hotlineSupplierMap.setSupplierId(shopId);
            int saveResult = hotlineSupplierMappingDao.insertHotlineSeat(hotlineSupplierMap);
            boolean saveSeatResult = false;
            if (StringUtils.isNotEmpty(seatName) && StringUtils.isNotEmpty(webName)) {
                Seat seat = new Seat();
                seat.setQunarName(seatName);
                seat.setSupplierId(shopId);
                seat.setWebName(webName);
                seat.setHost(QChatConstant.QTALK_HOST);
                seat.setServiceStatus(4);
                saveSeatResult = csrService.saveSeat(seat);
            }
            logger.info("save BusiShopMapResult:{} save hotlineResult:{} saveSeatResult:{}", saveBusiShopMapping, saveResult, saveSeatResult);
        }


        //String hotline = hotlineSupplierMappingDao.selectHotlineBySupplierId(supplierId);

        Map<String,Object> result = new HashMap<>();
        result.put("shopId", shop.getId());
        logger.debug("saveNewHotlineMapping result:{}", hotline);
        return JsonData.success(result);
    }

    public JsonData changeHotlineSeat(String qunarName, long shopId) {
        if (shopId < 1) {
            return JsonData.error("shopId error");
        }
        boolean updateSeat = csrService.updateSeatByShopId(shopId);
        logger.info("updateSeat result:{}", updateSeat);
        List<CSR> csrList = csrService.selectCsrByCsrNameAndShopIdWithoutStatus(qunarName, shopId);
        if (CollectionUtils.isNotEmpty(csrList)) {
            boolean updateSeatStatus = csrService.updateSeatStatusByShopIdAndName(shopId, qunarName);
            logger.info("updateSeatStatus result:{}", updateSeatStatus);
        } else {
            Seat seat = new Seat();
            seat.setQunarName(qunarName);
            seat.setSupplierId(shopId);
            seat.setWebName(qunarName);
            seat.setServiceStatus(4);
            seat.setHost(QChatConstant.QTALK_HOST);
            boolean saveSeatResult = csrService.saveSeat(seat);
            logger.info("saveSeatResult result:{}", saveSeatResult);
        }

        return JsonData.success("success");
    }


//    private boolean sendPresence(String hotline, String seat_id, String category, String body) {
//
//        Map<String, String> presenceParam = Maps.newHashMap();
//        presenceParam.put("hotline", hotline);
//        presenceParam.put("seatId", seat_id);
//        presenceParam.put("category", category);
//        presenceParam.put("data", body);
//
//        String response = HttpClientUtils.newPostJson(Config.QCHAT_INNER_SEND_NOTIFY, JacksonUtils.obj2String(presenceParam));
//
//        logger.info("sendPresence url {} ;data : {}; res : {}", Config.QCHAT_INNER_SEND_NOTIFY, JacksonUtils.obj2String(presenceParam), response);
//
//        if (Strings.isNullOrEmpty(response)) {
//            BaseResponce responce = JacksonUtil.string2Obj(response, BaseResponce.class);
//            if (null != responce && responce.isRet())
//                return true;
//        }
//        return false;
//    }
}
