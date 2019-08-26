package com.qunar.qchat.admin.service.impl;

import com.google.common.base.Splitter;
import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.constants.BusinessResponseCodeConstants;
import com.qunar.qchat.admin.dao.IBusiSeatGroupMappingDao;
import com.qunar.qchat.admin.dao.ISeatDao;
import com.qunar.qchat.admin.dao.ISeatGroupDao;
import com.qunar.qchat.admin.dao.ISeatGroupMappingDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.service.ISeatGroupService;
import com.qunar.qchat.admin.util.*;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.SeatGroupListVO;
import com.qunar.qchat.admin.vo.SeatGroupVO;
import com.qunar.qtalk.ss.utils.JacksonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/26/15.
 */
@Service("seatGroupService")
@Transactional
public class SeatGroupServiceImpl implements ISeatGroupService {

    private static final Logger logger = LoggerFactory.getLogger(SeatGroupServiceImpl.class);

    @Resource(name = "seatDao")
    ISeatDao seatDao;

    @Resource(name = "seatGroupDao")
    ISeatGroupDao seatGroupDao;

    @Autowired
    ISeatGroupMappingDao seatGroupMappingDao;

    @Autowired
    IBusiSeatGroupMappingDao busiSeatGroupMappingDao;

    @Override
    public BusiReturnResult saveOrUpdateSeatGroup(SeatGroupVO seatGroupVO) {
        BusiReturnResult result = null;
        if (seatGroupVO != null && CollectionUtil.isNotEmpty(seatGroupVO.getSuIdList())) {
            for (Long suId : seatGroupVO.getSuIdList()) {
                seatGroupVO.setSupplierId(suId);
                result = this.saveOrUpdateSingleGroup(seatGroupVO);
            }
        } else {
            result = this.saveOrUpdateSingleGroup(seatGroupVO);
        }

        return result;
    }

    private BusiReturnResult saveOrUpdateSingleGroup(SeatGroupVO seatGroupVO) {
        if(seatGroupVO == null) {
            logger.warn("saveOrUpdateSeatGroup --- 参数不正确, seatGroupVO == null");
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_PARAM_INVALID, false);
        }

        int sgId = seatGroupVO.getId();
        long supplierId = seatGroupVO.getSupplierId();
        if (!SessionUtils.checkInputSuIdIsValid(supplierId)) {
            logger.error("saveOrUpdateGroup -- 不能编辑其他供应商客服组, 当前登陆用户:{},操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), supplierId);
            return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_AUTH,false);
        }
        String operator = SessionUtils.getUserName();
        logger.info("saveOrUpdateGroup ,sgId: {}, supplierId: {}", sgId, supplierId);
        boolean isUpdate = sgId > 0;

        if(isUpdate) { // 编辑流程
            SeatGroup sgDB = seatGroupDao.getGroupById(sgId);
            if (sgDB == null) {
                logger.error("saveOrUpdateSeatGroup -- 组不存在, sgId:{}", sgId);
                return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_NOT_EXISTS,false);
            }

            sgDB.setName(seatGroupVO.getGroupName());
            // sgDB.setStrategy(seatGroupVO.getStrategyId());
            sgDB.setSupplierId(seatGroupVO.getSupplierId());
            sgDB.setDefaultValue(seatGroupVO.getDefaultValue());

            int num = seatGroupDao.updateSeatGroup(sgDB);
            if (num <= 0) {
                logger.error("saveOrUpdateSeatGroup -- 编辑组失败, sgId:{}", sgId);
                return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_SERVER_EXCEPTION,false);
            }
            LogUtil.doLog(LogEntity.OPERATE_UPDATE, LogEntity.ITEM_GROUP, sgId, seatGroupVO.getGroupName(), operator,
                    JacksonUtils.obj2String(seatGroupVO));
            delGroupRelationForUpdate(sgId);
        } else {
            String groupName = seatGroupVO.getGroupName();
            SeatGroup sgDB = seatGroupDao.getGroup(groupName, supplierId);
            if(sgDB != null) {
                return BusiReturnResultUtil.buildReturnResult(BusiResponseCodeEnum.FAIL_REPEAT, "组已经存在", false, null);
            }

            // 添加客服组
            SeatGroup sg = buildGroupObj(seatGroupVO, groupName, supplierId, sgId);
            sgId = seatGroupDao.saveSeatGroup(sg);
            LogUtil.doLog(LogEntity.OPERATE_INSERT, LogEntity.ITEM_GROUP, sgId, seatGroupVO.getGroupName(), operator,
                    JacksonUtils.obj2String(seatGroupVO));
            logger.info("saveOrUpdateGroup ,new sgId: {}", sgId);
        }

        addGroupRelation(seatGroupVO, sgId);

        return BusiReturnResultUtil.buildReturnResult(isUpdate ? BusiResponseCodeEnum.SUCCESS_UPDATE : BusiResponseCodeEnum.SUCCESS, true, sgId);
    }

    private void addGroupRelation(SeatGroupVO seatGroupVO, int sgId) {
        // 添加客服组所属业务
        List<Business> busiList = seatGroupVO.getBusiList();
        if (sgId > 0 && busiList != null && busiList.size() > 0) {
            saveBusiSeatGroupMapping(sgId, busiList);
        }

        // 添加组下客服
        List<Seat> seatList = seatGroupVO.getSeatList();
        if (sgId > 0 && seatList != null && seatList.size() > 0) {
            saveSeatGroupMapping(sgId, seatList);
        }
    }

    private void delGroupRelation(int sgId) {
        // 删除组下所有客服
        delGroupRelationCommon(sgId);

        // 删除组跟产品关联关系
        seatGroupDao.delGroupProductMappingByGroupId(sgId);
    }

    private void delGroupRelationForUpdate(int sgId) {
        delGroupRelationCommon(sgId);
    }

    private void delGroupRelationCommon(int sgId) {
        // 删除组下所有客服
        SeatGroupMapping sgMapping = new SeatGroupMapping();
        sgMapping.setGroupId(sgId);
        seatGroupMappingDao.delSeatGroupMapping(sgMapping);

        // 删除组所属业务
        BusiSeatGroupMapping busiSeatGroupMapping = new BusiSeatGroupMapping();
        busiSeatGroupMapping.setGroupId(sgId);
        busiSeatGroupMappingDao.delBusiSeatGroupMapping(busiSeatGroupMapping);
    }

    private SeatGroup buildGroupObj(SeatGroupVO seatGroupVO, String groupName, long supplierId, int sgId) {
        SeatGroup sg = new SeatGroup();
        sg.setId(sgId);
        sg.setName(groupName);
        sg.setSupplierId(supplierId);
        // sg.setStrategy(seatGroupVO.getStrategyId());
        sg.setDefaultValue(seatGroupVO.getDefaultValue());
        return sg;
    }

    private void saveSeatGroupMapping(int sgId, List<Seat> seatList) {
        for (Seat s : seatList) {
            SeatGroupMapping sgMapping = new SeatGroupMapping();
            sgMapping.setGroupId(sgId);
            sgMapping.setSeatId(s.getId());
            seatGroupMappingDao.saveSeatGroupMapping(sgMapping);
            logger.info("saveOrUpdateGroup - saveSeatGroupMapping ,sgId: " + sgId + ",seatId: " + s.getId());
        }
    }

    private void saveBusiSeatGroupMapping(int sgId, List<Business> busiList) {
        for (Business busi : busiList) {
            BusiSeatGroupMapping busiSeatGroupMapping = new BusiSeatGroupMapping();
            busiSeatGroupMapping.setGroupId(sgId);
            busiSeatGroupMapping.setBusiId(busi.getId());
            busiSeatGroupMappingDao.saveBusiSeatGroupMapping(busiSeatGroupMapping);
            logger.info("saveOrUpdateGroup - saveBusiSeatGroupMapping ,sgId: " + sgId + ",busiId: " + busi.getId());
        }
    }

    @Override
    public SeatGroupListVO pageQueryGroupList(GroupQueryFilter filter, int pageNum, int pageSize) {
        // 获取所有组的总数
        int totalCount = seatGroupDao.pageQueryGroupListCount(filter);
        if (totalCount <= 0) {
            return null;
        }
        SeatGroupListVO sglVO = new SeatGroupListVO();
        sglVO.setTotalCount(totalCount);
        sglVO.setPageNum(pageNum);
        sglVO.setPageSise(pageSize);
        List<SeatGroupVO> sgVOList = new ArrayList<SeatGroupVO>();

        // 分页获取所有组信息
        List<SeatGroup> groupList = seatGroupDao.pageQueryGroupList(filter,pageNum,pageSize);
        if(groupList == null) { return null; }
        for(SeatGroup sg : groupList) {
            SeatGroupVO sgVO = new SeatGroupVO();
            sgVO.setGroupName(sg.getName());
            sgVO.setSupplierId(sg.getSupplierId());
            sgVO.setSupplierName(sg.getSupplierName());
            //sgVO.setStrategyId(sg.getStrategy());
            sgVO.setDefaultValue(sg.getDefaultValue());
//            SeatSortStrategyEnum seatSortStrategyEnum = SeatSortStrategyEnum.getStrategy(sg.getStrategy());
//            if(seatSortStrategyEnum != null) {
//                sgVO.setStrategyName(seatSortStrategyEnum.getName());
//            }
            sgVO.setId(sg.getId());
            sgVO.setCreateTime(sg.getCreateTime().getTime());
            sgVOList.add(sgVO);
        }

        List<Integer> groupIds = buildGroupIds(groupList);

        try{
            // 获取组对应的业务
            getGroupBusiList(sgVOList, groupIds);
        } catch (Exception e) {
            logger.error("pageQueryGroupList -- 获取组所属业务发生异常.",e);
        }

        try{
            // 获取组下所有客服
            getGroupSeatList(sgVOList, groupIds, filter.getBusiId());
        } catch (Exception e) {
            logger.error("pageQueryGroupList -- 获取组下所有客服发生异常.",e);
        }

        Map<Integer, List<String>> gpMap = this.queryProductsByGroupIds(groupIds);
        if (gpMap != null && gpMap.size() > 0) {
            for (SeatGroupVO sgVO : sgVOList) {
                int gId = sgVO.getId();
                List<String> pidList = gpMap.get(gId);
                sgVO.setProductList(pidList);
            }
        }

        sglVO.setGroupList(sgVOList);

        return sglVO;
    }

    private void getGroupBusiList(List<SeatGroupVO> sgVOList, List<Integer> groupIds) {
        List<BusiSeatGroupMapping> bsgList = null;
        if(groupIds != null) {
            bsgList = busiSeatGroupMappingDao.getGroupBusiListByGroupId(groupIds);
        }

        Map<Integer,List<BusiSeatGroupMapping>> bsgMap = null;
        if(CollectionUtil.isNotEmpty(bsgList)) {
            bsgMap = new HashMap<>();
            for (BusiSeatGroupMapping bsg : bsgList) {
                List<BusiSeatGroupMapping> bsgmList = bsgMap.get(bsg.getGroupId());
                if(bsgmList == null) {
                    bsgmList = new ArrayList<>();
                }
                bsgmList.add(bsg);
                bsgMap.put(bsg.getGroupId(),bsgmList);
            }
        }


        if(bsgMap != null) {
            for(SeatGroupVO sgVO : sgVOList) {
                List<BusiSeatGroupMapping> bsgmList = bsgMap.get(sgVO.getId());
                if(bsgmList != null) {
                    List<Business> busiList = new ArrayList<Business>();
                    for (BusiSeatGroupMapping bsg : bsgmList) {
                        Business b = new Business();
                        b.setName(bsg.getBusiName());
                        b.setId(bsg.getBusiId());
                        busiList.add(b);
                    }
                    sgVO.setBusiList(busiList);
                }
            }
        }
    }

    private void getGroupSeatList(List<SeatGroupVO> sgVOList, List<Integer> groupIds, int busiId) {
        List<SeatAndGroup> sgList = null;
        if(groupIds != null) {
            sgList = seatDao.getSeatListByGroupIds(groupIds,busiId);
        }

        Map<Long,List<Seat>> gsMap = null;
        if(CollectionUtil.isNotEmpty(sgList)) {
            gsMap = new HashMap<>();
            for (SeatAndGroup sg : sgList) {
                Seat s = new Seat();
                s.setId(sg.getId());
                s.setQunarName(sg.getQunarName());

                List<Seat> seatList = gsMap.get(sg.getGroupId());
                if(seatList == null) {
                    seatList = new ArrayList<Seat>();
                }
                seatList.add(s);
                gsMap.put(sg.getGroupId(),seatList);
            }
        }


        if(gsMap != null) {
            for(SeatGroupVO sgVO : sgVOList) {
                Long groupId = Long.valueOf(sgVO.getId());
                List<Seat> seatList = gsMap.get(groupId);
                if(seatList != null) {
                    sgVO.setSeatList(seatList);
                }
            }
        }
    }

    private List<Integer> buildGroupIds(List<SeatGroup> groupList) {
        List<Integer> groupIdList = new ArrayList<Integer>();
        for(SeatGroup sg : groupList) {
            groupIdList.add(sg.getId());
        }
        return groupIdList;
    }

    @Override
    public int delGroupById(int groupId) {
        logger.info("delGroupById -- groupId: {}", groupId);
        SeatGroup sgDB = seatGroupDao.getGroupById(groupId);
        if(sgDB == null) {return 0;}

        if(!SessionUtils.checkInputSuIdIsValid(sgDB.getSupplierId())){
            logger.error("deleteGroup -- 不能删除其他供应商的客服组, 当前登陆管理员:{},操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), sgDB.getSupplierId());
            return BusinessResponseCodeConstants.FAIL_AUTH_OWNER;
        }

        int num = seatGroupDao.delGroupById(groupId);
        if(num > 0) {
            delGroupRelation(groupId);
            LogUtil.doLog(LogEntity.OPERATE_DELETE, LogEntity.ITEM_GROUP, groupId, null, SessionUtils.getUserName(),
                    "groupId:" + groupId + "; groupName:" + sgDB.getName() + "; supplierId:" + sgDB.getSupplierId());
        }
        logger.info("delGroupById -- groupId: {},执行记录数: {}", groupId, num);
        return num;
    }

    @Override
    public GROUPERRORCODE assignProductsInner(int groupId, List<String> pids) {
        if (groupId <= 0 ) {
            return GROUPERRORCODE.ERRCODE_GROUP_NOTFIND;
        }
        SeatGroup sg = seatGroupDao.getGroupById(groupId);
        if (sg == null) {
            return GROUPERRORCODE.ERRCODE_GROUP_NOPRODUCT;
        }

        if (null == pids || pids.isEmpty())
            return GROUPERRORCODE.ERRCODE_GROUP_OK;

        for (String pid : pids) {
            GroupProductMapping gpMapping = new GroupProductMapping();
            gpMapping.setGroupId(groupId);
            gpMapping.setPid(pid);
            seatGroupDao.saveGroupProductMapping(gpMapping);
        }

        logger.info("assignProducts success");
        return GROUPERRORCODE.ERRCODE_GROUP_OK;
    }

    @Override
    public boolean assignProducts(int groupId, String pIds) {
        logger.info("assignProducts -- groupId: {}, pIds: {}", groupId, pIds);
        if (groupId <= 0 ) {
            return false;
        }
        SeatGroup sg = seatGroupDao.getGroupById(groupId);
        if (sg == null) {
            return false;
        }

        if(!SessionUtils.checkInputSuIdIsValid(sg.getSupplierId())){
            logger.error("assignProducts -- 不能为其他供应商分配产品, 当前登陆管理员:{},操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), sg.getSupplierId());
            return false;
        }
        seatGroupDao.delGroupProductMappingByGroupId(groupId);
        if (StringUtils.isNotEmpty(pIds)) {
            List<String> pidArr = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(pIds);
            for (int i = 0; i < pidArr.size(); i++) {
                GroupProductMapping gpMapping = new GroupProductMapping();
                gpMapping.setGroupId(groupId);
                gpMapping.setPid(pidArr.get(i));
                seatGroupDao.saveGroupProductMapping(gpMapping);
            }
        }

        logger.info("assignProducts success");
        return true;
    }

    @Override
    public List<String> queryProductsInner(int groupId) {
        SeatGroup sg = seatGroupDao.getGroupById(groupId);
        if (sg == null) {
            return null;
        }

        List<GroupProductMapping> gpList = seatGroupDao.getProductListByGroupId(groupId);
        if (CollectionUtils.isEmpty(gpList)) {
            return null;
        }
        List<String> pidList = new ArrayList<>();
        for (GroupProductMapping gp : gpList) {
            pidList.add(gp.getPid());
        }
        return pidList;
    }

    @Override
    public List<String> queryProducts(int groupId) {
        SeatGroup sg = seatGroupDao.getGroupById(groupId);
        if (sg == null) {
            return null;
        }

        if(!SessionUtils.checkInputSuIdIsValid(sg.getSupplierId())){
            logger.error("queryProducts -- 不能查询其他供应商组分配产品, 当前登陆管理员:{},操作供应商:{}", SessionUtils.getLoginUser().getQunarName(), sg.getSupplierId());
            return null;
        }

        List<GroupProductMapping> gpList = seatGroupDao.getProductListByGroupId(groupId);
        if (CollectionUtils.isEmpty(gpList)) {
            return null;
        }
        List<String> pidList = new ArrayList<>();
        for (GroupProductMapping gp : gpList) {
            pidList.add(gp.getPid());
        }
        return pidList;
    }

    @Override
    public Map<Integer, List<String>> queryProductsByGroupIds(List<Integer> groupId) {
        Map<Integer, List<String>> gpMap = null;
        List<GroupProductMapping> gpMappingList = seatGroupDao.getProductListByGroupIds(groupId);
        if (CollectionUtils.isNotEmpty(gpMappingList)) {
            gpMap = new HashMap();
            for (GroupProductMapping gp : gpMappingList) {
                int gId = gp.getGroupId();
                List<String> pIdList = gpMap.get(gId);
                if (CollectionUtils.isEmpty(pIdList)) {
                    pIdList = new ArrayList<>();
                    gpMap.put(gId, pIdList);
                }
                pIdList.add(gp.getPid());
            }
        }
        return gpMap;
    }

    public enum GROUPERRORCODE{
        ERRCODE_GROUP_OK(0,"success"),
        ERRCODE_GROUP_NOTFIND(101,"groupid error"),
        ERRCODE_GROUP_NOPRODUCT(102,"productid error"),
        ;
        int code;
        String msg;

        GROUPERRORCODE(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
