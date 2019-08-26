package com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.util.HttpClientUtils;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.URLBuilder;
import com.qunar.qchat.admin.vo.third.ProductVO;
import org.apache.commons.collections.MapUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.qunar.qchat.admin.model.BusinessEnum.INTERFLIGHTVISA;

public class ProductInterFlightVisaAdapter extends ProductBaseAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ProductInterFlightVisaAdapter.class);

    @Override
    public String getBusiEnName() {
        return INTERFLIGHTVISA.getEnName();
    }

    @Override
    public ProductVO getProduct(int bType, String source, String pdtId, Map<String, String> subparams) {
        return getProduct(null,bType,source,pdtId,subparams);
    }

    @Override
    public ProductVO getProduct(HttpServletRequest request, int bType, String source, String pdtId, Map<String, String> subparams) {

        String url = getUrl(bType, source);
        if (TextUtils.isEmpty(url))
            return null;


        try {
            pdtId = URLEncoder.encode(pdtId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("产品id编码出错，pdtId:{}", pdtId, e);
        }
        url = String.format(url,pdtId);
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
            if (Strings.isNullOrEmpty(MapUtils.getString(mapRes, "data"))) {
                logger.info("getProduct 产品id错误,pid:{},bType:{}", pdtId, "");
                return null;
            }

            LinkedHashMap<String, Object> map5 = (LinkedHashMap<String, Object>) mapRes.get("data");
            if (map5 == null || map5.size() <= 0) {
                return null;
            }

            ProductVO pVO = new ProductVO();
            pVO.setBu(BusinessEnum.of(bType).getEnName());
            if (map5.containsKey("imgUrl"))
                pVO.setImageUrl(map5.get("imgUrl").toString());
            if (map5.containsKey("touchDtUrl")){
                String touchurl = map5.get("touchDtUrl").toString();
                String useragent = null;
                if (null!=request){
                    useragent = request.getHeader("User-Agent");
                    List<String> agents = Splitter.on(' ').splitToList(useragent);
                    for (String agent : agents){
                        if (agent.contains("QunariPhone/")){
                            long version = Long.valueOf(agent.substring("QunariPhone/".length()));
                            if (version == 80011146|| version == 80011145 ){
                                URLBuilder builder = new URLBuilder();
                                builder.setHost("QunariPhone://hy");
                                builder.addQuery("url",URLEncoder.encode(touchurl));
                                touchurl = builder.build();
                            }
                            if (version > 80011146) {
                                if (!touchurl.contains("showNav")){
                                    touchurl += (touchurl.contains("?") ? "&showNav=1" : "?showNav=1");
                                }
                            }
                            break;
                        }
                        if (agent.contains("rv:4.10")){
                            if (agent.contains("rv:4.10.22") || agent.contains("rv:4.10.21")){
                                URLBuilder builder = new URLBuilder();
                                builder.setHost("QunariPhone://hy");
                                builder.addQuery("url",URLEncoder.encode(touchurl));
                                touchurl = builder.build();
                            } else {
                                if (!touchurl.contains("showNav")){
                                    touchurl += (touchurl.contains("?") ? "&showNav=1" : "?showNav=1");
                                }
                            }
                            break;
                        }
                    }
                }
                pVO.setTouchDtlUrl(touchurl);
            }
            if (map5.containsKey("price"))
                pVO.setPrice(map5.get("price").toString());
            if (map5.containsKey("title"))
                pVO.setTitle(map5.get("title").toString());
            return pVO;

        } catch (Exception e) {
            logger.error("getProduct ---> 解析数据失败,pid:{},bType:{}", pdtId, "", e);
            return null;
        }
    }

}
