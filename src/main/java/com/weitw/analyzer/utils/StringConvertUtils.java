package com.weitw.analyzer.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义的一些字符串转换方法 工具类
 *
 * @author weitw
 * @date 2023/06/16
 */
public class StringConvertUtils {

    /**
     * 根据指定分隔符拼接多个字符串
     *
     * @param separator 分隔符，null时表示直接拼接，不适用分隔符
     * @param strings   字符串
     * @return {@link String}
     */
    public static String concatStringBySeparator(String separator, String... strings) {
        if (strings != null && strings.length > 0) {
            separator = separator == null ? "" : separator;
            List<String> list = new ArrayList<>();
            for (String string : strings) {
                if (StringUtils.isNotBlank(string)) {
                    list.add(string);
                }
            }
            return String.join(separator, list);
        }
        return "";
    }

    public static void main(String[] args) {
        System.out.println(concatStringBySeparator("","a"));
    }
}
