package org.liwanqi42.springforge.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.liwanqi42.springforge.exception.GenerationException;

import java.io.StringWriter;
import java.util.Map;

/**
 * FreeMarker 模板引擎封装。
 *
 * <p>负责模板加载、渲染和缓存管理。</p>
 */
public class FreeMarkerTemplateEngine {

    private final Configuration cfg;

    public FreeMarkerTemplateEngine() {
        cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(
                Thread.currentThread().getContextClassLoader(), "/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
    }

    /**
     * 渲染模板并返回生成的字符串。
     *
     * @param templatePath 模板路径（相对于 templates/ 目录），如 "java/entity.ftl"
     * @param dataModel    数据模型
     * @return 渲染后的文件内容
     * @throws GenerationException 如果模板不存在或渲染失败
     */
    public String render(String templatePath, Map<String, Object> dataModel) {
        try {
            Template template = cfg.getTemplate(templatePath);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new GenerationException("模板渲染失败：" + templatePath, e);
        }
    }
}
