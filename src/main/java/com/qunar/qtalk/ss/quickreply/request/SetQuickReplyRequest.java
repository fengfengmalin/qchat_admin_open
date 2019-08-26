package com.qunar.qtalk.ss.quickreply.request;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/7/26
 */
public class SetQuickReplyRequest {

    public String username;
    public String host;
    public long groupver;
    public long contentver;
    public List<AddQuickReply> add;
    public UpdateQuickReply update;
    public UpdateQuickReply delete;

    public static class AddQuickReply {
        public long sgid;//服务器id，如果存在，不插入
        public String groupname;
        public long groupseq;
        public String cgid;//client group id客户端对应组id，配合pc做本地缓存使用
        public List<ContentInfo> contents;
    }

    public static class UpdateQuickReply {
        public List<GroupInfo> groups;
        public List<ContentInfo> contents;
    }

    public static class GroupInfo {
        public long groupid;
        public String groupname;
        public long groupseq;
    }

    public static class ContentInfo {
        public long contentid;
        public String cgid;//client group id客户端对应组id，配合pc做本地缓存使用
        public String ccid;//client content id客户端对应内容id，配合pc做本地缓存使用
        public String content;
        public long contentseq;
    }
}
