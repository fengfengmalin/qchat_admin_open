package com.qunar.qchat.admin.vo.hotdog;

import com.qunar.qtalk.ss.utils.CustomRuntimeException;

/**
 * APP CODE Enum，目前给IOS应用使用
 * @author tbq0710 2013-08-02
 *
 */
public enum AppCodeEnum {
	SUCCESS(0,"成功"),
	FAILE(-1, "系统忙，请稍后再来"),//后台服务出现错误
	UNQUALIFIED(-2,"用户需要登录，但是未登录"),
	BNULL(-3, "请求中传递的b参数为空"),
	CNULL(-4, "请求中传递的c参数为空"),
	INVALID_IP(-5,"ip未在白名单"),
    PARAMETER_INVALID(-6, "业务参数不合法"),
    SEAT_NOT_FOUND(-7,"客服不存在"),
    NEED_YZM(-201,"需要验证码"),
    YZM_ERROR(-202,"验证码输入错误"),
    ;
	
	private int code;
	private String desc;
	private AppCodeEnum(int code, String desc) {
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
	
	public static AppCodeEnum valueOf(int code){
        for(AppCodeEnum e: AppCodeEnum.values()){
            if(e.code == code){
                return e;
            }
        }
		throw new CustomRuntimeException("AppCodeEnum不支持这种类型,【code: "+code+"】");
	}
}
