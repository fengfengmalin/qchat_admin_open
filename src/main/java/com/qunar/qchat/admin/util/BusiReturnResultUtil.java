package com.qunar.qchat.admin.util;

import com.qunar.qchat.admin.constants.BusiResponseCodeEnum;
import com.qunar.qchat.admin.vo.BusiReturnResult;

/**
 * Created by qyhw on 12/3/15.
 */
public class BusiReturnResultUtil {

    public static BusiReturnResult buildReturnResult(BusiResponseCodeEnum brcEnum,boolean ret) {
        BusiReturnResult bReturn = new BusiReturnResult();
        bReturn.setRet(ret);
        bReturn.setCode(brcEnum.getCode());
        bReturn.setMsg(brcEnum.getMsg());
        return bReturn;
    }


    public static BusiReturnResult buildReturnResult(BusiResponseCodeEnum brcEnum,boolean ret,Object Data) {
        BusiReturnResult bReturn = new BusiReturnResult();
        bReturn.setRet(ret);
        bReturn.setCode(brcEnum.getCode());
        bReturn.setMsg(brcEnum.getMsg());
        bReturn.setData(Data);
        return bReturn;
    }

    public static BusiReturnResult buildReturnResult(BusiResponseCodeEnum brcEnum,String msg, boolean ret,Object Data) {
        BusiReturnResult bReturn = new BusiReturnResult();
        bReturn.setRet(ret);
        bReturn.setCode(brcEnum.getCode());
        bReturn.setMsg(msg);
        bReturn.setData(Data);
        return bReturn;
    }
}
