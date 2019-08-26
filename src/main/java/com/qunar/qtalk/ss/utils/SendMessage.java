package com.qunar.qtalk.ss.utils;

import com.qunar.qtalk.ss.constants.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class SendMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessage.class);

    public static String sendMessage(String from, String to, String message) {
        Dictionary<String, String> args = new Hashtable();
        args.put("from", appendQCDomain(from));
        args.put("to", appendQCDomain(to));
        args.put("message", message);

        String ret = HttpClientUtils.postJson(Config.QCHAT_SEND_URL, JacksonUtils.obj2String(args));
        LOGGER.info("qchat send url message :{}, ret;{}", JacksonUtils.obj2String(args), ret);

        return ret;
    }

    public static String appendQCDomain(String str) {
        if (!StringUtils.contains(str, "@")) {
            return StringUtils.join(str, "@ejabhost2");
        } else {
            return str;
        }
    }

    public static String appendQCDomain(String str, String host) {
        if (!StringUtils.contains(str, "@")) {
            return String.format("%s@%s", str, host);
        } else {
            return str;
        }
    }
}
