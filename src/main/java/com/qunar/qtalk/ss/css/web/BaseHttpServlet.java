package com.qunar.qtalk.ss.css.web;

import com.alibaba.fastjson.JSONObject;
import com.qunar.qtalk.ss.utils.common.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

public class BaseHttpServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BaseHttpServlet.class);

    String takeBody(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();

        String str, wholeStr = "";
        while ((str = br.readLine()) != null) {
            wholeStr += str;
        }
        return wholeStr;
    }


    public Long getParameter(HttpServletRequest request, String key, Long defVal) {
        Long xyz = defVal;
        try {
            xyz = Long.parseLong(request.getParameter(key));
        } catch (Exception ex) {
            logger.warn("转换失败", ex);
        }
        return xyz;
    }


    public Integer getParameter(HttpServletRequest request, String key, Integer defVal) {
        Integer xyz = defVal;
        try {
            xyz = Integer.parseInt(request.getParameter(key));
        } catch (Exception ex) {
            logger.warn("转换失败", ex);
        }
        return xyz;
    }

    public static String parseReqeustBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();

        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        reader.close();

        return sb.toString();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    public Map<String, Object> noGetRequestResponse() {
        Map<String, Object> responseObj = new Hashtable<>();
        responseObj.put("errcode", Integer.valueOf(405));
        responseObj.put("errmsg", "不支持get方式请求!");
        return responseObj;
    }

    public Map<String, Object> OKResponse(Map<String, Object> inputParam) {
        Map<String, Object> responseObj = new Hashtable<>();

        responseObj.put("errcode", Integer.valueOf(0));
        responseObj.put("errmsg", "操作已成功");

        if (inputParam != null) {
            responseObj.putAll(inputParam);
        }

        return responseObj;
    }

    public void getMethodNotAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        packResponse(request, response, 405, "*GET* method not allowed!", null);
    }

    public void packOKResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        packResponse(request, response, 0, null, null);
    }

    public void packOKResponse(HttpServletRequest request, HttpServletResponse response, String text) throws IOException {
        packResponse(request, response, 200, text);

    }

    public void packOKResponse(Map<String, Object> inputMap, HttpServletRequest request, HttpServletResponse response) throws IOException {
        packResponse(request, response, 200, "操作已成功", inputMap);

    }

    public void packResponse(HttpServletRequest request, HttpServletResponse response, int errcode, String errmessage) throws IOException {
        response.setStatus(errcode);
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> resultObj = new Hashtable<>();

        resultObj.put("errcode", errcode);

        if (StringUtils.isNotEmpty(errmessage)) {
            resultObj.put("errmsg", errmessage);
        } else {
            if (errcode == 0) {
                resultObj.put("errmsg", "操作已成功.");
            }
        }

        OutputStream out = response.getOutputStream();
        try {
            out.write(errmessage.getBytes());
        } catch (IOException e) {
            throw e;
        } finally {
            out.close();
        }
    }

    public void packResponse(HttpServletRequest req, HttpServletResponse resp, int errcode, String errmessage, Map<String, Object> jsonObject) throws IOException {
        resp.setStatus(200);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> resultObj = new Hashtable<>();

        resultObj.put("errcode", errcode);

        if (StringUtils.isNotEmpty(errmessage)) {
            resultObj.put("errmsg", errmessage);
        } else {
            if (errcode == 0) {
                resultObj.put("errmsg", "操作已成功.");
            }
        }

        if (jsonObject != null) {
            logger.info("mock result:" + jsonObject.toString());
            resultObj.put("data", jsonObject);
        }

        String jsonString = JsonUtil.toJSONString(resultObj);

        req.setAttribute("RESPONSE", jsonString.replaceAll("[\\r\\n]", ""));
        OutputStream out = resp.getOutputStream();
        try {
            out.write(jsonString.getBytes());
        } catch (IOException e) {
            throw e;
        } finally {
            out.close();
        }

    }

    public void packResponse(HttpServletRequest req, HttpServletResponse resp, JSONObject jsonObject) throws IOException {

        logger.info("mock result:" + jsonObject.toString());


        req.setAttribute("RESPONSE", jsonObject.toJSONString().replaceAll("[\\r\\n]", ""));
        resp.setStatus(200);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        OutputStream out = resp.getOutputStream();
        try {
            out.write(jsonObject.toJSONString().getBytes());
        } catch (IOException e) {
            throw e;
        } finally {
            out.close();
        }
    }

    public static void main(String[] args) {

    }

}
//    /**
//     * 输出page对象到json
//     *
//     * @param request
//     * @param response
//     * @param pageInfo
//     * @throws IOException
//     */
//    public void packResponse(HttpServletRequest request, HttpServletResponse response, PageInfo pageInfo) throws IOException {
//        JSONObject jsonObject = jqGridJson(pageInfo);
//        packResponse(request, response, jsonObject);
//    }
//
//    private JSONObject jqGridJson(PageInfo page) {
//        JSONObject json = new JSONObject();
//        if (page == null) {
//            json.put("total", 0);
//            json.put("records", 0);
//            json.put("page", 1);
//            json.put("rows", ArrayUtils.EMPTY_OBJECT_ARRAY);
//            return json;
//        }
//        json.put("total", page.getTotalPage());
//        json.put("page", page.getPageNo());
//        json.put("records", page.getTotalCount());
//
//
//        List dataList = page.getDataList();
//        if (dataList != null && dataList.size() > 0) {
//            JSONArray jsonArray = new JSONArray();
//            for (Object item : dataList) {
//                jsonArray.add(JSON.toJSON(item));
//            }
//            json.put("rows", jsonArray);
//        } else {
//            json.put("rows", ArrayUtils.EMPTY_OBJECT_ARRAY);
//        }
//        return json;
//    }

