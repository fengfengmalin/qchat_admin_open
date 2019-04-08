package com.qunar.chat.dao;


import com.qunar.chat.entity.Shop;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopDao {
    Shop selectShopById(@Param("shopId")Long shopId);
    Shop selectShopByName(@Param("shopName") String shopName);
    int insertShop(@Param("name")String name);
    List<String> selectShopId(@Param("searchKey")String searchKey);
}
