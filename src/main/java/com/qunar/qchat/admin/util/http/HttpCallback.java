package com.qunar.qchat.admin.util.http;

import com.google.common.util.concurrent.FutureCallback;

/**
 * http回调接口
 *
 * @since 1.0.0
 * @author kris.zhang
 */
public interface HttpCallback extends FutureCallback<ResponseWrapper> {

    /**
     * 正确返回的时候将调用此方法
     *
     * @param wrapper ResponseWrapper
     */
    void onSuccess(ResponseWrapper wrapper);

    /**
     * 产生异常的时候调用此方法
     *
     * @param t Throwable
     */
    void onFailure(Throwable t);


}
