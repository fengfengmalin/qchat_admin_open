package com.qunar.qchat.admin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.BusiSupplierMapping;
import com.qunar.qchat.admin.model.product.HistoryProduct;
import com.qunar.qchat.admin.service.IHistoryProductService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-4-26.
 */
@Slf4j
@Service
public class HistoryProductService implements IHistoryProductService {

    @Resource
    private ISupplierService supplierService;

    private static final String SUPPLIER_PRE = "shop_";

    @Override
    public HistoryProduct getLastProduct(String seatQName, String userQName) {
        if (Strings.isNullOrEmpty(seatQName) || Strings.isNullOrEmpty(userQName)) {
            return null;
        }
        Map<String, String> param = Maps.newHashMap();
        param.put("customer", userQName);
        // 店铺id
        if (seatQName.startsWith(SUPPLIER_PRE)) {
            long supplierId = Long.valueOf(seatQName.replace(SUPPLIER_PRE, ""));
            BusiSupplierMapping supplier = supplierService.getSupplierBySupplierId(supplierId);
            if (supplier != null) {
                param.put("busi_id", String.valueOf(supplier.getBusiId()));
                param.put("supplier_id", supplier.getBusiSupplierId());
            }
        } else {
            param.put("kefu", seatQName);
        }

        String response = "";
        if (Strings.isNullOrEmpty(response)) {
            return null;
        }
        PdtResult pdtResult = JacksonUtils.string2Obj(response, PdtResult.class);
        if (pdtResult != null && pdtResult.getRet() == PdtResult.SUCCESS) {
            if (pdtResult.getData() != null) {
                HistoryProduct historyProduct = JacksonUtils.string2Obj(JacksonUtils.obj2String(pdtResult.getData()),
                        HistoryProduct.class);
                return historyProduct;
            }
        }
        return null;
    }

    @Override
    public List<HistoryProduct> getProductHistory(String userQName) {
        if (Strings.isNullOrEmpty(userQName)) {
            return null;
        }
        Map<String, String> param = Maps.newHashMap();
        param.put("customer", userQName);
        String response = "";
        if (Strings.isNullOrEmpty(response)) {
            return null;
        }
        PdtResult pdtResult = JacksonUtils.string2Obj(response, PdtResult.class);
        if (pdtResult != null && pdtResult.getRet() == PdtResult.SUCCESS) {
            if (pdtResult.getData() != null) {
                List<HistoryProduct> historyProducts = JacksonUtils.string2Obj(JacksonUtils.obj2String(pdtResult.getData()),
                        new TypeReference<List<HistoryProduct>>() {
                        });
                return historyProducts;
            }
        }
        return null;
    }
}

@Data
class PdtResult {
    public static final int SUCCESS = 1;
    private int ret;
    private String msg;
    private Object data;
}