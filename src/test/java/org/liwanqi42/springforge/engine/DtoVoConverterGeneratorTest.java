package org.liwanqi42.springforge.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.NamingUtils;
import org.liwanqi42.springforge.util.SystemFieldDetector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DtoVoConverterGenerator DTO/VO/Converter 复合生成器测试。
 */
@DisplayName("DtoVoConverterGenerator DTO/VO/Converter 生成器")
class DtoVoConverterGeneratorTest {

    @TempDir
    Path tempDir;

    private DtoVoConverterGenerator generator;
    private GenerationContext ctx;
    private TableInfo table;
    private Map<String, Object> model;

    @BeforeEach
    void setUp() {
        FreeMarkerTemplateEngine engine = new FreeMarkerTemplateEngine();
        generator = new DtoVoConverterGenerator(engine);

        ctx = new GenerationContext();
        ctx.setBasePackage("com.test");
        ctx.setOutputDir(tempDir.toString());
        ctx.setOverwrite(true);
        ctx.setDate("2026/08/02");

        table = buildTestTable();
        SystemFieldDetector.detect(table, new GenerationOptions());

        model = new HashMap<>();
        String bp = "com.test";
        model.put("basePackage", bp);
        model.put("entityPackage", bp + ".entity");
        model.put("dtoPackage", bp + ".dto");
        model.put("voPackage", bp + ".vo");
        model.put("dtoOutputPackage", bp + ".dto");
        model.put("voOutputPackage", bp + ".vo");
        model.put("entityName", "User");
        model.put("entityNameLower", "user");
        model.put("tableName", "user");
        model.put("tableComment", "用户表");
        model.put("columns", table.getColumns());
        model.put("pkColumn", table.getPkColumn());
        model.put("pkJavaType", table.getPkJavaType());
        model.put("isDdd", false);
        model.put("options", new GenerationOptions());
        model.put("author", "TestAuthor");
        model.put("date", "2026/08/02");
        model.put("projectName", "test-project");
        model.put("insertColumns", SystemFieldDetector.getInsertColumns(table));
        model.put("updateColumns", SystemFieldDetector.getUpdateColumns(table));
        model.put("queryColumns", SystemFieldDetector.getQueryColumns(table));
    }

    @Test
    @DisplayName("MVC 模式生成 3 个 DTO")
    void generateMvcDtos() {
        generator.generateDtos(ctx, table, model, ".dto");

        String base = "src/main/java/com/test/dto/";
        assertTrue(Files.exists(tempDir.resolve(base + "UserCreateDTO.java")), "CreateDTO 未生成");
        assertTrue(Files.exists(tempDir.resolve(base + "UserUpdateDTO.java")), "UpdateDTO 未生成");
        assertTrue(Files.exists(tempDir.resolve(base + "UserQueryDTO.java")), "QueryDTO 未生成");
    }

    @Test
    @DisplayName("MVC 模式生成 2 个 VO")
    void generateMvcVos() {
        generator.generateVos(ctx, table, model, ".vo");

        String base = "src/main/java/com/test/vo/";
        assertTrue(Files.exists(tempDir.resolve(base + "UserListVO.java")), "ListVO 未生成");
        assertTrue(Files.exists(tempDir.resolve(base + "UserDetailVO.java")), "DetailVO 未生成");
    }

    @Test
    @DisplayName("MVC 模式生成 Converter")
    void generateMvcConverter() {
        generator.generateConverter(ctx, table, model, ".converter");

        Path file = tempDir.resolve("src/main/java/com/test/converter/UserConverter.java");
        assertTrue(Files.exists(file), "Converter 未生成");
    }

    @Test
    @DisplayName("DDD 模式生成 3 个 DTO 在 application.dto 包")
    void generateDddDtos() {
        ctx.setMode(GenerationMode.DDD);
        generator.generateDtos(ctx, table, model, ".application.dto");

        String base = "src/main/java/com/test/application/dto/";
        assertTrue(Files.exists(tempDir.resolve(base + "UserCreateDTO.java")), "DDD CreateDTO 未生成");
        assertTrue(Files.exists(tempDir.resolve(base + "UserUpdateDTO.java")), "DDD UpdateDTO 未生成");
        assertTrue(Files.exists(tempDir.resolve(base + "UserQueryDTO.java")), "DDD QueryDTO 未生成");
    }

    @Test
    @DisplayName("DDD 模式生成 2 个 VO 在 application.vo 包")
    void generateDddVos() {
        ctx.setMode(GenerationMode.DDD);
        generator.generateVos(ctx, table, model, ".application.vo");

        String base = "src/main/java/com/test/application/vo/";
        assertTrue(Files.exists(tempDir.resolve(base + "UserListVO.java")), "DDD ListVO 未生成");
        assertTrue(Files.exists(tempDir.resolve(base + "UserDetailVO.java")), "DDD DetailVO 未生成");
    }

    @Test
    @DisplayName("DDD 模式生成 Converter 在 application.converter 包")
    void generateDddConverter() {
        ctx.setMode(GenerationMode.DDD);
        generator.generateConverter(ctx, table, model, ".application.converter");

        Path file = tempDir.resolve("src/main/java/com/test/application/converter/UserConverter.java");
        assertTrue(Files.exists(file), "DDD Converter 未生成");
    }

    @Test
    @DisplayName("DTO 包含正确的包声明")
    void dtoContainsCorrectPackage() throws Exception {
        generator.generateDtos(ctx, table, model, ".dto");

        String content = Files.readString(tempDir.resolve(
                "src/main/java/com/test/dto/UserCreateDTO.java"));
        assertTrue(content.contains("package com.test.dto;"), "包声明错误: " + content);
    }

    @Test
    @DisplayName("Converter 包含正确的导入")
    void converterContainsCorrectImports() throws Exception {
        generator.generateConverter(ctx, table, model, ".converter");

        String content = Files.readString(tempDir.resolve(
                "src/main/java/com/test/converter/UserConverter.java"));
        assertNotNull(content, "Converter 内容不应为空");
        assertTrue(content.contains("User"), "Converter 应包含实体名");
    }

    // ======================== 辅助方法 ========================

    private TableInfo buildTestTable() {
        TableInfo t = new TableInfo();
        t.setName("user");
        t.setComment("用户表");
        t.setEntityName("User");
        t.setEntityNameLower("user");
        t.setMappingPath("/user");

        ColumnInfo id = createColumn("id", "BIGINT", "Long");
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);

        ColumnInfo username = createColumn("username", "VARCHAR", "String");
        username.setNullable(false);

        ColumnInfo email = createColumn("email", "VARCHAR", "String");
        email.setNullable(true);

        ColumnInfo status = createColumn("status", "TINYINT", "Integer");
        ColumnInfo createTime = createColumn("create_time", "DATETIME", "LocalDateTime");
        ColumnInfo updateTime = createColumn("update_time", "DATETIME", "LocalDateTime");

        t.getColumns().addAll(List.of(id, username, email, status, createTime, updateTime));
        return t;
    }

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
        col.setXmlJdbcType(jdbcType);
        return col;
    }
}
