package org.liwanqi42.springforge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TableInfo 表信息模型测试。
 */
@DisplayName("TableInfo 表信息模型")
class TableInfoTest {

    @Test
    @DisplayName("默认值正确")
    void defaultValues() {
        TableInfo table = new TableInfo();

        assertNull(table.getName());
        assertNull(table.getComment());
        assertNotNull(table.getColumns(), "columns 不应为 null");
        assertTrue(table.getColumns().isEmpty(), "columns 默认空列表");
        assertNull(table.getEntityName());
        assertNull(table.getEntityNameLower());
        assertNull(table.getMappingPath());
        assertNull(table.getPkColumn());
        assertNull(table.getPkJavaType());
    }

    @Test
    @DisplayName("完整属性设置与读取")
    void fullPropertyAccess() {
        TableInfo table = new TableInfo();
        table.setName("user");
        table.setComment("用户表");
        table.setEntityName("User");
        table.setEntityNameLower("user");
        table.setMappingPath("/user");

        ColumnInfo pkCol = new ColumnInfo();
        pkCol.setRawName("id");
        pkCol.setPrimaryKey(true);
        table.getColumns().add(pkCol);
        table.setPkColumn(pkCol);
        table.setPkJavaType("Long");

        assertEquals("user", table.getName());
        assertEquals("用户表", table.getComment());
        assertEquals("User", table.getEntityName());
        assertEquals("user", table.getEntityNameLower());
        assertEquals("/user", table.getMappingPath());
        assertEquals(1, table.getColumns().size());
        assertEquals(pkCol, table.getPkColumn());
        assertEquals("Long", table.getPkJavaType());
        assertEquals("id", table.getColumns().get(0).getRawName());
    }

    @Test
    @DisplayName("多列添加到表中")
    void multipleColumnsAdd() {
        TableInfo table = new TableInfo();

        ColumnInfo c1 = new ColumnInfo();
        c1.setRawName("id");
        ColumnInfo c2 = new ColumnInfo();
        c2.setRawName("name");
        ColumnInfo c3 = new ColumnInfo();
        c3.setRawName("email");

        table.getColumns().add(c1);
        table.getColumns().add(c2);
        table.getColumns().add(c3);

        assertEquals(3, table.getColumns().size());
        assertEquals("id", table.getColumns().get(0).getRawName());
        assertEquals("name", table.getColumns().get(1).getRawName());
        assertEquals("email", table.getColumns().get(2).getRawName());
    }

    @Test
    @DisplayName("comment 可为 null 时的默认处理")
    void commentCanBeNull() {
        TableInfo table = new TableInfo();
        assertNull(table.getComment());
    }
}
