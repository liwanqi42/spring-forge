<#import "../macros.ftl" as m>
<@m.fileHeader packageName=entityPackage description="${tableComment!entityName}实体类"/>

<@m.columnAnnotationImports columns hasCreateTime hasUpdateTime pkColumn />
import java.io.Serializable;
<@m.importTypes columns />

/**
 * ${tableComment!entityName}实体类
 */
@TableName("`${tableName}`")
public class ${entityName} implements Serializable {

    private static final long serialVersionUID = 1L;

<#list columns as col>
    /** ${col.columnComment!col.fieldName} */
<@m.columnAnnotation col options />
    private ${col.javaTypeShort} ${col.fieldName};

</#list>
<@m.gettersAndSetters columns />
<@m.toStringBlock entityName columns />
}
