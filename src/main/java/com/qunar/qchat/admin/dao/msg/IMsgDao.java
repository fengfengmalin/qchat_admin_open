package com.qunar.qchat.admin.dao.msg;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 01/12/17.
 */
@Repository
public interface IMsgDao {

    String getLastSeatNameWithShopIdBefore(@Param("uname") String uname, @Param("virturalids") List<String> virtualIds,@Param("snames") List<String> seatNames,@Param("interval") int interval);

    String getLastSeatNameWithShopIdEx(@Param("uname") String uname, @Param("virturalids") List<String> virtualIds,@Param("snames") List<String> seatNames);

    List<Map<String,Object>> getLestConversationTime(@Param("uname") String uname, @Param("snames") List<String> seatNames);

}

