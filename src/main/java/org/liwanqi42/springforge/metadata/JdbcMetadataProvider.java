package org.liwanqi42.springforge.metadata;

import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.NamingUtils;
import org.liwanqi42.springforge.util.SqlTypeMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 JDBC DatabaseMetaData 的元数据提供者。
 * 直连数据库，自动读取表和列元数据。
 */
public class JdbcMetadataProvider implements MetadataProvider {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final GenerationOptions options;

    public JdbcMetadataProvider(String jdbcUrl, String username, String password,
                                GenerationOptions options) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.options = options;
    }

    @Override
    public List<TableInfo> fetchMetadata(List<String> tableNames) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schema = getSchemaSafely(conn);

            // 转为小写集合用于快速匹配（忽略大小写）
            Set<String> targetTableSet = null;
            if (tableNames != null && !tableNames.isEmpty()) {
                targetTableSet = tableNames.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
            }

            List<TableInfo> tables = new ArrayList<>();
            try (ResultSet tableRs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (tableRs.next()) {
                    String tableName = tableRs.getString("TABLE_NAME");
                    if (targetTableSet != null && !targetTableSet.contains(tableName.toLowerCase())) {
                        continue;
                    }
                    TableInfo table = buildTableInfo(tableRs, catalog, schema, metaData);
                    tables.add(table);
                }
            }
            return tables;
        } catch (SQLException e) {
            throw new GenerationException("数据库元数据读取失败：" + e.getMessage(), e);
        }
    }

    private String getSchemaSafely(Connection conn) {
        try {
            return conn.getSchema();
        } catch (Exception ignored) {
            // 部分驱动不支持 getSchema，兼容处理
            return null;
        }
    }

    private TableInfo buildTableInfo(ResultSet tableRs, String catalog, String schema, DatabaseMetaData metaData) throws SQLException {
        String tableName = tableRs.getString("TABLE_NAME");
        String prefix = options.getTablePrefix();
        String cleanName = stripTablePrefix(tableName, prefix);

        TableInfo table = new TableInfo();
        table.setName(tableName);
        table.setComment(tableRs.getString("REMARKS"));
        table.setEntityName(NamingUtils.toUpperCamel(cleanName));
        table.setEntityNameLower(NamingUtils.toLowerCamel(cleanName));
        table.setMappingPath(NamingUtils.tableNameToMappingPath(cleanName));

        // 读取列
        loadColumns(table, catalog, schema, tableName, metaData);
        // 读取主键并标记
        markPrimaryKeys(table, catalog, schema, tableName, metaData);
        return table;
    }

    private String stripTablePrefix(String tableName, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return tableName;
        }
        String lowerTable = tableName.toLowerCase();
        String lowerPrefix = prefix.toLowerCase();
        if (lowerTable.startsWith(lowerPrefix)) {
            return tableName.substring(prefix.length());
        }
        return tableName;
    }

    private void loadColumns(TableInfo table, String catalog, String schema, String tableName, DatabaseMetaData metaData) throws SQLException {
        try (ResultSet colRs = metaData.getColumns(catalog, schema, tableName, "%")) {
            while (colRs.next()) {
                ColumnInfo col = new ColumnInfo();
                String colName = colRs.getString("COLUMN_NAME");
                col.setRawName(colName);
                col.setFieldName(NamingUtils.toLowerCamel(colName));
                col.setFieldNameUpper(NamingUtils.toUpperCamel(colName));
                col.setColumnComment(colRs.getString("REMARKS"));

                String jdbcType = colRs.getString("TYPE_NAME");
                int dataType = colRs.getInt("DATA_TYPE");
                int colSize = colRs.getInt("COLUMN_SIZE");
                col.setJdbcType(jdbcType);
                col.setMaxLength(colSize);
                col.setNullable(colRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                col.setAutoIncrement("YES".equalsIgnoreCase(colRs.getString("IS_AUTOINCREMENT")));
                col.setDefaultValue(colRs.getString("COLUMN_DEF"));

                SqlTypeMapper.JavaTypeInfo typeInfo = SqlTypeMapper.getJavaType(jdbcType);
                col.setJavaType(typeInfo.fullyQualified());
                col.setJavaTypeShort(typeInfo.shortName());
                col.setJavaTypeBoxed(typeInfo.boxedName());
                col.setXmlJdbcType(SqlTypeMapper.getXmlJdbcType(jdbcType));

                table.getColumns().add(col);
            }
        }
    }

    private void markPrimaryKeys(TableInfo table, String catalog, String schema, String tableName, DatabaseMetaData metaData) throws SQLException {
        try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (pkRs.next()) {
                String pkColName = pkRs.getString("COLUMN_NAME");
                for (ColumnInfo col : table.getColumns()) {
                    if (col.getRawName().equalsIgnoreCase(pkColName)) {
                        col.setPrimaryKey(true);
                        break;
                    }
                }
            }
        }
    }
}