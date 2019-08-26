package com.qunar.qchat.admin.constants;

import com.google.common.base.Function;

/**
 * Created by yinmengwang on 17-5-31.
 */
public class Functions {

    public static final Function<String, Long> str2Long = new Function<String, Long>() {
        @Override
        public Long apply(String s) {
            return Long.valueOf(s);
        }
    };

}
