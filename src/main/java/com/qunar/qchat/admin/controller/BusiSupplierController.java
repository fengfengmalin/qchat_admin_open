package com.qunar.qchat.admin.controller;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yinmengwang on 17-4-26.
 */
@Slf4j
@Controller
@RequestMapping(value = "/busiSupplier")
public class BusiSupplierController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusiSupplierController.class);

    @Resource
    private ISupplierService supplierService;

    @RequestMapping("/getAllBusiSuppliers.json")
    @ResponseBody
    public JsonResultVO<?> getBusiSuppliers() {
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            LOGGER.warn("未找到该用户：{} - 可管理的店铺。", sysUserVO.getQunarName());
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有找到可管理的店铺.");
        }

        return JsonResultUtil.buildSucceedJsonResult("获取列表成功", sysUserVO.getCurBuSuList());
    }

    @RequestMapping("/pageQuerySupplierList.json")
    @ResponseBody
    public JsonResultVO<?> pageQuerySupplierList( @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "15") int pageSize) {
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            LOGGER.warn("未找到该用户：{} - 可管理的店铺。", sysUserVO.getQunarName());
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有找到可管理的店铺.");
        }
        BusinessEnum  businessEnum = sysUserVO.getbType();
        int businessId = businessEnum != null ? businessEnum.getId() : 0;
        List<SupplierVO> supplierVOList = supplierService.getSupplierByQunarName(sysUserVO.getQunarName(), pageNum, pageSize, businessId);
        SupplierListVO supplierListVO = new SupplierListVO();
        if (CollectionUtils.isNotEmpty(supplierVOList)) {
            Long total = supplierService.getPageCountSupplier(sysUserVO.getQunarName(), businessId);
            supplierVOList.sort(Comparator.comparing(SupplierVO::getId));
            supplierListVO.setSupplierList(supplierVOList);
            supplierListVO.setPageNum(pageNum);
            supplierListVO.setPageSise(pageSize);
            supplierListVO.setTotalCount(total);
        }
        return JsonResultUtil.buildSucceedJsonResult("获取列表成功", supplierListVO);
    }


    @RequestMapping("/getBusiSupplierByBusiName.json")
    @ResponseBody
    public JsonResultVO<?> getBusiSupplierByName(
            @RequestParam(value = "busiSupplierName", defaultValue = "") String busiSupplierName) {
        LOGGER.info("开始获取可管理店铺- 商铺名称：{}", busiSupplierName);
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            LOGGER.warn("未找到该用户：{} - 可管理的店铺。", sysUserVO.getQunarName());
            JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有找到可管理的店铺.");
        }

        List<SupplierVO> supplierDBs = supplierService.filterSupplierByBusiSupplierName(busiSupplierName, sysUserVO.getCurBuSuList());
        if (CollectionUtil.isEmpty(supplierDBs)) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(),
                    BusiResponseCodeEnum.FAIL_NOT_EXISTS.getMsg());
        }


        return JsonResultUtil.buildSucceedJsonResult(supplierDBs);
    }

    @RequestMapping("/getBusiSupplier.json")
    @ResponseBody
    public JsonResultVO<?> getBusiSupplier(@RequestParam(value = "busiSupplierId", defaultValue = "") String busiSupplierId ,
                                           @RequestParam(value = "businessName", defaultValue = "") String businessName
                                           ) {
        SysUserVO sysUserVO = SessionUtils.getLoginUser();
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配客服.");
        }
        BusinessEnum  businessEnum = sysUserVO.getbType();

        if (!businessEnum.getName().equals(businessName)){
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        Supplier supplierDB = supplierService.getSupplierByBusiSupplierIdEx(busiSupplierId, businessEnum.getId());
        if (supplierDB == null) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(),
                    BusiResponseCodeEnum.FAIL_NOT_EXISTS.getMsg());
        }


        return JsonResultUtil.buildSucceedJsonResult(supplierDB);
    }

    @RequestMapping("/setBusiSupplier.json")
    @ResponseBody
    public JsonResultVO<?> setBusiSupplier(@RequestParam(value = "busiSupplierId", defaultValue = "") String busiSupplierId,
                                           @RequestParam(value = "status", defaultValue =  "-1") int status,@RequestParam(value = "extFlag", defaultValue = "-1") int extFlag,
                                           @RequestParam(value = "name", defaultValue = "") String name, @RequestParam(value = "assignStragegy", defaultValue = "-1") int assignStragegy
                                           ) {
        try {
            SysUserVO sysUserVO = SessionUtils.getLoginUser();
            if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), "没有匹配客服.");
            }
            BusinessEnum  businessEnum = sysUserVO.getbType();

//            if (!businessEnum.getName().equals(businessName)){
//                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
//                        BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
//            }

            Supplier supplierDB = supplierService.getSupplierByBusiSupplierIdEx(busiSupplierId, businessEnum.getId());
            if (supplierDB == null) {
                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(),
                        BusiResponseCodeEnum.FAIL_NOT_EXISTS.getMsg());
            }

            if (!Strings.isNullOrEmpty(name)){
                supplierDB.setName(name);
            }
            supplierDB.setbType(businessEnum.getId());

            if (extFlag != -1) {
                supplierDB.setBQueue(extFlag);
            }
            if (status != -1) {
                supplierDB.setStatus(status);
            }
            if (assignStragegy != -1) {
                supplierDB.setAssignStrategy(assignStragegy);
            }
            supplierService.updateFullSupplier(supplierDB);
        //    supplierService.updateSupplierQunarNameMapping(sId, s.getQunarNameList());
            //logger.info("{}成功同步供应商管理员,业务线供应商编号: {} ", LOG_PRE, busiSupplierId);
        } catch (Exception e) {
            //logger.error("{}fail", LOG_PRE, e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }


        return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(),
                BusiResponseCodeEnum.SUCCESS.getMsg(), "");
    }


}
