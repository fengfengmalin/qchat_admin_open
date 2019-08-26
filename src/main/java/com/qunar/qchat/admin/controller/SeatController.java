package com.qunar.qchat.admin.controller;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.BusinessResponseCodeConstants;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.model.LogEntity;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.plugins.chatplugin.ChatPluginInstance;
import com.qunar.qchat.admin.plugins.chatplugin.IChatPlugin;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.service.CsrService;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qtalk.ss.sift.service.SiftStrategyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/19/15.
 */
@Controller
@RequestMapping("/seat")
public class SeatController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    @Autowired
    private ISeatService seatService;

    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private CsrService csrService;
    @Autowired
    ShopService shopService;

    @Autowired
    private SiftStrategyService siftStrategyService;

    @RequestMapping("/onlineStatus.json")
    @ResponseBody
    public JsonData getSeatOnLineStatus(String names, @RequestParam(value = "host", required = false) String host) {
        logger.info("查询客服状态，输入参数：{}", names);
        if (StringUtils.isBlank(names)) {
            return JsonData.error("参数为空");
        }
        try {
            List<String> nameList = Splitter.on(",").splitToList(names);
            if (nameList.size() > 50) {
                return JsonData.error("一次最多查询50个客服状态");
            }
            List<SeatOnlineState> seatOnlineStateList;
            IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(host);
            if (null!=plugin){
                seatOnlineStateList = plugin.getUsersOnlineStatus(nameList);
            } else {
                return JsonData.error(" host 错误");
            }

            List<SeatStatusVO> seatStatusVOList = Lists.newArrayList();
            if (!CollectionUtil.isEmpty(seatOnlineStateList)){
                for (SeatOnlineState sos : seatOnlineStateList) {
                    SeatStatusVO seatStatusVO = new SeatStatusVO();
                    seatStatusVO.setName(EjabdUtil.makeSureUserid(sos.getStrId()));
                    seatStatusVO.setStatus(sos.getOnlineState());
                    seatStatusVOList.add(seatStatusVO);
                }
            }
            return JsonData.success(seatStatusVOList, "查询成功");
        } catch (Exception e) {
            logger.error("查询客服状态发生异常，输入参数：{}，异常信息：", names, e);
            return JsonData.error("查询发生异常");
        }
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "saveOrUpdateSeat.qunar")
    public JsonResultVO<?> saveOrUpdateSeat(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
        logger.info("saveOrUpdateSeat -- p: {}", p);
        SeatVO seatVO = JacksonUtil.string2Obj(p, SeatVO.class);

        BusiReturnResult result = seatService.saveOrUpdateSeat(seatVO);
        return JsonResultUtil.buildJsonResult(result.isRet(), result.getCode(), result.getMsg(),"");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "pageQuerySeatList.qunar")
    public JsonResultVO<?> pageQuerySeatList(@RequestParam(value = "suIds", required = false, defaultValue = "") String suIds,
                                             @RequestParam(value = "qunarName", required = false, defaultValue = "") String qunarName,
                                             @RequestParam(value = "webName", required = false, defaultValue = "") String webName,
                                             @RequestParam(value = "busiType", required = false, defaultValue = "0") int busiType,
                                             @RequestParam(value = "bySort", required = false, defaultValue = "") String bySort,
                                             @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                             @RequestParam(value = "pageSize", required = false, defaultValue = "15") int pageSize) {
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配客服.");
        }

        List<Long> suIdList = buildSuIdList(suIds, sysUserVO.getCurBuSuList());
        if (CollectionUtil.isEmpty(suIdList)) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配客服.");
        }

        SeatQueryFilter fiter = buildQueryParam(suIdList, qunarName, webName, sysUserVO.getbType().getId(), bySort);
        SeatListVO seatListVO = seatService.pageQuerySeatList(fiter, pageNum, pageSize);
        return JsonResultUtil.buildSucceedJsonResult(seatListVO);
    }

    private SeatQueryFilter buildQueryParam(List<Long> suIdList, String qunarName, String webName, int busiType, String bySort) {
        SeatQueryFilter fiter = new SeatQueryFilter();
        fiter.setSuIdList(suIdList);
        fiter.setQunarName(qunarName);
        fiter.setWebName(webName);
        fiter.setBusiId(busiType);
        fiter.setBySort(bySort);
        return fiter;
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "deleteSeat.qunar")
    public JsonResultVO<?> deleteSeat(@RequestParam(value = "supplierId", required = true, defaultValue = "0") long supplierId,
                                                      @RequestParam(value = "seatId", required = false, defaultValue = "0") long seatId) {
        logger.info("deleteSeat -- 供应商编号为: {},客服编号: {}", supplierId, seatId);
        int num = seatService.delSeatById(seatId);

        if(num == BusinessResponseCodeConstants.FAIL_AUTH_OWNER) {
            return JsonResultUtil.buildFailedJsonResult("不能操作其他供应商客服.");
        }

        if(num <= 0) {
            logger.info("deleteSeat -- 删除客服失败");
            return JsonResultUtil.buildFailedJsonResult("删除客服失败");
        }
        LogUtil.doLog(LogEntity.OPERATE_DELETE, LogEntity.ITEM_SEAT, (int) seatId, null, SessionUtils.getUserName(),
                null);
        logger.info("deleteSeat -- 删除客服成功, 执行记录数: {}", num);
        return JsonResultUtil.buildSucceedJsonResult("删除客服成功","");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "sortSeat.qunar")
    public JsonResultVO<?> sortSeat(@RequestParam(value = "supplierId", required = true, defaultValue = "0") long supplierId,
                                    @RequestParam(value = "preSeatId", required = false, defaultValue = "0") long preSeatId,
                                    @RequestParam(value = "curSeatId", required = true) long curSeatId) {
        logger.info("sortSeat -- 供应商编号为: {},上一个位置客服编号: {},当前客服编号: {}", supplierId, preSeatId, curSeatId);

        if (!SessionUtils.checkInputSuIdIsValid(supplierId)) {
            logger.error("sortSeat -- 不能操作其他供应商的客服, 当前登陆用户:{}, 操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), supplierId);
            return JsonResultUtil.buildFailedJsonResult("不能获取其他供应商的客服.");
        }

        boolean isOK = seatService.sortSeat(preSeatId,curSeatId,supplierId);
        if(!isOK) {
            logger.info("sortSeat -- 客服排序失败");
            return JsonResultUtil.buildFailedJsonResult("客服排序失败");
        }
        return JsonResultUtil.buildSucceedJsonResult("sucess", "");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "checkLoginName.qunar")
    public JsonResultVO<?> checkLoginName(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
        logger.info("checkLoginName -- p: {}", p);
        // 检查是否需要去用户中心校验
        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        if (!needUserInfoCheck(systemUser)) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("userName", p);
            return JsonResultUtil.buildJsonResult(true, BusiResponseCodeEnum.SUCCESS.getCode(), null, map);
        }

        IChatPlugin plugin = ChatPluginInstance.getInstance().getChatPlugin(EjabdUtil.getDomain(p, QChatConstant.DEFAULT_HOST));
        if (null == plugin)
            return JsonResultUtil.buildFailedJsonResult("domain  error");


        BusiReturnResult result = plugin.checkUserExist(EjabdUtil.makeSureUserid(p));
        if (null == result)
            return JsonResultUtil.buildFailedJsonResult("not find  error");

        return JsonResultUtil.buildJsonResult(result.isRet(), result.getCode(), result.getMsg(), result.getData());
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "getSuListByQName.qunar")
    public JsonResultVO<?> getSuListByQName(@RequestParam(value = "qName", required = true) String qName) {
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        List<SupplierVO> curSuList = sysUserVO.getCurBuSuList();
        if (CollectionUtil.isEmpty(curSuList)) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配供应商.");
        }

        List<SupplierVO> suList = supplierService.getSupplierBySeatQName(qName, sysUserVO.getbType().getId());

        suList = filterSupplier(curSuList, suList);
        return JsonResultUtil.buildSucceedJsonResult("success", suList);
    }

    private List<SupplierVO> filterSupplier(List<SupplierVO> curSuList, List<SupplierVO> suList) {
            if (CollectionUtil.isNotEmpty(suList)) {
            List<SupplierVO> newSuList = Lists.newArrayList();
            Map<Long, SupplierVO> curSuMap = Maps.uniqueIndex(curSuList.iterator(), new Function<SupplierVO, Long>() {
                @Override
                public Long apply(SupplierVO supplierVO) {
                    return supplierVO.getId();
                }
            });

            Map<Long, SupplierVO> suMap = Maps.uniqueIndex(suList.iterator(), new Function<SupplierVO, Long>() {
                @Override
                public Long apply(SupplierVO supplierVO) {
                    return supplierVO.getId();
                }
            });

            for (Long key : suMap.keySet()) {
                if (curSuMap.containsKey(key)) {
                    newSuList.add(curSuMap.get(key));
                }
            }
            suList = newSuList;
        }
        return suList;
    }

    private boolean needUserInfoCheck(SysUserVO sysUserVO){
        if(sysUserVO==null){
            return true;
        }
        List<SupplierVO> buSuList = sysUserVO.getCurBuSuList();
        if(CollectionUtil.isEmpty(buSuList)){
            return true;
        }

        return true;
    }

    @RequestMapping("/judgeSeatBelong.json")
    @ResponseBody
    public JsonData jdugeSeatBelong(String qunarName, String supplierIds) {
        try {
            Map<String, Object> result = Maps.newHashMap();
            if (Strings.isNullOrEmpty(supplierIds)) {
                result.put("belongTo", false);
                return JsonData.success(result);
            }
            List<String> supplierIdList = Splitter.on(",").splitToList(supplierIds);
            List<SupplierVO> supplierVOs = supplierService.getSupplierBySeatQName(qunarName);
            if (CollectionUtils.isNotEmpty(supplierVOs)) {
                for (SupplierVO supplierVO : supplierVOs) {
                    if (supplierIdList.contains(String.valueOf(supplierVO.getId()))) {
                        result.put("belongTo", true);
                        return JsonData.success(result);
                    }
                }
            }
            result.put("belongTo", false);
            return JsonData.success(result);
        } catch (Exception e) {
            logger.error("查询{}是否属于{}供应商id出错", qunarName, supplierIds.toString(), e);
        }
        return JsonData.error("系统错误");
    }

    @RequestMapping("/updateWxStatus.json")
    @ResponseBody
    public JsonData updateWxStatus(@RequestParam String csrName, @RequestParam Integer bindWxStatus) {
        logger.debug("updateWxStatus csrName:{} bindWxStatus:{}", csrName, bindWxStatus);
        return seatService.updateWxStatus(csrName, bindWxStatus);
    }

    @RequestMapping("/getSeatList.json")
    @ResponseBody
    public JsonData getSeatListBySuId(@RequestParam String shopJid, @RequestParam String currentCsrName, @RequestParam String domain) {
        try {
            if (StringUtils.isNumeric(shopJid)) {
                shopJid = "shop_" + shopJid;
            }
            if (!shopJid.contains("@")) {
                shopJid = String.format("%s@%s", shopJid, domain);
            }

            Long shopId = shopService.selectShopByBsiId(shopJid);

//            Long shopId = Shop.parseString(shopJid);
            if (shopId == null || shopId < 1)
                return JsonData.error("参数错误", 500);

            List<CSR> csrList = csrService.queryCsrsByShopIdWithoutCarName(shopId, currentCsrName, domain);
            csrList = siftStrategyService.filterOnlineCSRs(csrList, shopId, null);
            logger.info("getSeatListBySuId result :{}", csrList.size());
            return JsonData.success(csrList, "success");
        } catch (Exception e) {
            return JsonData.error("系统错误", 500);
        }
    }

    @RequestMapping("/transformCsr.json")
    @ResponseBody
    public JsonData transformCsr(@RequestParam String shopJid, @RequestParam String currentCsrName, @RequestParam String domain,
                                 @RequestParam String newCsrName, @RequestParam String customerName, @RequestParam String reason) {
        logger.debug("transformCsr shopJid:{} oldCsrName:{} newCsrName:{} customName:{}", shopJid, currentCsrName, newCsrName, customerName);
        if (StringUtils.isEmpty(currentCsrName) || StringUtils.isEmpty(newCsrName) || StringUtils.isEmpty(customerName)) {
            logger.warn("currentCsrName:{} newCsrName:{} customerName:{}", currentCsrName, newCsrName, customerName);
            return JsonData.error("参数错误", 500);
        }

        if (currentCsrName.equalsIgnoreCase(newCsrName)) {
            return JsonData.error("相同客服，无法转移", 500);
        }

        return siftStrategyService.transformCsr(shopJid, currentCsrName, newCsrName, customerName, reason, domain);
    }

}
