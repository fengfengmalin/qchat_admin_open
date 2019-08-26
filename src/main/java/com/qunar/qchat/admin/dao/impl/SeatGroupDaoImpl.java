package com.qunar.qchat.admin.dao.impl;

import com.qunar.qchat.admin.dao.BaseSqlSessionDao;
import com.qunar.qchat.admin.dao.ISeatGroupDao;
import com.qunar.qchat.admin.model.GroupProductMapping;
import com.qunar.qchat.admin.model.SeatAndGroup;
import com.qunar.qchat.admin.model.SeatGroup;
import com.qunar.qchat.admin.util.CollectionUtil;
import com.qunar.qchat.admin.util.GroupQueryFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author : mingxing.shao
 * Date : 15-10-20
 *
 */
@Repository("seatGroupDao")
public class SeatGroupDaoImpl extends BaseSqlSessionDao implements ISeatGroupDao {
   // private static final Logger LOGGER = LoggerFactory.getLogger(SeatGroupDaoImpl.class);

    @Override
    public List<SeatGroup> getSeatGroup(int supplierId, int businessId) {
        Map<String,Object> args = new HashMap<>();
        args.put("supplierId",supplierId);
        args.put("businessId",businessId);
        return getReadSqlSession().selectList("GroupMapping.getGroupBySupplierAndBusi",args);
    }

    @Override
    public int saveSeatGroup(SeatGroup sg) {
        getWriteSqlSession().insert("GroupMapping.saveSeatGroup", sg);
        return sg.getId();
    }

    @Override
    public int updateSeatGroup(SeatGroup sg) {
        return getWriteSqlSession().update("GroupMapping.updateSeatGroup", sg);
    }

    @Override
    public List<SeatGroup> pageQueryGroupList(GroupQueryFilter filter, int pageNum, int pageSize) {
        Map<String, Object> map = new HashMap<>();
        buildPageQueryParams(filter, map);

        map.put("pageSize", pageSize);
        map.put("offset", (pageNum - 1) * pageSize);
        return getReadSqlSession().selectList("GroupMapping.pageQueryGroupList", map);
    }

    @Override
    public int pageQueryGroupListCount(GroupQueryFilter filter) {
        Map<String, Object> map = new HashMap<>();
        buildPageQueryParams(filter, map);
        return getReadSqlSession().selectOne("GroupMapping.pageQueryGroupListCount", map);
    }

    private void buildPageQueryParams(GroupQueryFilter filter, Map<String, Object> map) {
        if (CollectionUtil.isNotEmpty(filter.getSuIdList())) {
            map.put("suIdList", filter.getSuIdList());
        }
        if (StringUtils.isNotEmpty(filter.getGroupName())) {
            map.put("groupName", filter.getGroupName());
        }
        if (filter.getBusiId() > 0) {
            map.put("busiId", filter.getBusiId());
        }
    }

    @Override
    public int delGroupById(int groupId) {
        return getWriteSqlSession().delete("GroupMapping.delGroupById",groupId);
    }

    @Override
    public SeatGroup getGroup(String groupName, long supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId",supplierId);
        map.put("groupName",groupName);
        return getReadSqlSession().selectOne("GroupMapping.getGroup", map);
    }

    @Override
    public SeatGroup getGroupById(int id) {
        return getReadSqlSession().selectOne("GroupMapping.getGroupById", id);
    }

    @Override
    public int saveGroupProductMapping(GroupProductMapping gpMapping) {
        getWriteSqlSession().insert("GroupMapping.saveGroupProductMapping", gpMapping);
        return gpMapping.getId();
    }

    @Override
    public List<GroupProductMapping> getProductListByGroupId(int groupId) {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        return getReadSqlSession().selectList("GroupMapping.getProductListByGroupId", map);
    }

    @Override
    public List<GroupProductMapping> getProductListByGroupIds(List<Integer> groupIdList) {
        if (CollectionUtils.isEmpty(groupIdList)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("groupList", groupIdList);
        return getReadSqlSession().selectList("GroupMapping.getProductListByGroupIds", map);
    }

    @Override
    public List<SeatAndGroup> getSeatAndGroupListByPid(String pid) {
        if (StringUtils.isEmpty(pid)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        return getReadSqlSession().selectList("GroupMapping.getSeatAndGroupListByPid", map);
    }

    @Override
    public int delGroupProductMappingByGroupId(int groupId) {
        return getWriteSqlSession().delete("GroupMapping.delGroupProductMappingByGroupId", groupId);
    }
}
