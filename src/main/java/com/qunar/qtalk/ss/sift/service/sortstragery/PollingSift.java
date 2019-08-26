package com.qunar.qtalk.ss.sift.service.sortstragery;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.consult.QtQueueManager;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.entity.Shop;
import com.qunar.qtalk.ss.sift.service.ISiftByAssignStrategy;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class PollingSift implements ISiftByAssignStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollingSift.class);

    @Override
    public CSR sift(List<CSR> csrList, Shop shop) {
        //按照创建时间排序
        csrList.sort(Comparator.comparing(CSR::getCreateTime));
        List<JID> userNames = Lists.newArrayList();
        for (CSR csr : csrList) {
            userNames.add(csr.getQunarName());

        }

        LOGGER.info("准备获取客服-{}的最新分配时间", JacksonUtil.obj2String(userNames));
        LinkedList<Map.Entry<String, Timestamp>> pairList =
                QtQueueManager.getInstance().lastDistributeTime(userNames, shop.getId());
        LOGGER.info("获取客服-{}的最新分配时间时间结果为：{}",
                JacksonUtil.obj2String(userNames), JacksonUtil.obj2String(pairList));

        //都是新客服 按照创建时间推荐
        if (CollectionUtil.isEmpty(pairList)) {
            LOGGER.info("获取到的pairList为空，返回列表中的第一个。");
            return csrList.get(0);
        }
        //说明有客服是从来未被推荐过的
        if (pairList.size() < userNames.size()) {
            LOGGER.info("因为pairList的大小:{} 小于 客服的大小：{}", pairList.size(), userNames.size());
            return siftNoServiceCSR(csrList, userNames, pairList);
        }

        pairList.sort(new Comparator<Map.Entry<String, Timestamp>>() {
            @Override
            public int compare(Map.Entry<String, Timestamp> o1, Map.Entry<String, Timestamp> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        LOGGER.info("排序之后的客服为：{}", JacksonUtil.obj2String(pairList));

        JID siftCsrName = JID.parseAsJID(pairList.get(0).getKey());

        for (CSR csr : csrList) {
            if (csr.getQunarName().equals(siftCsrName)) {
                return csr;
            }
        }
        LOGGER.error("Error:session中筛选出来的名字未在传入的列表里",
                JacksonUtil.obj2String(csrList), JacksonUtil.obj2String(pairList));
        return csrList.get(0);
    }

    private CSR siftNoServiceCSR(List<CSR> csrList, List<JID> allUserNames, LinkedList<Map.Entry<String, Timestamp>> pairList) {
        //找到所有有服务的名字
        List<JID> hasServiceNames = Lists.newArrayList();
        for (Map.Entry<String, Timestamp> pair : pairList) {
            hasServiceNames.add(JID.parseAsJID(pair.getKey()));
        }
        LOGGER.info("全部客服名字的名字为：{}，有过服务的客服名字为：{}",
                JacksonUtil.obj2String(allUserNames), JacksonUtil.obj2String(hasServiceNames));
        //取差集
        allUserNames.removeAll(hasServiceNames);
        LOGGER.info("取差集之后的结果为：{}", JacksonUtil.obj2String(allUserNames));
        if (CollectionUtils.isEmpty(allUserNames)) {
            return null;
        }
        JID siftName = allUserNames.get(0);
        for (CSR csr : csrList) {
            if (csr.getQunarName().equals(siftName)) {
                return csr;
            }
        }

        //按照正常逻辑 应该是不会走到这步的
        LOGGER.error("Error:找不到未服务的名字，返回最早创建的那个, {}，{}，{}",
                JacksonUtils.obj2String(csrList), JacksonUtils.obj2String(allUserNames), JacksonUtils.obj2String(pairList));
        return csrList.get(0);
    }
}
