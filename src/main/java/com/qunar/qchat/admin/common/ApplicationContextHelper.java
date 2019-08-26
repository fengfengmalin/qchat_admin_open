package com.qunar.qchat.admin.common;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by yinmengwang on 17-3-24.
 */
public class ApplicationContextHelper implements ApplicationContextAware{
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }

    public static Object getBean(String name) {
        if(applicationContext == null) return null;
        return applicationContext.getBean(name);
    }

    @Deprecated //use popBean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object getBean(Class clazz) {
        if(applicationContext == null) return null;
        return applicationContext.getBean(clazz);
    }

    /**
     * 原来的getBean需要废弃了。使用popBean避免强制对象转换.
     * 请使用静态导入，以避免使用ApplicationContextHelper的前置引用。即：
     * ApplicationContextHelper.popBean。使用静态导入更加输入的直接：
     * MyService service = popBean(MyService.class);
     * 更加简洁明了
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T popBean(Class<T> clazz) {
        if(applicationContext == null) return null;
        return applicationContext.getBean(clazz);
    }

    /**
     * 当通过接口多个实现时，需要用 beanName区分
     *
     * @param name 注册的 bean名称
     * @param clazz 注册的 bean类型
     * @param <T>
     * @return
     */
    public static <T> T popBean(String name, Class<T> clazz) {
        if(applicationContext == null) return null;
        return applicationContext.getBean(name, clazz);
    }


    /**
     * 获取所有这个类的实例
     *
     * @param clazz 注册的 bean类型
     * @param <T>
     * @return
     */
    public static <T> Collection<T> popBeanList(Class<T> clazz) {
        if(applicationContext == null) return null;
        Map<String, T> beansOfType = applicationContext.getBeansOfType(clazz);
        if(MapUtils.isEmpty(beansOfType)) return Collections.emptyList();
        return beansOfType.values();
    }

}
