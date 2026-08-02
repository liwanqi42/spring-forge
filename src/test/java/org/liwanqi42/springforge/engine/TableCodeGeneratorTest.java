package org.liwanqi42.springforge.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TableCodeGenerator 单表代码生成器测试。
 */
@DisplayName("TableCodeGenerator 单表代码生成器")
class TableCodeGeneratorTest {

    @TempDir
    Path tempDir;

    private FreeMarkerTemplateEngine engine;
    private TableCodeGenerator generator;
    private TableInfo table;

    @BeforeEach
    void setUp() {
        engine = new FreeMarkerTemplateEngine();
        generator = new TableCodeGenerator(engine);

        table = buildTestTable();
        SystemFieldDetector.detect(table, new GenerationOptions());
    }

    @Nested
    @DisplayName("模式分发")
    class ModeDispatch {

        @Test
        @DisplayName("MVC 模式委托给 MvcStrategy")
        void mvcModeUsesMvcStrategy() {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            generator.generate(ctx, table);

            // MVC 文件应在标准 MVC 路径
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/entity/User.java")), "MVC Entity 应在 .entity 包");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/mapper/UserMapper.java")), "MVC Mapper 应在 .mapper 包");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/service/UserService.java")), "MVC Service 应在 .service 包");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/controller/UserController.java")), "MVC Controller 应在 .controller 包");
        }

        @Test
        @DisplayName("DDD 模式委托给 DddStrategy")
        void dddModeUsesDddStrategy() {
            GenerationContext ctx = buildCtx(GenerationMode.DDD);
            generator.generate(ctx, table);

            // DDD 文件应在 DDD 分层路径
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/domain/model/User.java")), "DDD Entity 应在 .domain.model 包");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/domain/repository/UserRepository.java")), "DDD Repository 应在 .domain.repository 包");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/application/service/UserApplicationService.java")), "DDD ApplicationService 应在 .application.service 包");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/adapter/web/UserController.java")), "DDD Controller 应在 .adapter.web 包");
        }

        @Test
        @DisplayName("MVC 模式不生成 DomainService")
        void mvcDoesNotGenerateDomainService() {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            generator.generate(ctx, table);

            assertFalse(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/domain/service/UserDomainService.java")),
                    "MVC 不应生成 DomainService");
        }

        @Test
        @DisplayName("DDD 模式生成 DomainService")
        void dddGeneratesDomainService() {
            GenerationContext ctx = buildCtx(GenerationMode.DDD);
            generator.generate(ctx, table);

            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/domain/service/UserDomainService.java")),
                    "DDD 应生成 DomainService");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/domain/service/impl/UserDomainServiceImpl.java")),
                    "DDD 应生成 DomainServiceImpl");
        }

        @Test
        @DisplayName("MVC 模式类名后缀为 Mapper/Service")
        void mvcClassSuffixes() {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            generator.generate(ctx, table);

            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/mapper/UserMapper.java")), "后缀应为 Mapper");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/service/UserService.java")), "后缀应为 Service");
        }

        @Test
        @DisplayName("DDD 模式类名后缀为 Repository/ApplicationService")
        void dddClassSuffixes() {
            GenerationContext ctx = buildCtx(GenerationMode.DDD);
            generator.generate(ctx, table);

            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/domain/repository/UserRepository.java")), "后缀应为 Repository");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/application/service/UserApplicationService.java")), "后缀应为 ApplicationService");
        }
    }

    @Nested
    @DisplayName("多表生成")
    class MultiTable {

        @Test
        @DisplayName("多表生成不互相干扰")
        void multipleTablesGeneration() {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);

            // 生成 User 表
            generator.generate(ctx, table);

            // 生成 Product 表
            TableInfo product = buildProductTable();
            SystemFieldDetector.detect(product, new GenerationOptions());
            generator.generate(ctx, product);

            // 两张表的文件都存在
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/entity/User.java")), "User Entity");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/entity/Product.java")), "Product Entity");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/controller/UserController.java")), "User Controller");
            assertTrue(Files.exists(tempDir.resolve(
                    "src/main/java/com/test/controller/ProductController.java")), "Product Controller");
        }
    }

    // ======================== 辅助方法 ========================

    private GenerationContext buildCtx(GenerationMode mode) {
        GenerationContext ctx = new GenerationContext();
        ctx.setMode(mode);
        ctx.setBasePackage("com.test");
        ctx.setOutputDir(tempDir.toString());
        ctx.setOverwrite(true);
        ctx.setAuthor("TestAuthor");
        ctx.setDate("2026/08/02");
        ctx.setProjectName("test-project");
        ctx.setOptions(new GenerationOptions());
        return ctx;
    }

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
        ColumnInfo name = col("username", "VARCHAR", "String");
        name.setNullable(false);
        ColumnInfo email = col("email", "VARCHAR", "String");
        email.setNullable(true);
        ColumnInfo createTime = col("create_time", "DATETIME", "LocalDateTime");
        ColumnInfo updateTime = col("update_time", "DATETIME", "LocalDateTime");
        ColumnInfo deleted = col("deleted", "TINYINT", "Integer");

        t.getColumns().addAll(List.of(id, name, email, createTime, updateTime, deleted));
        return t;
    }

    private TableInfo buildProductTable() {
        TableInfo t = new TableInfo();
        t.setName("product");
        t.setComment("商品表");
        t.setEntityName("Product");
        t.setEntityNameLower("product");
        t.setMappingPath("/product");

        ColumnInfo id = col("id", "BIGINT", "Long");
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        ColumnInfo name = col("name", "VARCHAR", "String");
        name.setNullable(false);
        ColumnInfo price = col("price", "DECIMAL", "BigDecimal");

        t.getColumns().addAll(List.of(id, name, price));
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
}
