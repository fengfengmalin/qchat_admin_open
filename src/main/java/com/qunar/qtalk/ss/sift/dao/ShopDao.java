package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.Shop;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopDao {
    Shop selectShopById(Long shopID);

    Shop selectShopByBsiIdAndBusiSupplierId(@Param("busiId") int busiId, @Param("busiSupplierId") String busiSupplierId);

    List<String> selectBusiSupplierIds(@Param("busiId") int busiId, @Param("list") List<String> list);

    String selectShopsByBusiIds(@Param("list") List<Integer> list);
    Long selectShopIdByHotline(@Param("hotline") String hotline);

    String selectHotlineByShopId(@Param("shopId") long shopId);

    List<Shop> selectShopByBsiId(@Param("busiId") int busiId);

    List<Long> selectOtherShopIdsByIdAndQunarName(@Param("supplierId") long supplierId);

    int saveShop(Shop shop);

}
