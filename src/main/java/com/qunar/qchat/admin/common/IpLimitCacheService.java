package com.qunar.qchat.admin.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qunar.qchat.admin.dao.IIpLimitDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by yhw on 01/12/2017.
 */
@Slf4j
@Service
public class IpLimitCacheService {

    @Autowired
    IIpLimitDao limitDao;

    private LoadingCache<String, Boolean> userCache = CacheBuilder
            .newBuilder().maximumSize(50000)
            .expireAfterWrite(16, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Boolean>() {
                @Override
                public Boolean load(String key) throws Exception {
                    int cnt  = limitDao.ipLimitCount(key);
                    return  cnt > 0;
                }
            });

    public boolean ipCheck(String ip){
        try {
            if (StringUtils.isEmpty(ip))
                return false;
            return userCache.get(ip);
        } catch (Exception e){
            log.warn("获取ip限制异常，ip : {}", ip, e);
        }
        return false;
    }



    /**
     * 清除缓存
     */
    public void refresh(String key) {
        userCache.refresh(key);
    }

}
