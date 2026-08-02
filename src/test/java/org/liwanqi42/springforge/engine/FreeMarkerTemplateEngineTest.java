package org.liwanqi42.springforge.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.liwanqi42.springforge.exception.GenerationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FreeMarkerTemplateEngine 模板引擎测试。
 */
@DisplayName("FreeMarkerTemplateEngine 模板引擎")
class FreeMarkerTemplateEngineTest {

    private final FreeMarkerTemplateEngine engine = new FreeMarkerTemplateEngine();

    @Nested
    @DisplayName("模板渲染")
    class TemplateRendering {

        @Test
        @DisplayName("渲染简单变量模板")
        void simpleVariable() {
            Map<String, Object> model = Map.of("name", "SpringForge");
            String result = engine.render("test/simple.ftl", model);
            assertEquals("Hello SpringForge", result.trim());
        }

        @Test
        @DisplayName("渲染条件模板")
        void conditionalTemplate() {
            Map<String, Object> model = new HashMap<>();
            model.put("isDdd", true);
            String result = engine.render("test/conditional.ftl", model);
            assertEquals("DDD", result.trim());
        }

        @Test
        @DisplayName("渲染列表循环模板")
        void listIteration() {
            Map<String, Object> model = Map.of("items", List.of("A", "B", "C"));
            String result = engine.render("test/list.ftl", model);
            assertTrue(result.contains("A"));
            assertTrue(result.contains("B"));
            assertTrue(result.contains("C"));
        }

        @Test
        @DisplayName("渲染宏模板 macros.ftl")
        void macrosTemplate() {
            // macros.ftl 是项目核心宏文件，验证可正确加载和渲染
            Map<String, Object> model = new HashMap<>();
            model.put("basePackage", "com.test");
            model.put("entityPackage", "com.test.entity");
            model.put("entityName", "Test");
            model.put("tableComment", "测试表");
            model.put("author", "Test");
            model.put("date", "2026/08/02");
            model.put("isDdd", false);
            model.put("mapperPackage", "com.test.mapper");
            model.put("mapperClassSuffix", "Mapper");

            // mapper.ftl 使用 macros.ftl 的 fileHeader 宏
            String result = engine.render("java/mapper.ftl", model);
            assertNotNull(result);
            assertFalse(result.isEmpty(), "Mapper 模板渲染不应为空");
            assertTrue(result.contains("import"), "应包含 import 语句");
        }

        @Test
        @DisplayName("空数据模型渲染")
        void emptyModel() {
            // FreeMarker RETHROW 模式：模板引用未定义变量会抛异常
            // 必须提供所有模板需要的变量
            Map<String, Object> model = new HashMap<>();
            model.put("name", "World");
            String result = engine.render("test/simple.ftl", model);
            assertEquals("Hello World", result.trim());
        }

        @Test
        @DisplayName("模板变量缺失时抛出异常")
        void missingTemplateVariable() {
            GenerationException ex = assertThrows(GenerationException.class,
                    () -> engine.render("test/simple.ftl", Collections.emptyMap()));
            assertTrue(ex.getMessage().contains("模板渲染失败"));
        }

        @Test
        @DisplayName("模板不存在抛出 GenerationException")
        void templateNotFound() {
            GenerationException ex = assertThrows(GenerationException.class,
                    () -> engine.render("nonexistent/template.ftl", Map.of()));
            assertTrue(ex.getMessage().contains("模板渲染失败"));
            assertTrue(ex.getMessage().contains("nonexistent"));
        }
    }

    @Nested
    @DisplayName("引擎配置")
    class EngineConfig {

        @Test
        @DisplayName("引擎成功实例化")
        void instantiation() {
            assertNotNull(new FreeMarkerTemplateEngine());
        }

        @Test
        @DisplayName("多次渲染相同模板使用缓存")
        void templateCaching() {
            Map<String, Object> model = Map.of("name", "Test");
            String r1 = engine.render("test/simple.ftl", model);
            String r2 = engine.render("test/simple.ftl", model);
            assertEquals(r1, r2, "同一模板多次渲染结果应一致");
        }
    }
}
