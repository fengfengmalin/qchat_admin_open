package com.qunar.qchat.admin.util;

import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qchat.admin.vo.SysUserVO;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qyhw on 10/19/15.
 */
public final class SessionUtils {

    private SessionUtils(){}

	private static final ThreadLocal<HttpSession> threadLocal = new ThreadLocal<>();

    private static HttpSession getSession(){
		return threadLocal.get();
	}
    public static void setSession(HttpSession s){
		 threadLocal.set(s);
	}

    public static Object getAttribute(String key) {
		HttpSession s = getSession();
		if(s == null) { return null;}
		return s.getAttribute(key);
	}

    public static SysUserVO getLoginUser() {
        SysUserVO sysUserVO = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        return sysUserVO;
    }

    public static boolean checkInputSuIdIsValid(long suId) {
        SysUserVO sysUserVO = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        if (sysUserVO == null)
            return false;
        if (CollectionUtil.isEmpty(sysUserVO.getCurBuSuList())) {
            return false;
        }
        List<Long> suIdList = new ArrayList<>(sysUserVO.getCurBuSuList().size());
        for (SupplierVO suVO : sysUserVO.getCurBuSuList()) {
            suIdList.add(suVO.getId());
        }
        return suIdList.contains(suId);
    }

    public static String getUserName() {
        SysUserVO user = (SysUserVO) SessionUtils.getAttribute(SessionConstants.SysUser);
        if (user == null) {
            return null;
        }
        return user.getQunarName();
    }


}
