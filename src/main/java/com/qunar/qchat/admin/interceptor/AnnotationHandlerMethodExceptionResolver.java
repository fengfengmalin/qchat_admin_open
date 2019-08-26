package com.qunar.qchat.admin.interceptor;

import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.ConfigConstants;
import com.qunar.qchat.admin.controller.BaseController;
import com.qunar.qchat.admin.util.JsonResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 1/8/16.
 */
public class AnnotationHandlerMethodExceptionResolver extends ExceptionHandlerExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMethodExceptionResolver.class);
    private String defaultErrorView;

    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
        if (handlerMethod == null) {
            return null;
        }
        Method method = handlerMethod.getMethod();
        if (method == null) {
            return null;
        }

        boolean isLog = Boolean.parseBoolean(Config.getPropertyInQConfig(ConfigConstants.EXCEPTION_LOG_SWITCH, "true"));
        if (isLog) {
            logger.warn("方法: {}, 来自请求：{}", method, request.getHeader("Referer"), exception);
        }

        ModelAndView mv = super.doResolveHandlerMethodException(request, response, handlerMethod, exception);
        if (mv == null) {
            return null;
        }
        ResponseBody responseBodyAnn = AnnotationUtils.findAnnotation(method, ResponseBody.class);
        if (responseBodyAnn != null) {
            return outputJsonResult(request, response, method, mv);
        }

        mv.clear();
        if (mv.getViewName() == null) {
            mv.setViewName(defaultErrorView);
        }
        mv.addObject(BaseController.ERROR_MSG_KEY_SPRING, BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());

        return mv;

    }

    private ModelAndView outputJsonResult(HttpServletRequest request, HttpServletResponse response, Method method, ModelAndView mv) {
        try {
            ResponseStatus responseStatusAnn = AnnotationUtils.findAnnotation(method, ResponseStatus.class);
            if (responseStatusAnn != null) {
                HttpStatus responseStatus = responseStatusAnn.value();
                String reason = responseStatusAnn.reason();
                if (!StringUtils.hasText(reason)) {
                    response.setStatus(responseStatus.value());
                } else {

                    response.sendError(responseStatus.value(), reason);

                }
            }

            return handleResponseBody(mv, request, response);
        } catch (Exception e) {
            logger.error("outputJsonResult error",e);
            return null;
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private ModelAndView handleResponseBody(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map value = mv.getModelMap();
        HttpInputMessage inputMessage = new ServletServerHttpRequest(request);
        List<MediaType> acceptedMediaTypes = inputMessage.getHeaders().getAccept();
        if (acceptedMediaTypes == null || acceptedMediaTypes.isEmpty()) {
            acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
        }
        MediaType.sortByQualityValue(acceptedMediaTypes);
        HttpOutputMessage outputMessage = new ServletServerHttpResponse(response);
        Class<?> returnValueType = value.getClass();
        List<HttpMessageConverter<?>> messageConverters = super.getMessageConverters();
        if (messageConverters != null) {
            for (MediaType acceptedMediaType : acceptedMediaTypes) {
                for (HttpMessageConverter messageConverter : messageConverters) {
                    if (messageConverter.canWrite(returnValueType, acceptedMediaType)) {
                        Object result = value.get(BaseController.ERROR_MSG_KEY_SPRING);
                        if (result == null) {
                            result = JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(), BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
                        }
                        messageConverter.write(result, acceptedMediaType, outputMessage);
                        return new ModelAndView();
                    }
                }
            }
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Could not find HttpMessageConverter that supports return type [" + returnValueType + "] and " + acceptedMediaTypes);
        }
        return null;
    }

    public String getDefaultErrorView() {
        return defaultErrorView;
    }
    public void setDefaultErrorView(String defaultErrorView) {
        this.defaultErrorView = defaultErrorView;
    }

}
