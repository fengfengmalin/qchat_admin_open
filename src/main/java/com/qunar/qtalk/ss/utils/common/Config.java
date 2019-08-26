package com.qunar.qtalk.ss.utils.common;

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

    public static final String REDIS_HOST = getProperty("redis.host");
    public static final String REDIS_PORT = getProperty("redis.port");
    public static final String REDIS_PASS = getProperty("redis.pass");
    public static final String REDIS_DATABASE = getProperty("redis.database");

    public static final int REDIS_POOL_MAXIDLE = getIntProperty("redis.pool.maxIdle", 10);
    public static final int REDIS_POOL_MAXACTIVE = getIntProperty("redis.pool.maxActive", 10);
    public static final int REDIS_POOL_MAXWAITMILLIS = getIntProperty("redis.pool.maxWaitMillis", 100);
    public static final boolean REDIS_POOL_TESTONBORROW = getBooleanItem("redis.pool.testOnBorrow", false);
    public static final boolean REDIS_POOL_TESTONRETURN = getBooleanItem("redis.pool.testOnReturn", false);
    public static final String REDIS_SENTINEL_HOST1 = getProperty("redis.sentinel.host1");
    public static final String REDIS_SENTINEL_HOST2 = getProperty("redis.sentinel.host2");
    public static final String REDIS_SENTINEL_MASTER = getProperty("redis.sentinel.master");
    public static final int REDIS_SENTINEL_PORT = getIntProperty("redis.sentinel.port", 26379);
    public static final String REDIS_SENTINEL_PASS = getProperty("redis.sentinel.pass");


    private synchronized static void init() {
        if (props != null) {
            return;
        }
        InputStreamReader isr = null;
        try {
            String filename = "redis.properties";
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

        String val = getProperty(name);
        if (val == null) return null;
        try {
            return new String(val.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

        List<String> list = new ArrayList();
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
