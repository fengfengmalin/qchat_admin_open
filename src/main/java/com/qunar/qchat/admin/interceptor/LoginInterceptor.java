package com.qunar.qchat.admin.interceptor;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.dao.IBusiSupplierMappingDao;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qchat.admin.vo.SysUserVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 登录拦截器
 * Created by qyhw on 10/19/15.
 */
@Component
public class LoginInterceptor extends AbstractLoginInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    IBusiSupplierMappingDao busiSupplierMappingDao;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception{
        String qunarName = AuthorityUtil.getThirdPartyUserName(request);
       // String qunarName = request.getParameter("qunarNames");

        List<SupplierVO> suList = supplierService.getSupplierByQunarName(qunarName);
        logger.info("qunarname:{},获取的店铺列表为：{}", qunarName, JacksonUtil.obj2String(suList));
        if (StringUtils.isNotEmpty(qunarName) && CollectionUtils.isEmpty(suList)) {
            logger.info("登录：没有管理员权限，当前登录用户：{}", qunarName);
            JsonUtil.responseStr("没有管理员权限.", response);
            return false;
        }

        if (StringUtils.isNotEmpty(qunarName)) {
            parseRequestAndinjectionSession(request, response, qunarName, suList);
        }

        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        if (systemUser == null) { // 未登录
            return redirectLoginPage(request, response, (HandlerMethod) handler);
        }

//        if (request.getRequestURL().toString().contains("dashboard/")) { // 说明已登录 && 开发数据请求
//            try {
//                setBusiSupplierId(request, systemUser);
//            } catch (Exception e) {
//                logger.error("setBusiSupplierId error", e);
//                JsonUtil.responseStr("参数错误.", response);
//                return false;
//            }
//        }

        return true;
    }

//    private void setBusiSupplierId(HttpServletRequest request, SysUserVO systemUser) {
//        logger.info("{}-请求setBusiSupplierId", JacksonUtil.obj2String(systemUser));
//        List<SupplierVO> supplierList = systemUser.getCurBuSuList();
//        if (CollectionUtils.isEmpty(supplierList)) return;
//
//        List<String> busiSupplierIdList =  FluentIterable.from(supplierList).transform(new Function<SupplierVO, String>() {
//            @Override
//            public String apply(SupplierVO input) {
//                BusiSupplierMapping bsm = busiSupplierMappingDao.getBusiSupplierMappingBySuId(input.getId());
//                if (bsm == null) return "0";
//                return bsm.getBusiSupplierId();
//            }
//        }).toList();
//
//        if (StringUtils.isNotEmpty(request.getParameter("bType"))) {
//            String busiName = BusinessEnum.of(Integer.valueOf(request.getParameter("bType"))).getEnName();
//            String supplierId = request.getParameter("shopId");
//            logger.info("获取到的业务线-{}， 商铺id-{}", busiName, supplierId);
//            String url;
//            if (StringUtils.isEmpty(supplierId)) {
//                url = String.format(Config.getProperty(Constants.GET_SUPPLIER_INFO_BY_QUNARNAME_FROM_TTS_URL + busiName), systemUser.getQunarName());
//            } else {
//                url = String.format(Config.getProperty(Constants.GET_SUPPLIER_INFO_BY_QUNARNAME_FROM_TTS_URL + busiName), systemUser.getQunarName(), supplierId);
//            }
//            logger.info("请求的url为：{}", url);
//            String result = HttpClientUtils.get(url);
//            logger.info("请求url-{}, 获取到的结果为：{}", url, result);
//            String requestSupplierId = JacksonUtil.getNodeText(result, "data/supplierId");
//            logger.info("busiSupplierList-{}, requestSupplierId-{}", JacksonUtil.obj2String(busiSupplierIdList), requestSupplierId);
//            if (busiSupplierIdList.contains(IDEncryptor.encode(Integer.valueOf(requestSupplierId)))){
//                setSessionAttr(request, SessionConstants.BUSI_SUPPLIER_ID, requestSupplierId);
//            }
//        } else {
//            String url = String.format(Config.getProperty(Constants.GET_SUPPLIER_INFO_BY_QUNARNAME_FROM_TTS_URL + "dujia"), systemUser.getQunarName());
//            String result = HttpClientUtils.get(url);
//            String requestSupplierId = JacksonUtil.getNodeText(result, "data/supplierId");
//            if (busiSupplierIdList.contains(IDEncryptor.encode(Integer.valueOf(requestSupplierId)))){
//                setSessionAttr(request, SessionConstants.BUSI_SUPPLIER_ID, requestSupplierId);
//            }
//        }
//
//    }

    private void setSessionAttr(HttpServletRequest request, String key, String value) {
        HttpSession session = request.getSession();
        session.setAttribute(key, value);
        SessionUtils.setSession(session);
    }

    private void parseRequestAndinjectionSession(HttpServletRequest request, HttpServletResponse response, String qunarName, List<SupplierVO> suList) {
        String uri = request.getRequestURL().toString();
        List<String> indexPage = Lists.newArrayList();
        indexPage.add("sys/manage.do");
        indexPage.add("sys/wechat.qunar");
        indexPage.add("sys/smartConsult.do");
        indexPage.add("sys/supplierFAQ.do");
        indexPage.add("dashboard/index.do");
        indexPage.add("dashboard/index.qunar");

        boolean isIndexUrl = false;
        for (String s : indexPage){
            if (uri.contains(s)){
                isIndexUrl = true;
                break;
            }
        }

        Integer bType = getBType(request, isIndexUrl);
        String bSuId = getBSuId(request, isIndexUrl);
        if (isIndexUrl) {
            CookieUtil.setCookie(response, SessionConstants.COOKIE_BTYPE, String.valueOf(bType), "/");
            CookieUtil.setCookie(response, SessionConstants.COOKIE_BUSI_SUID, bSuId, "/");
        }

        SysUserVO loginUser = buildLoginUserVO(qunarName, suList, bType, bSuId);
        HttpSession session = request.getSession();
        session.setAttribute(SessionConstants.SysUser, loginUser);
        session.setMaxInactiveInterval(30 * 60); //设置session时长
        SessionUtils.setSession(session);
    }

    private String getBSuId(HttpServletRequest request, boolean isIndexUrl) {
        String bSuId = null;
        if (isIndexUrl) {
            bSuId = request.getParameter("bSuId");
            return bSuId;
        }
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if (SessionConstants.COOKIE_BUSI_SUID.equals(cookie.getName())) {
                bSuId = cookie.getValue();
            }
        }
        return bSuId;
    }

    private Integer getBType(HttpServletRequest request, boolean isIndexUrl) {
        Integer bType = 0;
        if (isIndexUrl) {
            Object obj = request.getParameter("bType");
            if (obj == null) return bType;
            try {
                bType = Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                logger.error("NumberFormatException", e);
            }
            if (BusinessEnum.of(bType) == null) {
                bType = 0;
            }
            return bType;
        }

        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if (SessionConstants.COOKIE_BTYPE.equals(cookie.getName())) {
                bType = Integer.valueOf(cookie.getValue());
            }
        }
        return bType;
    }

//    private void injectionSession2(HttpServletRequest request, HttpServletResponse response, String qunarName, List<SupplierVO> suList) {
//        Integer bType = 0;
//        String bSuId = null;
//        String uri = request.getRequestURL().toString();
//        if (uri.contains("sys/manage.do") || uri.contains("dashboard/index.do")) {
//            Object obj = request.getParameter("bType");
//            if (obj != null) {
//                try {
//                    bType = Integer.valueOf((String)obj);
//                } catch (NumberFormatException e) {
//                    logger.error("injectionSession2 NumberFormatException", e);
//                }
//                if (BusinessEnum.of(bType) == null) {
//                    bType = 0;
//                }
//            }
//            bSuId = request.getParameter("bSuId");
//            CookieUtil.setCookie(response, SessionConstants.COOKIE_BTYPE, String.valueOf(bType), "/");
//            CookieUtil.setCookie(response, SessionConstants.COOKIE_BUSI_SUID, bSuId, "/");
//        } else {
//            Cookie[] cookies = request.getCookies();
//            for(Cookie cookie : cookies){
//                if (SessionConstants.COOKIE_BTYPE.equals(cookie.getName())) {
//                    bType = Integer.valueOf(cookie.getValue());
//                }
//                if (SessionConstants.COOKIE_BUSI_SUID.equals(cookie.getName())) {
//                    bSuId = cookie.getValue();
//                }
//            }
//        }
//
//        SysUserVO loginUser = buildLoginUserVO(qunarName, suList, bType, bSuId);
//        HttpSession session = request.getSession();
//        session.setAttribute(SessionConstants.SysUser, loginUser);
//        session.setMaxInactiveInterval(30*60); //设置session时长
//        SessionUtils.setSession(session);
//    }

    private SysUserVO buildLoginUserVO(String qunarName, List<SupplierVO> suList, Integer bType, String bSuId) {
        SysUserVO loginUser = new SysUserVO();
        if (StringUtils.isNotEmpty(bSuId)) {
            SupplierVO suVO = supplierService.getSupplierByBusiSupplierId(bSuId, bType);
            if (!checkSuId(suList, suVO)) {
                suVO = buildDefaultSuVO(bType, bSuId);
            }

            loginUser.setCurBuSuList(Arrays.asList(suVO));
        }

        loginUser.setAllSuList(suList);
        loginUser.setQunarName(qunarName);
        loginUser.setbType(BusinessEnum.of(bType));

        if (CollectionUtil.isEmpty(loginUser.getCurBuSuList()) && CollectionUtil.isNotEmpty(loginUser.getAllSuList())) {
            List<SupplierVO> allSuList = loginUser.getAllSuList();
            List<SupplierVO> curBuSuList = new ArrayList<>(allSuList.size());

            for (SupplierVO suVO : allSuList) {
                if (suVO.getBusiType() == bType) {
                    curBuSuList.add(suVO);
                }
            }
            loginUser.setCurBuSuList(curBuSuList);
        }
        return loginUser;
    }

    private SupplierVO buildDefaultSuVO(Integer bType, String bSuId) {
        SupplierVO suVO = new SupplierVO();
        suVO.setBusiType(bType);
        suVO.setBusiSupplierId(bSuId);
        suVO.setId(0);
        return suVO;
    }

    private boolean checkSuId(List<SupplierVO> suList, SupplierVO suVO) {
        if (suVO == null) {
            return false;
        }
        for (SupplierVO sVO : suList) {
            if (sVO.getId() == suVO.getId()) {
                return true;
            }
        }
        return false;
    }

}
