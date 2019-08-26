package com.qunar.qchat.admin.annotation;

import java.lang.annotation.*;

/**
 * Created by qyhw on 10/19/15.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SeatMustLogin {
	ViewType value();
	public enum ViewType{
		JSON,  //返回JSON格式结果
		JSP,   //返回jsp页面
        VM;    //返回VM页面
	}
}
