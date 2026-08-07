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

    /** MySQL 错误码：数据库不存在 */
    private static final int MYSQL_UNKNOWN_DATABASE = 1049;

    @Override
    public List<TableInfo> fetchMetadata(List<String> tableNames) {
        try {
            return doFetchMetadata(tableNames);
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_UNKNOWN_DATABASE) {
                // 数据库不存在 → 自动创建后重试
                String dbName = extractDatabaseName(jdbcUrl);
                if (dbName != null && !dbName.isEmpty()) {
                    tryAutoCreateDatabase(dbName);
                    try {
                        return doFetchMetadata(tableNames);
                    } catch (SQLException retryEx) {
                        throw new GenerationException(
                                "数据库 " + dbName + " 已自动创建，但重新连接失败：" + retryEx.getMessage(), retryEx);
                    }
                }
            }
            throw new GenerationException("数据库元数据读取失败：" + e.getMessage(), e);
        }
    }

    private List<TableInfo> doFetchMetadata(List<String> tableNames) throws SQLException {
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
        }
    }

    /**
     * 从 JDBC URL 中提取数据库名称。
     * <p>格式：{@code jdbc:mysql://host:port/dbname?params} → {@code dbname}</p>
     */
    static String extractDatabaseName(String jdbcUrl) {
        String prefix = "jdbc:mysql://";
        if (jdbcUrl == null || !jdbcUrl.startsWith(prefix)) {
            return null;
        }
        String rest = jdbcUrl.substring(prefix.length());
        int slashIndex = rest.indexOf('/');
        if (slashIndex < 0) {
            return null;
        }
        String afterSlash = rest.substring(slashIndex + 1);
        int questionIndex = afterSlash.indexOf('?');
        if (questionIndex >= 0) {
            return afterSlash.substring(0, questionIndex);
        }
        return afterSlash;
    }

    /**
     * 构造不含数据库名的 JDBC URL（仅连服务器）。
     * <p>{@code jdbc:mysql://host:port/dbname?params} → {@code jdbc:mysql://host:port?params}</p>
     */
    static String stripDatabaseFromUrl(String jdbcUrl) {
        String prefix = "jdbc:mysql://";
        if (jdbcUrl == null || !jdbcUrl.startsWith(prefix)) {
            return jdbcUrl;
        }
        String rest = jdbcUrl.substring(prefix.length());
        int slashIndex = rest.indexOf('/');
        if (slashIndex < 0) {
            return jdbcUrl;
        }
        int questionIndex = rest.indexOf('?', slashIndex);
        String hostPart = rest.substring(0, slashIndex);
        if (questionIndex >= 0) {
            return prefix + hostPart + rest.substring(questionIndex);
        }
        return prefix + hostPart;
    }

    /**
     * 尝试连接 MySQL 服务器并创建数据库。
     */
    private void tryAutoCreateDatabase(String dbName) {
        String serverUrl = stripDatabaseFromUrl(jdbcUrl);
        try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS `" + dbName.replace("`", "``")
                    + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("[Spring-Forge] 数据库 " + dbName + " 不存在，已自动创建。");
        } catch (SQLException ex) {
            throw new GenerationException(
                    "数据库 " + dbName + " 不存在，且自动创建失败（可能当前用户无 CREATE DATABASE 权限）："
                            + ex.getMessage(), ex);
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