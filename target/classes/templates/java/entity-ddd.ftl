<#import "../macros.ftl" as m>
<@m.fileHeader packageName=entityPackage description="${tableComment!entityName}领域实体（聚合根）"/>

<@m.columnAnnotationImports columns hasCreateTime hasUpdateTime pkColumn />
import java.io.Serializable;
<@m.importTypes columns />

/**
 * ${tableComment!entityName}领域实体（聚合根）。
 *
 * <p>封装 ${tableComment!entityName} 的核心业务规则和领域行为，
 * 基础设施层通过 MyBatis-Plus 注解实现持久化映射。</p>
 */
@TableName("`${tableName}`")
public class ${entityName} implements Serializable {

    private static final long serialVersionUID = 1L;

<#list columns as col>
    /** ${col.columnComment!col.fieldName} */
<@m.columnAnnotation col options />
    private ${col.javaTypeShort} ${col.fieldName};

</#list>

    // ======================== 工厂方法 ========================

    /**
     * 创建新实体。
     */
    public static ${entityName} create(<#list insertColumns as col><#if !col.autoFillInsert>${col.javaTypeShort} ${col.fieldName}<#if col?has_next && !col?is_last>, </#if></#if></#list>) {
        ${entityName} entity = new ${entityName}();
<#list insertColumns as col>
<#if !col.autoFillInsert>
        entity.${col.fieldName} = ${col.fieldName};
</#if>
</#list>
        return entity;
    }

    // ======================== 领域行为 ========================

    /**
     * 更新实体信息。
     */
    public void updateInfo(<#list updateColumns as col><#if !col.primaryKey && !col.autoFillInsert && !col.autoFillUpdate>${col.javaTypeShort} ${col.fieldName}<#if col?has_next && !col?is_last>, </#if></#if></#list>) {
<#list updateColumns as col>
<#if !col.primaryKey && !col.autoFillInsert && !col.autoFillUpdate>
        this.${col.fieldName} = ${col.fieldName};
</#if>
</#list>
    }

<#if hasDeleted>
    /**
     * 标记为已删除（逻辑删除）。
     */
    public void markAsDeleted() {
        this.deleted = ${options.logicDeleteValue!"1"};
    }

</#if>
    // ======================== getters/setters ========================

<@m.gettersAndSetters columns />
<@m.toStringBlock entityName columns />
}
