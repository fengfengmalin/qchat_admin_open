package com.qunar.qtalk.ss.quickreply;

import com.qunar.qchat.admin.annotation.RecordAccessLog;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonUtil;
import com.qunar.qtalk.ss.quickreply.request.GetQuickReplyRequest;
import com.qunar.qtalk.ss.quickreply.request.SetQuickReplyRequest;
import com.qunar.qtalk.ss.quickreply.result.QuickReplyResult;
import com.qunar.qtalk.ss.quickreply.service.IQuickReplyService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qtalk.ss.session.model.JsonResult;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import com.qunar.qtalk.ss.utils.JsonResultUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/7/26
 */
@RestController
@RequestMapping("/newapi/quickreply")
public class QuickReplyController {

    private static final Logger logger = LoggerFactory.getLogger(QuickReplyController.class);

    @Autowired
    IQuickReplyService iQuickReplyService;

    @RequestMapping(value = "/quickreplylist.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResult<?> getQuickReplyList(@RequestBody GetQuickReplyRequest getQuickReplyRequest) {

        if(getQuickReplyRequest == null) {
            logger.error("quickreplylist请求参数为null");
            return JsonResultUtils.fail(0,"请求参数错误");
        }
        logger.info("quickreplylist请求参数为 p={}", JacksonUtils.obj2String(getQuickReplyRequest));
        String username = getQuickReplyRequest.username;
        String host = getQuickReplyRequest.host;
        long groupver = getQuickReplyRequest.groupver;
        long contentver = getQuickReplyRequest.contentver;

        if(TextUtils.isEmpty(username)) {
            return JsonResultUtils.fail(0,"用户名不能为空");
        } else if(TextUtils.isEmpty(host)) {
            return JsonResultUtils.fail(0,"域名不能为空");
        } else if (groupver < 0 || contentver < 0) {
            return JsonResultUtils.fail(0,"版本号不能小于0");
        }

        QuickReplyResult quickReplyResult = iQuickReplyService.getQuickReplyList(username, host, groupver, contentver);
        logger.info("quickreplylist返回结果为 result={}", JacksonUtils.obj2String(quickReplyResult));
        if(quickReplyResult != null) {
            return JsonResultUtils.success(quickReplyResult);
        }

        return JsonResultUtils.fail(0,"未查询到任何信息");
    }

    @RequestMapping(value = "/setquickreply.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResult<?> setQuickReply(@RequestBody SetQuickReplyRequest setQuickReplyRequest){
        if(setQuickReplyRequest == null) {
            logger.error("setquickreply请求参数为null");
            return JsonResultUtils.fail(0,"请求参数错误");
        }
        logger.info("setquickreply请求参数为 p={}", JacksonUtils.obj2String(setQuickReplyRequest));

        String username = setQuickReplyRequest.username;
        String host = setQuickReplyRequest.host;
        long groupver = setQuickReplyRequest.groupver;
        long contentver = setQuickReplyRequest.contentver;



        if(TextUtils.isEmpty(username)) {
            return JsonResultUtils.fail(0,"用户名不能为空");
        } else if(TextUtils.isEmpty(host)) {
            return JsonResultUtils.fail(0,"域名不可以为空");
        } else if(groupver < 0 || contentver < 0) {
            return JsonResultUtils.fail(0,"版本号不能小于0");
        }


        List<SetQuickReplyRequest.AddQuickReply> add = setQuickReplyRequest.add;
        SetQuickReplyRequest.UpdateQuickReply update = setQuickReplyRequest.update;
        SetQuickReplyRequest.UpdateQuickReply delete = setQuickReplyRequest.delete;

        if(delete != null) {
            iQuickReplyService.deleteQuickReply(username, host, delete);
        }

        if(!CollectionUtil.isEmpty(add)) {
            iQuickReplyService.insertQuickReply(username, host, add);
        }

        if(update != null) {
            iQuickReplyService.updateQuickReply(username, host, update);
        }

        QuickReplyResult quickReplyResult = iQuickReplyService.getQuickReplyList(username, host, groupver, contentver);
        logger.info("setquickreply返回结果为 result={}", JacksonUtils.obj2String(quickReplyResult));

        if(quickReplyResult != null) {
            return JsonResultUtils.success(quickReplyResult);
        }

        return JsonResultUtils.fail(0,"设置快捷回复失败");
    }

}
