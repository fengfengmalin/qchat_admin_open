package com.qunar.qtalk.ss.sift.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionMappingDao {

    int selectYesterdaySessionCount(@Param("shopId") long shopId, @Param("dateString") String dateString);

    int selectYesterdayQueueCount(@Param("shopId") long shopId, @Param("dateString") String dateString);

}
