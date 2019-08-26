package com.qunar.qchat.admin.annotation.routingdatasource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以动态切换至指定的数据源
 *
 * @see com.qunar.qchat.admin.annotation.routingdatasource.RoutingDataSourceAdvisor
 * @author chengya.li on 2014-06-23 12:30
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoutingDataSource {

    DataSources value() default DataSources.QCADMIN_MASTER;
}
