package com.qunar.qchat.admin.service.impl;

import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.dao.IPageTemplateDao;
import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.PageTemplate;
import com.qunar.qchat.admin.service.IPageTemplateService;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.PageTemplateVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qyhw on 11/17/15.
 */
@Service("pageTemplateService")
@Transactional
public class PageTemplateServiceImpl implements IPageTemplateService {

    @Autowired
    IPageTemplateDao pageTemplateDao;

    @Override
    public PageTemplateVO getPageTemplateById(int id) {
        PageTemplate pt = pageTemplateDao.getPageTemplateById(id);
        return toVO(pt);
    }

    @Override
    public BusiReturnResult saveOrUpdateTemplate(PageTemplateVO pageTemplate) {
        BusiReturnResult brResult = new BusiReturnResult();
        brResult.setCode(BusiResponseCodeEnum.SUCCESS.getCode());

        if(pageTemplate != null && pageTemplate.getId() > 0) {
            int num = pageTemplateDao.updatePageTemplate(toModel(pageTemplate));
            brResult.setData(num);
            return brResult;
        }
        int id = pageTemplateDao.savePageTemplate(toModel(pageTemplate));
        brResult.setData(id);
        return brResult;
    }

    @Override
    public List<PageTemplateVO> queryPageTemplateList(int busiType) {
        List<PageTemplate> pageTemplateList = pageTemplateDao.queryPageTemplateList(busiType);

        List<PageTemplateVO> pageTemplateVOList = null;
        if(CollectionUtils.isNotEmpty(pageTemplateList)) {
            pageTemplateVOList = new ArrayList<PageTemplateVO>(pageTemplateList.size());
            for(PageTemplate pt : pageTemplateList) {
                pageTemplateVOList.add(toVO(pt));
            }
        }
        return pageTemplateVOList;
    }

    private PageTemplateVO toVO(PageTemplate pt){
        if(pt == null) {return null;}
        PageTemplateVO ptVO = new PageTemplateVO();

        ptVO.setId(pt.getId());
        ptVO.setName(pt.getName());
        ptVO.setPageCss(pt.getPageCss());
        ptVO.setPageHtml(pt.getPageHtml());
        ptVO.setCreateTime(pt.getCreateTime().getTime());
        ptVO.setBusiType(pt.getBusiType());
        ptVO.setBusiName(BusinessEnum.of(pt.getBusiType()).getName());

        return ptVO;
    }

    private PageTemplate toModel(PageTemplateVO ptVO){
        if(ptVO == null) {return null;}
        PageTemplate pt = new PageTemplate();

        pt.setId(ptVO.getId());
        pt.setName(ptVO.getName());
        pt.setPageCss(ptVO.getPageCss());
        pt.setPageHtml(ptVO.getPageHtml());
        pt.setBusiType(ptVO.getBusiType());

        return pt;
    }
}
