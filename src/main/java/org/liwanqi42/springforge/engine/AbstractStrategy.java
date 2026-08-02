package org.liwanqi42.springforge.engine;

import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.SystemFieldDetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成策略抽象基类。
 */
public abstract class AbstractStrategy {

    protected final FreeMarkerTemplateEngine engine;

    protected AbstractStrategy(FreeMarkerTemplateEngine engine) {
        this.engine = engine;
    }

    /**
     * 构建 FreeMarker 数据模型。
     *
     * <p>模板方法模式：公共部分在此实现，模式特定的包变量由子类通过</p>
     */
    protected Map<String, Object> buildDataModel(GenerationContext ctx, TableInfo table) {
        Map<String, Object> data = new HashMap<>();
        data.put("author", ctx.getAuthor());
        data.put("date", ctx.getDate());
        data.put("basePackage", ctx.getBasePackage());
        data.put("projectName", ctx.getProjectName());
        data.put("tableName", table.getName());
        data.put("tableComment", table.getComment() != null ? table.getComment() : table.getEntityName());
        data.put("entityName", table.getEntityName());
        data.put("entityNameLower", table.getEntityNameLower());
        data.put("mappingPath", table.getMappingPath());
        data.put("columns", table.getColumns());
        data.put("pkColumn", table.getPkColumn());
        data.put("pkJavaType", table.getPkJavaType());
        data.put("options", ctx.getOptions());

        // 系统字段检测标记（单次遍历所有列名）
        String colNames = table.getColumns().stream()
                .map(c -> c.getRawName().toLowerCase())
                .reduce("", (a, b) -> a + "," + b);
        data.put("hasCreateTime", colNames.contains("create"));
        data.put("hasUpdateTime", colNames.contains("update"));
        data.put("hasCreateBy", colNames.contains("create_by"));
        data.put("hasUpdateBy", colNames.contains("update_by"));
        data.put("hasDeleted", colNames.contains("deleted"));
        data.put("hasVersion", colNames.contains("version"));

        // 分层字段
        List<ColumnInfo> insertCols = SystemFieldDetector.getInsertColumns(table);
        List<ColumnInfo> updateCols = SystemFieldDetector.getUpdateColumns(table);
        List<ColumnInfo> queryCols = SystemFieldDetector.getQueryColumns(table);
        data.put("insertColumns", insertCols);
        data.put("updateColumns", updateCols);
        data.put("queryColumns", queryCols);

        // 模式特定的包变量注入（模板方法钩子）
        applyPackageOverrides(data, ctx);

        return data;
    }

    /**
     * 注入模式特定的包变量。
     */
    protected abstract void applyPackageOverrides(Map<String, Object> data, GenerationContext ctx);

    /**
     * 为指定表生成全部代码。
     *
     * <p>子类在此实现完整的生成编排逻辑。</p>
     */
    public abstract void generate(GenerationContext ctx, TableInfo table);
}
