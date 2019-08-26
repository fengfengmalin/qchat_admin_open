package com.qunar.qchat.admin.controller.inner.api;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.dao.msg.IMsgDao;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.model.request.SendPrompt4PackageBusiRequest;
import com.qunar.qchat.admin.plugins.chatplugin.BaseChatPlugin;
import com.qunar.qchat.admin.plugins.chatplugin.ChatPluginInstance;
import com.qunar.qchat.admin.plugins.chatplugin.IChatPlugin;
import com.qunar.qchat.admin.service.third.INoticeService;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
@Controller
@RequestMapping(value = "/i/api/qcmessage")
public class QCMessageInnerAPIController {

    private static final Logger logger = LoggerFactory.getLogger(QCMessageInnerAPIController.class);

    @Resource
    private IMsgDao msgDao;

    @Resource
    private INoticeService noticeService;

    @ResponseBody
    @RequestMapping(value = "/getLastConversationTime.qunar")
    public JsonResultVO<?> getLastConversationTime(
            @RequestParam(value = "userid", required = true, defaultValue = "") String userid,
            @RequestParam(value = "seat_id_list", required = true ,defaultValue = "") String seatids)
    {
        logger.info("getLastConversationTime -- userid : {} , seatidlist : {} ", userid,seatids);

        if (Strings.isNullOrEmpty(userid))
            return JsonResultUtil.buildFailedJsonResult("userid is empty");

        if (Strings.isNullOrEmpty(seatids))
            return JsonResultUtil.buildFailedJsonResult("seat_id_list is empty ");

        List<String> seatlist = Splitter.on(",").splitToList(seatids);
        if (seatlist.isEmpty())
            return JsonResultUtil.buildFailedJsonResult("seat_id_list parse empty ");

        List<Map<String,Object>> data = null;
        List<Map<String,Object>> resutlData = Lists.newArrayList();
        String domain = EjabdUtil.getUserDomain(userid, QChatConstant.DEFAULT_HOST);
        IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(domain);
        if (null!=plugin && plugin instanceof BaseChatPlugin){
            ((BaseChatPlugin) plugin).setMsgDao(msgDao);
            data = plugin.getLastConversationTime(userid,seatlist);
        }

        if (null != data) {
            for (Map<String,Object> line : data){
                Map<String,Object> resultItem = Maps.newHashMap();
                for (String key : line.keySet()){
                    if ("time".equalsIgnoreCase(key) )
                        resultItem.put(key,line.get(key));
                    if ("m_from".equalsIgnoreCase(key) )
                        resultItem.put("userid",line.get(key));
                    if ("m_to".equalsIgnoreCase(key) )
                        resultItem.put("seatid",line.get(key));
                }
                resutlData.add(resultItem);
            }
        }

        return JsonResultUtil.buildSucceedJsonResult(resutlData);
    }
    @ResponseBody
    @RequestMapping(value = "/sendPrompt4PackageBusi.qunar",method = RequestMethod.POST)
    public JsonResultVO<?> sendPrompt4PackageBusi(
            @RequestBody SendPrompt4PackageBusiRequest requestBody
    )
    {
        if (null == requestBody){
            return JsonResultUtil.buildFailedJsonResult("参数格式错误");
        }
        if (Strings.isNullOrEmpty(requestBody.userid) || Strings.isNullOrEmpty(requestBody.seatid)){
            return JsonResultUtil.buildFailedJsonResult("参数错误");
        }

        // 给商家发送一条通知
        // String callbackurl= requestBody.url;

        String seat = EjabdUtil.makeSureUserJid(requestBody.seatid, QChatConstant.QCHAR_HOST);
        String user   = EjabdUtil.makeSureUserJid(requestBody.userid,QChatConstant.QCHAR_HOST);
        String virturalid = EjabdUtil.makeSureUserJid(requestBody.virtualid,QChatConstant.QCHAR_HOST);

        Map<String,Object> jsonBody = Maps.newHashMap();
        if (!Strings.isNullOrEmpty(requestBody.virtualid)){
            jsonBody.put("from",virturalid);
            jsonBody.put("to",user);
            jsonBody.put("realFrom",seat);
            jsonBody.put("realTo",user);
            jsonBody.put("isConsult",true);
            jsonBody.put("consult",QChatConstant.Note.QCHAT_ID_USER2SEAT);
        } else {
            jsonBody.put("from",seat);
            jsonBody.put("to",user);
            jsonBody.put("isConsult",false);

        }


        // 操作列表
        List<Map<String,Object>> oprationList  = Lists.newArrayList();


        if (!Strings.isNullOrEmpty(requestBody.text)){
            Map<String,Object> textNode = Maps.newHashMap();
            textNode.put("type","text");
            textNode.put("str",requestBody.text);
            textNode.put("strColor","#333333");
            oprationList.add(textNode);
        }

        if (!Strings.isNullOrEmpty(requestBody.url)){
            Map<String,Object> textNode = Maps.newHashMap();
            textNode.put("type","request");
            textNode.put("url",requestBody.url);
            textNode.put("str",requestBody.urltext);
            textNode.put("strColor","#26b8f2");
            oprationList.add(textNode);
        }

        jsonBody.put("noticeStr",oprationList);

        String pBody = JacksonUtil.obj2String(jsonBody);

        String host = EjabdUtil.getUserDomain(requestBody.userid,QChatConstant.DEFAULT_HOST);
        IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(host);
        if (null!=plugin){
            plugin.sendThirePresence(requestBody.seatid,requestBody.userid,"99",pBody);
        }


         return JsonResultUtil.buildSucceedJsonResult("success");
    }

    @ResponseBody
    @RequestMapping(value = "/sendPackageInfo4PackageBusi.qunar",method = RequestMethod.POST)
    public JsonResultVO<?> sendPackageInfo4PackageBusi(
            @RequestBody SendPrompt4PackageBusiRequest requestBody
    )
    {
        if (null == requestBody){
            return JsonResultUtil.buildFailedJsonResult("参数格式错误");
        }
        if (Strings.isNullOrEmpty(requestBody.userid) || Strings.isNullOrEmpty(requestBody.seatid) || Strings.isNullOrEmpty(requestBody.packageInfo)){
            return JsonResultUtil.buildFailedJsonResult("参数错误");
        }
        boolean ret = noticeService.sendConversationNoticeMessage(requestBody.packageInfo,requestBody.userid,requestBody.seatid,requestBody.virtualid);

        if (ret)
            return JsonResultUtil.buildSucceedJsonResult("success");
        else
            return JsonResultUtil.buildFailedJsonResult("");

    }

}
