package com.qunar.qchat.admin.controller;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.service.seat.SeatNewService;
import com.qunar.qchat.admin.service.third.INoticeService;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.consult.entity.QtQueueKey;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-5-12.
 */
@Slf4j
@Controller
@RequestMapping(value = "/notice")
public class NoticeController {

    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class);

    @Resource
    private INoticeService noticeService;
    @Resource
    private ISupplierService supplierService;
    @Resource
    private SeatNewService seatNewService;
    @Resource
    private IRobotService robotService;

    private static final Function<String, Long> str2Long = new Function<String, Long>() {
        @Override
        public Long apply(String s) {
            return Long.valueOf(s);
        }
    };

    @RequestMapping(value = "/sayHello.json")
    @ResponseBody
    public JsonData sayHello(@RequestParam(value = "seatQName", required = false) String seatQName,
                             @RequestParam(value = "userQName") String userQName,
                             @RequestParam(value = "line", required = false) String line,
                             @RequestParam(value = "virtualId", required = false) String shopId,
                             @RequestParam(value = "seatId", required = false) String seatId,
                             @RequestParam(value = "seatHost", required = false) String seatHost) {
        // sayHello 接口临时添加分配逻辑,后续下掉
        distribute(userQName, shopId);
        return JsonData.success();

//        if (Strings.isNullOrEmpty(line)) {
//            return JsonData.error("line为业务线字段，不能为空");
//        }
//
//        if (Strings.isNullOrEmpty(seatQName) || Strings.isNullOrEmpty(userQName)) {
//            return JsonData.error("from或to不能为空");
//        }
//
//        if ("flight".equalsIgnoreCase(line) && "undefined".equalsIgnoreCase(shopId)) {
//            return JsonData.success("已被拦截");
//        }
//
//        Long lSeatId = null;
//        try {
//            lSeatId = Long.valueOf(seatId);
//        } catch (Exception e) {
//            log.info("调用sayHello 参数异常 接口，seatQName：{}，userQName：{}，virtualId:{},seatId：{}", seatQName, userQName, shopId, seatId);
//        }
//        log.info("调用sayHello接口，seatQName：{}，userQName：{}，virtualId:{},seatId：{}", seatQName, userQName, shopId, seatId);
//        BusinessEnum bEnum = BusinessEnum.ofByEnName(line);
//        if (bEnum == null) {
//            return JsonData.error("业务线错误");
//        }
//        try {
//            // 不是客服id，判断是否是机器人id
//            if (!seatNewService.isSeat(seatQName, seatHost)) {
//                Robot robot = null;
//                try {
//                    robot = robotService.getRobotByBusiness(bEnum);
//                    if (robot == null || !robot.toSeatWithoutSupplierId().getQunarName().equalsIgnoreCase(seatQName))
//                        return JsonData.error(seatQName + "不是机器人且不是客服");
//                    else {
//                        // 这里发送机器人的sayrobothello，比普通的sayhello多个标记字段
//
//                        boolean sayHello = noticeService.sayRobotHello(seatQName, userQName, shopId, lSeatId, seatHost, bEnum);
//                        QMonitor.recordOne(QMonitorConstant.SAY_HELLO_FROM_SEAT);
//                        return sayHello ? JsonData.success() : JsonData.error();
//                    }
//                } catch (Exception e) {
//                    log.error("robotService parse 错误，,robot {}, seatQName:{},userQName:{}", robot, seatQName, userQName, e);
//                    return JsonData.error(seatQName + "客服解析失败！");
//                }
//            } else {
//                log.info("调用sayHello接口 这是坐席，seatQName：{}，userQName：{}，virtualId:{},seatId：{}", seatQName, userQName, shopId, seatId);
//                boolean sayHello = noticeService.sayHello(seatQName, userQName, shopId, lSeatId, seatHost, bEnum);
//                QMonitor.recordOne(QMonitorConstant.SAY_HELLO_FROM_SEAT);
//                return sayHello ? JsonData.success() : JsonData.error();
//            }
//
//        } catch (Exception e) {
//            log.error("发送欢迎语错误,seatQName:{},userQName:{}", seatQName, userQName, e);
//            log.info("调用sayHello接口 异常，seatQName：{}，userQName：{}", seatQName, userQName);
//        }
//        return JsonData.error("系统错误");
    }

    @RequestMapping(value = "/save.json")
    @ResponseBody
    public JsonData save(@RequestBody List<Map> infos) {
        if (CollectionUtils.isEmpty(infos)) {
            return JsonData.error("参数错误！");
        }
        try {
            List<Supplier> suppliers = Lists.newArrayList();
            for (Map info : infos) {
                Long supplierId = MapUtils.getLong(info, "supplierId", null);
                String welcomes = MapUtils.getString(info, "welcomes", "");
                String noServiceWelcomes = MapUtils.getString(info, "noServiceWelcomes", "");
                if (supplierId == null || supplierId <= 0) {
                    continue;
                }
                if (Strings.isNullOrEmpty(welcomes) && StringUtils.isEmpty(noServiceWelcomes)) {
                    continue;
                }
                Supplier supplier = new Supplier();
                supplier.setId(supplierId);
                supplier.setWelcomes(welcomes);
                supplier.setNoServiceWelcomes(noServiceWelcomes);
                suppliers.add(supplier);
            }
            if (CollectionUtils.isEmpty(suppliers)) {
                return JsonData.error("参数错误");
            }
            boolean result = supplierService.updateWelcomes(suppliers);
            if (result) {
                return JsonData.success("更新成功！");
            } else {
                return JsonData.error("更新出错");
            }
        } catch (Exception e) {
            log.error("更新供应商欢迎语出错,param:{}", JacksonUtils.obj2String(infos), e);
        }
        return JsonData.error("系统错误");
    }

    @RequestMapping(value = "/welcomes/list.json")
    @ResponseBody
    public JsonData list(String supplierIds) {
        if (Strings.isNullOrEmpty(supplierIds)) {
            return JsonData.error("供应商id不能为空");
        }
        List<String> supplierIdsStr = Splitter.on(",").omitEmptyStrings().splitToList(supplierIds);
        try {
            List<Map> result = supplierService.querySupplierWelcomes(Lists.transform(supplierIdsStr, str2Long));
            return JsonData.success(result);
        } catch (Exception e) {
            log.error("获取供应商欢迎语出错,supplierIds:{}", supplierIds, e);
        }
        return JsonData.error("系统错误");
    }

    private void distribute(String userQName, String shopId) {
        // 之前这个接口没有触发分配和发送欢迎语的逻辑，调一下分配做兼容
        String qunarName = userQName;
        if (StringUtils.isEmpty(qunarName) || StringUtils.isEmpty(shopId))
            return;
        if (!qunarName.contains("@")) {
            qunarName = String.format("%s@%s", qunarName, QChatConstant.DEFAULT_HOST);
        }
        if (shopId.contains("@")) {
            shopId = JID.parseAsJID(shopId).getNode();
        }
        if (shopId.startsWith("shop_")) {
            shopId = shopId.replace("shop_", "");
        }
        JID parseAsJID = JID.parseAsJID(qunarName);
        long supplierid = StringUtils.isNumeric(shopId) ? Long.parseLong(shopId) : 0;
        String productId = null;

        HashSet<QtQueueKey> keys = QtQueueKey.parseFromRedisToHashSet(String.format("predistributionMapping:%s", parseAsJID.toBareJID()));
        if (CollectionUtils.isNotEmpty(keys)) {
            for (QtQueueKey qtQueueKey : keys) {
                if (qtQueueKey.getShopId() == supplierid) {
                    productId = qtQueueKey.getProductId();
                    break;
                }
            }
        }

        if (supplierid == 0 || productId == null)
            return;
        logger.info("SeatAPIController distribute qunarName:{} supplierid:{} productId:{}", qunarName, supplierid, productId);
        if ("12345678".equalsIgnoreCase(productId)) {
           // QtQueueManager.getInstance().judgmentOrRedistribution(parseAsJID, supplierid, productId, QChatConstant.DEFAULT_HOST, false, false);
        }
    }
}
