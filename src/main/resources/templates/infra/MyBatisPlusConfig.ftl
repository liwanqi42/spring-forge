package ${basePackage}.common.config;

<#if myBatisConfigMode == "full">
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
<#else>
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
</#if>

<#if myBatisConfigMode == "full">
/**
 * MyBatis-Plus 手动配置。
 *
 * <p>显式声明 DataSource / SqlSessionFactory / SqlSessionTemplate，
 * 不依赖 Spring Boot 自动配置，确保跨 Spring Boot 版本兼容。</p>
<#else>
/**
 * MyBatis-Plus 配置。
 *
 * <p>DataSource / SqlSessionFactory / SqlSessionTemplate 由
 * Spring Boot 自动配置管理，通过 application.yml 调整参数。</p>
</#if>
 */
@Configuration
@MapperScan("${mapperScanPackage}")
public class MyBatisPlusConfig {

<#if myBatisConfigMode == "full">
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(Environment env) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env.getProperty("spring.datasource.url",
                "jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1"));
        ds.setUsername(env.getProperty("spring.datasource.username", "sa"));
        ds.setPassword(env.getProperty("spring.datasource.password", ""));
        ds.setDriverClassName(env.getProperty("spring.datasource.driver-class-name",
                "org.h2.Driver"));
        return ds;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath*:/mapper/**/*.xml"));
        return factory.getObject();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /** 分页插件 */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
<#else>
    /** 分页插件 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
</#if>
}
