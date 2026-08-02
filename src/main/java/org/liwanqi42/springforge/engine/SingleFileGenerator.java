package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.SourceFileWriter;

import java.nio.file.Path;
import java.util.Map;

/**
 * 通用单文件代码生成器。
 */
public class SingleFileGenerator {

    private final FreeMarkerTemplateEngine engine;
    private final GeneratorDescriptor descriptor;

    public SingleFileGenerator(FreeMarkerTemplateEngine engine, GeneratorDescriptor descriptor) {
        this.engine = engine;
        this.descriptor = descriptor;
    }

    /**
     * 执行生成：渲染模板并写入文件。
     *
     * @param ctx   生成上下文
     * @param table 表信息
     * @param model FreeMarker 数据模型
     */
    public void generate(GenerationContext ctx, TableInfo table, Map<String, Object> model) {
        String content = engine.render(descriptor.templatePath(), model);
        String className = table.getEntityName() + descriptor.classNameSuffix();
        Path outputDir = Path.of(ctx.getOutputDir());
        boolean ow = ctx.isOverwrite();

        switch (descriptor.outputTarget()) {
            case JAVA -> {
                String pkg = ctx.getBasePackage() + descriptor.packageSuffix();
                SourceFileWriter.writeJavaFile(outputDir, pkg, className, content, ow);
            }
            case RESOURCE -> SourceFileWriter.writeResourceFile(
                    outputDir, descriptor.resourceSubPath(), className, content, ow);
        }
    }
}
