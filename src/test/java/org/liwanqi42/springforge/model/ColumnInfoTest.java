package org.liwanqi42.springforge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnInfo 列信息模型测试。
 */
@DisplayName("ColumnInfo 列信息模型")
class ColumnInfoTest {

    @Test
    @DisplayName("所有布尔标记默认 false")
    void booleanDefaults() {
        ColumnInfo col = new ColumnInfo();

        assertFalse(col.isNullable(), "nullable 默认 false");
        assertFalse(col.isPrimaryKey(), "primaryKey 默认 false");
        assertFalse(col.isAutoIncrement(), "autoIncrement 默认 false");
        assertFalse(col.isIdField(), "idField 默认 false");
        assertFalse(col.isLogicDeleteField(), "logicDeleteField 默认 false");
        assertFalse(col.isVersionField(), "versionField 默认 false");
        assertFalse(col.isAutoFillInsert(), "autoFillInsert 默认 false");
        assertFalse(col.isAutoFillUpdate(), "autoFillUpdate 默认 false");
        assertFalse(col.isSystemField(), "systemField 默认 false");
    }

    @Test
    @DisplayName("整数默认值正确")
    void integerDefaults() {
        ColumnInfo col = new ColumnInfo();
        assertEquals(0, col.getMaxLength(), "maxLength 默认 0");
    }

    @Test
    @DisplayName("字符串默认值正确")
    void stringDefaults() {
        ColumnInfo col = new ColumnInfo();

        assertNull(col.getRawName());
        assertNull(col.getFieldName());
        assertNull(col.getFieldNameUpper());
        assertNull(col.getJavaType());
        assertNull(col.getJavaTypeShort());
        assertNull(col.getJavaTypeBoxed());
        assertNull(col.getJdbcType());
        assertNull(col.getColumnComment());
        assertNull(col.getDefaultValue(), "defaultValue 默认 null（Lombok @Data）");
        assertNull(col.getDtoAnnotation(), "dtoAnnotation 默认 null（Lombok @Data）");
        assertNull(col.getXmlJdbcType());
    }

    @Test
    @DisplayName("完整属性设置和读取")
    void fullPropertyAccess() {
        ColumnInfo col = new ColumnInfo();
        col.setRawName("user_name");
        col.setFieldName("userName");
        col.setFieldNameUpper("UserName");
        col.setJavaType("java.lang.String");
        col.setJavaTypeShort("String");
        col.setJavaTypeBoxed("String");
        col.setJdbcType("VARCHAR");
        col.setColumnComment("用户名");
        col.setMaxLength(50);
        col.setNullable(false);
        col.setPrimaryKey(false);
        col.setAutoIncrement(false);
        col.setDefaultValue("default_user");
        col.setDtoAnnotation("@NotBlank(message = \"用户名不能为空\")");
        col.setXmlJdbcType("VARCHAR");

        col.setIdField(false);
        col.setLogicDeleteField(false);
        col.setVersionField(false);
        col.setAutoFillInsert(false);
        col.setAutoFillUpdate(false);
        col.setSystemField(false);

        assertEquals("user_name", col.getRawName());
        assertEquals("userName", col.getFieldName());
        assertEquals("UserName", col.getFieldNameUpper());
        assertEquals("java.lang.String", col.getJavaType());
        assertEquals("String", col.getJavaTypeShort());
        assertEquals("String", col.getJavaTypeBoxed());
        assertEquals("VARCHAR", col.getJdbcType());
        assertEquals("用户名", col.getColumnComment());
        assertEquals(50, col.getMaxLength());
        assertFalse(col.isNullable());
        assertEquals("default_user", col.getDefaultValue());
        assertTrue(col.getDtoAnnotation().contains("@NotBlank"));
        assertEquals("VARCHAR", col.getXmlJdbcType());
    }

    @Test
    @DisplayName("主键列完整标记设置")
    void primaryKeyColumn() {
        ColumnInfo col = new ColumnInfo();
        col.setRawName("id");
        col.setPrimaryKey(true);
        col.setAutoIncrement(true);
        col.setJavaTypeBoxed("Long");
        col.setIdField(true);
        col.setSystemField(true);

        assertTrue(col.isPrimaryKey());
        assertTrue(col.isAutoIncrement());
        assertTrue(col.isIdField());
        assertTrue(col.isSystemField());
    }

    @Test
    @DisplayName("逻辑删除列标记")
    void logicDeleteColumn() {
        ColumnInfo col = new ColumnInfo();
        col.setRawName("deleted");
        col.setLogicDeleteField(true);
        col.setSystemField(true);
        col.setDefaultValue("0");

        assertTrue(col.isLogicDeleteField());
        assertTrue(col.isSystemField());
        assertEquals("0", col.getDefaultValue());
    }

    @Test
    @DisplayName("乐观锁列标记")
    void versionColumn() {
        ColumnInfo col = new ColumnInfo();
        col.setRawName("version");
        col.setVersionField(true);
        col.setSystemField(true);

        assertTrue(col.isVersionField());
        assertTrue(col.isSystemField());
    }

    @Test
    @DisplayName("可空列标记")
    void nullableColumn() {
        ColumnInfo col = new ColumnInfo();
        col.setNullable(true);

        assertTrue(col.isNullable());
    }

    @Test
    @DisplayName("自定义 Java 类型的列")
    void customJavaType() {
        ColumnInfo col = new ColumnInfo();
        col.setJavaType("java.math.BigDecimal");
        col.setJavaTypeShort("BigDecimal");
        col.setJavaTypeBoxed("BigDecimal");

        assertEquals("java.math.BigDecimal", col.getJavaType());
        assertEquals("BigDecimal", col.getJavaTypeShort());
        assertEquals("BigDecimal", col.getJavaTypeBoxed());
    }

    @Test
    @DisplayName("dtoAnnotation 为 null 的默认行为（Lombok @Data）")
    void emptyDtoAnnotation() {
        ColumnInfo col = new ColumnInfo();
        assertNull(col.getDtoAnnotation(), "Lombok @Data String 字段默认为 null");
    }
}
