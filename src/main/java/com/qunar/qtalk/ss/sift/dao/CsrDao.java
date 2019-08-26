package com.qunar.qtalk.ss.sift.dao;

import com.qunar.qchat.admin.model.Seat;
import com.qunar.qtalk.ss.sift.entity.CSR;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CsrDao {
    List<CSR> selectCsrsByCsrIDs(List<Long> seatIDs);

    List<CSR> selectOnlineCsrsByShopID(@Param("shopId") long shopId, @Param("host") String host);

    List<CSR> selectCsrsByShopIdWithoutCarName(@Param("shopId")long shopID, @Param("csrName")String csrName, @Param("host") String host);
    List<CSR> selectCsrsByCsrName(@Param("qunarName")String qunarName, @Param("host") String host);

    List<CSR> selectCsrsByGroupIDs(@Param("list")List<Long> list , @Param("host") String host);

    List<CSR> selectCsrByCsrNameAndShopId(@Param("qunarName") String qunarName, @Param("shopId") long shopId, @Param("host") String host);

    List<CSR> selectCsrsByCsrBusiIdAndHost(@Param("busiId") int busiId, @Param("host") String host);

    int saveSeat(Seat seat);

    int updateSeatByShopId(@Param("shopId")long shopId);

    int updateSeatStatusByShopIdAndName(@Param("shopId")long shopId, @Param("qunarName") String qunarName);

    List<CSR> selectCsrByCsrNameAndShopIdWithoutStatus(@Param("qunarName") String qunarName, @Param("shopId") long shopId);
}
