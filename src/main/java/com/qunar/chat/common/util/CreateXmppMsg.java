package com.qunar.chat.common.util;



import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by MSI on 2017/9/13.
 */
public class CreateXmppMsg {


    private static final Logger LOGGER = LoggerFactory.getLogger(CreateXmppMsg.class);

    public static String makeConsultMsg(Map<String, Object> xmppMap) {
        if (org.apache.commons.collections.MapUtils.isEmpty(xmppMap)) {
            return null;
        }
        LOGGER.info("makeConsultMsg xmppMap:{}", JacksonUtils.obj2String(xmppMap));
        DocumentFactory df = DocumentFactory.getInstance();
        Document document = df.createDocument("utf-8");

        Element messageEle = document.addElement("message");
        HashMap<String, Object> messageMap = (HashMap<String, Object>) xmppMap.get("message");
        HashMap<String, Object> bodyMap = (HashMap<String, Object>) xmppMap.get("body");
        HashMap<String, Object> timeMap = (HashMap<String, Object>) xmppMap.get("time");

        setMapAttrs(messageMap, messageEle);

        Element bodyEle = messageEle.addElement("body");
        doCreateBodyEle(bodyEle, bodyMap);

        Element timeEle = messageEle.addElement("stime");

        timeMap.put("xmlns", "jabber:stime:delay");
        setMapAttrs(timeMap, timeEle);

        return trimXml(document);
    }

    private static void setMapAttrs(Map<String, Object> maps, Element messageEle) {
        for (String key : maps.keySet()) {
            Object value = maps.get(key);
            if (value != null && value.getClass() == String.class) {
                messageEle.addAttribute(key, value.toString());
            }
        }
    }

//    private static String doCreateChannelId(Map<String, String> commonHandledJsonMap) {
//        Map<String, Object> channelMap = new HashMap<>();
//        String channelid = commonHandledJsonMap.get("channelid");
//
//        if (channelid == null) {
//            // channelMap.put("d", "send");
//        } else {
//            channelMap = JacksonUtils.string2Map(channelid);
//            // channelMap.put("cn", MapUtils.getString(commonHandledJsonMap, "cn"));
//            channelMap.put("d", "send");
//        }
//        return JacksonUtils.obj2String(channelMap);
//    }

    @SuppressWarnings("unchecked")
    private static void doCreateBodyEle(final Element bodyEle, Map<String, Object> bodyMap) {

        //     HashMap<String, String> xmppMsgContPair = (HashMap<String, String>) commonJsonMap.get("body");

        //向Body标签插入内容
        if (bodyMap != null) {
            bodyEle.addText(bodyMap.get("content").toString());
            bodyMap.remove("content");

            setMapAttrs(bodyMap, bodyEle);
            String msgId = bodyMap.get("id").toString();
            if (StringUtils.isEmpty(msgId)) {
                msgId = getUUID();
            }
            bodyEle.addAttribute("id", msgId);
        }
    }


    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static String trimXml(Document document) {
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        OutputFormat of = new OutputFormat(null, false, "utf-8");
        of.setSuppressDeclaration(true);
        try {
            XMLWriter xw = new XMLWriter(baOs, of);
            xw.write(document);
            return baOs.toString("utf-8");
        } catch (IOException e) {
            String errorXmpp = document.asXML();
            LOGGER.error("从json转成xmpp出错，errorXmpp[{}]", errorXmpp);
            return errorXmpp;
        }
    }
}
