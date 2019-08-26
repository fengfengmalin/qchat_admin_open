package com.qunar.qchat.admin.service.third.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.dao.session.ISeatUserDao;
import com.qunar.qchat.admin.model.qchat.ProductNoteArgs;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.plugins.chatplugin.ChatPluginInstance;
import com.qunar.qchat.admin.plugins.chatplugin.IChatPlugin;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISessionV2Service;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.service.third.INoticeService;
import com.qunar.qchat.admin.service.third.impl.modules.RbtSuggestionListJson;
import com.qunar.qchat.admin.util.EjabdUtil;
import com.qunar.qchat.admin.util.HttpClientUtils;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.third.ProductVO;
import com.qunar.qtalk.ss.consult.ConsultUtils;
import com.qunar.qtalk.ss.sift.entity.BusiShopMapping;
import com.qunar.qtalk.ss.sift.service.BusiShopMapService;
import com.qunar.qtalk.ss.sift.service.GroupService;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import com.qunar.qtalk.ss.utils.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.TextUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by yinmengwang on 17-5-12.
 */
@Slf4j
@Service("noticeService")
public class NoticeServiceImpl implements INoticeService {

    private static final String OUT_OF_WORK_DAY_STRING = "殿下，我们已接到您的需求啦！小驼正在紧锣密鼓跟进中，我们会在工作时间内第一时间给您反馈哒~请稍安勿躁（工作时间：工作日10:00-19:00）";
    private static final String WORK_DAY_STRING = "殿下，您的需求单已提交成功！如有需求补充或变更，欢迎随时与小驼沟通哟~";
    private static final String DEFAULT_WELCOMES = "您好，我是在线客服，很高兴为您服务";

    @Resource
    private ISupplierService supplierService;

    @Resource
    IRobotService robotService;

    @Resource(name = "sessionV2Service")
    private ISessionV2Service sessionV2Service;
    @Resource
    private ISeatUserDao iSeatUserDao;

    @Resource(name = "seatService")
    private ISeatService seatService;

    @Autowired
    GroupService groupService;
    @Autowired
    BusiShopMapService busiShopMapService;



    @Override
    public boolean sendProductNote(ProductVO productVO, ProductNoteArgs args) {
        if (productVO == null || args == null) {
            return false;
        }
        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, args.getUserQName());
        Map<String, Object> msg = Maps.newHashMap();
        msg.put(QChatConstant.Note.IP, args.getIp());
        msg.put(QChatConstant.Note.BU, args.getBu());
        if (Strings.isNullOrEmpty(args.getUrl())) {
            args.setUrl(productVO.getTouchDtlUrl());
        }
        msg.put(QChatConstant.Note.URL, args.getUrl());

        String backupInfo = assembleBackUpInfo(productVO, args);

        // 普通消息
        String response;
        if (Strings.isNullOrEmpty(args.getVirtualId()) || args.getVirtualId().startsWith("bnb_")) {
            log.info("sendProductNote pdtId:{} ProductNoteArgs:{}", productVO.getProductId(), JacksonUtil.obj2String(args));
            param.put(QChatConstant.Note.TO, args.getSeatQName());

            msg.put(QChatConstant.Note.Type, args.getType());
            msg.put(QChatConstant.Note.DATA, productVO);

            String message = JacksonUtils.obj2String(msg);
            param.put(QChatConstant.Note.MESSAGE,
                    createProductMessage(args.getBu(), message, args.getUserQName(), args.getSeatQName(), backupInfo));
            response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        } else {
            param.put(QChatConstant.Note.TO, args.getVirtualId());

            msg.put(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);
            msg.put(QChatConstant.Note.DATA, productVO);

            String messgae = JacksonUtils.obj2String(msg);
            param.put(QChatConstant.Note.MESSAGE,
                    ConsultUtils.createConsultMessage(
                            messgae,
                            args.getUserQName(),
                            args.getUserQName(),
                            args.getVirtualId(),
                            args.getSeatQName(),
                            QChatConstant.Note.QCHAT_ID_USER2SEAT,
                            QChatConstant.Note.MSGTYPE_NOTE,
                            backupInfo, ""));
            response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        }
        if (!Strings.isNullOrEmpty(response)) {
            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);
            if (qChatResult != null && qChatResult.isRet()) {
                productVO.setSendNoteSuccess(true);
                return true;
            }
            String errorMsg = qChatResult != null ? qChatResult.getErrmsg() : "";
            productVO.setSendNoteMsg(errorMsg);
        }
        productVO.setSendNoteSuccess(false);
        log.error("发送产品详情note消息出错,param:{},返回:{}", JacksonUtils.obj2String(param), response);
        return false;
    }

    @Override
    public boolean sendProductNoteBySeat(ProductVO productVO, ProductNoteArgs args) {
        if (productVO == null || args == null) {
            return false;
        }
        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, args.getSeatQName());
        Map<String, Object> msg = Maps.newHashMap();
        msg.put(QChatConstant.Note.IP, args.getIp());
        msg.put(QChatConstant.Note.BU, args.getBu());
        if (Strings.isNullOrEmpty(args.getUrl())) {
            args.setUrl(productVO.getTouchDtlUrl());
        }
        msg.put(QChatConstant.Note.URL, args.getUrl());

        String backupInfo = assembleBackUpInfo(productVO, args);
        String response = null;
        if (QChatConstant.Note.CONSULT.equalsIgnoreCase(args.getType())) {
            param.put(QChatConstant.Note.TO, args.getVirtualId());
            msg.put(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);
            msg.put(QChatConstant.Note.DATA, productVO);
            String messgae = JacksonUtils.obj2String(msg);
            param.put(QChatConstant.Note.MESSAGE,
                    ConsultUtils.createConsultMessage(
                            messgae,
                            args.getSeatQName(),
                            args.getSeatQName(),
                            args.getVirtualId(),
                            args.getUserQName(),
                            QChatConstant.Note.QCHAT_ID_SEAT2USER,
                            QChatConstant.Note.MSGTYPE_NOTE,
                            backupInfo, ""));
            response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        } else {
            log.info("sendProductNoteBySeat pdtId:{} ProductNoteArgs:{}", productVO.getProductId(), JacksonUtil.obj2String(args));
            param.put(QChatConstant.Note.TO, args.getUserQName());

            msg.put(QChatConstant.Note.Type, args.getType());
            msg.put(QChatConstant.Note.DATA, productVO);

            String message = JacksonUtils.obj2String(msg);
            param.put(QChatConstant.Note.MESSAGE,
                    createProductMessage(args.getBu(), message, args.getSeatQName(), args.getUserQName(), backupInfo));
            response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        }
        if (StringUtils.isNotEmpty(response)) {
            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);
            if (qChatResult != null && qChatResult.isRet()) {
                productVO.setSendNoteSuccess(true);
                return true;
            }
            String errorMsg = qChatResult != null ? qChatResult.getErrmsg() : "";
            productVO.setSendNoteMsg(errorMsg);
        }
        productVO.setSendNoteSuccess(false);
        log.error("sendProductNoteBySeat 发送产品详情note消息出错,param:{},返回:{}", JacksonUtils.obj2String(param), response);
        return false;
    }

    @Override
    public boolean sendConsultMessage(
            String message,
            String from,
            String realFrom,
            String to,
            String realTo,
            String backupinfo) {
        if (Strings.isNullOrEmpty(message)) {
            return false;
        }
        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, to);
        param.put(QChatConstant.Note.MESSAGE, ConsultUtils.createConsultMessage(
                message,
                from,
                realFrom,
                to,
                realTo,
                QChatConstant.Note.QCHAT_ID_SEAT2USER,
                QChatConstant.Note.MSGTYPE_COMM,
                backupinfo, "", true, true, true));
        String response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        if (!Strings.isNullOrEmpty(response)) {
            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);
            if (qChatResult != null && qChatResult.isRet()) {
                return true;
            }
        }
        log.error("发送consult消息出错,param:{}  返回：{}", JacksonUtils.obj2String(param), response);
        return false;
    }

//    @Override
//    public boolean sendChatMessage(String message, String from, String to, String seatHost) {
//
//
//        boolean isQTALK = false;
//        if (!Strings.isNullOrEmpty(seatHost) && StringUtils.equals(seatHost, QChatConstant.QTALK_HOST)) {
//            isQTALK = true;
//        }
//
//        if (!isQTALK) {
//            return sendQChatMessage(message, from, to, seatHost);
//        }
//
//        String url = Config.QTALK_SEND_MESSAGE_URL;
//
//        Map<String, String> toMap = Maps.newHashMap();
//        toMap.put(QChatMessageBody.TO_KEY, to);
//        QChatMessageBody noticeBody = QChatMessageBody.builder().from(from).to(Lists.newArrayList(toMap)).body(message)
//                .msgType(QChatMessageBody.MEG_TYPE).extendInfo("").type(QChatMessageBody.CHAT_TYPE)
//                .host(QChatConstant.QCHAR_HOST).carbon("true").build();
//
//        noticeBody.setDomain(QChatMessageBody.QCHAT_DOMAIN);
//
//        String result = HttpClientUtils.newPostJson(url, JacksonUtils.obj2String(Lists.newArrayList(noticeBody)));
//        if (Strings.isNullOrEmpty(result)) {
//            return false;
//        }
//        // qtalk返回的没有ret参数，就直接返回true吧
//        if (isQTALK) {
//            return true;
//        }
//        QchatResult qChatResult = JacksonUtils.string2Obj(result, QchatResult.class);
//        if (qChatResult != null && qChatResult.isRet()) {
//            return true;
//        }
//        if (qChatResult != null) {
//            log.error("发送chat消息失败,from:{},to:{},body:{},errmsg:{}", from, to, message, qChatResult.getErrmsg());
//        }
//        return false;
//    }


    @Override
    public boolean sendSystemNotifyMessage(String msgbody, String to) {
        if (TextUtils.isEmpty(to))
            return false;

        String from = "rbt-system@" + QChatConstant.QCHAR_HOST;

        if (!to.contains("@")) {
            to += "@";
            to += QChatConstant.QCHAR_HOST;
        }

        Document document = DocumentHelper.createDocument();
        Element message = document.addElement(QChatConstant.Note.MESSAGE);
        message.addAttribute(QChatConstant.Note.FROM, from);
        message.addAttribute(QChatConstant.Note.TO, to);
        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.SUBSCRIPTION);

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
        body.addAttribute(QChatConstant.Note.MSGTYPE, "268435456");
        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
        body.addText(msgbody);
        String messageXml = message.asXML();


        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, to);
        param.put("direction", "1");
        param.put(QChatConstant.Note.MESSAGE, messageXml);

//        String response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
//        if (!Strings.isNullOrEmpty(response)) {
//            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);
//            if (qChatResult != null && qChatResult.isRet()) {
//                return true;
//            }
//        }
//        log.error("发送qchat消息出错,param:{},返回:{}", JacksonUtils.obj2String(param), response);
        return false;
    }


    @Override
    public boolean sendConversationNoticeMessage(String message, String from, String to, String virtualid) {
        if (Strings.isNullOrEmpty(message) || Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(to))
            return false;

        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);
        String messageParam = "";
        if (!Strings.isNullOrEmpty(virtualid)) {
            virtualid = EjabdUtil.makeSureUserJid(virtualid, QChatConstant.QCHAR_HOST);
            String msgId = QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", "");
            messageParam = createConsultMessageEx(
                    message,
                    from, from, virtualid, to, msgId,
                    QChatConstant.Note.QCHAT_ID_USER2SEAT, "15", "", "", false);
        } else {
            String msgId = QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", "");
            messageParam = createMessageEx(
                    from, to, message, msgId, "15", false);
        }

        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, Strings.isNullOrEmpty(virtualid) ? to : virtualid);
        param.put(QChatConstant.Note.MESSAGE, messageParam);

        String response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));

        log.info("发送ConversationNoticeMessage,from:{},to:{},msg:{},ret1:{}",
                from, to, messageParam, response);

        if (!Strings.isNullOrEmpty(response)) {
            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);

            if (qChatResult != null && qChatResult.isRet()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sendPromotConsultTextMessage(String message, String from, String to, String virtualid) {

        if (Strings.isNullOrEmpty(message) || Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(to) || Strings.isNullOrEmpty(virtualid))
            return false;


        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);
        virtualid = EjabdUtil.makeSureUserJid(virtualid, QChatConstant.QCHAR_HOST);


        RbtSuggestionListJson jsonObj = new RbtSuggestionListJson();
        RbtSuggestionListJson.ItemEvent itemEvent = new RbtSuggestionListJson.ItemEvent();
        itemEvent.type = "text";
        RbtSuggestionListJson.Item item = new RbtSuggestionListJson.Item();
        item.event = itemEvent;
        item.text = message;
        jsonObj.hints = Lists.newArrayList();
        jsonObj.hints.add(item);

        String exteredInfo = JacksonUtil.obj2String(jsonObj);

        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, virtualid);
        param.put(QChatConstant.Note.MESSAGE,
                ConsultUtils.createConsultMessage(
                        message,
                        from,
                        from,
                        virtualid,
                        to,
                        QChatConstant.Note.QCHAT_ID_USER2SEAT,
                        "65537",
                        "", exteredInfo));

        String response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        if (!Strings.isNullOrEmpty(response)) {
            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);
            if (qChatResult != null && qChatResult.isRet()) {
                return true;
            }
        }
        return false;
    }


    private boolean sendQChatMessage(String message, String from, String to, String seatHost) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to))
            return false;

        if (!from.contains("@")) {
            from += "@";
            from += QChatConstant.QCHAR_HOST;
        }
        if (!to.contains("@")) {
            to += "@";
            to += QChatConstant.QCHAR_HOST;
        }

        Map<String, String> param = Maps.newHashMap();
        param.put(QChatConstant.Note.FROM, from);
        param.put(QChatConstant.Note.TO, to);
        param.put(QChatConstant.Note.MESSAGE,
                createChatMessage(from, to, message));
        String response = HttpClientUtils.newPostJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));
        if (!Strings.isNullOrEmpty(response)) {
            QchatResult qChatResult = JacksonUtils.string2Obj(response, QchatResult.class);
            if (qChatResult != null && qChatResult.isRet()) {
                return true;
            }
        }
        log.error("发送qchat消息出错,param:{},返回:{} host:{}", JacksonUtils.obj2String(param), response, seatHost);
        return false;
    }

    private String createChatMessage(
            String from,
            String to,
            String msgbody) {

        Document document = DocumentHelper.createDocument();
        Element message = document.addElement(QChatConstant.Note.MESSAGE);
        message.addAttribute(QChatConstant.Note.FROM, from);
        message.addAttribute(QChatConstant.Note.TO, to);
        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CHAT);
        message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY, "true");

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
        body.addAttribute(QChatConstant.Note.MSGTYPE, "1");
        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
        body.addText(msgbody);

        return message.asXML();
    }

//    private String createMessage(
//            String from,
//            String to,
//            String msgbody,
//            String msgType
//    ) {
//
//        Document document = DocumentHelper.createDocument();
//        Element message = document.addElement(QChatConstant.Note.MESSAGE);
//        message.addAttribute(QChatConstant.Note.FROM, from);
//        message.addAttribute(QChatConstant.Note.TO, to);
//        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CHAT);
//
//        Element body = message.addElement(QChatConstant.Note.BODY);
//        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
//        body.addAttribute(QChatConstant.Note.MSGTYPE, msgType);
//        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
//        body.addText(msgbody);
//
//        return message.asXML();
//    }

    private String createMessageEx(
            String from,
            String to,
            String msgbody,
            String msgId,
            String msgType,
            boolean carbonFlag
    ) {

        Document document = DocumentHelper.createDocument();
        Element message = document.addElement(QChatConstant.Note.MESSAGE);
        message.addAttribute(QChatConstant.Note.FROM, from);
        message.addAttribute(QChatConstant.Note.TO, to);
        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CHAT);

        if (carbonFlag) {
            message.addAttribute(QChatConstant.Note.CARBON_MSG, "true");
        }

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID,
                Strings.isNullOrEmpty(msgId) ? QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", "") : msgId);
        body.addAttribute(QChatConstant.Note.MSGTYPE, msgType);
        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
        body.addText(msgbody);

        return message.asXML();
    }


    private String createProductMessage(
            String bu,
            String content,
            String from,
            String to,
            String backupinfo) {

        Document document = DocumentHelper.createDocument();
        Element message = document.addElement(QChatConstant.Note.MESSAGE);
        message.addAttribute(QChatConstant.Note.FROM, from);
        message.addAttribute(QChatConstant.Note.TO, to);
        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.NOTE);
        message.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_CLIENT);
        if (!StringUtils.isEmpty(bu)) {
            Map<String, String> channelIdValue = Maps.newHashMap();
            channelIdValue.put("cn", bu);
            channelIdValue.put("d", "send");
            channelIdValue.put("usrType", "usr");
            message.addAttribute(QChatConstant.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));
            message.addAttribute(QChatConstant.Note.BU, bu);
        }

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
        body.addAttribute(QChatConstant.Note.MSGTYPE, "11");
        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
        if (StringUtils.isNotEmpty(backupinfo))
            body.addAttribute(QChatConstant.Note.BODY_BACKUPINFO, backupinfo);
        body.addText(content);

        Element active = message.addElement(QChatConstant.Note.ACTIVE);
        active.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_URL);

        return message.asXML();
    }

//    private String createConsultMessage(
//            String content,
//            String from,
//            String realFrom,
//            String to,
//            String realTo,
//            String chatId,
//            String msgType,
//            String backupinfo,
//            String extInfo) {
//
//        Map<String, String> channelIdValue = Maps.newHashMap();
//        channelIdValue.put("cn", "consult");
//        channelIdValue.put("d", "send");
//        channelIdValue.put("usrType", "usr");
//        Document document = DocumentHelper.createDocument();
//        Element message = document.addElement(QChatConstant.Note.MESSAGE);
//        message.addAttribute(QChatConstant.Note.FROM, from);
//        message.addAttribute(QChatConstant.Note.TO, to);
//        if (!Strings.isNullOrEmpty(realFrom)) {
//            message.addAttribute(QChatConstant.Note.REAL_FROM, realFrom);
//        }
//        if (!Strings.isNullOrEmpty(realTo)) {
//            message.addAttribute(QChatConstant.Note.REAL_TO, realTo);
//        }
//        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);
//        message.addAttribute(QChatConstant.Note.IS_HIDDEN_MSG, "0");
//        message.addAttribute(QChatConstant.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));
//        message.addAttribute(QChatConstant.Note.QCHAT_ID, chatId); // 客人发给qchat客户端为4 qchat客户端给客人发是5
//        message.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_CLIENT);
//        if (!Strings.isNullOrEmpty(msgType) && !msgType.equalsIgnoreCase("11")) {
//            message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY, "true");
//        }
//
//        Element body = message.addElement(QChatConstant.Note.BODY);
//        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
//        body.addAttribute(QChatConstant.Note.MSGTYPE, msgType);
//        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
//        if (!TextUtils.isEmpty(backupinfo)) {
//            body.addAttribute(QChatConstant.Note.BODY_BACKUPINFO, backupinfo);
//        }
//        if (!Strings.isNullOrEmpty(extInfo)) {
//            body.addAttribute(QChatConstant.Note.BODY_EXTENDINFO, extInfo);
//        }
//        body.addText(content);
//
//        Element active = message.addElement(QChatConstant.Note.ACTIVE);
//        active.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_URL);
//
//        return message.asXML();
//    }

//    private String createConsultMessageV2(
//            String content,
//            String from,
//            String realFrom,
//            String to,
//            String realTo,
//            String chatId,
//            String msgType,
//            String backupinfo,
//            String extInfo) {
//
//        Map<String, String> channelIdValue = Maps.newHashMap();
//        channelIdValue.put("cn", "consult");
//        channelIdValue.put("d", "send");
//        channelIdValue.put("usrType", "usr");
//        Document document = DocumentHelper.createDocument();
//        Element message = document.addElement(QChatConstant.Note.MESSAGE);
//        message.addAttribute(QChatConstant.Note.FROM, from);
//        message.addAttribute(QChatConstant.Note.TO, to);
//        if (!Strings.isNullOrEmpty(realFrom)) {
//            message.addAttribute(QChatConstant.Note.REAL_FROM, realFrom);
//        }
//        if (!Strings.isNullOrEmpty(realTo)) {
//            message.addAttribute(QChatConstant.Note.REAL_TO, realTo);
//        }
//        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);
//        message.addAttribute(QChatConstant.Note.IS_HIDDEN_MSG, "0");
//        message.addAttribute(QChatConstant.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));
//        message.addAttribute(QChatConstant.Note.QCHAT_ID, chatId); // 客人发给qchat客户端为4 qchat客户端给客人发是5
//        message.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_CLIENT);
//      /*  if (!Strings.isNullOrEmpty(msgType) && !msgType.equalsIgnoreCase("11")) {
//            message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY,"true");
//        }*/
//
//        Element body = message.addElement(QChatConstant.Note.BODY);
//        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
//        body.addAttribute(QChatConstant.Note.MSGTYPE, msgType);
//        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
//        if (!TextUtils.isEmpty(backupinfo)) {
//            body.addAttribute(QChatConstant.Note.BODY_BACKUPINFO, backupinfo);
//        }
//        if (!Strings.isNullOrEmpty(extInfo)) {
//            body.addAttribute(QChatConstant.Note.BODY_EXTENDINFO, extInfo);
//        }
//        body.addText(content);
//
//        Element active = message.addElement(QChatConstant.Note.ACTIVE);
//        active.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_URL);
//
//        return message.asXML();
//    }

    private String createConsultMessageEx(
            String content,
            String from,
            String realFrom,
            String to,
            String realTo,
            String msgId,
            String chatId,
            String msgType,
            String backupinfo,
            String extInfo,
            boolean carbonFlag
    ) {

        Map<String, String> channelIdValue = Maps.newHashMap();
        channelIdValue.put("cn", "consult");
        channelIdValue.put("d", "send");
        channelIdValue.put("usrType", "usr");
        Document document = DocumentHelper.createDocument();
        Element message = document.addElement(QChatConstant.Note.MESSAGE);
        message.addAttribute(QChatConstant.Note.FROM, from);
        message.addAttribute(QChatConstant.Note.TO, to);
        if (!Strings.isNullOrEmpty(realFrom)) {
            message.addAttribute(QChatConstant.Note.REAL_FROM, realFrom);
        }
        if (!Strings.isNullOrEmpty(realTo)) {
            message.addAttribute(QChatConstant.Note.REAL_TO, realTo);
        }

        if (carbonFlag) {
            message.addAttribute(QChatConstant.Note.CARBON_MSG, "true");
        }

        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);
        message.addAttribute(QChatConstant.Note.IS_HIDDEN_MSG, "0");
        message.addAttribute(QChatConstant.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));
        message.addAttribute(QChatConstant.Note.QCHAT_ID, chatId); // 客人发给qchat客户端为4 qchat客户端给客人发是5
        message.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_CLIENT);
      /*  if (!Strings.isNullOrEmpty(msgType) && !msgType.equalsIgnoreCase("11")) {
            message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY,"true");
        }*/

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID,
                Strings.isNullOrEmpty(msgId) ? QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", "") : msgId);
        body.addAttribute(QChatConstant.Note.MSGTYPE, msgType);
        body.addAttribute(QChatConstant.Note.MATYPE, QChatConstant.Note.MATYPE_WEB);
        if (!TextUtils.isEmpty(backupinfo)) {
            body.addAttribute(QChatConstant.Note.BODY_BACKUPINFO, backupinfo);
        }


        if (!Strings.isNullOrEmpty(extInfo)) {
            body.addAttribute(QChatConstant.Note.BODY_EXTENDINFO, extInfo);
        }
        body.addText(content);

        Element active = message.addElement(QChatConstant.Note.ACTIVE);
        active.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_URL);

        return message.asXML();
    }

    public boolean sendSingelConsultPromoteMessage(String message, String from, String to, String realfrom, boolean toSeat) {
        if (Strings.isNullOrEmpty(message) || Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(to)) {
            return false;
        }
        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);

        /*
-        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
-        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);
-        */
        realfrom = EjabdUtil.makeSureUserJid(realfrom, QChatConstant.QCHAR_HOST);

        String messageParam = ConsultUtils.createConsultMessage(message, from, realfrom, to, to,
                toSeat ? QChatConstant.Note.QCHAT_ID_USER2SEAT : QChatConstant.Note.QCHAT_ID_SEAT2USER, "15", "", "");
        return innerSendMessage(messageParam, from, to);
    }


    public boolean sendSingelConsultPromoteMessageEx(String message, String from, String to, String realfrom, String realto, boolean toSeat) {
        if (Strings.isNullOrEmpty(message) || Strings.isNullOrEmpty(from) || Strings.isNullOrEmpty(to)
                || Strings.isNullOrEmpty(realfrom) || Strings.isNullOrEmpty(to)) {
            return false;
        }
        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);

        /*
-        from = EjabdUtil.makeSureUserJid(from, QChatConstant.QCHAR_HOST);
-        to = EjabdUtil.makeSureUserJid(to, QChatConstant.QCHAR_HOST);
-        */
        realfrom = EjabdUtil.makeSureUserJid(realfrom, QChatConstant.QCHAR_HOST);
        realto = EjabdUtil.makeSureUserJid(realto, QChatConstant.QCHAR_HOST);

        String messageParam = ConsultUtils.createConsultMessage(message, from, realfrom, to, realto,
                toSeat ? QChatConstant.Note.QCHAT_ID_USER2SEAT : QChatConstant.Note.QCHAT_ID_SEAT2USER, "15", "", "");
        return innerSendMessage(messageParam, from, to);
    }


    private boolean innerSendMessage(String message, String from, String to) {
        String hostName = EjabdUtil.getUserDomain(from, QChatConstant.DEFAULT_HOST);
        IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(hostName);
        if (null == plugin)

            return false;

        return plugin.sendThirdMessage(from, to, message);
    }

    private String assembleBackUpInfo(ProductVO productVO, ProductNoteArgs args) {
        String backupInfo = "";
        if (StringUtils.isNotEmpty(args.getSeatQName())) {
            String seatName = SendMessage.appendQCDomain(args.getSeatQName());
            seatName = JID.parseAsJID(seatName).getNode();
//            if (StringUtils.endsWith(seatName, "_robot")) {
            List<Map<String, Object>> backupinfoList = new ArrayList<>();

            Map<String, Object> backupinfo50010 = new HashMap<>();
            Map<String, Object> backupinfo50010data = new HashMap<>();
            backupinfo50010data.put("bu", productVO.getBu());
            String bsid = "";

            long shopId = 0;
            String virtualId = args.getVirtualId();
            if (StringUtils.isNotEmpty(virtualId) && virtualId.startsWith("shop_")) {
                String strShopId = JID.parseAsJID(virtualId).getNode().replace("shop_", "");
                shopId = StringUtils.isNumeric(strShopId) ? Long.parseLong(strShopId) : shopId;
            } else {
                List<Long> shopIdList = groupService.queryShopIdByProductId(productVO.getProductId());
                shopId = CollectionUtils.isNotEmpty(shopIdList) ? shopIdList.get(0) : shopId;
            }

            if (shopId != 0) {
                BusiShopMapping shopMapping = busiShopMapService.queryBusiShopMappingByShopID(shopId);
                bsid = shopMapping != null ? shopMapping.getBusiSupplierID() : "";
            }
            backupinfo50010data.put("bsid", bsid);
            backupinfo50010data.put("rbtMsg", 0);
            backupinfo50010data.put("isrobot", true);
            backupinfo50010data.put("pid", productVO.getProductId()); // 获取不到产品id,这里先给个空
            backupinfo50010.put("type", 50001);
            backupinfo50010.put("data", backupinfo50010data);
            backupinfoList.add(backupinfo50010);
            backupInfo = JacksonUtil.obj2String(backupinfoList);
//            }
        }
        log.info("assembleBackUpInfo backupInfo:{}", backupInfo);
        return backupInfo;
    }

    static class QchatResult {
        private boolean ret;
        private String errmsg;
        private int errcode;

        public boolean isRet() {
            return ret;
        }

        public void setRet(boolean ret) {
            this.ret = ret;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }

        public int getErrcode() {
            return errcode;
        }

        public void setErrcode(int errcode) {
            this.errcode = errcode;
        }
    }
}