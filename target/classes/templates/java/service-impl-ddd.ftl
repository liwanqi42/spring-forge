<#import "../macros.ftl" as m>
<#assign implName = entityName + serviceClassSuffix + "Impl">
<#assign ifaceName = entityName + serviceClassSuffix>
<#assign repoVar = entityNameLower + mapperClassSuffix>
<#assign domainServiceVar = entityNameLower + "DomainService">
<@m.fileHeader packageName=serviceImplPackage description="${tableComment!entityName} 应用服务实现"/>

import ${entityPackage}.${entityName};
import ${mapperPackage}.${entityName}${mapperClassSuffix};
import ${dtoPackage}.${entityName}CreateDTO;
import ${dtoPackage}.${entityName}UpdateDTO;
import ${dtoPackage}.${entityName}QueryDTO;
import ${voPackage}.${entityName}DetailVO;
import ${voPackage}.${entityName}ListVO;
import ${converterPackage!"${basePackage}.converter"}.${entityName}Converter;
import ${servicePackage}.${ifaceName};
import ${basePackage}.domain.service.${entityName}DomainService;
import ${basePackage}.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ${tableComment!entityName} 应用服务实现。
 *
 * <p>编排用例流程：
 * <ol>
 *   <li>DTO → Entity 转换</li>
 *   <li>调用领域服务进行业务规则校验</li>
 *   <li>通过仓储持久化</li>
 *   <li>Entity → VO 转换返回</li>
 * </ol>
 * </p>
 */
@Service
@Transactional
public class ${implName} implements ${ifaceName} {

    @Resource
    private ${entityName}${mapperClassSuffix} ${repoVar};

    @Resource
    private ${entityName}DomainService ${domainServiceVar};

    @Override
    public ${entityName}DetailVO create(${entityName}CreateDTO dto) {
        ${entityName} entity = ${entityName}Converter.toEntity(dto);
        ${domainServiceVar}.validateForCreation(entity);
        ${repoVar}.insert(entity);
        return ${entityName}Converter.toDetailVO(entity);
    }

    @Override
    public ${entityName}DetailVO update(${entityName}UpdateDTO dto) {
        ${entityName} entity = ${repoVar}.selectById(dto.get${pkColumn.fieldNameUpper}());
        if (entity == null) {
            throw new BizException("${tableComment!entityName}不存在");
        }
        ${entityName}Converter.mergeEntity(dto, entity);
        ${domainServiceVar}.validateForUpdate(entity);
        ${repoVar}.updateById(entity);
        return ${entityName}Converter.toDetailVO(entity);
    }

    @Override
    public void deleteById(${pkJavaType} id) {
        ${repoVar}.deleteById(id);
    }

    @Override
    public ${entityName}DetailVO getById(${pkJavaType} id) {
        ${entityName} entity = ${repoVar}.selectById(id);
        return ${entityName}Converter.toDetailVO(entity);
    }

    @Override
    public List<${entityName}ListVO> list(${entityName}QueryDTO query) {
        LambdaQueryWrapper<${entityName}> wrapper = new LambdaQueryWrapper<>();
<@m.queryWrapperConditions queryColumns entityName />
        wrapper.orderByDesc(${entityName}::get${pkColumn.fieldNameUpper});
        return ${repoVar}.selectList(wrapper).stream()
                .map(${entityName}Converter::toListVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<${entityName}ListVO> page(${entityName}QueryDTO query, long pageNum, long pageSize) {
        LambdaQueryWrapper<${entityName}> wrapper = new LambdaQueryWrapper<>();
<@m.queryWrapperConditions queryColumns entityName />
        wrapper.orderByDesc(${entityName}::get${pkColumn.fieldNameUpper});
        Page<${entityName}> page = ${repoVar}.selectPage(
                Page.of(pageNum, pageSize), wrapper);
        Page<${entityName}ListVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(${entityName}Converter::toListVO)
                .collect(Collectors.toList()));
        return voPage;
    }
}
