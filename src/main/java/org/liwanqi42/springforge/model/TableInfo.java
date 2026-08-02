package org.liwanqi42.springforge.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表信息模型。
 */
@Data
public class TableInfo {
    /** 原始表名 */
    private String name;
    /** 表中文注释 */
    private String comment;
    /** 列信息列表 */
    private List<ColumnInfo> columns = new ArrayList<>();
    /** 驼峰类名 */
    private String entityName;
    /** 首字母小写驼峰名 */
    private String entityNameLower;
    /** REST 映射路径 */
    private String mappingPath;
    /** 主键列（由 SystemFieldDetector 识别后填充） */
    private ColumnInfo pkColumn;
    /** 主键 Java 类型 */
    private String pkJavaType;

}
