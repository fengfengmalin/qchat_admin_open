package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.service.OrderInfoService;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/orderInfo")
public class OrderInfoController {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrderInfoController.class);

    @Resource
    OrderInfoService orderInfoService;
    /***
     * 推送订单数据的接口
     */
    @RequestMapping(value = "/orderDataPush.qunar")
    @ResponseBody
    public JsonResultVO<?> pushOrderData(HttpServletRequest request) {

        LOGGER.info("开始推送订单数据-{}", request.getQueryString());

        String queryString = request.getQueryString();

        JsonResultVO result = orderInfoService.pushData(queryString);

        LOGGER.info("请求-{}的订单数据推送结果为{}",
                JacksonUtil.obj2String(request.getQueryString()), JacksonUtil.obj2String(result));

        return result;
    }
}
