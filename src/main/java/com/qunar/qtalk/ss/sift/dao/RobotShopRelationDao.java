package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.RobotShopRelation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RobotShopRelationDao {

    RobotShopRelation queryRobotShopRelationByShopIDAndRobotID(@Param("shopID") long shopID, @Param("robotID") String robotID);
}
