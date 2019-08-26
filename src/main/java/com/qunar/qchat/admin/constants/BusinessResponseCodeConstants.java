package com.qunar.qchat.admin.constants;
/**  
 * Created by qyhw on 10/28/15.
 */
public interface BusinessResponseCodeConstants {

	final static int SUCCESS_ADD = -10000; // 添加成功

    final static int ADD_REPETITION = -10001; // 添加重复

    final static int SUCCESS_UPDATE = -10002; // 编辑成功


    final static int FAIL_UCENTER_QUNARNAME_NOT_EXIST = -11000; // qunarName 用户中心不存在
    final static int FAIL_AUTH_OWNER = -11001; // 所属者认证失败

}