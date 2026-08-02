package org.liwanqi42.springforge.util;

/**
 * 命名转换工具。
 *
 * <p>提供下划线与驼峰命名之间的双向转换、包路径转换、表名到映射路径转换。</p>
 */
public final class NamingUtils {

    private NamingUtils() {
    }

    /**
     * 下划线转大驼峰：user_order → UserOrder
     */
    public static String toUpperCamel(String underscoreName) {
        if (underscoreName == null || underscoreName.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = true;
        for (char c : underscoreName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * 下划线转小驼峰：user_order → userOrder
     */
    public static String toLowerCamel(String underscoreName) {
        if (underscoreName == null || underscoreName.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        boolean first = true;
        for (char c : underscoreName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (first) {
                result.append(Character.toLowerCase(c));
                first = false;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * 表名转映射路径：user_order → /user/order
     */
    public static String tableNameToMappingPath(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return "";
        }
        return "/" + tableName.toLowerCase().replace("_", "/");
    }

}
