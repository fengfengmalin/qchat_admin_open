package com.qunar.qchat.admin.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by qyhw on 10/19/15.
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);


    public static void serializer(JsonResultVO vo, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(JacksonUtils.obj2String(vo));
        } catch (IOException e) {
            logger.info("fail to serializer Json ", e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void responseStr(String msg, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            logger.info("fail to response",e);
        }

        try {
            response.getWriter().close();
        } catch (IOException e) {
            logger.error("responseStr error", e);
        }
    }

    @Deprecated
    public static String getString(JSONObject jsonObject,String key){
        if(jsonObject == null || key == null) {
            return null;
        }

        if(jsonObject.get(key) == null) {
            return null;
        }
        return jsonObject.getString(key);
    }

    @Deprecated
    public static Integer getInteger(JSONObject jsonObject,String key){
        if(jsonObject == null || key == null) {
            return null;
        }

        if(jsonObject.get(key) == null) {
            return null;
        }
        return jsonObject.getInteger(key);
    }

    @Deprecated
    public static Long getLong(JSONObject jsonObject,String key){
        if(jsonObject == null || key == null) {
            return null;
        }

        if(jsonObject.get(key) == null) {
            return null;
        }
        return jsonObject.getLong(key);
    }

    public static Map<String, Object> json2Map(String json) {
        try {
            return JacksonUtils.string2Obj(json,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            logger.error("解析json发生错误:{}", json, e);
        }
        return Maps.newHashMap();
    }
}
