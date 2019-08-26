package com.qunar.qchat.admin.model;

import lombok.Data;

/**
 * 供应商
 * Created by qyhw on 10/16/15.
 */
@Data
public class SupplierWithRobot extends Supplier {
    private String robot_id;
    private int strategy;
    private String welcome;
}
