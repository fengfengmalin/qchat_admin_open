package com.qunar.chat.service;

import com.qunar.chat.dao.ShopDao;
import com.qunar.chat.entity.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;



@Service
public class TestService {

    @Autowired
    ShopDao shopDao;

//    @Autowired
//    RedisUtil redisUtil;

    // @Cacheable(value = "test", key = "#p0")
    public String test() {
        Shop shop = shopDao.selectShopById(1L);
        System.out.println(shop.getName());
        sendMsg();

//        redisUtil.set("1111", "22222");
//        String o = redisUtil.get("1111");
//        System.out.println(o);
        return "success";
    }

//    @Scheduled(fixedRate = 1000)
//    protected void redisMonitor() {
//
//        System.out.println("time:" + System.currentTimeMillis());
//
//    }

    @Async("asyncTaskExecutor")
    public void sendMsg() {
        // final long t1 = System.currentTimeMillis();
        System.out.println("name:" + Thread.currentThread().getName());

    }

}
