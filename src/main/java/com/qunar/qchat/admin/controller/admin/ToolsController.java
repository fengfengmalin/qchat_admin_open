package com.qunar.qchat.admin.controller.admin;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.dao.impl.AdminToolDaoImpl;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.LogEntity;
import com.qunar.qchat.admin.model.SeatSortStrategyEnum;
import com.qunar.qchat.admin.model.ServiceStatusEnum;
import com.qunar.qchat.admin.service.LogOperationService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.DateUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.SeatOnlineState;
import com.qunar.qchat.admin.vo.conf.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by hongwu.yang on 2016/12/1.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class ToolsController {

   // private Logger logger = LoggerFactory.getLogger(ToolsController.class);

    @Autowired
    private AdminToolDaoImpl adminToolDao;
//    @Resource
//    private IQChatService qChatService;
    @Resource
    private LogOperationService logOperationService;

    @RequestMapping("querySeat.json")
    @ResponseBody
    public JsonResultVO querySeat(
            @RequestParam(value = "qunarName", required = false) String qunarName,
            HttpServletRequest request, HttpServletResponse response) {

        List<ASeatVO> seatList = adminToolDao.getSeatList(qunarName);
        if (CollectionUtil.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("没有查到结果");
        }

        SeatOnlineState sos = getSeatOnlineState(qunarName);

        for (ASeatVO s : seatList) {
            s.setCreateTimeText(DateUtil.getTimestampString(s.getCreateTime()));
            if (s.getLastUpdateTime() != null) {
                s.setLastUpdateTimeText(DateUtil.getTimestampString(s.getLastUpdateTime()));

            }
            if (sos != null) {
                s.setLoginStatus(sos.getOnlineState().name());
                s.setTerminalType(sos.getResource());
            }

            BusinessEnum bEnum = BusinessEnum.of(s.getBType());
            if (bEnum != null) {
                s.setBuTypeText(bEnum.getName());
            }
            
            s.setServiceStatusStr(ServiceStatusEnum.getValue(s.getServiceStatus()));
            s.setStatusStr(s.getStatus() == 0 ? "删除" : "有效");
        }

        return JsonResultUtil.buildSucceedJsonResult(seatList);
    }

    @RequestMapping("querySeatGroup.json")
    @ResponseBody
    public JsonResultVO querySeatGroup(
            @RequestParam(value = "qunarName", required = false) String qunarName,
            HttpServletRequest request, HttpServletResponse response) {

        List<ASeatGroupVO> seatGroupList = adminToolDao.getSeatGroupList(qunarName);
        if (CollectionUtil.isEmpty(seatGroupList)) {
            return JsonResultUtil.buildFailedJsonResult("没有查到结果");
        }

        for (ASeatGroupVO sg : seatGroupList) {
            sg.setStrategyText(SeatSortStrategyEnum.getStrategy(sg.getStrategy()).getName());
        }

        return JsonResultUtil.buildSucceedJsonResult(seatGroupList);
    }


    @RequestMapping("querySeatByBusiSupplier.json")
    @ResponseBody
    public JsonResultVO querySeatByBusiSupplier(
            @RequestParam(value = "busiSupplierId", required = false) String busiSupplierId
            ,@RequestParam(value = "supplierId", required = false) Integer supplierId,
            HttpServletRequest request, HttpServletResponse response) {

        List<ASeatVO> seatList = adminToolDao.getSeatByBusiSupplier(busiSupplierId, supplierId);
        if (CollectionUtil.isEmpty(seatList)) {
            return JsonResultUtil.buildFailedJsonResult("没有查到结果");
        }

        for (ASeatVO s : seatList) {
            s.setCreateTimeText(DateUtil.getTimestampString(s.getCreateTime()));
            if (s.getLastUpdateTime() != null) {
                s.setLastUpdateTimeText(DateUtil.getTimestampString(s.getLastUpdateTime()));

            }
            SeatOnlineState sos = getSeatOnlineState(s.getQunarName());
            if (sos != null) {
                s.setLoginStatus(sos.getOnlineState().name());
                s.setTerminalType(sos.getResource());
            }

            BusinessEnum bEnum = BusinessEnum.of(s.getBType());
            if (bEnum != null) {
                s.setBuTypeText(bEnum.getName());
            }
        }

        return JsonResultUtil.buildSucceedJsonResult(seatList);
    }


    @RequestMapping("queryGroupProduct.json")
    @ResponseBody
    public JsonResultVO queryGroupProduct(
            @RequestParam(value = "groupId", required = false) Integer groupId,
            HttpServletRequest request, HttpServletResponse response) {

        List<ASeatGroupVO> seatGroupList = adminToolDao.getGroupProductList(groupId);
        if (CollectionUtil.isEmpty(seatGroupList)) {
            JsonResultUtil.buildFailedJsonResult("没有查到结果");
        }

        for (ASeatGroupVO sg : seatGroupList) {
            sg.setStrategyText(SeatSortStrategyEnum.getStrategy(sg.getStrategy()).getName());
        }

        return JsonResultUtil.buildSucceedJsonResult(seatGroupList);
    }



    private SeatOnlineState getSeatOnlineState(String qunarName) {

        return null;
    }


    @RequestMapping(value = "seatWorkModel.json")
    @ResponseBody
    public JsonData seatWorkModel(String qunarName) {
        if (Strings.isNullOrEmpty(qunarName)) {
            return JsonData.error("参数错误");
        }
        try {
            List<LogEntity> logEntities = logOperationService.querySeatWorkModelLogs(qunarName);
            if (CollectionUtil.isEmpty(logEntities)) {
                return JsonData.error("查询结果为空");
            }
            return JsonData.success(logEntities);
        } catch (Exception e) {
            log.error("查询客服工作模式日志出错，qunarName={}", qunarName, e);
        }
        return JsonData.error("系统错误");
    }
}
