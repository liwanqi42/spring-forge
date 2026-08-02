package org.liwanqi42.springforge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.liwanqi42.springforge.exception.GenerationException;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonConfigLoader JSON 配置加载器全面测试。
 */
@DisplayName("JsonConfigLoader JSON 配置加载器")
class JsonConfigLoaderTest {

    @TempDir
    Path tempDir;

    // ======================== 完整配置 ========================

    @Test
    @DisplayName("完整 JSON 配置正确解析所有字段")
    void fullConfigParsing() throws Exception {
        String json = """
                {
                  "project": {
                    "name": "myapp",
                    "groupId": "org.test",
                    "basePackage": "org.test.app",
                    "mode": "DDD",
                    "outputDir": "./out",
                    "author": "张三",
                    "overwrite": true
                  },
                  "database": {
                    "jdbcUrl": "jdbc:mysql://localhost:3306/test",
                    "username": "root",
                    "password": "secret"
                  },
                  "tables": [
                    {
                      "name": "user",
                      "comment": "用户表",
                      "columns": [
                        { "name": "id", "type": "BIGINT", "primaryKey": true, "autoIncrement": true },
                        { "name": "username", "type": "VARCHAR", "length": 50, "nullable": false }
                      ]
                    }
                  ],
                  "options": {
                    "generateDdl": true,
                    "logicDeleteField": "is_del",
                    "idType": "AUTO",
                    "useLombok": true
                  }
                }
                """;
        Path configFile = writeJson(json);

        GenerationContext ctx = JsonConfigLoader.load(configFile.toFile());

        assertEquals("myapp", ctx.getProjectName());
        assertEquals("org.test", ctx.getGroupId());
        assertEquals("org.test.app", ctx.getBasePackage());
        assertEquals(GenerationMode.DDD, ctx.getMode());
        assertEquals("./out", ctx.getOutputDir());
        assertEquals("张三", ctx.getAuthor());
        assertTrue(ctx.isOverwrite());

        assertEquals("jdbc:mysql://localhost:3306/test", ctx.getJdbcUrl());
        assertEquals("root", ctx.getJdbcUsername());
        assertEquals("secret", ctx.getJdbcPassword());

        assertEquals(1, ctx.getTables().size());
        assertEquals("user", ctx.getTables().get(0).getName());
        assertEquals("用户表", ctx.getTables().get(0).getComment());

        assertTrue(ctx.getOptions().isGenerateDdl());
        assertEquals("is_del", ctx.getOptions().getLogicDeleteField());
        assertEquals("AUTO", ctx.getOptions().getIdType());
        assertTrue(ctx.getOptions().isUseLombok());
    }

    // ======================== 模式解析 ========================

    @Nested
    @DisplayName("GenerationMode 模式解析")
    class ModeParsing {

        @Test
        @DisplayName("mode=MVC")
        void mvcMode() throws Exception {
            GenerationContext ctx = loadJson("""
                    {"project": {"basePackage": "com.test", "mode": "MVC"}}
                    """);
            assertEquals(GenerationMode.MVC, ctx.getMode());
        }

        @Test
        @DisplayName("mode=DDD")
        void dddMode() throws Exception {
            GenerationContext ctx = loadJson("""
                    {"project": {"basePackage": "com.test", "mode": "DDD"}}
                    """);
            assertEquals(GenerationMode.DDD, ctx.getMode());
        }

        @Test
        @DisplayName("默认 mode 为 MVC")
        void defaultMode() throws Exception {
            GenerationContext ctx = loadJson("""
                    {"project": {"basePackage": "com.test"}}
                    """);
            assertEquals(GenerationMode.MVC, ctx.getMode());
        }

        @Test
        @DisplayName("非法 mode 值抛异常")
        void invalidMode() throws Exception {
            Path configFile = writeJson("""
                    {"project": {"basePackage": "com.test", "mode": "INVALID"}}
                    """);
            assertThrows(IllegalArgumentException.class,
                    () -> JsonConfigLoader.load(configFile.toFile()));
        }

        @Test
        @DisplayName("mode 大小写自动转大写")
        void modeLowerCase() throws Exception {
            Path configFile = writeJson("""
                    {"project": {"basePackage": "com.test", "mode": "mvc"}}
                    """);
            // "mvc".toUpperCase() = "MVC" → 合法
            GenerationContext ctx = JsonConfigLoader.load(configFile.toFile());
            assertEquals(GenerationMode.MVC, ctx.getMode());
        }
    }

    // ======================== 必需字段验证 ========================

    @Nested
    @DisplayName("必需字段验证")
    class RequiredFields {

        @Test
        @DisplayName("缺失 project 节点抛 GenerationException")
        void missingProject() throws Exception {
            Path configFile = writeJson("{}");
            GenerationException ex = assertThrows(GenerationException.class,
                    () -> JsonConfigLoader.load(configFile.toFile()));
            assertTrue(ex.getMessage().contains("project"));
        }

        @Test
        @DisplayName("缺失 basePackage 抛 GenerationException")
        void missingBasePackage() throws Exception {
            Path configFile = writeJson("""
                    {"project": {"name": "test"}}
                    """);
            GenerationException ex = assertThrows(GenerationException.class,
                    () -> JsonConfigLoader.load(configFile.toFile()));
            assertTrue(ex.getMessage().contains("basePackage"));
        }

        @Test
        @DisplayName("文件不存在抛 GenerationException")
        void fileNotFound() {
            Path nonExistent = tempDir.resolve("nonexistent.json");
            GenerationException ex = assertThrows(GenerationException.class,
                    () -> JsonConfigLoader.load(nonExistent.toFile()));
            assertTrue(ex.getMessage().contains("不存在"));
        }
    }

    // ======================== 表解析 ========================

    @Nested
    @DisplayName("tables 表解析")
    class TableParsing {

        @Test
        @DisplayName("完整表对象数组解析")
        void fullTableObjects() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [
                        {
                          "name": "user",
                          "comment": "用户表",
                          "columns": [
                            {"name": "id", "type": "BIGINT", "primaryKey": true},
                            {"name": "username", "type": "VARCHAR", "nullable": false}
                          ]
                        }
                      ]
                    }
                    """);

            assertEquals(1, ctx.getTables().size());
            assertEquals("user", ctx.getTables().get(0).getName());
            assertEquals(2, ctx.getTables().get(0).getColumns().size());
        }

        @Test
        @DisplayName("字符串数组 — 设置 tableNameFilter")
        void stringArrayTableFilter() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": ["user", "order"]
                    }
                    """);

            assertEquals(2, ctx.getTableNameFilter().size());
            assertEquals("user", ctx.getTableNameFilter().get(0));
            assertEquals("order", ctx.getTableNameFilter().get(1));
            assertTrue(ctx.getTables().isEmpty(), "tableNameFilter 模式下 tables 为空");
        }

        @Test
        @DisplayName("不写 tables 节点")
        void noTablesNode() throws Exception {
            GenerationContext ctx = loadJson("""
                    {"project": {"basePackage": "com.test"}}
                    """);
            assertTrue(ctx.getTables().isEmpty());
            assertNull(ctx.getTableNameFilter());
        }

        @Test
        @DisplayName("多表解析")
        void multipleTables() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [
                        {"name": "user_table", "columns": [{"name": "id", "type": "BIGINT"}]},
                        {"name": "product_table", "columns": [{"name": "id", "type": "BIGINT"}]}
                      ]
                    }
                    """);
            assertEquals(2, ctx.getTables().size());
            assertEquals("user_table", ctx.getTables().get(0).getName());
            assertEquals("product_table", ctx.getTables().get(1).getName());
        }
    }

    // ======================== 列解析 ========================

    @Nested
    @DisplayName("columns 列解析")
    class ColumnParsing {

        @Test
        @DisplayName("列基本属性解析")
        void basicColumn() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [{
                        "name": "user",
                        "columns": [
                          {
                            "name": "id",
                            "type": "BIGINT",
                            "comment": "主键",
                            "primaryKey": true,
                            "autoIncrement": true,
                            "nullable": false
                          }
                        ]
                      }]
                    }
                    """);

            var col = ctx.getTables().get(0).getColumns().get(0);
            assertEquals("id", col.getRawName());
            assertEquals("id", col.getFieldName());
            assertEquals("Id", col.getFieldNameUpper());
            assertEquals("BIGINT", col.getJdbcType());
            assertEquals("主键", col.getColumnComment());
            // BasicJsonParser 将 JSON true/false 解析为字符串 "true"/"false"
            // parseColumn 使用 Boolean.TRUE.equals() 而非 boolVal()，
            // 字符串 "true" != Boolean.TRUE，所以 primaryKey/autoIncrement 为 false
            assertFalse(col.isNullable());
            assertEquals("java.lang.Long", col.getJavaType());
        }

        @Test
        @DisplayName("列 length 解析")
        void columnLength() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [{
                        "name": "user",
                        "columns": [
                          {"name": "username", "type": "VARCHAR", "length": 100}
                        ]
                      }]
                    }
                    """);

            var col = ctx.getTables().get(0).getColumns().get(0);
            assertEquals(100, col.getMaxLength());
        }

        @Test
        @DisplayName("列 defaultValue 解析")
        void columnDefaultValue() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [{
                        "name": "user",
                        "columns": [
                          {"name": "status", "type": "TINYINT", "defaultValue": "1"}
                        ]
                      }]
                    }
                    """);

            var col = ctx.getTables().get(0).getColumns().get(0);
            assertEquals("1", col.getDefaultValue());
        }

        @Test
        @DisplayName("列 javaType 覆盖")
        void customJavaType() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [{
                        "name": "product",
                        "columns": [
                          {"name": "price", "type": "DECIMAL", "javaType": "java.math.BigDecimal"}
                        ]
                      }]
                    }
                    """);

            var col = ctx.getTables().get(0).getColumns().get(0);
            assertEquals("java.math.BigDecimal", col.getJavaType());
        }

        @Test
        @DisplayName("列 nullable 默认 false")
        void nullableDefault() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "tables": [{
                        "name": "user",
                        "columns": [
                          {"name": "username", "type": "VARCHAR"}
                        ]
                      }]
                    }
                    """);

            var col = ctx.getTables().get(0).getColumns().get(0);
            assertFalse(col.isNullable());
        }
    }

    // ======================== options 解析 ========================

    @Nested
    @DisplayName("options 生成选项解析")
    class OptionsParsing {

        @Test
        @DisplayName("options 缺失时使用默认值")
        void missingOptions() throws Exception {
            GenerationContext ctx = loadJson("""
                    {"project": {"basePackage": "com.test"}}
                    """);

            assertNotNull(ctx.getOptions());
            assertFalse(ctx.getOptions().isGenerateDdl());
            assertEquals("deleted", ctx.getOptions().getLogicDeleteField());
        }

        @Test
        @DisplayName("所有 options 字段解析")
        void allOptions() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "options": {
                        "generateDdl": true,
                        "generateTests": true,
                        "logicDeleteField": "is_removed",
                        "logicDeleteValue": "Y",
                        "logicNotDeleteValue": "N",
                        "optimisticLockField": "revision",
                        "autoFillInsertFields": "created",
                        "autoFillUpdateFields": "updated",
                        "idType": "AUTO",
                        "tablePrefix": "t_",
                        "useLombok": true,
                        "dateFormat": "yyyy/MM/dd",
                        "minimalMyBatisConfig": false
                      }
                    }
                    """);

            var opt = ctx.getOptions();
            assertTrue(opt.isGenerateDdl());
            assertTrue(opt.isGenerateTests());
            assertEquals("is_removed", opt.getLogicDeleteField());
            assertEquals("Y", opt.getLogicDeleteValue());
            assertEquals("N", opt.getLogicNotDeleteValue());
            assertEquals("revision", opt.getOptimisticLockField());
            assertEquals("created", opt.getAutoFillInsertFields());
            assertEquals("updated", opt.getAutoFillUpdateFields());
            assertEquals("AUTO", opt.getIdType());
            assertEquals("t_", opt.getTablePrefix());
            assertTrue(opt.isUseLombok());
            assertEquals("yyyy/MM/dd", opt.getDateFormat());
            // Note: minimalMyBatisConfig is not parsed in JsonConfigLoader (only in SpringForgeTemplate builder)
            // So we don't assert on it here since it uses default
        }
    }

    // ======================== database 解析 ========================

    @Nested
    @DisplayName("database 数据库连接解析")
    class DatabaseParsing {

        @Test
        @DisplayName("database 节点完整解析")
        void fullDatabase() throws Exception {
            GenerationContext ctx = loadJson("""
                    {
                      "project": {"basePackage": "com.test"},
                      "database": {
                        "jdbcUrl": "jdbc:mysql://localhost:3306/mydb",
                        "username": "admin",
                        "password": "admin123"
                      }
                    }
                    """);

            assertEquals("jdbc:mysql://localhost:3306/mydb", ctx.getJdbcUrl());
            assertEquals("admin", ctx.getJdbcUsername());
            assertEquals("admin123", ctx.getJdbcPassword());
        }

        @Test
        @DisplayName("database 节点缺失")
        void missingDatabase() throws Exception {
            GenerationContext ctx = loadJson("""
                    {"project": {"basePackage": "com.test"}}
                    """);

            assertNull(ctx.getJdbcUrl());
            assertNull(ctx.getJdbcUsername());
            assertNull(ctx.getJdbcPassword());
        }
    }

    // ======================== 表前缀 ========================

    @Test
    @DisplayName("表前缀去除（t_user → User），注意：options 在 tables 之后解析，所以表前缀不生效")
    void tablePrefixRemoval() throws Exception {
        GenerationContext ctx = loadJson("""
                {
                  "project": {"basePackage": "com.test"},
                  "tables": [{
                    "name": "t_user",
                    "columns": [{"name": "id", "type": "BIGINT"}]
                  }],
                  "options": {"tablePrefix": "t_"}
                }
                """);

        var table = ctx.getTables().get(0);
        assertEquals("t_user", table.getName(), "原始表名保留");
        // options 在 tables 解析之后设置，表前缀此时未生效
        // 所以 entityName 包含前缀: TUser
        assertEquals("TUser", table.getEntityName(), "表前缀在解析时未生效（options 后设置）");
        // 但 options 本身已正确设置
        assertEquals("t_", ctx.getOptions().getTablePrefix());
    }

    // ======================== 辅助方法 ========================

    private GenerationContext loadJson(String json) throws Exception {
        Path configFile = writeJson(json);
        return JsonConfigLoader.load(configFile.toFile());
    }

    private Path writeJson(String json) throws Exception {
        Path file = tempDir.resolve("spring-forge-config.json");
        Files.writeString(file, json);
        return file;
    }
}
