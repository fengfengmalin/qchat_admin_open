package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.BusinessEnum;
import com.qunar.qchat.admin.model.Robot;
import com.qunar.qchat.admin.model.SupplierWithRobot;

/**
 * Created by yinmengwang on 17-8-17.
 */
public interface IRobotService {

    /**
     * 保存机器人
     */
    boolean saveRobot(Robot robot);

    /**
     * 根据业务线id查找机器人
     */
    Robot getRobotByBusiness(BusinessEnum businessEnum);


    Robot getRobotByRobotid(String robotid);

    Robot getRobotById(String robotid);
    Robot getRobotWithConfigById(String robotid);

    boolean updateOrInsertSupplierRobotConfig(SupplierWithRobot supplierWithRobot);

    SupplierWithRobot getRobotConfig(String robotname,long supplierid);
}
