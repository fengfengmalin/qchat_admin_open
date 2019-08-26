package com.qunar.qchat.admin.util;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import org.apache.http.util.TextUtils;

import java.util.List;
import java.util.Map;

public class EjabdUtil {
    public static String makeSureUserJid(String jid, String defaultDomain) {
        if (Strings.isNullOrEmpty(jid))
            return "";

        if (jid.indexOf("/") > 0) {
            jid = jid.substring(0, jid.indexOf("/"));
        }

        if (!jid.contains("@") && !TextUtils.isEmpty(defaultDomain)) {
            jid += "@";
            jid += defaultDomain;
        }

        return jid;
    }

    public static String makeSureUserid(String jid) {
        if (Strings.isNullOrEmpty(jid))
            return "";

        if (jid.indexOf("@") > 0) {
            jid = jid.substring(0, jid.indexOf("@"));
        }
        return jid;
    }

    public static String getUserDomain(String jid, String defaultDomain) {
        if (Strings.isNullOrEmpty(jid))
            return null;

        if (jid.indexOf("/") > 0) {
            jid = jid.substring(0, jid.indexOf("/"));
        }

        if (!jid.contains("@"))
            return defaultDomain;

        return jid.substring(jid.indexOf("@") + 1);
    }

    public static String getDomain(String jid, String defaultDomain) {
        if (Strings.isNullOrEmpty(jid))
            return null;

        if (!jid.endsWith(QChatConstant.QTALK_DOMAIN_POSTFIX) && !jid.endsWith(QChatConstant.QCHAT_HOST_POSTFIX)) {
            return defaultDomain;
        }

        return jid.substring(jid.lastIndexOf("@") + 1);
    }

    public static Map<String, List<String>> spliteUsersByDomain(List<String> userids) {
        Map<String, List<String>> rets = Maps.newHashMap();

        if (!CollectionUtil.isEmpty(userids)) {
            for (String userid : userids) {
                String domian = getUserDomain(userid, QChatConstant.DEFAULT_HOST);
                if (rets.containsKey(domian)) {
                    rets.get(domian).add(userid);
                } else {
                    List<String> ids = Lists.newArrayList();
                    ids.add(userid);
                    rets.put(domian, ids);
                }
            }
        }

        return rets;
    }

    private static final Function<String, String> getQunarName = new Function<String, String>() {
        @Override
        public String apply(String jid) {
            return EjabdUtil.makeSureUserid(jid);
        }
    };
    public static List<String> makeSureUseridList(List<String> jids){
        if (CollectionUtil.isEmpty(jids))
            return null;

        return Lists.transform(jids,getQunarName);
    }

    public static String makeShopName(long shopid){
        return QChatConstant.SEATSHOPPREFIX + shopid;
    }

    public static long getSupplieridFromShopName(String shopName){
        String uid = makeSureUserid(shopName);
        if (Strings.isNullOrEmpty(uid))
            return 0;
        return Long.valueOf(uid.replace(QChatConstant.SEATSHOPPREFIX,""));
    }
}
