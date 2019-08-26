package com.qunar.qchat.admin.util;

import com.qunar.qchat.admin.constants.SessionConstants;
import com.qunar.qchat.admin.controller.seatselect.SelectorConfigration;
import com.qunar.qchat.admin.model.SeatAndGroup;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qchat.admin.vo.SysUserVO;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qyhw on 10/19/15.
 */
public final class SeatInfoUtil {

    private SeatInfoUtil(){}

//	private static final ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    static ThreadLocal<HashMap> threadLocal = new ThreadLocal<HashMap>();


    private static Object getSeatInfo(String key){
        HashMap<String,Object> objectHashMap = threadLocal.get();
        if (objectHashMap == null){
            return null;
        }
        return objectHashMap.get(key);
	}
    private static void setSeatInfo(String key,Object o){
        HashMap<String,Object> objectHashMap = threadLocal.get();
        if (objectHashMap == null){
            objectHashMap = new HashMap<String,Object>();
        }
        objectHashMap.put(key,o);
        threadLocal.set(objectHashMap);
	}


    /*
        protected SelectorConfigration d;

    // 中间值
    private List<SeatAndGroup> allSeat;
    private Supplier supplier;

     */
    public static SelectorConfigration getSelectorConfigragtion(){
        return (SelectorConfigration) getSeatInfo("d");
    }

    public static void setSelectorConfigragtion(SelectorConfigration d){
        setSeatInfo("d",d);
    }

    public static  List<SeatAndGroup> getAllSeat(){
        return (List<SeatAndGroup>) getSeatInfo("allSeat");
    }

    public static void setAllSeat(List<SeatAndGroup> allSeat){
        setSeatInfo("allSeat",allSeat);
    }

    public static Supplier getSupplier(){
        return (Supplier) getSeatInfo("supplier");
    }

    public static void setSupplier(Supplier s){
        setSeatInfo("supplier",s);

    }
}
