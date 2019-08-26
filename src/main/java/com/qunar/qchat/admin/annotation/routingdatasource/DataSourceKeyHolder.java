package com.qunar.qchat.admin.annotation.routingdatasource;

import java.util.LinkedList;

/**
 * Created by yinmengwang on 17-4-11.
 */
public class DataSourceKeyHolder {

    // private final static Logger logger = LoggerFactory.getLogger(DataSourceKeyHolder.class);

    public static final ThreadLocal<LinkedList<String>> holder = new ThreadLocal<LinkedList<String>>() {
        @Override
        protected LinkedList<String> initialValue() {
            return new LinkedList<String>();
        }
    };

    public static void set(String key) {
        holder.get().push(key);
    }

    public static void clear() {
        holder.get().pop();
    }

    public static void clearAll() {
        holder.get().clear();
    }

    public static String getCurrentKey() {
        if (holder.get().size() == 0)
            return null;
        return holder.get().getFirst();
    }

    public static boolean isNestedCall() {
        return holder.get().size() > 1;
    }


    /**
     * 切换数据源为指定的数据源
     * @param dataSource
     */
    public static void switchDataSource(DataSources dataSource){
        set(dataSource.key());
    }
}
