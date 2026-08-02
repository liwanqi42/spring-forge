package org.liwanqi42.springforge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerationContext 模型类测试。
 */
@DisplayName("GenerationContext 生成上下文")
class GenerationContextTest {

    @Test
    @DisplayName("默认值正确")
    void defaultValues() {
        GenerationContext ctx = new GenerationContext();

        assertEquals(GenerationMode.MVC, ctx.getMode(), "默认模式应为 MVC");
        assertEquals("demo", ctx.getProjectName(), "默认项目名应为 demo");
        assertEquals("com.example", ctx.getGroupId(), "默认 groupId");
        assertEquals("com.example.demo", ctx.getBasePackage(), "默认 basePackage");
        assertEquals(".", ctx.getOutputDir(), "默认 outputDir");
        assertEquals("CodeGenerator", ctx.getAuthor(), "默认 author");
        assertFalse(ctx.isOverwrite(), "默认 overwrite 为 false");
        assertNotNull(ctx.getTables(), "tables 列表不应为 null");
        assertTrue(ctx.getTables().isEmpty(), "tables 默认空列表");
        assertNotNull(ctx.getOptions(), "options 不应为 null");
    }

    @Test
    @DisplayName("完整属性设置和读取")
    void fullPropertyAccess() {
        GenerationContext ctx = new GenerationContext();

        ctx.setMode(GenerationMode.DDD);
        ctx.setProjectName("myapp");
        ctx.setGroupId("org.test");
        ctx.setBasePackage("org.test.app");
        ctx.setOutputDir("./output");
        ctx.setOverwrite(true);
        ctx.setAuthor("张三");
        ctx.setDate("2026/08/02");
        ctx.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        ctx.setJdbcUsername("root");
        ctx.setJdbcPassword("pwd123");
        ctx.setJsonConfigPath("/path/to/config.json");

        List<String> filter = List.of("user", "order");
        ctx.setTableNameFilter(filter);

        List<TableInfo> tables = new ArrayList<>();
        TableInfo t = new TableInfo();
        t.setName("user");
        tables.add(t);
        ctx.setTables(tables);

        GenerationOptions opts = new GenerationOptions();
        opts.setGenerateDdl(true);
        ctx.setOptions(opts);

        assertEquals(GenerationMode.DDD, ctx.getMode());
        assertEquals("myapp", ctx.getProjectName());
        assertEquals("org.test", ctx.getGroupId());
        assertEquals("org.test.app", ctx.getBasePackage());
        assertEquals("./output", ctx.getOutputDir());
        assertTrue(ctx.isOverwrite());
        assertEquals("张三", ctx.getAuthor());
        assertEquals("2026/08/02", ctx.getDate());
        assertEquals("jdbc:mysql://localhost:3306/test", ctx.getJdbcUrl());
        assertEquals("root", ctx.getJdbcUsername());
        assertEquals("pwd123", ctx.getJdbcPassword());
        assertEquals("/path/to/config.json", ctx.getJsonConfigPath());
        assertEquals(2, ctx.getTableNameFilter().size());
        assertEquals(1, ctx.getTables().size());
        assertEquals("user", ctx.getTables().get(0).getName());
        assertTrue(ctx.getOptions().isGenerateDdl());
    }

    @Test
    @DisplayName("date 可为 null")
    void dateCanBeNull() {
        GenerationContext ctx = new GenerationContext();
        assertNull(ctx.getDate());
    }

    @Test
    @DisplayName("jdbcUrl 为 null 时表示非 JDBC 模式")
    void jdbcUrlNullByDefault() {
        GenerationContext ctx = new GenerationContext();
        assertNull(ctx.getJdbcUrl());
    }

    @Test
    @DisplayName("tableNameFilter 为 null 表示不过滤")
    void tableNameFilterNullByDefault() {
        GenerationContext ctx = new GenerationContext();
        assertNull(ctx.getTableNameFilter());
    }
}
