package com.qunar.qtalk.ss.consulttag.service;


import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.consulttag.dao.IConsultTagDao;
import com.qunar.qtalk.ss.consulttag.entity.ConsultTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConsultTagService {
    private static final Logger logger = LoggerFactory.getLogger(ConsultTagService.class);

    @Autowired
    IConsultTagDao consultTagDao;

    public JsonData findTagBySupplierId(long supplierId) {
        List<ConsultTag> consultTags = consultTagDao.selectBySupplierId(supplierId);
        logger.info("findTag SupplierId:{} result:{}", supplierId, JacksonUtil.obj2String(consultTags));
        return JsonData.success(consultTags);
    }

    public JsonData insertTag(long supplierId, String busiSupplierId, String pid,
                              int busiId, String title, String content, int type) {
        ConsultTag consultTag = new ConsultTag();
        consultTag.setSupplierId(supplierId);
        consultTag.setBusiSupplierId(busiSupplierId);
        consultTag.setPid(pid);
        consultTag.setBusiId(busiId);
        consultTag.setTitle(title);
        consultTag.setContent(content);
        consultTag.setConsultType(type);

        consultTagDao.insertConsultTag(consultTag);
        logger.info("id:{}", consultTag.getId());
        logger.info("insert consultTag:{}", supplierId, JacksonUtil.obj2String(consultTag));
        return JsonData.success("success");
    }
}
