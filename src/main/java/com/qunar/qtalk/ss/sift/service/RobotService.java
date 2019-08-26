package com.qunar.qtalk.ss.sift.service;

import com.qunar.qchat.admin.annotation.routingdatasource.DataSources;
import com.qunar.qchat.admin.annotation.routingdatasource.RoutingDataSource;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.model.ServiceStatusEnum;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qtalk.ss.sift.dao.RobotInfoDao;
import com.qunar.qtalk.ss.sift.dao.RobotShopRelationDao;
import com.qunar.qtalk.ss.sift.entity.*;
import com.qunar.qtalk.ss.sift.enums.csr.CsrStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("ssRobotService")
public class RobotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RobotService.class);
    @Autowired
    private RobotShopRelationDao robotShopRelationDao;
    @Autowired
    private RobotInfoDao robotInfoDao;

    @Resource
    private BusiShopMapService busiShopMapService;

    @Resource
    private BusiService busiService;

    @RoutingDataSource(DataSources.QCADMIN_MASTER)
    public RobotInfo queryRobotInfoByBusinessID(int busiID) {
        LOGGER.info("开始查询业务线-{}的机器人", String.valueOf(busiID));
        RobotInfo robotInfo = robotInfoDao.queryRobotInfoByBusiID(busiID);
        LOGGER.info("查询业务线-{}的机器人，机器人结果-{}", busiID, JacksonUtil.obj2String(robotInfo));
        return robotInfo;
    }

    /***
     * 根据店铺和业务线配置，获取机器人分配内容
     * @param shop
     * @return
     */
    public RobotShopRelation siftRobot(Shop shop) {
        LOGGER.info("开始筛选机器人-{}", JacksonUtil.obj2String(shop));
        //查看该业务线总开关
        if (!RobotConfig.ROBOT_SWITCH) {
            LOGGER.warn("shop id-{} 机器人总开关关闭", shop.getId());
            return null;
        }

        // 查到业务线 根据shop id 查到业务线ID
        BusiShopMapping mapping = busiShopMapService.queryBusiShopMappingByShopID(shop.getId());
        if (mapping == null) {
            LOGGER.error("未找到该商铺-{}对应的业务线店铺对应关系", shop.getId());
            return null;
        }


        //查询该业务线是否有机器人 ，如果没有就返回null
        RobotInfo robotInfo = queryRobotInfoByBusinessID(mapping.getBusiID());
        if (robotInfo == null) {
            LOGGER.error("该业务线未配备机器人");
            return null;
        }

        /*** 其实好像缺失了点东西，需要先查是否开通了一键排队 ***/

        //如果未开 则查询该业务线的店铺是否在例外名单里
        Busi busi = busiService.queryBusiByID(mapping.getBusiID());
        if (busi == null) {
            LOGGER.error("没有找到该商铺-{}所属的业务线", shop.getId());
            return null;
        }
        boolean isRobotEnable = RobotConfig.robotEnabel(busi.getEnglishName(), shop.getId());
        if (!isRobotEnable) {
            LOGGER.warn("该店铺没有在qunar的店铺名单里");
            return null;
        }

        LOGGER.info("通过商铺ID-{}和机器人ID-{},查询对应关系。", shop.getId(), robotInfo.getRobotID());
        RobotShopRelation relation = robotShopRelationDao.
                queryRobotShopRelationByShopIDAndRobotID(shop.getId(), robotInfo.getRobotID());
        LOGGER.info("{}, {} 查询到的结果为：{}",
                shop.getId(), robotInfo.getRobotID(), JacksonUtil.obj2String(relation));
        if (relation == null) {
            LOGGER.error("没有找到该店铺-{}的机器人配置", shop.getId());
        }
        LOGGER.info("商铺-{}-查询到的机器人策略为：{}",
                JacksonUtil.obj2String(shop), JacksonUtil.obj2String(relation));
        // 如果有 查询配置，机器人策略
        return relation;
    }

    @RoutingDataSource(DataSources.QCADMIN_MASTER)
    public CSR buildCSRByRobot(RobotShopRelation robotShopRelation, String host) {
        CSR csr = new CSR();
        csr.setId(-1L);
        csr.setQunarName(String.format("%s@%s", robotShopRelation.getRobotID(), host));
        csr.setHost(host);
        BusiShopMapping mapping = busiShopMapService.queryBusiShopMappingByShopID(robotShopRelation.getShopID());
        if (mapping != null) {
            RobotInfo robotInfo = robotInfoDao.
                    queryRobotInfoByRobotID(robotShopRelation.getRobotID(), mapping.getBusiID());
            csr.setFaceLink(robotInfo.getImgUrl());
        }
        csr.setMaxServiceCount(Integer.MAX_VALUE);
        csr.setSupplierID(robotShopRelation.getShopID());
        csr.setWebName(robotShopRelation.getRobotID());
        csr.setServiceStatus(ServiceStatusEnum.SUPER_MODE.getKey());
        csr.setStatus(CsrStatus.online.code);
        return csr;
    }
}
