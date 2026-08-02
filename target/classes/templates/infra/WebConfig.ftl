package ${basePackage}.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 全局配置（CORS 跨域 + 日期格式）。
 *
 * <p>Jackson 序列化由 Spring Boot 自动配置（spring.jackson.*），
 * 无需手动创建 ObjectMapper Bean，避免 Jackson 版本兼容问题。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
