package com.qunar.qchat.admin.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.common.ApplicationContextHelper;

import com.qunar.qchat.admin.model.Robot;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.model.qchat.QChatConstant;
import com.qunar.qchat.admin.service.IRobotService;
import com.qunar.qchat.admin.service.ISupplierService;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qtalk.ss.sift.enums.shop.AssignStragegy;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by qyhw on 10/30/15.
 */
public class SupplierServiceUtil {

    public static SupplierVO modelToVO(Supplier s) {
        if(s == null) {return null;}
        SupplierVO vo = new SupplierVO();

        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setBusiType(s.getbType());
        vo.setStatus(s.getStatus());
        vo.setExt_flag(s.getBQueue());
        vo.setBusiSupplierId(s.getBusiSupplierId());
        if (s.getCreateDate() != null) {
            vo.setCreateDate(s.getCreateDate().getTime());
        }
        vo.setAssignStrategy(s.getAssignStrategy());
        return vo;
    }

    static Supplier voToModel(SupplierVO vo) {
        if(vo == null) {return null;}
        Supplier s = new Supplier();

        s.setId(vo.getId());
        s.setName(vo.getName());
        return s;
    }



    public static List<Map<String,Object>> buildRobotInfo(List<String> robotids){
        if (CollectionUtil.isEmpty(robotids)) {
            return null;
        }
        List<Map<String, Object>> result = Lists.newArrayList();
        IRobotService supplierService = ApplicationContextHelper.popBean(IRobotService.class);

        for (String robotid : robotids ) {
            String id  = robotid.replace(QChatConstant.QCHAT_HOST_POSTFIX,"")
                    .replace(QChatConstant.QTALK_DOMAIN_POSTFIX,"");
            Robot robot = supplierService.getRobotById(id);
            if (null!=robot){
                Map<String, Object> anonyInfo = Maps.newHashMap();
                anonyInfo.put("username", robotid);
                anonyInfo.put("loginName", robotid);
                anonyInfo.put("nickname", robot.getRobotName());
                anonyInfo.put("uid",0);
                anonyInfo.put("mobile","");
                anonyInfo.put("prenum","");
                anonyInfo.put("status",1);
                anonyInfo.put("type",1);
                anonyInfo.put("regip",0);
                anonyInfo.put("regtime",0);
                anonyInfo.put("loginip",0);
                anonyInfo.put("logintime",0);
                anonyInfo.put("appflag",0);
                anonyInfo.put("email","");
                anonyInfo.put("emailverified",0);
                anonyInfo.put("quickFlag","");
                anonyInfo.put("credit",0);
                anonyInfo.put("mobileverified",1);
                anonyInfo.put("gender",0);
                anonyInfo.put("birthday","");
                anonyInfo.put("imageurl",robot.getImageurl());
                anonyInfo.put("tempnickname","");
                anonyInfo.put("oauth","");
                anonyInfo.put("pwdType",3);
                anonyInfo.put("suppliername","");
                result.add(anonyInfo);
            }
        }
        return result;
    }

    public static List<Map<String,Object>> buildRobotInfoWithConfig(List<String> robotids){
        if (CollectionUtil.isEmpty(robotids)) {
            return null;
        }
        List<Map<String, Object>> result = Lists.newArrayList();
        IRobotService supplierService = ApplicationContextHelper.popBean(IRobotService.class);

        for (String robotid : robotids ) {
            String id  = robotid.replace(QChatConstant.QCHAT_HOST_POSTFIX,"")
                    .replace(QChatConstant.QTALK_DOMAIN_POSTFIX,"");
            Robot robot = supplierService.getRobotWithConfigById(id);
            if (null!=robot){
                Map<String, Object> anonyInfo = Maps.newHashMap();
                anonyInfo.put("username", robotid);
                anonyInfo.put("loginName", robotid);
                anonyInfo.put("nickname", robot.getRobotName());
                anonyInfo.put("webname", robot.getRobotName());
                anonyInfo.put("uid",0);
                anonyInfo.put("displaytype",robot.getDisplayType());
                anonyInfo.put("mobile","");
                anonyInfo.put("prenum","");
                anonyInfo.put("status",1);
                anonyInfo.put("type",2);
                anonyInfo.put("regip",0);
                anonyInfo.put("regtime",0);
                anonyInfo.put("loginip",0);
                anonyInfo.put("logintime",0);
                anonyInfo.put("appflag",0);
                anonyInfo.put("email","");
                anonyInfo.put("emailverified",0);
                anonyInfo.put("quickFlag","");
                anonyInfo.put("credit",0);
                anonyInfo.put("mobileverified",1);
                anonyInfo.put("gender",0);
                anonyInfo.put("birthday","");
                anonyInfo.put("imageurl",robot.getImageurl());
                anonyInfo.put("tempnickname","");
                anonyInfo.put("oauth","");
                anonyInfo.put("pwdType",3);
                anonyInfo.put("suppliername","");
                anonyInfo.put("uType",Supplier.ROBOT);
                result.add(anonyInfo);
            }
        }
        return result;
    }

    public static List<Map<String, Object>> buildSupplierInfo(List<Long> shopIds) {
        if (CollectionUtil.isEmpty(shopIds)) {
            return null;
        }
        List<Map<String, Object>> result = Lists.newArrayList();
        ISupplierService supplierService = ApplicationContextHelper.popBean(ISupplierService.class);
        List<Supplier> suppliers = supplierService.getSupplierByIds(shopIds);
        if (CollectionUtils.isEmpty(suppliers)) {
            return result;
        }
        for (Supplier supplier : suppliers) {
            Map<String, Object> map = MapBeanUtil.beanToMap(supplier);
            map.put("shopId", Supplier.SHOPID_PREFIX + supplier.getId());
            map.put("uType", Supplier.SHOP);
            result.add(map);
        }
        return result;
    }
}
