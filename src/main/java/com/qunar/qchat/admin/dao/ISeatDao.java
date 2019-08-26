package com.qunar.qchat.admin.dao;

import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.util.SeatQueryFilter;

import java.util.List;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
public interface ISeatDao {

    List<SeatAndGroup> getAllSeatsWithGroupBySupplierId(long supplierId);
    List<SeatAndGroup> getAllSeatsWithGroup(String busiSupplierId, int businessId);

    List<SeatAndGroup> getSeatsWithoutGroupIds(String busiSupplierId, int businessId);

    List<SeatAndGroup> getSeatsByGroupIds(String busiSupplierId, int businessId);

    List<SeatAndBuSupplier> getSeatsByBuSuIdAndType(List<String> bSuIdAndTypeList);

    List<SeatAndGroup> getSeatListByGroupIds(List<Integer> groupIdList, int busiId);

    long saveSeat(Seat seat);

    int updateSeat(Seat seat);

    List<Seat> getSeat(String qunarName);

    List<Seat> getSeatListByQunarName(String csrName);

    Seat getSeatBySeatId(long seatId);

    List<Seat> getSeatList(long supplierId);

    List<Seat> getSeatListBySupplierName(String supplierName);

    List<Seat> getSeatListByQunarNames(List<String> qunarNames);

    List<Seat> getSeatListByPid(List<String> pidList);

    List<Seat> getAllSeatServiceStatusList();

    /**
     * 获取客服名称
     * @param supplierIds 业务线供应商编号
     * @param busiType 小于等于0 查询全部
     * @return 多个客服使用英文逗号分割
     */
    public String getSeatNameList(List<String> supplierIds, int busiType);

    /**
     * 获取多个供应商下的客服信息(不包含删除)
     * @param suIdList 供应商编号集合
     * @return
     */
    public String getSeatNameList(List<Long> suIdList);

    /**
     * 获取多个供应商下的客服信息(包含删除)，可能会有重复的qunar_name.
     * @param suIdList 供应商编号集合
     * @return
     */
    public String getAllSeatNameList(List<Long> suIdList);
    /**
     * 获取客服名称
     * @param supplierId 供应商编号
     * @return 多个客服使用英文逗号分割
     */
    public String getSeatNameStrBySupplierId(long supplierId);

    int updateSeatByQunarName(String qunarName, String newWebName);

    List<Seat> pageQuerySeatList(SeatQueryFilter filter, int pageNum, int pageSize);

    long pageQuerySeatListCount(SeatQueryFilter filter);

    List<SeatGroupBusiMapping> getGroupAndBusiListBySeatId(List<Long> seatIds);

    List<BusiSeatMapping> getSeatBusiListBySeatId(List<Long> seatIds);

    int delSeatById(long seatId);

    /**
     * 更新大于指定优先级的客服优先级, 加1.
     * @param supplierId
     * @param prePriority
     * @return
     */
    int updateAfterSeatPriority(long supplierId, int prePriority);

    int updateSeatPriority(long curSeatId, int curPriority);

    int updateSeatWxStatus(String csrName, int bindWx);

    Integer getMaxSeatPriority(long supplierId);
    
    SeatAndGroup getSeatAndGroupByQName(String qName, long supplierId);

    List<SeatAndGroup> getSeatAndGroupBySupplierId(long supplierId);
    
    List<SeatAndGroup> getSeatBySeatQNameGroup(long supplierId, String seatQName);

    List<SeatAndGroup> getSeatsWithoutGroupBySupplierId(long supplierId);

    List<Seat> querySeatListBySupplierIds(List<Long> supplierIds);
    
    List<SeatAndGroup> getSeatsWithGroupType(String busiSupplierId, int businessId, String groupType);

    String getBusiSupplieridBySupplierID(int supplierid,int businessId);


}
