package com.qunar.qchat.admin.dao.session;

import com.qunar.qchat.admin.model.*;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


/**
 * Created by qyhw on 01/12/17.
 */
@Repository
public interface ISeatUserDao {
    int setSeatMaxUser( @Param(value = "supplier_id") int supplier_id,
                        @Param(value = "max_user") int max_user,
                        @Param(value = "qunar_name") String qunar_name
                        );

    int getSeatMaxUser( @Param(value = "supplier_id") long supplier_id,
                        @Param(value = "qunar_name") String qunar_name
    );

    int getExtFlag(@Param(value = "supplier_id") int supplier_id);

    int setExtFlag(@Param(value = "supplier_id") int supplier_id,
                   @Param(value = "status") int status
                   );


}
