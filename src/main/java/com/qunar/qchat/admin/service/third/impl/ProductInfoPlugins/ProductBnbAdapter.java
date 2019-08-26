package com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.model.BusinessEnum;
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

import static com.qunar.qchat.admin.model.BusinessEnum.BNB;

public class ProductBnbAdapter extends ProductBaseAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ProductBnbAdapter.class);

    @Override
    public String getBusiEnName() {
        return BNB.getEnName();
    }


    @Override
    public ProductVO getProduct(int bType, String source, String pdtId, Map<String, String> subparams) {

        String url = getUrl(bType, source);
        if (TextUtils.isEmpty(url))
            return null;


        //兼容机票 对产品id进行encode
        try {
            Map<String,Object> data  = JacksonUtil.string2Map(pdtId);
            if (null != data){
                // 说明是json,要encode,不是json就原样传输了
                pdtId = URLEncoder.encode(pdtId, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("产品id编码出错，pdtId:{}", pdtId, e);
        }
        url = String.format(url, pdtId);
        if (logger.isDebugEnabled()) {
            logger.debug("getProduct ---> http request url : {}", url);
        }

        String host = getHost(bType, source);
        Map<String, String> headers = new HashMap<>();
        if (!TextUtils.isEmpty(host))
            headers.put("host", host);
        String jsonRes = HttpClientUtils.getWithHeader(url, headers);

        if (logger.isDebugEnabled()) {
            logger.debug("getProduct ---> from http response result : {}", jsonRes);
        }

        try {
            Map<String, Object> mapRes = JacksonUtil.string2Map(jsonRes);
            if (mapRes == null) {
                logger.error("getProduct ---> 请求pdtId: {}, tEnId: {}, bType: {},返回结果：{}", pdtId, "", "", jsonRes);
                return null;
            }

            if (0 != MapUtils.getInteger(mapRes, "status")) {
                logger.info("getProduct 产品id错误,pid:{},bType:{}", pdtId, "");
                return null;
            }
            if (Strings.isNullOrEmpty(MapUtils.getString(mapRes, "productInfo"))) {
                logger.info("getProduct 产品id错误,pid:{},bType:{}", pdtId, "");
                return null;
            }
            logger.info("getProduct bnb result:{}", jsonRes);

            LinkedHashMap<String, Object> map5 = (LinkedHashMap<String, Object>) mapRes.get("productInfo");
            if (map5 == null || map5.size() <= 0) {
                return null;
            }

            ProductVO pVO = new ProductVO();
            pVO.setBu(BusinessEnum.of(bType).getEnName());
            if (map5.containsKey("ImgUrl"))
                pVO.setImageUrl(map5.get("ImgUrl").toString());
            if (map5.containsKey("price"))
                pVO.setPrice(map5.get("price").toString());
            if (map5.containsKey("roomName"))
                pVO.setTitle(map5.get("roomName").toString());
            if (map5.containsKey("appUrl") && map5.get("appUrl") != null) {
                pVO.setAppDtlUrl(map5.get("appUrl").toString());
                pVO.setTouchDtlUrl(map5.get("appUrl").toString());
            }
            if (map5.containsKey("houseTag") && map5.get("houseTag") != null)
                pVO.setTag(map5.get("houseTag").toString());
            return pVO;

        } catch (Exception e) {
            logger.error("getProduct ---> 解析数据失败,pid:{},bType:{}", pdtId, "", e);
            return null;
        }
    }
}
