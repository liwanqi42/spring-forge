package org.liwanqi42.springforge;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Spring-Forge 自动配置入口。
 *
 * <p>通过 {@link Import} 精确导入所需 Bean，避免 {@code ComponentScan} 的宽泛扫描。</p>
 */
@AutoConfiguration
@Import(SpringForgeRunner.class)
public class SpringForgeAutoConfig {
}
