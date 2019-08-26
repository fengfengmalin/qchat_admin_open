package com.qunar.qchat.admin.util.http;

/**
 * http客户端工厂方法，支持同步异步以及https客户端创建
 *
 * @since 1.0.0
 * @author kris.zhang
 */
public class HttpClients {

    /**
     * 一些常用的 timeout 值，尽量使用这些常量，如果这些常量不满足，再考虑使用自定义的 timeout 值
     */
    public static final int TIMEOUT_SHORT = 5;
    public static final int TIMEOUT_MEDIUM = 30;
    public static final int TIMEOUT_LONG = 120;
    public static final int TIMEOUT_VERY_LONG = 300;

    /**
     * 获取同步http客户端
     *
     * @return AbstractHttpClient
     */
    public static AbstractHttpClient syncClient() {
        return new SyncHttpClient();
    }

    /**
     * 获取同步http客户端
     *
     * @param connTimeout int
     * @param soTimeout int
     * @return AbstractHttpClient
     */
    public static AbstractHttpClient syncClient(int connTimeout, int soTimeout) {
        return new SyncHttpClient().connectionTimeout(connTimeout).soTimeout(soTimeout);
    }

    /**
     * 获取异步http客户端
     *
     * @param connTimeout int
     * @param soTimeout int
     * @return AbstractHttpClient
     */
    public static AbstractHttpClient asyncClient(int connTimeout, int soTimeout) {
        return new AsyncHttpClient().connectionTimeout(connTimeout).soTimeout(soTimeout);
    }

    /**
     * 获取异步http客户端
     *
     * @return AbstractHttpClient
     */
    public static AbstractHttpClient asyncClient() {
        return new AsyncHttpClient();
    }

    /**
     * 关闭客户端资源，关闭后将不可再使用异步客户端
     */
    public static void shutdownHttpClient() {
        AsyncHttpClient.threadPool.shutdown();
    }
}
