package com.qunar.chat.dao;


import com.qunar.chat.entity.QtUnSentMessage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnSentMessageDao {

    List<QtUnSentMessage> selectByCustomerNameAndShopId(@Param("customerName") String customerName, @Param("shopId") long shopId);

    int deleteUnSentMessages(@Param("list") List<String> list);

    int insertNoneRealtoMessage(@Param("customerName") String customerName, @Param("shopId") long shopId, @Param("message")String message);
}
