package com.qunar.qchat.admin.service.third.impl;


import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.service.third.IProductService;
import com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins.IProductAdapter;
import com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins.ProductBnbAdapter;
import com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins.ProductCommonAdapter;
import com.qunar.qchat.admin.service.third.impl.ProductInfoPlugins.ProductInterFlightVisaAdapter;
import com.qunar.qchat.admin.vo.third.ProductVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by qyhw on 12/17/15.
 */
@Service("productService")
public class ProductServiceImpl implements IProductService {

    private static LinkedHashMap<String, IProductAdapter> mapofPruductAdapters = new LinkedHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    static {
        IProductAdapter adapter = null;
        adapter = new ProductCommonAdapter();
        mapofPruductAdapters.put(adapter.getBusiEnName(), adapter);

        adapter = new ProductBnbAdapter();
        mapofPruductAdapters.put(adapter.getBusiEnName(), adapter);

        adapter = new ProductInterFlightVisaAdapter();
        mapofPruductAdapters.put(adapter.getBusiEnName(),adapter);
    }



    @Override
    public ProductVO getProduct(HttpServletRequest request,String pdtId, String tEnId, int bType, String tuId, String t3id, String source) {


        if (StringUtils.isEmpty(pdtId) || bType < 1) {
            return null;
        }
        pdtId = pdtId.trim();


        Map<String, String> subParam = new HashMap<>();
        if (!TextUtils.isEmpty(tEnId))
            subParam.put("tEnId", tEnId);
        if (!TextUtils.isEmpty(tuId))
            subParam.put("tuId", tuId);
        if (StringUtils.isNotEmpty(t3id))
            subParam.put("t3id", t3id);

        BusinessEnum businessEnum = BusinessEnum.of(bType);
        if (null != businessEnum && !TextUtils.isEmpty(businessEnum.getEnName())) {
            if (mapofPruductAdapters.containsKey(businessEnum.getEnName())) {
                IProductAdapter adapter = mapofPruductAdapters.get(businessEnum.getEnName());
                return adapter.getProduct(request,bType,source, pdtId, subParam);
            }
        }
        return mapofPruductAdapters.get(ProductCommonAdapter.defaultBusiEnName).getProduct(bType,source,pdtId,subParam);

    }
}
