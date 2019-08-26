package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.util.SeatQueryFilter;
import com.qunar.qchat.admin.vo.*;
import com.qunar.qchat.admin.vo.conf.JsonData;
import com.qunar.qtalk.ss.utils.JID;

import java.util.List;
import java.util.Map;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
public interface ISeatService {

    /**
     * 获取坐席列表及在线状态
     *
     * @param busiSupplierId 　业务线供应商编号
     * @param businessEnum   　业务线
     * @param pid            产品编号 根据产品编号获取关联客服
     * @return 坐席列表及在线状态
     * @see BusinessEnum
     */
    List<GroupAndSeatVO> getSeatWithOnlineStateList(String busiSupplierId, BusinessEnum businessEnum, String pid);

    Map<String,List<GroupAndSeatVO>> getBatchBusiSupplierSeatsWithOnlineStatus(List<String> busiSupplierId,BusinessEnum businessEnum, String pid);

    /**
     * 获取多个供应商下的客服(轮询策略)及在线状态
     *
     * @param suList 供应商 和 业务类型
     * @return
     */
    List<SupplierAndSeatVO> getMoreSuSeatWithOnStList(List<SupplierRequestVO> suList);

    /**
     * 获取单个单个客服,为客服按钮使用
     * <br>
     * <p>会话保持策略:</p> <br>
     * 情况1: 用户没登录，按照轮询策略； <br>
     * 情况2：用户已登录，返回与用户最后一次聊天的客服，如果客服不在线，走轮询策略；
     *
     * @param busiSupplierId 业务线供应商编号
     * @param businessEnum
     * @param qunarName
     * @param pid            产品编号 根据产品编号获取关联客服
     * @return
     */
    SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineState(String busiSupplierId, BusinessEnum businessEnum,
                                                                String qunarName, String pid);

//    SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineStateNotRobot(String busiSupplierId, BusinessEnum businessEnum,
//                                                                        String qunarName, String pid);

    SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineState(String busiSupplierId, BusinessEnum businessEnum,
                                                                String qunarName, String pid, String groupType, boolean forRobot);

    /**
     * 酒店结算业务增加参数，重载了getSingleSeatWithOnlineState方法
     */
//    SeatsResultVO<SeatWithStateVO> getSingleSeatWithOnlineState(String busiSupplierId, BusinessEnum businessEnum,
//                                                                String qunarName, String pid, String groupType, CustomArgs customArgs);

    /**
     * 单纯的分配一个坐席，根绝 supplierid 和qunarName去确定 会话关系等产品信息等
     * @param supplierid 店铺id
     * @param qunarName  客人id
     * @return 坐席信息
     */
    SeatsResultVO<SeatWithStateVO> assignedOneSeat(
            String supplierid,
            String qunarName);

    /**
     * 1.使用选择器选择对应的人
     * 2.酌情创建session
     * 3.busi_session_id 与 本地 session_id 的对应关系
     *
     * @param busiSupplierId 业务线供应商编号
     * @param businessEnum   业务线代号
     * @param qunarName      客人/游客 的id,推荐带 @host
     * @param pid            产品id
//     * @param groupType      产品所对应的分组情况
//     * @param busi_session_id 业务线的事物代码，如果为空，用 func(pid,qunarName,busiSupplierId,businessEnum) 计算出个默认的
     * @return 最终分配的人员信息
     */
    SeatsResultVO<SeatWithStateVO> preAssignedOneSeat(
            String busiSupplierId,
            BusinessEnum businessEnum,
            String qunarName,
            String pid,
            String host);

    List<SeatWithStateVO> onlineSeats(String busiSupplierId, BusinessEnum businessEnum, String pid, String host);

//    SeatsResultVO<SeatWithStateVO> turnOnRealSeat(String pid,long supplierid,String userName);

    JsonData redistributionEx(long shopId, JID userQName, String pdtId, String seatQName, String host);

    void transferReadSeat(String userName,String shopid,String fromSeatName,String toSeatName);

    /**
     * 获取多个供应商下的单个客服
     *
     * @param busiSupplierIds 　各个业务线的供应商id
     * @param businessEnum    业务线id
     * @return 坐席
     */
    List<SupplierQunarNameMappingVO> getQunarNamesByBusiSupplierIds(List<String> busiSupplierIds, BusinessEnum businessEnum);

    /**
     * 查询该坐席组的列表
     *
     * @param busiSupplierId 　供应商id
     * @param businessEnum   业务线
     * @return 坐席组的列表
     */
//    List<SeatGroup> getSeatGroupList(int busiSupplierId, BusinessEnum businessEnum);

    /**
     * 添加 or 编辑客服信息
     *
     * @param seatVO
     * @return
     */
    BusiReturnResult saveOrUpdateSeat(SeatVO seatVO);

    /**
     * 根据用户中心登录名获取客服
     *
     * @param qunarName
     * @return
     */
    List<SeatVO> getSeatByQunarName(String qunarName);

    /**
     * 根据客服编号获取客服
     *
     * @param seatId
     * @return
     */
    Seat getSeatBySeatId(long seatId);

    /**
     * 根据供应商编号获取客服列表
     *
     * @param supplierId
     * @return
     */
    List<SeatVO> getSeatListBySupplierId(long supplierId);

    /**
     * 根据供应商名称获取客服列表
     *
     * @param supplierName
     * @return
     */
    List<SeatVO> getSeatListBySupplierName(String supplierName);

    /**
     * 根据QunarName去获取坐席的基本信息
     *
     * @param qunarNames QunarName的列表
     * @return Seat信息列表
     */
    List<Seat> getSeatListByQunarNames(List<String> qunarNames);

    /**
     * 获取客服名称列表
     *
     * @param supplierIds 业务线供应商编号列表
     * @param busiType    业务线类型
     * @return 客服列表, 多个客服使用英文逗号分割
     */
    String getSeatNameList(List<String> supplierIds, BusinessEnum busiType);

    Map<String, ?> getUserAndSeatInfo(List<String> qunarNames, String fields);
    Map<String, ?> getNewUserAndSeatInfo(List<String> qunarNames, String fields);

    Map<String, ?> getUserInfoByQunarNames(List<String> qunarNames, String fields);

    /**
     * 获取途家用户信息
     * @param qunarNames bnb_开头id
     * @return
     */
    List<Map<String,Object>> getBnbUserInfoByQunarNames(List<String> qunarNames);
//    List<Map<String,Object>> getHotelPreSaleUserInfoByQunarNames(List<String> qunarNames);

    int updateSeatByQunarName(String qunarName, String webName);

    int updateSeat(SeatVO seat);

    /**
     * 分页获取坐席列表
     *
     * @param filter   过滤条件
     * @param pageNum
     * @param pageSize
     * @return
     */
    SeatListVO pageQuerySeatList(SeatQueryFilter filter, int pageNum, int pageSize);

    /**
     * 删除坐席
     *
     * @param seatId
     * @return
     */
    int delSeatById(long seatId);

    /**
     * 为指定客服排序
     *
     * @param preSeatId  上一个位置的客服编号
     * @param curSeatId  当前客服编号
     * @param supplierId
     * @return
     */
    boolean sortSeat(long preSeatId, long curSeatId, long supplierId);


    SeatsResultVO<SeatWithStateVO> judgeOrRedistribute(long supplierId, String seatQName, String userQName);

    /**
     * 获取供应商下的客服信息
     */
    Map<Long, List<Seat>> getSeatListBySupplierIds(List<Long> supplierIds);

    /**
     * 添加supplierVo中的seatlist信息
     */
    List<SupplierVO> filterSeatList(List<SupplierVO> supplierVOs);

    /**
     * 分配机器人 seatList & userName 可为空 若不为空且规定时间内用户和在线客服有咨询，则返回客服id
     */
    SeatsResultVO<SeatWithStateVO> getRobotSeat(BusinessEnum businessEnum, List<SeatWithStateVO> seatList,
                                                String userName);

    /**
     * 根据内部业务线店铺id和业务线id获取对应的真实商店id
     * @param supplierID
     * @param businessEnum
     * @return
     */
    String getBusiSupplieridBySupplierID(String supplierID,BusinessEnum businessEnum);


    public List<SeatOnlineState> getSeatOnlineFixedStatie (long lSupplierid, List<String > qunarList);


    JsonData updateWxStatus(String csrName, Integer bindWx);
}
