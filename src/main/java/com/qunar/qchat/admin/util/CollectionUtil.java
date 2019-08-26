package com.qunar.qchat.admin.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author : mingxing.shao
 * Date : 15-10-19
 *
 */
public class CollectionUtil {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionUtil.class);

    public static List<Integer> split(String fullStr, String separator) {
        if (StringUtils.isEmpty(fullStr)) {
            return null;
        }
        Iterator<String> iterator = Splitter.on(separator).omitEmptyStrings().trimResults().split(fullStr).iterator();
        List<Integer> res = new ArrayList<>();
        while (iterator.hasNext()) {
            res.add(Integer.parseInt(iterator.next()));
        }
        return res;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isAllEmptyElement(Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        for (Object obj : collection) {
            if (obj != null) {
                return false;
            }
        }
        return true;
    }

    public static void filterNull(final Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        CollectionUtils.filter(collection, new org.apache.commons.collections.Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return object != null;
            }
        });
    }

    public static <K, V, E> Map<K, V> uniqueIndex(Iterable<E> iterable, Function<E, ? extends K> keyFunc, Function<E, ? extends V> valueFunc) {
        Preconditions.checkNotNull(iterable);
        Iterator<E> iterator = iterable.iterator();
        Map<K, V> map = Maps.newHashMap();
        while (iterator.hasNext()) {
            E elem = iterator.next();
            map.put(keyFunc.apply(elem), valueFunc.apply(elem));
        }
        return map;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.size() == 0;
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
