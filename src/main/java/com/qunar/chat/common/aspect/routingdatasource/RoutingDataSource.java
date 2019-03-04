package com.qunar.chat.common.aspect.routingdatasource;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoutingDataSource {

    DataSources value() default DataSources.MASTER;
}
