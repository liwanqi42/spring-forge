package org.liwanqi42.springforge.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成上下文，聚合项目配置、表信息和生成选项。
 */
@Data
public class GenerationContext {
    /** 生成模式 */
    private GenerationMode mode = GenerationMode.MVC;
    /** 项目名称（Maven artifactId） */
    private String projectName = "demo";
    /** Maven groupId */
    private String groupId = "com.example";
    /** Java 基础包名 */
    private String basePackage = "com.example.demo";
    /** 输出目录 */
    private String outputDir = ".";
    /** 是否覆盖已有文件 */
    private boolean overwrite;
    /** 作者名 */
    private String author = "CodeGenerator";
    /** 日期字符串 */
    private String date;

    /** 数据库 JDBC URL（JDBC 模式） */
    private String jdbcUrl;
    /** 数据库用户名 */
    private String jdbcUsername;
    /** 数据库密码 */
    private String jdbcPassword;

    /** JSON 配置文件路径（JSON 模式） */
    private String jsonConfigPath;

    /** 表名过滤列表（JDBC 模式下指定需要生成的表名） */
    private List<String> tableNameFilter;

    /** 需要生成代码的表信息列表 */
    private List<TableInfo> tables = new ArrayList<>();

    /** 生成选项 */
    private GenerationOptions options = new GenerationOptions();
}
