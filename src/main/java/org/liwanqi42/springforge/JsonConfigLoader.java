package org.liwanqi42.springforge;

import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.NamingUtils;
import org.liwanqi42.springforge.util.SqlTypeMapper;
import org.springframework.boot.json.BasicJsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON 配置文件加载器。
 *
 * <p>使用 Spring Boot 内置的 {@link BasicJsonParser}（零额外依赖），
 * 将完整的 spring-forge-config.json 解析为 {@link GenerationContext}。</p>
 */
final class JsonConfigLoader {

    private JsonConfigLoader() {
    }

    /**
     * 从 JSON 文件加载完整生成配置。
     */
    @SuppressWarnings("unchecked")
    static GenerationContext load(File jsonFile) {
        if (!jsonFile.exists()) {
            throw new GenerationException("配置文件不存在：" + jsonFile.getAbsolutePath()
                    + "\n请在项目根目录创建 spring-forge-config.json");
        }
        try {
            String jsonContent = Files.readString(jsonFile.toPath());
            Map<String, Object> root = new BasicJsonParser().parseMap(jsonContent);

            GenerationContext ctx = new GenerationContext();

            // === project ===
            Map<String, Object> project = (Map<String, Object>) root.get("project");
            if (project == null) {
                throw new GenerationException("JSON 缺少 project 节点");
            }
            ctx.setProjectName((String) project.getOrDefault("name", "demo"));
            ctx.setGroupId((String) project.getOrDefault("groupId", "com.example"));
            ctx.setBasePackage((String) project.get("basePackage"));
            if (ctx.getBasePackage() == null) {
                throw new GenerationException("project.basePackage 是必填项");
            }
            String modeStr = (String) project.getOrDefault("mode", "MVC");
            ctx.setMode(GenerationMode.valueOf(modeStr.toUpperCase()));
            ctx.setOutputDir((String) project.getOrDefault("outputDir", "."));
            ctx.setAuthor((String) project.getOrDefault("author", "CodeGenerator"));
            ctx.setOverwrite(boolVal(project.get("overwrite")));

            // === database ===
            Map<String, Object> database = (Map<String, Object>) root.get("database");
            if (database != null) {
                ctx.setJdbcUrl((String) database.get("jdbcUrl"));
                ctx.setJdbcUsername((String) database.get("username"));
                ctx.setJdbcPassword((String) database.get("password"));
            }

            // === tables ===
            // 三种模式：
            //   1. 不写 tables 节点  → 从数据库/JSON 文件读取全部表
            //   2. 字符串数组 ["user","order"] → 从数据库读取指定表（保存过滤名）
            //   3. 完整表对象数组 → 直接解析，避免二次读取 JSON 文件
            Object tablesObj = root.get("tables");
            if (tablesObj instanceof List list && !list.isEmpty()) {
                if (list.get(0) instanceof String) {
                    // 简写模式：["user", "order"] — 记录表名过滤，走 JDBC 读取
                    ctx.setTableNameFilter(list.stream().map(Object::toString).toList());
                } else if (list.get(0) instanceof Map) {
                    // 完整表对象模式 — 直接解析，避免 CodeGenerator 二次读取
                    ctx.setTables(parseTables(list, ctx.getOptions()));
                }
            }

            // 保存 JSON 文件路径，供 CodeGenerator 回退使用
            ctx.setJsonConfigPath(jsonFile.getAbsolutePath());

            // === options ===
            Map<String, Object> optionsMap = (Map<String, Object>) root.get("options");
            if (optionsMap != null) {
                GenerationOptions opt = new GenerationOptions();
                opt.setGenerateDdl(boolVal(optionsMap.get("generateDdl")));
                opt.setGenerateTests(boolVal(optionsMap.get("generateTests")));
                if (optionsMap.get("logicDeleteField") instanceof String s) opt.setLogicDeleteField(s);
                if (optionsMap.get("logicDeleteValue") instanceof String s) opt.setLogicDeleteValue(s);
                if (optionsMap.get("logicNotDeleteValue") instanceof String s) opt.setLogicNotDeleteValue(s);
                if (optionsMap.get("optimisticLockField") instanceof String s) opt.setOptimisticLockField(s);
                if (optionsMap.get("autoFillInsertFields") instanceof String s) opt.setAutoFillInsertFields(s);
                if (optionsMap.get("autoFillUpdateFields") instanceof String s) opt.setAutoFillUpdateFields(s);
                if (optionsMap.get("idType") instanceof String s) opt.setIdType(s);
                if (optionsMap.get("tablePrefix") instanceof String s) opt.setTablePrefix(s);
                opt.setUseLombok(boolVal(optionsMap.get("useLombok")));
                if (optionsMap.get("dateFormat") instanceof String s) opt.setDateFormat(s);
                ctx.setOptions(opt);
            }

            return ctx;
        } catch (IOException e) {
            throw new GenerationException("读取配置文件失败：" + jsonFile.getAbsolutePath(), e);
        }
    }

    /**
     * BasicJsonParser 将 JSON 布尔值解析为字符串 "true"/"false"，
     * 此方法同时兼容 String 和 Boolean 类型。
     */
    private static boolean boolVal(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return false;
    }

    // ======================== 内联表解析（避免 JsonMetadataProvider 二次读取文件） ========================

    @SuppressWarnings("unchecked")
    private static List<TableInfo> parseTables(List<?> tableList, GenerationOptions options) {
        List<TableInfo> tables = new ArrayList<>();
        for (Object tObj : tableList) {
            if (tObj instanceof Map<?, ?> tMap) {
                TableInfo table = parseTable((Map<String, Object>) tMap, options);
                if (table != null) {
                    tables.add(table);
                }
            }
        }
        return tables;
    }

    private static TableInfo parseTable(Map<String, Object> tMap, GenerationOptions options) {
        String name = (String) tMap.get("name");
        if (name == null) return null;

        TableInfo table = new TableInfo();
        String prefix = options.getTablePrefix();
        String cleanName = name;
        if (prefix != null && !prefix.isEmpty() && cleanName.startsWith(prefix)) {
            cleanName = cleanName.substring(prefix.length());
        }
        table.setName(name);
        table.setComment((String) tMap.getOrDefault("comment", ""));
        table.setEntityName(NamingUtils.toUpperCamel(cleanName));
        table.setEntityNameLower(NamingUtils.toLowerCamel(cleanName));
        table.setMappingPath(NamingUtils.tableNameToMappingPath(cleanName));

        Object columnsObj = tMap.get("columns");
        if (columnsObj instanceof List<?> colList) {
            for (Object cObj : colList) {
                if (cObj instanceof Map<?, ?> cMap) {
                    table.getColumns().add(parseColumn((Map<String, Object>) cMap));
                }
            }
        }
        return table;
    }

    @SuppressWarnings("unchecked")
    private static ColumnInfo parseColumn(Map<String, Object> cMap) {
        ColumnInfo col = new ColumnInfo();
        String rawName = (String) cMap.get("name");
        col.setRawName(rawName);
        col.setFieldName(NamingUtils.toLowerCamel(rawName));
        col.setFieldNameUpper(NamingUtils.toUpperCamel(rawName));
        col.setColumnComment((String) cMap.getOrDefault("comment", ""));
        col.setNullable(Boolean.TRUE.equals(cMap.get("nullable")));
        col.setPrimaryKey(Boolean.TRUE.equals(cMap.get("primaryKey")));
        col.setAutoIncrement(Boolean.TRUE.equals(cMap.get("autoIncrement")));

        String jdbcType = (String) cMap.get("type");
        col.setJdbcType(jdbcType);

        // JSON 指定 javaType 时使用，否则从类型映射表获取
        String javaType = (String) cMap.get("javaType");
        SqlTypeMapper.JavaTypeInfo typeInfo = (javaType != null && !javaType.isEmpty())
                ? new SqlTypeMapper.JavaTypeInfo(javaType, javaType, javaType)
                : SqlTypeMapper.getJavaType(jdbcType);
        col.setJavaType(typeInfo.fullyQualified());
        col.setJavaTypeShort(typeInfo.shortName());
        col.setJavaTypeBoxed(typeInfo.boxedName());
        col.setXmlJdbcType(SqlTypeMapper.getXmlJdbcType(jdbcType));

        Object length = cMap.get("length");
        if (length instanceof Number n) {
            col.setMaxLength(n.intValue());
        }

        col.setDefaultValue((String) cMap.getOrDefault("defaultValue", ""));
        return col;
    }
}
