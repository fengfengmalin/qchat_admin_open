package com.qunar.qchat.admin.model;

import lombok.Data;

import java.util.Date;

@Data
public class SeatSessionsDetail {
    private String seat_name;
    private Date last_session_time;
    private Integer session_counts;
}
