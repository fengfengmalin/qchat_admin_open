package com.qunar.qtalk.ss.css.web;


import com.qunar.qtalk.ss.utils.common.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class QTalkBaseServlet extends BaseHttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        long t1 = System.currentTimeMillis();
        String type = request.getPathInfo();

        Map<String, String[]> requestParameterMap = request.getParameterMap();
        String data = parseReqeustBody(request);

        Map<String, Object> var = null;

        if (StringUtils.isNoneEmpty(data)) {
            var = JsonUtil.parseJSONObject(data);
        }
        if (!onPost(type, var, requestParameterMap, request, response)) {
            packResponse(request, response, 404, "服务器未知错误", null);
        }
    }

    protected abstract boolean onPost(String method, Map<String, Object> inputMap, Map<String, String[]> requestParameterMap, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    protected abstract boolean onGet(String type, Map<String, String[]> requestParameterMap, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    protected abstract boolean isSupportGetRequest();


    protected static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean isSupportGetRequest = isSupportGetRequest();

        if (isSupportGetRequest) {
            long t1 = System.currentTimeMillis();
            String type = request.getPathInfo();

            Map<String, String[]> requestParameterMap = request.getParameterMap();
            if (!onGet(type, requestParameterMap, request, response)) {
                packResponse(request, response, 404, "服务器未知错误", null);
            }
        }
    }


}
