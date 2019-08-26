package com.qunar.qtalk.ss.quickreply.dao;

import com.qunar.qtalk.ss.quickreply.QuickReplyController;
import com.qunar.qtalk.ss.quickreply.entity.QuickReplyContent;
import com.qunar.qtalk.ss.quickreply.entity.QuickReplyGroup;
import com.qunar.qtalk.ss.quickreply.request.SetQuickReplyRequest;
import com.qunar.qtalk.ss.quickreply.result.QuickReplyResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/7/26
 */
@Component
@Repository
public interface IQuickReplyDao {

    List<QuickReplyGroup> getQuickReplyGroupList(@Param(value = "username") String username,
                                                 @Param(value = "host") String host,
                                                 @Param(value = "version") long version);

    List<QuickReplyContent> getQuickReplyContentList(@Param(value = "username") String username,
                                                     @Param(value = "host") String host,
                                                     @Param(value = "version") long version);


    long selectGroupMaxVersion(
            @Param("username") String username,
            @Param("host") String host
    );

    long selectContentMaxVersion(
            @Param("username") String username,
            @Param("host") String host
    );

    int deleteGroupQuickReply(@Param("username") String username,
                              @Param("host") String host,
                              @Param("list") List<SetQuickReplyRequest.GroupInfo> groups,
                              @Param("groupver") long groupver);

    int deleteContentQuickReplyByGroup(@Param("list") List<SetQuickReplyRequest.GroupInfo> groups,
                                       @Param("contentver") long contentver);

    int deleteContentQuickReply(@Param("username") String username,
                                @Param("host") String host,
                                @Param("list") List<SetQuickReplyRequest.ContentInfo> contents,
                                @Param("contentver") long contentver);

    int updateContentQuickReply(@Param("username") String username,
                                @Param("host") String host,
                                @Param("content") String content,
                                @Param("contentseq") long contentseq,
                                @Param("contentid") long contentid,
                                @Param("contentver") long contentver);

    int updateGroupQuickReply(@Param("username") String username,
                              @Param("host") String host,
                              @Param("groupname") String groupname,
                              @Param("groupseq") long groupseq,
                              @Param("groupid") long groupid,
                              @Param("groupver") long groupver);

    int insertGroupQuickReply(@Param("username") String username,
                              @Param("host") String host,
                              @Param("groupname") String groupname,
                              @Param("groupseq") long groupseq,
                              @Param("version") long version);

    int insertGroupQuickReply(QuickReplyGroup quickReplyGroup);

    int insertContentQuickReply(@Param("username") String username,
                                @Param("host") String host,
                                @Param("groupid") long groupid,
                                @Param("cgid") String cgid,
                                @Param("list") List<SetQuickReplyRequest.ContentInfo> contents,
                                @Param("version") long version);

    int selectGroupExits(@Param("username") String username,
                         @Param("host") String host,
                         @Param("sgid") long sgid);
}
