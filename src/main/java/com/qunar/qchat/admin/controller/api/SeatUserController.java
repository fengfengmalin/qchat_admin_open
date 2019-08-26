package com.qunar.qchat.admin.controller.api;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.dao.session.ISeatUserDao;
import com.qunar.qchat.admin.vo.conf.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by MSI on 2018/4/20.
 */
@Slf4j
@Controller
@RequestMapping(value = "/stapi/")
public class SeatUserController {

    @Resource
    private ISeatUserDao iSeatUserDao;

    @RequestMapping(value = "/setmaxuser.json")
    @ResponseBody
    public JsonData setMaxUser(@RequestParam(value = "sid") int sid,
                            @RequestParam(value = "muser") int muser,
                               @RequestParam(value = "qname") String qname,
                               HttpServletRequest request) {

        if (Strings.isNullOrEmpty(qname)){
            return JsonData.error("参数错误");
        }

        try{
            int i = iSeatUserDao.setSeatMaxUser(sid,muser,qname);
            if (i == 0){
                return JsonData.error("update 0 row",2);
            }
        }catch (Exception e){

            return JsonData.error("系统错误");
        }
        return JsonData.success();
    }

    @RequestMapping(value = "/getmaxuser.json")
    @ResponseBody
    public JsonData getMaxUser(@RequestParam(value = "sid") int sid,
                               @RequestParam(value = "qname") String qname,
                               HttpServletRequest request) {

        if (Strings.isNullOrEmpty(qname)){
            return JsonData.error("参数错误");
        }

        try{
            iSeatUserDao.getSeatMaxUser(sid,qname);
            return JsonData.success();

        }catch (Exception e){

            return JsonData.error("系统错误");
        }
    }


    @RequestMapping(value = "/getextflag.json")
    @ResponseBody
    public JsonData getExtFlag(@RequestParam(value = "sid") int sid,
                               HttpServletRequest request) {

        try {
            int i = iSeatUserDao.getExtFlag(sid);
            return JsonData.success(0,Integer.toBinaryString(i));

        }catch (Exception e){
            return JsonData.error("getExtFlag Error",1);
        }
    }

    @RequestMapping(value = "/setextflag.json")
    @ResponseBody
    public JsonData setExtFlag(@RequestParam(value = "sid") int sid,
                               @RequestParam(value = "eflag") int eflag,
                               HttpServletRequest request) {

        try {
            if (eflag != 0){
                eflag = 1;
            }

            int i = iSeatUserDao.setExtFlag(Integer.valueOf(sid),eflag);
            if (i == 0){
                return  JsonData.error("update 0 row",2);
            }

        }catch (Exception e){
            return JsonData.error("getExtFlag Error",1);
        }
        return JsonData.success();
    }




}
