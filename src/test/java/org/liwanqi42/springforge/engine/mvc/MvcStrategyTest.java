package org.liwanqi42.springforge.engine.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.liwanqi42.springforge.engine.AbstractStrategy;
import org.liwanqi42.springforge.engine.FreeMarkerTemplateEngine;
import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.NamingUtils;
import org.liwanqi42.springforge.util.SystemFieldDetector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MvcStrategy MVC 模式代码生成策略测试。
 */
@DisplayName("MvcStrategy MVC 模式策略")
class MvcStrategyTest {

    @TempDir
    Path tempDir;

    private FreeMarkerTemplateEngine engine;
    private MvcStrategy strategy;
    private GenerationContext ctx;
    private TableInfo table;

    @BeforeEach
    void setUp() {
        engine = new FreeMarkerTemplateEngine();
        strategy = new MvcStrategy(engine);

        ctx = new GenerationContext();
        ctx.setMode(GenerationMode.MVC);
        ctx.setBasePackage("com.example.demo");
        ctx.setOutputDir(tempDir.toString());
        ctx.setOverwrite(true);
        ctx.setAuthor("TestAuthor");
        ctx.setDate("2026/08/02");
        ctx.setProjectName("demo");

        table = buildTestTable();
        SystemFieldDetector.detect(table, new GenerationOptions());
    }

    // ======================== 包路径验证（通过生成文件内容） ========================

    @Nested
    @DisplayName("MVC 标准包路径验证")
    class MvcPackagePaths {

        @Test
        @DisplayName("Entity 包在 com.example.demo.entity")
        void entityPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/entity/User.java"));
            assertTrue(content.contains("package com.example.demo.entity;"));
        }

        @Test
        @DisplayName("Mapper 包在 com.example.demo.mapper")
        void mapperPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/mapper/UserMapper.java"));
            assertTrue(content.contains("package com.example.demo.mapper;"));
        }

        @Test
        @DisplayName("Service 包在 com.example.demo.service")
        void servicePackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/service/UserService.java"));
            assertTrue(content.contains("package com.example.demo.service;"));
        }

        @Test
        @DisplayName("Controller 包在 com.example.demo.controller")
        void controllerPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/controller/UserController.java"));
            assertTrue(content.contains("package com.example.demo.controller;"));
        }

        @Test
        @DisplayName("DTO 包在 com.example.demo.dto")
        void dtoPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/dto/UserCreateDTO.java"));
            assertTrue(content.contains("package com.example.demo.dto;"));
        }

        @Test
        @DisplayName("VO 包在 com.example.demo.vo")
        void voPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/vo/UserListVO.java"));
            assertTrue(content.contains("package com.example.demo.vo;"));
        }

        @Test
        @DisplayName("Converter 包在 com.example.demo.converter")
        void converterPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/converter/UserConverter.java"));
            assertTrue(content.contains("package com.example.demo.converter;"));
        }
    }

    // ======================== 类名后缀验证 ========================

    @Nested
    @DisplayName("MVC 标准类名后缀验证")
    class MvcClassSuffixes {

        @Test
        @DisplayName("Mapper 后缀为 Mapper")
        void mapperSuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/mapper/UserMapper.java")));
        }

        @Test
        @DisplayName("Service 后缀为 Service")
        void serviceSuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/service/UserService.java")));
        }

        @Test
        @DisplayName("ServiceImpl 后缀为 ServiceImpl")
        void serviceImplSuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/service/impl/UserServiceImpl.java")));
        }

        @Test
        @DisplayName("Controller 后缀为 Controller")
        void controllerSuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/controller/UserController.java")));
        }
    }

    // ======================== 完整生成验证 ========================

    @Nested
    @DisplayName("generate 完整生成")
    class Generate {

        @Test
        @DisplayName("生成 MVC 全部分层文件（10 个业务文件）")
        void generateAllMvcFiles() {
            strategy.generate(ctx, table);

            String base = "src/main/java/com/example/demo/";
            assertFileExists(base + "entity/User.java", "Entity");
            assertFileExists(base + "mapper/UserMapper.java", "Mapper");
            assertFileExists("src/main/resources/mapper/UserMapper.xml", "Mapper XML");
            assertFileExists(base + "dto/UserCreateDTO.java", "CreateDTO");
            assertFileExists(base + "dto/UserUpdateDTO.java", "UpdateDTO");
            assertFileExists(base + "dto/UserQueryDTO.java", "QueryDTO");
            assertFileExists(base + "vo/UserListVO.java", "ListVO");
            assertFileExists(base + "vo/UserDetailVO.java", "DetailVO");
            assertFileExists(base + "converter/UserConverter.java", "Converter");
            assertFileExists(base + "service/UserService.java", "Service");
            assertFileExists(base + "service/impl/UserServiceImpl.java", "ServiceImpl");
            assertFileExists(base + "controller/UserController.java", "Controller");
        }

        @Test
        @DisplayName("Entity 文件内容包含 @TableName 注解")
        void entityContainsTableNameAnnotation() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/entity/User.java"));
            assertTrue(content.contains("@TableName"));
        }

        @Test
        @DisplayName("Controller 包含 @RestController")
        void controllerContainsRestController() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/controller/UserController.java"));
            assertTrue(content.contains("@RestController"));
            assertTrue(content.contains("@RequestMapping"));
        }

        @Test
        @DisplayName("Mapper 继承 BaseMapper")
        void mapperExtendsBaseMapper() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/mapper/UserMapper.java"));
            assertTrue(content.contains("extends BaseMapper<User>"));
        }

        @Test
        @DisplayName("ServiceImpl 继承 ServiceImpl")
        void serviceImplExtendsServiceImpl() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/service/impl/UserServiceImpl.java"));
            assertTrue(content.contains("extends ServiceImpl<UserMapper, User>"));
        }
    }

    @Nested
    @DisplayName("策略实例化")
    class Instantiation {

        @Test
        @DisplayName("MvcStrategy 正确实例化且是 AbstractStrategy 子类")
        void instantiation() {
            assertNotNull(strategy);
            assertTrue(strategy instanceof AbstractStrategy);
        }
    }

    // ======================== 辅助方法 ========================

    private TableInfo buildTestTable() {
        TableInfo t = new TableInfo();
        t.setName("user");
        t.setComment("用户表");
        t.setEntityName("User");
        t.setEntityNameLower("user");
        t.setMappingPath("/user");

        ColumnInfo id = col("id", "BIGINT", "Long");
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);

        ColumnInfo username = col("username", "VARCHAR", "String");
        username.setNullable(false);
        ColumnInfo email = col("email", "VARCHAR", "String");
        email.setNullable(true);
        ColumnInfo createTime = col("create_time", "DATETIME", "LocalDateTime");
        ColumnInfo updateTime = col("update_time", "DATETIME", "LocalDateTime");
        ColumnInfo deleted = col("deleted", "TINYINT", "Integer");

        t.getColumns().addAll(List.of(id, username, email, createTime, updateTime, deleted));
        return t;
    }

    private ColumnInfo col(String name, String jdbcType, String javaType) {
        ColumnInfo c = new ColumnInfo();
        c.setRawName(name);
        c.setFieldName(NamingUtils.toLowerCamel(name));
        c.setFieldNameUpper(NamingUtils.toUpperCamel(name));
        c.setJdbcType(jdbcType);
        c.setJavaType("java.lang." + javaType);
        c.setJavaTypeShort(javaType);
        c.setJavaTypeBoxed(javaType);
        c.setXmlJdbcType(jdbcType);
        c.setColumnComment(name + "注释");
        return c;
    }

    private void assertFileExists(String relativePath, String description) {
        Path file = tempDir.resolve(relativePath);
        assertTrue(Files.exists(file), description + " 未生成: " + file);
    }
}
