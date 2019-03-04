package com.qunar.chat.dao;


import com.qunar.chat.entity.Seat;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatDao {

    List<Seat> selectOnlineSeatsByShopID(@Param("shopId") long shopId, @Param("host") String host);

    List<Seat> selectSeatBySeatNameAndShopId(@Param("qunarName") String qunarName, @Param("shopId") long shopId, @Param("host") String host);

    int saveSeat(@Param("qunarName") String qunarName, @Param("supplierId") long supplierId, @Param("host") String host,
                 @Param("webName") String webName, @Param("serviceStatus") int serviceStatus, @Param("maxSessions") int maxSessions);
}
