package com.qunar.qchat.admin.service.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.RobotConfig;
import com.qunar.qchat.admin.dao.*;
import com.qunar.qchat.admin.dao.welcomes.SupplierWelcomesDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.util.BusiReturnResultUtil;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.JacksonUtil;
import com.qunar.qchat.admin.util.SupplierServiceUtil;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.third.SupplierOperatorInfo;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qtalk.ss.sift.dao.HotlineSupplierMappingDao;
import com.qunar.qtalk.ss.sift.entity.HotlineSupplierMapping;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/15/15.
 */
@Service("supplierService")
@Transactional
public class SupplierServiceImpl implements ISupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

    @Autowired
    ISystemUserDao systemUserDao;
    @Autowired
    ISupplierDao supplierDao;
    @Autowired
    IBusinessDao businessDao;
    @Autowired
    IBusiSupplierMappingDao busiSupplierMappingDao;
    @Autowired
    IBusiSeatMappingDao busiSeatMappingDao;
    @Autowired
    HotlineSupplierMappingDao hotlineSupplierMappingDao;
    @Resource(name = "seatDao")
    private ISeatDao seatDao;
    @Resource
    private SupplierWelcomesDao supplierWelcomesDao;
    @Autowired
    IRobotService robotService;

    private static final Function<Supplier, Map> supplier2Map = new Function<Supplier, Map>() {
        @Override
        public Map apply(Supplier supplier) {
            if (supplier == null) {
                return null;
            }
            Map<String, Object> result = Maps.newHashMap();
            result.put("supplierId", supplier.getId());
            result.put("welcomes", supplier.getWelcomes());
            result.put("noServiceWelcomes", supplier.getNoServiceWelcomes());
            return result;
        }
    };

    private static final Predicate<Map.Entry<String, Object>> FILTER_SUPPLIER = new Predicate<Map.Entry<String, Object>>() {
        @Override
        public boolean apply(Map.Entry<String, Object> entry) {
            return entry != null && SupplierInfo.SUPPLIER_COLS.contains(entry.getKey());
        }
    };

    private static final Predicate<Map.Entry<String, Object>> FILTER_BSUM = new Predicate<Map.Entry<String, Object>>() {
        @Override
        public boolean apply(Map.Entry<String, Object> entry) {
            return entry != null && SupplierInfo.BSUM_COLS.contains(entry.getKey());
        }
    };

    @Override
    @Deprecated
    public List<Supplier> getSupplierList() {
        return supplierDao.getSupplierList();
    }

    @Override
    public BusiReturnResult saveSupplier(SupplierVO supplierVO) {
        if (supplierVO == null) {
            logger.error("saveSupplier --- 参数不正确, supplierVO == null");
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, false);
        }
        logger.info("saveSupplier --- supplier info: " + JacksonUtil.obj2String(supplierVO));

        String busiSupplierId = supplierVO.getBusiSupplierId();
        SupplierVO supplierDB = this.getSupplierByBusiSupplierId(busiSupplierId, supplierVO.getBusiType());
        if (supplierDB != null) {
            logger.warn("saveSupplier --- 业务线供应商已经存在, 业务供应商编号: {}", busiSupplierId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_REPEAT, false);
        }

        Supplier supplier = buildSupplierObj(supplierVO);
        long supplierId = supplierDao.saveSupplier(supplier);
        if (supplierId <= 0) {
            logger.error("saveSupplier --- 添加供应商失败, 业务供应商编号: {}", busiSupplierId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION, false);
        }

        afterSet(supplierVO, supplierId);

        logger.info("saveSupplier --- 成功添加供应商,供应商编号:{}", supplierId);
        return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.SUCCESS, true, supplierId);
    }


    @Override
    public BusiReturnResult saveSupplierEx(SupplierVO supplierVO) {
        if (supplierVO == null) {
            logger.error("saveSupplier --- 参数不正确, supplierVO == null");
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, false);
        }
        logger.info("saveSupplier --- supplier info: " + JacksonUtil.obj2String(supplierVO));

        String busiSupplierId = supplierVO.getBusiSupplierId();
        SupplierVO supplierDB = this.getSupplierByBusiSupplierId(busiSupplierId, supplierVO.getBusiType());
        if (supplierDB != null) {
            logger.warn("saveSupplier --- 业务线供应商已经存在, 业务供应商编号: {}", busiSupplierId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_REPEAT, false);
        }

        Supplier supplier = new Supplier();
        supplier.setName(supplierVO.getName());
        supplier.setBQueue(supplierVO.getExt_flag());
        supplier.setStatus(supplierVO.getStatus());
        supplier.setbType(supplierVO.getBusiType());

        long supplierId = supplierDao.saveSupplier(supplier);
        if (supplierId <= 0) {
            logger.error("saveSupplier --- 添加供应商失败, 业务供应商编号: {}", busiSupplierId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION, false);
        }

        afterSet(supplierVO, supplierId);

        logger.info("saveSupplier --- 成功添加供应商,供应商编号:{}", supplierId);
        return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.SUCCESS, true, supplierId);
    }



    private Supplier buildSupplierObj(SupplierVO supplierVO) {
        Supplier supplier = new Supplier();
        supplier.setName(supplierVO.getName());
        return supplier;
    }

    private void afterSet(SupplierVO supplierVO, long supplierId) {
        List<String> qNameList = supplierVO.getQunarNameList();
        // 添加管理员账号
        addManager(supplierId, qNameList);
        // 添加与业务线映射关系
        addMapping(supplierVO, supplierId);
        // 添加与热线映射
        addHotlineMapping(supplierId);
        // 添加客服
        List<Seat> seatList = supplierVO.getSeatList();
        addSeats(seatList, supplierId, supplierVO.getBusiType());
    }

    public void setReturnResult(BusiResponseCodeEnum brcEnum, BusiReturnResult bReturn, boolean ret) {
        bReturn.setRet(ret);
        bReturn.setCode(brcEnum.getCode());
        bReturn.setMsg(brcEnum.getMsg());
    }

    private void addSeats(List<Seat> seatList, long supplierId, int busiType) {
        if (CollectionUtil.isEmpty(seatList)) {
            return;
        }
        for (Seat s : seatList) {
            if (StringUtils.isEmpty(s.getQunarName())) {
                continue;
            }
            List<Seat> seatLists = seatDao.getSeat(s.getQunarName());
            if (CollectionUtils.isNotEmpty(seatLists)) {
                logger.info("quanrName:{} already exist", s.getQunarName());
                continue;
            }
            s.setSupplierId(supplierId);
            s.setPriority(s.getPriority() == null ? 0 : s.getPriority());
            long seatId = seatDao.saveSeat(s);

            BusiSeatMapping busiSeatMapping = new BusiSeatMapping();
            busiSeatMapping.setSeatId(seatId);
            busiSeatMapping.setBusiId(busiType);
            busiSeatMappingDao.saveBusiSeatMapping(busiSeatMapping);
        }
    }

    private void addMapping(SupplierVO supplierVO, long supplierId) {
        BusiSupplierMapping busiSupplierMapping = new BusiSupplierMapping();
        busiSupplierMapping.setSupplierId(supplierId);
        busiSupplierMapping.setBusiId(supplierVO.getBusiType());
        busiSupplierMapping.setBusiSupplierId(supplierVO.getBusiSupplierId());
        busiSupplierMapping.setbSuIdAndType(supplierVO.getBusiSupplierId() + supplierVO.getBusiType());
        busiSupplierMappingDao.saveBusiSupplierMapping(busiSupplierMapping);
        logger.info("saveSupplier -- saveBusiSupplierMapping : supplierId = {} , busiSupplierId = {}.", supplierId,
                supplierVO.getBusiSupplierId());
    }

    private void addHotlineMapping(long supplierId) {
        HotlineSupplierMapping mapping = new HotlineSupplierMapping();
        mapping.setHotline(String.format("shop_%d@%s", supplierId, QChatConstant.DEFAULT_HOST));
        mapping.setSupplierId(supplierId);
        hotlineSupplierMappingDao.insertHotlineSeat(mapping);
    }

    private void addManager(long supplierId, List<String> qNameList) {
        if (CollectionUtil.isEmpty(qNameList)) {
            return;
        }
        List<SystemUser> systemUsers = new ArrayList<>();
        for (String qunarName : qNameList) {
            SystemUser sysUserDB = systemUserDao.getSystemUser(qunarName, supplierId);
            if (sysUserDB != null) {
                logger.warn("saveSupplier -- 用户中心登陆用户名: {} 已关联供应商,请核对.", qunarName);
                continue;
            }

            SystemUser sysUser = new SystemUser();
            sysUser.setSupplierId(supplierId);
            sysUser.setQunarName(qunarName);
           // systemUserDao.saveSystemUser(sysUser);
            systemUsers.add(sysUser);
        }
        systemUserDao.saveSystemUserList(systemUsers);
    }

    @Override
    public BusiReturnResult updateSupplier(SupplierVO supplierVO) {
        BusiReturnResult bReturn = new BusiReturnResult();
        if (supplierVO == null) {
            logger.error("updateSupplier --- 参数不正确");
            setReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, bReturn, false);
            return bReturn;
        }

        logger.info("updateSupplier --- 输入: " + JacksonUtil.obj2String(supplierVO));

        String busiSupplierId = supplierVO.getBusiSupplierId();
        int busiType = supplierVO.getBusiType();
        Supplier supplierDB = supplierDao.getSupplierByBusiSupplierId(busiSupplierId, busiType);
        if (supplierDB == null) {
            logger.error("updateSupplier --- 供应商不存在");
            setReturnResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS, bReturn, false);
            return bReturn;
        }
        supplierDB.setName(supplierVO.getName());
        int num = supplierDao.updateSupplier(supplierDB);

        logger.info("updateSupplier --- 成功修改供应商信息,业务线供应商编号:{}", busiSupplierId);
        setReturnResult(BusiResponseCodeEnum.SUCCESS, bReturn, true);
        bReturn.setData(num);
        return bReturn;
    }


    @Override
    public BusiReturnResult updateFullSupplier(Supplier supplier) {
        BusiReturnResult bReturn = new BusiReturnResult();
        if (supplier == null) {
            logger.error("updateSupplier --- 参数不正确");
            setReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, bReturn, false);
            return bReturn;
        }
        int num = supplierDao.updateFullSupplier(supplier);
        setReturnResult(BusiResponseCodeEnum.SUCCESS, bReturn, true);
        bReturn.setData(num);
        return bReturn;
    }


    @Override
    public List<SupplierVO> getSupplierByQunarName(String qunarName){
        return getSupplierByQunarName(qunarName, 0, 0, 0);
    }

    @Override
    public List<SupplierVO> getSupplierByQunarName(String qunarName, int pageNum, int pageSize, int businessId) {
//        List<Supplier> suList = supplierDao.getSupplierByQunarName(qunarName);
//        if (CollectionUtil.isEmpty(suList)) {
//            return null;
//        }
//
//        List<SupplierVO> suVOList = new ArrayList<>(suList.size());
//        for (Supplier s : suList) {
//            suVOList.add(SupplierServiceUtil.modelToVO(s));
//        }

        if (Strings.isNullOrEmpty(qunarName))
            return null;
        List<SupplierWithRobot> suList = null;
        if (pageNum > 0 && pageSize > 0 && businessId > 0) {
            suList = supplierDao.getPageSupplierWithRobotByQunarName(qunarName, pageNum, pageSize, businessId);
        } else {
            suList = supplierDao.getSupplierWithRobotByQunarName(qunarName);
        }

        if (CollectionUtil.isEmpty(suList))
            return null;

        Map<Long, SupplierVO> suVOList = Maps.newHashMap();
        for (SupplierWithRobot s : suList) {
            SupplierVO vo = null;
            if (suVOList.containsKey(s.getId())) {
                vo = suVOList.get(s.getId());
            } else {
                vo = SupplierServiceUtil.modelToVO(s);
                suVOList.put(vo.getId(), vo);
            }


            if (!Strings.isNullOrEmpty(s.getRobot_id())) {
                SupplierRobotVO supplierRobotVO = new SupplierRobotVO();
                supplierRobotVO.setRobotname(s.getRobot_id());
                if (RobotStrategyEnum.RSE_INHERIT.getValue() == s.getStrategy()) {
                    s.setStrategy(
                            RobotConfig.robotEnabel(BusinessEnum.of(vo.getBusiType()).getEnName(), vo.getId()) ?
                                    RobotStrategyEnum.RSE_DEFAULT.getValue() :
                                    RobotStrategyEnum.RSE_NO_ROBOT.getValue()
                    );
                }
                supplierRobotVO.setRobotStrategy(s.getStrategy());
                supplierRobotVO.setRobotWebcome(s.getWelcome());
                if (Strings.isNullOrEmpty(supplierRobotVO.getRobotWebcome())) {
                    supplierRobotVO.setRobotWebcome(
                            RobotConfig.getRobotDefaultByBu(BusinessEnum.of(vo.getBusiType()).getEnName()));
                }
                List<SupplierRobotVO> supplierRobotVOList = vo.getRobots();
                if (null == vo.getRobots()) {
                    supplierRobotVOList = Lists.newArrayList();
                    vo.setRobots(supplierRobotVOList);
                    vo.setRobotName(supplierRobotVO.getRobotname());
                    vo.setRobotStrategy(supplierRobotVO.getRobotStrategy());
                    vo.setRobotWebcome(supplierRobotVO.getRobotWebcome());
                }
                supplierRobotVOList.add(supplierRobotVO);
            }
        }

        List<SupplierVO> result = Lists.newArrayList(suVOList.values());
        for (SupplierVO supplierVO : result) {
            if (Strings.isNullOrEmpty(supplierVO.getRobotName())) {
                BusinessEnum bu = BusinessEnum.of(supplierVO.getBusiType());
                Robot robot = robotService.getRobotByBusiness(bu);
                // 业务线配置了就应该显示
                if (null != robot) {
                    supplierVO.setRobotName(robot.getRobotId());
                    supplierVO.setRobotStrategy(
                            RobotConfig.robotEnabel(bu.getEnName(), supplierVO.getId()) ?
                                    RobotStrategyEnum.RSE_DEFAULT.getValue() :
                                    RobotStrategyEnum.RSE_NO_ROBOT.getValue());
                    supplierVO.setRobotWebcome(RobotConfig.getRobotDefaultByBu(bu.getEnName()));
                }
            }

        }
        return result;

    }

    @Override
    public Long getPageCountSupplier(String qunarName, int businessId) {
        return supplierDao.getPageCountSupplier(qunarName, businessId);
    }

    @Override
    public List<SupplierVO> getSupplierBySeatQName(String qName, int bType) {
        List<Supplier> suList = supplierDao.getSupplierBySeatQName(qName, bType);
        if (CollectionUtil.isEmpty(suList)) {
            return null;
        }

        List<SupplierVO> suVOList = new ArrayList<>(suList.size());
        for (Supplier s : suList) {
            suVOList.add(SupplierServiceUtil.modelToVO(s));
        }

        return suVOList;
    }

    @Override
    public List<SupplierVO> getSupplierBySeatQName(String qName) {
        if (Strings.isNullOrEmpty(qName)) {
            return null;
        }
        List<Supplier> supplierList = supplierDao.getAllSupplierBySeatQName(qName);
        if (CollectionUtil.isEmpty(supplierList)) {
            return null;
        }
        List<SupplierVO> supplierVOs = Lists.newArrayList();
        for (Supplier supplier : supplierList) {
            supplierVOs.add(SupplierServiceUtil.modelToVO(supplier));
        }
        return supplierVOs;
    }

    @Override
    public SupplierVO getSupplierByBusiSupplierId(String busiSupplierId, int busiType) {
        Supplier s = supplierDao.getSupplierByBusiSupplierId(busiSupplierId, busiType);
        return SupplierServiceUtil.modelToVO(s);
    }

    @Override
    public Supplier getSupplierByBusiSupplierIdEx(String busiSupplierId, int busiType) {
        Supplier s = supplierDao.getSupplierByBusiSupplierIdEx(busiSupplierId, busiType);
        return s;
    }

    @Override
    public int updateSupplierQunarNameMapping(long supplierId, List<String> qunarNameList) {
        if (supplierId <= 0 || CollectionUtil.isEmpty(qunarNameList)) {
            return 0;
        }
        systemUserDao.delSystemUserBySupplierId(supplierId);

        int num = 0;
        for (String qunarName : qunarNameList) {
            SystemUser sysUserDB = systemUserDao.getSystemUser(qunarName, supplierId);
            if (sysUserDB != null) {
                logger.error("updateSupplierQunarNameMapping -- 管理员账号: {} 已关联供应商,供应商编号 {},请核对.", qunarName, supplierId);
                continue;
            }
            num++;
            SystemUser sysUser = new SystemUser();
            sysUser.setSupplierId(supplierId);
            sysUser.setQunarName(qunarName);
            systemUserDao.saveSystemUser(sysUser);
        }
        return num;
    }

    @Override
    public int updateSupplierSysUsers(long supplierId, List<String> addList, List<String> delList) {
        if (supplierId <= 0 ) {
            return 0;
        }

        if (!CollectionUtil.isEmpty(addList)) {
            for (String qunarName : addList) {
                SystemUser sysUserDB = systemUserDao.getSystemUser(qunarName, supplierId);
                if (sysUserDB != null) {
                    logger.error("updateSupplierQunarNameMapping -- 管理员账号: {} 已关联供应商,供应商编号 {},请核对.", qunarName, supplierId);
                } else {
                    SystemUser sysUser = new SystemUser();
                    sysUser.setSupplierId(supplierId);
                    sysUser.setQunarName(qunarName);
                    systemUserDao.saveSystemUser(sysUser);
                }
            }
        }

        if (!CollectionUtil.isEmpty(delList)){
            for (String qunarName : delList) {
                SystemUser sysUserDB = systemUserDao.getSystemUser(qunarName, supplierId);
                if (sysUserDB == null) {
                    logger.error("updateSupplierQunarNameMapping -- 管理员账号: {} 已关联供应商,供应商编号 {},请核对.", qunarName, supplierId);
                } else {
                    systemUserDao.delSystemUser(qunarName,supplierId);
                }
            }
        }
        return 0;
    }

    @Override
    public List<SupplierGroupVO> getSuGroupList(List<Long> suIdList) {
        if (CollectionUtil.isEmpty(suIdList)) {
            return null;
        }
        return supplierDao.getSuGroupList(suIdList);
    }

    @Override
    public List<DeptUserVO> getOperatorsBySeatQunarName(String seatQunarName) {
        List<SupplierOperatorInfo> operatorInfos = busiSupplierMappingDao.getOperatorsBySeatQunarName(seatQunarName);
        if (CollectionUtil.isEmpty(operatorInfos)) {
            return null;
        }
        List<DeptUserVO> deptUserVOs = Lists.newArrayList();
        for (SupplierOperatorInfo operatorInfo : operatorInfos) {
            if (operatorInfo == null) {
                continue;
            }
            deptUserVOs.add(operatorToDeptUser(operatorInfo));
        }
        return deptUserVOs;
    }

    private DeptUserVO operatorToDeptUser(SupplierOperatorInfo operatorInfo) {
        if (operatorInfo == null) {
            return null;
        }
        DeptUserVO deptUserVO = new DeptUserVO();
        deptUserVO.setQunarName(operatorInfo.getQunarName());
        deptUserVO.setWebName(operatorInfo.getWebName());
        deptUserVO.setNickName(operatorInfo.getNickName());
        return deptUserVO;
    }

    @Override
    public boolean saveSupplierOperator(SupplierOperatorInfo supplierOperatorInfo) {
        return busiSupplierMappingDao.saveSupplierOperator(supplierOperatorInfo) > 0;
    }

    @Override
    public List<Supplier> getSupplierByIds(List<Long> ids) {
        return supplierDao.getSupplierByIds(ids);
    }

    @Override
    public void saveSupplierInfo(List<SupplierInfo> supplierInfos) {
        if (CollectionUtil.isEmpty(supplierInfos)) {
            return;
        }
        for (SupplierInfo supplierInfo : supplierInfos) {
            Map<String, Object> columns = supplierInfo.getColumns();
            if (MapUtils.isEmpty(columns)) {
                continue;
            }
            // 过滤supplier表信息
            Map<String, Object> supplierCols = Maps.filterEntries(columns, FILTER_SUPPLIER);
            // 过滤busi_supplier_mapping表信息
            Map<String, Object> bsumCols = Maps.filterEntries(columns, FILTER_BSUM);
            if (MapUtils.isEmpty(supplierCols) && MapUtils.isEmpty(bsumCols)) {
                continue;
            }
            if (MapUtils.isNotEmpty(supplierCols)) {
                supplierInfo.setSupplierCols(supplierCols);
                supplierInfo.setHasSupplierCol(true);
            }
            if (MapUtils.isNotEmpty(bsumCols)) {
                supplierInfo.setBsumCols(bsumCols);
                supplierInfo.setHasBsumCol(true);
            }
            supplierDao.saveSupplierInfo(supplierInfo);
        }
    }

    @Override
    public BusiSupplierMapping getSupplierBySupplierId(long supplierId) {
        if (supplierId <= 0) {
            return null;
        }
        return busiSupplierMappingDao.getBusiSupplierMappingBySuId(supplierId);
    }

    @Override
    public boolean updateWelcomes(List<Supplier> suppliers) {
        if (CollectionUtil.isEmpty(suppliers)) {
            return false;
        }
        return supplierWelcomesDao.updateWelcomesBySupplierId(suppliers) > 0;
    }

    @Override
    public List<Map> querySupplierWelcomes(List<Long> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return null;
        }
        List<Supplier> suppliers = supplierWelcomesDao.getWelcomesBySupplierIds(supplierIds);
        if (CollectionUtils.isEmpty(suppliers)) {
            return null;
        }
        return Lists.transform(suppliers, supplier2Map);
    }

    @Override
    public String queryWelcomesBySeat(String seatQName, Long seatId) {
        if (seatId != null && seatId > 0) {
            return supplierWelcomesDao.queryWelcomesBySeatId(seatId);
        } else {
            return supplierWelcomesDao.queryWelcomesBySeatQName(seatQName);
        }
    }

    @Override
    public String queryWelcomesById(Long supplierId) {
        if (supplierId == null || supplierId <= 0) {
            return null;
        }
        return supplierWelcomesDao.queryWelcomesById(supplierId);
    }

    public List<SupplierVO> filterSupplierByBusiSupplierName(String busiSupplierName, List<SupplierVO> curBuSuList){
        List<SupplierVO> vos = Lists.newArrayList();
        busiSupplierName = busiSupplierName.trim();
        for (SupplierVO vo : curBuSuList) {
            if (busiSupplierName.equals(vo.getName().trim())) {
                vos.add(vo);
            }
        }
        return vos;
    }

}
