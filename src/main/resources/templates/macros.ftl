<#-- ======================= Spring-Forge 共享 FreeMarker 宏 ======================= -->

<#-- ============================ 基础宏 ============================ -->

<#-- 文件头 package 声明 -->
<#macro fileHeader packageName description>
package ${packageName};
</#macro>

<#-- 类 Javadoc -->
<#macro classDoc description>
/**
 * ${description}
 */
</#macro>

<#-- ============================ Import 宏 ============================ -->

<#-- 导入非 java.lang 的列类型 -->
<#macro importTypes columns>
<#list columns as col>
<#if col.javaType?contains(".") && !col.javaType?starts_with("java.lang.")>
import ${col.javaType};
</#if>
</#list>
</#macro>

<#-- ============================ POJO 样板宏 ============================ -->

<#-- 字段声明（含 Javadoc 和校验注解） -->
<#macro beanFields columns>
<#list columns as col>
<#if col.dtoAnnotation?? && col.dtoAnnotation != "">
    ${col.dtoAnnotation}
</#if>
    /** ${col.columnComment!col.fieldName} */
    private ${col.javaTypeShort} ${col.fieldName};

</#list>
</#macro>

<#-- Getter/Setter 对 -->
<#macro gettersAndSetters columns>
<#list columns as col>
    public ${col.javaTypeShort} get${col.fieldNameUpper}() {
        return ${col.fieldName};
    }

    public void set${col.fieldNameUpper}(${col.javaTypeShort} ${col.fieldName}) {
        this.${col.fieldName} = ${col.fieldName};
    }

</#list>
</#macro>

<#-- toString 方法 -->
<#macro toStringBlock className columns>
    @Override
    public String toString() {
        return "${className}{" +
<#list columns as col>
            <#if col?index == 0>"${col.fieldName}=" + ${col.fieldName} +</#if>
            <#if col?index gt 0>", ${col.fieldName}=" + ${col.fieldName} +</#if>
</#list>
            '}';
    }
</#macro>

<#-- ============================ MyBatis-Plus 注解宏 ============================ -->

<#-- 列注解（@TableId / @TableLogic / @Version / @TableField） -->
<#macro columnAnnotation col options>
<#if col.primaryKey>
    @TableId(type = IdType.${options.idType!"ASSIGN_ID"})
<#elseif col.logicDeleteField>
    @TableLogic(value = "${options.logicNotDeleteValue!"0"}", delval = "${options.logicDeleteValue!"1"}")
<#elseif col.versionField>
    @Version
<#elseif col.autoFillInsert && col.autoFillUpdate>
    @TableField(fill = FieldFill.INSERT_UPDATE)
<#elseif col.autoFillInsert>
    @TableField(fill = FieldFill.INSERT)
<#elseif col.autoFillUpdate>
    @TableField(fill = FieldFill.UPDATE)
<#else>
    @TableField("${col.rawName}")
</#if>
</#macro>

<#-- 列注解所需的 import 判断 -->
<#macro columnAnnotationImports columns hasCreateTime hasUpdateTime pkColumn>
import com.baomidou.mybatisplus.annotation.*;
<#if hasCreateTime || hasUpdateTime>
import com.baomidou.mybatisplus.annotation.FieldFill;
</#if>
<#if pkColumn.autoIncrement>
import com.baomidou.mybatisplus.annotation.IdType;
</#if>
</#macro>

<#-- ============================ 查询条件宏 ============================ -->

<#-- LambdaQueryWrapper 条件构建块 -->
<#macro queryWrapperConditions queryColumns entityName>
<#list queryColumns as col>
<#if col.javaTypeShort == "String">
        wrapper.eq(query.get${col.fieldNameUpper}() != null && !query.get${col.fieldNameUpper}().isEmpty(),
                ${entityName}::get${col.fieldNameUpper}, query.get${col.fieldNameUpper}());
<#else>
        wrapper.eq(query.get${col.fieldNameUpper}() != null,
                ${entityName}::get${col.fieldNameUpper}, query.get${col.fieldNameUpper}());
</#if>
</#list>
</#macro>

<#-- ============================ MyBatis XML 列映射宏 ============================ -->

<#-- ResultMap 列映射 -->
<#macro xmlResultMap columns>
<#list columns as col>
<#if col.primaryKey>
        <id column="${col.rawName}" property="${col.fieldName}" jdbcType="${col.xmlJdbcType}"/>
<#else>
        <result column="${col.rawName}" property="${col.fieldName}" jdbcType="${col.xmlJdbcType}"/>
</#if>
</#list>
</#macro>

<#-- BaseColumnList -->
<#macro xmlBaseColumnList columns>
        <#list columns as col>${col.rawName}<#if col?has_next>, </#if></#list>
</#macro>

<#-- QueryCondition where 子句 -->
<#macro xmlQueryCondition queryColumns>
        <where>
<#list queryColumns as col>
<#if col.javaTypeShort == "String">
            <if test="query.${col.fieldName} != null and query.${col.fieldName} != ''">
                AND ${col.rawName} = ${"#"}{query.${col.fieldName}}
            </if>
<#else>
            <if test="query.${col.fieldName} != null">
                AND ${col.rawName} = ${"#"}{query.${col.fieldName}}
            </if>
</#if>
</#list>
        </where>
</#macro>

<#-- Insert 列名列表 -->
<#macro xmlInsertColumns insertColumns>
<#list insertColumns as col>
            ${col.rawName}<#if col?has_next>,</#if>
</#list>
</#macro>

<#-- Insert 值列表 -->
<#macro xmlInsertValues insertColumns>
<#list insertColumns as col>
            ${"#"}{${col.fieldName},jdbcType=${col.xmlJdbcType}}<#if col?has_next>,</#if>
</#list>
</#macro>

<#-- Update set 子句 -->
<#macro xmlUpdateSet updateColumns>
<#list updateColumns as col>
            <if test="${col.fieldName} != null">
                ${col.rawName} = ${"#"}{${col.fieldName},jdbcType=${col.xmlJdbcType}},
            </if>
</#list>
</#macro>

<#-- ============================ 标准 POJO 骨架宏 ============================ -->

<#-- POJO 类头部：package + extraImports + Serializable + importTypes + doc + class + serialVersionUID
     抽离所有 DTO/VO 模板共有的重复样板 -->
<#macro pojoBegin packageName className description columns extraImports="">
package ${packageName};

<#if extraImports != "">
${extraImports}
</#if>
import java.io.Serializable;
<@importTypes columns />

/**
 * ${description}
 */
public class ${className} implements Serializable {

    private static final long serialVersionUID = 1L;
</#macro>

<#-- POJO 类尾部：toString + 闭合括号 -->
<#macro pojoEnd className columns>
<@toStringBlock className columns />
}
</#macro>

<#-- 完整简单 POJO：pojoBegin + beanFields + gettersAndSetters + pojoEnd
     适用于 create-dto / query-dto / list-vo / detail-vo 等无特殊字段的模板 -->
<#macro simplePojo packageName className description columns extraImports="">
<@pojoBegin packageName=packageName className=className description=description columns=columns extraImports=extraImports />
<@beanFields columns />
<@gettersAndSetters columns />
<@pojoEnd className=className columns=columns />
</#macro>
