package com.qunar.qchat.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-4-18.
 */
@Data
@JsonIgnoreProperties
public class SupplierInfo {

    // 表supplier信息
    public static final String LOGO_URL = "logo_url";

    public static final String SUPPLIER_TABLE = "supplier";
    public static final List<String> SUPPLIER_COLS = Lists.newArrayList(LOGO_URL);

    // 表busi_supplier_mapping
    public static final String SUPPLIER_OPERATOR = "supplier_operator";
    public static final String OPERATOR_WEBNAME = "operator_webname";

    public static final String BSUM_TABLE = "busi_supplier_mapping";
    public static final List<String> BSUM_COLS = Lists.newArrayList(SUPPLIER_OPERATOR, OPERATOR_WEBNAME);

    private String busiSupplierId;
    private int business;
    private Map<String, Object> columns;

    private Map<String, Object> supplierCols;
    private Map<String, Object> bsumCols;
    private boolean hasSupplierCol;
    private boolean hasBsumCol;

}
