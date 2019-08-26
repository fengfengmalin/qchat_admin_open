package com.qunar.qchat.admin.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.qunar.qchat.admin.annotation.FilterField;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yinmengwang on 17-4-14.
 */
public class MapBeanUtil {
    private final static Logger logger = LoggerFactory.getLogger(MapBeanUtil.class);

    /**
     * Map转换为一个Bean
     * <p/>
     * 该类需要完善，需要使用apache的工具类来完善。
     *
     * @return
     */
    public static <T> T mapToBean(Class<T> clazz, Map<String, Object> map) {
        T bean = null;
        Assert.notNull(clazz, "MapBeanUtil method of  mapToBean   argument clazz  is null");
        try {
            bean = clazz.newInstance();

        } catch (Exception e) {
            logger.warn("class {} instantiation occured an  exception", clazz, e);
            return null;
        }
        //Class<T> c = (Class<T>)bean.getClass();
        for (String key : map.keySet()) {

            try {
                Field field = clazz.getDeclaredField(key);
                Object value = map.get(key);
                if (value != null) {
                    field.setAccessible(true);
                    field.set(bean, value);
                }
            } catch (Exception e) {
                logger.warn("fieldName {} set Value error", key, e);
            }
        }
        return bean;
    }

    /**
     * bean转换为Map
     *
     * @param bean
     * @return
     */
    public static <T> Map<String, Object> beanToMap(Object bean) {
        Class<?> c = bean.getClass();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getAnnotation(FilterField.class) != null || Modifier.isFinal(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String propertyName = field.getName();
                field.setAccessible(true);
                Object value = field.get(bean);
                if (value != null) {
                    returnMap.put(propertyName, value);
                }

            } catch (IllegalArgumentException e) {
                logger.warn("convert  {} type to  Map type ", bean.getClass(), e);
            } catch (IllegalAccessException e) {
                logger.warn("convert  {} type to  Map type ", bean.getClass(), e);
            }
        }

        return returnMap;
    }

    /**
     * 使用场景：
     * map中有不定量的下划线风格的key值
     * t中对应的均为驼峰风格变量名
     * 还可以优化，比如Collection中的对象，比如对象中的对象，暂时都没处理
     * @author zxuan.zhou
     * @param t
     * @param map
     * @param <T>
     * @return
     */
    public static <T> T mapToBeanSuper(T t, Map<String, Object> map) {
        Map<String, Object> extraMap = Maps.newHashMap();
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : map.keySet()) {
            if (key.contains("_")) {
                List<String> keyParts = Splitter.on("_").splitToList(key);
                String newKey = keyParts.get(0);
                for (int i = 1; i < keyParts.size(); i++) {
                    String keyPart = keyParts.get(i);
                    String newKeyPart = keyPart.substring(0, 1).toUpperCase() +
                            keyPart.substring(1, keyPart.length());
                    stringBuilder.append(newKey).append(newKeyPart);
                    // newKey += newKeyPart;
                }
                if (map.containsKey(newKey)) {
                    logger.warn("convert Map to {}, existing key pair:{},{}", t.getClass(), key, newKey);
                } else {
                    extraMap.put(newKey, map.get(key));
                }
            }
        }
        map.putAll(extraMap);
        try {
            BeanUtils.populate(t, map);
        } catch (Exception e) {
            logger.error("mapToBeanSuper error", e);
        }
        return t;
    }
}
