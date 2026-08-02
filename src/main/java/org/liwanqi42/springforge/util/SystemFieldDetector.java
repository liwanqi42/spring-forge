package org.liwanqi42.springforge.util;

import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统字段自动检测器。
 *
 * <p>按字段名模式自动识别系统通用字段：主键、自动填充、逻辑删除、乐观锁等。</p>
 */
public final class SystemFieldDetector {

    private SystemFieldDetector() {
    }

    /** 默认 ID 字段名集合 */
    private static final Set<String> ID_NAMES = new HashSet<>(Arrays.asList("id"));
    /** 默认创建时间字段名 */
    private static final Set<String> CREATE_TIME_NAMES = new HashSet<>(Arrays.asList(
            "create_time", "createTime", "gmt_create", "gmtCreate"));
    /** 默认更新时间字段名 */
    private static final Set<String> UPDATE_TIME_NAMES = new HashSet<>(Arrays.asList(
            "update_time", "updateTime", "gmt_modified", "gmtModified"));
    /** 默认创建人字段名 */
    private static final Set<String> CREATE_BY_NAMES = new HashSet<>(Arrays.asList(
            "create_by", "createBy", "creator"));
    /** 默认更新人字段名 */
    private static final Set<String> UPDATE_BY_NAMES = new HashSet<>(Arrays.asList(
            "update_by", "updateBy", "modifier"));
    /** 默认逻辑删除字段名 */
    private static final Set<String> DELETED_NAMES = new HashSet<>(Arrays.asList(
            "deleted", "is_deleted", "isDeleted"));
    /** 默认乐观锁字段名 */
    private static final Set<String> VERSION_NAMES = new HashSet<>(Arrays.asList("version"));

    /** 查询条件支持的 Java 类型 */
    private static final Set<String> QUERY_SUPPORTED_TYPES = Set.of(
            "String", "Integer", "Long", "LocalDateTime", "LocalDate");

    /**
     * 对表信息中的所有列进行系统字段识别和标记。
     *
     * @param table   表信息
     * @param options 生成选项（自定义字段名）
     */
    public static void detect(TableInfo table, GenerationOptions options) {
        for (ColumnInfo col : table.getColumns()) {
            String rawLower = col.getRawName().toLowerCase();
            String camel = col.getFieldName();

            // 主键检测
            if (col.isPrimaryKey() || ID_NAMES.contains(rawLower) || ID_NAMES.contains(camel)) {
                col.setIdField(true);
                col.setSystemField(true);
                table.setPkColumn(col);
                table.setPkJavaType(col.getJavaTypeBoxed());
            }

            // 创建时间检测
            if (CREATE_TIME_NAMES.contains(rawLower) || CREATE_TIME_NAMES.contains(camel)) {
                col.setAutoFillInsert(true);
                col.setSystemField(true);
            }

            // 更新时间检测
            if (UPDATE_TIME_NAMES.contains(rawLower) || UPDATE_TIME_NAMES.contains(camel)) {
                col.setAutoFillInsert(true);
                col.setAutoFillUpdate(true);
                col.setSystemField(true);
            }

            // 创建人检测
            if (CREATE_BY_NAMES.contains(rawLower) || CREATE_BY_NAMES.contains(camel)) {
                col.setAutoFillInsert(true);
                col.setSystemField(true);
            }

            // 更新人检测
            if (UPDATE_BY_NAMES.contains(rawLower) || UPDATE_BY_NAMES.contains(camel)) {
                col.setAutoFillInsert(true);
                col.setAutoFillUpdate(true);
                col.setSystemField(true);
            }

            // 逻辑删除检测
            String configuredDelete = options.getLogicDeleteField();
            if ((configuredDelete != null && !configuredDelete.isEmpty())
                    && (rawLower.equals(configuredDelete.toLowerCase())
                    || camel.equals(configuredDelete)
                    || DELETED_NAMES.contains(rawLower)
                    || DELETED_NAMES.contains(camel))) {
                col.setLogicDeleteField(true);
                col.setSystemField(true);
            }

            // 乐观锁检测
            String configuredVersion = options.getOptimisticLockField();
            if ((configuredVersion != null && !configuredVersion.isEmpty())
                    && (rawLower.equals(configuredVersion.toLowerCase())
                    || camel.equals(configuredVersion)
                    || VERSION_NAMES.contains(rawLower)
                    || VERSION_NAMES.contains(camel))) {
                col.setVersionField(true);
                col.setSystemField(true);
            }
        }

        // 生成列级别的 JSR380 校验注解
        buildDtoAnnotations(table);
    }

    /**
     * 根据列属性生成 JSR380 校验注解字符串。
     */
    private static void buildDtoAnnotations(TableInfo table) {
        for (ColumnInfo col : table.getColumns()) {
            if (col.isSystemField()) {
                col.setDtoAnnotation("");
                continue;
            }
            StringBuilder ann = new StringBuilder();
            if (!col.isNullable()) {
                if ("String".equals(col.getJavaTypeShort())) {
                    ann.append("@NotBlank(message = \"").append(col.getColumnComment())
                            .append("不能为空\")\n    ");
                } else {
                    ann.append("@NotNull(message = \"").append(col.getColumnComment())
                            .append("不能为空\")\n    ");
                }
            }
            int len = col.getMaxLength();
            if (len > 0 && "String".equals(col.getJavaTypeShort())) {
                ann.append("@Size(max = ").append(len).append(", message = \"")
                        .append(col.getColumnComment()).append("长度不能超过").append(len)
                        .append("\")\n    ");
            }
            col.setDtoAnnotation(ann.toString().trim());
        }
    }

    /**
     * 获取用于插入的列（排除自增ID、自动填充的插入字段）
     */
    public static List<ColumnInfo> getInsertColumns(TableInfo table) {
        return table.getColumns().stream()
                .filter(c -> !(c.isIdField() && c.isAutoIncrement()))
                .filter(c -> !c.isAutoFillInsert())
                .toList();
    }

    /**
     * 获取用于更新的列（排除主键、自动填充字段、系统字段）
     */
    public static List<ColumnInfo> getUpdateColumns(TableInfo table) {
        return table.getColumns().stream()
                .filter(c -> !c.isIdField())
                .filter(c -> !c.isAutoFillUpdate())
                .filter(c -> !c.isLogicDeleteField())
                .filter(c -> !c.isVersionField())
                .toList();
    }

    /**
     * 获取查询条件列（排除系统字段，仅保留支持的类型）
     */
    public static List<ColumnInfo> getQueryColumns(TableInfo table) {
        return table.getColumns().stream()
                .filter(c -> !c.isSystemField() && QUERY_SUPPORTED_TYPES.contains(c.getJavaTypeShort()))
                .toList();
    }
}
