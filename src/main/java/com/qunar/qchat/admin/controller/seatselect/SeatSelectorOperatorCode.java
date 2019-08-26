package com.qunar.qchat.admin.controller.seatselect;

public enum SeatSelectorOperatorCode {
    OK(0,""),
    BUSINOTEXIST(1,"busi not exist"),
    POSTROBOTSELECT(2,"post robot select"),
    NOTSEAT(3,"do not have any seat"),
    SEATCOLLECTEMPTY(4,"seat collect error"),
    CONVESATIONHOLDSECCESS(5,"conversation holed"),
    PREROBOTSELECTD(6,"pre robot selected"),
    NEWREALSEATSELECT(7,"new seat selectd"),
    REALSEATSELECTED(8,"real seat selectd"),
    NOREALSEATSELECTABLE(9,"all seat is not select able")

    ;
    private int code;
    private String desc;

    SeatSelectorOperatorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
