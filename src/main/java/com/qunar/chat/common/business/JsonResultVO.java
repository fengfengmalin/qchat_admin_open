package com.qunar.chat.common.business;

/**
 * Created by qyhw on 10/19/15.
 */
public class JsonResultVO<T> {

    private boolean ret;
    private Integer total;
    private String code = "";
    private String msg;
    private T data;

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public enum ResultStatus{
        SUCCESS(true),FAILED(false);

        private boolean value;

        private ResultStatus(boolean ret) {
            this.value = ret;
        }

        public boolean getValue(){
            return value;
        }
    }

    public JsonResultVO()
    {
        this.ret = ResultStatus.SUCCESS.getValue();
        this.msg = "";
    }
    
    public JsonResultVO(ResultStatus ret, String msg, T data) {
        this.ret = ret.getValue();
        this.msg = msg;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean getRet() {
        return ret;
    }

    public void setRet(ResultStatus ret) {
        this.ret = ret.getValue();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
