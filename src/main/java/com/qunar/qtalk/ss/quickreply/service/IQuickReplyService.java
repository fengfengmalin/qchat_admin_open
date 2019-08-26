package com.qunar.qtalk.ss.quickreply.service;

import com.qunar.qtalk.ss.quickreply.request.GetQuickReplyRequest;
import com.qunar.qtalk.ss.quickreply.request.SetQuickReplyRequest;
import com.qunar.qtalk.ss.quickreply.result.QuickReplyResult;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/7/26
 */
public interface IQuickReplyService {

    QuickReplyResult getQuickReplyList(String username, String host, long groupver, long contentver);


    int updateQuickReply(String username, String host, SetQuickReplyRequest.UpdateQuickReply updateQuickReply);

    int insertQuickReply(String username, String host, List<SetQuickReplyRequest.AddQuickReply> list);

    int deleteQuickReply(String username, String host, SetQuickReplyRequest.UpdateQuickReply deleteQuickReply);

}
