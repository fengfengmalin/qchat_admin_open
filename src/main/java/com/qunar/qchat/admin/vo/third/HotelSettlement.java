package com.qunar.qchat.admin.vo.third;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yinmengwang on 17-2-13.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties
public class HotelSettlement {
    private Integer cooperationType;
    private String xianfuSettlementStaff;
    private String xianfuSettlementStaffName;
    private Integer bizSys;
    private Integer bizSupplierId;
    private String bizSupplierName;
}