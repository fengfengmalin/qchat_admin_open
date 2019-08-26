package com.qunar.qtalk.ss.quickreply.entity;

/**
 * create by hubo.hu (lex) at 2018/8/1
 */
public class QuickReplyGroup {

    public long id;
    public String username;
    public String host;
    public String groupname;
    public long groupseq;
    public long version;
    public int isdel;
    public String cgid;//client group id客户端对应组id，配合pc做本地缓存使用
}
