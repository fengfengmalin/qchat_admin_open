package com.qunar.chat.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.chat.common.Cont;
import com.qunar.chat.common.business.JsonResultVO;
import com.qunar.chat.config.Config;
import com.qunar.chat.entity.QtUnSentMessage;
import com.qunar.chat.entity.Shop;
import com.qunar.chat.service.SpringComponents;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConsultUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConsultUtils.class);

    public static boolean sendMessage(JID from, JID to, JID realfrom, JID realto, String message, boolean toSeat) {
        return sendMessage(from, to, realfrom, realto, message, toSeat, false, true);
    }

    public static boolean sendMessage(JID from, JID to, JID realfrom, JID realto, String message, boolean toSeat, boolean isAutoReply, boolean noUpdateMsgLog) {
        return sendMessage(from, to, realfrom, realto, message, toSeat, true, isAutoReply, noUpdateMsgLog);
    }

    public static boolean sendMessage(JID from, JID to, JID realfrom, JID realto, String message, boolean toSeat, boolean isRecv, boolean isAutoReply, boolean noUpdateMsgLog) {
        String messageParam = createConsultMessage(message, from.toBareJID(), realfrom.toBareJID(), to.toBareJID(), realto.toBareJID(),
                toSeat ? Cont.Note.QCHAT_ID_USER2SEAT : Cont.Note.QCHAT_ID_SEAT2USER, "15", "", "", isRecv, isAutoReply, noUpdateMsgLog);
//        return innerSendMessage(messageParam, from, to);

        return sendThirdMessage(from.toBareJID(), to.toBareJID(), messageParam);
    }


    public static void sendWelcome(JID shopId, JID jid, JID qunarName) {

        if (shopId == null || jid == null || qunarName == null) {
            logger.info("sendWelcome and param is wrong:{}, {}, {}", shopId, jid, qunarName);
        } else {
            long shopKey = Shop.parseJIDToShopId(shopId);

            logger.debug("sendWelcome, shopKey is {}", shopKey);

            Shop shop = SpringComponents.components.shopDao.selectShopById(shopKey);
            String welcome = shop.getWelcomes();
//            String supplierId = SpringComponents.components.hotlineSupplierService.selectHotlineBySupplierId(shopKey);
//            JID from = StringUtils.isNotEmpty(supplierId) ? JID.parseAsJID(supplierId) : shopId;
            if (StringUtils.isNotEmpty(welcome)) {
                String messageContent = createWelcomeMessage(shopId, jid, qunarName, jid, welcome);

                sendThirdMessage(shopId.toFullJID(), jid.toBareJID(), messageContent);
                logger.info("send selcome message: {} - {} - {}", shopId, jid, messageContent);
            }
        }
    }

    public static void resendUnsentMesasge(long shopId, JID jid) {

        try {
            List<QtUnSentMessage> messages = SpringComponents.components.unSentMessageDao.selectByCustomerNameAndShopId(jid.toBareJID(), shopId);
            List<String> messageIds = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(messages)) {
                for (QtUnSentMessage msg : messages) {
                    messageIds.add(msg.getMessageId());
                    logger.info("send un sent message:{}", msg.getMessage());
                    SpringComponents.components.consultMessageService.sendConsultMsg(JacksonUtils.string2Map(msg.getMessage()));
                }
                SpringComponents.components.unSentMessageDao.deleteUnSentMessages(messageIds);
            }
        } catch (Exception e) {
            logger.error("resendUnsentMesasge failed. {} - {}", shopId, jid, e);
        }
    }

    public static String createWelcomeMessage(JID from, JID to, JID realfrom, JID realto, String content) {

        Document document = DocumentHelper.createDocument();

        Element message = document.addElement(Cont.Note.MESSAGE);

        message.addAttribute(Cont.Note.FROM, from.toFullJID());
        message.addAttribute(Cont.Note.TO, to.toBareJID());

        if (realfrom != null)
            message.addAttribute(Cont.Note.REAL_FROM, realfrom.toFullJID());

        if (realto != null)
            message.addAttribute(Cont.Note.REAL_TO, realto.toBareJID());

        message.addAttribute(Cont.Note.Type, Cont.Note.CONSULT);

        Map<String, String> channelIdValue = Maps.newHashMap();
        channelIdValue.put("cn", "consult");
        channelIdValue.put("d", "recv");
        channelIdValue.put("usrType", "usr");

        message.addAttribute(Cont.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));

        message.addAttribute(Cont.Note.QCHAT_ID, "5");
        message.addAttribute(Cont.Note.MESSAGE_AUTO_REPLY, "true");
        message.addAttribute(Cont.Note.MESSAGE_NO_UPDATE_MSG_LOG, "true");
        message.addAttribute(Cont.Note.XMLNS, Cont.Note.JABBER_CLIENT);

        Element body = message.addElement(Cont.Note.BODY);
        body.addAttribute(Cont.Note.ID, Cont.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
        body.addAttribute(Cont.Note.MSGTYPE, "1");
        body.addAttribute(Cont.Note.MATYPE, "3");

        body.addText(content);
        Element active = message.addElement(Cont.Note.ACTIVE);
        active.addAttribute(Cont.Note.XMLNS, Cont.Note.JABBER_URL);
        return message.asXML();
    }

    public static String createConsultMessage(
            String content,
            String from,
            String realFrom,
            String to,
            String realTo,
            String chatId,
            String msgType,
            String backupinfo,
            String extInfo, boolean isAutoReply) {
        return createConsultMessage(content,
                from,
                realFrom,
                to,
                realTo,
                chatId,
                msgType,
                backupinfo,
                extInfo, false, isAutoReply, false);
    }

    public static String createConsultMessage(
            String content,
            String from,
            String realFrom,
            String to,
            String realTo,
            String chatId,
            String msgType,
            String backupinfo,
            String extInfo) {
        return createConsultMessage(content,
                from,
                realFrom,
                to,
                realTo,
                chatId,
                msgType,
                backupinfo,
                extInfo, false, true, false);
    }

    public static String createConsultMessage(
            String content,
            String from,
            String realFrom,
            String to,
            String realTo,
            String chatId,
            String msgType,
            String backupinfo,
            String extInfo,
            boolean isRecv,
            boolean isAutoReply, boolean noUpdateMsgLog) {

        Map<String, String> channelIdValue = Maps.newHashMap();
        channelIdValue.put("cn", "consult");
        channelIdValue.put("d", isRecv ? "recv" : "send");
        channelIdValue.put("usrType", "usr");
        Document document = DocumentHelper.createDocument();
        Element message = document.addElement(Cont.Note.MESSAGE);
        message.addAttribute(Cont.Note.FROM, from);
        message.addAttribute(Cont.Note.TO, to);
        if (!Strings.isNullOrEmpty(realFrom)) {
            message.addAttribute(Cont.Note.REAL_FROM, realFrom);
        }
        if (!Strings.isNullOrEmpty(realTo)) {
            message.addAttribute(Cont.Note.REAL_TO, realTo);
        }
        message.addAttribute(Cont.Note.Type, Cont.Note.CONSULT);
        message.addAttribute(Cont.Note.IS_HIDDEN_MSG, "0");
        message.addAttribute(Cont.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));
        message.addAttribute(Cont.Note.QCHAT_ID, chatId); // 客人发给qchat客户端为4 qchat客户端给客人发是5
        message.addAttribute(Cont.Note.XMLNS, Cont.Note.JABBER_CLIENT);

        if (!isAutoReply)
            message.addAttribute(Cont.Note.MESSAGE_AUTO_REPLY, "false");
        else {
            if (!Strings.isNullOrEmpty(msgType) && !msgType.equalsIgnoreCase("11")) {
                message.addAttribute(Cont.Note.MESSAGE_AUTO_REPLY, "true");
            }
        }
        if (noUpdateMsgLog) {
            message.addAttribute(Cont.Note.MESSAGE_NO_UPDATE_MSG_LOG, "true");
        }

        Element body = message.addElement(Cont.Note.BODY);
        body.addAttribute(Cont.Note.ID, Cont.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
        body.addAttribute(Cont.Note.MSGTYPE, msgType);
        body.addAttribute(Cont.Note.MATYPE, Cont.Note.MATYPE_WEB);
        if (!TextUtils.isEmpty(backupinfo)) {
            body.addAttribute(Cont.Note.BODY_BACKUPINFO, backupinfo);
        }
        if (!Strings.isNullOrEmpty(extInfo)) {
            body.addAttribute(Cont.Note.BODY_EXTENDINFO, extInfo);
        }
        body.addText(content);

        Element active = message.addElement(Cont.Note.ACTIVE);
        active.addAttribute(Cont.Note.XMLNS, Cont.Note.JABBER_URL);

        return message.asXML();
    }

    public static boolean sendThirdMessage(String from, String to, String message) {
        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to))
            return false;


        Map<String, String> param = Maps.newHashMap();
        param.put(Cont.Note.FROM, from);
        param.put(Cont.Note.TO, to);
        param.put(Cont.Note.MESSAGE, message);
        param.put("system", "vs_qchat_admin");
        String response = HttpClientUtils.postJson(Config.SEND_NOTE_URL, JacksonUtils.obj2String(param));


        if (!Strings.isNullOrEmpty(response)) {
            JsonResultVO qChatResult = JacksonUtils.string2Obj(response, JsonResultVO.class);
            if (qChatResult != null && qChatResult.isRet()) {
                return true;
            }
        }
        return false;
    }
}
