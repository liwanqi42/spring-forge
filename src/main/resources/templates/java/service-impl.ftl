<#import "../macros.ftl" as m>
<#assign implName = entityName + serviceClassSuffix + "Impl">
<#assign ifaceName = entityName + serviceClassSuffix>
<@m.fileHeader packageName=serviceImplPackage description="${tableComment!entityName}业务实现"/>

import ${entityPackage}.${entityName};
import ${mapperPackage}.${entityName}${mapperClassSuffix};
import ${dtoPackage}.${entityName}CreateDTO;
import ${dtoPackage}.${entityName}UpdateDTO;
import ${dtoPackage}.${entityName}QueryDTO;
import ${voPackage}.${entityName}DetailVO;
import ${voPackage}.${entityName}ListVO;
import ${converterPackage!"${basePackage}.converter"}.${entityName}Converter;
import ${servicePackage}.${ifaceName};
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ${tableComment!entityName}业务实现
 */
@Service
@Transactional
public class ${implName} extends ServiceImpl<${entityName}${mapperClassSuffix}, ${entityName}> implements ${ifaceName} {

    @Override
    public ${entityName}DetailVO create(${entityName}CreateDTO dto) {
        ${entityName} entity = ${entityName}Converter.toEntity(dto);
        save(entity);
        return ${entityName}Converter.toDetailVO(entity);
    }

    @Override
    public ${entityName}DetailVO update(${entityName}UpdateDTO dto) {
        ${entityName} entity = super.getById(dto.get${pkColumn.fieldNameUpper}());
        if (entity == null) {
            throw new RuntimeException("${tableComment!entityName}不存在");
        }
        ${entityName}Converter.mergeEntity(dto, entity);
        updateById(entity);
        return ${entityName}Converter.toDetailVO(entity);
    }

    @Override
    public void deleteById(${pkJavaType} id) {
        removeById(id);
    }

    @Override
    public ${entityName}DetailVO getById(${pkJavaType} id) {
        ${entityName} entity = super.getById(id);
        return ${entityName}Converter.toDetailVO(entity);
    }

    @Override
    public List<${entityName}ListVO> list(${entityName}QueryDTO query) {
        LambdaQueryWrapper<${entityName}> wrapper = new LambdaQueryWrapper<>();
<@m.queryWrapperConditions queryColumns entityName />
        wrapper.orderByDesc(${entityName}::get${pkColumn.fieldNameUpper});
        return list(wrapper).stream()
                .map(${entityName}Converter::toListVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<${entityName}ListVO> page(${entityName}QueryDTO query, long pageNum, long pageSize) {
        LambdaQueryWrapper<${entityName}> wrapper = new LambdaQueryWrapper<>();
<@m.queryWrapperConditions queryColumns entityName />
        wrapper.orderByDesc(${entityName}::get${pkColumn.fieldNameUpper});
        Page<${entityName}> page = page(Page.of(pageNum, pageSize), wrapper);
        Page<${entityName}ListVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(${entityName}Converter::toListVO)
                .collect(Collectors.toList()));
        return voPage;
    }
}
