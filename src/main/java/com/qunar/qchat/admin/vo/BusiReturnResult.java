package com.qunar.qchat.admin.vo;

/**
 * Created by qyhw on 11/23/15.
 */
public class BusiReturnResult<T> {

    private boolean ret;
    private String code;
    private String msg;
    private T data;

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
