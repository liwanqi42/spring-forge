package org.liwanqi42.springforge.model;

import lombok.Data;

/**
 * 代码生成选项。
 */
@Data
public class GenerationOptions {
    /** 是否生成 DDL SQL */
    private boolean generateDdl;
    /** 是否生成单元测试 */
    private boolean generateTests;
    /** 逻辑删除字段名，空字符串表示禁用 */
    private String logicDeleteField = "deleted";
    /** 逻辑删除值 */
    private String logicDeleteValue = "1";
    /** 逻辑未删除值 */
    private String logicNotDeleteValue = "0";
    /** 乐观锁字段名，空字符串表示禁用 */
    private String optimisticLockField = "version";
    /** 自动填充插入字段（逗号分隔） */
    private String autoFillInsertFields = "createTime,createBy";
    /** 自动填充更新字段（逗号分隔） */
    private String autoFillUpdateFields = "updateTime,updateBy";
    /** MyBatis-Plus 主键 ID 类型 */
    private String idType = "ASSIGN_ID";
    /** 表前缀（生成实体名时去除） */
    private String tablePrefix = "";
    /** 是否使用 Lombok */
    private boolean useLombok = true;
    /** 日期格式 */
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    /** 使用精简 MyBatis 配置（信任 Spring Boot 自动配置 DataSource/SqlSessionFactory） */
    private boolean minimalMyBatisConfig = true;
}
