package com.qunar.qchat.admin.util;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by qyhw on 11/20/15.
 */
public class MapUtil {

    /**
     * 获取Map中value类型为String
     * @param m
     * @param key
     * @return
     */
    public static String getMapString(Map m, String key) {
        if(m == null) {return "";}
        if(StringUtils.isEmpty(key)) {return "";}
        Object o = m.get(key);
        if(o == null) {return "";}

        try {
            return (String)m.get(key);
        } catch (Exception e) {
            return "";
        }

    }


    /**
     * 获取Map中value类型为Integer
     * @param m
     * @param key
     * @return 如果没有取到,默认返回0
     */
    public static int getMapInteger(Map m, String key) {
        if(m == null) {return 0;}
        if(StringUtils.isEmpty(key)) {return 0;}
        Object o = m.get(key);
        if(o == null) {return 0;}
        try {
            return (Integer)m.get(key);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将Map 转成 String,  转化后格式: key = value , key = value , key = value
     * @param m
     * @return
     */
    public static String toString(Map m) {

        return Joiner.on(" , ").withKeyValueSeparator(" = ").join(m);
    }

}
