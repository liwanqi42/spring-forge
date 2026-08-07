package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.util.SourceFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 全局基础类生成器。
 *
 * <p>仅首次运行时生成全局通用类（Result、PageResult、BizException 等），
 * 后续运行通过 .spring-forge.lock 文件跳过。</p>
 *
 * <p>模板位于 {@code templates/infra/} 下，与项目整体 FreeMarker 体系一致。</p>
 */
public class GlobalClassGenerator {

    private static final Logger logger = LoggerFactory.getLogger(GlobalClassGenerator.class);
    private static final String LOCK_FILE = ".spring-forge.lock";
    private static final String TEMPLATE_DIR = "infra/";

    /** 全局类描述：FreeMarker 模板路径 + 类名 + 相对于 common 包的子路径 */
    private record GlobalClass(String templatePath, String className, String subPackage) {}

    private static final List<GlobalClass> GLOBAL_CLASSES = List.of(
            new GlobalClass(TEMPLATE_DIR + "Result",               "Result",               ""),
            new GlobalClass(TEMPLATE_DIR + "PageResult",           "PageResult",           ""),
            new GlobalClass(TEMPLATE_DIR + "BizException",         "BizException",         ""),
            new GlobalClass(TEMPLATE_DIR + "ErrorCode",            "ErrorCode",            ""),
            new GlobalClass(TEMPLATE_DIR + "RestExceptionHandler", "RestExceptionHandler", ".config"),
            new GlobalClass(TEMPLATE_DIR + "MyBatisPlusConfig",    "MyBatisPlusConfig",    ".config"),
            new GlobalClass(TEMPLATE_DIR + "WebConfig",            "WebConfig",            ".config"),
            new GlobalClass(TEMPLATE_DIR + "DateUtils",            "DateUtils",            ".util")
    );

    private final FreeMarkerTemplateEngine engine;

    public GlobalClassGenerator(FreeMarkerTemplateEngine engine) {
        this.engine = engine;
    }

    /**
     * 检查是否需要生成全局类（lock 文件不存在或模式变更则需生成）。
     */
    public boolean shouldGenerate(GenerationContext ctx) {
        Path lockPath = Path.of(ctx.getOutputDir(), LOCK_FILE);
        if (!Files.exists(lockPath)) return true;
        try (var reader = Files.newBufferedReader(lockPath)) {
            var props = new Properties();
            props.load(reader);
            return !ctx.getMode().name().equals(props.getProperty("mode"));
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 生成全局基础类并写入 lock 文件。
     *
     * <p>通过 {@link #GLOBAL_CLASSES} 数据驱动 + FreeMarker 模板渲染，
     * 新增/删除全局类只需修改列表定义和对应 .ftl 模板。</p>
     */
    public void generate(GenerationContext ctx) {
        if (!shouldGenerate(ctx)) {
            logger.info("全局基础类已存在，跳过生成（删除 {} 可强制重新生成）",
                    Path.of(ctx.getOutputDir(), LOCK_FILE));
            return;
        }

        String pkg = ctx.getBasePackage() + ".common";
        Path dir = Path.of(ctx.getOutputDir());
        boolean ow = ctx.isOverwrite();

        Map<String, Object> model = buildModel(ctx);

        for (GlobalClass gc : GLOBAL_CLASSES) {
            String templatePath = resolveTemplate(gc, ctx);
            String content = engine.render(templatePath, model);
            SourceFileWriter.writeJavaFile(dir, pkg + gc.subPackage, gc.className, content, ow);
        }

        writeLockFile(ctx);

        logger.info("全局基础类生成完成");
    }

    /** 解析模板路径（统一追加 .ftl 后缀）。 */
    private String resolveTemplate(GlobalClass gc, GenerationContext ctx) {
        return gc.templatePath + ".ftl";
    }

    /** 构建 FreeMarker 数据模型。 */
    private Map<String, Object> buildModel(GenerationContext ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("basePackage", ctx.getBasePackage());
        model.put("mapperScanPackage", ctx.getBasePackage()
                + (ctx.getMode() == org.liwanqi42.springforge.model.GenerationMode.DDD
                        ? ".domain.repository" : ".mapper"));
        // Spring Boot 4.x+ 自动配置可能不兼容 → 强制使用 full 模式（显式声明 Bean）
        boolean useFull = !ctx.getOptions().isMinimalMyBatisConfig() || getBootMajorVersion() >= 4;
        model.put("myBatisConfigMode", useFull ? "full" : "minimal");
        model.put("useLombok", ctx.getOptions().isUseLombok());
        return model;
    }

    /** 获取当前运行环境的 Spring Boot 主版本号（如 3.4.5 → 3，4.1.0 → 4）。 */
    private static int getBootMajorVersion() {
        try {
            Class<?> c = Class.forName("org.springframework.boot.SpringBootVersion");
            String v = (String) c.getMethod("getVersion").invoke(null);
            if (v != null && !v.isEmpty()) {
                return Integer.parseInt(v.split("\\.")[0]);
            }
        } catch (Exception e) {
            logger.debug("无法读取 Spring Boot 版本，假设为 3.x", e);
        }
        return 3;
    }

    /** 写入 .spring-forge.lock 标记文件。 */
    private void writeLockFile(GenerationContext ctx) {
        String lockContent = "# Spring-Forge Global Classes Marker\n"
                + "# 删除此文件可强制重新生成全局基础类\n"
                + "generatedAt=" + LocalDateTime.now() + "\n"
                + "mode=" + ctx.getMode() + "\n";
        SourceFileWriter.writeFile(Path.of(ctx.getOutputDir()), LOCK_FILE, lockContent, true);
    }
}
