package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.PageTemplate;

import java.util.List;

/**
 * Created by qyhw on 11/17/15.
 */
public interface IPageTemplateDao {

    PageTemplate getPageTemplateById(int id);

    int savePageTemplate(PageTemplate pageTemplate);

    int updatePageTemplate(PageTemplate pageTemplate);

    List<PageTemplate> queryPageTemplateList(int busiId);

}
