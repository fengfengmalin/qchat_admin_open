package com.qunar.qtalk.ss.quickreply.result;

import com.qunar.qtalk.ss.quickreply.entity.QuickReplyContent;
import com.qunar.qtalk.ss.quickreply.entity.QuickReplyGroup;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/7/26
 */
public class QuickReplyResult {

    public GroupInfo groupInfo;
    public ContentInfo contentInfo;

    public static class GroupInfo {
        public long version;
        public List<QuickReplyGroup> groups;
    }

    public static class ContentInfo {
        public long version;
        public List<QuickReplyContent> contents;

    }


}
