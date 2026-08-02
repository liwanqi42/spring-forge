<#import "../macros.ftl" as m>
<@m.fileHeader packageName="${basePackage}.domain.service.impl" description="${tableComment!entityName} 领域服务实现"/>

import ${basePackage}.domain.model.${entityName};
import ${basePackage}.domain.repository.${entityName}Repository;
import ${basePackage}.domain.service.${entityName}DomainService;
import ${basePackage}.common.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ${tableComment!entityName} 领域服务实现。
 *
 * <p>纯领域逻辑，不依赖基础设施（仅依赖 Repository 接口）。
 * 此处的 Repository 由 MyBatis-Plus 在运行时提供动态代理实现。</p>
 */
@Service
public class ${entityName}DomainServiceImpl implements ${entityName}DomainService {

    private static final Logger log = LoggerFactory.getLogger(${entityName}DomainServiceImpl.class);

    @Override
    public void validateForCreation(${entityName} entity) {
        if (entity == null) {
            throw new IllegalArgumentException("${tableComment!entityName} 实体不能为空");
        }
        // TODO: 在此添加创建前的业务规则校验
        log.debug("${entityName} 创建校验通过：{}", entity.get${pkColumn.fieldNameUpper}());
    }

    @Override
    public void validateForUpdate(${entityName} entity) {
        if (entity == null) {
            throw new IllegalArgumentException("${tableComment!entityName} 实体不能为空");
        }
        if (entity.get${pkColumn.fieldNameUpper}() == null) {
            throw new BizException("${tableComment!entityName} 主键不能为空");
        }
        // TODO: 在此添加更新前的业务规则校验
        log.debug("${entityName} 更新校验通过：{}", entity.get${pkColumn.fieldNameUpper}());
    }
}
