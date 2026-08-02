package org.liwanqi42.springforge;

import org.liwanqi42.springforge.engine.CodeGenerator;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Spring-Forge 公开 API
 */
public final class SpringForgeTemplate {

    private SpringForgeTemplate() {
    }

    /**
     * 创建构建器。
     */
    public static Builder forge() {
        return new Builder();
    }

    /**
     * 流式构建器。
     */
    public static final class Builder {
        private final GenerationContext ctx = new GenerationContext();

        public Builder mode(GenerationMode mode) {
            ctx.setMode(mode);
            return this;
        }

        public Builder projectName(String name) {
            ctx.setProjectName(name);
            return this;
        }

        public Builder groupId(String groupId) {
            ctx.setGroupId(groupId);
            return this;
        }

        public Builder basePackage(String basePackage) {
            ctx.setBasePackage(basePackage);
            return this;
        }

        public Builder outputDir(String outputDir) {
            ctx.setOutputDir(outputDir);
            return this;
        }

        public Builder overwrite(boolean overwrite) {
            ctx.setOverwrite(overwrite);
            return this;
        }

        public Builder author(String author) {
            ctx.setAuthor(author);
            return this;
        }

        public Builder jsonConfig(String path) {
            ctx.setJsonConfigPath(path);
            return this;
        }

        public Builder jdbcUrl(String url) {
            ctx.setJdbcUrl(url);
            return this;
        }

        public Builder jdbcUsername(String username) {
            ctx.setJdbcUsername(username);
            return this;
        }

        public Builder jdbcPassword(String password) {
            ctx.setJdbcPassword(password);
            return this;
        }

        public Builder options(Consumer<GenerationOptions> configurer) {
            configurer.accept(ctx.getOptions());
            return this;
        }

        /**
         * 直接设置表信息（编程模式，跳过元数据获取）。
         */
        public Builder tables(TableInfo... tables) {
            ctx.setTables(Arrays.asList(tables));
            return this;
        }

        /**
         * 直接设置表信息列表。
         */
        public Builder tables(List<TableInfo> tables) {
            ctx.setTables(tables);
            return this;
        }

        /**
         * 执行代码生成。
         */
        public void generate() {
            CodeGenerator generator = new CodeGenerator();
            generator.generate(ctx);
        }

        /**
         * 获取构建的上下文（用于调试或扩展）。
         */
        public GenerationContext getContext() {
            return ctx;
        }
    }
}
