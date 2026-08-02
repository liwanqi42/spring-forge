<#import "../macros.ftl" as m>
<#assign mapperDesc = isDdd?then("仓储接口", "Mapper 接口")>
<@m.fileHeader packageName=mapperPackage description="${tableComment!entityName} ${mapperDesc}"/>

import ${entityPackage}.${entityName};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

<#if isDdd>
/**
 * ${tableComment!entityName} 仓储接口。
 *
 * <p>继承 MyBatis-Plus BaseMapper，自动获得 CRUD 能力。
 * 无需额外实现类——MyBatis-Plus 在运行时通过动态代理生成实现。
 * 复杂查询可在对应 XML 中扩展。</p>
 */
<#else>
/**
 * ${tableComment!entityName} Mapper 接口
 */
</#if>
@Mapper
public interface ${entityName}${mapperClassSuffix} extends BaseMapper<${entityName}> {

}
