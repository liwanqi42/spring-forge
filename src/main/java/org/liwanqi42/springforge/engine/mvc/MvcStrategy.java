package org.liwanqi42.springforge.engine.mvc;

import org.liwanqi42.springforge.engine.AbstractStrategy;
import org.liwanqi42.springforge.engine.DtoVoConverterGenerator;
import org.liwanqi42.springforge.engine.FreeMarkerTemplateEngine;
import org.liwanqi42.springforge.engine.GeneratorDescriptor;
import org.liwanqi42.springforge.engine.SingleFileGenerator;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.TableInfo;

import java.util.Map;

/**
 * MVC 模式代码生成策略实现。
 *
 * <p>职责：编排各文件生成器，基于单张数据表元信息，生成传统 Spring MVC 分层代码。
 * 分层顺序遵循常规开发依赖流向：实体 → Mapper → DTO/VO/转换器 → Service → Controller。</p>
 *
 * <p>分层说明：
 * <ul>
 *     <li>entity：数据库映射实体</li>
 *     <li>mapper：MyBatis 数据访问接口</li>
 *     <li>service / service.impl：业务接口与实现</li>
 *     <li>controller：HTTP 请求入口</li>
 *     <li>dto / vo / converter：入参、出参与对象转换工具</li>
 * </ul>
 * </p>
 * </p>
 */
public class MvcStrategy extends AbstractStrategy {

    // ====================== 单文件生成器：一类模板对应一个生成器实例 ======================
    // 数据库实体 Entity 生成器
    private final SingleFileGenerator entityGen;
    // MyBatis Mapper 接口生成器
    private final SingleFileGenerator mapperGen;
    // Mapper 对应的 MyBatis XML 文件生成器
    private final SingleFileGenerator mapperXmlGen;
    // Service 业务接口生成器
    private final SingleFileGenerator serviceGen;
    // Service 业务实现类生成器
    private final SingleFileGenerator serviceImplGen;
    // HTTP Controller 控制器生成器
    private final SingleFileGenerator controllerGen;

    // ====================== 复合生成器：批量产出 DTO、VO、Converter 一组文件 ======================
    // DTO、VO、对象转换器批量生成器
    private final DtoVoConverterGenerator dtoVoGen;

    /**
     * 构造器：初始化 MVC 分层所有模板生成器。
     */
    public MvcStrategy(FreeMarkerTemplateEngine engine) {
        super(engine);
        this.entityGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/entity.ftl", ".entity", ""));
        this.mapperGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/mapper.ftl", ".mapper", "Mapper"));
        this.mapperXmlGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.resource("java/mapper-xml.ftl", "mapper", "Mapper.xml"));
        this.serviceGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/service.ftl", ".service", "Service"));
        this.serviceImplGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/service-impl.ftl", ".service.impl", "ServiceImpl"));
        this.controllerGen = new SingleFileGenerator(engine,
                GeneratorDescriptor.java("java/controller.ftl", ".controller", "Controller"));
        this.dtoVoGen = new DtoVoConverterGenerator(engine);
    }

    /**
     * 对单张数据表执行完整 MVC 代码生成流水线。
     */
    @Override
    public void generate(GenerationContext ctx, TableInfo table) {
        Map<String, Object> model = buildDataModel(ctx, table);

        entityGen.generate(ctx, table, model);
        mapperGen.generate(ctx, table, model);
        mapperXmlGen.generate(ctx, table, model);
        dtoVoGen.generateDtos(ctx, table, model, ".dto");
        dtoVoGen.generateVos(ctx, table, model, ".vo");
        dtoVoGen.generateConverter(ctx, table, model, ".converter");
        serviceGen.generate(ctx, table, model);
        serviceImplGen.generate(ctx, table, model);
        controllerGen.generate(ctx, table, model);
    }

    /**
     * 向模板数据模型注入 MVC 标准包路径与类后缀变量。
     * <p>与 DddStrategy 配合，实现一套共享 ftl 模板同时支持两种架构模式；
     * 模板内可通过 {@code isDdd} 分支区分渲染逻辑。</p>
     */
    @Override
    protected void applyPackageOverrides(Map<String, Object> data, GenerationContext ctx) {
        String bp = ctx.getBasePackage();
        data.put("entityPackage", bp + ".entity");
        data.put("dtoPackage", bp + ".dto");
        data.put("voPackage", bp + ".vo");
        data.put("dtoOutputPackage", bp + ".dto");
        data.put("voOutputPackage", bp + ".vo");
        data.put("converterPackage", bp + ".converter");
        data.put("mapperPackage", bp + ".mapper");
        data.put("servicePackage", bp + ".service");
        data.put("serviceImplPackage", bp + ".service.impl");
        data.put("controllerPackage", bp + ".controller");

        data.put("serviceClassSuffix", "Service");
        data.put("mapperClassSuffix", "Mapper");
        data.put("isDdd", false);
    }
}