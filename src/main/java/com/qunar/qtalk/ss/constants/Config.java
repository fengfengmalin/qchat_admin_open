package com.qunar.qtalk.ss.constants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Author : mingxing.shao
 * Date : 16-3-29
 *
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private static Properties props;


    public static final String QCHAT_SEND_URL = getProperty("qchat.send.url");

    public static final int Consult_SeatsNotifyTime = Integer.parseInt(getProperty("qchat.consult.seat.notify.time", "120")) * 1000;
    public static final int Consult_CustomerNotifyTime = Integer.parseInt(getProperty("qchat.consult.customer.notify.time", "900")) * 1000;
    public static final String QCHAT_COOKIE_DOMAIN = getProperty("qchat.cookie.domain", "127.0.0.1");

    public static final int Consult_ReleasedTime = Integer.parseInt(getProperty("qchat.consult.released.time", "900")) * 1000;
    public static final int Consult_RecycledTime = Integer.parseInt(getProperty("qchat.consult.recycled.time", "86400")) * 1000;

    public static final int Redis_Released_Session_Time = Integer.parseInt(getProperty("redis.released.session.time", "1440"));


    private synchronized static void init() {
        if (props != null) {
            return;
        }
        InputStreamReader isr = null;
        try {
            String filename = "app.properties";
            isr = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream(filename), "UTF-8");
            props = new Properties();

            props.load(isr);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        } finally {
            closeStream(isr);
        }
    }

    public static String getProperty(String name) {
        if (props == null) {
            init();
        }
        String val = props.getProperty(name.trim());
        if (val == null) {
            return null;
        } else {
            //去除前后端空格
            return val.trim();
        }
    }

    public static String getProperty(String name, String defaultValue) {
        if (props == null) {
            init();
        }

        String value = getProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        return value.trim();
    }

    //获得整数属性值
    public static int getIntProperty(String name, int defaultVal) {
        if (props == null) {
            init();
        }

        int val = defaultVal;
        String valStr = getProperty(name);
        if (valStr != null) {
            val = Integer.parseInt(valStr);
        }
        return val;
    }

    //获得double属性值
    public static double getDoubleProperty(String name, double defaultVal) {
        if (props == null) {
            init();
        }

        double val = defaultVal;
        String valStr = getProperty(name);
        if (valStr != null) {
            val = Double.parseDouble(valStr);
        }
        return val;
    }

    public static boolean getBooleanItem(String name, boolean defaultValue) {
        if (props == null) {
            init();
        }

        boolean b = defaultValue;
        String valStr = getProperty(name);
        if (valStr != null) {
            b = Boolean.parseBoolean(valStr);
        }
        return b;
    }

    public static String getPropertyByEncoding(String name, String encoding) {
        if (props == null) {
            init();
        }
        encoding = StringUtils.isEmpty(encoding) ? "UTF-8" : encoding;
        String val = getProperty(name);
        if (val == null) return null;
        try {
            return new String(val.getBytes("ISO8859-1"), encoding);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("UnsupportedEncodingException error", e);
            return val;
        }
    }

    public static String[] getArrayItem(String name) {
        if (props == null) {
            init();
        }

        String value = getProperty(name, "");
        if (value.trim().isEmpty()) {
            return null;
        }

        String sepChar = ",";
        if (value.contains(";")) {
            sepChar = ";";
        }
        return value.split(sepChar);

    }

    public static List<String> getListItem(String item) {
        if (props == null) {
            init();
        }

        List<String> list = new ArrayList<>();
        String value = getProperty(item, "");
        if (value.trim().isEmpty()) {
            return list;
        }

        String sepChar = ",";
        if (value.contains(";")) {
            sepChar = ";";
        }
        String[] sa = value.split(sepChar);
        for (String aSa : sa) {
            list.add(aSa.trim());
        }
        return list;
    }

    public static void setProperty(String name, String value) {
        if (props == null) {
            init();
        }

        props.setProperty(name, value);
    }

    private static void closeStream(InputStreamReader is) {
        if (is == null) {
            return;
        }

        try {
            is.close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        }
    }
}
