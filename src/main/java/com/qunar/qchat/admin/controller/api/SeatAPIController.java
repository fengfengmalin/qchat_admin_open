package com.qunar.qchat.admin.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.annotation.RecordAccessLog;
import com.qunar.qchat.admin.common.ApplicationContextHelper;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.Config;
import com.qunar.qchat.admin.controller.BaseController;
import com.qunar.qchat.admin.controller.seatselect.ISeatSelectorEvents;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.controller.seatselect.impl.SeatSelectorBaseNoticeEvents;
import com.qunar.qchat.admin.controller.seatselect.impl.WhichSeatOnDutySelector;
import com.qunar.qchat.admin.dao.ISessionDao;
import com.qunar.qchat.admin.dao.ISupplierDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISessionV2Service;
import com.qunar.qchat.admin.service.third.IProductService;
import com.qunar.qchat.admin.service.util.SeatUtil;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.consult.entity.QtSessionItem;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.service.CsrService;
import com.qunar.qtalk.ss.sift.service.ShopService;
import com.qunar.qtalk.ss.sift.service.SiftStrategyService;
import com.qunar.qtalk.ss.utils.JID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
@Controller
@RequestMapping("/api/seat")
public class SeatAPIController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SeatAPIController.class);

    @Resource(name = "seatService")
    private ISeatService seatService;

    @Resource(name = "sessionV2Service")
    protected ISessionV2Service sessionV2Service;

    @Resource(name = "sessionDao")
    private ISessionDao sessionDao;

    @Autowired
    IProductService productService;

//    @Resource
//    private INoticeService noticeService;

    @Autowired
    private ISupplierDao supplierDao;
    @Autowired
    public SiftStrategyService siftStrategyService;
    @Autowired
    ShopService shopService;
    @Autowired
    CsrService csrService;


    private static final String QCHAT_HOST = "@ejabhost2";
    private static final String QTALK_HOST = "@ejabhost1";


    @RequestMapping(value = "/list.do")
    public ModelAndView getSeatListPage() {
        return new ModelAndView("/page/iframe/GMList");
    }

    @RequestMapping(value = "/ordergm.do")
    public ModelAndView getOneSeatOrderPage() {
        return new ModelAndView("/page/iframe/orderGMButton");
    }

    @RequestMapping(value = "/productgm.do")
    public ModelAndView getOneSeatProductPage() {
        return new ModelAndView("/page/iframe/productGMButton");
    }

    @RequestMapping(value = "/orderdetailgm.do")
    public ModelAndView getOneSeatOrderDetailPage() {
        return new ModelAndView("/page/iframe/orderDetailButton");
    }

    @RequestMapping(value = "/touch.do")
    public ModelAndView getTouchButton() {
        return new ModelAndView("/page/iframe/touchButton");
    }


    @RequestMapping(value = "/batchlist.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> getSeatBatchList(
            @RequestParam(value = "busiSupplierIds") String busiSupplierIds
            , @RequestParam(value = "businessId") int businessId
            , @RequestParam(value = "pId", required = false) String pid
    ) {
        long startTime = System.currentTimeMillis();
        BusinessEnum businessEnum = BusinessEnum.of(businessId);
        if (businessEnum == null) {
            return JsonResultUtil.buildFailedJsonResult("该业务线不存在");
        }

        List<String> busiSupplierId = Splitter.on(',').omitEmptyStrings().splitToList(busiSupplierIds);
        if (CollectionUtil.isEmpty(busiSupplierId)) {
            return JsonResultUtil.buildFailedJsonResult("该业务线店铺不存在");
        }
        Map<String, List<GroupAndSeatVO>> result = seatService.getBatchBusiSupplierSeatsWithOnlineStatus(busiSupplierId, businessEnum, pid);
        if (CollectionUtil.isNotEmpty(result)) {
            return JsonResultUtil.buildSucceedJsonResult(result);
        }

        return JsonResultUtil.buildFailedJsonResult("未查询到任何信息");
    }

    @RequestMapping(value = "/list.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> getSeatList(@RequestParam(value = "busiSupplierId") String busiSupplierId
            , @RequestParam(value = "businessId") int businessId
            , @RequestParam(value = "pId", required = false) String pid
            , @RequestParam(value = "host", required = false) String host
            , HttpServletRequest request
    ) {
        host = StringUtils.isNotEmpty(host) ? host : QChatConstant.DEFAULT_HOST;
        {
            String qunarName = AuthorityUtil.getThirdPartyUserName(request);
            if (StringUtils.isNotEmpty(qunarName)) {
                if (!qunarName.contains("@")) {
                    qunarName = String.format("%s@%s", qunarName, host);
                }
                JID userId = JID.parseAsJID(qunarName);
                QtQueueManager.getInstance().judgmentForOne(busiSupplierId, businessId, pid, userId);
            }
        }

        long startTime = System.currentTimeMillis();
        BusinessEnum businessEnum = BusinessEnum.of(businessId);
        if (businessEnum == null) {
            return JsonResultUtil.buildFailedJsonResult("该业务线不存在");
        }

        List<SeatWithStateVO> seatList = seatService.onlineSeats(busiSupplierId,
                businessEnum, pid, host);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("未查询到任何信息");
        }

        //SeatWithStateVO svoData = svo.getData();

        List<GroupAndSeatVO> groupAndSeatVOList = new ArrayList<>();
        GroupAndSeatVO groupAndSeatVO = new GroupAndSeatVO();
        groupAndSeatVO.setSeatWithStateVOList(seatList);
        // TODO 后续改为客服组
        groupAndSeatVO.setGroupId(seatList.get(0).getSupplier().getId());
        groupAndSeatVO.setGroupName(seatList.get(0).getSupplier().getName());

        groupAndSeatVOList.add(groupAndSeatVO);

        SeatsResultVO<List<GroupAndSeatVO>> result = new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), groupAndSeatVOList);
        return JsonResultUtil.buildSucceedJsonResult(result);

//        List<GroupAndSeatVO> groupAndSeatVOList = seatService.getSeatWithOnlineStateList(busiSupplierId, businessEnum, pid);
//        if (CollectionUtil.isNotEmpty(groupAndSeatVOList)) {
//            SeatsResultVO<List<GroupAndSeatVO>> result = new SeatsResultVO<>(Config.SEAT_POLLING_TIME, businessEnum.getEnName(), groupAndSeatVOList);
//            QMonitor.recordOne(QMonitorConstant.API_SEAT_LIST_INVOKE, System.currentTimeMillis() - startTime);
//            return JsonResultUtil.buildSucceedJsonResult(result);
//        }
//        QMonitor.recordOne(QMonitorConstant.API_SEAT_LIST_INVOKE, System.currentTimeMillis() - startTime);
//        return JsonResultUtil.buildFailedJsonResult("未查询到任何信息");
    }


    @RequestMapping(value = "/one.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> getOne2Seat(
            @RequestParam(value = "busiSupplierId") String busiSupplierId
            , @RequestParam(value = "businessId") Integer businessId
            , @RequestParam(value = "qunarName", required = false) String qunarName
            , @RequestParam(value = "pId", required = false) String pid
            , @RequestParam(value = "groupType", required = false) String groupType
            , @RequestParam(value = "busiSessionId", required = false) String busiSessionId
            , @RequestParam(value = "host", required = false) String host
            , HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        long spanTime = System.currentTimeMillis() - startTime;


        host = StringUtils.isNotEmpty(host) ? host : QChatConstant.DEFAULT_HOST;
        {
            if (StringUtils.isNotEmpty(qunarName)) {
                if (!qunarName.contains("@")) {
                    qunarName = String.format("%s@%s", qunarName, host);
                }
                JID userId = JID.parseAsJID(qunarName);

                QtQueueManager.getInstance().judgmentForOne(busiSupplierId, businessId, pid, userId);
            }
        }

        BusinessEnum businessEnum = BusinessEnum.of(businessId);
        if (businessEnum == null) {
            logger.info("进入one.qunar接口中getOneSeat函数 该业务线不存在 pId:{},qunarName:{}", pid, qunarName);
            return JsonResultUtil.buildFailedJsonResult("该业务线不存在");
        }

        String requstStr = request.getQueryString();

/*        HttpSession session = request.getSession();
        session.setAttribute(SessionConstants.SysUser, loginUser);
        session.setMaxInactiveInterval(30*60); //设置session时长
        SessionUtils.setSession(session);*/
        host = StringUtils.isNotEmpty(host) ? host : QChatConstant.DEFAULT_HOST;

        SeatsResultVO<SeatWithStateVO> svo = seatService.preAssignedOneSeat(busiSupplierId,
                businessEnum,
                qunarName,
                pid, host);

        if (null == svo) {
            logger.info("one2.qunar接口getOneSeat分配结果 ! input {},span : {}",
                    requstStr
                    , spanTime);
            return JsonResultUtil.buildFailedJsonResult("没有坐席");
        }

        String str = JacksonUtil.obj2String(svo);

        logger.info("one2.qunar接口getOneSeat分配结果 ! input {}, result: {},span : {}",
                requstStr,
                str.replace("\r\n", "")
                , spanTime);
        svo.setTime(spanTime);
        return JsonResultUtil.buildSucceedJsonResult(svo);
    }



    @RequestMapping(value = "/moreOne.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> moreOne(
            @RequestParam(value = "p") String p
    ) {
        long startTime = System.currentTimeMillis();
        List<SupplierRequestVO> suVOList = JacksonUtil.string2Obj(p, new TypeReference<List<SupplierRequestVO>>() {
        });
        List<SupplierAndSeatVO> suAndSeatList = seatService.getMoreSuSeatWithOnStList(suVOList);
        if (CollectionUtil.isEmpty(suAndSeatList)) {
            return JsonResultUtil.buildFailedJsonResult(BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getCode(), BusiResponseCodeEnum.FAIL_NOT_FOUND_RESULT.getMsg());
        }

        Map<String, Object> result = buildOutputResult(suVOList, suAndSeatList);
        return JsonResultUtil.buildSucceedJsonResult(BusiResponseCodeEnum.SUCCESS.getCode(), BusiResponseCodeEnum.SUCCESS.getMsg(), result);
    }

    @RequestMapping("/getAllSeatNames.qunar")
    @ResponseBody
    public JsonResultVO<?> getAllSeatNames(@RequestParam(value = "supplierIds", required = false, defaultValue = "") String supplierIdsStr,
                                           @RequestParam(value = "busiType") Integer busiType) {
        if (Strings.isNullOrEmpty(supplierIdsStr) || busiType == null) {
            return JsonResultUtil.buildFailedJsonResult("参数错误,supplierIds=" + supplierIdsStr + "&busiType=" + busiType);
        }
        return getAllSeatNamesBase(supplierIdsStr, busiType);
    }

    @RequestMapping("/startSession.qunar")
    @ResponseBody
    public JsonResultVO<?> updateSeatSession(
            @RequestParam(value = "seatId", required = false, defaultValue = "0") String seatId
            , @RequestParam(value = "qunarName", required = false, defaultValue = "") String qunarName
            , @RequestParam(value = "uname", required = false, defaultValue = "") String uname
            , HttpServletRequest request
    ) {
        Long lSeatId = 0l;
        try {
            lSeatId = Long.valueOf(seatId);

        } catch (Exception e) {
            logger.debug("updateSeatSession error", e);
            return JsonResultUtil.buildFailedJsonResult("参数错误");
        }
        return doSession(lSeatId, qunarName, uname);
    }

    @RequestMapping("/closeSession.qunar")
    @ResponseBody
    public JsonResultVO<?> closeSession(
            @RequestParam(value = "userName") String userName
            , @RequestParam(value = "seatName") String seatName
            , @RequestParam(value = "virtualname") String virtualname
            , @RequestParam(value = "host", required = false) String host
    ) {
        if (Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(virtualname) || Strings.isNullOrEmpty(seatName))
            return JsonResultUtil.buildFailedJsonResult("参数错误");
        host = StringUtils.isEmpty(host) ? QChatConstant.DEFAULT_HOST : host;
        {
            String customerName = userName;
            if (StringUtils.isNotEmpty(customerName) && !customerName.contains("@")) {
                customerName = String.format("%s@%s", customerName, host);
            }

            String shopString = virtualname;

            long shopId = 0;

            if (StringUtils.isNotEmpty(shopString) && shopString.startsWith("shop_")) {
                shopId = Shop.parseString(shopString);
//                shopId = Long.parseLong(shopString.replace("shop_", ""));
            } else {
                shopId = shopService.selectShopByBsiId(shopString);
            }

            logger.info("enter to close session. {} {} {}", customerName, seatName, shopId);
            if (StringUtils.isNotEmpty(customerName) && shopId != 0) {
                QtQueueManager.getInstance().closeSession(JID.parseAsJID(customerName), JID.parseAsJID(seatName), shopId);
            } else {
                logger.info("closesession failed. userName:{} seatName:{} virtualname:{}", userName, seatName, virtualname);
            }
        }
//        sessionV2Service.closeSession(userName, virtualname, seatName);

        return JsonResultUtil.buildSucceedJsonResult("操作成功");
    }


    private Map<String, Object> buildOutputResult(List<SupplierRequestVO> suVOList, List<SupplierAndSeatVO> suAndSeatList) {
        Map<String, SupplierAndSeatVO> ssMap = new HashMap<>();
        for (SupplierAndSeatVO ssVO : suAndSeatList) {
            String key = ssVO.getbSuId() + ssVO.getbType();
            if (StringUtils.isNotEmpty(ssVO.getpId())) {
                key = ssVO.getpId() + key;
            }
            ssMap.put(key, ssVO);
        }

        Map<String, Object> result = Maps.newHashMap();
        for (SupplierRequestVO srVO : suVOList) {
            Map<String, Object> obj = Maps.newHashMap();
            String key = srVO.getbSuId() + srVO.getbType();
            if (StringUtils.isNotEmpty(srVO.getpId())) {
                key = srVO.getpId() + key;
            }
            SupplierAndSeatVO sasVO = ssMap.get(key);

            String qName = null;
            int os = 0;
            long seatId = 0;
            String shopId = null;
            String sName = null;
            String logoUrl = null;
            if (sasVO != null && CollectionUtils.isNotEmpty(sasVO.getSeatWithStateVOList())) {
                SeatWithStateVO ssVO = sasVO.getSeatWithStateVOList().get(0);
                qName = ssVO.getSeat().getQunarName();
                seatId = ssVO.getSeat().getId();
                os = OnlineState.getOnlineStatePriority(ssVO.getOnlineState());
                shopId = sasVO.getShopId();
                sName = sasVO.getsName();
                logoUrl = sasVO.getLogoUrl();
            }
            Long supplierId = 0L;
            if (StringUtils.isNotEmpty(shopId)) {
                String replace = shopId.replace(Supplier.SHOPID_PREFIX, "");
                supplierId = Long.valueOf(StringUtils.isNotEmpty(replace) ? replace : "0");
            }
            obj.put("qName", qName);
            obj.put("sId", seatId);
            obj.put("seatId", seatId);
            obj.put("os", os);
            Supplier supplier = null;
            if (null != sasVO) {
                supplier = new Supplier();
                supplier.setId(supplierId);
                supplier.setbType(sasVO.getbType());
            }

            obj.put("switchOn", true);
            obj.put("shopId", shopId);
            obj.put("sName", sName);
            obj.put("logoUrl", logoUrl);

            result.put(key, obj);
        }
        return result;
    }

    @RequestMapping(value = "/onlineState.qunar")
    @ResponseBody
    public JsonResultVO<?> getOnlineState(
            @RequestParam(value = "qunarNames") String qunarNames,
            @RequestParam(value = "busiSupplierId", required = false) String busiSupplierId,
            @RequestParam(value = "businessId", required = false) Integer businessId
    ) {

        long startTime = System.currentTimeMillis();
        if (null == businessId) {
            businessId = 0;
        }

        List<String> qunarNamesList = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(qunarNames);
        Supplier supplier = null;
        BusinessEnum businessEnum = BusinessEnum.of(businessId);
        if (null != businessEnum) {
            supplier = supplierDao.getSupplier(businessId, busiSupplierId, 0);
        }


        List<SeatOnlineState> stateList = seatService.getSeatOnlineFixedStatie(null == supplier ? 0 : supplier.getId(), qunarNamesList);

        if (CollectionUtil.isEmpty(stateList)) {
            return JsonResultUtil.buildFailedJsonResult("未查询到任何数据");
        }
        return JsonResultUtil.buildSucceedJsonResult(stateList);
    }

    @RequestMapping("/getTouchQChatURL.qunar")
    @ResponseBody
    public JsonResultVO<?> getTouchQChatURL(@RequestParam(value = "supplierId") String busiSupplierId,
                                            @RequestParam(value = "busiType") int busiType,
                                            @RequestParam(value = "webUrl", required = false) String webUrl,
                                            @RequestParam(value = "productId", required = false, defaultValue = "0") String productId) {
        long startTime = System.currentTimeMillis();
        BusinessEnum businessEnum = BusinessEnum.of(busiType);
        if (businessEnum == null) {
            return JsonResultUtil.buildFailedJsonResult("该业务线不存在");
        }

        List<GroupAndSeatVO> groupAndSeatVOList = seatService.getSeatWithOnlineStateList(busiSupplierId, businessEnum, null);
        if (CollectionUtil.isEmpty(groupAndSeatVOList) || CollectionUtil.isEmpty(groupAndSeatVOList.get(0).getSeatWithStateVOList())) {
            return JsonResultUtil.buildFailedJsonResult("没有坐席");
        }

        SeatWithStateVO s = groupAndSeatVOList.get(0).getSeatWithStateVOList().get(0);
        String url = SeatUtil.getChatUrl(webUrl, productId, businessEnum, s.getSeat().getQunarName(), s.getSeat().getId());
        return JsonResultUtil.buildSucceedJsonResult("success", url);
    }

    @RequestMapping("/upSeatSeStatus.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> updateSeatServiceStatus(
            @RequestParam(value = "qName") String qunarName
            , @RequestParam(value = "st") Integer status
            , HttpServletRequest request
    ) {
        if (status == null || (status != 0 && OnlineState.of(status) == null)) {
            logger.info("服务状态不合法，status: {}", status);
            return JsonResultUtil.buildFailedJsonResult("服务状态不合法");
        }

        // 目前能设置为24小时在线&休息中 设置休息中不再接收消息 即offline
        if (status != OnlineState.getOnlineStatePriority(OnlineState.ONLINE)
                && status != OnlineState.getOnlineStatePriority(OnlineState.OFFLINE)) {
            status = 0;
        }
        String loginUserName = AuthorityUtil.getThirdPartyUserName(request);
        if (!loginUserName.equals(qunarName)) {
            logger.info("不能修改其他客服服务状态，登陆用户: {}，操作用户：{}", loginUserName, qunarName);
            return JsonResultUtil.buildFailedJsonResult("不能修改其他客服服务状态");
        }
        List<SeatVO> seatList = seatService.getSeatByQunarName(qunarName);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("客服不存在");
        }
        // String preString = "";
        StringBuilder sb = new StringBuilder();

        for (SeatVO s : seatList) {
            String preStatus = ServiceStatusEnum.getValue(s.getServiceStatus());
            s.setServiceStatus(status);
            seatService.updateSeat(s);
            sb.append("客服Id:").append(s.getId()).append(" 从:").append(preStatus).append(" 修改为:")
                    .append(ServiceStatusEnum.getValue(status)).append(" 修改商铺:").append(s.getSupplierName());

            logger.info("更新客服服务状态，qName:{}, content:{}", qunarName, sb.toString());
            LogUtil.doLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_SEAT, null, qunarName, SessionUtils.getUserName(),
                    sb.toString());
        }

        return JsonResultUtil.buildSucceedJsonResult("success", "更新客服服务状态成功");
    }

    @RequestMapping("/upSeatSeStatusWithSid.qunar")
    @ResponseBody
    @RecordAccessLog
    public JsonResultVO<?> updateSeatServiceStatusWithSid(
            @RequestParam(value = "qName") String qunarName,
            @RequestParam(value = "sid") Long sid,
            @RequestParam(value = "st") Integer status
            , HttpServletRequest request
    ) {
        if (status == null || (status != 0 && OnlineState.of(status) == null)) {
            logger.info("服务状态不合法，status: {}", status);
            return JsonResultUtil.buildFailedJsonResult("服务状态不合法");
        }

        // 目前能设置为24小时在线&休息中 设置休息中不再接收消息 即offline
        if (status != OnlineState.getOnlineStatePriority(OnlineState.ONLINE)
                && status != OnlineState.getOnlineStatePriority(OnlineState.OFFLINE)) {
            status = 0;
        }
        String loginUserName = AuthorityUtil.getThirdPartyUserName(request);
        if (Strings.isNullOrEmpty(loginUserName) || !loginUserName.equals(qunarName)) {
            logger.info("不能修改其他客服服务状态，登陆用户: {}，操作用户：{}", loginUserName, qunarName);
            //return JsonResultUtil.buildFailedJsonResult("不能修改其他客服服务状态");
        }

        List<SeatVO> seatList = seatService.getSeatByQunarName(qunarName);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("客服不存在");
        }
        StringBuilder sb = new StringBuilder();

        for (SeatVO s : seatList) {
            if (sid == s.getSupplierId()) {
                String preStatus = ServiceStatusEnum.getValue(s.getServiceStatus());
                s.setServiceStatus(status);
                seatService.updateSeat(s);
                if (s.getServiceStatus() == ServiceStatusEnum.SUPER_MODE.getKey()) {
                    QtQueueManager.getInstance().goOnline(sid);
                }
                sb.append("客服Id:").append(s.getId()).append(" 从:").append(preStatus).append(" 修改为:")
                        .append(ServiceStatusEnum.getValue(status)).append(" 修改商铺:").append(s.getSupplierName());

                logger.info("更新客服服务状态，qName:{}, content:{}", qunarName, sb.toString());
                LogUtil.doLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_SEAT, null, qunarName, SessionUtils.getUserName(),
                        sb.toString());
            }
        }


        return JsonResultUtil.buildSucceedJsonResult("success", "更新客服服务状态成功");
    }


    @RequestMapping("/getSeatSeStatus.qunar")
    @ResponseBody
    public JsonResultVO<?> getSeatServiceStatus(
            @RequestParam(value = "qName") String qunarName
    ) {
        List<SeatVO> seatList = seatService.getSeatByQunarName(qunarName);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("客服不存在");
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("st", String.valueOf(seatList.get(0).getServiceStatus()));
        return JsonResultUtil.buildSucceedJsonResult("success", map);
    }

    @RequestMapping("/getSeatSeStatusWithSid.qunar")
    @ResponseBody
    public JsonResultVO<?> getSeatServiceStatusWithSid(
            @RequestParam(value = "qName") String qunarName
    ) {
        List<SeatVO> seatList = seatService.getSeatByQunarName(qunarName);
        if (CollectionUtils.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("客服不存在");
        }

        List<Map<String, Object>> stBySupllier = Lists.newArrayList();

        for (SeatVO seatVO : seatList) {
            Map<String, Object> objectMap = Maps.newHashMap();
            objectMap.put("sid", seatVO.getSupplierId());
            objectMap.put("sname", seatVO.getSupplierName());
            objectMap.put("st", seatVO.getServiceStatus());
            stBySupllier.add(objectMap);
        }
        return JsonResultUtil.buildSucceedJsonResult("success", stBySupllier);
    }


    @RequestMapping("/reloadSeatSeStatus.qunar")
    @ResponseBody
    public JsonResultVO<?> reloadSeatSeStatus() {
        return JsonResultUtil.buildSucceedJsonResult("success", "重载客服服务状态成功");
    }

    // 检查参数
//    private static boolean checkNoteArg(ProductNoteArgs args) {
//        if (args == null) {
//            return false;
//        }
//        if (Strings.isNullOrEmpty(args.getUserQName()) || Strings.isNullOrEmpty(args.getSeatQName())) {
//            return false;
//        }
//        if (Strings.isNullOrEmpty(args.getUserHost())
//                || !StringUtils.equals(args.getUserHost(), QChatConstant.QTALK_HOST)) {
//            args.setUserQName(args.getUserQName() + QChatConstant.QCHAT_HOST_POSTFIX);
//        } else {
//            args.setUserQName(args.getUserQName() + QChatConstant.QTALK_DOMAIN_POSTFIX);
//        }
//        // 默认虚拟id的host和客服的一致
//        if (Strings.isNullOrEmpty(args.getSeatHost())
//                || !StringUtils.equals(args.getSeatHost(), QChatConstant.QTALK_HOST)) {
//            args.setSeatQName(args.getSeatQName() + QChatConstant.QCHAT_HOST_POSTFIX);
//            if (!Strings.isNullOrEmpty(args.getVirtualId())) {
//                args.setVirtualId(args.getVirtualId() + QChatConstant.QCHAT_HOST_POSTFIX);
//            }
//        } else {
//            args.setSeatQName(args.getSeatQName() + QChatConstant.QTALK_DOMAIN_POSTFIX);
//            if (!Strings.isNullOrEmpty(args.getVirtualId())) {
//                args.setVirtualId(args.getVirtualId() + QChatConstant.QTALK_DOMAIN_POSTFIX);
//            }
//        }
//        return true;
//    }

    @RequestMapping("/judgmentOrRedistributionEx.json")
    @ResponseBody
    @RecordAccessLog
    public JsonData judgementOrRedistributionEx(@RequestParam(value = "shopId") String shopId,
                                                @RequestParam(value = "pdtId", required = false) String pdtId,
                                                @RequestParam(value = "seatQName", required = false) String seatQName,
                                                @RequestParam(value = "userQName", required = false) String userQName,
                                                @RequestParam(value = "tEnId", required = false) String tEnId,
                                                @RequestParam(value = "tuId", required = false) String tuId,
                                                @RequestParam(value = "bType", required = false) Integer bType,
                                                @RequestParam(value = "line", required = false) String line,
                                                @RequestParam(value = "source", required = false) String source,
                                                @RequestParam(value = "host", required = false) String host,
                                                @RequestParam(value = "noteArgs", required = false) String noteArgs, HttpServletRequest request) {
        try {
            logger.info("judgementOrRedistributionEx函数 judgmentOrRedistributionEx.json  shopid {}, seatQName:{},userQName:{}", shopId, seatQName, userQName);;

            String supplierId = shopId.replace(Supplier.SHOPID_PREFIX, "").replace(QCHAT_HOST, "").replace(QTALK_HOST,
                    "");
            if (!shopId.startsWith(Supplier.SHOPID_PREFIX) || Long.valueOf(supplierId) <= 0) {
                logger.info("judgementOrRedistributionEx函数 judgmentOrRedistributionEx.json  shopid {}, 店铺id错误", shopId);
                return JsonData.error(shopId + "，店铺id错误");
            }
            long lSuppllierId = 0;
            try {
                lSuppllierId = Long.valueOf(supplierId);
            } catch (Exception e) {
                logger.info("judgementOrRedistribution函数 shopid格式错误 shopId:{},seatQName:{},userQName:{}", shopId, seatQName, userQName);
                return JsonData.error(shopId + "，店铺id 格式错误");
            }

            host = StringUtils.isNotEmpty(host) ? host : QChatConstant.DEFAULT_HOST;
            JID fromJid;

            if (userQName.contains("@")) {
                fromJid = JID.parseAsJID(userQName);
            } else {
                fromJid = JID.parseAsJID(String.format("%s@%s", userQName, host));
            }

            pdtId = StringUtils.isEmpty(pdtId) ? QtSessionItem.DEFAULT_PRODUCTID : pdtId;

            return seatService.redistributionEx(lSuppllierId, fromJid, pdtId, seatQName, host);
        } catch (Exception e) {
            logger.error("/judgmentOrRedistributionEx.json接口出错,shopId:{},seatQName:{},userQName:{}", shopId, seatQName,
                    userQName, e);
        }
        return JsonData.error("未知错误");
    }

    @RequestMapping("/judgmentOrRedistribution.json")
    @ResponseBody
    @RecordAccessLog
    public JsonData judgementOrRedistribution(
            @RequestParam(value = "shopId") String shopId,
            @RequestParam(value = "seatQName", required = false) String seatQName,
            @RequestParam(value = "userQName", required = false) String userQName,
            @RequestParam(value = "pdtId", required = false) String pdtId,
            @RequestParam(value = "host", required = false) String host,
            HttpServletRequest request) {
        // 兼容客户端的问题请求，如果含有noteArgs,的请求倒入到judgmentOrRedistributionEx接口中
        logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx.json  shopid:{}, seatQName:{},userQName:{}", shopId, seatQName, userQName);
        Map<String, String[]> parameters = request.getParameterMap();

        if (parameters.containsKey("noteArgs")) {
            logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx.json contain noteArgs params 调用judgementOrRedistributionEx函数 seatQName:{},userQName:{}", seatQName, userQName);
            return judgementOrRedistributionEx(
                    request.getParameter("shopId"),
                    request.getParameter("pdtId"),
                    request.getParameter("seatQName"),
                    request.getParameter("userQName"),
                    request.getParameter("tEnId"),
                    request.getParameter("tuId"),
                    null == request.getParameter("bType") ? null : Integer.valueOf(request.getParameter("bType")),
                    request.getParameter("line"),
                    request.getParameter("source"),
                    request.getParameter("noteArgs"),
                    host,
                    request
            );
        }
        String fixUserName = "";
        String rawUserName = userQName;

        if (TextUtils.isEmpty(userQName)) {
            // 兼容逻辑
            StringBuilder builder = new StringBuilder();
            builder
                    .append("realip")
                    .append("=")
                    .append(IPUtil.getUserIPString(request))
                    .append(";");
            builder
                    .append("user-agent")
                    .append("=")
                    .append(request.getHeader("user-agent"))
                    .append(";");

            Cookie[] cookies = request.getCookies();
            if (null != cookies) {
                for (Cookie cookie : cookies) {
                    builder
                            .append(cookie.getName())
                            .append("=")
                            .append(cookie.getValue())
                            .append(";");

                    String cookiePrefix = "U.";
                    if ("_q".equalsIgnoreCase(cookie.getName())) {
                        String cookieV = cookie.getValue();
                        if (!TextUtils.isEmpty(cookieV) && 0 == cookieV.indexOf(cookiePrefix)) {

                            userQName = cookieV.substring(cookiePrefix.length());
                            fixUserName = userQName;
                            builder
                                    .append("fixedusername")
                                    .append("=")
                                    .append(userQName)
                                    .append(";");
                        }
                    }
                }
            }

            if (TextUtils.isEmpty(userQName)) {
                logger.info("judgementOrRedistribution函数 builder:{}, seatQName:{},userQName:{},shopid: {}", builder.toString(), seatQName, userQName, shopId);
            }
        }


        try {
            if (StringUtils.isEmpty(shopId)) {
                return JsonData.error("店铺id错误");
            }
            String supplierId = shopId.replace(Supplier.SHOPID_PREFIX, "").replace(QCHAT_HOST, "").replace(QTALK_HOST,
                    "");
            if (!shopId.startsWith(Supplier.SHOPID_PREFIX) || Long.valueOf(supplierId) <= 0) {
                logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx.json 店铺id错误 shopId:{},seatQName:{},userQName:{}", shopId, seatQName, userQName);
                return JsonData.error(shopId + "，店铺id错误");
            }

            long lSuppllierId = 0;
            try {
                lSuppllierId = Long.valueOf(supplierId);
            } catch (Exception e) {
                logger.info("judgementOrRedistribution函数 shopid格式错误 shopId:{},seatQName:{},userQName:{}", shopId, seatQName, userQName);
                return JsonData.error(shopId + "，店铺id 格式错误");
            }
            ;


            SeatsResultVO<SeatWithStateVO> resultVO = null;
//            if (validSupplierId(lSuppllierId) || Config.canSessionTranferV2(lSuppllierId)) {

                WhichSeatOnDutySelector ss = ApplicationContextHelper.popBean("whichSeatOnDutySelector", WhichSeatOnDutySelector.class);

                if (null != ss) {
                    ISeatSelectorEvents events = ApplicationContextHelper.popBean("seatSelectorBaseNoticeEvents", SeatSelectorBaseNoticeEvents.class);
                    SelectorConfigration configration =
                            new SelectorConfigration.Builder()
                                    .businessEnum(null)
                                    .busiSupplierId(null)
                                    .groupType(null)
                                    .productID(null)
                                    .qunarName(userQName)
                                    .supplierId(lSuppllierId)
                                    .lastSeatName(seatQName)
                                    .events(events)
                                    .build();

                    // 先分出一个来在说
                    resultVO = ss.getOneSeat(configration);
                }

//            } else {
//                logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx.json supplierId:{},seatQName:{},userQName:{}", supplierId, seatQName, userQName);
//                resultVO = seatService.judgeOrRedistribute(lSuppllierId, seatQName, userQName);
//
//                if (null != resultVO && null != resultVO.getData() && null != resultVO.getData().getSeat() && null != resultVO.getData().getSeat().getQunarName()
//                        && null != resultVO.getData().getSeat().getCustomerName()) {
//                    logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx. 分配客服： seatId :{},customerId:{},seatQName:{},userQName:{}",
//                            resultVO.getData().getSeat().getQunarName(), resultVO.getData().getSeat().getCustomerName(), seatQName, userQName);
//                } else {
//                    logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx. resultV0  某个值位空 seatQName:{},userQName:{}", seatQName, userQName);
//                }
//            }

            if (null == resultVO) {
                logger.error("judgementOrRedistribution分配异常 supplierId:{},seatQName:{},userQName:{},rawUsername:{},fixedUserName:{}", supplierId, seatQName, userQName, rawUserName, fixUserName);

                Supplier supplier = supplierDao.getSupplier(BusinessEnum.EMPTY.getId(), "", lSuppllierId);

                if (supplier == null) {
                    return JsonData.error("找不到对应的客服信息");
                } else {
                    SeatWithStateVO seatWithStateVO = new SeatWithStateVO();
                    seatWithStateVO.setSupplier(supplier);
                    // resultVO.setData(seatWithStateVO);
                    return JsonData.success(seatWithStateVO);
                }
                //return JsonData.error("找不到对应的客服信息");
            }
            return JsonData.success(resultVO.getData());

        } catch (Exception e) {
            logger.error("/judgmentOrRedistributionEx.json接口出错,sh" +
                            "opId:{},seatQName:{},userQName:{}", shopId, seatQName,
                    userQName, e);
        }
        logger.info("judgementOrRedistribution函数 judgmentOrRedistributionEx.系统错误 seatQName:{},userQName:{}", seatQName, userQName);
        return JsonData.error("系统错误");
    }


    @RequestMapping("/transferreply.qunar")
    @ResponseBody
    public JsonResultVO<?> transferReply(
            @RequestParam(value = "user") String user,
            @RequestParam(value = "shopid") String shopid,
            @RequestParam(value = "oldseat") String oldseat,
            @RequestParam(value = "newseat") String newseat,
            @RequestParam(value = "whoami") String whoami,
            HttpServletRequest request) {
        Map<String, String> retmap = Maps.newHashMap();
        retmap.put("user", user);
        retmap.put("shopid", shopid);
        retmap.put("oldseat", oldseat);
        retmap.put("newseat", newseat);
        retmap.put("whoami", whoami);
        return JsonResultUtil.buildSucceedJsonResult(retmap);
    }



//    private void safePutObject(Map<String, Object> maps, String key, Object value) {
//        if (maps != null) {
//            if (key != null && value != null)
//                maps.put(key, value);
//        }
//    }

//    private boolean validSupplierId(Long supplierId) {
//        List<Integer> businessIdList = Config.getBusinessIdList();
//        return CollectionUtils.isNotEmpty(businessIdList) && shopService.selectShopsByBusiIds(businessIdList).contains(String.valueOf(supplierId));
//    }


}
