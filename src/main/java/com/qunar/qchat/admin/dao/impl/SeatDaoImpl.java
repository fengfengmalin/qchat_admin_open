package com.qunar.qchat.admin.dao.impl;

import com.google.common.collect.Maps;
import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISeatDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.SeatQueryFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
@Repository("seatDao")
public class SeatDaoImpl extends BaseSqlSessionDao implements ISeatDao {
    // private static final Logger LOGGER = LoggerFactory.getLogger(SeatDaoImpl.class);

    @Override
    public List<SeatAndGroup> getAllSeatsWithGroupBySupplierId(long supplierId) {
        Map<String, Object> args = new HashMap<>();
        args.put("supplierId", supplierId);
        return getReadSqlSession().selectList("SeatMapping.getAllSeatsWithGroupBySupplierId", args);
    }

    @Override
    public List<SeatAndGroup> getAllSeatsWithGroup(String busiSupplierId, int businessId) {
        Map<String, Object> args = new HashMap<>();
        args.put("busiSupplierId", busiSupplierId);
        args.put("businessId", businessId);
        return getReadSqlSession().selectList("SeatMapping.getAllSeatsWithGroup", args);
    }

    @Override
    public List<SeatAndGroup> getSeatsWithoutGroupIds(String busiSupplierId, int businessId) {
        Map<String, Object> args = new HashMap<>();
        args.put("busiSupplierId", busiSupplierId);
        args.put("businessId", businessId);
        return getReadSqlSession().selectList("SeatMapping.getSeatListWithoutGroup", args);
    }

    @Override
    public List<SeatAndGroup> getSeatsByGroupIds(String busiSupplierId, int businessId) {
        Map<String, Object> args = new HashMap<>();
        args.put("busiSupplierId", busiSupplierId);
        args.put("businessId", businessId);
        return getReadSqlSession().selectList("SeatMapping.getSeatListByGroup", args);
    }

    @Override
    public List<SeatAndBuSupplier> getSeatsByBuSuIdAndType(List<String> bSuIdAndTypeList) {
        Map<String, Object> args = new HashMap<>();
        args.put("bSuIdAndTypeList", bSuIdAndTypeList);
        return getReadSqlSession().selectList("SeatMapping.getSeatsByBuSuIdAndType", args);
    }

    @Override
    public List<SeatAndGroup> getSeatListByGroupIds(List<Integer> groupIdList, int busiId) {
        Map<String, Object> args = new HashMap<>();
        args.put("busiId", busiId);
        args.put("groupList", groupIdList);
        return getReadSqlSession().selectList("SeatMapping.getSeatListByGroupIds", args);
    }

    @Override
    public long saveSeat(Seat seat) {
        if (seat.getMaxSessions() == null)
            seat.setMaxSessions(10);
        getWriteSqlSession().insert("SeatMapping.saveSeat", seat);
        return seat.getId();
    }

    @Override
    public int updateSeat(Seat seat) {
        return getWriteSqlSession().update("SeatMapping.updateSeat", seat);
    }

    @Override
    public List<Seat> getSeat(String qunarName) {
        return getReadSqlSession().selectList("SeatMapping.getSeatByQunarName", qunarName);
    }

    @Override
    public List<Seat> getSeatListByQunarName(String qunarName) {
        Map<String, Object> args = new HashMap<>();
        args.put("qunarName", qunarName);
        return getReadSqlSession().selectList("SeatMapping.getSeatListByQunarName", args);
    }

    @Override
    public Seat getSeatBySeatId(long seatId) {
        return getReadSqlSession().selectOne("SeatMapping.getSeatBySeatId", seatId);
    }

    @Override
    public List<Seat> getSeatList(long supplierId) {
        return getReadSqlSession().selectList("SeatMapping.getSeatListBySupplierId", supplierId);
    }

    @Override
    public List<Seat> getAllSeatServiceStatusList() {
        return getReadSqlSession().selectList("SeatMapping.getAllSeatServiceStatusList");
    }

    @Override
    public String getSeatNameList(List<String> supplierIds, int busiType) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierIds", supplierIds);
        if(busiType > 0) {
            map.put("busiType", busiType);
        }
        if(CollectionUtil.isEmpty(supplierIds)) {
            return getReadSqlSession().selectOne("SeatMapping.getSeatNameListByBusiType", map);
        }
        return getReadSqlSession().selectOne("SeatMapping.getSeatNameListByBSuIds", map);
    }

    @Override
    public String getSeatNameList(List<Long> suIdList) {
        if (CollectionUtil.isEmpty(suIdList)) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("suIdList", suIdList);
        return getReadSqlSession().selectOne("SeatMapping.getSeatNameListBySuIds", map);
    }

    @Override
    public String getAllSeatNameList(List<Long> suIdList) {
        if (CollectionUtil.isEmpty(suIdList)) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("suIdList", suIdList);
        return getReadSqlSession().selectOne("SeatMapping.getAllSeatNameListBySuIds", map);
    }

    @Override
    public String getSeatNameStrBySupplierId(long supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        return getReadSqlSession().selectOne("SeatMapping.getSeatNameStrBySupplierId", map);
    }

    @Override
    public List<Seat> getSeatListBySupplierName(String supplierName) {
        return getReadSqlSession().selectList("SeatMapping.getSeatListBySupplierName", supplierName);
    }

    @Override
    public List<Seat> getSeatListByQunarNames(List<String> qunarNames) {
        return getReadSqlSession().selectList("SeatMapping.getSeatListByQunarNames", qunarNames);
    }

    @Override
    public List<Seat> getSeatListByPid(List<String> pidList) {
        return getReadSqlSession().selectList("SeatMapping.getSeatListByPid", pidList);
    }

    @Override
    public int updateSeatByQunarName(String qunarName, String newWebName) {
        Map<String, Object> args = new HashMap<>();
        args.put("qunarName", qunarName);
        args.put("webName", newWebName);
        return getWriteSqlSession().update("SeatMapping.updateSeatByQunarName", args);
    }

    @Override
    public List<Seat> pageQuerySeatList(SeatQueryFilter filter, int pageNum, int pageSize) {
        Map<String, Object> map = new HashMap<>();
        buildPageQueryParams(filter, map);

        map.put("pageSize", pageSize);
        map.put("offset", (pageNum - 1) * pageSize);
        return getReadSqlSession().selectList("SeatMapping.pageQuerySeatList", map);
    }

    @Override
    public long pageQuerySeatListCount(SeatQueryFilter filter) {
        Map<String, Object> map = new HashMap<>();
        buildPageQueryParams(filter, map);
        return getReadSqlSession().selectOne("SeatMapping.pageQuerySeatListCount", map);
    }

    private void buildPageQueryParams(SeatQueryFilter filter, Map<String, Object> map) {
        if (CollectionUtil.isNotEmpty(filter.getSuIdList())) {
            map.put("suIdList", filter.getSuIdList());
        }
        if (StringUtils.isNotEmpty(filter.getQunarName())) {
            map.put("qunarName", filter.getQunarName());
        }
        if (StringUtils.isNotEmpty(filter.getWebName())) {
            map.put("webName", filter.getWebName());
        }
        if (filter.getBusiId() > 0) {
            map.put("busiId", filter.getBusiId());
        }
        if (StringUtils.isNotEmpty(filter.getBySort())) {
            map.put("sort", filter.getBySort());
        }
    }

    @Override
    public List<SeatGroupBusiMapping> getGroupAndBusiListBySeatId(List<Long> seatIds) {
        return getReadSqlSession().selectList("SeatMapping.getGroupAndBusiListBySeatId", seatIds);
    }

    @Override
    public List<BusiSeatMapping> getSeatBusiListBySeatId(List<Long> seatIds) {
        return getReadSqlSession().selectList("SeatMapping.getSeatBusiListBySeatId", seatIds);
    }

    @Override
    public int delSeatById(long seatId) {
        return getWriteSqlSession().update("SeatMapping.delSeatById", seatId);
    }

    @Override
    public int updateAfterSeatPriority(long supplierId, int prePriority) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        map.put("prePriority", prePriority);
        return getWriteSqlSession().update("SeatMapping.updateAfterSeatPriority", map);
    }

    @Override
    public int updateSeatPriority(long curSeatId, int curPriority) {
        Map<String, Object> map = new HashMap<>();
        map.put("seatId", curSeatId);
        map.put("priority", curPriority);
        return getWriteSqlSession().update("SeatMapping.updateSeatPriority", map);
    }

    @Override
    public int updateSeatWxStatus(String csrName, int bindWx) {
        Map<String, Object> map = new HashMap<>();
        map.put("csrName", csrName);
        map.put("bindWx", bindWx);
        return getWriteSqlSession().update("SeatMapping.updateSeatWxStatus", map);
    }
    //    @Override
//    public List<SeatAndGroup> getSeatWithGroupList(int supplierId, int businessId, List<Integer> groupIdList) {
//        Map<String, Object> args = new HashMap<>();
//        args.put("supplierId", supplierId);
//        args.put("businessId", businessId);
//        args.put("groupIdList",groupIdList);
//        return getReadSqlSession().selectList("SeatMapping.getSeatWithGroupList", args);
//    }


    @Override
    public Integer getMaxSeatPriority(long supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        return getReadSqlSession().selectOne("SeatMapping.getMaxSeatPriority", map);
    }

    @Override
    public SeatAndGroup getSeatAndGroupByQName(String qName, long supplierId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("qunarName", qName);
        map.put("supplierId", supplierId);
        return getReadSqlSession().selectOne("SeatMapping.getSeatAndGroupByQName", map);
    }

    @Override
    public List<SeatAndGroup> getSeatAndGroupBySupplierId(long supplierId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("supplierId", supplierId);
        return getReadSqlSession().selectList("SeatMapping.getSeatAndGroupBySupplierId", map);
    }

    @Override
    public List<SeatAndGroup> getSeatBySeatQNameGroup(long supplierId, String seatQName) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("supplierId", supplierId);
        map.put("seatQName", seatQName);
        return getReadSqlSession().selectList("SeatMapping.getSeatBySeatQNameGroup", map);
    }

    @Override
    public List<SeatAndGroup> getSeatsWithoutGroupBySupplierId(long supplierId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("supplierId", supplierId);
        return getReadSqlSession().selectList("SeatMapping.getSeatsWithoutGroupBySupplierId", map);
    }

    public List<Seat> querySeatListBySupplierIds(List<Long> supplierIds) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("supplierIds", supplierIds);
        return getReadSqlSession().selectList("SeatMapping.querySeatListBySupplierIds", map);
    }

    @Override
    public List<SeatAndGroup> getSeatsWithGroupType(String busiSupplierId, int businessId, String groupType) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("busiSupplierId", busiSupplierId);
        map.put("businessId", businessId);
        map.put("groupType", groupType);
        return getReadSqlSession().selectList("SeatMapping.getSeatsWithGroupType", map);
    }

    /**
     * 根据内部店铺id反查真实的店铺id
     * @param supplierid
     * @return
     */
    @Override
    public String getBusiSupplieridBySupplierID(int supplierid,int businessId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("supplierid", supplierid);
        map.put("businessId", businessId);
        return getReadSqlSession().selectOne("SeatMapping.getBusiSupplieridBySupplierID", map);
    }


}
