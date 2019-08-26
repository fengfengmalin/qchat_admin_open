package com.qunar.qchat.admin.model;

import lombok.Data;

@Data
public class Session {
    private Integer id;
    private String user_name;
    private String seat_name;
    private String shop_name;
    private Integer isrobot_seat;
    private Integer session_state;
    private String session_id;
    private String product_id;
}
