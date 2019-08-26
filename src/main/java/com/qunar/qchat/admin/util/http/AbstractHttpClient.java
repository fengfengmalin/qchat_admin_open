package com.qunar.qchat.admin.util.http;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.qunar.qchat.admin.util.http.ext.AuthSSLProtocolSocketFactory;
import com.qunar.qchat.admin.util.http.ext.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.qunar.qchat.admin.util.http.AbstractHttpClient.DefaultConfig.*;


/**
 * 封装了apache的HttpClient，简化同步，异步，https，代理设置，get/post请求
 * 等常用的http操作。请参考如下wiki:
 *
 * @since 1.0.0
 * @author kris.zhang
 * @author mengfan.feng
 */

public abstract class AbstractHttpClient {

    private final static Logger logger = LoggerFactory.getLogger(AbstractHttpClient.class);

    static class DefaultConfig {
        final static String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; InfoPath.2)";
        final static String DEFAULT_CHARSET = "UTF-8";
        final static int DEFAULT_SOTIME = 60000;
        final static int DEFAULT_CONNTIME = 60000;
        final static HttpCallback DEFAULT_CALLBACK = new CallbackAdaptor();
        final static String DEFAULT_COOKIE_POLICY = CookiePolicy.BROWSER_COMPATIBILITY;
        final static int DEFAULT_HTTPS_PORT = 443;
    }

    protected final HttpClient httpClient = new HttpClient();
    protected HttpCallback callBack = DEFAULT_CALLBACK;
    protected Map<String, String> headers = Maps.newHashMap();
    protected HttpMethodRetryHandler retryHandler;
    protected List<NameValuePair> parameters = Lists.newArrayList();
    protected URL keyStoreUrl;
    protected String keyStorePwd;
    protected URL trustStoreUrl;
    protected String trustStorePwd;
    protected String charset = DEFAULT_CHARSET;
    protected String cookie;
    protected String cookiePolicy;
    protected int httpsPort = DEFAULT_HTTPS_PORT;
    protected boolean keepAlive;

    static {
        /* escape the warning */
        ProtocolSocketFactory sslFactory = new EasySSLProtocolSocketFactory();
        Protocol https = new Protocol("https", sslFactory, 443);
        Protocol.registerProtocol("https", https);
    }

    AbstractHttpClient() { /** 我们读取默认的配置 */
        connectionTimeout(DEFAULT_CONNTIME);
        socketTimeout(DEFAULT_SOTIME);

    }

    /**
     * 连接超时时间
     *
     * @param millisecond int
     * @return AbstractHttpClient
     */
    public AbstractHttpClient connectionTimeout(int millisecond) {
        checkArgument(millisecond >= 0, "connection time 必须大于等于0");
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(millisecond);
        return this;
    }

    /**
     * 设置socket timeout
     *
     * @param millisecond int
     * @return AbstractHttpClient
     * @deprecated use socketTimeout
     */
    @Deprecated
    public AbstractHttpClient soTimeout(int millisecond) {
        checkArgument(millisecond >= 0, "socket time 必须大于等于0");
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(millisecond);
        return this;
    }

    /**
     * 设置socket timeout
     *
     * @param millisecond int
     * @return AbstractHttpClient
     */
    public AbstractHttpClient socketTimeout(int millisecond) {
        checkArgument(millisecond >= 0, "socket time 必须大于等于0");
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(millisecond);
        return this;
    }

    /**
     * 设置https的端口号，默认为https通用的443
     *
     * @param httpsPort int
     * @return AbstractHttpClient
     */
    public AbstractHttpClient httpsPort(int httpsPort) {
        checkArgument(httpsPort > 0 && httpsPort < 65536 , "端口号必须在(0,65535]之间");
        this.httpsPort = httpsPort;
        return this;
    }

    /**
     * 设置字符编码，默认为utf-8
     *
     * @param charset Charset
     * @return AbstractHttpClient
     */
    public AbstractHttpClient charset(Charset charset) {
        checkNotNull(charset);
        this.charset = charset.name();
        return this;
    }

    /**
     * 设置代理host和端口号
     *
     * @param host String
     * @param port int
     * @return AbstractHttpClient
     */
    public AbstractHttpClient proxy(String host, int port) {
        checkArgument(!isNullOrEmpty(host), "host值错误");
        checkArgument(port > 0 && port < 65535, "端口号需要在(0,65535)之间");
        HostConfiguration config = httpClient.getHostConfiguration();
        config.setProxy(host, port);
        config.getParams().setParameter(
                "http.default-headers",
                Lists.newArrayList(new Header("User-Agent", DEFAULT_USER_AGENT))
        );
        return this;
    }

    /**
     * 设置cookie，默认的cookie策略：BROWSER_COMPATIBILITY
     *
     * @param cookie String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient cookie(String cookie) {
        return cookie(cookie, DEFAULT_COOKIE_POLICY);
    }

    /**
     * 设置cookie以及cookie策略
     * {@link CookiePolicy}
     *
     * @param cookie String
     * @param policy String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient cookie(String cookie, String policy) {
        checkArgument(!isNullOrEmpty(cookie), "cookie值错误");
        checkArgument(!isNullOrEmpty(policy), "policy值错误");
        this.cookie = cookie;
        this.cookiePolicy = policy;
        return this;
    }

    /**
     * 设置cookie策略
     * {@link CookiePolicy}
     *
     * @param policy String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient cookiePolicy(String policy) {
        checkArgument(!isNullOrEmpty(policy), "policy值错误");
        this.cookiePolicy = policy;
        return this;
    }

    /**
     * 增加request header
     *
     * @param headerName String
     * @param headerValue String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient addHeader(String headerName, String headerValue) {
        checkArgument(!isNullOrEmpty(headerName), "headerName值错误");
        checkArgument(!isNullOrEmpty(headerValue), "headerValue值错误");
        headers.put(headerName, headerValue);
        return this;
    }

    /**
     * 注入出错重试处理器
     *
     * @param retryHandler HttpMethodRetryHandler
     * @return AbstractHttpClient
     */
    public AbstractHttpClient retry(HttpMethodRetryHandler retryHandler) {
        checkNotNull(retryHandler, "retryHandler不能为null");
        this.retryHandler = retryHandler;
        return this;
    }

    /**
     * 注入回调
     *
     * @param callBack HttpCallback
     * @return AbstractHttpClient
     */
    public AbstractHttpClient callback(HttpCallback callBack) {
        checkNotNull(callBack, "callback不能为null");
        this.callBack = callBack;
        return this;
    }

    /**
     * 增加请求参数，根据get或者post能够自动组装
     *
     * @param key String
     * @param value String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient addParameter(String key, String value) {
        checkNotNull(key, "key不能为null");
        checkNotNull(value, "value不能为null");
        parameters.add(new NameValuePair(key, value));
        return this;
    }

    /**
     * 增加请求参数，根据get或者post能够自动组装
     *
     * @param parameters Map
     * @return AbstractHttpClient
     */
    public AbstractHttpClient addParameter(Map<String, String> parameters) {
        checkNotNull(parameters, "parameters不能为null");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            this.parameters.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    /**
     * 设置是否keepAlive，目前都是短连接
     *
     * @return AbstractHttpClient
     */
    public AbstractHttpClient keepAlive() {
        this.keepAlive = true;
        return this;
    }

    /**
     * 设定key store的url和密码
     *
     * @param url String
     * @param password String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient keyStore(String url, String password) {
        checkArgument(!Strings.isNullOrEmpty(url), "key store的url不能为空");
        checkArgument(!Strings.isNullOrEmpty(password), "key store的密码不能为空");
        try {
            this.keyStoreUrl = new URL(url);
            this.keyStorePwd = password;
        } catch (MalformedURLException e) {
            Throwables.propagate(e);
        }
        return this;
    }

    /**
     * 设定 trust store的url和密码
     * @param url String
     * @param password String
     * @return AbstractHttpClient
     */
    public AbstractHttpClient trustStore(String url, String password) {
        checkArgument(!Strings.isNullOrEmpty(url), "trust store的url不能为空");
        checkArgument(!Strings.isNullOrEmpty(password), "trust store的密码不能为空");
        try {
            this.trustStoreUrl = new URL(url);
            this.trustStorePwd = password;
        } catch (MalformedURLException e) {
            Throwables.propagate(e);
        }
        return this;
    }

    protected PostMethod buildPostMethod(String uri, String content) {
        PostMethod method = new PostMethod(uri);
        buildCommons(method);
        try {
            if (!parameters.isEmpty()) { //parameters first
                method.setRequestBody(parameters.toArray(new NameValuePair[0]));
            }
            if (content != null) {
                method.setRequestEntity(new StringRequestEntity(content,
                        "application/x-www-form-urlencoded", charset));
            }
        } catch (UnsupportedEncodingException e) {  //we swallow this exp
            Throwables.propagate(e);
        }
        return method;
    }

    protected GetMethod buildGetMethod(String uri) {
        if (!parameters.isEmpty()) {
            StringBuilder sb = new StringBuilder(uri);
            sb.append("?");
            for (int i = 0; i < parameters.size(); i++) {
                NameValuePair kv = parameters.get(i);
                if (i != 0) {
                    sb.append("&");
                }
                sb.append(urlEncode(kv.getName())).append("=").append(urlEncode(kv.getValue()));
            }
            uri = sb.toString();
        }

        GetMethod method = new GetMethod(uri);
        buildCommons(method);
        return method;
    }

    private String urlEncode(String queryString) {
        try {
            return URLEncoder.encode(queryString,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    //post and get方法都需要构建的信息
    private void buildCommons(HttpMethod method) {
        trySetDefaults(method);
        appendHeaders(method);
    }

    private void trySetDefaults(HttpMethod method) {
        HttpMethodParams params = method.getParams();
        params.setContentCharset(charset);
        if (retryHandler != null) {
            params.setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
        }
        if (!headers.containsKey("Accept-Language")) {
            addHeader("Accept-Language", "zh-cn");
        }
        if (!headers.containsKey("User-Agent")) {
            addHeader("User-Agent", DEFAULT_USER_AGENT);
        }
        if (!headers.containsKey("Connection") && keepAlive) {
            addHeader("Connection", " Keep-Alive");
        }
        if (!isNullOrEmpty(cookie)) {
            method.setRequestHeader("cookie", cookie);
        }
        if (!isNullOrEmpty(cookiePolicy)) {
            params.setCookiePolicy(DEFAULT_COOKIE_POLICY);
        }
    }

    private void appendHeaders(HttpMethod method) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            method.setRequestHeader(entry.getKey(), entry.getValue());
        }
    }

    private boolean trySetAuthSSLFactory(String host, String path) {
        checkArgument(!isNullOrEmpty(host),"host不能为空");
        checkArgument(!isNullOrEmpty(path),"path不能为空");

        if (this.trustStorePwd != null && this.trustStoreUrl != null &&
                this.keyStorePwd != null && this.keyStoreUrl != null) {

            ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(
                    keyStoreUrl, keyStorePwd,
                    trustStoreUrl, trustStorePwd
            );

            httpClient.getHostConfiguration()
                    .setHost(host, httpsPort, new Protocol("https", factory, httpsPort));

            return true;
        }
        logger.info("未设定trust store和keystroe，将按照默认证书校验处理");
        return false;
    }

    private String combineHttpsURI(String host, String path) {
        if (path.startsWith("/")) {
            return "https://" + host + path;
        } else {
            return "https://" + host + "/" + path;
        }
    }


    /**
     * 该方法兼容https端口号不是443的情况
     * @param host
     * @param path
     * @return
     * @since 1.2.9
     */
    private String combineHttpsURIWithPort(String host, String path) {
        if (httpsPort == DEFAULT_HTTPS_PORT) {
            return this.combineHttpsURI(host, path);
        } else {
            if (path.startsWith("/")) {
                return "https://" + host + ":" + httpsPort + path;
            } else {
                return "https://" + host + ":" + httpsPort + "/" + path;
            }
        }
    }

    /**
     * 支持https的同步get
     *
     * @param host 主机名如：user.qunar.com
     * @param path 地址路径如：/passport/login.jsp
     * @return ResponseWrapper
     */
    public ResponseWrapper httpsGet(String host, String path) {
        if (!trySetAuthSSLFactory(host, path)) {
            return get(combineHttpsURI(host, path));
        }
        return get(path);
    }

    /**
     * 支持https的异步get
     *
     * @param host 主机名如：user.qunar.com
     * @param path 地址路径如：/passport/login.jsp
     * @return ListenableFuture
     */
    public ListenableFuture<ResponseWrapper> httpsAsyncGet(String host, String path) {
        if (!trySetAuthSSLFactory(host, path)) {
            return asyncGet(combineHttpsURI(host, path));
        }
        return asyncGet(path);
    }

    /**
     * 支持https的同步post
     *
     * @param host 主机名如：user.qunar.com
     * @param path 地址路径如：/passport/login.jsp
     * @return ResponseWrapper
     */
    public ResponseWrapper httpsPost(String host, String path) {
        if (!trySetAuthSSLFactory(host, path)) {
            return post(combineHttpsURI(host, path));
        }
        return post(path);
    }

    /**
     * 支持https的同步post
     *
     * @param host 主机名如：user.qunar.com
     * @param path 地址路径如：/passport/login.jsp
     * @param content request body
     * @return ResponseWrapper
     */
    public ResponseWrapper httpsPost(String host, String path,String content) {
        if (!trySetAuthSSLFactory(host, path)) {
            return post(combineHttpsURIWithPort(host, path), content);
        }
        return post(path,content);
    }

    /**
     * 支持https的异步post
     *
     * @param host 主机名如：user.qunar.com
     * @param path 地址路径如：/passport/login.jsp
     * @return ListenableFuture
     */
    public ListenableFuture<ResponseWrapper> httpsAsyncPost(String host, String path) {
        if (!trySetAuthSSLFactory(host, path)) {
            return asyncPost(combineHttpsURI(host, path));
        }
        return asyncPost(path);
    }

    /**
     * 支持https的异步post
     *
     * @param host 主机名如：user.qunar.com
     * @param path 地址路径如：/passport/login.jsp
     * @param content request body
     * @return ListenableFuture
     */
    public ListenableFuture<ResponseWrapper> httpsAsyncPost(String host, String path,String content) {
        if (!trySetAuthSSLFactory(host, path)) {
            return asyncPost(combineHttpsURI(host, path),content);
        }
        return asyncPost(path,content);
    }

    /**
     * 同步get
     *
     * @param uri String
     * @return ResponseWrapper
     */
    public abstract ResponseWrapper get(String uri);

    /**
     * 同步post
     *
     * @param uri String
     * @param content String
     * @return ResponseWrapper
     */
    public abstract ResponseWrapper post(String uri, String content);

    /**
     * 同步post
     *
     * @param uri String
     * @return ResponseWrapper
     */
    public abstract ResponseWrapper post(String uri);

    /**
     * 异步get
     *
     * @param uri String
     * @return ListenableFuture
     */
    public abstract ListenableFuture<ResponseWrapper> asyncGet(String uri);

    /**
     * 异步post
     *
     * @param uri String
     * @param content String
     * @return ListenableFuture
     */
    public abstract ListenableFuture<ResponseWrapper> asyncPost(String uri, String content);

    /**
     * 异步post
     *
     * @param uri String
     * @return ListenableFuture
     */
    public abstract ListenableFuture<ResponseWrapper> asyncPost(String uri);

}
