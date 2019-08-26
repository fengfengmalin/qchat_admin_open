package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qtalk.ss.sift.entity.HotlineSupplierMapping;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotlineSupplierMappingDao {

    int insertHotlineSeat(HotlineSupplierMapping hotlineSupplierMapping);

    int updateHotlineSeat(@Param("hotline") String hotline, @Param("id") long id);

    int deleteById(long Id);

    String selectHotlineBySupplierId(@Param("supplierId")long supplierId);

    List<String> selectHotlineByQunarNameAndHost(@Param("qunarName")String qunarName, @Param("host")String host);

}