<#import "../macros.ftl" as m>
<@m.fileHeader packageName="${basePackage}.domain.service" description="${tableComment!entityName} 领域服务接口"/>

import ${basePackage}.domain.model.${entityName};
import java.util.List;

/**
 * ${tableComment!entityName} 领域服务接口。
 *
 * <p>封装跨实体、无自然归属的领域业务逻辑。
 * 单一实体的行为应放在 {@link ${entityName}} 实体类中。</p>
 */
public interface ${entityName}DomainService {

    /**
     * 业务规则校验（示例：创建前检查）。
     *
     * @param entity 待校验的实体
     * @throws IllegalArgumentException 校验失败时抛出
     */
    void validateForCreation(${entityName} entity);

    /**
     * 业务规则校验（示例：更新前检查）。
     *
     * @param entity 待校验的实体
     * @throws IllegalArgumentException 校验失败时抛出
     */
    void validateForUpdate(${entityName} entity);
}
