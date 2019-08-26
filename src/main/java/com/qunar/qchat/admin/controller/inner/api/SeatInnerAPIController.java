package com.qunar.qchat.admin.controller.inner.api;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.annotation.RecordAccessLog;
import com.qunar.qchat.admin.controller.BaseController;
import com.qunar.qchat.admin.dao.IUserDao;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.LogEntity;
import com.qunar.qchat.admin.model.Robot;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.model.responce.CommonUserVcardInfo;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.conf.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-11-9
 *
 */
@Slf4j
@Controller
@RequestMapping("/i/api/seat")
public class SeatInnerAPIController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SeatInnerAPIController.class);

    @Resource(name = "seatService")
    private ISeatService seatService;


    @Resource(name = "userDao")
    private IUserDao userDao;

    @Resource
    private ISupplierService supplierService;


    @Resource
    private IRobotService robotService;


    @RequestMapping("/virtual_info.qunar")
    @ResponseBody
    public JsonResultVO<?> getVirtualInfo(
        @RequestParam(value = "name") String virtualName
    ){
        if (Strings.isNullOrEmpty(virtualName))
            return JsonResultUtil.buildFailedJsonResult("input error");
        if (virtualName.endsWith(QChatConstant.SEATROBOTPOSTFIX)){

            Robot robot = robotService.getRobotByRobotid(virtualName);
            if (null!=robot){
                CommonUserVcardInfo info = new CommonUserVcardInfo();
                info.setUsername(virtualName);
                info.setNickname(robot.getRobotName());
                return JsonResultUtil.buildSucceedJsonResult(info);
            }
        }

        if (virtualName.startsWith(QChatConstant.SEATSHOPPREFIX)){
            List<Long> shopIds = Lists.newArrayList();

            shopIds.add(Long.valueOf(virtualName.replace(Supplier.SHOPID_PREFIX, "")));

            List<Map<String, Object>> shops = SupplierServiceUtil.buildSupplierInfo(shopIds);
            if (!CollectionUtil.isEmpty(shops)){
                CommonUserVcardInfo info = new CommonUserVcardInfo();
                if (!Strings.isNullOrEmpty(shops.get(0).get("name").toString()))
                    info.setNickname(shops.get(0).get("name").toString());
                info.setUsername(virtualName);

                return  JsonResultUtil.buildSucceedJsonResult(info);
            }
        }
        return JsonResultUtil.buildFailedJsonResult("can not find user");
    }

    /**
     * 该接口只允许内网访问
     *
     * @param strQunarNames qunarNames
     * @param fields        需要查询的属性
     * @return 信息
     */
    @RequestMapping("/info.qunar")
    @ResponseBody
    public Map<String, ?> getInfo(@RequestParam(value = "qunarNames") String strQunarNames,
                                  @RequestParam(value = "fields", required = false, defaultValue = "") String fields) {
        long startTime = System.currentTimeMillis();

        List<String> qunarNames = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(StringUtils.lowerCase(strQunarNames));
        Map<String, ?> res = seatService.getUserAndSeatInfo(qunarNames, fields);


        if (CollectionUtil.isNotEmpty(res)) {
            List<String> lstQunarNames = Lists.newArrayList();
            if (res.containsKey("data") && null != res.get("data")) {
                Object dataValue = res.get("data");
                if (dataValue instanceof List) {
                    for (Object obj : (List) dataValue) {
                        if (obj instanceof Map && ((Map) obj).containsKey("username")) {
                            String userid = (String) ((Map) obj).get("username");
                            if (!Strings.isNullOrEmpty(userid))
                                lstQunarNames.add(userid);
                        }
                    }

                    for (String qunarid : qunarNames) {
                        if (!Strings.isNullOrEmpty(qunarid) && !lstQunarNames.contains(qunarid) && 0 == qunarid.indexOf("anony.")) {
                            Map<String, Object> anonyInfo = Maps.newHashMap();
                            anonyInfo.put("username", qunarid);
                            anonyInfo.put("loginName", qunarid);
                            String nickName = ("游客" + String.valueOf(qunarid.hashCode() % 10000)).replace("-","");
                            anonyInfo.put("nickname", nickName);

                            anonyInfo.put("uid",0);
                            anonyInfo.put("mobile","");
                            anonyInfo.put("prenum","");
                            anonyInfo.put("status",1);
                            anonyInfo.put("type",1);
                            anonyInfo.put("regip",0);
                            anonyInfo.put("regtime",0);
                            anonyInfo.put("loginip",0);
                            anonyInfo.put("logintime",0);
                            anonyInfo.put("appflag",0);
                            anonyInfo.put("email","");
                            anonyInfo.put("emailverified",0);
                            anonyInfo.put("quickFlag","");
                            anonyInfo.put("credit",0);
                            anonyInfo.put("mobileverified",1);
                            anonyInfo.put("gender",0);
                            anonyInfo.put("birthday","");
                            anonyInfo.put("imageurl","");
                            anonyInfo.put("tempnickname","");
                            anonyInfo.put("oauth","");
                            anonyInfo.put("pwdType",3);
                            anonyInfo.put("webname",nickName);
                            anonyInfo.put("suppliername","");

                            ((List) dataValue).add(anonyInfo);
                        }
                    }
                }
            }

            return res;
        }
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("ret", false);
        errorMap.put("errmsg", "未查询到任何数据");
        return errorMap;
    }

    /**
     * 该接口只允许内网访问
     * 整合qchat.php接口
     * @param strQunarNames qunarNames
     * @param fields        需要查询的属性
     * @return 信息
     */
    @RequestMapping("/newinfo.qunar")
    @ResponseBody
    public Map<String, ?> getNewInfo(@RequestParam(value = "qunarNames") String strQunarNames,
                                  @RequestParam(value = "fields", required = false, defaultValue = "") String fields) {
        long startTime = System.currentTimeMillis();

        List<String> qunarNames = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(StringUtils.lowerCase(strQunarNames));
        Map<String, ?> res = seatService.getNewUserAndSeatInfo(qunarNames, fields);


        if (CollectionUtil.isNotEmpty(res)) {
            List<String> lstQunarNames = Lists.newArrayList();
            if (res.containsKey("data") && null != res.get("data")) {
                Object dataValue = res.get("data");
                if (dataValue instanceof List) {
                    for (Object obj : (List) dataValue) {
                        if (obj instanceof Map && ((Map) obj).containsKey("username")) {
                            String userid = (String) ((Map) obj).get("username");
                            if (!Strings.isNullOrEmpty(userid))
                                lstQunarNames.add(userid);
                        }
                    }

                    for (String qunarid : qunarNames) {
                        if (!Strings.isNullOrEmpty(qunarid) && !lstQunarNames.contains(qunarid) && 0 == qunarid.indexOf("anony.")) {
                            Map<String, Object> anonyInfo = Maps.newHashMap();
                            anonyInfo.put("username", qunarid);
                            anonyInfo.put("loginName", qunarid);
                            String nickName = ("游客" + String.valueOf(qunarid.hashCode() % 10000)).replace("-","");
                            anonyInfo.put("nickname", nickName);

                            anonyInfo.put("uid",0);
                            anonyInfo.put("mobile","");
                            anonyInfo.put("prenum","");
                            anonyInfo.put("status",1);
                            anonyInfo.put("type",1);
                            anonyInfo.put("regip",0);
                            anonyInfo.put("regtime",0);
                            anonyInfo.put("loginip",0);
                            anonyInfo.put("logintime",0);
                            anonyInfo.put("appflag",0);
                            anonyInfo.put("email","");
                            anonyInfo.put("emailverified",0);
                            anonyInfo.put("quickFlag","");
                            anonyInfo.put("credit",0);
                            anonyInfo.put("mobileverified",1);
                            anonyInfo.put("gender",0);
                            anonyInfo.put("birthday","");
                            anonyInfo.put("imageurl","");
                            anonyInfo.put("tempnickname","");
                            anonyInfo.put("oauth","");
                            anonyInfo.put("pwdType",3);
                            anonyInfo.put("webname",nickName);
                            anonyInfo.put("suppliername","");

                            ((List) dataValue).add(anonyInfo);
                        }
                    }
                }
            }

            return res;
        }
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("ret", false);
        errorMap.put("errmsg", "未查询到任何数据");
        return errorMap;
    }

    @RequestMapping("/updateInfo.qunar")
    @ResponseBody
    public JsonResultVO<?> updateSeatInfo(@RequestParam(value = "qunarName") String qunarName,
                                          @RequestParam(value = "webName") String newWebName) {
        int updateSize = seatService.updateSeatByQunarName(StringUtils.lowerCase(qunarName), newWebName);
        if (updateSize <= 0) {
            return JsonResultUtil.buildFailedJsonResult("更新失败");
        }
        LogUtil.doLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_SEAT, null, qunarName, SessionUtils.getUserName(),
                "/updateInfo.qunar 更新webName为:" + newWebName);
        return JsonResultUtil.buildSucceedJsonResult("更新成功", null);
    }

    @RequestMapping("/infoList.qunar")
    @ResponseBody
    public JsonResultVO<?> getSeatInfoListByBusiSupplierIds(
            @RequestParam(value = "busiSupplierIds") String busiSupplierIds
            , @RequestParam(value = "businessId") int businessId
    ) {
        long startTime = System.currentTimeMillis();
        BusinessEnum businessEnum = BusinessEnum.of(businessId);
        if (businessEnum == null) {
            return JsonResultUtil.buildFailedJsonResult("该业务线不存在");
        }
        List<String> busiSupplierIdList = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(busiSupplierIds);
        if (CollectionUtil.isEmpty(busiSupplierIdList)) {
            return JsonResultUtil.buildFailedJsonResult("没有传入任何供应商");
        }

        List<SupplierQunarNameMappingVO> resultData = seatService.getQunarNamesByBusiSupplierIds(busiSupplierIdList, businessEnum);
        return JsonResultUtil.buildSucceedJsonResult(resultData);
    }

    @RequestMapping("/getAllSeatNames.qunar")
    @ResponseBody
    public JsonResultVO<?> getAllSeatNames(@RequestParam(value = "supplierIds", required = false, defaultValue = "") String supplierIdsStr,
                                           @RequestParam(value = "busiType") Integer busiType) {
        return getAllSeatNamesBase(supplierIdsStr, busiType);
    }

    @RequestMapping("/startSession.qunar")
    @ResponseBody
    public JsonResultVO<?> updateSeatSession(@RequestParam(value = "seatId", required = false, defaultValue = "0") String seatId
            , @RequestParam(value = "qunarName", required = false, defaultValue = "") String qunarName
            , @RequestParam(value = "uname", required = false, defaultValue = "") String uname
            , HttpServletRequest request
    ) {
        Long lSeatId = 0l;
        try {
            lSeatId = Long.valueOf(seatId);

        } catch (Exception e) {
            logger.error("updateSeatSession error", e);
        }
        return doSession(lSeatId, qunarName, uname);
    }



    @RequestMapping("/getBelongSuppliers.json")
    @ResponseBody
    public JsonData getBelongSuppliers(@RequestParam(value = "qunarName") String qunarName,
                                       @RequestParam(value = "busiType", required = false) Integer businessId) {
        if (Strings.isNullOrEmpty(qunarName)) {
            return JsonData.error("用户名不能为空");
        }
        try {
            List<SupplierVO> supplierVOs;
            if (businessId == null) {
                supplierVOs = supplierService.getSupplierBySeatQName(qunarName);
            } else {
                BusinessEnum businessEnum = BusinessEnum.of(businessId);
                if (businessEnum == null || businessEnum == BusinessEnum.EMPTY) {
                    return JsonData.error("业务线id：" + businessId + "不存在");
                }
                supplierVOs = supplierService.getSupplierBySeatQName(qunarName, businessId);
            }
            return JsonData.success(supplierVOs);
        } catch (Exception e) {
            log.error("查询客服所属供应商信息出错,qunarName:{}, busiId:{}", qunarName, businessId);
        }
        return JsonData.error("系统错误");
    }

    @RequestMapping(value = "oneBySupplier.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> getOneBySupplier(
            @RequestParam(value = "shopid") String shopid,
            @RequestParam(value = "qunarName") String userName,
            HttpServletRequest request
    ){
        long startTime = System.currentTimeMillis();
        long spanTime = System.currentTimeMillis() - startTime;

        if (Strings.isNullOrEmpty(shopid) || Strings.isNullOrEmpty(userName))
            return  JsonResultUtil.buildFailedJsonResult("参数错误");

        String requstStr =  request.getQueryString();

        SeatsResultVO<SeatWithStateVO> svo = seatService.assignedOneSeat(shopid,userName);

        if (null == svo){
            spanTime = System.currentTimeMillis() - startTime;
            logger.info("one2.qunar接口getOneSeat分配 失败 ! input {},span : {}",
                    requstStr
                    ,spanTime);
            return JsonResultUtil.buildFailedJsonResult("没有坐席");
        }

        String str = JacksonUtil.obj2String(svo);

        spanTime = System.currentTimeMillis() - startTime;
        logger.info("one2.qunar接口getOneSeat分配结果 ! input {}, result: {},span : {}",
                requstStr,
                str.replace("\r\n","")
                ,spanTime);
        svo.setTime(spanTime);

        return JsonResultUtil.buildSucceedJsonResult(svo);
    }

    @RequestMapping(value = "transferReadSeat.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> transferReadSeat (
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "fromShopId") String fromShopId,
            @RequestParam(value = "fromSeatName") String fromSeatName,
            @RequestParam(value = "toSeatName") String toSeatName,
            @RequestParam(value = "toSeatShopId",required = false) String toSeatShopId,
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();


        seatService.transferReadSeat(userName,fromShopId,fromSeatName,toSeatName);
        long spanTime = System.currentTimeMillis() - startTime;
        Map<String,Object> retInfo = Maps.newHashMap();
        retInfo.put("time",spanTime);

        String requstStr =  request.getQueryString();
        logger.info("transferReadSeat result ! input {}, span : {}",
                requstStr,spanTime);

        return JsonResultUtil.buildSucceedJsonResult(retInfo);
    }
}
