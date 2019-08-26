package com.qunar.qchat.admin.service.util;

import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.OnlineState;
import com.qunar.qchat.admin.model.ServiceStatusEnum;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 封装业务上的通用方法
 * Created by qyhw on 1/12/16.
 */
public class SeatUtil {

    private static final Logger logger = LoggerFactory.getLogger(SeatUtil.class);

    /**
     * 获取聊天窗口URL
     * @param webUrl
     * @param productId
     * @param businessEnum
     * @param qunarName
     * @return 获取失败返回null
     */
    public static String getChatUrl(String webUrl, String productId, BusinessEnum businessEnum, String qunarName, long seatId) {
        String url = null;
        try {
            url = Config.getProperty("");
//            if (StringUtils.isNotEmpty(webUrl)) {
//                webUrl = URLEncoder.encode(webUrl,"UTF-8");
//            }
            url = String.format(url, businessEnum.getEnName(), qunarName, productId, seatId, webUrl);
        } catch (Exception e) {
            logger.error("getChatUrl --- fail to encode, webUrl:{}", webUrl, e);
        }
        return url;
    }


    public static boolean isSeatServiceable(SeatWithStateVO ss,OnlineState baseline){
        if (null == ss)
            return  false;
        if (null == ss.getSeat())
            return false;
        if (ss.getSeat().getServiceStatus() == ServiceStatusEnum.SUPER_MODE.getKey())
            return true;
        if (ss.getSeat().getServiceStatus() == ServiceStatusEnum.DND_MODE.getKey())
            return false;

        return OnlineState.compare(ss.getOnlineState(),baseline)>0;
    }

}
