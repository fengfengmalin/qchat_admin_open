package com.qunar.qchat.admin.annotation.routingdatasource;

/**
 * Created by yinmengwang on 17-4-11.
 */
public enum DataSources {
    /**
     * qcadmin主库
     */
    QCADMIN_MASTER("qcadmin-master"),

    /**
     * TTS从库
     */
    QCADMIN_SLAVE("qcadmin-slave"),

    /**
     * 聊天消息库
     */
    MSG_MASTER("msg-master"),

    /**
     * qtalk 消息库
     */
    QTALK_MSG_SLAVE("qtalk-msg-slave"),

    /**
     * qchat登陆信息库
     */
    QCHAT_MASTER("qchat-master")

    ;

    /**
     * Spring中配置的数据源的id
     */
    private String key;

    private DataSources(String key) {this.key = key;}

    public String key(){return key;}
}
