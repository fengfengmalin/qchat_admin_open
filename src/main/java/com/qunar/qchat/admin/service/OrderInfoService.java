package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.service.third.qchat.model.QChatJsonResult;
import com.qunar.qchat.admin.util.HttpClientUtils;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoService {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrderInfoService.class);

    /***
     * 推送数据
     * @param queryString
     * @return
     */
    public JsonResultVO pushData(String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            LOGGER.error("订单数据推送输参为空");
            return JsonResultUtil.buildFailedJsonResult("订单数据输入参数错误");
        }
        String url = constructDataInfoUrl(queryString, "/orderDataPush.qunar?");
        try {
            LOGGER.info("订单数据推送的url为：{}", url);
            String result = HttpClientUtils.get(url);
            LOGGER.info("订单数据推送-请求URL：{}, 响应结果：{}", url, result);
            QChatJsonResult parseResult = JacksonUtil.string2Obj(result, QChatJsonResult.class);
            if (parseResult != null && parseResult.isRet()) {
                return JsonResultUtil.buildSucceedJsonResultWithTotal(parseResult.getData(), parseResult.getTotal());
            }
        } catch (Exception e) {
            LOGGER.error("订单数据推送-请求URL: {}, 发送异常-{}" , url , e);
        }
        return JsonResultUtil.buildFailedJsonResult("获取数据发生异常");
    }

    public String constructDataInfoUrl(String queryString, String midUrl) {
        String url = "" + midUrl + queryString;
        return url;
    }
}
