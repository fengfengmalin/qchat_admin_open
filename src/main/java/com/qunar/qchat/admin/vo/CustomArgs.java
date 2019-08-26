package com.qunar.qchat.admin.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yinmengwang on 17-2-16.
 */
@Data
@NoArgsConstructor
public class CustomArgs {

    private String bizSys;
    private String bizSupplierId;
    private String productType;
    private boolean needCallback;

}
