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
 * SingleFileGenerator 单文件生成器测试。
 */
@DisplayName("SingleFileGenerator 单文件生成器")
class SingleFileGeneratorTest {

    @TempDir
    Path tempDir;

    private FreeMarkerTemplateEngine engine;
    private GenerationContext ctx;
    private TableInfo table;
    private Map<String, Object> model;

    @BeforeEach
    void setUp() {
        engine = new FreeMarkerTemplateEngine();
        ctx = new GenerationContext();
        ctx.setMode(GenerationMode.MVC);
        ctx.setBasePackage("com.test");
        ctx.setOutputDir(tempDir.toString());
        ctx.setOverwrite(true);
        ctx.setAuthor("TestAuthor");
        ctx.setDate("2026/08/02");
        ctx.setProjectName("test-project");

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
        model.put("converterPackage", bp + ".converter");
        model.put("mapperPackage", bp + ".mapper");
        model.put("servicePackage", bp + ".service");
        model.put("serviceImplPackage", bp + ".service.impl");
        model.put("controllerPackage", bp + ".controller");
        model.put("serviceClassSuffix", "Service");
        model.put("mapperClassSuffix", "Mapper");
        model.put("projectName", "test-project");
        model.put("entityName", "User");
        model.put("entityNameLower", "user");
        model.put("tableName", "user");
        model.put("tableComment", "用户表");
        model.put("mappingPath", "/user");
        model.put("columns", table.getColumns());
        model.put("pkColumn", table.getPkColumn());
        model.put("pkJavaType", table.getPkJavaType());
        model.put("isDdd", false);
        model.put("options", new GenerationOptions());
        model.put("author", "TestAuthor");
        model.put("date", "2026/08/02");
        // 这些用于 entity.ftl 和 service-impl.ftl
        model.put("insertColumns", SystemFieldDetector.getInsertColumns(table));
        model.put("updateColumns", SystemFieldDetector.getUpdateColumns(table));
        model.put("queryColumns", SystemFieldDetector.getQueryColumns(table));
        model.put("hasCreateTime", true);
        model.put("hasUpdateTime", true);
        model.put("hasCreateBy", false);
        model.put("hasUpdateBy", false);
        model.put("hasDeleted", true);
        model.put("hasVersion", false);
    }

    @Test
    @DisplayName("生成 Java Entity 文件")
    void generateJavaEntity() {
        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/entity.ftl", ".entity", ""));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/entity/User.java");
        assertTrue(Files.exists(file), "Entity 文件应生成");
    }

    @Test
    @DisplayName("生成 Mapper 接口文件")
    void generateMapper() {
        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/mapper.ftl", ".mapper", "Mapper"));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/mapper/UserMapper.java");
        assertTrue(Files.exists(file), "Mapper 文件应生成");
    }

    @Test
    @DisplayName("生成资源文件 Mapper XML")
    void generateMapperXml() {
        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.resource("java/mapper-xml.ftl", "mapper", "Mapper.xml"));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/resources/mapper/UserMapper.xml");
        assertTrue(Files.exists(file), "Mapper XML 应生成");
    }

    @Test
    @DisplayName("生成 Service 接口文件")
    void generateService() {
        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/service.ftl", ".service", "Service"));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/service/UserService.java");
        assertTrue(Files.exists(file), "Service 文件应生成");
    }

    @Test
    @DisplayName("生成 ServiceImpl 实现文件")
    void generateServiceImpl() {
        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/service-impl.ftl", ".service.impl", "ServiceImpl"));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/service/impl/UserServiceImpl.java");
        assertTrue(Files.exists(file), "ServiceImpl 文件应生成");
    }

    @Test
    @DisplayName("生成 Controller 文件")
    void generateController() {
        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/controller.ftl", ".controller", "Controller"));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/controller/UserController.java");
        assertTrue(Files.exists(file), "Controller 文件应生成");
    }

    @Test
    @DisplayName("DDD 模式下的实体生成")
    void generateDddEntity() {
        ctx.setMode(GenerationMode.DDD);
        String bp = "com.test";
        model.put("isDdd", true);
        model.put("entityPackage", bp + ".domain.model");
        model.put("dtoPackage", bp + ".application.dto");
        model.put("voPackage", bp + ".application.vo");
        model.put("dtoOutputPackage", bp + ".application.dto");
        model.put("voOutputPackage", bp + ".application.vo");
        model.put("converterPackage", bp + ".application.converter");
        model.put("mapperPackage", bp + ".domain.repository");
        model.put("servicePackage", bp + ".application.service");
        model.put("serviceImplPackage", bp + ".application.service.impl");
        model.put("controllerPackage", bp + ".adapter.web");
        model.put("serviceClassSuffix", "ApplicationService");
        model.put("mapperClassSuffix", "Repository");

        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/entity.ftl", ".domain.model", ""));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/domain/model/User.java");
        assertTrue(Files.exists(file), "DDD Entity 应生成在 domain.model 包");
    }

    @Test
    @DisplayName("DDD 模式下的 Repository 生成")
    void generateDddRepository() {
        ctx.setMode(GenerationMode.DDD);
        String bp = "com.test";
        model.put("isDdd", true);
        model.put("mapperPackage", bp + ".domain.repository");
        model.put("mapperClassSuffix", "Repository");

        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/mapper.ftl", ".domain.repository", "Repository"));

        gen.generate(ctx, table, model);

        Path file = tempDir.resolve("src/main/java/com/test/domain/repository/UserRepository.java");
        assertTrue(Files.exists(file), "DDD Repository 应生成");
    }

    @Test
    @DisplayName("overwrite=false 时不覆盖已存在文件")
    void respectOverwriteFlag() throws Exception {
        ctx.setOverwrite(false);

        Path file = tempDir.resolve("src/main/java/com/test/entity/User.java");
        Files.createDirectories(file.getParent());
        String oldContent = "// old content";
        Files.writeString(file, oldContent);

        SingleFileGenerator gen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/entity.ftl", ".entity", ""));

        gen.generate(ctx, table, model);

        assertEquals(oldContent, Files.readString(file), "overwrite=false 应保留原文件");
    }

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

        ColumnInfo name = createColumn("username", "VARCHAR", "String");
        ColumnInfo email = createColumn("email", "VARCHAR", "String");
        ColumnInfo createTime = createColumn("create_time", "DATETIME", "LocalDateTime");
        ColumnInfo updateTime = createColumn("update_time", "DATETIME", "LocalDateTime");
        ColumnInfo deleted = createColumn("deleted", "TINYINT", "Integer");

        t.getColumns().addAll(List.of(id, name, email, createTime, updateTime, deleted));
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
