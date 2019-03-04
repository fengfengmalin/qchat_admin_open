package com.qunar.chat.common.util;

import org.apache.commons.lang3.StringUtils;

public class SendMessage {


    public static String appendQCDomain(String str, String host) {
        if (!StringUtils.contains(str, "@")) {
            return String.format("%s@%s", str, host);
        } else {
            return str;
        }
    }
}
