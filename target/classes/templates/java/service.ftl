<#import "../macros.ftl" as m>
<#assign ifaceName = entityName + serviceClassSuffix>
<@m.fileHeader packageName=servicePackage description="${tableComment!entityName}" + (isDdd)?then(" 应用服务接口", "业务接口")/>

<#if !isDdd>
import ${entityPackage}.${entityName};
</#if>
import ${dtoPackage}.${entityName}CreateDTO;
import ${dtoPackage}.${entityName}UpdateDTO;
import ${dtoPackage}.${entityName}QueryDTO;
import ${voPackage}.${entityName}DetailVO;
import ${voPackage}.${entityName}ListVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

<#if isDdd>
/**
 * ${tableComment!entityName} 应用服务接口。
 *
 * <p>编排用例流程，协调仓储（Repository）和领域服务（DomainService）。
 * 应用层不包含业务规则——所有业务逻辑在领域层。</p>
 */
<#else>
/**
 * ${tableComment!entityName}业务接口
 */
</#if>
public interface ${ifaceName} {

    /**
     * 新增
     */
    ${entityName}DetailVO create(${entityName}CreateDTO dto);

    /**
     * 修改
     */
    ${entityName}DetailVO update(${entityName}UpdateDTO dto);

    /**
     * 根据主键删除
     */
    void deleteById(${pkJavaType} id);

    /**
     * 根据主键查询详情
     */
    ${entityName}DetailVO getById(${pkJavaType} id);

    /**
     * 条件列表查询
     */
    List<${entityName}ListVO> list(${entityName}QueryDTO query);

    /**
     * 分页查询
     */
    Page<${entityName}ListVO> page(${entityName}QueryDTO query, long pageNum, long pageSize);
}
