package com.qunar.qchat.admin.util.log;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang3.StringUtils;

/**
 * 基于logback的报警邮件过滤器，该过滤器主要提供如下功能：
 * （1）等级过滤，目前只接受error的日志
 * （2）
 */
public class AlertLogFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {

        if(!event.getLevel().equals(Level.ERROR)){
            return FilterReply.DENY;
        }

        // 根据规则过滤


        // 根据错误类型过滤
        String exClassName = event.getThrowableProxy().getClassName();

        if (StringUtils.isNotEmpty(exClassName) && exClassName.contains("MissingServletRequestParameterException")) {
            return FilterReply.DENY;
        }

        return FilterReply.ACCEPT;
    }

}
