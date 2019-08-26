package com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.ConfigConstants;
import com.qunar.qchat.admin.util.HttpClientUtils;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.third.ProductVO;
import org.apache.commons.collections.MapUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductCommonAdapter extends ProductBaseAdapter{

    private static final Logger logger = LoggerFactory.getLogger(ProductCommonAdapter.class);


    @Override
    public String getBusiEnName() {
        return defaultBusiEnName;
    }

    @Override
    public ProductVO getProduct(int bType, String source, String pdtId, Map<String, String> subparams) {

        String url = getUrl(bType, source);
        if (TextUtils.isEmpty(url))
            return  null;

        //兼容机票 对产品id进行encode
        try {
            pdtId = URLEncoder.encode(pdtId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("产品id编码出错，pdtId:{}", pdtId, e);
        }
        url = String.format(url, pdtId);
        if (logger.isDebugEnabled()) {
            logger.debug("getProduct ---> http request url : {}", url);
        }


        Map<String, String> form = new HashMap<>();
        if (null != subparams){
            if (subparams.containsKey("tEnId"))
                form.put("tEnId",subparams.get("tEnId"));
            if (subparams.containsKey("tuId")){
                form.put("tuId",subparams.get("tuId"));
            }
            if (subparams.containsKey("t3id")) {
                form.put("t3id", subparams.get("t3id"));
            }
        }

        String jsonRes = HttpClientUtils.post(url, form);
        if (logger.isDebugEnabled()) {
            logger.debug("getProduct ---> from http response result : {}", jsonRes);
        }

        Map<String, Object> mapRes = JacksonUtil.string2Map(jsonRes);
        if (mapRes == null) {
            logger.error("getProduct ---> 请求pdtId: {}, tEnId: {}, bType: {},返回结果：{}", pdtId, "", "", jsonRes);
            return null;
        }

        ProductVO pVO;
        try {
            if (Strings.isNullOrEmpty(MapUtils.getString(mapRes, "data"))) {
                logger.info("getProduct 产品id错误,pid:{},bType:{}", pdtId, "");
                return null;
            }
            LinkedHashMap<String, Object> map5 = (LinkedHashMap<String, Object>) mapRes.get("data");
            if (map5 == null || map5.size() <= 0) {
                return null;
            }
            String dataJson = JacksonUtil.obj2String(map5);
            pVO = JacksonUtil.string2Obj(dataJson, ProductVO.class);
        } catch (Exception e) {
            logger.error("getProduct ---> 解析数据失败,pid:{},bType:{}", pdtId, "", e);
            return null;
        }

        if (pVO == null) {
            return null;
        }

        checkParam(pVO);

        String template = Config.getPropertyInQConfig(ConfigConstants.QCHAT_HTML_TEMPLATE, "<div class=\"qt-meeting-detail\"><ul><li class=\"meeting-img\"><img src=\"{{imageUrl}}\"/></li><li class=\"meeting-content\">{{content}}</li></ul></div>");
        pVO.setpHtml(template.replace("{{imageUrl}}", pVO.getImageUrl()).replace("{{content}}", pVO.getTitle()));
        return pVO;
    }

}
