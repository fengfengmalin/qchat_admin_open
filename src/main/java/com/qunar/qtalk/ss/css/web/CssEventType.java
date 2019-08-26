package com.qunar.qtalk.ss.css.web;

import java.util.HashMap;
import java.util.Map;

public enum CssEventType {
    UNKNOWN(""),
    judgmentOrRedistributionEx("/judgmentOrRedistributionEx"),
    judgmentOrRedistribution("/judgmentOrRedistribution"),
    reloadCache("/reloadcache"),
    online("/goonline"), SayHello("/sayhello");

    private String des;
    private static Map<String, CssEventType> EVENT_MAP;

    CssEventType(String des) {
        this.des = des;
    }

    public String getDes() {
        return this.des;
    }

    public static CssEventType valueByDes(String des) {
        CssEventType type = EVENT_MAP.get(des);
        return type == null ? UNKNOWN : type;
    }

    static {
        EVENT_MAP = new HashMap(values().length);
        CssEventType[] arr$ = values();
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            CssEventType type = arr$[i$];
            EVENT_MAP.put(type.des, type);
        }

    }


}
