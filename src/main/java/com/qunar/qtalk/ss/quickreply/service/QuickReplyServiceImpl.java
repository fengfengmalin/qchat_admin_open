package com.qunar.qtalk.ss.quickreply.service;

import com.qunar.qchat.admin.annotation.routingdatasource.DataSources;
import com.qunar.qchat.admin.annotation.routingdatasource.RoutingDataSource;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qtalk.ss.quickreply.dao.IQuickReplyDao;
import com.qunar.qtalk.ss.quickreply.entity.QuickReplyContent;
import com.qunar.qtalk.ss.quickreply.entity.QuickReplyGroup;
import com.qunar.qtalk.ss.quickreply.request.SetQuickReplyRequest;
import com.qunar.qtalk.ss.quickreply.result.QuickReplyResult;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/7/26
 */
@Service
@Transactional
public class QuickReplyServiceImpl implements IQuickReplyService {

    @Autowired
    IQuickReplyDao iQuickReplyDao;

    @Override
    @RoutingDataSource(DataSources.QCADMIN_MASTER)
    public QuickReplyResult getQuickReplyList(String username, String host, long groupver, long contentver) {

        long maxgroupver = iQuickReplyDao.selectGroupMaxVersion(username, host);
        List<QuickReplyGroup> groups = iQuickReplyDao.getQuickReplyGroupList(username, host, (groupver > maxgroupver) ? 0 : groupver);

        long maxcontentver = iQuickReplyDao.selectContentMaxVersion(username, host);
        List<QuickReplyContent> contents = iQuickReplyDao.getQuickReplyContentList(username, host, (contentver > maxcontentver) ? 0 : contentver);

        QuickReplyResult quickReplyResult = new QuickReplyResult();
        QuickReplyResult.GroupInfo groupInfo = new QuickReplyResult.GroupInfo();
        groupInfo.version = iQuickReplyDao.selectGroupMaxVersion(username, host);
        groupInfo.groups = groups;
        quickReplyResult.groupInfo = groupInfo;

        QuickReplyResult.ContentInfo contentInfo = new QuickReplyResult.ContentInfo();
        contentInfo.version = iQuickReplyDao.selectContentMaxVersion(username, host);
        contentInfo.contents = contents;
        quickReplyResult.contentInfo = contentInfo;

        return quickReplyResult;
    }


    @Override
    public int updateQuickReply(String username, String host, SetQuickReplyRequest.UpdateQuickReply updateQuickReply) {
        List<SetQuickReplyRequest.GroupInfo> groups = updateQuickReply.groups;
        if(!CollectionUtil.isEmpty(groups)) {
            long maxgroupver = iQuickReplyDao.selectGroupMaxVersion(username, host);
            for(SetQuickReplyRequest.GroupInfo info : groups) {
                iQuickReplyDao.updateGroupQuickReply(username, host, info.groupname, info.groupseq, info.groupid, maxgroupver + 1);
            }

        }
        List<SetQuickReplyRequest.ContentInfo> contents = updateQuickReply.contents;
        if(!CollectionUtil.isEmpty(contents)) {
            long maxcontentver = iQuickReplyDao.selectContentMaxVersion(username, host);
            for(SetQuickReplyRequest.ContentInfo info : contents) {
                iQuickReplyDao.updateContentQuickReply(username, host, info.content, info.contentseq, info.contentid, maxcontentver + 1);
            }
        }

        return 0;
    }

    @Override
    public int insertQuickReply(String username, String host, List<SetQuickReplyRequest.AddQuickReply> list) {
        long version = iQuickReplyDao.selectGroupMaxVersion(username, host);
        long contentver = iQuickReplyDao.selectContentMaxVersion(username, host);
        for(SetQuickReplyRequest.AddQuickReply addQuickReply : list) {
//            long groupid = iQuickReplyDao.insertGroupQuickReply(username, host, addQuickReply.groupname, addQuickReply.groupseq, version);
            if(addQuickReply.sgid > 0 && iQuickReplyDao.selectGroupExits(username, host, addQuickReply.sgid) > 0) {//组已存在，直接插入内容
                if(!TextUtils.isEmpty(addQuickReply.cgid) && addQuickReply.contents != null && addQuickReply.contents.size() > 0) {
                    iQuickReplyDao.insertContentQuickReply(username, host, addQuickReply.sgid, addQuickReply.cgid, addQuickReply.contents, contentver + 1);
                }
            } else {
                if(!TextUtils.isEmpty(addQuickReply.cgid)
                        && !TextUtils.isEmpty(addQuickReply.groupname)) {
                    QuickReplyGroup quickReplyGroup = new QuickReplyGroup();
                    quickReplyGroup.username = username;
                    quickReplyGroup.host = host;
                    quickReplyGroup.groupname = addQuickReply.groupname;
                    quickReplyGroup.groupseq = addQuickReply.groupseq;
                    quickReplyGroup.version = version + 1;
                    quickReplyGroup.cgid = addQuickReply.cgid;
                    iQuickReplyDao.insertGroupQuickReply(quickReplyGroup);
                    if(addQuickReply.contents != null && addQuickReply.contents.size() > 0) {
                        iQuickReplyDao.insertContentQuickReply(username, host, quickReplyGroup.id, quickReplyGroup.cgid, addQuickReply.contents, contentver + 1);
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public int deleteQuickReply(String username, String host, SetQuickReplyRequest.UpdateQuickReply deleteQuickReply) {
        List<SetQuickReplyRequest.GroupInfo> groups = deleteQuickReply.groups;

        long maxgroupver = iQuickReplyDao.selectGroupMaxVersion(username, host);
        long maxcontentver = iQuickReplyDao.selectContentMaxVersion(username, host);
        if(!CollectionUtil.isEmpty(groups)) {
            //删组
            iQuickReplyDao.deleteGroupQuickReply(username, host, groups, maxgroupver + 1);
            //同时删内容
            iQuickReplyDao.deleteContentQuickReplyByGroup(groups, maxcontentver + 1);
        }
        List<SetQuickReplyRequest.ContentInfo> contents = deleteQuickReply.contents;
        if(!CollectionUtil.isEmpty(contents)) {
            iQuickReplyDao.deleteContentQuickReply(username, host, contents, maxcontentver + 1);
        }

        return 0;
    }

}
