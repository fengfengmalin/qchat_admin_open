package com.qunar.qchat.admin.common;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class AsyncProcessor {

    private static ListeningExecutorService threadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    public static <T> ListenableFuture<T> process (Callable<T> callback) {
        checkNotNull(callback);
        ListenableFuture future = threadPool.submit(callback);
        return future;
    }
}
