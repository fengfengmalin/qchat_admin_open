package com.qunar.qtalk.ss.sift.service;

import com.google.common.collect.Lists;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.utils.JID;
import com.qunar.qtalk.ss.sift.dao.CsrDao;
import com.qunar.qtalk.ss.sift.entity.CSR;
import com.qunar.qtalk.ss.sift.enums.csr.CsrServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CsrService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsrService.class);

    @Autowired
    private CsrDao csrDao;

    public List<CSR> queryCsrsByCsrIDs(List<Long> seatIDs) {
        LOGGER.debug("查询csr List-{}", JacksonUtil.obj2String(seatIDs));
        List<CSR> csrList = csrDao.selectCsrsByCsrIDs(seatIDs);
        LOGGER.info("csr List-{}的查询列表结果为：{}", JacksonUtil.obj2String(seatIDs),
                JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public List<CSR> queryOnlineCsrsByShopID(long shopID, String host) {
        LOGGER.debug("查询商铺的csr-{}", shopID);
        List<CSR> csrList = csrDao.selectOnlineCsrsByShopID(shopID, host);
        LOGGER.info("商铺-{}-csr的查询结果为：{}", shopID, JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public List<CSR> queryCsrsByShopIdWithoutCarName(long shopID, String csrName, String domain) {
        LOGGER.debug("查询商铺的csr-{} csrName:{}", shopID, csrName);
        List<CSR> csrList = csrDao.selectCsrsByShopIdWithoutCarName(shopID, csrName, domain);
        LOGGER.info("商铺-{}-csr的查询结果为：{}", shopID, JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public List<CSR> queryOnlineCsrByCsrQunarName(JID csrName) {
        LOGGER.debug("即将通过客服的qunarName-{}, 查询到该客服的所有信息。",
                JacksonUtil.obj2String(csrName));
        List<CSR> csrList = csrDao.selectCsrsByCsrName(csrName.getNode(), csrName.getDomain());
        LOGGER.info("通过客服的qunarName-{}, 查询到该客服的结果为：{}",
                JacksonUtil.obj2String(csrName), JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public List<Long> queryServiceShopByCsrName(JID csrName) {
        LOGGER.debug("即将通过客服的qunarName-{}，查询服务的商铺",
                JacksonUtil.obj2String(csrName));
        List<CSR> csrList = queryOnlineCsrByCsrQunarName(csrName);
        if (CollectionUtil.isEmpty(csrList)) {
            LOGGER.error("未查询到该客服-{}所服务的商铺。",
                    JacksonUtil.obj2String(csrList));
            return Collections.EMPTY_LIST;
        }
        List<Long> shopIDs = Lists.newArrayList();
        for (CSR csr : csrList) {
            if (!shopIDs.contains(csr.getSupplierID())
                    && csr.getServiceStatus() != CsrServiceStatus.DND_MODE.code) {
                shopIDs.add(csr.getSupplierID());
            }

        }
        LOGGER.info("{}-当前客服获取的正在服务的商铺有：{}",
                JacksonUtil.obj2String(shopIDs));
        return shopIDs;
    }

    public List<CSR> queryCsrsByGroupIDs(List<Long> groupIds, String host) {
        LOGGER.debug("即将通过客服组GroupIDs -{}，查询客服列表", JacksonUtil.obj2String(groupIds));
        List<CSR> csrList = csrDao.selectCsrsByGroupIDs(groupIds, host);
        LOGGER.info("GroupIDs:{} 获取的客服列表有：{}", JacksonUtil.obj2String(groupIds), JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public List<CSR> queryCsrByQunarNameAndShopId(String qunarName, long shopId, String host) {
        List<CSR> csrList = csrDao.selectCsrByCsrNameAndShopId(qunarName, shopId, host);
        LOGGER.info("QunarName:{} ShopId:{} 获取的客服列表有：{}", qunarName, shopId, JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public List<CSR> queryCsrsByCsrBusiIdAndHost(int busId, String host) {
        List<CSR> csrList = csrDao.selectCsrsByCsrBusiIdAndHost(busId, host);
        LOGGER.info("busId:{} host:{} 获取的客服列表有：{}", busId, host, JacksonUtil.obj2String(csrList));
        return csrList;
    }

    public boolean saveSeat(Seat seat) {
        int saveCSR = csrDao.saveSeat(seat);
        LOGGER.info("saveSeat:{} result:{}", seat.getQunarName(), saveCSR);
        return saveCSR == 1;
    }

    public boolean updateSeatByShopId(long shopId) {
        int updateSeat = csrDao.updateSeatByShopId(shopId);
        LOGGER.info("updateSeat:{} result:{}", shopId, updateSeat);
        return updateSeat == 1;
    }

    public boolean updateSeatStatusByShopIdAndName(long shopId, String qunarName) {
        int updateSeat = csrDao.updateSeatStatusByShopIdAndName(shopId, qunarName);
        LOGGER.info("updateSeatStatus:{} result:{}", shopId, updateSeat);
        return updateSeat == 1;
    }

    public List<CSR> selectCsrByCsrNameAndShopIdWithoutStatus(String qunarName, long shopId) {
        List<CSR> csrList = csrDao.selectCsrByCsrNameAndShopIdWithoutStatus(qunarName, shopId);
        LOGGER.info("WithoutStatus qunarName:{} shopId:{} 获取的客服列表有：{}", qunarName, shopId, JacksonUtil.obj2String(csrList));
        return csrList;
    }
}
