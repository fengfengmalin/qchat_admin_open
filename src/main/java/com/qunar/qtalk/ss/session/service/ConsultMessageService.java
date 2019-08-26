package com.qunar.qtalk.ss.session.service;

import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.constants.Config;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.consult.QueueMappingDao;
import com.qunar.qtalk.ss.consult.entity.QtQueueKey;
import com.qunar.qtalk.ss.consult.entity.QtQueueStatus;
import com.qunar.qtalk.ss.session.dao.IConsultDao;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qtalk.ss.utils.*;
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
    private IConsultDao iConsultDao;

    @Resource
    private QueueMappingDao queueMappingDao;

    @Autowired
    ShopService shopService;

    public void processConsultMessage( Map<String, Object> chatMessage) {
        try {
            LOGGER.info("MQ消息:[{}]", JacksonUtils.obj2String(chatMessage));
            consultChatMsg(chatMessage);
        } catch (Exception e) {
            LOGGER.error("processConsultMessage key error", e);
        }
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
        try {
            String direc = getString(consultMessage.get("d"));

            Map<String, Object> mmap = new HashMap<>();
            String msg = getString(consultMessage.get("m_body"));
            mmap = Xml2Json.xmppToMap(msg);
            Map<String, Object> messageMap = (Map<String, Object>) mmap.get("message");
            String qchatId = getString(messageMap.get("qchatid"));
            // 兼容客户端老版本，适当时候可以删除这段代码
            if (StringUtils.isEmpty(qchatId)) {
                Map<String, Object> bodyMap = (Map<String, Object>) mmap.get("body");
                qchatId = getString(bodyMap.get("qchatid"));
            }
            if (direc.equals("send")) {
//                checkTranferMsg(consultMessage, mmap);
                String noUpdateMsgLog = getString(messageMap.get("no_update_msg_log"));
                boolean noUpdate = StringUtils.isNotEmpty(noUpdateMsgLog) && "true".equals(noUpdateMsgLog);

                // 自动回复消息不带to，不做处理
                String autoReply = getString(messageMap.get("auto_reply"));
                String to = getString(messageMap.get("to"));
                if ("true".equals(autoReply) && StringUtils.isEmpty(to)) {
                    LOGGER.info("ignore message:{}", JacksonUtil.obj2String(messageMap));
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
                    recvSaveMsgLog(messageMap, consultMessage);
                }
            }
        } catch (Throwable throwable) {
            LOGGER.error("consultChatMsg error: {}, {}", consultMessage, throwable);
        }
    }


    private void makeNewConsultMsg(Map<String, Object> consultMessage, boolean noUpdate) {
        String realFromHost = getHost(consultMessage.get("from_host"));
        String toHost = getHost(consultMessage.get("to_host"));
        String realFrom = SendMessage.appendQCDomain(getString(consultMessage.get("m_from")), realFromHost);
        String to = SendMessage.appendQCDomain(getString(consultMessage.get("m_to")), toHost);

        Long shopid = shopService.selectShopByBsiId(to);
        if (shopid == null) {
            LOGGER.warn("makeNewConsultMsg shopId is null to:{}", to);
            return;
        }
//        if (StringUtils.isNotEmpty(to)) {
//
//            if (to.startsWith("shop_"))
//                to = to.replace("shop_", "");
//
//            if (!to.contains("@")) {
//                to = String.format("%s@%s", to, toHost);
//            }
//        }
//
//        JID theTo = JID.parseAsJID(to);
//
//        int shopid = Integer.parseInt(theTo.getNode());

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


        JID realTo = QtQueueManager.getInstance().getRealTo(fromJid, shopid, productId, toHost, noUpdate);
        LOGGER.info("[{}, {}] get  realto is :{}", realFrom, to, realTo);

        if (realTo == null) {
            // 不存在调用，realTo 存在时再次回调本方法
            QtQueueManager.getInstance().saveMessageWithoutRealto(fromJid, shopid, consultMessage);
        } else {
            String bareJID = realTo.toBareJID();
//            String msg_id = getString(consultMessage.get("msg_id"));
            final String pid = productId;
            final JID customer = fromJid;
//            CompletableFuture.runAsync(() -> {
//                sendWxNotify(realTo, pid, shopid, customer, consultMessage);
//            });
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
        if (QChatConstant.QCHAR_HOST.equalsIgnoreCase(toHost)) {
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
        iConsultDao.insertConsultMsg(m_from, from_host, m_to, to_host, msg, id, time, realFrom, realTo, type, qchatId);
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
            return  QChatConstant.DEFAULT_HOST;
        }
        return object.toString();
    }

    private void recvSaveMsgLog(Map<String, Object> messageMap, Map<String, Object> consultMessage) {
        String from = SendMessage.appendQCDomain(getString(messageMap.get("from")));
        String to = SendMessage.appendQCDomain(getString(messageMap.get("to")));
        String realFrom = SendMessage.appendQCDomain(getString(messageMap.get("realfrom")));
        LOGGER.info("from msg:{} to msg:{}", getString(messageMap.get("from")), getString(messageMap.get("to")));
        Long shopid = shopService.selectShopByBsiId(from);
//        if (StringUtils.isNotEmpty(from)) {
//            if (from.startsWith("shop_"))
//                from = from.replace("shop_", "");
//            if (!from.contains("@")) {
//                from = String.format("%s@%s", from,  QChatConstant.DEFAULT_HOST);
//            }
//        }
//        JID theFrom = JID.parseAsJID(from);
//        long shopid = 0;
//        if (StringUtils.isNumeric(theFrom.getNode())) {
//            shopid = Long.parseLong(theFrom.getNode());
//        }

        JID toJid = null;
        if (StringUtils.isNotEmpty(to)) {
            if (!to.contains("@")) {
                to = String.format("%s@%s", to,  QChatConstant.DEFAULT_HOST);
            }
            toJid = JID.parseAsJID(to);
        }

        final String customer = to;

        if (shopid!= null && shopid > 0) {
//            sendCustomerWxNotify(shopid, customer, realFrom, consultMessage);
            saveMsgLog(toJid, shopid, System.currentTimeMillis(), false);
        }
    }

//    private void sendWxNotify(JID csrJid, String productId, long shopId, JID realFrom, Map<String, Object> consultMessage) {
//        try {
//            List<CSR> csrList = SpringComponents.components.csrService.queryCsrByQunarNameAndShopId(csrJid.getNode(), shopId, csrJid.getDomain());
//            if (CollectionUtils.isEmpty(csrList)) {
//                LOGGER.warn("csrList isEmpty not sendWxNotify csrJid:{} shopId:{} ", csrJid.toBareJID(), shopId);
//                return;
//            }
//            CSR csr = csrList.get(0);
//
//            boolean judgeOnline = SpringComponents.components.siftStrategyService.judgeOnline(csrJid);
//            if (!judgeOnline && csr.getServiceStatus() == CsrServiceStatus.STANDARD_MODE.code
//                    && csr.getBindWx() == BindWxStatus.BIND_WX.code) {
//                sendWxRequest(csr.getQunarName().getNode(), String.valueOf(shopId), productId, realFrom.getNode(), realFrom.getNode(), consultMessage);
//            }
//        } catch (Exception e) {
//            LOGGER.error("sendWxNotify error csrJid:{} consultMessage:{}", csrJid.toBareJID(), JacksonUtil.obj2String(consultMessage), e);
//        }
//    }

//    private void sendCustomerWxNotify(Long shopId, String customerJid, String seatJid, Map<String, Object> consultMessage) {
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Config.Consult_Customer_Wx_NotifyTime);
//
//        String productId = QtQueueDao.getInstance().getConsultStatus(customerJid, shopId, seatJid, timestamp);
//        LOGGER.info("sendCustomerWxNotify shopId:{} customerJid:{} seatJid:{} productId:{}", shopId, customerJid, seatJid, productId);
//        if (productId != null && customerJid.length() < 32) {
//            Shop shop = shopService.selectShopById(shopId);
//            sendWxRequest(JID.parseAsJID(customerJid).getNode(), String.valueOf(shopId), productId, shop.getName(), JID.parseAsJID(seatJid).getNode(), consultMessage);
//        }
//    }

//    private void sendWxRequest(String username, String shopId, String productId, String webName, String realTo, Map<String, Object> consultMessage) {
//
//        String msg = getString(consultMessage.get("m_body"));
//        Map<String, Object> mmap = Xml2Json.xmppToMap(msg);
//        Map<String, Object> bodyMap = (Map<String, Object>) mmap.get("body");
//
//        String url = com.qunar.qchat.admin.constants.Config.QCHAT_MESSAGE_NOTIFY_WECHAT_URL;
//        Map<String, String> param = Maps.newHashMap();
//        // String username = String.valueOf(shopId);
//        String pagepath = "pages/chat/index?username=%s&shopId=%s&webname=%s&realto=%s";
//        param.put("username", username);
//        param.put("first", "您好，有客户会话消息未回复");
//        param.put("keyword1", "产品ID-" + productId);
//        param.put("keyword2", DateUtil.getDefaultYearMonthDayTime());
//        param.put("keyword3", bodyMap.get("content").toString());
//        param.put("remark", "【点击本消息回复对话】");
//        param.put("url", "qchataphone://start_qchat_activity");
//        param.put("appid", "wxae0393b9067ba669");
//        param.put("pagepath", String.format(pagepath, username, "shop_" + shopId, webName, realTo));
//        LOGGER.debug("will send postUrl:{} param:{}", url, JacksonUtil.obj2String(param));
//        String result = com.qunar.qchat.admin.util.HttpClientUtils.post(url, param);
//        JSONObject jsonObject = JSONObject.parseObject(result);
//        LOGGER.info("sendWxRequest success url:{} param:{} result:{}", url, JacksonUtil.obj2String(param), result);
//        if (jsonObject != null && !jsonObject.getBoolean("ret")) {
//            LOGGER.error("sendWxNotify error url:{} param:{} result:{}", url, JacksonUtil.obj2String(param), result);
//        }
//    }

    public void saveMsgLog(JID customer, long shopId, long time, boolean isCustomerMsg) {

        int status = isCustomerMsg ? QtQueueStatus.CustomerLast.getCode() : QtQueueStatus.SeatLast.getCode();
        queueMappingDao.updateByNameAndShopId(new Date(time), status, customer.toBareJID(), shopId);
    }

}