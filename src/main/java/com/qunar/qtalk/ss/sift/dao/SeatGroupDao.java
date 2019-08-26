package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.SeatGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ssSeatGroupDao")
public interface SeatGroupDao {
    List<SeatGroup> selectGroupListByShopIDAndProductID(@Param("shopID")long shopID, @Param("productID")String productID);
    List<SeatGroup> selectDefaultGroupsByShopID(long shopID);

    List<Long> selectSeatGroupIdByShopIdAndProductId(@Param("shopId") Long shopId, @Param("productId") String productId);

    List<Long> selectGroupIdByShopId(@Param("shopId") Long shopId);

    List<Long> selectShopIdByProductId(@Param("productId") String productId);
}
