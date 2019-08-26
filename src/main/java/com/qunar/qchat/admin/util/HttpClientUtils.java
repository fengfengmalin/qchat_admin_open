package com.qunar.qchat.admin.util;

import com.google.common.base.Splitter;
import com.qunar.qchat.admin.util.http.AbstractHttpClient;
import com.qunar.qchat.admin.util.http.ResponseWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author : mingxing.shao
 * Date : 15-10-19
 *
 */
public class HttpClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);
    private static boolean IS_POOL_DEBUG = false;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36";
    public static final int SO_TIMEOUT = 5000;
    public static final int CONN_TIMEOUT = 3000;

    private static final int MAX_REDIRECTS = 3;//重定向的最大次数
    private static final PoolingHttpClientConnectionManager poolManage;
    private static final RequestConfig requestConfig;

    private static final String HTTP_SCHEMA = "http://";

    static {
        requestConfig = RequestConfig.custom().setSocketTimeout(SO_TIMEOUT).setConnectTimeout(CONN_TIMEOUT)
                .setMaxRedirects(MAX_REDIRECTS).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        poolManage = new PoolingHttpClientConnectionManager();
        poolManage.setMaxTotal(200);
        poolManage.setDefaultMaxPerRoute(50);
    }

    /**
     * 从连接池中获取HttpClient
     *
     * @return HttpClient
     */
    private static HttpClient initHttpClient() {
        return HttpClients.custom().setUserAgent(USER_AGENT).setConnectionTimeToLive(1L, TimeUnit.MINUTES).setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolManage).build();
    }

    /**
     * GET方法
     *
     * @param url url
     * @return Http返回结果
     */
    public static String get(String url) {
        checkArgument(StringUtils.isNotEmpty(url), "url should not be null or empty");
        debug("before get method");
        try {
            // LOGGER.info("调用接口:{},begin", url);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).get(url);

//            DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy",
//                    CookiePolicy.BROWSER_COMPATIBILITY);
//            com.qunar.qchat.admin.util.http.HttpClients.syncClient(1000, 1000).callback(new CallbackAdaptor() {
//                @Override
//                public void onSuccess(ResponseWrapper wrapper) {
//                    if (wrapper != null) {
//                        // jsConf.set(0, wrapper.getContent());
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    super.onFailure(t);
//                    LOGGER.info("调用接口失败{}", t);
//                }
//            }).get(url);
            // LOGGER.info("调用接口:{},end,返回:{}", url, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口:{}, error", url, e);
        }
        return "";
    }

    /**
     * GET方法
     *
     * @param url url
     * @return Http返回结果
     */
    public static String getWithHeader(String url,Map<String,String> headers) {
        checkArgument(StringUtils.isNotEmpty(url), "url should not be null or empty");
        debug("before get method");
        try {
            // LOGGER.info("调用接口:{},begin", url);
            AbstractHttpClient client = com.qunar.qchat.admin.util.http.HttpClients.syncClient(CONN_TIMEOUT,SO_TIMEOUT);
            for (String key : headers.keySet()){
                client.addHeader(key,headers.get(key));
            }

            ResponseWrapper responseWrapper = client.get(url);

            // LOGGER.info("调用接口:{},end,返回:{}", url, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口:{}, error", url, e);
        }
        return "";
    }

    /**
     * https get
     */
    public static String httpsGet(String url) {
        checkArgument(StringUtils.isNotEmpty(url), "url should not be null or empty");
        if (url.startsWith(HTTP_SCHEMA)) {
            return get(url);
        }
        try {
            // LOGGER.info("调用接口:{},begin:", url);
            URL temp = new URL(url);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).httpsGet(temp.getHost(), temp.getFile());
            // LOGGER.info("调用接口:{},end, back:{}", url, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口:{}, error.", url, e);
        }
        return "";
    }

    public static String get(String url, Map<String, String> params) {
        checkArgument((StringUtils.isNotEmpty(url) && params != null),
                "url should not be null or empty and params should not be empty");
        try {
            // LOGGER.info("调用接口{},参数:{},begin:", url, params);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).addParameter(params).get(url);
           // LOGGER.info("调用接口{},参数:{},返回:{}", url, params, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url,e);
            LOGGER.error("调用接口{}出错,参数：{}", url, params, e);
        }
        return null;
    }

    /**
     * https get
     */
    public static String httpsGet(String url, Map<String, String> params) {
        checkArgument((StringUtils.isNotEmpty(url) && params != null),
                "url should not be null or empty and params should not be empty");
        if (url.startsWith(HTTP_SCHEMA)) {
            return get(url, params);
        }
        try {
            // LOGGER.info("调用接口：{},参数：{}, begin:", url, params);
            URL temp = new URL(url);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).addParameter(params).httpsGet(temp.getHost(), temp.getFile());
            // LOGGER.info("调用接口：{},参数：{}, 返回:{}", url, params, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口：{},参数：{}, 出错", url, params, e);
        }
        return "";
    }

    private static void recordTimeoutUrl(String url, Exception e) {
        if (e instanceof  java.net.SocketTimeoutException) {

                LOGGER.info("keyMapEnum ....", e);

        }
    }

    /**
     * POST方法
     *
     * @param url  url
     * @param params form表单里的参数
     * @return Http返回结果
     */
    public static String post(String url, Map<String, String> params) {
        checkArgument((StringUtils.isNotEmpty(url) && params != null),
                "url should not be null or empty,and form should not be null");
        debug("before post form");
        try {
            // LOGGER.info("调用接口：{},参数：{}, begin:", url, params);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).addParameter(params).post(url);
            // LOGGER.info("调用接口：{},参数：{}, 返回：{}", url, params, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口：{},参数：{},出错", url, params, e);
        }
        return "";
    }

    /**
     * https post
     */
    public static String httpsPost(String url, Map<String, String> params) {
        checkArgument((StringUtils.isNotEmpty(url) && params != null),
                "url should not be null or empty,and form should not be null");
        debug("before post form");
        if (url.startsWith(HTTP_SCHEMA)) {
            return post(url, params);
        }
        try {
            // LOGGER.info("调用接口：{},参数：{}, begin:", url, params);
            URL temp = new URL(url);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).addParameter(params)
                    .httpsPost(temp.getHost(), temp.getFile());
            // LOGGER.info("调用接口：{},参数：{}, 返回：{}", url, params, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口：{},参数：{},出错", url, params, e);
        }
        return "";
    }
    
    public static String post(String url) {
        checkArgument((StringUtils.isNotEmpty(url)), "url should not be null or empty");
        debug("before post form");
        try {
            // LOGGER.info("调用接口：{},begin：", url);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).post(url);
            // LOGGER.info("调用接口：{}, 返回：{}", url, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("调用接口：{}出错", url, e);
        }
        return "";
    }

    /**
     * post方法传递json
     *
     * @param url      url
     * @param jsonBody json参数
     * @return Http返回结果
     */
    public static String postJson(String url, String jsonBody) {
        checkArgument((StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(jsonBody)), "url or jsonBody should not be null or empty");
        debug("before post json");
        HttpClient hc = initHttpClient();
        HttpPost post = new HttpPost(url);
        HttpEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        String res = null;
        try {
            HttpResponse response = hc.execute(post);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                res = EntityUtils.toString(response.getEntity());
            } else {
                LOGGER.info("post json is not ok,status:{},url:{},jsonBody:{}", status, url, jsonBody);
            }
        } catch (IOException e) {
            recordTimeoutUrl(url, e);
            LOGGER.info("post json error,url:{},jsonBody:{}", url, jsonBody, e);
        } finally {
            post.releaseConnection();
            debug("end post json");
        }
        return res;
    }

    public static String newPostJson(String url, String json) {
        checkArgument((StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(json)),
                "url or jsonBody should not be null or empty");
        debug("before post json");
        try {
            // LOGGER.info("请求接口：{}，参数：{}", url, json);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).addHeader("Content-Type", "application/json").post(url, json);
            // LOGGER.info("请求接口：{}，参数：{}，结果：{}", url, json, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("请求接口：{}失败，参数：{}", url, json, e);
        }
        return "";
    }

    /**
     * https post方法传递json
     */
    public static String httpsPostJson(String url, String json) {
        checkArgument((StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(json)),
                "url or jsonBody should not be null or empty");
        debug("before post json");
        if (url.startsWith(HTTP_SCHEMA)) {
            return newPostJson(url, json);
        }
        try {
            // LOGGER.info("请求接口：{}，参数：{}", url, json);
            URL temp = new URL(url);
            ResponseWrapper responseWrapper = com.qunar.qchat.admin.util.http.HttpClients
                    .syncClient(CONN_TIMEOUT, SO_TIMEOUT).httpsPost(temp.getHost(), temp.getFile(), json);
            // LOGGER.info("请求接口：{}，参数：{}，结果：{}", url, json, responseWrapper.getContent());
            if (responseWrapper != null && responseWrapper.getStatus() == HttpStatus.SC_OK) {
                return responseWrapper.getContent();
            }
        } catch (Exception e) {
            recordTimeoutUrl(url, e);
            LOGGER.error("请求接口：{}失败，参数：{}", url, json, e);
        }
        return "";
    }

    /**
     * POST方法上传单个文件
     *
     * @param url   url
     * @param bytes 文件的二进制码
     * @param name  文件的名字
     * @return Http返回结果
     */
    public static String postFile(String url, byte[] bytes, String name) {
        checkArgument((StringUtils.isNotEmpty(url) && bytes != null && bytes.length > 0 && StringUtils.isNotEmpty(name)),
                "url or files should not be null or empty");
        Map<String, byte[]> fileHolder = new HashMap<>();
        fileHolder.put(name, bytes);
        return postFiles(url, fileHolder);
    }

    /**
     * POST方法上传多个文件
     *
     * @param url   url
     * @param files 文件，包括文件名字和二进制吗
     * @return Http返回结果
     */
    public static String postFiles(String url, Map<String, byte[]> files) {
        checkArgument((StringUtils.isNotEmpty(url) && files != null && files.size() != 0), "url or files should not be null or empty");
        debug("before post bytes");
        HttpClient hc = initHttpClient();
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            entityBuilder.addPart("file", new ByteArrayBody(entry.getValue(), entry.getKey()));
        }
        post.setEntity(entityBuilder.build());
        HttpResponse response;
        String res = null;
        try {
            response = hc.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                res = EntityUtils.toString(response.getEntity());
                LOGGER.debug(res);
            } else {
                LOGGER.info("post bytes is not ok,status:{},url:{}", statusCode, url);
            }
        } catch (IOException e) {
            recordTimeoutUrl(url, e);
            LOGGER.info("post bytes error,url:{}", url, e);
        } finally {
            post.releaseConnection();
            debug("after post bytes");
        }
        return res;
    }

    /**
     * 对一个url里的参数进行编码，可以把整个url整个传进来
     * @param urlStr　full url
     * @return encode full url
     */
    public static String encode(String urlStr) {
        if (StringUtils.isBlank(urlStr)) {
            return urlStr;
        }

        int indexOfParam = urlStr.indexOf('?');

        if (indexOfParam < 0 || indexOfParam == urlStr.length()) {
            return urlStr;
        }

        String params = urlStr.substring(indexOfParam + 1);

        Map<String,String> pkvMap = Splitter.on('&').trimResults().omitEmptyStrings().withKeyValueSeparator('=').split(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry : pkvMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(encodeUrl(entry.getKey())).append("=").append(encodeUrl(entry.getValue()));
        }
        return StringUtils.join(urlStr.substring(0, indexOfParam + 1), sb.toString());
    }

    private static String encodeUrl(String params) {
        try {
            return URLEncoder.encode(params, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("encode {} error", params, e);
            return params;
        }
    }

//    private static List<NameValuePair> buildParams(Map<String, String> form) {
//        List<NameValuePair> params = new ArrayList<>();
//        if (form == null || form.size() == 0) {
//            return params;
//        }
//        for (Map.Entry<String, String> entry : form.entrySet()) {
//            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
//            params.add(pair);
//        }
//        return params;
//
//    }

    private static void debug(String position) {
        if (!IS_POOL_DEBUG) {
            return;
        }
        PoolStats poolStats = poolManage.getTotalStats();
        LOGGER.info("{} >> [leased:{}]--[pending:{}]--[available:{}]--[max:{}]", position, poolStats.getLeased(), poolStats.getPending(),
                poolStats.getAvailable(), poolStats.getMax());
    }

    /**
     * 设置每次请求在INFO级别观察连接池状况
     *
     * @param isDebug 是否INFO级别观察连接池状况
     */
    @SuppressWarnings("unused")
    public static void isPoolDebug(boolean isDebug) {
        IS_POOL_DEBUG = isDebug;
    }

}