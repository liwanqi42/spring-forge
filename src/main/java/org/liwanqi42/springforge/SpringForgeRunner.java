package org.liwanqi42.springforge;

import org.liwanqi42.springforge.engine.CodeGenerator;
import org.liwanqi42.springforge.model.GenerationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 启动时自动执行代码生成的 Runner。
 *
 * <p>按以下优先级查找配置文件：</p>
 * <ol>
 *   <li>系统属性 {@code spring-forge.config.path}</li>
 *   <li>当前目录下的 {@code spring-forge-config.json}</li>
 * </ol>
 *
 * <p>未找到配置文件时静默跳过（不阻止应用启动）。</p>
 */
@Component
public class SpringForgeRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SpringForgeRunner.class);
    private static final String DEFAULT_CONFIG_FILE = "spring-forge-config.json";

    @Override
    public void run(String... args) {
        String configPath = System.getProperty("spring-forge.config.path");
        File configFile;
        if (configPath != null && !configPath.isEmpty()) {
            configFile = new File(configPath);
        } else {
            configFile = new File(DEFAULT_CONFIG_FILE);
        }

        if (!configFile.exists()) {
            logger.info("未找到配置文件 {}，跳过代码生成。", configFile.getAbsolutePath());
            logger.info("提示：在工作目录创建 spring-forge-config.json 即可自动生成代码。");
            return;
        }

        logger.info("发现配置文件：{}", configFile.getAbsolutePath());
        GenerationContext ctx = JsonConfigLoader.load(configFile);

        CodeGenerator generator = new CodeGenerator();
        generator.generate(ctx);

        logger.info("============================================");
        logger.info("  生成完成！输出目录：{}", ctx.getOutputDir());
        logger.info("  用 IDE 打开该目录，mvn spring-boot:run 启动");
        logger.info("============================================");
    }
}
