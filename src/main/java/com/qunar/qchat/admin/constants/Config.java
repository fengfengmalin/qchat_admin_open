package com.qunar.qchat.admin.constants;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.vo.conf.Conf;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Author : mingxing.shao
 * Date : 15-10-19
 *
 */
@Component
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static Properties props;

    private static Map<String, String> qConfigMap;
    private static Map<String,String> kv;

    private static String accessControlConf;

    public static final String USER_CENTER_INFO_URL = Config.getProperty("user.center.info.url");
    public static final String BNB_USER_INFO_URL = Config.getProperty("bnb.user.info.url");

    public static final String SESSION_DETAIL_URL = Config.getProperty("session.detail.url");

    public static final String SEND_NOTE_URL = Config.getProperty("send.note.url");


    public static final String QCHAT_HISTORY_MSG_SEARCH_URL = Config.getProperty("history.msg.search.url");


    public static final String NEW_SUPPLIERROBOT_URL = Config.getProperty("new.supplierrobot.url");

    public static long SEAT_POLLING_TIME = 60;



    private synchronized static void init() {
        if (props != null) {
            return;
        }
        InputStreamReader isr = null;
        try {
            String filename = "qchatadmin.properties";
            isr = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream(filename), "UTF-8");
            props = new Properties();

            props.load(isr);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        } finally {
            closeStream(isr);
        }
    }

    private synchronized static void initQConfig() {
        InputStreamReader isr = null;
        Properties temp;
        try {
            String filename = "qchat-qconfig.properties";
            isr = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream(filename), "UTF-8");
            qConfigMap = new HashMap<>();
            temp = new Properties();
            temp.load(isr);
            for (String key : temp.stringPropertyNames()) {
                qConfigMap.put(key, temp.getProperty(key));
            }
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

    private static final Function<String, Integer> toInteger = new Function<String, Integer>() {
        @Override
        public Integer apply(String s) {
            return StringUtils.isNotEmpty(s) ? Integer.valueOf(s) : null;
        }
    };
    private static final Function<String, Long> toLong = new Function<String, Long>() {
        @Override
        public Long apply(String s) {
            return StringUtils.isNotEmpty(s) ? Long.valueOf(s) : null;
        }
    };

    public static List<Integer> getIntegerListQConfig(String item, Conf conf) {
        List<String> listItem = getStringListQConfig(item, conf);
        if (CollectionUtils.isEmpty(listItem)) {
            return Lists.newArrayList();
        }
        return Lists.transform(listItem, toInteger);
    }

    public static List<Long> getLongListQConfig(String item, Conf conf) {
        List<String> listItem = getStringListQConfig(item, conf);
        if (CollectionUtils.isEmpty(listItem)) {
            return Lists.newArrayList();
        }
        return Lists.transform(listItem, toLong);
    }

    public static List<String> getStringListQConfig(String item, Conf conf) {
        String str = conf.getString(item, "");
        if (Strings.isNullOrEmpty(str)) {
            return Lists.newArrayList();
        }
        return Splitter.on(",").omitEmptyStrings().splitToList(str);
    }

    public static void setProperty(String name, String value) {
        if (props == null) {
            init();
        }

        props.setProperty(name, value);
    }


    public static String getPropertyInQConfig(String name, String defaultValue) {
        if (qConfigMap == null) {
            initQConfig();
        }
        String value = qConfigMap.get(name);
        if (StringUtils.isEmpty(value) && StringUtils.isNotEmpty(defaultValue)) {
            value = defaultValue;
        }
        return value;
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

    public static String getAccessControlConf() {
        return accessControlConf;
    }



    public static String urlOfSessionBusiCallback(String busiEnName){
        if (null == kv){
            kv = Maps.newHashMap();
        }

        String key = "session.busicallback." + busiEnName;

        if (!kv.containsKey(key))
            kv.put(key,Config.getProperty(key));

        return kv.get(key);
    }
}
