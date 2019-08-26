package com.qunar.qchat.admin.model;

/**
 * Author : mingxing.shao
 * Date : 15-10-16
 *
 */
public enum OnlineState {
    ONLINE, OFFLINE, BUSY, AWAY;

    /**
     * 获取在线状态所拥有的优先级，数越大优先级越高，当{@link OnlineState}为<code>null</code>时，优先级最低，为0
     *
     * @param onlineState 　在线状态
     * @return 优先级别
     */
    public static int maxPriority = 4;
    public static int getOnlineStatePriority(OnlineState onlineState) {
        if (onlineState == null) {
            return 0;
        }
        switch (onlineState) {
            case ONLINE:
                return 4;
            case BUSY:
                return 3;
            case AWAY:
                return 2;
            case OFFLINE:
                return 1;
            default:
                return 0;
        }
    }

    public static OnlineState of(Integer status) {
        switch (status) {
            case 4:
                return ONLINE;
            case 3:
                return BUSY;
            case 2:
                return AWAY;
            case 1:
                return OFFLINE;
            default:
                return null;
        }
    }

    public static OnlineState of(String status) {
        switch (status) {
        case "online":
            return ONLINE;
        case "busy":
            return BUSY;
        case "away":
            return AWAY;
        case "offline":
            return OFFLINE;
        default:
            return null;
        }
    }

    public static String stringValue(OnlineState onlineState){
        if (onlineState == null) {
            return "unknown";
        }
        switch (onlineState) {
            case ONLINE:
                return "online";
            case BUSY:
                return "busy";
            case AWAY:
                return "away";
            case OFFLINE:
                return "offline";
            default:
                return "unknown";
        }
    }
    public static int compare(OnlineState o1,OnlineState o2){
        int oi1 = getOnlineStatePriority(o1);
        int oi2 = getOnlineStatePriority(o2);
        return oi1 - oi2;
    }
}
