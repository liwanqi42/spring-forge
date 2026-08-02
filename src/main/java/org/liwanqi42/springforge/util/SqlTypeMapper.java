package org.liwanqi42.springforge.util;

import java.util.Map;

/**
 * JDBC 类型到 Java 类型的映射工具。
 *
 * <p>覆盖 MySQL 常用数据类型。</p>
 */
public final class SqlTypeMapper {

    private SqlTypeMapper() {
    }

    /**
     * Java 类型信息记录。
     */
    public record JavaTypeInfo(String fullyQualified, String shortName, String boxedName) {
    }

    private static final Map<String, JavaTypeInfo> TYPE_MAP = Map.<String, JavaTypeInfo>ofEntries(
            // 字符串
            Map.entry("VARCHAR", new JavaTypeInfo("java.lang.String", "String", "String")),
            Map.entry("CHAR", new JavaTypeInfo("java.lang.String", "String", "String")),
            Map.entry("TEXT", new JavaTypeInfo("java.lang.String", "String", "String")),
            Map.entry("MEDIUMTEXT", new JavaTypeInfo("java.lang.String", "String", "String")),
            Map.entry("LONGTEXT", new JavaTypeInfo("java.lang.String", "String", "String")),
            // 整数
            Map.entry("TINYINT", new JavaTypeInfo("java.lang.Integer", "Integer", "Integer")),
            Map.entry("SMALLINT", new JavaTypeInfo("java.lang.Integer", "Integer", "Integer")),
            Map.entry("INT", new JavaTypeInfo("java.lang.Integer", "Integer", "Integer")),
            Map.entry("INTEGER", new JavaTypeInfo("java.lang.Integer", "Integer", "Integer")),
            Map.entry("BIGINT", new JavaTypeInfo("java.lang.Long", "Long", "Long")),
            // 浮点
            Map.entry("FLOAT", new JavaTypeInfo("java.math.BigDecimal", "BigDecimal", "BigDecimal")),
            Map.entry("DOUBLE", new JavaTypeInfo("java.math.BigDecimal", "BigDecimal", "BigDecimal")),
            Map.entry("DECIMAL", new JavaTypeInfo("java.math.BigDecimal", "BigDecimal", "BigDecimal")),
            // 日期时间
            Map.entry("DATE", new JavaTypeInfo("java.time.LocalDate", "LocalDate", "LocalDate")),
            Map.entry("TIME", new JavaTypeInfo("java.time.LocalTime", "LocalTime", "LocalTime")),
            Map.entry("DATETIME", new JavaTypeInfo("java.time.LocalDateTime", "LocalDateTime", "LocalDateTime")),
            Map.entry("TIMESTAMP", new JavaTypeInfo("java.time.LocalDateTime", "LocalDateTime", "LocalDateTime")),
            // 布尔
            Map.entry("BIT", new JavaTypeInfo("java.lang.Boolean", "Boolean", "Boolean")),
            Map.entry("BOOLEAN", new JavaTypeInfo("java.lang.Boolean", "Boolean", "Boolean")),
            // 二进制
            Map.entry("BLOB", new JavaTypeInfo("byte[]", "byte[]", "byte[]")),
            Map.entry("LONGBLOB", new JavaTypeInfo("byte[]", "byte[]", "byte[]"))
    );

    /**
     * 根据 JDBC 类型名获取对应的 Java 类型信息。
     *
     * @param jdbcType JDBC 类型名（如 VARCHAR、BIGINT）
     * @return Java 类型信息，未知类型默认返回 String
     */
    public static JavaTypeInfo getJavaType(String jdbcType) {
        if (jdbcType == null) {
            return TYPE_MAP.get("VARCHAR");
        }
        String upper = jdbcType.toUpperCase();
        // 处理带参数的如 VARCHAR(255) → VARCHAR
        int paren = upper.indexOf('(');
        if (paren > 0) {
            upper = upper.substring(0, paren);
        }
        // 处理 UNSIGNED
        if (upper.contains("UNSIGNED")) {
            upper = upper.replace("UNSIGNED", "").trim();
        }
        return TYPE_MAP.getOrDefault(upper, new JavaTypeInfo("java.lang.String", "String", "String"));
    }

    /**
     * 根据 JDBC 类型名获取 MyBatis XML 中的 jdbcType 值。
     */
    public static String getXmlJdbcType(String jdbcType) {
        if (jdbcType == null) {
            return "VARCHAR";
        }
        String upper = jdbcType.toUpperCase();
        int paren = upper.indexOf('(');
        if (paren > 0) {
            upper = upper.substring(0, paren);
        }
        return switch (upper) {
            case "TINYINT" -> "TINYINT";
            case "SMALLINT" -> "SMALLINT";
            case "INT", "INTEGER" -> "INTEGER";
            case "BIGINT" -> "BIGINT";
            case "FLOAT" -> "FLOAT";
            case "DOUBLE" -> "DOUBLE";
            case "DECIMAL" -> "DECIMAL";
            case "DATE" -> "DATE";
            case "TIME" -> "TIME";
            case "DATETIME", "TIMESTAMP" -> "TIMESTAMP";
            case "BIT", "BOOLEAN" -> "BOOLEAN";
            case "TEXT" -> "LONGVARCHAR";
            case "BLOB" -> "BLOB";
            default -> "VARCHAR";
        };
    }
}
