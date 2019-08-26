package com.qunar.qchat.admin.constants;

/**
 * Created by qyhw on 11/23/15.
 */
public enum BusiResponseCodeEnum {
    SUCCESS("100000","操作成功"),
    SUCCESS_UPDATE("100001","编辑成功"),

    FAIL_REPEAT("200000","重复操作"),
    FAIL_NOT_EXISTS("200001","不存在"),
    FAIL_AUTH("200002","认证失败"),
    FAIL_PARAM_INVALID("200003","参数错误"),
    FAIL_NOT_FOUND_RESULT("200004","没有获取到返回结果"),
    IP_LIMIT("200005","IP限制"),
    UALIMIT_LIMIT("200006","IP限制"),
    FAIL_SERVER_EXCEPTION("500000","服务器异常");

    private String code;
    private String msg;
    private BusiResponseCodeEnum(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
