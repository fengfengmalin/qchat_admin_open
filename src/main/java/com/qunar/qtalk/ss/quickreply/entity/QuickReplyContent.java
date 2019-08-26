package com.qunar.qtalk.ss.quickreply.entity;

/**
 * create by hubo.hu (lex) at 2018/8/1
 */
public class QuickReplyContent {

    public long id;
    public String content;
    public long groupid;
    public long contentseq;
    public long version;
    public int isdel;
    public String cgid;//client group id客户端对应组id，配合pc做本地缓存使用
    public String ccid;//client content id客户端对应内容id，配合pc做本地缓存使用
}
