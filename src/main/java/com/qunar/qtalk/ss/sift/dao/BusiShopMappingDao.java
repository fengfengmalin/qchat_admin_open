package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.BusiShopMapping;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BusiShopMappingDao {
    BusiShopMapping queryBusiShopMappingByShopID(long shopID);

    BusiShopMapping queryBusiShopMappingByBusiShopIDAndBusiID(@Param("busiID") int busiID, @Param("busiSupplierID") String busiSupplierID);

    int saveBusiShopMapping(BusiShopMapping busiShopMapping);

}
