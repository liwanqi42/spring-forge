package org.liwanqi42.springforge.engine.ddd;

import org.liwanqi42.springforge.engine.AbstractStrategy;
import org.liwanqi42.springforge.engine.DtoVoConverterGenerator;
import org.liwanqi42.springforge.engine.FreeMarkerTemplateEngine;
import org.liwanqi42.springforge.engine.GeneratorDescriptor;
import org.liwanqi42.springforge.engine.SingleFileGenerator;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.TableInfo;

import java.util.Map;

/**
 * DDD 模式代码生成策略实现。
 *
 * <p>职责：统一编排各个文件生成器，基于单张数据表信息，按 DDD 分层规范输出完整领域代码结构。
 * 分层顺序遵循依赖流向：领域层 → 应用层 → 外部适配层。</p>
 *
 * <p>分层说明：
 * <ul>
 *     <li>领域层（domain）：领域实体、仓储接口、领域服务；承载业务规则与核心领域逻辑</li>
 *     <li>应用层（application）：DTO/VO、转换器、应用服务；协调领域能力，编排业务流程，不包含业务规则</li>
 *     <li>适配层（adapter.web）：Controller；接收外部 HTTP 请求，适配输入输出</li>
 * </ul>
 */
public class DddStrategy extends AbstractStrategy {

    // ====================== 单文件生成器：每个实例对应一类模板 + 输出路径 + 类后缀 ======================
    // 领域实体 Entity 生成器：domain.model
    private final SingleFileGenerator entityGen;
    // 仓储接口 Repository 生成器：domain.repository（替代传统 Mapper）
    private final SingleFileGenerator repositoryGen;
    // Repository MyBatis XML 映射文件生成器：resources/mapper
    private final SingleFileGenerator repositoryXmlGen;
    // 领域服务接口 DomainService 生成器：domain.service
    private final SingleFileGenerator domainServiceGen;
    // 领域服务实现 DomainServiceImpl 生成器：domain.service.impl
    private final SingleFileGenerator domainServiceImplGen;
    // 应用服务接口 ApplicationService 生成器：application.service
    private final SingleFileGenerator appServiceGen;
    // 应用服务实现 ApplicationServiceImpl 生成器：application.service.impl
    private final SingleFileGenerator appServiceImplGen;
    // HTTP 控制器 Controller 生成器：adapter.web
    private final SingleFileGenerator controllerGen;

    // ====================== 多文件复合生成器：一次性产出 DTO / VO / Converter 一组文件 ======================
    /** DTO、VO、对象转换器批量生成器，复用通用模板 */
    private final DtoVoConverterGenerator dtoVoGen;

    /**
     * 构造器：按 DDD 分层约定初始化所有模板生成器。
     */
    public DddStrategy(FreeMarkerTemplateEngine engine) {
        super(engine);
        // 领域实体（DDD 专用模板，含工厂方法和领域行为）
        this.entityGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/entity.ftl", ".domain.model", ""));
        // 仓储接口
        this.repositoryGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/mapper.ftl", ".domain.repository", "Repository"));
        // Repository 对应的 MyBatis XML
        this.repositoryXmlGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.resource("java/mapper-xml.ftl", "mapper", "Repository.xml"));
        // 领域服务接口
        this.domainServiceGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/domain-service.ftl", ".domain.service", "DomainService"));
        // 领域服务实现类
        this.domainServiceImplGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/domain-service-impl.ftl", ".domain.service.impl", "DomainServiceImpl"));
        // 应用服务接口
        this.appServiceGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/service.ftl", ".application.service", "ApplicationService"));
        // 应用服务实现类（DDD 专用模板，使用 Repository + DomainService）
        this.appServiceImplGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/service-impl.ftl", ".application.service.impl", "ApplicationServiceImpl"));
        // Web 控制器（适配层）
        this.controllerGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/controller.ftl", ".adapter.web", "Controller"));
        // DTO/VO/转换器复合生成器
        this.dtoVoGen = new DtoVoConverterGenerator(engine);
    }

    /**
     * 对单张数据表执行完整 DDD 代码生成流水线。
     * 执行顺序遵循分层依赖顺序：领域层 → 应用层 → 适配层。
     */
    @Override
    public void generate(GenerationContext ctx, TableInfo table) {
        Map<String, Object> model = buildDataModel(ctx, table);

        // —— 领域层（Domain Layer）：实体、仓储、领域服务 ——
        entityGen.generate(ctx, table, model);
        repositoryGen.generate(ctx, table, model);
        repositoryXmlGen.generate(ctx, table, model);
        domainServiceGen.generate(ctx, table, model);
        domainServiceImplGen.generate(ctx, table, model);

        // —— 应用层（Application Layer）：DTO/VO/Converter + 应用服务 ——
        dtoVoGen.generateDtos(ctx, table, model, ".application.dto");
        dtoVoGen.generateVos(ctx, table, model, ".application.vo");
        dtoVoGen.generateConverter(ctx, table, model, ".application.converter");
        appServiceGen.generate(ctx, table, model);
        appServiceImplGen.generate(ctx, table, model);

        // —— 外部适配层（Adapter Layer）：HTTP Controller ——
        controllerGen.generate(ctx, table, model);
    }

    /**
     * 覆盖模板数据模型中的包路径变量，把通用 MVC 模板变量映射为 DDD 分层包。
     * <p>作用：让一套共享 ftl 模板无需大量修改，即可同时支持 MVC / DDD 两种策略。</p>
     */
    @Override
    protected void applyPackageOverrides(Map<String, Object> data, GenerationContext ctx) {
        String bp = ctx.getBasePackage();
        data.put("entityPackage", bp + ".domain.model");
        data.put("dtoPackage", bp + ".application.dto");
        data.put("voPackage", bp + ".application.vo");
        data.put("dtoOutputPackage", bp + ".application.dto");
        data.put("voOutputPackage", bp + ".application.vo");
        data.put("converterPackage", bp + ".application.converter");
        data.put("mapperPackage", bp + ".domain.repository");
        data.put("servicePackage", bp + ".application.service");
        data.put("serviceImplPackage", bp + ".application.service.impl");
        data.put("controllerPackage", bp + ".adapter.web");

        // 类名后缀覆盖，区分 MVC Service 与 DDD ApplicationService / Repository
        data.put("serviceClassSuffix", "ApplicationService");
        data.put("mapperClassSuffix", "Repository");

        // 模板内可通过 isDdd 分支渲染 DDD 专属注释、导入、结构
        data.put("isDdd", true);
    }
}