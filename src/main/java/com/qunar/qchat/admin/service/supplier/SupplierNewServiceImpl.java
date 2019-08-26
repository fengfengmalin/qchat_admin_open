package com.qunar.qchat.admin.service.supplier;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.GuavaCache.SuggestCacheService;
import com.qunar.qchat.admin.dao.ISystemUserDao;
import com.qunar.qchat.admin.dao.seat.SeatNewDao;
import com.qunar.qchat.admin.dao.supplier.SupplierNewDao;
import com.qunar.qchat.admin.model.*;
import com.qunar.qchat.admin.service.ISeatService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.service.seat.GroupNewService;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.LogUtil;
import com.qunar.qchat.admin.vo.SeatOnlineState;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qchat.admin.vo.conf.JsonData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-5-26.
 */
@Service
public class SupplierNewServiceImpl implements SupplierNewService {

    @Resource
    private ISeatService seatService;
    @Resource
    private SeatNewDao seatNewDao;
    @Resource
    private SupplierNewDao supplierNewDao;
    @Resource
    private ISupplierService supplierService;
    @Resource
    private SuggestCacheService suggestCacheService;
    @Resource
    private GroupNewService groupNewService;
    @Resource
    private ISystemUserDao systemUserDao;

    private static final Function<Seat, String> getSeatName = new Function<Seat, String>() {
        @Override
        public String apply(Seat seat) {
            return seat != null ? seat.getQunarName() : null;
        }
    };
    private static final Function<SeatOnlineState, String> filterQunarName = new Function<SeatOnlineState, String>() {
        @Override
        public String apply(SeatOnlineState seatOnlineState) {
            return seatOnlineState != null ? seatOnlineState.getStrId() : null;
        }
    };

    @Override
    public List<Supplier> getAllSupplierInfo() {
        return supplierNewDao.getAllSupplierInfo();
    }

    @Override
    public JsonData supplierSuggest(String qunarName, String query) {
        if (Strings.isNullOrEmpty(qunarName) || Strings.isNullOrEmpty(query)) {
            return JsonData.error("参数错误");
        }
        long startTime = System.currentTimeMillis();

        List<SupplierVO> supplierVOs = supplierService.getSupplierBySeatQName(qunarName);
        return JsonData.success(supplierVOs);

    }

    @Override
    public JsonData seatSuggest(String qunarName, String query) {
        return null;
    }

    @Override
    public JsonData queryOrganization(String qunarName, long supplierId) {
        if (Strings.isNullOrEmpty(qunarName) || supplierId <= 0) {
            return JsonData.error("参数错误");
        }
        // 判断是否有转接所有供应商的权限，如果没有，判断客服是否属于该店铺
        if (inSupplier(qunarName, supplierId)) {
            long startTime = System.currentTimeMillis();
            List<Seat> seats = seatNewDao.querySeatBySupplierId(supplierId);
            if (CollectionUtil.isEmpty(seats)) {
                return JsonData.success(null);
            }
            List<SeatOnlineState> onlineStates = seatService.getSeatOnlineFixedStatie(supplierId,Lists.transform(seats, getSeatName));
            ImmutableMap<String, SeatOnlineState> map = Maps.uniqueIndex(onlineStates, filterQunarName);

            List<Map> result = Lists.newArrayList();
            for (Seat seat : seats) {
                String seatQName = seat.getQunarName();
                SeatOnlineState state = map.get(seatQName);
                if ((state != null && state.getOnlineState() != null
                        && state.getOnlineState() == OnlineState.OFFLINE)) {
                    continue;
                }
                Map<String, String> tmp = Maps.newHashMap();
                tmp.put("N", seatQName);
                tmp.put("W", seat.getWebName());
                tmp.put("U", seat.getQunarName());
                map.get(seat.getQunarName());
                if (state == null || state.getOnlineState() == null || state.getOnlineState() == OnlineState.OFFLINE) {
                    tmp.put("O", OnlineState.OFFLINE.name());
                } else {
                    tmp.put("O", state.getOnlineState().name());
                }
                result.add(tmp);
            }

            return JsonData.success(result);
        } else {
            return JsonData.error(qunarName + "没有权限查看" + supplierId + "的客服信息");
        }
    }

    @Override
    public List<Seat> querySeatsBySupplierId(long supplierId) {
        return null;
    }

    private boolean inSupplier(String qunarName, long supplierId) {
        List<SupplierVO> suppliers = supplierService.getSupplierBySeatQName(qunarName);
        if (CollectionUtil.isEmpty(suppliers)) {
            return false;
        }
        for (SupplierVO supplier : suppliers) {
            if (supplier.getId() == supplierId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteSuppliersByBusiIds(List<String> busiSupplierIds, BusinessEnum busiEnum) {
        if (CollectionUtil.isEmpty(busiSupplierIds) || busiEnum == null) {
            return false;
        }
        // 查询待删除的supplierId
        List<Long> supplierIds = supplierNewDao.querySupplierIds(busiSupplierIds, busiEnum.getId());
        return deleteSuppliersByIds(supplierIds);
    }

    @Override
    public boolean deleteSuppliersByIds(List<Long> supplierIds) {
        if (CollectionUtil.isEmpty(supplierIds)) {
            return false;
        }
        // 查询需更新的supplierId
        List<Long> onlineIds = supplierNewDao.filterOnlineSuppliers(supplierIds);
        if (CollectionUtil.isEmpty(onlineIds)) {
            return true;
        }
        // 更新表supplier / busi_supplier_mapping / supplier
        supplierNewDao.deleteSuppliers(onlineIds);
        // 删除管理员
        systemUserDao.delSystemUserBySupplierIds(onlineIds);
        // 删除客服组相关信息
        groupNewService.deleteGroupBySupplierIds(onlineIds);
        LogUtil.doLog(LogEntity.OPERATE_DELETE, LogEntity.ITEM_SUPPLIER, null, null, LogEntity.OPERATOR_SYSTEM,
                "supplierIds:" + Joiner.on(",").join(onlineIds));
        return true;
    }
}
