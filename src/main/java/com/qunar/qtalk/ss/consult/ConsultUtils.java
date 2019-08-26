package com.qunar.qtalk.ss.consult;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.plugins.chatplugin.ChatPluginInstance;
import com.qunar.qchat.admin.plugins.chatplugin.IChatPlugin;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
import com.qunar.qtalk.ss.consult.entity.QtUnSentMessage;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
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
                toSeat ? QChatConstant.Note.QCHAT_ID_USER2SEAT : QChatConstant.Note.QCHAT_ID_SEAT2USER, "15", "", "", isRecv, isAutoReply, noUpdateMsgLog);
//        return innerSendMessage(messageParam, from, to);
        IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(to.getDomain());

        if (plugin == null)
            return false;

        logger.info("send message: {} - {} - {}", from, to, messageParam);

        return plugin.sendThirdMessage(from.toBareJID(), to.toBareJID(), messageParam);
    }


    public static void sendWelcome(JID shopId, JID jid, JID qunarName) {

        if (shopId == null || jid == null || qunarName == null || qunarName.getNode().endsWith("_robot")) {
            logger.info("sendWelcome and param is wrong:{}, {}, {}", shopId, jid, qunarName);
        } else {
            long shopKey = Shop.parseJIDToShopId(shopId);

            logger.debug("sendWelcome, shopKey is {}", shopKey);

            Shop shop = SpringComponents.components.shopService.selectShopById(shopKey);
            String welcome = shop.getWelcomes();
            String supplierId = SpringComponents.components.hotlineSupplierService.selectHotlineBySupplierId(shopKey);
            JID from = StringUtils.isNotEmpty(supplierId) ? JID.parseAsJID(supplierId) : shopId;
            if (StringUtils.isNotEmpty(welcome)) {
                String messageContent = createWelcomeMessage(from, jid, qunarName, jid, welcome);

                IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(jid.getDomain());
                plugin.sendThirdMessage(from.toFullJID(), jid.toBareJID(), messageContent);
                logger.info("send selcome message: {} - {} - {}", from, jid, messageContent);
            }
        }
    }

    public static void resendUnsentMesasge(long shopId, JID jid) {


        Collection<QtUnSentMessage> messages = QtQueueDao.getInstance().getUnsentMessage(shopId, jid);

        try {
            if (CollectionUtils.isNotEmpty(messages)) {
                for (QtUnSentMessage msg : messages) {
                    //
                    // t

                    logger.info("send un sent message:{}", msg.getMessage());
                    SpringComponents.components.consultMessageService.sendConsultMsg(JsonUtil.parseJSONObject(msg.getMessage()));
                }
                QtQueueDao.getInstance().deleteUnSentMessages(messages);
            }
        } catch (IOException e) {
            logger.error("resendUnsentMesasge failed. {} - {}", shopId, jid, e);
        }
    }

    public static String createWelcomeMessage(JID from, JID to, JID realfrom, JID realto, String content) {

        Document document = DocumentHelper.createDocument();

        Element message = document.addElement(QChatConstant.Note.MESSAGE);

        message.addAttribute(QChatConstant.Note.FROM, from.toFullJID());
        message.addAttribute(QChatConstant.Note.TO, to.toBareJID());

        if (realfrom != null)
            message.addAttribute(QChatConstant.Note.REAL_FROM, realfrom.toFullJID());

        if (realto != null)
            message.addAttribute(QChatConstant.Note.REAL_TO, realto.toBareJID());

        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);

        Map<String, String> channelIdValue = Maps.newHashMap();
        channelIdValue.put("cn", "consult");
        channelIdValue.put("d", "recv");
        channelIdValue.put("usrType", "usr");

        message.addAttribute(QChatConstant.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));

        message.addAttribute(QChatConstant.Note.QCHAT_ID, "5");
        message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY, "true");
        message.addAttribute(QChatConstant.Note.MESSAGE_NO_UPDATE_MSG_LOG, "true");
        message.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_CLIENT);

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
        body.addAttribute(QChatConstant.Note.MSGTYPE, "1");
        body.addAttribute(QChatConstant.Note.MATYPE, "3");

        body.addText(content);
        Element active = message.addElement(QChatConstant.Note.ACTIVE);
        active.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_URL);
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
        Element message = document.addElement(QChatConstant.Note.MESSAGE);
        message.addAttribute(QChatConstant.Note.FROM, from);
        message.addAttribute(QChatConstant.Note.TO, to);
        if (!Strings.isNullOrEmpty(realFrom)) {
            message.addAttribute(QChatConstant.Note.REAL_FROM, realFrom);
        }
        if (!Strings.isNullOrEmpty(realTo)) {
            message.addAttribute(QChatConstant.Note.REAL_TO, realTo);
        }
        message.addAttribute(QChatConstant.Note.Type, QChatConstant.Note.CONSULT);
        message.addAttribute(QChatConstant.Note.IS_HIDDEN_MSG, "0");
        message.addAttribute(QChatConstant.Note.CHANNEL_ID, JacksonUtils.obj2String(channelIdValue));
        message.addAttribute(QChatConstant.Note.QCHAT_ID, chatId); // 客人发给qchat客户端为4 qchat客户端给客人发是5
        message.addAttribute(QChatConstant.Note.XMLNS, QChatConstant.Note.JABBER_CLIENT);

        if (!isAutoReply)
            message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY, "false");
        else {
            if (!Strings.isNullOrEmpty(msgType) && !msgType.equalsIgnoreCase("11")) {
                message.addAttribute(QChatConstant.Note.MESSAGE_AUTO_REPLY, "true");
            }
        }
        if (noUpdateMsgLog) {
            message.addAttribute(QChatConstant.Note.MESSAGE_NO_UPDATE_MSG_LOG, "true");
        }

        Element body = message.addElement(QChatConstant.Note.BODY);
        body.addAttribute(QChatConstant.Note.ID, QChatConstant.QCADMIN + UUID.randomUUID().toString().replace("-", ""));
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

}
