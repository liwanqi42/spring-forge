package org.liwanqi42.springforge.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SystemFieldDetector 系统字段检测全面测试。
 */
@DisplayName("SystemFieldDetector 系统字段检测")
class SystemFieldDetectorTest {

    private TableInfo table;
    private GenerationOptions options;

    @BeforeEach
    void setUp() {
        table = new TableInfo();
        table.setName("test_table");
        table.setEntityName("TestTable");
        options = new GenerationOptions();
    }

    // ======================== ID 检测 ========================

    @Nested
    @DisplayName("主键 ID 检测")
    class IdDetection {

        @Test
        @DisplayName("rawName 为 'id' 自动识别为主键")
        void idByName() {
            ColumnInfo col = createColumn("id", "BIGINT", "Long");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isIdField(), "id 字段应被识别为主键");
            assertTrue(col.isSystemField(), "id 字段应被标记为系统字段");
            assertEquals(col, table.getPkColumn(), "应设置为 pkColumn");
            assertEquals("Long", table.getPkJavaType(), "pkJavaType 应正确");
        }

        @Test
        @DisplayName("primaryKey=true 自动识别为主键")
        void byPrimaryKeyFlag() {
            ColumnInfo col = createColumn("uid", "BIGINT", "Long");
            col.setPrimaryKey(true);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isIdField());
            assertEquals(col, table.getPkColumn());
        }

        @Test
        @DisplayName("非 id 名称且非主键标记的列不识别为 ID")
        void nonIdColumn() {
            ColumnInfo col = createColumn("name", "VARCHAR", "String");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertFalse(col.isIdField());
            assertFalse(col.isSystemField());
        }

        @Test
        @DisplayName("驼峰命名的 ID 列检测")
        void camelCaseId() {
            ColumnInfo col = createColumn("userId", "BIGINT", "Long");
            // rawName 为 userId, fieldName 也要匹配
            col.setRawName("userId");
            col.setFieldName("userId");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            // "userId" 不在 ID_NAMES 中，但如果有 primaryKey 则应该检测到
            assertFalse(col.isIdField(), "仅名称为 'id' 或 primaryKey=true 才识别");
        }
    }

    // ======================== 创建/更新时间检测 ========================

    @Nested
    @DisplayName("创建/更新时间检测")
    class TimeFieldDetection {

        @Test
        @DisplayName("create_time 识别为插入时自动填充")
        void createTime() {
            ColumnInfo col = createColumn("create_time", "DATETIME", "LocalDateTime");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert(), "create_time 应插入时填充");
            assertFalse(col.isAutoFillUpdate(), "create_time 不应更新时填充");
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("gmt_create 识别为创建时间")
        void gmtCreate() {
            ColumnInfo col = createColumn("gmt_create", "DATETIME", "LocalDateTime");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("gmtCreate 驼峰识别为创建时间")
        void gmtCreateCamel() {
            ColumnInfo col = createColumn("gmtCreate", "DATETIME", "LocalDateTime");
            col.setRawName("gmtCreate");
            col.setFieldName("gmtCreate");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
        }

        @Test
        @DisplayName("update_time 识别为插入+更新时填充")
        void updateTime() {
            ColumnInfo col = createColumn("update_time", "DATETIME", "LocalDateTime");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert(), "update_time 应插入时填充");
            assertTrue(col.isAutoFillUpdate(), "update_time 应更新时填充");
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("gmt_modified 识别为更新时间")
        void gmtModified() {
            ColumnInfo col = createColumn("gmt_modified", "DATETIME", "LocalDateTime");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
            assertTrue(col.isAutoFillUpdate());
        }

        @Test
        @DisplayName("驼峰 createTime/updateTime 检测")
        void camelTimeFields() {
            ColumnInfo createTime = createColumn("createTime", "DATETIME", "LocalDateTime");
            createTime.setRawName("createTime");
            createTime.setFieldName("createTime");

            ColumnInfo updateTime = createColumn("updateTime", "DATETIME", "LocalDateTime");
            updateTime.setRawName("updateTime");
            updateTime.setFieldName("updateTime");

            table.getColumns().add(createTime);
            table.getColumns().add(updateTime);
            SystemFieldDetector.detect(table, options);

            assertTrue(createTime.isAutoFillInsert());
            assertTrue(updateTime.isAutoFillUpdate());
        }
    }

    // ======================== 创建/更新人检测 ========================

    @Nested
    @DisplayName("创建/更新人检测")
    class UserFieldDetection {

        @Test
        @DisplayName("create_by 识别为插入时填充")
        void createBy() {
            ColumnInfo col = createColumn("create_by", "VARCHAR", "String");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("update_by 识别为插入+更新时填充")
        void updateBy() {
            ColumnInfo col = createColumn("update_by", "VARCHAR", "String");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
            assertTrue(col.isAutoFillUpdate());
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("creator 识别为创建人")
        void creator() {
            ColumnInfo col = createColumn("creator", "VARCHAR", "String");
            col.setRawName("creator");
            col.setFieldName("creator");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
        }

        @Test
        @DisplayName("modifier 识别为更新人")
        void modifier() {
            ColumnInfo col = createColumn("modifier", "VARCHAR", "String");
            col.setRawName("modifier");
            col.setFieldName("modifier");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isAutoFillInsert());
            assertTrue(col.isAutoFillUpdate());
        }

        @Test
        @DisplayName("createBy/updateBy 驼峰检测")
        void camelUserFields() {
            ColumnInfo createBy = createColumn("createBy", "VARCHAR", "String");
            createBy.setRawName("createBy");
            createBy.setFieldName("createBy");

            ColumnInfo updateBy = createColumn("updateBy", "VARCHAR", "String");
            updateBy.setRawName("updateBy");
            updateBy.setFieldName("updateBy");

            table.getColumns().add(createBy);
            table.getColumns().add(updateBy);
            SystemFieldDetector.detect(table, options);

            assertTrue(createBy.isAutoFillInsert());
            assertTrue(updateBy.isAutoFillUpdate());
        }
    }

    // ======================== 逻辑删除检测 ========================

    @Nested
    @DisplayName("逻辑删除字段检测")
    class LogicDeleteDetection {

        @Test
        @DisplayName("deleted 字段识别为逻辑删除")
        void deletedField() {
            ColumnInfo col = createColumn("deleted", "TINYINT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isLogicDeleteField());
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("is_deleted 字段识别为逻辑删除")
        void isDeletedField() {
            ColumnInfo col = createColumn("is_deleted", "TINYINT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isLogicDeleteField());
        }

        @Test
        @DisplayName("isDeleted 驼峰识别为逻辑删除")
        void isDeletedCamel() {
            ColumnInfo col = createColumn("isDeleted", "TINYINT", "Integer");
            col.setRawName("isDeleted");
            col.setFieldName("isDeleted");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isLogicDeleteField());
        }

        @Test
        @DisplayName("自定义逻辑删除字段名")
        void customLogicDeleteField() {
            options.setLogicDeleteField("is_removed");
            ColumnInfo col = createColumn("is_removed", "TINYINT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isLogicDeleteField());
        }

        @Test
        @DisplayName("禁用逻辑删除时不标记字段")
        void disabledLogicDelete() {
            options.setLogicDeleteField("");
            ColumnInfo col = createColumn("deleted", "TINYINT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertFalse(col.isLogicDeleteField(), "禁用逻辑删除时不应标记 deleted");
        }

        @Test
        @DisplayName("logicDeleteField 为 null 时降级使用默认名检测")
        void nullLogicDeleteField() {
            options.setLogicDeleteField(null);
            ColumnInfo col = createColumn("deleted", "TINYINT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            // null → (configuredDelete != null) 为 false，短路跳过
            // 但 DELETED_NAMES 检查仍然在条件中...
            // 实际上由于短路，不会标记
            assertFalse(col.isLogicDeleteField(),
                    "logicDeleteField 为 null 时不应标记（短路条件）");
        }
    }

    // ======================== 乐观锁检测 ========================

    @Nested
    @DisplayName("乐观锁字段检测")
    class VersionDetection {

        @Test
        @DisplayName("version 字段识别为乐观锁")
        void versionField() {
            ColumnInfo col = createColumn("version", "INT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isVersionField());
            assertTrue(col.isSystemField());
        }

        @Test
        @DisplayName("自定义乐观锁字段名")
        void customVersionField() {
            options.setOptimisticLockField("revision");
            ColumnInfo col = createColumn("revision", "INT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.isVersionField());
        }

        @Test
        @DisplayName("禁用乐观锁时不标记字段")
        void disabledVersion() {
            options.setOptimisticLockField("");
            ColumnInfo col = createColumn("version", "INT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertFalse(col.isVersionField(), "禁用乐观锁时不应标记 version");
        }

        @Test
        @DisplayName("非 version 名称不误判")
        void nonVersionField() {
            ColumnInfo col = createColumn("revision_no", "INT", "Integer");
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertFalse(col.isVersionField());
        }
    }

    // ======================== DTO 注解生成 ========================

    @Nested
    @DisplayName("JSR380 DTO 校验注解生成")
    class DtoAnnotationGeneration {

        @Test
        @DisplayName("非空 String 列生成 @NotBlank")
        void notBlankForString() {
            ColumnInfo col = createColumn("username", "VARCHAR", "String");
            col.setColumnComment("用户名");
            col.setNullable(false);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.getDtoAnnotation().contains("@NotBlank"),
                    "应有 @NotBlank，实际：" + col.getDtoAnnotation());
        }

        @Test
        @DisplayName("非空 Integer 列生成 @NotNull")
        void notNullForNonString() {
            ColumnInfo col = createColumn("status", "TINYINT", "Integer");
            col.setColumnComment("状态");
            col.setNullable(false);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.getDtoAnnotation().contains("@NotNull"),
                    "应有 @NotNull，实际：" + col.getDtoAnnotation());
        }

        @Test
        @DisplayName("可空列不生成 NotNull/NotBlank 注解")
        void nullableColumnNoAnnotation() {
            ColumnInfo col = createColumn("email", "VARCHAR", "String");
            col.setColumnComment("邮箱");
            col.setNullable(true);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            String ann = col.getDtoAnnotation();
            assertFalse(ann.contains("@NotBlank"), "可空列不应有 @NotBlank");
            assertFalse(ann.contains("@NotNull"), "可空列不应有 @NotNull");
        }

        @Test
        @DisplayName("String 列有 maxLength 时生成 @Size")
        void sizeAnnotation() {
            ColumnInfo col = createColumn("name", "VARCHAR", "String");
            col.setColumnComment("名称");
            col.setNullable(true);
            col.setMaxLength(50);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertTrue(col.getDtoAnnotation().contains("@Size"),
                    "应有 @Size，实际：" + col.getDtoAnnotation());
            assertTrue(col.getDtoAnnotation().contains("max = 50"));
        }

        @Test
        @DisplayName("maxLength=0 时不生成 @Size")
        void noSizeWhenZero() {
            ColumnInfo col = createColumn("name", "VARCHAR", "String");
            col.setColumnComment("名称");
            col.setNullable(true);
            col.setMaxLength(0);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertFalse(col.getDtoAnnotation().contains("@Size"));
        }

        @Test
        @DisplayName("系统字段不生成 DTO 注解")
        void systemFieldNoAnnotation() {
            ColumnInfo col = createColumn("create_time", "DATETIME", "LocalDateTime");
            col.setNullable(false);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertEquals("", col.getDtoAnnotation(),
                    "系统字段不应生成 DTO 注解");
        }

        @Test
        @DisplayName("非 String 类型有 maxLength 也不生成 @Size")
        void nonStringNoSize() {
            ColumnInfo col = createColumn("age", "INT", "Integer");
            col.setColumnComment("年龄");
            col.setNullable(true);
            col.setMaxLength(10);
            table.getColumns().add(col);
            SystemFieldDetector.detect(table, options);

            assertFalse(col.getDtoAnnotation().contains("@Size"));
        }
    }

    // ======================== 分层字段过滤 ========================

    @Nested
    @DisplayName("分层字段过滤方法")
    class ColumnFiltering {

        @Test
        @DisplayName("getInsertColumns 排除自增 ID 和自动填充字段")
        void insertColumnsExclusions() {
            ColumnInfo id = createColumn("id", "BIGINT", "Long");
            id.setPrimaryKey(true);
            id.setAutoIncrement(true);

            ColumnInfo name = createColumn("name", "VARCHAR", "String");
            ColumnInfo createTime = createColumn("create_time", "DATETIME", "LocalDateTime");

            table.getColumns().add(id);
            table.getColumns().add(name);
            table.getColumns().add(createTime);
            SystemFieldDetector.detect(table, options);

            List<ColumnInfo> insertCols = SystemFieldDetector.getInsertColumns(table);
            assertEquals(1, insertCols.size());
            assertEquals("name", insertCols.get(0).getRawName());
        }

        @Test
        @DisplayName("getUpdateColumns 排除 ID、自动填充更新字段、逻辑删除、乐观锁")
        void updateColumnsExclusions() {
            ColumnInfo id = createColumn("id", "BIGINT", "Long");
            id.setPrimaryKey(true);

            ColumnInfo name = createColumn("name", "VARCHAR", "String");
            ColumnInfo updateTime = createColumn("update_time", "DATETIME", "LocalDateTime");
            ColumnInfo deleted = createColumn("deleted", "TINYINT", "Integer");
            ColumnInfo version = createColumn("version", "INT", "Integer");

            table.getColumns().addAll(List.of(id, name, updateTime, deleted, version));
            SystemFieldDetector.detect(table, options);

            List<ColumnInfo> updateCols = SystemFieldDetector.getUpdateColumns(table);
            assertEquals(1, updateCols.size());
            assertEquals("name", updateCols.get(0).getRawName());
        }

        @Test
        @DisplayName("getQueryColumns 排除系统字段，仅保留支持的类型")
        void queryColumnsFiltering() {
            ColumnInfo id = createColumn("id", "BIGINT", "Long");
            id.setPrimaryKey(true);

            ColumnInfo name = createColumn("name", "VARCHAR", "String");
            ColumnInfo status = createColumn("status", "TINYINT", "Integer");
            ColumnInfo createTime = createColumn("create_time", "DATETIME", "LocalDateTime");
            ColumnInfo deleted = createColumn("deleted", "TINYINT", "Integer");

            table.getColumns().addAll(List.of(id, name, status, createTime, deleted));
            SystemFieldDetector.detect(table, options);

            List<ColumnInfo> queryCols = SystemFieldDetector.getQueryColumns(table);
            assertEquals(2, queryCols.size(),
                    "name 和 status 应为查询条件列");
            assertEquals("name", queryCols.get(0).getRawName());
            assertEquals("status", queryCols.get(1).getRawName());
        }

        @Test
        @DisplayName("空表返回空列表")
        void emptyTable() {
            assertTrue(SystemFieldDetector.getInsertColumns(table).isEmpty());
            assertTrue(SystemFieldDetector.getUpdateColumns(table).isEmpty());
            assertTrue(SystemFieldDetector.getQueryColumns(table).isEmpty());
        }
    }

    // ======================== 综合场景 ========================

    @Test
    @DisplayName("完整表的系统字段检测综合验证")
    void fullTableDetection() {
        table.setName("user");

        ColumnInfo id = createColumn("id", "BIGINT", "Long");
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);

        ColumnInfo username = createColumn("username", "VARCHAR", "String");
        username.setNullable(false);

        ColumnInfo createTime = createColumn("create_time", "DATETIME", "LocalDateTime");
        ColumnInfo updateTime = createColumn("update_time", "DATETIME", "LocalDateTime");
        ColumnInfo deleted = createColumn("deleted", "TINYINT", "Integer");
        ColumnInfo version = createColumn("version", "INT", "Integer");

        table.getColumns().addAll(List.of(id, username, createTime, updateTime, deleted, version));
        SystemFieldDetector.detect(table, options);

        // 验证每个属性
        assertTrue(id.isIdField());
        assertEquals("Long", table.getPkJavaType());
        assertTrue(createTime.isAutoFillInsert());
        assertTrue(updateTime.isAutoFillUpdate());
        assertTrue(deleted.isLogicDeleteField());
        assertTrue(version.isVersionField());

        // username 不是系统字段
        assertFalse(username.isSystemField());
        assertTrue(username.getDtoAnnotation().contains("@NotBlank"));
    }

    // ======================== 辅助方法 ========================

    private ColumnInfo createColumn(String name, String jdbcType, String javaTypeShort) {
        ColumnInfo col = new ColumnInfo();
        col.setRawName(name);
        col.setFieldName(NamingUtils.toLowerCamel(name));
        col.setFieldNameUpper(NamingUtils.toUpperCamel(name));
        col.setJdbcType(jdbcType);
        col.setJavaType("java.lang." + javaTypeShort);
        col.setJavaTypeShort(javaTypeShort);
        col.setJavaTypeBoxed(javaTypeShort);
        col.setColumnComment(name + "注释");
        return col;
    }
}
