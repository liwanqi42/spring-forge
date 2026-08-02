package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.SourceFileWriter;

import java.nio.file.Path;
import java.util.Map;

/**
 * DTO / VO / Converter 共享生成器。
 *
 * <p>DTO、VO、Converter 在 MVC 和 DDD 两种模式下使用完全相同的 FreeMarker 模板，
 * 每种模式只需在调用时传入不同的 {@code packageSuffix}：</p>
 * <ul>
 *   <li>MVC：{@code ".dto"} / {@code ".vo"} / {@code ".converter"}</li>
 *   <li>DDD：{@code ".application.dto"} / {@code ".application.vo"} / {@code ".application.converter"}</li>
 * </ul>
 */
public class DtoVoConverterGenerator {

    private final FreeMarkerTemplateEngine engine;

    public DtoVoConverterGenerator(FreeMarkerTemplateEngine engine) {
        this.engine = engine;
    }

    /** 统一的 POJO 模板路径 */
    private static final String POJO_TEMPLATE = "java/pojo.ftl";

    /**
     * 生成三个 DTO 文件（CreateDTO、UpdateDTO、QueryDTO）。
     */
    public void generateDtos(GenerationContext ctx, TableInfo table,
                              Map<String, Object> model, String packageSuffix) {
        String pkg = ctx.getBasePackage() + packageSuffix;
        Path dir = Path.of(ctx.getOutputDir());
        boolean ow = ctx.isOverwrite();
        String name = table.getEntityName();

        renderAndWrite(dir, pkg, name, "CreateDTO", "create-dto", model, ow);
        renderAndWrite(dir, pkg, name, "UpdateDTO", "update-dto", model, ow);
        renderAndWrite(dir, pkg, name, "QueryDTO", "query-dto", model, ow);
    }

    /**
     * 生成两个 VO 文件（ListVO、DetailVO）。
     */
    public void generateVos(GenerationContext ctx, TableInfo table,
                             Map<String, Object> model, String packageSuffix) {
        String pkg = ctx.getBasePackage() + packageSuffix;
        Path dir = Path.of(ctx.getOutputDir());
        boolean ow = ctx.isOverwrite();
        String name = table.getEntityName();

        renderAndWrite(dir, pkg, name, "ListVO", "list-vo", model, ow);
        renderAndWrite(dir, pkg, name, "DetailVO", "detail-vo", model, ow);
    }

    /**
     * 生成 Converter 转换器类（使用独立模板，不走 pojo.ftl）。
     */
    public void generateConverter(GenerationContext ctx, TableInfo table,
                                   Map<String, Object> model, String packageSuffix) {
        SourceFileWriter.writeJavaFile(
                Path.of(ctx.getOutputDir()),
                ctx.getBasePackage() + packageSuffix,
                table.getEntityName() + "Converter",
                engine.render("java/converter.ftl", model),
                ctx.isOverwrite());
    }

    /** 渲染 POJO 统一模板并写入 Java 文件。 */
    private void renderAndWrite(Path dir, String pkg, String name,
                                 String suffix, String type, Map<String, Object> model, boolean ow) {
        model.put("type", type);
        try {
            SourceFileWriter.writeJavaFile(dir, pkg, name + suffix, engine.render(POJO_TEMPLATE, model), ow);
        } finally {
            model.remove("type");
        }
    }
}
