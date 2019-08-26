package com.qunar.qchat.admin.controller.newapi;


import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.service.HotlineSupplierService;
import com.qunar.qtalk.ss.sift.service.SessionMappingService;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/newapi/qcadmin/")
public class NewApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewApiController.class);

    @Autowired
    ShopService shopService;

    @Autowired
    HotlineSupplierService hotlineSupplierService;

    @Autowired
    SessionMappingService sessionMappingService;

    @RequestMapping("/getHotlineShopList.qunar")
    @ResponseBody
    public JsonData getHotlineSeat(@RequestParam(value = "line") String line,
                                   @RequestParam(value = "username") String username,
                                   @RequestParam(value = "host") String host) {
        BusinessEnum businessEnum = BusinessEnum.ofByEnName(line);
        if (businessEnum == null || !StringUtils.equalsAny(host, QChatConstant.QCHAR_HOST, QChatConstant.QTALK_HOST)) {
            return JsonData.error("参数错误");
        }
        List<Shop> shopList = shopService.selectShopByBsiId(businessEnum.getId());
        Map<String, String> allhotlines = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(shopList))
            shopList.stream().forEach(shop -> allhotlines.put( shop.getHotline(), String.format("shop_%s@%s", shop.getId(), host)));
        List<String> hotlineList = hotlineSupplierService.selectHotlineByQunarNameAndHost(username, host);
        Map<String, Object> result = Maps.newHashMap();
        result.put("allhotlines", allhotlines);
        result.put("myhotlines", hotlineList);
        return JsonData.success(result);
    }



    // 添加查询昨天 session 会话数量接口
    @RequestMapping(value = "/querySessionCount.qunar")
    @ResponseBody
    public JsonData querySessionCount(@RequestParam Long shopId, @RequestParam("dateString") String dateString) {
        JsonData jsonData = sessionMappingService.queryYesterdaySessionCount(shopId, dateString);
        LOGGER.info("querySessionCount result:{}", JacksonUtils.obj2String(jsonData));
        return jsonData;
    }


    @RequestMapping("/saveHotline.qunar")
    @ResponseBody
    public JsonData saveHotline( String hotline,
                                @RequestParam(value = "supplierName") String supplierName,
                                @RequestParam(value = "seatName") String seatName,
                                 @RequestParam(value = "seatName") String webName) {
        LOGGER.info("saveHotline begin");
        JsonData mapping = hotlineSupplierService.saveNewHotlineMapping(hotline, supplierName, seatName, webName);
        LOGGER.info("result:{}", JacksonUtils.obj2String(mapping));
        return mapping;
    }

    @RequestMapping("/updateHotlineSeat.qunar")
    @ResponseBody
    public JsonData updateHotlineSeat(
            @RequestParam(value = "seatName") String seatName,
            @RequestParam(value = "shopId") long shopId) {
        LOGGER.info("updateHotlineSeat begin");
        return hotlineSupplierService.changeHotlineSeat(seatName, shopId);
    }

}
