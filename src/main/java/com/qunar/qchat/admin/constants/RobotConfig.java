package com.qunar.qchat.admin.constants;

import com.google.common.base.Strings;
import com.qunar.qchat.admin.vo.conf.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-8-16.
 */
@Component
public class RobotConfig {

    private static final Logger logger = LoggerFactory.getLogger(RobotConfig.class);

    public static boolean ROBOT_SWITCH;
    public static boolean ALLSEAT_OFFLINE_BYFORCE;
    public static int ROBOT_ALLOCATION_INTERVAL_TIME_MIN;
    private static Conf conf;

//    @QConfig("robot.properties")
    private void onChange(Map<String, String> map) {

        conf = Conf.fromMap(map);


        ROBOT_SWITCH = conf.getBoolean("robot.switch", false);
        ALLSEAT_OFFLINE_BYFORCE = conf.getBoolean("allseat.offline.byforce",false);
        ROBOT_ALLOCATION_INTERVAL_TIME_MIN = conf.getInt("robot.allocation.interval.time.min", 60);
        for (String key : map.keySet()){
            logger.info(" robot.properties changed {} - {}",
                    key,map.get(key));
        }
    }

    private static boolean getRobotSwitchByBu(final String bu) {
        if (null!=conf){
            boolean sw  = conf.getBoolean("robot.switch.subbu." + bu,false);
            return sw;
        }
        return false;
    }

    private static boolean isOpenRobot(final String bu, final long supplierid) {
        if (Strings.isNullOrEmpty(bu))
            return false;
        if (null == conf)
            return false;

        List<Long> longList = Config.getLongListQConfig("robot.switch.subbu.allow.supplierids." + bu, conf);
        return longList.contains(supplierid);
    }

    public static boolean robotEnabel(String bu, long supplierid) {

        if (!RobotConfig.getRobotSwitchByBu(bu) && !RobotConfig.isOpenRobot(bu, supplierid)) {
            return false;
        }
        return true;
    }


    public static String getRobotDefaultByBu(final String bu) {
        String webcome = "";
        if (null != conf) {
            webcome = conf.getString("robot.switch.subbu.welcome." + bu, "");
            if (Strings.isNullOrEmpty(webcome)) {
                webcome = conf.getString("robot.switch.subbu.welcome.default", "");
            }
        }
        return webcome;
    }
}


