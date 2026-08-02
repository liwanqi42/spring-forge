package org.liwanqi42.springforge.model;

import lombok.Data;

/**
 * 数据库列信息模型。
 */
@Data
public class ColumnInfo {
    /** 原始列名（数据库字段名） */
    private String rawName;
    /** Java 驼峰字段名 */
    private String fieldName;
    /** 首字母大写驼峰名 */
    private String fieldNameUpper;
    /** Java 类型全限定名 */
    private String javaType;
    /** Java 类型简称，如 String */
    private String javaTypeShort;
    /** Java 包装类型简称，如 Long（用于可空字段） */
    private String javaTypeBoxed;
    /** JDBC 类型名，如 VARCHAR */
    private String jdbcType;
    /** 列中文注释 */
    private String columnComment;
    /** 列长度 */
    private int maxLength;
    /** 是否可为空 */
    private boolean nullable;
    /** 是否主键 */
    private boolean primaryKey;
    /** 是否自增 */
    private boolean autoIncrement;
    /** 默认值 */
    private String defaultValue;

    // 系统字段标记（由 SystemFieldDetector 填充）
    private boolean idField;
    private boolean logicDeleteField;
    private boolean versionField;
    private boolean autoFillInsert;
    private boolean autoFillUpdate;
    private boolean systemField;
    /** JSR380 校验注解字符串，如 @NotBlank */
    private String dtoAnnotation;
    /** MyBatis XML 中的 JDBC 类型，如 VARCHAR */
    private String xmlJdbcType;

}
