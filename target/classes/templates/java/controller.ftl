<#import "../macros.ftl" as m>
<#assign svcClass = entityName + serviceClassSuffix>
<#assign svcVar = entityNameLower + serviceClassSuffix>
<@m.fileHeader packageName=controllerPackage description="${tableComment!entityName}" + (isDdd)?then(" REST 适配器", "控制器")/>

import ${dtoPackage}.${entityName}CreateDTO;
import ${dtoPackage}.${entityName}UpdateDTO;
import ${dtoPackage}.${entityName}QueryDTO;
import ${voPackage}.${entityName}DetailVO;
import ${voPackage}.${entityName}ListVO;
import ${servicePackage}.${svcClass};
import ${basePackage}.common.Result;
import ${basePackage}.common.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

<#if isDdd>
/**
 * ${tableComment!entityName} REST 适配器。
 *
 * <p>接收 HTTP 请求，调用应用服务，返回统一响应。
 * 适配器层不包含任何业务逻辑——仅做协议转换。</p>
 */
<#else>
/**
 * ${tableComment!entityName}控制器
 */
</#if>
@RestController
@RequestMapping("${mappingPath}")
public class ${entityName}Controller {

    @Resource
    private ${svcClass} ${svcVar};

    /**
     * 新增
     */
    @PostMapping
    public Result<${entityName}DetailVO> create(@Valid @RequestBody ${entityName}CreateDTO dto) {
        return Result.ok(${svcVar}.create(dto));
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<${entityName}DetailVO> update(@Valid @RequestBody ${entityName}UpdateDTO dto) {
        return Result.ok(${svcVar}.update(dto));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable ${pkJavaType} id) {
        ${svcVar}.deleteById(id);
        return Result.ok();
    }

    /**
     * 主键查询详情
     */
    @GetMapping("/{id}")
    public Result<${entityName}DetailVO> detail(@PathVariable ${pkJavaType} id) {
        return Result.ok(${svcVar}.getById(id));
    }

    /**
     * 条件列表查询
     */
    @GetMapping("/list")
    public Result<List<${entityName}ListVO>> list(@Valid ${entityName}QueryDTO query) {
        return Result.ok(${svcVar}.list(query));
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public PageResult<${entityName}ListVO> page(@Valid ${entityName}QueryDTO query,
                                                 @RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "10") long pageSize) {
        Page<${entityName}ListVO> page = ${svcVar}.page(query, pageNum, pageSize);
        return PageResult.of(page);
    }
}
