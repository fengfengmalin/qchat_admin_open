package com.qunar.qchat.admin.vo;

import com.qunar.qchat.admin.model.OnlineState;
import lombok.Data;

/**
 * @Description: SeatStatusVO.java
 * @User: sunfayun
 * @Date: 2017/05/12
 * @Version: 1.0
 */
@Data
public class SeatStatusVO {

    private String name;
    private OnlineState status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OnlineState getStatus() {
        return status;
    }

    public void setStatus(OnlineState status) {
        this.status = status;
    }
}
