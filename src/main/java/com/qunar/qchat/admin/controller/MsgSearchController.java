package com.qunar.qchat.admin.controller;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.model.third.QChatForTransferParam;
import com.qunar.qchat.admin.service.HistoryMsgService;
import com.qunar.qchat.admin.util.AuthorityUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.session.service.ConsultMessageService;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import com.qunar.qtalk.ss.utils.SendMessage;
import com.qunar.qtalk.ss.utils.common.CacheHelper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Map;

@Controller
@RequestMapping("/msg")
public class MsgSearchController {

    private static final Logger logger = LoggerFactory.getLogger(MsgSearchController.class);

    @Autowired
    HistoryMsgService historyMsgService;

    @Autowired
    private ConsultMessageService consultMessageService;

    @RequestMapping("/historyMsg.json")
    @ResponseBody
    public JsonData historyMsgSearch( QChatForTransferParam param,
                                     @RequestParam(value = "u") String qunarName, String k, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        logger.info("historyMsgSearch: requestParam:{}", JacksonUtil.obj2String(request.getParameterMap()));
//        logger.info("historyMsgSearch cookies:{}", JacksonUtil.obj2String(request.getCookies()));
        if (param == null || Strings.isNullOrEmpty(qunarName)
                || StringUtils.isEmpty(param.getDirection()) || !StringUtils.isNumeric(param.getDirection())) {
            return JsonData.error("参数错误");
        }

        String direction = Integer.parseInt(param.getDirection()) == 0 ? "up" : "down";

        String currentCsr = AuthorityUtil.getThirdPartyUserName(request);
        currentCsr = SendMessage.appendQCDomain(currentCsr);
        String customer = SendMessage.appendQCDomain(param.getFrom());
        String oldCsrName = SendMessage.appendQCDomain(qunarName);
        logger.info("currentCsr:{} customer:{} oldCsrName:{}", currentCsr, customer, oldCsrName);

        int limit =  param.getLimitNum() <= 0 ? 30 : param.getLimitNum();
        String time = param.getTimestamp();
        long timestamp = StringUtils.isNotEmpty(time) && StringUtils.isNumeric(time) ? Long.parseLong(time) * 1000 : System.currentTimeMillis();
        String shopJid = param.getTo();
        if (!shopJid.startsWith("shop_"))
            shopJid = "shop_" + shopJid;
        shopJid = SendMessage.appendQCDomain(shopJid);

        String value = CacheHelper.get(CacheHelper.CacheType.SeatCache, customer + "_" + shopJid, String.class);
        if (StringUtils.isEmpty(value) || !StringUtils.equalsIgnoreCase(value, currentCsr)) {
            return JsonData.error("auth failed");
        }

        shopJid = JID.parseAsJID(shopJid).getNode();

//        logger.info("historyMsgSearch shopJid:{} realFrom:{} realTo:{} limit:{} timestamp:{}", shopJid, customer, oldCsrName, limit, timestamp);
        JsonData jsonData = historyMsgService.historyMsgSearch(shopJid, customer, oldCsrName, limit, direction, new Timestamp(timestamp));
        long costTime = System.currentTimeMillis() - start;
        logger.info("historyMsgSearch cost:{}", costTime);
        return jsonData;

    }

    /**
     * 消费消息接口
     * @param message 消息体json格式
     * @return
     */
    @RequestMapping(value = "/consumerMsg.qunar", method = RequestMethod.POST)
    @ResponseBody
    public JsonData consumerMsg(@RequestBody String message) {
        logger.info("consumerMsg begin message:{}", message);
        try {
            Map<String, Object> chatMessage = JacksonUtils.string2Map(message);
            if (QChatConstant.Note.CONSULT.equalsIgnoreCase(MapUtils.getString(chatMessage, "type"))) {
                consultMessageService.processConsultMessage(chatMessage);

            }
        } catch (Exception e) {
            logger.error("consumerMsg message error", message, e);
            return JsonData.error("consumerMsg error");
        }

        return JsonData.success("success");
    }

}