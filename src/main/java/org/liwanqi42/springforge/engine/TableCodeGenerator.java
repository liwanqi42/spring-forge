package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.engine.ddd.DddStrategy;
import org.liwanqi42.springforge.engine.mvc.MvcStrategy;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.TableInfo;

/**
 * 单表代码生成器。
 *
 * <p>根据模式（MVC/DDD）委托对应的策略执行生成。</p>
 */

public class TableCodeGenerator {

    private final MvcStrategy mvcStrategy;
    private final DddStrategy dddStrategy;

    public TableCodeGenerator(FreeMarkerTemplateEngine engine) {
        this.mvcStrategy = new MvcStrategy(engine);
        this.dddStrategy = new DddStrategy(engine);
    }

    /**
     * 为指定表生成代码。
     */
    public void generate(GenerationContext ctx, TableInfo table) {
        var strategy = ctx.getMode() == GenerationMode.DDD ? dddStrategy : mvcStrategy;
        strategy.generate(ctx, table);
    }
}
