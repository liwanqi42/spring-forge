-- ============================================
-- ${tableComment!entityName} 建表 DDL
-- 由 Spring-Forge 自动生成
-- ============================================
CREATE TABLE IF NOT EXISTS `${tableName}` (
<#list columns as col>
    `${col.rawName}` ${col.jdbcType}<#if col.maxLength gt 0>(${col.maxLength})</#if> <#if !col.nullable>NOT NULL</#if><#if col.autoIncrement> AUTO_INCREMENT</#if><#if col.defaultValue?? && col.defaultValue != ""> DEFAULT ${col.defaultValue}</#if> COMMENT '${col.columnComment!"${col.fieldName}"}'<#if col?has_next>,</#if>
</#list>
<#if pkColumn??>,
    PRIMARY KEY (`${pkColumn.rawName}`)
</#if>
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='${tableComment!"${entityName}"}';
