package com.qunar.qchat.admin.util.http;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 返回实体封装
 *
 * @since 1.0.0
 * @author kris.zhang
 * @author mengfan.feng
 */

@ToString(exclude = {"bytes"})
public class ResponseWrapper {
    @Setter @Getter
    private String uri;

    @Setter @Getter
    private Integer status;

    @Setter @Getter
    private String charset;

    /**
     * 目前支持 10M = 10 * 1024 * 1024B
     */
    @Setter @Getter
    private byte[] bytes;

    @Setter @Getter
    private String content;

    @Setter @Getter
    private Map<String, Collection<String>> headers;

    ResponseWrapper() {}

    static ResponseWrapper of(HttpMethodBase httpMethod) {
        checkNotNull(httpMethod);
        ResponseWrapper wrapper = new ResponseWrapper();
        try {
            wrapUri(wrapper,httpMethod);
            wrapStatus(wrapper, httpMethod);
            wrapCharset(wrapper, httpMethod);
            wrapBytes(wrapper, httpMethod);
            wrapContent(wrapper, httpMethod);
            wrapHeaders(wrapper, httpMethod);
        } catch (URIException e) {
            //ignored
        } catch (IOException e) {
            //ignored
        }
        return wrapper;
    }

    static void wrapUri(ResponseWrapper wrapper, HttpMethodBase httpMethod) throws URIException {
        wrapper.uri = httpMethod.getURI().getURI();
    }

    static void wrapStatus(ResponseWrapper wrapper, HttpMethodBase httpMethod) {
        wrapper.status = httpMethod.getStatusCode();
    }

    static void wrapCharset(ResponseWrapper wrapper, HttpMethodBase httpMethod) {
        wrapper.charset = httpMethod.getResponseCharSet();
    }

    static void wrapBytes(ResponseWrapper wrapper, HttpMethodBase httpMethod) throws IOException {
        wrapper.bytes = httpMethod.getResponseBody(10 * 1024 * 1024);
    }

    static void wrapContent(ResponseWrapper wrapper, HttpMethodBase httpMethod) throws IOException {
        checkNotNull(wrapper.getBytes());
        Header encodingHeader = httpMethod.getResponseHeader("Content-Encoding");
        InputStream inputStream = new ByteArrayInputStream(wrapper.getBytes());
        if (encodingHeader != null && "gzip".equals(encodingHeader.getValue())) {
            wrapper.content = CharStreams.toString(new InputStreamReader(new GZIPInputStream(inputStream)));
        } else {
            wrapper.content = wrapStream(inputStream, httpMethod.getResponseCharSet());
        }
    }

    static String wrapStream(InputStream inputStream, String charSet) throws IOException {
        checkNotNull(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charSet));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line + "\n");
        }
        return builder.toString();
    }

    static void wrapHeaders(ResponseWrapper wrapper, HttpMethodBase httpMethod) {
        Header[] headers = httpMethod.getResponseHeaders();
        Multimap<String, String> headerMap;
        if (headers.length > 0) {
            headerMap = ArrayListMultimap.create();
            for (Header header : headers) {
                headerMap.put(header.getName(), header.getValue());
            }
        } else {
            headerMap = ArrayListMultimap.create(2, 2);
        }
        wrapper.headers = headerMap.asMap();
    }
}
