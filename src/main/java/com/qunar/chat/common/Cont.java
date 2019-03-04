package com.qunar.chat.common;


public interface Cont {


    String DEFAULT_PRODUCTID = "*";
    String QCADMIN = "qcadmin";
    String ACCOUNT_REGEX = "^[a-z0-9_.]*";
    // 咨询消息类型
    String CHAT_TYPE = "consult";

    interface Note {
        String FROM = "from";
        String TO = "to";
        String IP = "ip";
        String Type = "type";
        String URL = "url";
        String DATA = "data";
        String NOTE = "note";
        String CHAT = "chat";
        String SUBSCRIPTION = "subscription";
        String MESSAGE = "message";
        String MESSAGE_AUTO_REPLY="auto_reply";
        String MESSAGE_NO_UPDATE_MSG_LOG="no_update_msg_log";
        String XMLNS = "xmlns";
        String JABBER_CLIENT = "jabber:client";
        String BODY = "body";
        String ID = "id";
        String MSGTYPE = "msgType";
        String MSGTYPE_NOTE = "11";
        String MSGTYPE_COMM = "1";
        String MATYPE = "maType";
        String MATYPE_WEB = "6";
        String ACTIVE = "active";
        String JABBER_URL = "http://jabber.org/protocol/chatstates";

        String CONSULT = "consult";
        String QCHAT_ID = "qchatid";
        String QCHAT_ID_USER2SEAT = "4";
        String QCHAT_ID_SEAT2USER = "5";
        String CHANNEL_ID = "channelid";
        String REAL_FROM = "realfrom";
        String REAL_TO = "realto";
        String IS_HIDDEN_MSG = "isHiddenMsg";

        String BODY_BACKUPINFO = "backupinfo";
        String BODY_EXTENDINFO = "extendInfo";

        String CARBON_MSG = "carbon_message";
    }
}
