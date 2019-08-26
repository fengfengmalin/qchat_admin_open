package com.qunar.qchat.admin.util.log;


import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.helpers.CyclicBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 继承自SMTPAppender并且覆盖了源有的makeSubjectLayout方法，
 * 这是为了实现程序中发送报警邮件的邮件标题修改功能。
 */
public class SmtpAppender extends SMTPAppender {
    private static final Logger logger = LoggerFactory.getLogger(SmtpAppender.class);

    private static String HOST_NAME;
    private static String HOST_ADDRESS;
    private static AtomicInteger counter = new AtomicInteger(0);

    static {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            HOST_NAME = localHost.getHostName();
            HOST_ADDRESS = localHost.getHostAddress();
        } catch (UnknownHostException ignored) {
            HOST_NAME = "unknown-host";
            HOST_ADDRESS = "unknown-host";
        }
    }

    private String buildTitle(){
        return new StringBuilder().append(HOST_NAME)
                .append(" [ ").append(HOST_ADDRESS).append(" ]")
                .toString();
    }

    /**
     * 邮件主题, 检查是否有丢邮件的情况, 在标题题后面加了计数.
     */
    class SubjectLayout extends PatternLayout {
        @Override
        public String doLayout(ILoggingEvent event) {
            return super.doLayout(event) + " " + counter.incrementAndGet();
        }
    }

    @Override
    protected Layout<ILoggingEvent> makeSubjectLayout(String subjectStr) {
        PatternLayout pl = new SubjectLayout();
        pl.setContext(getContext());
        pl.setPattern(buildTitle());
        pl.setPostCompileProcessor(null);
        pl.start();
        return pl;
    }

    final static int MAX_DELAY_BETWEEN_STATUS_MESSAGES = 1228800 * CoreConstants.MILLIS_IN_ONE_SECOND;
    int delayBetweenStatusMessages = 300 * CoreConstants.MILLIS_IN_ONE_SECOND;
    long lastTrackerStatusPrint = 0;
    private int errorCount = 0;

    private Object lock = new Object();

    /**
     * 这个方法和父类完全一样, 复写的原因是因为重写了SenderRunnable类
     * @param eventObject
     */
    protected void append(ILoggingEvent eventObject) {

        if (!checkEntryConditions()) {
            return;
        }

        String key = discriminator.getDiscriminatingValue(eventObject);
        long now = System.currentTimeMillis();
        final CyclicBuffer<ILoggingEvent> cb = cbTracker.getOrCreate(key, now);
        subAppend(cb, eventObject);
        try {
            if (eventEvaluator.evaluate(eventObject)) {
                // clone the CyclicBuffer before sending out asynchronously
                CyclicBuffer<ILoggingEvent> cbClone = new CyclicBuffer<ILoggingEvent>(cb);
                // see http://jira.qos.ch/browse/LBCLASSIC-221
                cb.clear();

                if (isAsynchronousSending()) {
                    // perform actual sending asynchronously
                    SenderRunnable senderRunnable = new SenderRunnable(cbClone, eventObject);
                    context.getExecutorService().execute(senderRunnable);
                } else {
                    // synchronous sending
                    sendBuffer(cbClone, eventObject);
                }
            }
        } catch (EvaluationException ex) {
            errorCount++;
            if (errorCount < CoreConstants.MAX_ERROR_COUNT) {
                addError("SMTPAppender's EventEvaluator threw an Exception-", ex);
            }
        }

        // immediately remove the buffer if asked by the user
        if (eventMarksEndOfLife(eventObject)) {
            cbTracker.endOfLife(key);
        }

        cbTracker.removeStaleComponents(now);

        if (lastTrackerStatusPrint + delayBetweenStatusMessages < now) {
            addInfo("SMTPAppender [" + name + "] is tracking [" + cbTracker.getComponentCount() + "] buffers");
            lastTrackerStatusPrint = now;
            // quadruple 'delay' assuming less than max delay
            if (delayBetweenStatusMessages < MAX_DELAY_BETWEEN_STATUS_MESSAGES) {
                delayBetweenStatusMessages *= 4;
            }
        }
    }

    /**
     * 因为邮箱服务端有限制, 所以连续的logger.error最终只有一个会发出邮件
     * 猜测可能是服务器一段时间内(经反复实验, 应该小于100ms) 收到的第一封/最后一封邮件
     * 又因为是异步发送, 所以就相当于随机了.
     *
     * 所以重新实现了父类当中的SenderRunnable, run方法中获取了锁(此类单例), 并在每发一封邮件之后, 休眠100ms
     */
    class SenderRunnable implements Runnable {

        final CyclicBuffer<ILoggingEvent> cyclicBuffer;
        final ILoggingEvent e;

        SenderRunnable(CyclicBuffer<ILoggingEvent> cyclicBuffer, ILoggingEvent e) {
            this.cyclicBuffer = cyclicBuffer;
            this.e = e;
            try {
                mimeMsg.setHeader("From", e.getLoggerName() + "@qunar.com");
            } catch (MessagingException ignored) {
                logger.error("MessagingException error", ignored);
            }
        }

        public void run() {
            synchronized (lock) {
                sendBuffer(cyclicBuffer, e);
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ignored) {
                    logger.error("InterruptedException error", ignored);
                }
            }
        }
    }

}
