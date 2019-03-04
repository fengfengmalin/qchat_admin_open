package com.qunar.chat.dao;


import com.qunar.chat.entity.Shop;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopDao {
    Shop selectShopById(@Param("shopId")Long shopId);
    Shop selectShopByName(@Param("shopName") String shopName);
    int insertShop(@Param("name")String name);
}
