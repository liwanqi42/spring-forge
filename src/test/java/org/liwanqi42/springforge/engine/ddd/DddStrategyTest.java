package org.liwanqi42.springforge.engine.ddd;

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
 * DddStrategy DDD 模式代码生成策略测试。
 */
@DisplayName("DddStrategy DDD 模式策略")
class DddStrategyTest {

    @TempDir
    Path tempDir;

    private FreeMarkerTemplateEngine engine;
    private DddStrategy strategy;
    private GenerationContext ctx;
    private TableInfo table;

    @BeforeEach
    void setUp() {
        engine = new FreeMarkerTemplateEngine();
        strategy = new DddStrategy(engine);

        ctx = new GenerationContext();
        ctx.setMode(GenerationMode.DDD);
        ctx.setBasePackage("com.example.demo");
        ctx.setOutputDir(tempDir.toString());
        ctx.setOverwrite(true);
        ctx.setAuthor("TestAuthor");
        ctx.setDate("2026/08/02");
        ctx.setProjectName("demo");

        table = buildTestTable();
        SystemFieldDetector.detect(table, new GenerationOptions());
    }

    // ======================== DDD 包路径验证（通过生成文件内容） ========================

    @Nested
    @DisplayName("DDD 分层包路径验证")
    class DddPackagePaths {

        @Test
        @DisplayName("Entity 包在 domain.model")
        void entityPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/model/User.java"));
            assertTrue(content.contains("package com.example.demo.domain.model;"));
        }

        @Test
        @DisplayName("Repository 包在 domain.repository")
        void repositoryPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/repository/UserRepository.java"));
            assertTrue(content.contains("package com.example.demo.domain.repository;"));
        }

        @Test
        @DisplayName("ApplicationService 包在 application.service")
        void appServicePackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/application/service/UserApplicationService.java"));
            assertTrue(content.contains("package com.example.demo.application.service;"));
        }

        @Test
        @DisplayName("Controller 包在 adapter.web")
        void controllerPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/adapter/web/UserController.java"));
            assertTrue(content.contains("package com.example.demo.adapter.web;"));
        }

        @Test
        @DisplayName("DTO 包在 application.dto")
        void dtoPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/application/dto/UserCreateDTO.java"));
            assertTrue(content.contains("package com.example.demo.application.dto;"));
        }

        @Test
        @DisplayName("VO 包在 application.vo")
        void voPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/application/vo/UserListVO.java"));
            assertTrue(content.contains("package com.example.demo.application.vo;"));
        }

        @Test
        @DisplayName("Converter 包在 application.converter")
        void converterPackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/application/converter/UserConverter.java"));
            assertTrue(content.contains("package com.example.demo.application.converter;"));
        }

        @Test
        @DisplayName("DomainService 包在 domain.service")
        void domainServicePackagePath() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/service/UserDomainService.java"));
            assertTrue(content.contains("package com.example.demo.domain.service;"));
        }
    }

    // ======================== 类名后缀验证 ========================

    @Nested
    @DisplayName("DDD 标准类名后缀验证")
    class DddClassSuffixes {

        @Test
        @DisplayName("Repository 后缀为 Repository")
        void repositorySuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/repository/UserRepository.java")));
        }

        @Test
        @DisplayName("ApplicationService 后缀为 ApplicationService")
        void appServiceSuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/application/service/UserApplicationService.java")));
        }

        @Test
        @DisplayName("DomainService 后缀为 DomainService")
        void domainServiceSuffix() {
            strategy.generate(ctx, table);
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/service/UserDomainService.java")));
        }
    }

    // ======================== 完整生成验证 ========================

    @Nested
    @DisplayName("generate 完整生成")
    class Generate {

        @Test
        @DisplayName("生成 DDD 全部分层文件（11 个业务文件）")
        void generateAllDddFiles() {
            strategy.generate(ctx, table);

            String base = "src/main/java/com/example/demo/";

            // 领域层（5 个文件）
            assertFileExists(base + "domain/model/User.java", "Entity");
            assertFileExists(base + "domain/repository/UserRepository.java", "Repository");
            assertFileExists("src/main/resources/mapper/UserRepository.xml", "Repository XML");
            assertFileExists(base + "domain/service/UserDomainService.java", "DomainService");
            assertFileExists(base + "domain/service/impl/UserDomainServiceImpl.java", "DomainServiceImpl");

            // 应用层（5 个文件）
            assertFileExists(base + "application/dto/UserCreateDTO.java", "CreateDTO");
            assertFileExists(base + "application/dto/UserUpdateDTO.java", "UpdateDTO");
            assertFileExists(base + "application/dto/UserQueryDTO.java", "QueryDTO");
            assertFileExists(base + "application/vo/UserListVO.java", "ListVO");
            assertFileExists(base + "application/vo/UserDetailVO.java", "DetailVO");
            assertFileExists(base + "application/converter/UserConverter.java", "Converter");
            assertFileExists(base + "application/service/UserApplicationService.java", "ApplicationService");
            assertFileExists(base + "application/service/impl/UserApplicationServiceImpl.java", "ApplicationServiceImpl");

            // 适配层（1 个文件）
            assertFileExists(base + "adapter/web/UserController.java", "Controller");
        }

        @Test
        @DisplayName("DDD Entity 包含 @TableName 注解")
        void entityContainsTableName() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/model/User.java"));
            assertTrue(content.contains("@TableName"));
        }

        @Test
        @DisplayName("DDD Repository 继承 BaseMapper")
        void repositoryExtendsBaseMapper() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/repository/UserRepository.java"));
            assertTrue(content.contains("extends BaseMapper<User>"));
            assertTrue(content.contains("@Mapper"));
        }

        @Test
        @DisplayName("DDD DomainService 是接口")
        void domainServiceIsInterface() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/service/UserDomainService.java"));
            assertTrue(content.contains("interface"));
        }

        @Test
        @DisplayName("DDD Controller 包含 @RestController")
        void controllerContainsRestController() throws Exception {
            strategy.generate(ctx, table);
            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/example/demo/adapter/web/UserController.java"));
            assertTrue(content.contains("@RestController"));
        }
    }

    // ======================== MVC vs DDD 关键差异 ========================

    @Nested
    @DisplayName("DDD 特有行为")
    class DddSpecificBehavior {

        @Test
        @DisplayName("DDD 生成 DomainService 和 DomainServiceImpl")
        void generatesDomainService() {
            strategy.generate(ctx, table);

            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/service/UserDomainService.java")));
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/domain/service/impl/UserDomainServiceImpl.java")));
        }

        @Test
        @DisplayName("DDD 不生成 MVC 风格的 .mapper 目录")
        void noMvcStyleMapper() {
            strategy.generate(ctx, table);

            assertFalse(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/mapper/")));
        }

        @Test
        @DisplayName("DDD 不生成 MVC 风格的 .service 目录")
        void noMvcStyleService() {
            strategy.generate(ctx, table);

            assertFalse(Files.exists(tempDir.resolve(
                    "src/main/java/com/example/demo/service/")));
        }
    }

    @Nested
    @DisplayName("策略实例化")
    class Instantiation {

        @Test
        @DisplayName("DddStrategy 正确实例化且是 AbstractStrategy 子类")
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
