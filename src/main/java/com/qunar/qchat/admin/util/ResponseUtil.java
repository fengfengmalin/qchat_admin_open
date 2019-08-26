package com.qunar.qchat.admin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by qyhw on 12/30/15.
 */
public class ResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    public static void print(String msg, HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        ServletOutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(msg.getBytes("UTF-8"));
        } catch (IOException e) {
            logger.info("fail to response",e);
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e2) {
                logger.error("print error", e);
            }
        }

    }

}
