package org.liwanqi42.springforge.metadata;

import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.model.ColumnInfo;
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
 * 基于 JSON 配置文件的元数据提供者。
 *
 * <p>使用 Spring Boot 内置的 {@link BasicJsonParser} 解析 JSON 配置，
 * 零额外依赖。</p>
 */
public class JsonMetadataProvider implements MetadataProvider {

    private final GenerationOptions options;
    private final File jsonFile;

    public JsonMetadataProvider(String jsonConfigPath, GenerationOptions options) {
        this.options = options;
        this.jsonFile = new File(jsonConfigPath);
        if (!jsonFile.exists()) {
            throw new GenerationException("JSON 配置文件不存在：" + jsonConfigPath);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TableInfo> fetchMetadata(List<String> tableNames) {
        try {
            String jsonContent = Files.readString(jsonFile.toPath());
            Map<String, Object> config = new BasicJsonParser().parseMap(jsonContent);
            List<TableInfo> tables = new ArrayList<>();

            Object tablesObj = config.get("tables");
            if (tablesObj instanceof List<?> tableList) {
                for (Object tObj : tableList) {
                    if (tObj instanceof Map<?, ?> tMap) {
                        TableInfo table = parseTable((Map<String, Object>) tMap, tableNames);
                        if (table != null) {
                            tables.add(table);
                        }
                    }
                }
            }
            return tables;
        } catch (IOException e) {
            throw new GenerationException("解析 JSON 配置文件失败：" + jsonFile.getAbsolutePath(), e);
        }
    }

    private TableInfo parseTable(Map<String, Object> tMap, List<String> filterNames) {
        String name = (String) tMap.get("name");
        if (name == null) return null;

        // 如果指定了表名过滤
        if (filterNames != null && !filterNames.isEmpty() && !filterNames.contains(name)) {
            return null;
        }

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
                    ColumnInfo col = parseColumn((Map<String, Object>) cMap);
                    table.getColumns().add(col);
                }
            }
        }
        return table;
    }

    @SuppressWarnings("unchecked")
    private ColumnInfo parseColumn(Map<String, Object> cMap) {
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

        // 如果 JSON 指定了 javaType，使用它；否则从类型映射表获取
        String javaType = (String) cMap.get("javaType");
        SqlTypeMapper.JavaTypeInfo typeInfo;
        if (javaType != null && !javaType.isEmpty()) {
            typeInfo = new SqlTypeMapper.JavaTypeInfo(javaType, javaType, javaType);
        } else {
            typeInfo = SqlTypeMapper.getJavaType(jdbcType);
        }
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
