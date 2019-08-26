package com.qunar.qchat.admin.model;

public enum RobotStrategyEnum {
    RSE_INHERIT(0,"继承全局选项"),
    RSE_DEFAULT(3,"不使用机器人"),
    RSE_ROBOT_ADVANCED(1,"机器人优先"),
    RSE_SEAT_ADVANCED(2,"真人优先"),
    RSE_NO_ROBOT(3,"不使用机器人")
    ;

    private int value;
    private String desc;

    RobotStrategyEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static RobotStrategyEnum of(Integer id) {
        if (id == null) {
            return null;
        }
        for (RobotStrategyEnum businessEnum : RobotStrategyEnum.values()) {
            if (businessEnum.value == id) {
                return businessEnum;
            }
        }
        return null;
    }

}
