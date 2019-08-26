package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.RobotInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RobotInfoDao {
    RobotInfo queryRobotInfoByBusiID(int busiID);

    RobotInfo queryRobotInfoByRobotID(@Param("robotID") String robotID, @Param("busiID") int busiID);

}
