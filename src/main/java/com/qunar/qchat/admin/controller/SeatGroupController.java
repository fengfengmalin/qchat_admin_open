package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.BusinessResponseCodeConstants;
import com.qunar.qchat.admin.service.IBusinessService;
import com.qunar.qchat.admin.service.ISeatGroupService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qyhw on 10/26/15.
 */
@Controller
@RequestMapping("/group")
public class SeatGroupController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(SeatGroupController.class);

    @Autowired
    private ISeatGroupService seatGroupService;
    @Autowired
    private IBusinessService businessService;
    @Autowired
    private ISupplierService supplierService;

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "saveOrUpdateGroup.qunar")
    public JsonResultVO<?> saveOrUpdateGroup(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
        logger.info("saveOrUpdateGroup -- 请求参数 p: {}", p);
        SeatGroupVO sgVO = JacksonUtil.string2Obj(p, SeatGroupVO.class);

        BusiReturnResult result = seatGroupService.saveOrUpdateSeatGroup(sgVO);
        return JsonResultUtil.buildJsonResult(result.isRet(), result.getCode(), result.getMsg(),"");
    }


    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "pageQueryGroupList.qunar")
    public JsonResultVO<?> pageQueryGroupList(@RequestParam(value = "suIds", required = false, defaultValue = "") String suIds,
                                              @RequestParam(value = "groupName", required = false, defaultValue = "") String groupName,
                                              @RequestParam(value = "busiId", required = false, defaultValue = "0") int busiId,
                                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize", required = false, defaultValue = "15") int pageSize) {
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配客服组.");
        }

        List<Long> suIdList = buildSuIdList(suIds, sysUserVO.getCurBuSuList());
        if (CollectionUtil.isEmpty(suIdList)) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配客服组.");
        }

        GroupQueryFilter fiter = buildQueryFilter(suIdList, groupName, sysUserVO.getbType().getId());
        SeatGroupListVO groupList = seatGroupService.pageQueryGroupList(fiter, pageNum, pageSize);
        return JsonResultUtil.buildSucceedJsonResult(groupList);
    }

    private GroupQueryFilter buildQueryFilter(List<Long> suIdList, String groupName, int busiId) {
        GroupQueryFilter fiter = new GroupQueryFilter();
        fiter.setSuIdList(suIdList);
        fiter.setGroupName(groupName);
        fiter.setBusiId(busiId);
       // fiter.setStrategy(strategyId);
        return fiter;
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "deleteGroup.qunar")
    public JsonResultVO<?> deleteGroup(@RequestParam(value = "supplierId", required = true, defaultValue = "0") long supplierId,
                                           @RequestParam(value = "groupId", required = false, defaultValue = "0") int groupId) {
        logger.info("deleteGroup -- 供应商编号: {},客服组编号: {}", supplierId, groupId);

        int num = seatGroupService.delGroupById(groupId);
        if(num == BusinessResponseCodeConstants.FAIL_AUTH_OWNER) {
            return JsonResultUtil.buildFailedJsonResult("不能操作其他供应商的客服组.");
        }
        if(num <= 0) {
            logger.info("deleteGroup -- 删除客服组失败");
            return JsonResultUtil.buildFailedJsonResult("删除客服组失败");
        }
        logger.info("deleteGroup -- 删除客服组成功,执行记录数:{}" + num);
        return JsonResultUtil.buildSucceedJsonResult("删除客服组成功","");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "queryBusiGroup.qunar")
    public JsonResultVO<?> queryBusiGroup(@RequestParam(value = "supplierId", required = true, defaultValue = "0") long supplierId) {
        if (!SessionUtils.checkInputSuIdIsValid(supplierId)) {
            logger.error("queryBusiGroup -- 不能获取其他供应商的业务组信息, 当前登陆用户:{}, 操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), supplierId);
            return JsonResultUtil.buildFailedJsonResult("不能获取其他供应商的业务组信息.");
        }
        List<BusinessVO> busiList = businessService.getBusiGroupMappingBySupplierId(supplierId);
        return JsonResultUtil.buildSucceedJsonResult(busiList);
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "querySuGroup.qunar")
    public JsonResultVO<?> querySuGroup() {
        List<SupplierGroupVO> suGroupList = new ArrayList<>();

        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isNotEmpty(sysUserVO.getCurBuSuList())) {
            List<Long> suIdList = new ArrayList<>(sysUserVO.getCurBuSuList().size());
            for (SupplierVO suVO : sysUserVO.getCurBuSuList()) {
                suIdList.add(suVO.getId());
            }
            suGroupList = supplierService.getSuGroupList(suIdList);
        }

        return JsonResultUtil.buildSucceedJsonResult(suGroupList);
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "assignProducts.qunar")
    public JsonResultVO<?> assignProducts(
            @RequestParam(value = "groupId") Integer groupId
            ,@RequestParam(value = "pIds") String pIds
    ) {
        boolean isOK = seatGroupService.assignProducts(groupId, pIds);
        if (isOK) {
            return JsonResultUtil.buildSucceedJsonResult("分配成功", null);
        }
        return JsonResultUtil.buildFailedJsonResult("分配失败");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "queryProducts.qunar")
    public JsonResultVO<?> queryProducts(
            @RequestParam(value = "groupId") Integer groupId
    ) {
        List<String> pidList = seatGroupService.queryProducts(groupId);
        if (pidList == null) {
            pidList = new ArrayList<>();
        }
        return JsonResultUtil.buildSucceedJsonResult(pidList);
    }
}