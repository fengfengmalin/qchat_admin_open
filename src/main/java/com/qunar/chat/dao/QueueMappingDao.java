package com.qunar.chat.dao;


import com.qunar.chat.common.business.QueueUser;
import com.qunar.chat.entity.QueueMapping;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface QueueMappingDao {

    int updateByNameAndShopId(@Param("lastAckTime") Date lastAckTime, @Param("status") int status, @Param("customerName") String customerName, @Param("shopId") long shopId);

    Set<QueueMapping> selectNoSeatByShopId(@Param("shopId") long shopId);

    QueueUser addQueue(@Param("customerName") String customerName, @Param("shopId") long shopId, @Param("productId") String productId);

    int updateSeatName(@Param("seatId") long seatId, @Param("seatName") String seatName,
                       @Param("productId") String productId, @Param("customerName") String customerName, @Param("shopId") long shopId);

    QueueMapping selectByCustomerNameAndShopId(@Param("customerName") String customerName, @Param("shopId") long shopId);

    int updateStatusBySessionIds(@Param("status") int status, @Param("list") List<String> list);

    List<QueueMapping> selectBetweenTime(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime);

    List<QueueMapping> selectByAckTime(@Param("ackTime") Timestamp ackTime);

    List<QueueMapping> selectValidQueue(@Param("ackTime") Timestamp ackTime);

    List<QueueMapping> finishedSession(@Param("ackTime") Timestamp ackTime);

    List<QueueMapping> selectTimeoutSession(@Param("ackTime") Timestamp ackTime);

    int deleteBySessionIds(@Param("list") List<String> list);

    List<QueueMapping> selectTimeoutByStatus(@Param("ackTime") Timestamp ackTime, @Param("status") int status);

    QueueMapping closeSession(@Param("customerName") String customerName, @Param("shopId") long shopId, @Param("seatName")String seatName);

    int selectSeatServiceCount(@Param("seatName") String seatName);

}
