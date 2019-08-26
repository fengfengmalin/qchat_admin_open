package com.qunar.qchat.admin.util.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简化HttpCallback接口的实现复杂度
 *
 * @since 1.0.0
 * @author kris.zhang
 */
public class CallbackAdaptor implements HttpCallback{

    private final static Logger logger = LoggerFactory.getLogger(CallbackAdaptor.class);

    @Override public void onSuccess(ResponseWrapper wrapper) {}

    @Override public void onFailure(Throwable t) { throw new RuntimeException(t.getMessage(),t); }



}
