package com.qunar.qchat.admin.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.controller.BaseController;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.product.HistoryProduct;
import com.qunar.qchat.admin.model.qchat.ProductNoteArgs;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.service.IHistoryProductService;
import com.qunar.qchat.admin.service.third.INoticeService;
import com.qunar.qchat.admin.service.third.IProductService;
import com.qunar.qchat.admin.util.AuthorityUtil;
import com.qunar.qchat.admin.util.IPUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qchat.admin.vo.third.ProductVO;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.service.CsrService;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 12/17/15.
 */
@Controller
@RequestMapping("/api/pdt")
public class ProductAPIController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProductAPIController.class);

    @Autowired
    IProductService productService;
    @Resource
    private IHistoryProductService historyProductService;
    @Resource
    private INoticeService noticeService;
    @Autowired
    CsrService csrService;

    /**
     * @param seatQName 坐席的id，可能是机器人，必须带host
     * @param userQName 用户的id, 必须带host
     * @param virtualId consult虚拟号，必须带host
     * @param pdtId     产品id 用户获取产品信息
     * @param bType     业务线的编号，与line的功能一样，用于确定唯一一个业务线，二者取其一
     * @param line      业务线的编号，与bType的功能一样，用于确定唯一一个业务线，二者取其一
     * @param source    业务线的分支小线，可以为空
     * @param bizparams 业务线参数集合
     * @param request
     * @return
     */
    @RequestMapping(value = "/sendProductNote.qunar")
    @ResponseBody
    public JsonResultVO<?> sendProductNote(
            @RequestParam(value = "seatQName") String seatQName,
            @RequestParam(value = "userQName") String userQName,
            @RequestParam(value = "virtualId", required = false) String virtualId,
            @RequestParam(value = "pdtId") String pdtId,
            @RequestParam(value = "bType", required = false) Integer bType,
            @RequestParam(value = "line", required = false) String line,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "bizparams", required = false) String bizparams,
            HttpServletRequest request) {
//
        Map<String, String> params = JacksonUtils.string2Obj(bizparams, Map.class);
        String tEnId = "";
        String tuId = "";
        String t3id = "";

        if (null != params) {
            tEnId = params.get("tEnId");
            tuId = params.get("tuId");
            t3id = params.get("t3id");
        }

        long startTime = System.currentTimeMillis();

        logger.info("sendProductNote, pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line);


        if (bType == null) {
            bType = -1;
        }

        BusinessEnum bEnum = BusinessEnum.of(bType);
        if (bEnum == null && StringUtils.isNotEmpty(line)) {
            bEnum = BusinessEnum.ofByEnName(line);
        }

        if (bEnum == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("dtlProduct --- 业务线类型参数不正确, pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line);
            }
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(), "业务线类型参数不正确");
        }

        bType = bEnum.getId();

        ProductVO pVO;
        try {
            pVO = productService.getProduct(request, pdtId, tEnId, bType, tuId, t3id, source);
        } catch (Exception e) {
            logger.error("dtlProduct -- 获取产品详情发生异常， pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line, e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(), BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        if (pVO == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("dtlProduct --- 没有获取到返回结果 pVO == null, pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line);
            }
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getMsg());
        }
        // 兼容android 发送产品卡片没有 virtualId
        if (StringUtils.isEmpty(virtualId) &&
                bEnum == BusinessEnum.VACATION && StringUtils.isNotEmpty(seatQName)) {
            if (!seatQName.contains("@"))
                seatQName = String.format("%s@%s", seatQName, QChatConstant.DEFAULT_HOST);
            JID seatJid = JID.parseAsJID(seatQName);
            List<CSR> csrList = csrService.queryOnlineCsrByCsrQunarName(seatJid);
            virtualId = CollectionUtils.isNotEmpty(csrList) ? "shop_" + csrList.get(0).getSupplierID().toString() : "";
            logger.info("sendProductNote VirtualId:{}", virtualId);

        }

        // 兼容android
        pVO.setBu(bEnum.getEnName());
        pVO.setProductId(pdtId);

        ProductNoteArgs productNoteArgs = new ProductNoteArgs();
        productNoteArgs.setSeatQName(seatQName);
        productNoteArgs.setUserQName(userQName);
        productNoteArgs.setVirtualId(virtualId);
        productNoteArgs.setIp(IPUtil.getUserIPString(request));
        productNoteArgs.setBu(bEnum.getEnName());
        productNoteArgs.setUrl(pVO.getTouchDtlUrl());
        boolean result = noticeService.sendProductNote(pVO, productNoteArgs);

        if (!result)
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.SUCCESS.getMsg());
        else
            return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(), BusiResponseCodeEnum.SUCCESS.getMsg());
    }

    @RequestMapping(value = "/productDtl.qunar")
    @ResponseBody
    public JsonResultVO<?> dtlProduct(@RequestParam(value = "pdtId") String pdtId,
                                      @RequestParam(value = "tEnId", required = false) String tEnId,
                                      @RequestParam(value = "tuId", required = false) String tuId,
                                      @RequestParam(value = "t3id", required = false) String t3id,
                                      @RequestParam(value = "bType", required = false) Integer bType,
                                      @RequestParam(value = "line", required = false) String line,
                                      @RequestParam(value = "source", required = false) String source,
                                      @RequestParam(value = "sendNote", required = false) boolean sendNote,
                                      @RequestParam(value = "noteArgs", required = false) String noteArgs, HttpServletRequest request) {


        long startTime = System.currentTimeMillis();

        logger.info("dtlProduct pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line);


        if (bType == null) {
            bType = -1;
        }

        BusinessEnum bEnum = BusinessEnum.of(bType);
        if (bEnum == null && StringUtils.isNotEmpty(line)) {
            bEnum = BusinessEnum.ofByEnName(line);
        }

        if (bEnum == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("dtlProduct --- 业务线类型参数不正确, pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line);
            }
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(), "业务线类型参数不正确");
        }
        bType = bEnum.getId();

        ProductVO pVO;
        try {
            pVO = productService.getProduct(request, pdtId, tEnId, bType, tuId, t3id, source);
        } catch (Exception e) {
            logger.error("dtlProduct -- 获取产品详情发生异常， pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line, e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(), BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        if (pVO == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("dtlProduct --- 没有获取到返回结果 pVO == null, pdtId : {}， tEnId : {}， bType : {}, line : {}", pdtId, tEnId, bType, line);
            }
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getMsg());
        }
        // 兼容android
        pVO.setBu(bEnum.getEnName());
        pVO.setProductId(pdtId);

        // 需要发送产品详情note消息
        if (sendNote) {
            ProductNoteArgs productNoteArgs = JacksonUtils.string2Obj(noteArgs, ProductNoteArgs.class);
            if (productNoteArgs != null && StringUtils.isEmpty(productNoteArgs.getVirtualId()) &&
                    bEnum == BusinessEnum.VACATION && StringUtils.isNotEmpty(productNoteArgs.getSeatQName())) {
                JID seatJid = JID.parseAsJID(String.format("%s@%s", productNoteArgs.getSeatQName(), QChatConstant.DEFAULT_HOST));
                List<CSR> csrList = csrService.queryOnlineCsrByCsrQunarName(seatJid);
                String supplierId = CollectionUtils.isNotEmpty(csrList) ? "shop_" + csrList.get(0).getSupplierID().toString() : "";
                logger.info("productNoteArgs VirtualId:{}", supplierId);
                productNoteArgs.setVirtualId(supplierId);
            }

            if (checkNoteArg(productNoteArgs)) {
                //获取ip
                String ip = IPUtil.getUserIPString(request);
                productNoteArgs.setIp(ip);

                if (Strings.isNullOrEmpty(productNoteArgs.getBu())) {
                    productNoteArgs.setBu(bEnum.getEnName());
                }
                productNoteArgs.setUrl(pVO.getTouchDtlUrl());
                noticeService.sendProductNote(pVO, productNoteArgs);
            } else {
                pVO.setSendNoteMsg("noteArgs参数检查错误");
                pVO.setSendNoteSuccess(false);
            }
        } else {
            pVO.setSendNoteMsg("sendNote is false");
            pVO.setSendNoteSuccess(false);
        }


        logger.info("dtlProduct --- 获取到返回结果 pVO == {} ; pdtId : {} ; agent {}", JacksonUtil.obj2String(pVO), pdtId, request.getHeader("User-Agent"));

        return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(), BusiResponseCodeEnum.SUCCESS.getMsg(), pVO);
    }

    @RequestMapping(value = "/sendNoteByJson.qunar")
    @ResponseBody
    public JsonResultVO<?> sendNoteByJson(@RequestBody String json, HttpServletRequest request) {
        try {
            JSONObject paramJson = JSONObject.parseObject(json);
            JSONObject data = paramJson.getJSONObject("productVO").getJSONObject("data");
            String jsonString = data.toJSONString();
            ProductVO productVO = JacksonUtils.string2Obj(jsonString, ProductVO.class);

            String noteArgs = paramJson.getString("noteArgs");

            ProductNoteArgs productNoteArgs = JacksonUtils.string2Obj(noteArgs, ProductNoteArgs.class);
            if (checkNoteArg(productNoteArgs)) {
                String ip = IPUtil.getUserIPString(request);
                productNoteArgs.setIp(ip);
                productNoteArgs.setUrl(productVO.getTouchDtlUrl());
                noticeService.sendProductNoteBySeat(productVO, productNoteArgs);
            } else {
                productVO.setSendNoteMsg("noteArgs参数检查错误");
                productVO.setSendNoteSuccess(false);
            }

            return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(), BusiResponseCodeEnum.SUCCESS.getMsg(), productVO);
        } catch (Exception e) {
            logger.error("sendNoteByJson error", e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(), "服务端错误");
        }

    }


    // @MustLogin(MustLogin.ViewType.JSON)
    @RequestMapping(value = "lastOne.json")
    @ResponseBody
    public JsonData lastOne(String seatQName, HttpServletRequest request) {
        if (Strings.isNullOrEmpty(seatQName)) {
            return JsonData.error("客服id错误");
        }
        String userName = AuthorityUtil.getThirdPartyUserName(request);

        userName = StringUtils.isEmpty(userName) ? request.getParameter("userQName") : userName;
        if (StringUtils.isEmpty(userName)) {
            return JsonData.error("请先登陆");
        }

        try {
            HistoryProduct lastProduct = historyProductService.getLastProduct(seatQName, userName);
            return JsonData.success(lastProduct);
        } catch (Exception e) {
            logger.error("查询最近一次咨询产品出错,seatQName:{},userQName:{}", seatQName, userName, e);
        }
        return JsonData.error("系统错误");
    }

    // @MustLogin(value = MustLogin.ViewType.JSP)
    @RequestMapping(value = "/history.json")
    @ResponseBody
    public JsonData history(HttpServletRequest request) {

        String userName = AuthorityUtil.getThirdPartyUserName(request);
        userName = StringUtils.isEmpty(userName) ? request.getParameter("userQName") : userName;
        if (StringUtils.isEmpty(userName)) {
            return JsonData.error("请先登陆");
        }
        try {
            List<HistoryProduct> productHistory = historyProductService.getProductHistory(userName);
            return JsonData.success(productHistory);
        } catch (Exception e) {
            logger.error("查询咨询产品历史列表出错,userQName:{}", userName, e);
        }
        return JsonData.error("系统错误");
    }

    // 检查参数
    private static boolean checkNoteArg(ProductNoteArgs args) {
        if (args == null) {
            return false;
        }
        if (Strings.isNullOrEmpty(args.getUserQName()) || Strings.isNullOrEmpty(args.getSeatQName())) {
            return false;
        }
        if (Strings.isNullOrEmpty(args.getUserHost())
                || !StringUtils.equals(args.getUserHost(), QChatConstant.QTALK_HOST)) {
            args.setUserQName(args.getUserQName() + QChatConstant.QCHAT_HOST_POSTFIX);
        } else {
            args.setUserQName(args.getUserQName() + QChatConstant.QTALK_DOMAIN_POSTFIX);
        }
        // 默认虚拟id的host和客服的一致
        if (Strings.isNullOrEmpty(args.getSeatHost())
                || !StringUtils.equals(args.getSeatHost(), QChatConstant.QTALK_HOST)) {
            args.setSeatQName(args.getSeatQName() + QChatConstant.QCHAT_HOST_POSTFIX);
            if (!Strings.isNullOrEmpty(args.getVirtualId())) {
                args.setVirtualId(args.getVirtualId() + QChatConstant.QCHAT_HOST_POSTFIX);
            }
        } else {
            args.setSeatQName(args.getSeatQName() + QChatConstant.QTALK_DOMAIN_POSTFIX);
            if (!Strings.isNullOrEmpty(args.getVirtualId())) {
                args.setVirtualId(args.getVirtualId() + QChatConstant.QTALK_DOMAIN_POSTFIX);
            }
        }
        return true;
    }
}
