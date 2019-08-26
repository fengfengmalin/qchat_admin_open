package com.qunar.qchat.admin.vo.third;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yinmengwang on 17-2-15.
 */
@Data
@NoArgsConstructor
public class SupplierOperatorInfo {

    private String qunarName;
    private String webName;
    private String nickName;

    private Integer businessLine;   //业务线
    private String busiSupplierId;   //业务线的加密id
    private Long supplierId;    //qcadmin中的supplierId
}
