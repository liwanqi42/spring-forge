package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.metadata.JdbcMetadataProvider;
import org.liwanqi42.springforge.metadata.JsonMetadataProvider;
import org.liwanqi42.springforge.metadata.MetadataProvider;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.SourceFileWriter;
import org.liwanqi42.springforge.util.SystemFieldDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成主编排器。
 *
 * <p>协调元数据获取 → 全局类生成 → 单表代码生成的完整流水线。</p>
 */
public class CodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);

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
            // JsonConfigLoader 已在 load() 中直接解析了对象表，此处仅处理 fallback 路径
            List<String> nameFilter = ctx.getTableNameFilter();
            MetadataProvider provider;
            if (ctx.getJsonConfigPath() != null && !ctx.getJsonConfigPath().isEmpty()) {
                provider = new JsonMetadataProvider(ctx.getJsonConfigPath(), ctx.getOptions());
            } else if (ctx.getJdbcUrl() != null && !ctx.getJdbcUrl().isEmpty()) {
                provider = new JdbcMetadataProvider(ctx.getJdbcUrl(), ctx.getJdbcUsername(),
                        ctx.getJdbcPassword(), ctx.getOptions());
            } else {
                throw new GenerationException("请提供数据库连接（jdbcUrl）或 JSON 配置文件路径（jsonConfigPath）");
            }
            tables = provider.fetchMetadata(nameFilter);
            ctx.setTables(tables);
        }

        if (tables.isEmpty()) {
            throw new GenerationException("未找到任何表信息，请检查数据库连接或 JSON 配置");
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
     * 生成项目脚手架文件
     */
    private void generateProjectScaffold(GenerationContext ctx) {
        Path outputDir = Path.of(ctx.getOutputDir());
        GenerationOptions opt = ctx.getOptions();

        boolean hasJdbc = ctx.getJdbcUrl() != null && !ctx.getJdbcUrl().isEmpty();

        // === pom 参考文件 ===
        Map<String, Object> pomModel = new HashMap<>();
        pomModel.put("projectName", ctx.getProjectName());
        pomModel.put("groupId", ctx.getGroupId());
        pomModel.put("basePackage", ctx.getBasePackage());
        pomModel.put("author", ctx.getAuthor());
        pomModel.put("date", ctx.getDate());
        pomModel.put("useLombok", opt.isUseLombok());
        pomModel.put("generateTests", opt.isGenerateTests());
        pomModel.put("hasJdbc", hasJdbc);

        String pomContent = templateEngine.render("project/pom.ftl", pomModel);
        SourceFileWriter.writePomXml(outputDir, pomContent, false);

        // === application 参考文件 ===
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
        SourceFileWriter.writeResourceFile(outputDir, "", "application.yml", ymlContent, false);

        logger.info("项目脚手架参考文件已生成（如文件已存在则跳过）");
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
}
