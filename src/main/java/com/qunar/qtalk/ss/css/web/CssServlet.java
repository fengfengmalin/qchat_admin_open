package com.qunar.qtalk.ss.css.web;

import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.util.AuthorityUtil;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.consult.QtSessionWatcher;
import com.qunar.qtalk.ss.consult.SpringComponents;
import com.qunar.qtalk.ss.consult.entity.QtSessionItem;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.utils.CustomException;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class CssServlet extends QTalkBaseServlet {
    public static final Logger logger = LoggerFactory.getLogger(CssServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        innerFunc(request, response);
    }

    @Override
    protected boolean onPost(String type, Map<String, Object> map, Map<String, String[]> map1, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        innerFunc(httpServletRequest, httpServletResponse);
        return true;
    }

    @Override
    protected boolean onGet(String type, Map<String, String[]> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        innerFunc(httpServletRequest, httpServletResponse);
        return true;
    }

    @Override
    protected boolean isSupportGetRequest() {
        return true;
    }

    private void safePutObject(Map<String, Object> maps, String key, Object value) {
        if (maps != null) {
            if (key != null && value != null)
                maps.put(key, value);
        }
    }

    private void sayHello(HttpServletRequest request, HttpServletResponse response) throws CustomException, IOException {


        String line = request.getParameter("line");
        if (StringUtils.isEmpty(line))
            throw new CustomException(404, "line为业务线字段，不能为空");

        String seatQName = request.getParameter("seatQName");
        if (StringUtils.isEmpty(seatQName))
            throw new CustomException(404, "seatQName is missing");

        String userQName = request.getParameter("userQName");
        if (StringUtils.isEmpty(userQName))
            throw new CustomException(404, "userQName is missing");

        String shopId = request.getParameter("virtualId");

        if (StringUtils.equalsIgnoreCase("flight", line) && StringUtils.equalsIgnoreCase("undefined", shopId))
            throw new CustomException(404, "已被拦截");

        packOKResponse(request, response);
    }

    private void goOnline(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String qunarName = AuthorityUtil.getThirdPartyUserName(request);

        logger.debug("enter to goOnline method, and qunarName = {}", qunarName);
        if (StringUtils.isNotEmpty(qunarName)) {
            if (!qunarName.contains("@")) {
                qunarName = String.format("%s@%s", qunarName,  QChatConstant.DEFAULT_HOST);
            }
        }

        if (StringUtils.isNotEmpty(qunarName)) {
            JID userId = JID.parseAsJID(qunarName);
            logger.debug("begin to query shopIds by jid:{}", userId);
            List<Long> shopIds = SpringComponents.components.csrService.queryServiceShopByCsrName(userId);

            if (CollectionUtils.isNotEmpty(shopIds)) {
                for (Long shopId : shopIds) {
                    logger.debug("fire goOnline:{}", shopId.longValue());
                    QtQueueManager.getInstance().goOnline(shopId.longValue());
                }
            }
        }
        packOKResponse(request, response);
    }

    private void jagementEx(HttpServletRequest request, HttpServletResponse response, boolean isEx) throws CustomException, IOException {


//        String jsonString = parseReqeustBody(request);
//        if (StringUtils.isNotEmpty(jsonString)) {
//            Map<String, Object> jsonObject = JsonUtil.parseJSONObject(jsonString);
//        }

        String shopId = request.getParameter("shopId");
        if (StringUtils.isEmpty(shopId))
            throw new CustomException(404, "shopId is missing");

        String seatQName = request.getParameter("seatQName");
        if (StringUtils.isEmpty(seatQName))
            throw new CustomException(404, "seatQName is missing");

        String userQName = request.getParameter("userQName");
        if (StringUtils.isEmpty(userQName))
            throw new CustomException(404, "userQName is missing");

        String productId = request.getParameter("productId");
        if (StringUtils.isEmpty(productId))
            productId = QtSessionItem.DEFAULT_PRODUCTID;

        String host = request.getParameter("host");
        if (StringUtils.isEmpty(host))
            host =  QChatConstant.DEFAULT_HOST;

        JID fromJid;

        if (userQName.contains("@")) {
            fromJid = JID.parseAsJID(userQName);
        } else {
            fromJid = JID.parseAsJID(String.format("%s@%s", userQName, host));
        }

//        Map<String, String[]> parameters = request.getParameterMap();

//        if (parameters.containsKey("noteArgs") && !"shop_10613".equalsIgnoreCase(shopId)) {
//            String pdtId = request.getParameter("pdtId");
//            String tEnId = request.getParameter("tEnId");
//            String tuId = request.getParameter("tuId");
//            String var = request.getParameter("bType");
//            Integer bType = 0;
//            if (StringUtils.isNotEmpty(var)) {
//                bType = Integer.valueOf(var);
//            }
//
//            String line = request.getParameter("line");
//            String source = request.getParameter("source");
//            String noteArgs = request.getParameter("noteArgs");
//        }

        if (shopId.startsWith("shop_")) {
            shopId = shopId.replace("shop_", "");
        }

        logger.debug("judgmentOrRedistribution, {} - {} - {}", fromJid, Long.parseLong(shopId), productId);

        QtSessionItem sessionItem = QtQueueManager.getInstance().judgmentOrRedistribution(fromJid, Long.parseLong(shopId), productId, host, isEx, false);

        CSR csr = null;

        if (sessionItem != null && sessionItem.getSeatId() > 0) {
            logger.info("seatid is {}", sessionItem.getSeatId());
            List<CSR> scrs = SpringComponents.components.csrService.queryCsrsByCsrIDs(Arrays.asList(sessionItem.getSeatId()));
            if (CollectionUtils.isNotEmpty(scrs)) {
                csr = scrs.get(0);
            } else {
                logger.info("seatid is {}, and scrs is empty", sessionItem.getSeatId());
            }
        }

        if (csr != null) {
            Shop shop = SpringComponents.components.shopService.selectShopById(Long.parseLong(shopId));

            Map<String, Object> result = new Hashtable<>();
            result.put("switchOn", true);

            Map<String, Object> supplier = new Hashtable<>();
            result.put("supplier", supplier);
            Map<String, Object> seat = new Hashtable<>();
            result.put("seat", seat);
            safePutObject(result, "session", sessionItem.getSessionId());
            result.put("onlineState", "online");
            result.put("lastStartTime", 0);

            safePutObject(supplier, "id", shop.getId());
            safePutObject(supplier, "name", shop.getName());
            safePutObject(supplier, "shopId", String.format("shop_%d", shop.getId()));
            safePutObject(supplier, "logoUrl", shop.getLogoURL());
            safePutObject(supplier, "welcomes", shop.getWelcomes());
            safePutObject(supplier, "createDate", shop.getCreateTime());
            safePutObject(supplier, "status", shop.getStatus());

            safePutObject(seat, "id", csr.getId());
            safePutObject(seat, "qunarName", csr.getQunarName());

            safePutObject(seat, "webName", csr.getWebName());
            safePutObject(seat, "nickName", csr.getNickName());
            safePutObject(seat, "faceLink", csr.getFaceLink());
            safePutObject(seat, "createTime", csr.getCreateTime());

            safePutObject(seat, "priority", csr.getPriority());
            safePutObject(seat, "supplierId", shop.getId());
            safePutObject(seat, "supplierName", shop.getName());
            safePutObject(seat, "serviceStatus", csr.getServiceStatus());
            seat.put("isrobot", false);

            packOKResponse(result, request, response);
        } else {
            if (sessionItem != null)
                logger.info("sessionItem id = {}", sessionItem.getSeatId());
            else
                logger.info("sessionItem = null");
            packOKResponse(request, response);
        }
    }

    private void innerFunc(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getPathInfo();

        logger.info("request = {}, method = {}", getFullURL(request), type);

        try {
            if (StringUtils.equalsIgnoreCase(type, CssEventType.reloadCache.getDes())) {
                QtSessionWatcher.getInstance().reloadCache();

            } else if (StringUtils.equals(type, CssEventType.judgmentOrRedistribution.getDes())) {
                jagementEx(request, response, false);
            } else if (StringUtils.equals(type, CssEventType.judgmentOrRedistributionEx.getDes())) {
                jagementEx(request, response, true);
            } else if (StringUtils.equals(type, CssEventType.online.getDes())) {
                goOnline(request, response);
            } else if (StringUtils.equals(type, CssEventType.SayHello.getDes())) {
                sayHello(request, response);
            } else {
                packOKResponse(request, response);
            }

        } catch (CustomException execption) {
            packResponse(request, response, execption.getErrcode(), execption.getMessage(), null);
        } catch (Exception e) {
            logger.error("failed to exec post", e);
            try {
                packResponse(request, response, 500, "内部服务器错误", null);
            } catch (IOException e1) {
                logger.error("failed to exec packResponse", e1);
            }
        }
    }


}
