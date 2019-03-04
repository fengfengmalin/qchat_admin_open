package com.qunar.chat.common.util;


import java.text.DecimalFormat;


/**
 * Created by Administrator on 2017/7/22.
 */
public class PgTimeUtils {
    public static double getCorrectTime(String time)
    {
        DecimalFormat df = new DecimalFormat("#.###");

        if (Long.valueOf(time) >= 1000000000000L) {
            String time2 =   df.format(Double.valueOf(time)/1000);
            return Double.valueOf(time2);
        }else if (Long.valueOf(time) == 0)
        {
            return System.currentTimeMillis();
        }else {
            return Double.valueOf(time);
        }
    }

}
