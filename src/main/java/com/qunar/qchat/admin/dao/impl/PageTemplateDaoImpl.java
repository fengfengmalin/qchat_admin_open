package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.IPageTemplateDao;
import com.qunar.qchat.admin.model.PageTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 11/17/15.
 */
@Repository("pageTemplateDao")
public class PageTemplateDaoImpl extends BaseSqlSessionDao implements IPageTemplateDao {

    @Override
    public PageTemplate getPageTemplateById(int id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        return getReadSqlSession().selectOne("PageTemplateMapping.getPageTemplateById", map);
    }

    @Override
    public int savePageTemplate(PageTemplate pageTemplate) {
        getWriteSqlSession().insert("PageTemplateMapping.savePageTemplate", pageTemplate);
        return pageTemplate.getId();
    }

    @Override
    public int updatePageTemplate(PageTemplate pageTemplate) {
        return getWriteSqlSession().update("PageTemplateMapping.updatePageTemplate", pageTemplate);
    }

    @Override
    public List<PageTemplate> queryPageTemplateList(int busiId) {
        Map<String, Object> map = new HashMap<>();
        map.put("busiId", busiId);
        return getReadSqlSession().selectList("PageTemplateMapping.queryPageTemplateList", map);
    }
}
