package com.qunar.chat.service;



import com.qunar.chat.common.business.QtQueueKey;
import com.qunar.chat.common.util.*;
import com.qunar.chat.config.Config;
import com.qunar.chat.dao.ConsultMsgDao;
import com.qunar.chat.dao.ShopDao;
import com.qunar.chat.dao.UnSentMessageDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;

@Service
public class ConsultMessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultMessageService.class);

    @Resource
    private ConsultMsgDao consultMsgDao;

    @Autowired
    QueueManagerService queueManagerService;

    @Autowired
    ShopDao shopDao;

    @Autowired
    UnSentMessageDao unSentMessageDao;

    public void processConsultMessage(Map<String, Object> chatMessage){

        LOGGER.info("MQ消息:[{}]", JacksonUtils.obj2String(chatMessage));
        consultChatMsg(chatMessage);

    }

    public void sendConsultMsg(Map<String, Object> consultMessage) {
        Long currentTimeMillis = System.currentTimeMillis();
        consultMessage.put("create_time", currentTimeMillis);
        Map<String, Object> mmap = new HashMap<>();
        String msg = getString(consultMessage.get("m_body"));
        mmap = Xml2Json.xmppToMap(msg);
        Map<String, Object> messageMap = (Map<String, Object>) mmap.get("message");
        messageMap.put("msec_times", String.valueOf(currentTimeMillis));
        String message = CreateXmppMsg.makeConsultMsg(mmap);
        consultMessage.put("m_body", message);
        consultChatMsg(consultMessage);
    }

    public void consultChatMsg(Map<String, Object> consultMessage) {

        String direc = getString(consultMessage.get("d"));

        Map<String, Object> mmap = new HashMap<>();
        String msg = getString(consultMessage.get("m_body"));
        mmap = Xml2Json.xmppToMap(msg);
        Map<String, Object> messageMap = (Map<String, Object>) mmap.get("message");
        String qchatId = getString(messageMap.get("qchatid"));
        if (direc.equals("send")) {
            String noUpdateMsgLog = getString(messageMap.get("no_update_msg_log"));
            boolean noUpdate = StringUtils.isNotEmpty(noUpdateMsgLog) && "true".equals(noUpdateMsgLog);

            // 自动回复消息不带to，不做处理
            String autoReply = getString(messageMap.get("auto_reply"));
            String to = getString(messageMap.get("to"));
            if ("true".equals(autoReply) && StringUtils.isEmpty(to)) {
                LOGGER.info("ignore message:{}", JacksonUtils.obj2String(messageMap));
                return;
            }

            if (qchatId.equals("4")) {
                makeNewConsultMsg(consultMessage, noUpdate);
            } else {
                insertConsultMsg(consultMessage, "5");
                makeRecvConsultMsg(consultMessage);
            }
        } else {
            String noUpdateMsgLog = getString(messageMap.get("no_update_msg_log"));

            insertConsultMsg(consultMessage, qchatId);
            if ("5".equals(qchatId) && !(StringUtils.isNotEmpty(noUpdateMsgLog) && "true".equals(noUpdateMsgLog))) {
                recvSaveMsgLog(messageMap);
            }
        }

    }


    private void makeNewConsultMsg(Map<String, Object> consultMessage, boolean noUpdate) {
        String realFromHost = getHost(consultMessage.get("from_host"));
        String toHost = getHost(consultMessage.get("to_host"));
        String realFrom = SendMessage.appendQCDomain(getString(consultMessage.get("m_from")), realFromHost);
        String to = SendMessage.appendQCDomain(getString(consultMessage.get("m_to")), toHost);

        if (to.startsWith("shop_"))
            to = to.replace("shop_", "");

        JID theTo = JID.parseAsJID(to);
        Long shopid = Long.parseLong(theTo.getNode());

        JID fromJid = null;
        if (StringUtils.isNotEmpty(realFrom)) {
            if (!realFrom.contains("@")) {
                realFrom = String.format("%s@%s", realFrom, realFromHost);
            }
            fromJid = JID.parseAsJID(realFrom);
        }
        if (fromJid == null)
            return;
        HashSet<QtQueueKey> keys = QtQueueKey.parseFromRedisToHashSet(String.format("predistributionMapping:%s", fromJid.toBareJID()));
        String productId = "*";
        if(CollectionUtils.isNotEmpty(keys)) {
            for (QtQueueKey qtQueueKey : keys) {
                if (qtQueueKey.getShopId() == shopid) {
                    productId = qtQueueKey.getProductId();
                    break;
                }
            }
        }


        JID realTo = queueManagerService.getRealTo(fromJid, shopid, productId, toHost, noUpdate);
        LOGGER.info("[{}, {}] get  realto is :{}", realFrom, to, realTo);

        if (realTo == null) {
            //  realTo不存在调用，realTo 存在时再次回调本方法
             unSentMessageDao.insertNoneRealtoMessage(fromJid.toBareJID(), shopid, JacksonUtils.obj2String(consultMessage));
        } else {
            String bareJID = realTo.toBareJID();
            consultMessage.put("realfrom", realFrom);
            consultMessage.put("realto", bareJID);

            insertConsultMsg(consultMessage, "4");
            makeRecvConsultMsg(consultMessage);

        }
    }

    public void makeRecvConsultMsg(Map<String, Object> consultMessage) {
        String to = getString(consultMessage.get("m_to"));
        String toHost = getHost(consultMessage.get("to_host"));
        toHost = StringUtils.isEmpty(toHost) ? getHost(consultMessage.get("from_host")) : toHost;
        String realTo = getString(consultMessage.get("realto"));
        consultMessage.put("from", to);
        consultMessage.put("to", realTo);

        String msg = getString(consultMessage.get("m_body"));

        Map<String, Object> mmap;
        mmap = Xml2Json.xmppToMap(msg);

        Map<String, Object> messageMap = (Map<String, Object>) mmap.get("message");
        Map<String, Object> bodyMap = (Map<String, Object>) mmap.get("body");

        String msgId = getString(bodyMap.get("id"));
        if (msgId.equals("")) {
            msgId = UUID.randomUUID().toString();
        } else {
            msgId = "consult-" + msgId;
        }

        bodyMap.put("id", msgId);
        String channelid = getString(messageMap.get("channelid"));
        String newChennelId = null;
        if (channelid.equals("")) {
            newChennelId = getNewChannelID();
        } else {
            Map<String, Object> channelIdMap = JacksonUtils.string2Map(channelid);
            channelIdMap.put("d", "recv");
            newChennelId = JacksonUtils.obj2String(channelIdMap);
        }
        messageMap.put("channelid", newChennelId);
        messageMap.put("from", SendMessage.appendQCDomain(to, toHost));
        messageMap.put("to", SendMessage.appendQCDomain(realTo, toHost));
        messageMap.put("realto", SendMessage.appendQCDomain(realTo, toHost));

        String recvConsultMsg = CreateXmppMsg.makeConsultMsg(mmap);
        Dictionary<String, String> args = new Hashtable<>();
        args.put("from", SendMessage.appendQCDomain(to, toHost));
        args.put("to", SendMessage.appendQCDomain(realTo, toHost));
        args.put("message", recvConsultMsg);
        args.put("system", "vs_qchat_admin");
        String ret = null;
        if (Config.QCHAT_DEFAULT_HOST.equalsIgnoreCase(toHost)) {
            ret = HttpClientUtils.postJson(Config.QCHAT_SEND_URL, JacksonUtils.obj2String(args));
        } else {
            LOGGER.warn("message host error");
        }

        LOGGER.info("qchat send url message :{}, ret;{}", JacksonUtils.obj2String(args), ret);
    }


    private void insertConsultMsg(Map<String, Object> consultMessage, String qchatId) {
        String realFromHost = getHost(consultMessage.get("from_host"));
        String toHost = getHost(consultMessage.get("to_host"));
        String realFrom = SendMessage.appendQCDomain(getString(consultMessage.get("realfrom")), realFromHost);
        String realTo = SendMessage.appendQCDomain(getString(consultMessage.get("realto")), toHost);
        String from = SendMessage.appendQCDomain(getString(consultMessage.get("m_from")), realFromHost);
        String to = SendMessage.appendQCDomain(getString(consultMessage.get("m_to")), toHost);
        String id = getString(consultMessage.get("msg_id"));
        String msg = getString(consultMessage.get("m_body"));

        String[] fromArray = from.split("@");
        String m_from = fromArray[0];
        String from_host = fromArray[1];

        String[] toArray = to.split("@");
        String m_to = toArray[0];
        String to_host = toArray[1];
        String type = "consult";

        double time = getMescTime(msg);

        LOGGER.info("qchatId:{} the insert message is {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", qchatId,
                m_from, from_host, m_to, to_host, msg, id, time, realFrom, realTo, type);
        consultMsgDao.insertConsultMsg(m_from, from_host, m_to, to_host, msg, id, time, realFrom, realTo, type);
    }


    private double getMescTime(String msg) {
        Map<String, Object> xmppMap = Xml2Json.xmppToMap(msg);
        Map<String, Object> messageMap = (Map<String, Object>) xmppMap.get("message");

        String mSecTime = getString(messageMap.get("msec_times"));
        if (mSecTime.equals("")) {
            return System.currentTimeMillis();
        } else {
            return Double.valueOf(mSecTime) / 1000;
        }
    }


    private String getNewChannelID() {
        Dictionary<String, Object> channelidMap = new Hashtable<>();
        channelidMap.put("d", "recv");
        channelidMap.put("cn", "consult");
        channelidMap.put("usrType", "usr");

        return JacksonUtils.obj2String(channelidMap);
    }

    private String getString(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    private String getHost(Object object) {
        if (object == null) {
            return  Config.QCHAT_DEFAULT_HOST;
        }
        return object.toString();
    }

    private void recvSaveMsgLog(Map<String, Object> messageMap) {
        String from = SendMessage.appendQCDomain(getString(messageMap.get("from")), Config.QCHAT_DEFAULT_HOST);
        String to = SendMessage.appendQCDomain(getString(messageMap.get("to")), Config.QCHAT_DEFAULT_HOST);
//        String realFrom = SendMessage.appendQCDomain(getString(messageMap.get("realfrom")));
        LOGGER.info("from msg:{} to msg:{}", getString(messageMap.get("from")), getString(messageMap.get("to")));
        if (from.startsWith("shop_"))
            from = from.replace("shop_", "");
        JID theFrom = JID.parseAsJID(from);
        Long shopid = 0L;
        if (StringUtils.isNumeric(theFrom.getNode())) {
            shopid = Long.parseLong(theFrom.getNode());
        }

        JID toJid = JID.parseAsJID(to);


        if (shopid > 0) {
            queueManagerService.saveMsgLog(toJid, shopid, System.currentTimeMillis(), false);
        }
    }
}