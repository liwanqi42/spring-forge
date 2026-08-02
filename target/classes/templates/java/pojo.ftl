<#import "../macros.ftl" as m>
<#-- 统一 POJO 模板：通过 type 控制生成 DTO / VO
     type: create-dto | update-dto | query-dto | list-vo | detail-vo -->
<#if type == "create-dto">
<@m.simplePojo packageName=dtoOutputPackage className="${entityName}CreateDTO"
              description="${tableComment!entityName}新增入参DTO"
              columns=insertColumns
              extraImports="import jakarta.validation.constraints.*;" />
<#elseif type == "update-dto">
<#assign nonPkUpdateCols = updateColumns?filter(col -> !col.primaryKey)>
<@m.pojoBegin packageName=dtoOutputPackage className="${entityName}UpdateDTO"
              description="${tableComment!entityName}修改入参DTO"
              columns=updateColumns
              extraImports="import jakarta.validation.constraints.*;" />

    /** ${pkColumn.columnComment!"主键ID"} */
    @NotNull(message = "主键ID不能为空")
    private ${pkJavaType} ${pkColumn.fieldName};

<@m.beanFields nonPkUpdateCols />

    public ${pkJavaType} get${pkColumn.fieldNameUpper}() {
        return ${pkColumn.fieldName};
    }

    public void set${pkColumn.fieldNameUpper}(${pkJavaType} ${pkColumn.fieldName}) {
        this.${pkColumn.fieldName} = ${pkColumn.fieldName};
    }

<@m.gettersAndSetters nonPkUpdateCols />
    @Override
    public String toString() {
        return "${entityName}UpdateDTO{${pkColumn.fieldName}=" + ${pkColumn.fieldName} + "}";
    }
}
<#elseif type == "query-dto">
<@m.simplePojo packageName=dtoOutputPackage className="${entityName}QueryDTO"
              description="${tableComment!entityName}查询入参DTO"
              columns=queryColumns />
<#elseif type == "list-vo">
<#assign voCols = columns?filter(col -> !col.systemField)>
<@m.simplePojo packageName=voOutputPackage className="${entityName}ListVO"
              description="${tableComment!entityName}列表视图对象（已脱敏，不暴露系统字段）"
              columns=voCols />
<#elseif type == "detail-vo">
<#assign voCols = columns?filter(col -> !col.systemField)>
<@m.simplePojo packageName=voOutputPackage className="${entityName}DetailVO"
              description="${tableComment!entityName}详情视图对象（已脱敏，不暴露系统字段）"
              columns=voCols />
</#if>
