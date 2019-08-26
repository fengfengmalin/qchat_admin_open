package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.Busi;
import org.springframework.stereotype.Repository;

@Repository
public interface BusiDao {
    Busi selectBusiByID(long id);
}
