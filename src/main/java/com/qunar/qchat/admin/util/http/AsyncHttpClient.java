package com.qunar.qchat.admin.util.http;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.httpclient.HttpMethodBase;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * 支持异步操作的http client
 *
 * @since 1.0.0
 * @author kris.zhang
 */
class AsyncHttpClient extends SyncHttpClient{

    static ListeningExecutorService threadPool;

    static { //init the thread pool

        threadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    }

    @Override
    public ResponseWrapper get(String uri) {
        throw new UnsupportedOperationException("异步客户端不支持调用同步方法，请调用 asyncGet");
    }

    @Override
    public ResponseWrapper post(String uri, String content) {
        throw new UnsupportedOperationException("异步客户端不支持调用同步方法，请调用 asyncPost");
    }

    @Override
    public ResponseWrapper post(String uri) {
        throw new UnsupportedOperationException("异步客户端不支持调用同步方法，请调用 asyncPost");
    }

    @Override
    public ListenableFuture<ResponseWrapper> asyncGet(final String uri) {
        checkArgument(!isNullOrEmpty(uri),"uri不能为null或空");
        ListenableFuture<ResponseWrapper> future = threadPool.submit(new Callable<ResponseWrapper>() {
            @Override
            public ResponseWrapper call() throws Exception {
                return doMethod(buildGetMethod(uri));
            }
        });
        Futures.addCallback(future,callBack);
        return future;
    }

    @Override
    public ListenableFuture<ResponseWrapper> asyncPost(final String uri, final String content) {
        checkArgument(!isNullOrEmpty(uri),"uri不能为null或空");
        checkArgument(!isNullOrEmpty(content),"content不能为null或空");
        ListenableFuture<ResponseWrapper> future = threadPool.submit(new Callable<ResponseWrapper>() {
            @Override
            public ResponseWrapper call() throws Exception {
                return doMethod(buildPostMethod(uri, content));
            }
        });
        Futures.addCallback(future,callBack);
        return future;
    }

    @Override
    public ListenableFuture<ResponseWrapper> asyncPost(final String uri) {
        checkArgument(!isNullOrEmpty(uri),"uri不能为null或空");
        ListenableFuture<ResponseWrapper> future = threadPool.submit(new Callable<ResponseWrapper>() {
            @Override
            public ResponseWrapper call() throws Exception {
                return doMethod(buildPostMethod(uri,null));
            }
        });
        Futures.addCallback(future,callBack);
        return future;
    }

    /**
     * 实际上，我们的异步方式不过使采用多线程进行异步操作，而不是基于异步套接子的操作。
     * 因为我们暂时不需要引入HttpAsyncClient，这种多线程的形式已经能够满足大部分的需要
     * 因为大部分场景，我们都是不希望影响主流程。并且后期切换也非常的容易
     *
     * @param method HttpMethodBase
     * @return ResponseWrapper
     */
    private ResponseWrapper doMethod(HttpMethodBase method) {
        try {
            /** we blocked here */
            httpClient.executeMethod(method);
            return ResponseWrapper.of(method);
        } catch (Throwable e) { //not only exp
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }

    }
}
