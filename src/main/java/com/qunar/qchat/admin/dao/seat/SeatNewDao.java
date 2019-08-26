package com.qunar.qchat.admin.dao.seat;

import com.qunar.qchat.admin.model.Robot;
import com.qunar.qchat.admin.model.Seat;
import com.qunar.qchat.admin.model.SupplierWithRobot;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by yinmengwang on 17-5-27.
 */
@Repository
public interface SeatNewDao {

    /**
     * 获取店铺id下的所有客服信息
     */
    List<Seat> querySeatBySupplierId(@Param(value = "supplierId") long supplierId);

    /**
     * 根据qunarName查询客服信息
     */
    List<Seat> querySeatByQunarName(@Param(value = "qunarName") String qunarName);

    /**
     * 查询业务线配置的机器人id
     */
    Robot getRobotByBusinessId(@Param(value = "businessId") int businessId);

    Robot getRobotById(@Param(value ="robot_id") String robot_id);

    /**
     * 保存机器人
     */
    int saveRobotInfo(Robot robot);


    /**
     * 根据robotid精确查找robot资料
     * @param robotid
     * @return
     */
    Robot getRobotByRobotid(@Param(value = "robotid") String robotid);
    Robot getRobotWithConfigById(@Param(value = "robotid") String robotid);

    List<SupplierWithRobot> qunarSupplierWithRobot (@Param(value = "robotname") String robotname,
                                                    @Param(value = "supplierid") long supplierid);

    boolean insertSupplierRobotConfig(SupplierWithRobot supplierWithRobot);

    boolean updateSupplierRobotConfig(SupplierWithRobot supplierWithRobot);
}
