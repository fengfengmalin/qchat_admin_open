package com.qunar.qtalk.ss.consulttag.dao;


import com.qunar.qtalk.ss.consulttag.entity.ConsultTag;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IConsultTagDao {

    int insertConsultTag(ConsultTag consultTag);

    List<ConsultTag> selectBySupplierId(@Param("supplierId") long supplierId);
}
