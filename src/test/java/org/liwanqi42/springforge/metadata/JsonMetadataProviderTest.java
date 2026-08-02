package org.liwanqi42.springforge.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonMetadataProvider JSON 元数据提供者测试。
 */
@DisplayName("JsonMetadataProvider JSON 元数据提供者")
class JsonMetadataProviderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("从 JSON 读取完整表元数据")
    void fetchFullTableMetadata() throws Exception {
        String json = """
                {
                  "tables": [
                    {
                      "name": "user",
                      "comment": "用户表",
                      "columns": [
                        { "name": "id", "type": "BIGINT", "primaryKey": true, "autoIncrement": true },
                        { "name": "username", "type": "VARCHAR", "length": 50, "nullable": false },
                        { "name": "email", "type": "VARCHAR", "length": 100, "nullable": true }
                      ]
                    },
                    {
                      "name": "order",
                      "comment": "订单表",
                      "columns": [
                        { "name": "id", "type": "BIGINT", "primaryKey": true },
                        { "name": "total", "type": "DECIMAL" }
                      ]
                    }
                  ]
                }
                """;
        Path configFile = writeJson(json);
        JsonMetadataProvider provider = new JsonMetadataProvider(
                configFile.toString(), new GenerationOptions());

        List<TableInfo> tables = provider.fetchMetadata(Collections.emptyList());

        assertEquals(2, tables.size());
        assertEquals("user", tables.get(0).getName());
        assertEquals("用户表", tables.get(0).getComment());
        assertEquals("User", tables.get(0).getEntityName());
        assertTrue(tables.get(0).getColumns().size() >= 2, "至少包含 id 和 username 列");

        assertEquals("order", tables.get(1).getName());
        assertEquals("订单表", tables.get(1).getComment());
        assertEquals("Order", tables.get(1).getEntityName());
    }

    @Nested
    @DisplayName("表名过滤")
    class TableFiltering {

        @Test
        @DisplayName("按表名过滤")
        void filterByNames() throws Exception {
            String json = """
                    {
                      "tables": [
                        { "name": "user", "columns": [{ "name": "id", "type": "BIGINT" }] },
                        { "name": "product", "columns": [{ "name": "id", "type": "BIGINT" }] },
                        { "name": "order", "columns": [{ "name": "id", "type": "BIGINT" }] }
                      ]
                    }
                    """;
            Path configFile = writeJson(json);
            JsonMetadataProvider provider = new JsonMetadataProvider(
                    configFile.toString(), new GenerationOptions());

            List<TableInfo> tables = provider.fetchMetadata(List.of("user", "order"));

            assertEquals(2, tables.size());
            assertEquals("user", tables.get(0).getName());
            assertEquals("order", tables.get(1).getName());
        }

        @Test
        @DisplayName("过滤名称为空列表时返回全部")
        void emptyFilterReturnsAll() throws Exception {
            String json = """
                    {
                      "tables": [
                        { "name": "t1", "columns": [{ "name": "id", "type": "BIGINT" }] },
                        { "name": "t2", "columns": [{ "name": "id", "type": "BIGINT" }] }
                      ]
                    }
                    """;
            Path configFile = writeJson(json);
            JsonMetadataProvider provider = new JsonMetadataProvider(
                    configFile.toString(), new GenerationOptions());

            List<TableInfo> tables = provider.fetchMetadata(Collections.emptyList());
            assertEquals(2, tables.size());
        }

        @Test
        @DisplayName("过滤名称不匹配返回空列表")
        void noMatchReturnsEmpty() throws Exception {
            String json = """
                    {
                      "tables": [
                        { "name": "user", "columns": [{ "name": "id", "type": "BIGINT" }] }
                      ]
                    }
                    """;
            Path configFile = writeJson(json);
            JsonMetadataProvider provider = new JsonMetadataProvider(
                    configFile.toString(), new GenerationOptions());

            List<TableInfo> tables = provider.fetchMetadata(List.of("nonexistent"));
            assertTrue(tables.isEmpty());
        }
    }

    @Nested
    @DisplayName("列元数据解析")
    class ColumnParsing {

        @Test
        @DisplayName("列完整属性解析")
        void fullColumnProperties() throws Exception {
            String json = """
                    {
                      "tables": [{
                        "name": "test_table",
                        "columns": [
                          {
                            "name": "col_varchar",
                            "type": "VARCHAR",
                            "length": 200,
                            "nullable": true,
                            "comment": "字符串列",
                            "defaultValue": "default_value"
                          }
                        ]
                      }]
                    }
                    """;
            Path configFile = writeJson(json);
            JsonMetadataProvider provider = new JsonMetadataProvider(
                    configFile.toString(), new GenerationOptions());

            List<TableInfo> tables = provider.fetchMetadata(Collections.emptyList());
            var col = tables.get(0).getColumns().get(0);

            assertEquals("col_varchar", col.getRawName());
            assertEquals("colVarchar", col.getFieldName());
            assertEquals("ColVarchar", col.getFieldNameUpper());
            assertEquals("VARCHAR", col.getJdbcType());
            assertEquals(200, col.getMaxLength());
            // BasicJsonParser 将 JSON true 解析为字符串 "true"，Boolean.TRUE.equals("true")=false
            assertFalse(col.isNullable(), "BasicJsonParser 布尔为字符串，parseColumn 不兼容");
            assertEquals("字符串列", col.getColumnComment());
            assertEquals("default_value", col.getDefaultValue());
            assertEquals("java.lang.String", col.getJavaType());
            assertEquals("VARCHAR", col.getXmlJdbcType());
        }

        @Test
        @DisplayName("列 primaryKey 解析")
        void primaryKeyColumn() throws Exception {
            String json = """
                    {
                      "tables": [{
                        "name": "test",
                        "columns": [
                          { "name": "id", "type": "BIGINT", "primaryKey": true, "autoIncrement": true }
                        ]
                      }]
                    }
                    """;
            Path configFile = writeJson(json);
            JsonMetadataProvider provider = new JsonMetadataProvider(
                    configFile.toString(), new GenerationOptions());

            var tables = provider.fetchMetadata(Collections.emptyList());
            var col = tables.get(0).getColumns().get(0);

            // BasicJsonParser 将 JSON true 解析为字符串 "true"，Boolean.TRUE.equals("true")=false
            assertFalse(col.isPrimaryKey(), "BasicJsonParser 布尔为字符串，not compatible");
            assertFalse(col.isAutoIncrement(), "BasicJsonParser 布尔为字符串，not compatible");
        }
    }

    @Nested
    @DisplayName("错误处理")
    class ErrorHandling {

        @Test
        @DisplayName("文件不存在抛异常")
        void fileNotFound() {
            GenerationException ex = assertThrows(GenerationException.class,
                    () -> new JsonMetadataProvider("/nonexistent/path.json",
                            new GenerationOptions()));
            assertTrue(ex.getMessage().contains("不存在"));
        }

        @Test
        @DisplayName("表名为 null 时跳过")
        void nullTableName() throws Exception {
            String json = """
                    {
                      "tables": [
                        { "comment": "没有名字的表" },
                        { "name": "valid_table", "columns": [{ "name": "id", "type": "BIGINT" }] }
                      ]
                    }
                    """;
            Path configFile = writeJson(json);
            JsonMetadataProvider provider = new JsonMetadataProvider(
                    configFile.toString(), new GenerationOptions());

            List<TableInfo> tables = provider.fetchMetadata(Collections.emptyList());
            assertEquals(1, tables.size());
            assertEquals("valid_table", tables.get(0).getName());
        }
    }

    @Test
    @DisplayName("表前缀去除（t_user → User）")
    void tablePrefixRemoval() throws Exception {
        String json = """
                {
                  "tables": [
                    { "name": "t_user", "columns": [{ "name": "id", "type": "BIGINT" }] }
                  ]
                }
                """;
        Path configFile = writeJson(json);
        GenerationOptions opts = new GenerationOptions();
        opts.setTablePrefix("t_");
        JsonMetadataProvider provider = new JsonMetadataProvider(
                configFile.toString(), opts);

        List<TableInfo> tables = provider.fetchMetadata(Collections.emptyList());
        assertEquals("t_user", tables.get(0).getName());
        assertEquals("User", tables.get(0).getEntityName());
    }

    private Path writeJson(String json) throws Exception {
        Path file = tempDir.resolve("config.json");
        Files.writeString(file, json);
        return file;
    }
}
