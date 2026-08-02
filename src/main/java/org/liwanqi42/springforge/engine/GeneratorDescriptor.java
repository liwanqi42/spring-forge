package org.liwanqi42.springforge.engine;

/**
 * 单文件生成器配置描述符。
 *
 * <p>封装了一次 FreeMarker 模板渲染 + 文件输出的全部变体参数。
 * 用于替代所有单文件生成器类——原来需要创建 30 行的 Java 类，现在只需一行配置声明。</p>
 *
 * @param templatePath    FreeMarker 模板路径（相对于 templates/），如 "mvc/entity.ftl"
 * @param packageSuffix   输出包后缀（相对于 basePackage），如 ".entity"、".domain.model"
 * @param classNameSuffix 输出类名后缀（拼接在实体名后），如 ""、"Mapper"、"Service"
 * @param outputTarget    输出目标类型（Java 源码 或 资源文件）
 * @param resourceSubPath 资源文件子路径（仅当 outputTarget=RESOURCE 时使用），如 "mapper"
 */
public record GeneratorDescriptor(
        String templatePath,
        String packageSuffix,
        String classNameSuffix,
        OutputTarget outputTarget,
        String resourceSubPath
) {
    /**
     * 创建 Java 源码输出描述符的便捷工厂方法。
     */
    public static GeneratorDescriptor java(String templatePath, String packageSuffix, String classNameSuffix) {
        return new GeneratorDescriptor(templatePath, packageSuffix, classNameSuffix, OutputTarget.JAVA, "");
    }

    /**
     * 创建资源文件输出描述符的便捷工厂方法。
     */
    public static GeneratorDescriptor resource(String templatePath, String resourceSubPath, String classNameSuffix) {
        return new GeneratorDescriptor(templatePath, "", classNameSuffix, OutputTarget.RESOURCE, resourceSubPath);
    }
}
