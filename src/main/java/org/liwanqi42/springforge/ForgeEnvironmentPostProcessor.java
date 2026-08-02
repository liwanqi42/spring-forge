package org.liwanqi42.springforge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.util.Properties;

/**
 * 环境后处理器：当项目根目录存在 {@code spring-forge-config.json} 时，
 * 排除 DataSource 自动配置以避免 "Failed to configure a DataSource" 错误。
 *
 * <p>仅在代码生成阶段生效，不会影响生成后项目的正常运行。</p>
 */
public class ForgeEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ForgeEnvironmentPostProcessor.class);
    private static final String CONFIG_FILE = "spring-forge-config.json";
    private static final String LOCK_FILE = ".spring-forge.lock";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        File configFile = new File(CONFIG_FILE);
        File lockFile = new File(LOCK_FILE);

        logger.info("ForgeEnvironmentPostProcessor 被调用 — config={}, lock={}",
                configFile.exists(), lockFile.exists());

        // 没有配置文件 → 正常项目运行，不干预
        if (!configFile.exists()) {
            logger.info("无配置文件，跳过 DataSource 排除");
            return;
        }

        // 有配置文件但代码已经生成过（lock 文件存在）→ 不再排除 DataSource
        if (lockFile.exists()) {
            logger.info("检测到 lock 文件，跳过 DataSource 排除");
            return;
        }

        // 有配置文件且尚未生成代码 → 代码生成模式，排除 DataSource
        logger.info("首次运行，排除 DataSource 自动配置");

        Properties props = new Properties();
        props.put("spring.autoconfigure.exclude",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration");
        environment.getPropertySources()
                .addFirst(new PropertiesPropertySource("spring-forge-excludes", props));
    }
}
