package com.qunar.qchat.admin.vo.third;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yinmengwang on 17-2-10.
 */
@Data
@NoArgsConstructor
public class HotelSettlementRet {
    private boolean ret;
    private String errmsg;
    private HotelSettlement data;

}
