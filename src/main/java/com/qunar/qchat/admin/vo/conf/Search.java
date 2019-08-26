package com.qunar.qchat.admin.vo.conf;

import java.util.ArrayList;
import java.util.Map;

public final class Search {

    public static String[] getKeys(String text, char startTag, char endTag) {
        String[] keys = splitKey(text, startTag, endTag);
        String[] points = new String[keys.length / 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = keys[i * 2 + 1];
        }
        return points;
    }

    public static String[] getKeys(String text, String startTag, String endTag) {
        String[] keys = splitKey(text, startTag, endTag);
        String[] points = new String[keys.length / 2];
        for (int i = 0; i < points.length; i++) {
            points[i] = keys[i * 2 + 1];
        }
        return points;
    }

    public static String replaceKey(String text, char startTag, char endTag, Map<String, ?> map) {
        return replaceKey(text, startTag + "", endTag + "", map, null);
    }

    public static String replaceKey(String text, char startTag, char endTag, Map<String, ?> map, String def) {
        return replaceKey(text, startTag + "", endTag + "", map, def);
    }

    public static String replaceKey(String text, String startTag, String endTag, Map<String, ?> map) {
        return replaceKey(text, startTag, endTag, map, null);
    }

    public static String replaceKey(String text, String startTag, String endTag, Map<String, ?> map, String def) {
        String[] arr = splitKey(text, startTag, endTag);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            if ((i & 1) != 0) {
                Object o = map.get(arr[i]);
                if (o == null) {
                    if (def != null)
                        arr[i] = def;
                    else
                        arr[i] = startTag + arr[i] + endTag;
                } else
                    arr[i] = o.toString();
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }


    /**
     * 按照前后tag将字符串拆分为数组.
     * <p>
     * 实例 : " 中华人{民共和}国" 拆分后为 ["中华人","民共和","国"]
     * <pre>
     * 约定:
     * 其中 startTag 与endTag之间的关键字必须在拆分后数组中的奇数位.
     * 填充字段的个数总是比关键字多1
     * </pre>
     *
     * @param text
     * @param startTag
     * @param endTag
     * @return
     */
    public static String[] splitKey(String text, char startTag, char endTag) {
        ArrayList<String> list = new ArrayList<String>();
        int start = 0;
        int l = text.length();
        char c;
        for (int i = 0; i < l; i++) {
            c = text.charAt(i);
            if (c == startTag || c == endTag) {
                list.add(text.substring(start, i));
                start = i + 1;
            }
        }
        list.add(text.substring(start, l));
        String[] re = new String[list.size()];
        list.toArray(re);
        return re;
    }

    /**
     * 按照前后tag将字符串拆分为数组.
     * <p>
     * 实例 : " 中华人{民共和}国" 拆分后为 ["中华人","民共和","国"]
     * <pre>
     * 约定:
     * 其中 startTag 与endTag之间的关键字必须在拆分后数组中的奇数位.
     * 填充字段的个数总是比关键字多1
     * </pre>
     *
     * @param text
     * @param startTag
     * @param endTag
     * @return
     */
    public static String[] splitKey(String text, String startTag, String endTag) {

        int index = text.indexOf(startTag);
        if (index == -1)
            return new String[]{text};

        ArrayList<String> list = new ArrayList<String>();

        int sLen = startTag.length();
        int eLen = endTag.length();
        int start = 0;
        boolean isKey = false;

        while (true) {
            list.add(text.substring(start, index));
            if (!isKey) {
                isKey = true;
                start = index + sLen;
                index = text.indexOf(endTag, start);
                if (index == -1)
                    break;
            } else {
                isKey = false;
                start = index + eLen;
                index = text.indexOf(startTag, start);
            }
            if (index == -1)
                break;
        }
        list.add(text.substring(start));
        String[] arr = new String[list.size()];
        list.toArray(arr);
        return arr;
    }


    /**
     * 将字符串以limit为分割符拆分成数组.
     * 高性能
     *
     * @param text      需要拆分的字符串
     * @param separator 分隔符
     * @param max       拆分后的数组的最大长度
     */
    public static String[] split(String text, char separator, int max) {

        ArrayList<String> list = new ArrayList<String>();

        int start = 0;
        if (max <= 0)
            max = Integer.MAX_VALUE;

        for (int i = 0, n = text.length(); i < n; i++) {
            char c = text.charAt(i);
            if (c == separator) {
                list.add((start == i) ? "" : text.substring(start, i));

                if (list.size() >= max)
                    break;
                start = i + 1;
            }
        }
        if (list.size() < max)
            list.add((start == text.length()) ? "" : text.substring(start));

        String[] re = new String[list.size()];
        list.toArray(re);
        return re;
    }

    /**
     * 将字符串以limit为分割符拆分成数组.
     * 高性能
     *
     * @param text      需要拆分的字符串
     * @param separator 分隔符
     */
    public static String[] split(String text, char separator) {
        return split(text, separator, Integer.MAX_VALUE);
    }
}