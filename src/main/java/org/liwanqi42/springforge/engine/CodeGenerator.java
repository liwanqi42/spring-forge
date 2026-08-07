package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.metadata.JdbcMetadataProvider;
import org.liwanqi42.springforge.metadata.JsonMetadataProvider;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.SourceFileWriter;
import org.liwanqi42.springforge.util.SystemFieldDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 代码生成主编排器。
 *
 * <p>协调元数据获取 → 全局类生成 → 单表代码生成的完整流水线。</p>
 */
public class CodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);

    private static final String FORGE_VERSION = loadForgeVersion();
    private static final String BOOT_VERSION = loadBootVersion();

    private final FreeMarkerTemplateEngine templateEngine;
    private final GlobalClassGenerator globalClassGen;
    private final TableCodeGenerator tableCodeGen;

    public CodeGenerator() {
        this.templateEngine = new FreeMarkerTemplateEngine();
        this.globalClassGen = new GlobalClassGenerator(templateEngine);
        this.tableCodeGen = new TableCodeGenerator(templateEngine);
    }

    /**
     * 执行代码生成。
     *
     * @param ctx 生成上下文（含完整配置、表信息、选项）
     */
    public void generate(GenerationContext ctx) {
        logger.info("===== Spring-Forge 代码生成开始 =====");
        logger.info("模式：{}", ctx.getMode());
        logger.info("输出目录：{}", ctx.getOutputDir());

        // 1. 设置日期
        if (ctx.getDate() == null || ctx.getDate().isEmpty()) {
            ctx.setDate(DateTimeFormatter.ofPattern("yyyy/MM/dd").format(LocalDate.now()));
        }

        // 2. 获取元数据
        List<TableInfo> tables = ctx.getTables();
        if (tables == null || tables.isEmpty()) {
            // JsonConfigLoader 已在 load() 中直接解析了对象表（模式3），此处处理 fallback 路径
            List<String> nameFilter = ctx.getTableNameFilter();

            // 优先尝试 JSON（模式3：完整表对象数组）
            boolean hasJson = ctx.getJsonConfigPath() != null && !ctx.getJsonConfigPath().isEmpty();
            boolean hasJdbc = ctx.getJdbcUrl() != null && !ctx.getJdbcUrl().isEmpty();

            if (hasJson) {
                tables = new JsonMetadataProvider(ctx.getJsonConfigPath(), ctx.getOptions())
                        .fetchMetadata(nameFilter);
            }

            // JSON 无结果时回退到 JDBC（模式1：无 tables 节点；模式2：字符串数组过滤）
            if (tables.isEmpty() && hasJdbc) {
                tables = new JdbcMetadataProvider(ctx.getJdbcUrl(), ctx.getJdbcUsername(),
                        ctx.getJdbcPassword(), ctx.getOptions())
                        .fetchMetadata(nameFilter);
            }

            if (tables.isEmpty()) {
                throw new GenerationException("未找到任何表信息，请检查数据库连接或 JSON 配置");
            }
            ctx.setTables(tables);
        }
        logger.info("发现 {} 张表", tables.size());

        // 3. 对每张表执行系统字段识别
        for (TableInfo table : tables) {
            SystemFieldDetector.detect(table, ctx.getOptions());
        }

        // 4. 项目脚手架（pom.xml + application.yml，已存在则跳过）
        generateProjectScaffold(ctx);

        // 5. 全局基础类（仅首次生成）
        globalClassGen.generate(ctx);

        // 6. 逐表生成业务代码
        for (TableInfo table : tables) {
            logger.info("正在生成表：{} ({})", table.getName(), table.getComment());
            tableCodeGen.generate(ctx, table);
        }

        // 7. DDL 建表语句（写入 schema.sql，Spring Boot 启动时自动执行）
        if (ctx.getOptions().isGenerateDdl()) {
            generateDdl(ctx, tables);
        }

        logger.info("===== Spring-Forge 代码生成完成 =====");
    }

    /**
     * 生成项目脚手架文件。
     *
     * <p>首次运行（无 .spring-forge.lock）时强制覆盖 pom.xml 和 application.yml，
     * 确保生成的代码在第二次启动时有完整的依赖声明。
     * 后续运行时保持不覆盖，保护用户的手动修改。</p>
     */
    private void generateProjectScaffold(GenerationContext ctx) {
        Path outputDir = Path.of(ctx.getOutputDir());
        GenerationOptions opt = ctx.getOptions();

        // 首次生成：lock 文件不存在 → 覆盖脚手架文件
        // 后续运行：lock 文件存在 → 保护用户修改，不覆盖
        boolean isFirstRun = !Files.exists(outputDir.resolve(".spring-forge.lock"));

        boolean hasJdbc = ctx.getJdbcUrl() != null && !ctx.getJdbcUrl().isEmpty();

        // === pom.xml ===
        Map<String, Object> pomModel = new HashMap<>();
        pomModel.put("projectName", ctx.getProjectName());
        pomModel.put("groupId", ctx.getGroupId());
        pomModel.put("basePackage", ctx.getBasePackage());
        pomModel.put("author", ctx.getAuthor());
        pomModel.put("date", ctx.getDate());
        pomModel.put("useLombok", opt.isUseLombok());
        pomModel.put("hasJdbc", hasJdbc);
        pomModel.put("forgeVersion", FORGE_VERSION);
        pomModel.put("bootVersion", BOOT_VERSION);

        String pomContent = templateEngine.render("project/pom.ftl", pomModel);
        SourceFileWriter.writePomXml(outputDir, pomContent, isFirstRun);

        // === application.yml ===
        Map<String, Object> ymlModel = new HashMap<>();
        ymlModel.put("hasJdbc", hasJdbc);
        if (hasJdbc) {
            ymlModel.put("jdbcUrl", ctx.getJdbcUrl());
            ymlModel.put("jdbcUsername", ctx.getJdbcUsername());
            ymlModel.put("jdbcPassword", ctx.getJdbcPassword());
        }
        ymlModel.put("idType", opt.getIdType());
        ymlModel.put("logicDeleteField", opt.getLogicDeleteField());
        ymlModel.put("logicDeleteValue", opt.getLogicDeleteValue());
        ymlModel.put("logicNotDeleteValue", opt.getLogicNotDeleteValue());
        ymlModel.put("optimisticLockField", opt.getOptimisticLockField());
        ymlModel.put("tablePrefix", opt.getTablePrefix());
        ymlModel.put("dateFormat", opt.getDateFormat());

        String ymlContent = templateEngine.render("project/application.ftl", ymlModel);
        SourceFileWriter.writeResourceFile(outputDir, "", "application.yml", ymlContent, isFirstRun);

        logger.info("项目脚手架文件已生成（{}）",
                isFirstRun ? "首次运行，覆盖已有文件" : "文件已存在，跳过覆盖");
    }

    /**
     * 生成 DDL 建表语句到 schema.sql。
     */
    private void generateDdl(GenerationContext ctx, List<TableInfo> tables) {
        Path outputDir = Path.of(ctx.getOutputDir());
        boolean ow = ctx.isOverwrite();

        StringBuilder ddl = new StringBuilder();
        ddl.append("-- Spring-Forge 自动生成的 DDL\n");
        ddl.append("-- 启动时由 Spring Boot 自动执行（spring.sql.init.mode=always）\n\n");

        for (TableInfo table : tables) {
            Map<String, Object> model = new HashMap<>();
            model.put("tableName", table.getName());
            model.put("tableComment", table.getComment());
            model.put("entityName", table.getEntityName());
            model.put("columns", table.getColumns());
            model.put("pkColumn", table.getPkColumn());
            model.put("basePackage", ctx.getBasePackage());

            String tableDdl = templateEngine.render("project/ddl.ftl", model);
            ddl.append(tableDdl).append("\n");
        }

        SourceFileWriter.writeResourceFile(outputDir, "", "schema.sql",
                ddl.toString(), ow);
        logger.info("已生成 schema.sql（DDL 建表语句）");
    }

    /**
     * 从 classpath 读取当前 spring-forge 版本号。
     * Maven 打包时自动生成 pom.properties，包含 version 字段。
     */
    private static String loadForgeVersion() {
        try (InputStream is = CodeGenerator.class.getClassLoader()
                .getResourceAsStream("META-INF/maven/io.github.liwanqi42/spring-forge/pom.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String version = props.getProperty("version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException e) {
            logger.debug("无法读取 pom.properties，使用默认版本号", e);
        }
        return "3.0.0"; // fallback（开发阶段 classpath 无 pom.properties）
    }

    /**
     * 从 classpath 读取当前 Spring Boot 版本号。
     */
    private static String loadBootVersion() {
        // 优先从 Spring Boot 自身 API 获取
        try {
            Class<?> bootVersionClass = Class.forName("org.springframework.boot.SpringBootVersion");
            String version = (String) bootVersionClass.getMethod("getVersion").invoke(null);
            if (version != null && !version.isEmpty()) {
                return version;
            }
        } catch (Exception e) {
            logger.debug("SpringBootVersion.getVersion() 不可用，回退到 pom.properties", e);
        }
        // 回退到 spring-boot JAR 的 pom.properties
        try (InputStream is = CodeGenerator.class.getClassLoader()
                .getResourceAsStream("META-INF/maven/org.springframework.boot/spring-boot/pom.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String version = props.getProperty("version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException e) {
            logger.debug("无法读取 spring-boot pom.properties", e);
        }
        return "3.4.5"; // fallback
    }
}
