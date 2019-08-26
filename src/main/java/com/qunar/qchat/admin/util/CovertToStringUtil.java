package com.qunar.qchat.admin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by qyhw on 12/6/15.
 */
public class CovertToStringUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CovertToStringUtil.class);
    /**
     * 将请求参数拼接字符串    格式为: key:value;key:value;key:value;
     * @param request
     * @return
     */
    public static String convertRequestParaToString(HttpServletRequest request) {
        try {
            StringBuffer content = new StringBuffer();
            Map map = request.getParameterMap();
            Set<Map.Entry<String, String[]>> set = map.entrySet();
            Iterator<Map.Entry<String, String[]>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> entry = it.next();
                content.append(entry.getKey());
                content.append(":");
                content.append(entry.getValue()[0]);
                content.append(";");
            }
            return content.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将对象属性拼接字符串;  格式为: key:value;key:value;key:value;
     * @param model
     * @return
     */
    public static String convertClassAttrToString(Object model) {
        StringBuffer content = new StringBuffer();
        Field[] fieldList = model.getClass().getDeclaredFields();
        for(Field field : fieldList) {
            String name = field.getName();
            content.append(name);
            content.append(":");

            String type = field.getGenericType().toString();
            try {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                String value = null;
                Method m = model.getClass().getMethod("get" + name);
                if (type.equals("class java.lang.String")) {
                    value = (String) m.invoke(model);
                }
                if (type.equals("long")) {
                    value = String.valueOf((Long) m.invoke(model));
                }
                if (type.equals("int")) {
                    value = String.valueOf((Integer) m.invoke(model));
                }
                content.append(value);
                content.append(";");
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                LOGGER.error("convertClassAttrToString error", e);
            }
        }
        return content.toString();
    }
}
