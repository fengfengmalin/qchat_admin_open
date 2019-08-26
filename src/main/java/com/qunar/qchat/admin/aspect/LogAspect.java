package com.qunar.qchat.admin.aspect;

import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.ConfigConstants;
import com.qunar.qchat.admin.util.DateUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.utils.CustomException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 日志记录AOP实现
 * Created by yhw on 06/17/2016.
 */
@Component
@Aspect
public class LogAspect {
    private final Logger assignSeatAppender = LoggerFactory.getLogger("assignSeatAppender");

    @Pointcut("@annotation(com.qunar.qchat.admin.annotation.RecordAccessLog)")
    public void inWebLayer(){
    }

    @Around("@annotation(com.qunar.qchat.admin.annotation.RecordAccessLog)")
    public Object doAroundInControllerLayer(ProceedingJoinPoint pjp) throws CustomException {
        try {
            boolean isRecord = Boolean.parseBoolean(Config.getPropertyInQConfig(ConfigConstants.ASSIGN_SEAT_LOG_SWITCH, "true"));
            if (!isRecord) {
                return pjp.proceed();
            }
            long startTimeMillis = System.currentTimeMillis(); // 开始时间
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes sra = (ServletRequestAttributes) ra;
            HttpServletRequest request = sra.getRequest();
            String userName = "";// AuthorityUtil.getThirdPartyUserName(request);
            Map<String, String[]> inputParamMap = request.getParameterMap();
            String requestPath = request.getRequestURI();
            Object result = pjp.proceed();
            long endTimeMillis = System.currentTimeMillis(); // 结束时间
            String agent = request.getHeader("User-Agent");
            String optTime = DateUtil.longToString(startTimeMillis, "yyyy-MM-dd HH:mm:ss");
            assignSeatAppender.info("登录用户:{}, 请求地址: {}, UA: {}, 请求时间:{}, 用时: {}, 输入:{}, 输出:{}", userName, requestPath, agent, optTime,
                    (endTimeMillis - startTimeMillis) + "ms", JacksonUtil.obj2String(inputParamMap), JacksonUtil.obj2String(result));
            return result;
        } catch (Throwable throwable) {
            assignSeatAppender.error("doAroundInControllerLayer error", throwable);
            throw new CustomException(throwable);
        }
    }
}

