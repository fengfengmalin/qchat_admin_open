package com.qunar.qchat.admin.vo;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-19
 *
 */
public class OnlineJsonResult {
    public static final int SUCCESS = 0;
    private int ret;
    private String msg;
    private List<SeatOnlineState> strids;


    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<SeatOnlineState> getStrids() {
        return strids;
    }

    public void setStrids(List<SeatOnlineState> strids) {
        this.strids = strids;
    }
}
