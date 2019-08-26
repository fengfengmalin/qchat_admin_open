package com.qunar.qchat.admin.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.annotation.RecordAccessLog;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.service.IBusinessService;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qchat.admin.vo.SysUserVO;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import com.qunar.qchat.admin.vo.conf.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Author : mingxing.shao
 * Date : 15-10-26
 *
 */
@RequestMapping("/sys")
@Controller
public class ManageController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ManageController.class);

    @Resource(name = "businessService")
    private IBusinessService businessService;

    @Resource
    private IRobotService robotService;

    @Resource
    private ISupplierService supplierService;


    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/manage.do")
    public ModelAndView getManagePage() {
        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        ModelAndView mav = new ModelAndView("page/admin/GMManage");

        mav.addObject("suList", JacksonUtil.obj2String(systemUser.getCurBuSuList()));
        mav.addObject("bType", systemUser.getbType().getId());
        mav.addObject("isSupplierService", BusinessEnum.VACATION.getId() == systemUser.getbType().getId());
        return mav;
    }

    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/sessionList.do")
    public ModelAndView getSessionListPage() {
        return new ModelAndView("page/admin/sessionManage");
    }

    @RequestMapping("/queryBusinessList.qunar")
    @ResponseBody
    public JsonResultVO<?> getBusinesses(@RequestParam(value = "supplierId", required = true) long supplierId) {
        List<Business> businessList = businessService.getBusinessesBySupplierId(supplierId);
        if (CollectionUtil.isNotEmpty(businessList)) {
            return JsonResultUtil.buildSucceedJsonResult(businessList);
        }
        return JsonResultUtil.buildFailedJsonResult("未查询到任何数据");
    }

//    @RequestMapping("/queryAllStrategyList.qunar")
//    @ResponseBody
//    public JsonResultVO<?> getAllStrategy() {
//        SeatSortStrategyEnum[] strategyEnums = SeatSortStrategyEnum.values();
//        List<Map<String, Object>> res = new ArrayList<>(strategyEnums.length);
//
//        for (SeatSortStrategyEnum strategy : strategyEnums) {
//            if (SeatSortStrategyEnum.DEFAULT_STRATEGY.equals(strategy)) {
//                continue;
//            }
//            Map<String, Object> enumMap = new HashMap<>(2);
//            enumMap.put("id", strategy.getStrategyId());
//            enumMap.put("name", strategy.getName());
//            res.add(enumMap);
//        }
//        return JsonResultUtil.buildSucceedJsonResult(res);
//    }

    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/wechat.qunar")
    public ModelAndView wechatPage() {
        SysUserVO userVO = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        ModelAndView mv = new ModelAndView("page/admin/bindWeixin");
        List<SupplierVO> surSupplierInfos = userVO.getCurBuSuList();
        if (CollectionUtil.isEmpty(surSupplierInfos)) {
            return mv;
        }
        //组装客服信息
        List<SupplierVO> supplierInfos = seatService.filterSeatList(surSupplierInfos);
        mv.addObject("supplierInfos", JacksonUtils.obj2String(supplierInfos));

        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        mv.addObject("suList", JacksonUtil.obj2String(systemUser.getCurBuSuList()));
        mv.addObject("bType", systemUser.getbType().getId());
        mv.addObject("isSupplierService", BusinessEnum.VACATION.getId() == systemUser.getbType().getId());

        return mv;
    }

    @RequestMapping(value = "/saveRobotConfig.qunar", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultVO<?> saveRobotConfig(
            @RequestBody String json
    ) {
        try {
            if (Strings.isNullOrEmpty(json))
                return JsonResultUtil.buildFailedJsonResult("param body error");
            Map<String, Object> input = JacksonUtil.string2Obj(json, Map.class);
            if (null == input)
                return JsonResultUtil.buildFailedJsonResult("param body format error");
            String robotName = input.get("robotname").toString();
            long supplierid = Long.valueOf(input.get("supplierid").toString());
            int robotstrategy = Integer.valueOf(input.get("robotstrategy").toString());
            String robotwelcome = input.get("robotwelcome").toString();
            // sava to data base
            boolean result = false;
            if (null != robotService) {
                SupplierWithRobot supplierWithRobot = new SupplierWithRobot();
                supplierWithRobot.setId(supplierid);
                supplierWithRobot.setRobot_id(robotName);
                supplierWithRobot.setStrategy(robotstrategy);
                supplierWithRobot.setWelcome(robotwelcome);
                result = robotService.updateOrInsertSupplierRobotConfig(supplierWithRobot);
            }
            return result ? JsonResultUtil.buildSucceedJsonResult("操作成功") : JsonResultUtil.buildFailedJsonResult("操作失败");
        } catch (Exception e) {
            return JsonResultUtil.buildFailedJsonResult(e.getMessage());
        }
    }

    private String translateUrl(String url, HttpServletRequest request) {
        URLBuilder builder = URLBuilder.builder();
        builder.setHost(url);
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (null != parameterMap) {
            for (String key :
                    parameterMap.keySet()) {
                for (String value : parameterMap.get(key)
                        ) {
                    if ("supplier_id".equalsIgnoreCase(key)) {
                        {
                            try {
                                BusiSupplierMapping busiSupplierMapping = supplierService.getSupplierBySupplierId(Long.valueOf(value));
                                if (null != busiSupplierMapping) {
                                    value = String.valueOf(busiSupplierMapping.getBusiSupplierId());
                                }
                            } catch (Exception e) {
                                logger.error("translateUrl error", e);
                            }
                        }

                    }
                    builder.addQuery(key, value);
                }
            }

        }
        String resultUrl = builder.build();
        logger.info("translateUrl result:{}", resultUrl);
        return resultUrl;
    }


    @RequestMapping(value = "/ml/supplier_robot/config.qunar")
    @ResponseBody
    public String mlSupplierRobotConfig(HttpServletRequest request) {
        String url = "";

        url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/config", request);

        return HttpClientUtils.get(url);
    }

    @RequestMapping(value = "/ml/supplier_robot/update_config.qunar")
    @ResponseBody
    public String mlSupplierRobotUpdateConfig(HttpServletRequest request) {
        String url = "";

        url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/update_config", request);

        return HttpClientUtils.get(url);
    }


    @RequestMapping(value = "/ml/supplier_robot/qalist.qunar")
    @ResponseBody
    public String mlSupplierRobotQAList(HttpServletRequest request) {
        String url;
        url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/qalist", request);

        return HttpClientUtils.get(url);
    }

    @RequestMapping(value = "/ml/supplier_robot/qaid.qunar")
    @ResponseBody
    public String mlSupplierRobotQAId(HttpServletRequest request) {
        String url;
        url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/qaid", request);

        return HttpClientUtils.get(url);
    }

    @RequestMapping(value = "/ml/supplier_robot/update_qa.qunar")
    @ResponseBody
    public String mlSupplierRobotUpdateQA(HttpServletRequest request) {
        String url;

        url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/update_qa", request);

        String result = HttpClientUtils.get(url);
        return result;
    }


    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/smartConsult.do")
    public ModelAndView smartConsultPage() {


        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);

        List<SupplierVO> openRbtSupplierVoList = Lists.newArrayList();

        List<SupplierVO> supplierVOList = systemUser.getCurBuSuList();
        String buName = systemUser.getbType().getEnName();
        if (null != supplierVOList) {
            for (SupplierVO supplierVO : supplierVOList) {
                if (RobotConfig.robotEnabel(buName, supplierVO.getId())) {
                    openRbtSupplierVoList.add(supplierVO);
                }
            }
        }

        ModelAndView mav = new ModelAndView("page/admin/smartConsult");
        mav.addObject("suList", JacksonUtil.obj2String(openRbtSupplierVoList));
        mav.addObject("bType", systemUser.getbType().getId());

        mav.addObject("isSupplierService", BusinessEnum.VACATION.getId() == systemUser.getbType().getId());


        return mav;
    }

    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/supplierFAQ.do")
    public ModelAndView supplierFAQPage() {

        SysUserVO systemUser = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);

        ModelAndView mav = new ModelAndView("page/admin/supplierFAQ");
        mav.addObject("suList", JacksonUtil.obj2String(systemUser.getCurBuSuList()));
        mav.addObject("bType", systemUser.getbType().getId());

        mav.addObject("isSupplierService", BusinessEnum.VACATION.getId() == systemUser.getbType().getId());

        return mav;
    }


    @RequestMapping(value = "/addSupplier.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonData addSupplier() {
        SupplierVO supplier2 = new SupplierVO();
        supplier2.setName("IT热线");
        supplier2.setQunarNameList(Arrays.asList("ywud2995"));
        supplier2.setBusiSupplierId("1");
        supplier2.setBusiType(2000);
//
        Seat s1 = new Seat();
        s1.setQunarName("wz.wang@ejabhost1");
        s1.setWebName("");

        List<Seat> seats = Lists.newArrayList();
        seats.add(s1);
        supplier2.setSeatList(seats);

        BusiReturnResult r = supplierService.saveSupplier(supplier2);

        return JsonData.success(r);
    }


    @RequestMapping(value = "/yn_feedback.qunar")
    @ResponseBody
    public String ynFeedback(HttpServletRequest request) {

//        String paramString = translateJsonUrl( request);
//        String uri = Config.NEW_SUPPLIERROBOT_URL + "/yn_feedback";
//        String result = HttpClientUtils.postJson(uri, paramString);
        String url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/yn_feedback", request);
        String result = HttpClientUtils.get(url);
        logger.info("ynFeedback result:{}", result);
        return result;
    }

    @RequestMapping(value = "/feedback.qunar")
    @ResponseBody
    public String feedback(HttpServletRequest request) {


        String url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/feedback", request);
        String result = HttpClientUtils.get(url);
        logger.info("feedback result:{}", result);
        return result;
    }

    @RequestMapping(value = "/update_feedback.qunar")
    @ResponseBody
    public String updateFeedback(HttpServletRequest request) {


        String url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/update_feedback", request);
        String result = com.qunar.qtalk.ss.utils.HttpClientUtils.get(url);
        logger.info("updateFeedback result:{}", result);
        return result;
    }

    @RequestMapping(value = "/ignore_feedback.qunar")
    @ResponseBody
    public String ignoreFeedback(HttpServletRequest request) {

        String url = translateUrl(Config.NEW_SUPPLIERROBOT_URL + "/ignore_feedback", request);
        String result = HttpClientUtils.get(url);
        logger.info("ignoreFeedback result:{}", result);
        return result;
    }



}
