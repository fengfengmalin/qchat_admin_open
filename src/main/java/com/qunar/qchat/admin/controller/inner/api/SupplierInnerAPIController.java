package com.qunar.qchat.admin.controller.inner.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.Functions;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.SupplierInfo;
import com.qunar.qchat.admin.model.SysUserUpdateRequest;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.service.supplier.SupplierNewService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qchat.admin.vo.third.SupplierOperatorInfo;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
@Controller
@RequestMapping(value = "/i/api/supplier")
public class SupplierInnerAPIController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierInnerAPIController.class);

    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private ISeatService seatService;
    @Resource
    private SupplierNewService supplierNewService;
    @Autowired
    ShopService shopService;


    private static final String DOMAIN1 = "@ejabhost1";
    private static final Function<SeatVO, Long> getSeatSupplierId = new Function<SeatVO, Long>() {
        @Override
        public Long apply(SeatVO seatVO) {
            return seatVO != null ? seatVO.getSupplierId() : null;
        }
    };
    private static final Function<SeatVO, Integer> getBusinessId = new Function<SeatVO, Integer>() {
        @Override
        public Integer apply(SeatVO seatVO) {
            return seatVO != null ? seatVO.getBusinessId() : null;
        }
    };

    @ResponseBody
    @RequestMapping(value = "/saveSupplier.qunar")
    public JsonResultVO<?> saveSupplier(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
        logger.info("saveSupplier -- 请求参数 p: {}", p);
        SupplierVO s = JacksonUtil.string2Obj(p, SupplierVO.class);
        if (null == s) {
            logger.error("saveSupplier -- 添加供应商发生异常");
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        try {
            BusiReturnResult result = supplierService.saveSupplier(s);
            return JsonResultUtil.buildSucceedJsonResult(result.getCode(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("saveSupplier -- 添加供应商发生异常", e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }
    }

    @ResponseBody
    @RequestMapping(value = "/changeSupplierName.qunar")
    public JsonResultVO<?> changeSupplierName(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
        logger.info("changeSupplierName -- 请求参数 p: {}", p);

        try {
            SupplierVO s = JacksonUtil.string2Obj(p, SupplierVO.class);
            BusiReturnResult result = supplierService.updateSupplier(s);
            return JsonResultUtil.buildSucceedJsonResult(result.getCode(), result.getMsg(), "");
        } catch (Exception e) {
            logger.error("changeSupplierName --- 更改供应商名称发生异常", e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }
    }

    @ResponseBody
    @RequestMapping(value = "/updateSupplierManager.qunar")
    public JsonResultVO<?> updateSupplierManager(
            @RequestParam(value = "p", required = true, defaultValue = "") String p) {
        final String LOG_PRE = "updateSupplierManager -- ";
        logger.info("{}p: {}", LOG_PRE, p);
        try {
            SupplierVO s = JacksonUtil.string2Obj(p, SupplierVO.class);
            if (s == null) {
                logger.info("{}请求参数不正确", LOG_PRE);
                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID.getCode(),
                        BusiResponseCodeEnum.FAIL_PARAM_INVALID.getMsg());
            }

            String busiSupplierId = s.getBusiSupplierId();
            int busiType = s.getBusiType();



            SupplierVO supplierDB = supplierService.getSupplierByBusiSupplierId(busiSupplierId, busiType);
            if (supplierDB == null) {
                logger.error("{}业务线供应商不存在, 业务供应商编号: {}", LOG_PRE, busiSupplierId);
                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(),
                        BusiResponseCodeEnum.FAIL_NOT_EXISTS.getMsg());
            }

            long sId = supplierDB.getId();


            supplierService.updateSupplierQunarNameMapping(sId, s.getQunarNameList());
            logger.info("{}成功同步供应商管理员,业务线供应商编号: {} ", LOG_PRE, busiSupplierId);
        } catch (Exception e) {
            logger.error("{}fail", LOG_PRE, e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(),
                BusiResponseCodeEnum.SUCCESS.getMsg(), "");
    }

    @ResponseBody
    @RequestMapping(value = "/sysUserUpdate.qunar", method = RequestMethod.POST)
    public JsonResultVO<?> SysUserUpdate(
            @RequestBody String json
    ) {
        final String LOG_PRE = "sysUserUpdate -- ";
        logger.info("{}p: {}", LOG_PRE, json);
        try {
            SysUserUpdateRequest requestJson = JacksonUtil.string2Obj(json, SysUserUpdateRequest.class);
            if (null == requestJson || Strings.isNullOrEmpty(requestJson.busiSupplierId)) {
                logger.info("{}请求参数不正确", LOG_PRE);
                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID.getCode(),
                        BusiResponseCodeEnum.FAIL_PARAM_INVALID.getMsg());
            }


            SupplierVO supplierDB = supplierService.getSupplierByBusiSupplierId(requestJson.busiSupplierId, requestJson.busiId);
            if (supplierDB == null) {
                logger.error("{}业务线供应商不存在, 业务供应商编号: {}", LOG_PRE, requestJson.busiSupplierId);
                return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS.getCode(),
                        BusiResponseCodeEnum.FAIL_NOT_EXISTS.getMsg());
            }

            long sId = supplierDB.getId();

            supplierService.updateSupplierSysUsers(sId,requestJson.addusers,requestJson.delusers);

            logger.info("{}成功同步供应商管理员,业务线供应商编号: {} ", LOG_PRE, requestJson.busiSupplierId);
        } catch (Exception e) {
            logger.error("{}fail", LOG_PRE, e);
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getCode(),
                    BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION.getMsg());
        }

        return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(),
                BusiResponseCodeEnum.SUCCESS.getMsg(), "");

    }

    @ResponseBody
    @RequestMapping(value = "/getDeptTree.qunar")
    public JsonResultVO<?> getDeptTree(String qunarName) {
        DeptTreeVO root = new DeptTreeVO();
        root.setId(0);
        root.setName("QChatStaff");

        List<SeatVO> sVOList = seatService.getSeatByQunarName(qunarName);
        buildAllInfoList(root, sVOList, qunarName);

        return JsonResultUtil.buildSucceedJsonResult(root);
    }

    private void buildAllInfoList(DeptTreeVO root, List<SeatVO> sList, String qunarName) {
        List<DeptTreeVO> deptList = new ArrayList<DeptTreeVO>();
        // 特殊组 联系运营
        String contactOperator = "联系运营";
        List<DeptUserVO> operators = supplierService.getOperatorsBySeatQunarName(qunarName);
        DeptTreeVO dept = new DeptTreeVO();
        dept.setName(contactOperator);
        dept.setUserList(operators);
        if (CollectionUtil.isNotEmpty(operators)) {
            deptList.add(dept);
        }


        // 特殊组 商务结算
        String businessSettlementOperator = "商务结算";
        DeptTreeVO busiDept = new DeptTreeVO();

        DeptUserVO deptUserVO = new DeptUserVO();
        deptUserVO.setQunarName("kmonich6032");
        deptUserVO.setWebName("度假商务结算");

        List<DeptUserVO> businessOperators = new ArrayList<DeptUserVO>() ;
        businessOperators.add(deptUserVO);

        busiDept.setName(businessSettlementOperator);
        busiDept.setUserList(businessOperators);
        if (CollectionUtil.isNotEmpty(businessOperators)) {
            deptList.add(busiDept);
        }


        // 特殊组 QChat问题反馈中心
        String supplierName = "QChat问题反馈中心";
        List<SeatVO> seatVOList1 = seatService.getSeatListBySupplierName(supplierName);
        long globalSupplierId = getSupplierId(seatVOList1);
        DeptTreeVO dept1 = buildDepTree(supplierName, globalSupplierId, seatVOList1);

        deptList.add(dept1);

        // 特殊组 供应商保险支持 -- 权限控制
        if (CollectionUtils.isNotEmpty(sList)) {
            List<Long> supplierIds = Lists.transform(sList, getSeatSupplierId);
            List<Integer> businessIds = Lists.transform(sList, getBusinessId);
            if (showInsuranceSupport(supplierIds, businessIds)) {
                String insuranceSupport = "供应商保险支持";
                List<SeatVO> insuranceSeats = seatService.getSeatListBySupplierName(insuranceSupport);
                if (CollectionUtils.isNotEmpty(insuranceSeats)) {
                    long insuranceSupplierId = getSupplierId(insuranceSeats);
                    DeptTreeVO insuranceTree = buildDepTree(insuranceSupport, insuranceSupplierId, insuranceSeats);
                    deptList.add(insuranceTree);
                }
            }
        }
        if (CollectionUtil.isNotEmpty(sList)) {
            for (SeatVO s : sList) {
                // 所属供应商
                long supplierId = s.getSupplierId();
                if (globalSupplierId == supplierId) {
                    continue;
                }
                List<SeatVO> seatVOlist2 = seatService.getSeatListBySupplierId(supplierId);
                DeptTreeVO dept2 = buildDepTree(s.getSupplierName(), supplierId, seatVOlist2);
                deptList.add(dept2);
            }
        }

        root.setSubDeptList(deptList);
    }

    private long getSupplierId(List<SeatVO> seatVOList1) {
        long sId = 0;
        if (CollectionUtil.isNotEmpty(seatVOList1)) {
            for (SeatVO s : seatVOList1) {
                sId = s.getSupplierId();
                if (sId > 0) {
                    break;
                }
            }
        }
        return sId;
    }

    private DeptTreeVO buildDepTree(String supplierName, long supplierId, List<SeatVO> seatVOlist) {
        List<DeptUserVO> userList = buildUserList(seatVOlist);

        return buildDeptList(supplierName, supplierId, userList);
    }

    private DeptTreeVO buildDeptList(String supplierName, long supplierId, List<DeptUserVO> userList) {
        DeptTreeVO busiDept = new DeptTreeVO();
        busiDept.setId(supplierId);
        busiDept.setName(StringUtils.trimToEmpty(supplierName));
        if (userList != null) {
            busiDept.setUserList(userList);
        }

        return busiDept;
    }

    private List<DeptUserVO> buildUserList(List<SeatVO> seatVOlist) {
        if (CollectionUtil.isEmpty(seatVOlist)) {
            return new ArrayList<DeptUserVO>();
        }

        List<DeptUserVO> duVOList = new ArrayList<DeptUserVO>();
        for (SeatVO s : seatVOlist) {
            DeptUserVO du = new DeptUserVO();
            du.setQunarName(s.getQunarName());
            du.setWebName(StringUtils.trimToEmpty(s.getWebName()));
            du.setNickName(StringUtils.trimToEmpty(s.getNickName()));
            duVOList.add(du);
        }
        return duVOList;
    }


    @RequestMapping("/saveSupplierOperator.json")
    @ResponseBody
    public JsonData saveSupplierOperator(String p) {
        if (Strings.isNullOrEmpty(p)) {
            return JsonData.error("参数错误");
        }
        List<String> supplierOperatorInfoList = JacksonUtil.string2Obj(p, List.class);
        List<SupplierOperatorInfo> wrongInput = Lists.newArrayList();
        for (Object info : supplierOperatorInfoList) {
            SupplierOperatorInfo supplierOperatorInfo = JacksonUtil.string2Obj(JacksonUtil.obj2String(info),
                    SupplierOperatorInfo.class);
            if (supplierOperatorInfo == null) {
                continue;
            }
            BusinessEnum businessEnum = BusinessEnum.of(supplierOperatorInfo.getBusinessLine());
            if (businessEnum == null) {
                wrongInput.add(supplierOperatorInfo);
                continue;
            }
            if (Strings.isNullOrEmpty(supplierOperatorInfo.getBusiSupplierId())) {
                wrongInput.add(supplierOperatorInfo);
                continue;
            }
            try {
                SupplierVO supplierVO = supplierService
                        .getSupplierByBusiSupplierId(supplierOperatorInfo.getBusiSupplierId(), businessEnum.getId());
                if (supplierVO == null || supplierVO.getId() <= 0) {
                    return JsonData.success("供应商不存在");
                }
                supplierOperatorInfo.setSupplierId(supplierVO.getId());
                // 运营人员均为qunar员工，使用qtalk，增加域名
                String qunarName = supplierOperatorInfo.getQunarName();
                if (!Strings.isNullOrEmpty(qunarName) && !qunarName.endsWith(DOMAIN1)) {
                    supplierOperatorInfo.setQunarName(qunarName + DOMAIN1);
                }
                supplierService.saveSupplierOperator(supplierOperatorInfo);
            } catch (Exception e) {
                logger.error("保存供应商运营人员出错,参数:{}", supplierOperatorInfo.toString(), e);
                wrongInput.add(supplierOperatorInfo);
            }
        }
        if (CollectionUtil.isNotEmpty(wrongInput)) {
            logger.error("保存供应商人员出错,{}", wrongInput.toString());
        }
        return JsonData.success();
    }

    @RequestMapping("/saveSupplierInfo.json")
    @ResponseBody
    public JsonData saveSupplierInfo(String p) {
        if (Strings.isNullOrEmpty(p)) {
            return JsonData.error("参数为空");
        }
        try {
            List<SupplierInfo> supplierInfos = JacksonUtils.string2Obj(p, new TypeReference<List<SupplierInfo>>() {
            });
            supplierService.saveSupplierInfo(supplierInfos);
            return JsonData.success();
        } catch (Exception e) {
            logger.error("同步供应商信息出错,input:{}", p, e);
        }
        return JsonData.error("系统错误");
    }

    @RequestMapping(value = "/deleteSuppliers.json")
    @ResponseBody
    public JsonData deleteSupplier(String busiSupplierIds, int busiId) {
        if (Strings.isNullOrEmpty(busiSupplierIds)) {
            return JsonData.error("供应商id不能为空");
        }
        BusinessEnum busiEnum = BusinessEnum.of(busiId);
        if (busiEnum == null) {
            return JsonData.error("业务线id错误");
        }
        try {
            // 暂时先不考虑下线问题
//            List<String> ids = Splitter.on(",").splitToList(busiSupplierIds);
//            supplierNewService.deleteSuppliersByBusiIds(ids, busiEnum);
            return JsonData.success("删除供应商" + busiSupplierIds + ",busiId=" + busiId + "成功");
        } catch (Exception e) {
            logger.error("删除供应商出错,busiSupplierIds:{},busiId:{}", busiSupplierIds, busiId, e);
        }
        return JsonData.error("系统错误");
    }

    @RequestMapping(value = "/deleteBySupplierIds.json")
    @ResponseBody
    public JsonData deleteSupplier(String supplierIds) {
        if (Strings.isNullOrEmpty(supplierIds)) {
            return JsonData.error("参数错误");
        }
        try {
            List<Long> strIds = Lists.transform(Splitter.on(",").splitToList(supplierIds), Functions.str2Long);
            supplierNewService.deleteSuppliersByIds(strIds);
            return JsonData.success("操作成功");
        } catch (Exception e) {
            logger.error("删除供应商信息出错,supplierIds:{}", supplierIds, e);
        }
        return JsonData.error("系统错误");
    }

    @RequestMapping("/getSupplierStatus.json")
    @ResponseBody
    public JsonData getSupplierStatus(@RequestParam(value = "originalSupplierId") List<Integer> originalSupplierId ,
                                             @RequestParam(value = "businessName") String businessName) {
        logger.debug("getSupplierStatus param originalSupplierId:{}", JacksonUtil.obj2String(originalSupplierId));
        BusinessEnum businessEnum = BusinessEnum.ofByEnName(businessName);
        if (CollectionUtils.isEmpty(originalSupplierId) || businessEnum == null) {
            return JsonData.error("参数错误");
        }

        return shopService.selectBusiSupplierIds(businessEnum.getId(), originalSupplierId);
    }

    // 判断店铺id是否有保险供应商入口
    private boolean showInsuranceSupport(List<Long> supplierIds, List<Integer> busiIds) {


        return false;
    }

    @RequestMapping("/getOtherSupplier.json")
    @ResponseBody
    public JsonData getSupplierStatus(@RequestParam(value = "supplierId") Long supplierId) {
        List<Long> longs = shopService.selectOtherSupplier(supplierId);

        return JsonData.success(longs);
    }
}
