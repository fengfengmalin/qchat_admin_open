package com.qunar.qchat.admin.vo;

/**
 * 供应商
 * Created by qyhw on 10/19/15.
 */
public class SupplierRobotVO {
    private String robotname;
    private int robotStrategy;
    private String robotWebcome;

    public String getRobotname() {
        return robotname;
    }

    public void setRobotname(String robotname) {
        this.robotname = robotname;
    }

    public int getRobotStrategy() {
        return robotStrategy;
    }

    public void setRobotStrategy(int robotStrategy) {
        this.robotStrategy = robotStrategy;
    }

    public String getRobotWebcome() {
        return robotWebcome;
    }

    public void setRobotWebcome(String robotWebcome) {
        this.robotWebcome = robotWebcome;
    }
}
