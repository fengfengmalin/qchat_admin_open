package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.vo.GroupAndSeatVO;
import com.qunar.qchat.admin.vo.SeatWithStateVO;
import com.qunar.qchat.admin.vo.SupplierAndSeatVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-10-19
 *
 */
public abstract class ASeatSortFactory implements ISeatSortFactory {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ASeatSortFactory.class);

    protected abstract List<GroupAndSeatVO> sortGroupAndSeatVO(List<GroupAndSeatVO> groupAndSeatVOs);

    protected abstract void sortBuSupplierAndSeatVO(List<SupplierAndSeatVO> buSupplierAndSeatVOList);

    @Override
    public List<GroupAndSeatVO> getSeatsWithOnlineStateList(List<SeatAndGroup> seatAndGroupList, Map<String, OnlineState> onlineStateMap) {
        if (CollectionUtil.isEmpty(seatAndGroupList)) {
            return null;
        }

        // 分组
        Map<Long, GroupAndSeatVO> groupAndSeatVOMap = buildGroupAndSeatVOMap(seatAndGroupList, onlineStateMap);

        List<GroupAndSeatVO> resultGasVOList = toList(groupAndSeatVOMap);
        return sortGroupAndSeatVO(resultGasVOList);
    }

    @Override
    public List<SupplierAndSeatVO> getSeatsWithOSListByBuSupplier(List<SeatAndBuSupplier> seatAndBuSupplierList, Map<String, OnlineState> onlineStateMap) {
        if (CollectionUtil.isEmpty(seatAndBuSupplierList)) {
            return null;
        }

        List<SupplierAndSeatVO> sasVOList = groupSeatByBuSupplier(seatAndBuSupplierList, onlineStateMap);

        // 对供应商下的客服进行排序
        sortBuSupplierAndSeatVO(sasVOList);

        return sasVOList;
    }

    private List<SupplierAndSeatVO> groupSeatByBuSupplier(List<SeatAndBuSupplier> seatAndBuSupplierList, Map<String, OnlineState> onlineStateMap) {
        Map<String,SupplierAndSeatVO> bsasMap = new HashMap<>();
        for (SeatAndBuSupplier sabs : seatAndBuSupplierList) {
            String key = sabs.getbSuIdAndTypeId();
            SupplierAndSeatVO ssVOfromMap = bsasMap.get(key);
            if(ssVOfromMap == null) {
                SupplierAndSeatVO bsasVO = new SupplierAndSeatVO();
                bsasVO.setbSuId(sabs.getbSuId());
                bsasVO.setbType(sabs.getbType());
                bsasVO.setStrategy(SeatSortStrategyEnum.POLLING_STRATEGY);
                bsasVO.setSeatWithStateVOList(new ArrayList<SeatWithStateVO>());
                bsasVO.setShopId(Supplier.SHOPID_PREFIX + sabs.getSupplierId());
                bsasVO.setsName(sabs.getSupplierName());
                bsasVO.setLogoUrl(sabs.getSupplierLogo());

                bsasMap.put(key,bsasVO);
                ssVOfromMap = bsasVO;
            }
            List<SeatWithStateVO> seatWithStateVOList = ssVOfromMap.getSeatWithStateVOList();

            SeatWithStateVO ssVO = new SeatWithStateVO();
            Seat s = new Seat();
            s.setId(sabs.getSeatId());
            String qName = sabs.getSeatName();
            s.setQunarName(qName);
            ssVO.setSeat(s);

            OnlineState onlineState = CollectionUtil.isNotEmpty(onlineStateMap) ? onlineStateMap.get(qName) : OnlineState.OFFLINE;
            ssVO.setOnlineState(onlineState);

            seatWithStateVOList.add(ssVO);
        }

        List<SupplierAndSeatVO> sasVOList = new ArrayList<>();
        for (String buSuIdAndTypeId : bsasMap.keySet()) {
            sasVOList.add(bsasMap.get(buSuIdAndTypeId));
        }
        return sasVOList;
    }

    private List<GroupAndSeatVO> toList(Map<Long, GroupAndSeatVO> groupAndSeatVOMap) {
        List<GroupAndSeatVO> resultGasVOList = new ArrayList<>();
        for (Long groupId : groupAndSeatVOMap.keySet()) {
            resultGasVOList.add(groupAndSeatVOMap.get(groupId));
        }
        return resultGasVOList;
    }

    private Map<Long, GroupAndSeatVO> buildGroupAndSeatVOMap(List<SeatAndGroup> seatAndGroupList, Map<String, OnlineState> onlineStateMap) {
        Map<Long, GroupAndSeatVO> groupAndSeatVOMap = new HashMap<>();

        for (SeatAndGroup seatAndGroup : seatAndGroupList) {
            GroupAndSeatVO groupAndSeatVO = groupAndSeatVOMap.get(seatAndGroup.getGroupId());
            List<SeatWithStateVO> groupedSeatWithStateVOList;
            if (groupAndSeatVO != null) {
                groupedSeatWithStateVOList = groupAndSeatVO.getSeatWithStateVOList();
            } else {
                groupedSeatWithStateVOList = new ArrayList<>();
                groupAndSeatVO = buildGroup(seatAndGroup, groupedSeatWithStateVOList);
                groupAndSeatVOMap.put(seatAndGroup.getGroupId(), groupAndSeatVO);
            }

            //获取不到在线状态就默认为离线
            OnlineState onlineState = CollectionUtil.isNotEmpty(onlineStateMap) ? onlineStateMap.get(seatAndGroup.getQunarName()) : OnlineState.OFFLINE;
            if(groupedSeatWithStateVOList != null) {
                Supplier supplier = null;
                if (seatAndGroup.getSupplierId() != null) {
                    supplier = new Supplier();
                    supplier.setId(seatAndGroup.getSupplierId());
                    supplier.setbType(seatAndGroup.getBusiId());
                }

                groupedSeatWithStateVOList.add(
                        new SeatWithStateVO(
                                seatAndGroup, onlineState != null ? onlineState : OnlineState.OFFLINE,
                                true
                        )
                );
            }
        }
        return groupAndSeatVOMap;
    }

    private GroupAndSeatVO buildGroup(SeatAndGroup seatAndGroup, List<SeatWithStateVO> groupedSeatWithStateVOList) {
        GroupAndSeatVO groupAndSeatVO = new GroupAndSeatVO();
        groupAndSeatVO.setGroupId(seatAndGroup.getGroupId());
        groupAndSeatVO.setGroupName(seatAndGroup.getGroupName());
        groupAndSeatVO.setStrategy(SeatSortStrategyEnum.getStrategy(seatAndGroup.getStrategy()));
        groupAndSeatVO.setSeatWithStateVOList(groupedSeatWithStateVOList);
        return groupAndSeatVO;
    }


}
