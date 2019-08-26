package com.qunar.qchat.admin.controller;

import com.qunar.qchat.admin.annotation.MustLogin;
import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.model.SystemUser;
import com.qunar.qchat.admin.service.IPageTemplateService;
import com.qunar.qchat.admin.service.ISystemUserService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.util.SessionUtils;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.PageTemplateVO;
import com.qunar.qchat.admin.vo.SupplierVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by qyhw on 11/25/15.
 */
@Controller
@RequestMapping("/template")
public class PageTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(PageTemplateController.class);

    @Autowired
    private IPageTemplateService pageTemplateService;

    @Resource(name = "systemUserService")
    private ISystemUserService systemUserService;

    @MustLogin(value = MustLogin.ViewType.VM)
    @RequestMapping("/list.do")
    public ModelAndView getManagePage() {
        return new ModelAndView("page/busiRelated/inputTmp");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "saveOrUpdateTemplate.qunar")
    public JsonResultVO<?> saveOrUpdateTemplate(@RequestParam(value = "p", required = true, defaultValue = "") String p) {
        logger.info("saveOrUpdateTemplate -- p: {}", p);
        PageTemplateVO templateVO = null;

        try {
            templateVO = JacksonUtil.string2Obj(p, PageTemplateVO.class);
        } catch (Exception e) {
            logger.error("saveOrUpdateTemplate -- " + e.getMessage(), e);
            return JsonResultUtil.buildFailedJsonResult("参数不正确.");
        }

        pageTemplateService.saveOrUpdateTemplate(templateVO);
        return JsonResultUtil.buildSucceedJsonResult("操作成功.", "");
    }

    @MustLogin(MustLogin.ViewType.JSON)
    @ResponseBody
    @RequestMapping(value = "queryTemplateList.qunar")
    public JsonResultVO<?> pageQuerySeatList(@RequestParam(value = "busiType", required = false, defaultValue = "1") int busiType) {
        List<PageTemplateVO> ptList = pageTemplateService.queryPageTemplateList(busiType);
        return JsonResultUtil.buildSucceedJsonResult(ptList);
    }
}
