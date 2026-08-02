<#import "../macros.ftl" as m>
<@m.fileHeader packageName="${converterPackage!'${basePackage}.converter'}" description="${entityName}数据转换器"/>

import ${entityPackage!"${basePackage}.entity"}.${entityName};
import ${dtoPackage!"${basePackage}.dto"}.${entityName}CreateDTO;
import ${dtoPackage!"${basePackage}.dto"}.${entityName}UpdateDTO;
import ${voPackage!"${basePackage}.vo"}.${entityName}DetailVO;
import ${voPackage!"${basePackage}.vo"}.${entityName}ListVO;

/**
 * ${entityName} 数据转换器
 *
 * <p>统一封装 Entity ↔ DTO ↔ VO 的双向转换，禁止散落 BeanUtils 拷贝。</p>
 */
public class ${entityName}Converter {

    /**
     * CreateDTO → Entity
     */
    public static ${entityName} toEntity(${entityName}CreateDTO dto) {
        if (dto == null) return null;
        ${entityName} entity = new ${entityName}();
<#list insertColumns as col>
<#if !col.autoFillInsert>
        entity.set${col.fieldNameUpper}(dto.get${col.fieldNameUpper}());
</#if>
</#list>
        return entity;
    }

    /**
     * UpdateDTO → Entity（合并到已有实体）
     */
    public static void mergeEntity(${entityName}UpdateDTO dto, ${entityName} entity) {
        if (dto == null || entity == null) return;
<#list updateColumns as col>
<#if !col.primaryKey && !col.autoFillInsert && !col.autoFillUpdate>
        if (dto.get${col.fieldNameUpper}() != null) {
            entity.set${col.fieldNameUpper}(dto.get${col.fieldNameUpper}());
        }
</#if>
</#list>
    }

    /**
     * Entity → DetailVO
     */
    public static ${entityName}DetailVO toDetailVO(${entityName} entity) {
        if (entity == null) return null;
        ${entityName}DetailVO vo = new ${entityName}DetailVO();
<#list columns as col>
<#if !col.systemField>
        vo.set${col.fieldNameUpper}(entity.get${col.fieldNameUpper}());
</#if>
</#list>
        return vo;
    }

    /**
     * Entity → ListVO
     */
    public static ${entityName}ListVO toListVO(${entityName} entity) {
        if (entity == null) return null;
        ${entityName}ListVO vo = new ${entityName}ListVO();
<#list columns as col>
<#if !col.systemField>
        vo.set${col.fieldNameUpper}(entity.get${col.fieldNameUpper}());
</#if>
</#list>
        return vo;
    }
}
