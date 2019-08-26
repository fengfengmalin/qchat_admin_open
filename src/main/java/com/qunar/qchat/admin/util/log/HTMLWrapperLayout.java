package com.qunar.qchat.admin.util.log;


import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 由于默认的HTMLLayout没有显示机器名的功能，因此我们继承了{code HTMLLayout}
 * 类，并且覆盖相应的方法，以生成我们需要的html日志。
 */
public class HTMLWrapperLayout extends HTMLLayout {
    private static final Logger logger = LoggerFactory.getLogger(HTMLWrapperLayout.class);

    /*
     * 使用策略模式封装wrapper的策略方式
     */
    private interface WrapperHtmlStrategy{
        public StringBuilder wrap(String content);
    }

    private abstract class BaseStrategy implements WrapperHtmlStrategy{

        /* 这里无需使用缓存，因为内部实现已经帮你考虑到了！他缓存的是cachedLocalHost */
        protected String getHostName(){

            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) {
                logger.error("UnknownHostException error", ignored);
            }

            return "unknown-host";
        }

        protected String getHostAddress(){

            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ignored) {
                logger.error("UnknownHostException error", ignored);
            }

            return "unknown-host";
        }

        protected StringBuilder decorateColorFont(StringBuilder content,String color,int fontSize){
            StringBuilder builder = new StringBuilder();
            builder.append("<font size=\"")
                    .append(fontSize)
                    .append("\" color=\"")
                    .append(color)
                    .append("\">")
                    .append(content)
                    .append("</font>");
            return builder;
        }

        protected StringBuilder decorateStrong(StringBuilder content){
            StringBuilder builder = new StringBuilder();
            builder.append("<strong>")
                    .append(content)
                    .append("</strong>");
            return builder;
        }
    }


    private class DefaultStrategy extends BaseStrategy{

        @Override
        public StringBuilder wrap(String content) {
            StringBuilder builder = new StringBuilder();

            builder.append(this.getHostName())
                    .append(" @ [ ")
                    .append(this.getHostAddress())
                    .append(" ] ");

            return decorateColorFont(builder, "red", 3)
                    .append("<br><br>")
                    .append(content);
        }
    }

    private WrapperHtmlStrategy wrapper = new DefaultStrategy();

    /*
     * 覆盖doLayout方法，对父类生成的html模板进行wrap从而生成期望的html日志内容
     */
    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder builder = wrapper.wrap(super.doLayout(event));
        return builder.toString();
    }

}
