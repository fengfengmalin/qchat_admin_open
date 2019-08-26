package com.qunar.qchat.admin.service.auth;


import com.qunar.qchat.admin.service.auth.impl.DefaultAuthorization;
import com.qunar.qchat.admin.service.auth.impl.QunarAuthorization;

public class AuthFactory {
    public static IAuth getAuth(String type){
        IAuth auth = null;
        do {
            if ("qunar".equalsIgnoreCase(type)){
                auth = new QunarAuthorization();
                break;
            }
            auth = new DefaultAuthorization();
        } while (false);

        return auth;
    }
}
