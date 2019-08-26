package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.PageTemplateVO;

import java.util.List;

/**
 * Created by hongwu.yang on 11/17/15.
 */
public interface IPageTemplateService {

    /**
     * 获取页面模板内容
     * @param templateId 模板编号
     * @return
     */
    PageTemplateVO getPageTemplateById(int templateId);

    /**
     * 添加 or 编辑模板
     * @param pageTemplate
     * @return
     */
    BusiReturnResult saveOrUpdateTemplate(PageTemplateVO pageTemplate);

    /**
     * 获取模板列表
     * @param busiType
     * @return
     */
    List<PageTemplateVO> queryPageTemplateList(int busiType);

}
