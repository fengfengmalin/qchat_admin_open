package com.qunar.qchat.admin.constants;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
public class QMonitorConstant {

    // 统计次数
    public static final String SUPPLIER_NEW_ADD = "supplier_new_add"; // 供应商新增个数
    public static final String SUPPLIER_NEW_ADD_FAIL = "supplier_new_add_fail"; // 供应商新增失败个数
    public static final String SUPPLIER_CHANGE_NAME = "supplier_change_name"; // 供应商改名个数
    public static final String SUPPLIER_CHANGE_NAME_FAIL = "supplier_change_name_fail"; // 供应商改名失败个数
    public static final String SUPPLIER_MANAGER_UPDATE = "supplier_manager_update"; // 供应商管理员更新次数
    public static final String SUPPLIER_MANAGER_UPDATE_FAIL = "supplier_manager_update_fail"; // 供应商管理员更新失败次数

    public static final String API_SEAT_LIST_INVOKE = "api_seat_list_invoke";// 查询一个供应商下的坐席列表接口的调用次数和响应时间
    public static final String API_SEAT_BATCHLIST_INVOKE = "api_seat_batch_list_invoke";// 查询多个供应商下的坐席列表接口的调用次数和响应时间
    public static final String API_SEAT_ONE_INVOKE = "api_seat_one_invoke";// 查询一个供应商下的单个坐席接口的调用次数和响应时间
    public static final String API_SEAT_ONE_BY_SUPPLIER_INVOKE = "api_seat_by_supplier_one_invoke";// 查询一个供应商下的单个坐席接口的调用次数和响应时间
    public static final String API_SEAT_MORE_ONE_INVOKE = "api_seat_more_one_invoke";// 查询多个供应商下的客服列表接口的调用次数和响应时间
    public static final String API_SEAT_LIST_MORE_SUP_INVOKE = "api_seat_list_more_sup_invoke";// 查询多个供应商下的单个坐席接口的调用次数和响应时间
    public static final String API_SEAT_ONLINE_STATE_INVOKE = "api_seat_online_state_invoke";// 查询客服在线状态的调用次数和时间
    public static final String API_QUERY_INFO_INVOKE = "api_query_info_invoke";// 查询用户或者客服信息的接口的次数和响应时间
    public static final String API_UPDATE_SEAT_INFO_INVOKE = "api_update_seat_info";// 更新客服信息接口调用次数
    public static final String API_START_SESSION = "api_start_session";// 客服最近开始会话的时间
    public static final String API_GET_TOUCH_QCHAT_URL = "api_get_touch_qchat_url";// 获取会话框URL次数
    public static final String API_GET_BUSI_PRODUCT_INFO_TIME = "api_get_busi_product_info_time"; // 获取产品接口用时

    public static final String HOTDOG_API_GET_TOUCH_QCHAT_URL = "hotdog_api_get_touch_qchat_url";// 大客户端获取会话框URL次数
    public static final String HOTDOG_APP_SERVICE = "hotdog_app_service";// 大客户端请求次数
    public static final String JUDGMENT_OR_REDISTRIBUTION = "judgment_or_redistribution";// judgmentOrRedistribution请求次数
    public static final String GET_BUSI_PRODUCT_INFO = "get_busi_product_info";// 获取业务线产品详情信息
    public static final String API_USER_UNREAD_INVOKE = "api_user_unread_invoke";// 获取用户(用户中心)未读消息数
    public static final String API_USER_UNREAD_INFO_INVOKE = "api_user_unread_info_invoke";// 获取用户(用户中心)未读消息的相关信息

    public static final String I_QCMESSAGE_LAST_CONVERSATION = "i_getLastConversationTime"; // 请求一坨商家对一个用户的服务最后的服务时间
    public static final String URL_TIMEOUT_GETONLINEFLAG = "url_timeout_getonlineflag";
    public static final String URL_TIMEOUT_GET_LAST_MERCHANT = "url_timeout_get_last_merchant";
    public static final String URL_TIMEOUT_USERINFOEX = "url_timeout_userinfoEx";
    public static final String URL_TIMEOUT_DUJIA_PDTL = "url_timeout_dujia_pdtl";
    public static final String URL_TIMEOUT_JIJIU_PDTL = "url_timeout_jijiu_pdtl";
    public static final String URL_TIMEOUT_LOCAL_PDTL = "url_timeout_local_pdtl";
    public static final String URL_TIMEOUT_MENPIAO_PDTL = "url_timeout_menpiao_pdtl";
    public static final String SEND_WX_NOTIFY_FAILD = "send_wx_notify_faild";
    public static final String SEND_PRODUCT_QCHAT_NOTE = "send_product_qchat_note";

    /**
     * 统计错误或失败次数
     **/
    public static final String EXCEPTION_5XX = "qchatadmin_5xx_exception"; // 系统发生错误次数统计

    /**
     * 会话转接相关
     */
    public static final String TRANSFER_TO_ALL = "transfer_to_all";
    public static final String TRANSFER_TO_INNER = "transfer_to_inner";
    public static final String TRANSFER_TO_OFFLINE = "transfer_to_offline";
    public static final String TRANSFER_TO_ONLINE = "transfer_to_online";

    /**
     * 微信相关
     */
    public static final String WECHAT_ADD_RELATION = "wechat_add_relation";

    public static final String SAY_HELLO_FROM_SEAT = "say_hello_from_seat"; // 发送欢迎语次数
    public static final String SEND_PRODUCT_NOTE = "send_product_note"; // 发送产品详情note消息次数
}
