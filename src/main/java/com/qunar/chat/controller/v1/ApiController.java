package com.qunar.chat.controller.v1;

import com.qunar.chat.common.Cont;
import com.qunar.chat.common.business.JsonResultVO;
import com.qunar.chat.common.util.JID;
import com.qunar.chat.common.util.JacksonUtils;
import com.qunar.chat.common.util.JsonResultUtil;
import com.qunar.chat.config.Config;
import com.qunar.chat.entity.Seat;
import com.qunar.chat.service.ApiService;
import com.qunar.chat.service.ConsultMessageService;
import com.qunar.chat.service.QueueManagerService;
import com.qunar.chat.service.SeatService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
@RequestMapping(value = "/v1/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    QueueManagerService queueManagerService;

    @Autowired
    ApiService apiService;

    @Autowired
    SeatService seatService;
    @Autowired
    ConsultMessageService consultMessageService;

    /**
     * 关闭用户会话释放客服资源
     * @param customerName 客人名称
     * @param seatName 客服名称
     * @param shopId 店铺id
     * @return
     */
    @RequestMapping("/closeSession.qunar")
    @ResponseBody
    public JsonResultVO<?> closeSession(@RequestParam(value = "customerName") String customerName,
                                        @RequestParam(value = "seatName") String seatName, @RequestParam(value = "shopId") long shopId) {
        if (StringUtils.isNotEmpty(customerName) && !customerName.contains("@")) {
            customerName = String.format("%s@%s", customerName, Config.QCHAT_DEFAULT_HOST);
        }
        if (StringUtils.isNotEmpty(seatName) && !seatName.contains("@")) {
            seatName = String.format("%s@%s", seatName, Config.QCHAT_DEFAULT_HOST);
        }
        queueManagerService.closeSession(JID.parseAsJID(customerName), JID.parseAsJID(seatName), shopId);
        return JsonResultUtil.buildSucceedJsonResult("success");
    }

    /**
     *  新添客服
     * @param seatName 客服用户名
     * @param shopId 店铺id
     * @param webName web 名称
     * @param serviceStatus 服务模式
     * @param maxSessions 最大服务数
     * @return
     */
    @RequestMapping("/saveSeat.qunar")
    @ResponseBody
    public JsonResultVO<?> saveSeat(@RequestParam(value = "seatName") String seatName, @RequestParam(value = "shopId") long shopId,
                                    @RequestParam(value = "webName",required = false) String webName,
                                    @RequestParam(value = "serviceStatus",required = false, defaultValue = "0") int serviceStatus,
                                    @RequestParam(value = "maxSessions",required = false, defaultValue = "10") int maxSessions) {
        logger.info("saveSeat start seatName:{}", seatName);
        return apiService.insertSeat(seatName, shopId, webName, serviceStatus, maxSessions);
    }

    /**
     *  新添店铺
     * @param shopName 店铺名称
     * @return
     */
    @RequestMapping("/saveShop.qunar")
    @ResponseBody
    public JsonResultVO<?> saveShop(@RequestParam(value = "shopName") String shopName){
        logger.info("saveShop start shopName:{}", shopName);
        return apiService.insertSupplier(shopName);
    }

    @RequestMapping("/searchSupplier.qunar")
    @ResponseBody
    public JsonResultVO<?> searchSupplier(@RequestParam(value = "searchKey") String searchKey){
        logger.info("searchSupplier start searchKey:{}", searchKey);
        return apiService.selectSupplierByName(searchKey);
    }


    /**
     * 判断是否有可用客服
     * @param shopId 店铺id
     * @param customerName 客人名称，需有效、以当前host结尾
     * @return
     */
    @RequestMapping("/siftSeat.qunar")
    @ResponseBody
    public JsonResultVO<?> siftSeat(@RequestParam(value = "shopId") long shopId, @RequestParam String customerName) {
        logger.info("siftSeat start shopId:{}", shopId);
        if (!customerName.matches(Cont.ACCOUNT_REGEX) || !customerName.endsWith("@" + Config.QCHAT_DEFAULT_HOST)) {
            JsonResultUtil.buildFailedJsonResult("用户名不合法");
        }

        Seat seat = seatService.siftSeat(shopId);
        if(seat == null) {
            // seat 不存在时不可咨询
            JsonResultUtil.buildFailedJsonResult("没有可用客服");
        }
        return JsonResultUtil.buildSucceedJsonResult(seat);
    }

    /**
     * 消费消息接口
     * @param message 消息体json格式
     * @return
     */
    @RequestMapping(value = "/consumerMsg.qunar", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultVO consumerMsg(@RequestBody String message) {
        logger.info("consumerMsg begin message:{}", message);
        try {
            Map<String, Object> chatMessage = JacksonUtils.string2Map(message);
            if (Cont.CHAT_TYPE.equalsIgnoreCase(MapUtils.getString(chatMessage, "type"))) {
                consultMessageService.processConsultMessage(chatMessage);

            }
        } catch (Exception e) {
            logger.error("consumerMsg message error", message, e);
            return JsonResultUtil.buildFailedJsonResult("consumerMsg error");
        }

        return JsonResultUtil.buildSucceedJsonResult("success");
    }
}
