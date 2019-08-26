package com.qunar.qtalk.ss.consult;


import com.qunar.qtalk.ss.sift.entity.QueueMapping;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface QueueMappingDao {

    int updateByNameAndShopId(@Param("lastAckTime") Date lastAckTime, @Param("status") int status, @Param("customerName") String customerName, @Param("shopId") long shopId);

    int updateInServiceSeat(@Param("seatId") long seatId, @Param("seatName") String seatName,@Param("productId") String productId, @Param("customerName") String customerName, @Param("shopId") long shopId);

    QueueMapping selectByCustomerNameAndShopId(@Param("customerName") String customerName, @Param("shopId") long shopId);

    List<QueueMapping> selectMappingByShopId(@Param("shopId") long shopId);
}
