package org.liwanqi42.springforge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.liwanqi42.springforge.model.ColumnInfo;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.GenerationOptions;
import org.liwanqi42.springforge.model.TableInfo;
import org.liwanqi42.springforge.util.NamingUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringForgeTemplate 公开 API 测试。
 */
@DisplayName("SpringForgeTemplate 公开 API")
class SpringForgeTemplateTest {

    @Nested
    @DisplayName("Builder 构建器")
    class Builder {

        @Test
        @DisplayName("forge() 返回非 null Builder")
        void forgeReturnsBuilder() {
            assertNotNull(SpringForgeTemplate.forge());
        }

        @Test
        @DisplayName("所有链式方法返回 Builder 自身")
        void allMethodsReturnBuilder() {
            SpringForgeTemplate.Builder builder = SpringForgeTemplate.forge();

            assertSame(builder, builder.mode(GenerationMode.MVC));
            assertSame(builder, builder.projectName("test"));
            assertSame(builder, builder.groupId("com.test"));
            assertSame(builder, builder.basePackage("com.test"));
            assertSame(builder, builder.outputDir("."));
            assertSame(builder, builder.overwrite(true));
            assertSame(builder, builder.author("author"));
            assertSame(builder, builder.jsonConfig("/path/to/config"));
            assertSame(builder, builder.jdbcUrl("jdbc:test"));
            assertSame(builder, builder.jdbcUsername("root"));
            assertSame(builder, builder.jdbcPassword("pwd"));
        }

        @Test
        @DisplayName("所有 setter 方法正确更新 ctx")
        void settersUpdateContext() {
            GenerationContext ctx = SpringForgeTemplate.forge()
                    .mode(GenerationMode.DDD)
                    .projectName("myapp")
                    .groupId("org.example")
                    .basePackage("org.example.app")
                    .outputDir("./gen")
                    .overwrite(true)
                    .author("李四")
                    .jsonConfig("/config.json")
                    .jdbcUrl("jdbc:mysql://localhost/db")
                    .jdbcUsername("admin")
                    .jdbcPassword("pass")
                    .getContext();

            assertEquals(GenerationMode.DDD, ctx.getMode());
            assertEquals("myapp", ctx.getProjectName());
            assertEquals("org.example", ctx.getGroupId());
            assertEquals("org.example.app", ctx.getBasePackage());
            assertEquals("./gen", ctx.getOutputDir());
            assertTrue(ctx.isOverwrite());
            assertEquals("李四", ctx.getAuthor());
            assertEquals("/config.json", ctx.getJsonConfigPath());
            assertEquals("jdbc:mysql://localhost/db", ctx.getJdbcUrl());
            assertEquals("admin", ctx.getJdbcUsername());
            assertEquals("pass", ctx.getJdbcPassword());
        }

        @Test
        @DisplayName("options Consumer 正确修改选项")
        void optionsConsumer() {
            GenerationContext ctx = SpringForgeTemplate.forge()
                    .options(opt -> {
                        opt.setGenerateDdl(true);
                        opt.setIdType("AUTO");
                    })
                    .getContext();

            assertTrue(ctx.getOptions().isGenerateDdl());
            assertEquals("AUTO", ctx.getOptions().getIdType());
        }

        @Test
        @DisplayName("tables 可变参数设置")
        void tablesVarargs() {
            TableInfo t1 = new TableInfo();
            t1.setName("user");
            TableInfo t2 = new TableInfo();
            t2.setName("order");

            GenerationContext ctx = SpringForgeTemplate.forge()
                    .tables(t1, t2)
                    .getContext();

            assertEquals(2, ctx.getTables().size());
            assertEquals("user", ctx.getTables().get(0).getName());
            assertEquals("order", ctx.getTables().get(1).getName());
        }

        @Test
        @DisplayName("tables List 设置")
        void tablesList() {
            TableInfo t1 = new TableInfo();
            t1.setName("product");

            GenerationContext ctx = SpringForgeTemplate.forge()
                    .tables(List.of(t1))
                    .getContext();

            assertEquals(1, ctx.getTables().size());
            assertEquals("product", ctx.getTables().get(0).getName());
        }

        @Test
        @DisplayName("默认值验证")
        void defaultValues() {
            GenerationContext ctx = SpringForgeTemplate.forge().getContext();

            assertEquals(GenerationMode.MVC, ctx.getMode());
            assertEquals("demo", ctx.getProjectName());
            assertEquals("com.example", ctx.getGroupId());
            assertEquals("com.example.demo", ctx.getBasePackage());
            assertEquals(".", ctx.getOutputDir());
            assertEquals("CodeGenerator", ctx.getAuthor());
            assertFalse(ctx.isOverwrite());
        }
    }

    @Nested
    @DisplayName("构造函数")
    class ConstructorTests {

        @Test
        @DisplayName("SpringForgeTemplate 构造函数私有")
        void privateConstructor() throws Exception {
            Constructor<SpringForgeTemplate> ctor = SpringForgeTemplate.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(ctor.getModifiers()));
        }
    }

    @Test
    @DisplayName("集成：通过 API 生成 MVC 项目（快速验证）")
    void quickMvcGeneration() throws Exception {
        java.nio.file.Path outputDir = java.nio.file.Files.createTempDirectory("quick-mvc-");

        TableInfo table = new TableInfo();
        table.setName("item");
        table.setComment("项目");
        table.setEntityName("Item");
        table.setEntityNameLower("item");
        table.setMappingPath("/item");

        ColumnInfo id = new ColumnInfo();
        id.setRawName("id");
        id.setFieldName("id");
        id.setFieldNameUpper("Id");
        id.setPrimaryKey(true);
        id.setJavaType("java.lang.Long");
        id.setJavaTypeShort("Long");
        id.setJavaTypeBoxed("Long");
        id.setJdbcType("BIGINT");
        id.setXmlJdbcType("BIGINT");
        table.getColumns().add(id);

        ColumnInfo name = new ColumnInfo();
        name.setRawName("name");
        name.setFieldName("name");
        name.setFieldNameUpper("Name");
        name.setJavaType("java.lang.String");
        name.setJavaTypeShort("String");
        name.setJavaTypeBoxed("String");
        name.setJdbcType("VARCHAR");
        name.setXmlJdbcType("VARCHAR");
        name.setColumnComment("名称");
        table.getColumns().add(name);

        SpringForgeTemplate.forge()
                .mode(GenerationMode.MVC)
                .basePackage("com.quick.test")
                .outputDir(outputDir.toString())
                .overwrite(true)
                .tables(table)
                .generate();

        assertTrue(java.nio.file.Files.exists(outputDir.resolve(
                "src/main/java/com/quick/test/entity/Item.java")));
    }
}
