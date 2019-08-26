package com.qunar.qchat.admin.controller.api;

import com.qunar.qchat.admin.service.IPageTemplateService;
import com.qunar.qchat.admin.util.JsonResultUtil;
import com.qunar.qchat.admin.vo.JsonResultVO;
import com.qunar.qchat.admin.vo.PageTemplateVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Controller
@RequestMapping("/api/template")
public class PageTemplateAPIController {
    private static final Logger logger = LoggerFactory.getLogger(PageTemplateAPIController.class);

    @Autowired
    private IPageTemplateService pageTemplateService;

    private static Map<String,JsonResultVO<?>> styleMap = new HashMap<String,JsonResultVO<?>>();

    @RequestMapping(value = "/getTemplate.qunar")
    @ResponseBody
    public JsonResultVO<?> getStyle(@RequestParam(value = "templateId", required = true) int templateId) {
        if(logger.isDebugEnabled()) {
            logger.debug("getStyle --- 获取模板信息, templateId : {}", templateId);
        }

        String key = String.valueOf(templateId);
        JsonResultVO<?> result = styleMap.get(key);
        if(result != null) {
            return result;
        }

        PageTemplateVO pts = pageTemplateService.getPageTemplateById(templateId);
        if(pts == null) {
            logger.warn("getStyle --- 未取到模板信息, templateId : {}", templateId);
            return JsonResultUtil.buildFailedJsonResult("未取到模板信息");
        }

        result = JsonResultUtil.buildSucceedJsonResult("成功获取模板信息",pts);
        styleMap.put(key,result);
        return result;
    }

//    private String buildKey(String pageType, int busiType) {
//        StringBuffer keyBuf = new StringBuffer("");
//        keyBuf.append(pageType);
//        keyBuf.append("-");
//        keyBuf.append(busiType);
//        return keyBuf.toString();
//    }

}
