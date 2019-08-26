package com.qunar.qtalk.ss.sift.enums.shop;

public enum RobotStrategy {
    RSE_INHERIT(0,"继承全局选项"),
    RSE_ROBOT_ADVANCED(1,"机器人优先"),
    RSE_SEAT_ADVANCED(2,"真人优先"),
    RSE_NO_ROBOT(3,"不使用机器人");

    public int code;
    public String desc;

    RobotStrategy(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


}
