package org.liwanqi42.springforge.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GeneratorDescriptor 记录类测试。
 */
@DisplayName("GeneratorDescriptor 生成器描述符")
class GeneratorDescriptorTest {

    @Test
    @DisplayName("java 工厂方法正确创建")
    void javaFactory() {
        GeneratorDescriptor desc = GeneratorDescriptor.java("java/entity.ftl", ".entity", "");

        assertEquals("java/entity.ftl", desc.templatePath());
        assertEquals(".entity", desc.packageSuffix());
        assertEquals("", desc.classNameSuffix());
        assertEquals(OutputTarget.JAVA, desc.outputTarget());
        assertEquals("", desc.resourceSubPath());
    }

    @Test
    @DisplayName("resource 工厂方法正确创建")
    void resourceFactory() {
        GeneratorDescriptor desc = GeneratorDescriptor.resource("java/mapper-xml.ftl", "mapper", "Mapper.xml");

        assertEquals("java/mapper-xml.ftl", desc.templatePath());
        assertEquals("", desc.packageSuffix());
        assertEquals("Mapper.xml", desc.classNameSuffix());
        assertEquals(OutputTarget.RESOURCE, desc.outputTarget());
        assertEquals("mapper", desc.resourceSubPath());
    }

    @Test
    @DisplayName("record equals 和 hashCode")
    void equality() {
        GeneratorDescriptor a = GeneratorDescriptor.java("t.ftl", ".pkg", "Suffix");
        GeneratorDescriptor b = GeneratorDescriptor.java("t.ftl", ".pkg", "Suffix");
        GeneratorDescriptor c = GeneratorDescriptor.java("t.ftl", ".other", "Suffix");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("record toString 包含字段值")
    void toStringIncludesFields() {
        GeneratorDescriptor desc = GeneratorDescriptor.java("t.ftl", ".pkg", "Suffix");
        String str = desc.toString();
        assertTrue(str.contains("t.ftl"));
        assertTrue(str.contains(".pkg"));
    }

    @Test
    @DisplayName("MVC 模式常用描述符")
    void mvcDescriptors() {
        GeneratorDescriptor entity = GeneratorDescriptor.java("java/entity.ftl", ".entity", "");
        GeneratorDescriptor mapper = GeneratorDescriptor.java("java/mapper.ftl", ".mapper", "Mapper");
        GeneratorDescriptor service = GeneratorDescriptor.java("java/service.ftl", ".service", "Service");
        GeneratorDescriptor controller = GeneratorDescriptor.java("java/controller.ftl", ".controller", "Controller");

        assertEquals(OutputTarget.JAVA, entity.outputTarget());
        assertEquals("Mapper", mapper.classNameSuffix());
        assertEquals(".service", service.packageSuffix());
        assertEquals("Controller", controller.classNameSuffix());
    }

    @Test
    @DisplayName("DDD 模式常用描述符")
    void dddDescriptors() {
        GeneratorDescriptor entity = GeneratorDescriptor.java("java/entity.ftl", ".domain.model", "");
        GeneratorDescriptor repository = GeneratorDescriptor.java("java/mapper.ftl", ".domain.repository", "Repository");
        GeneratorDescriptor domainService = GeneratorDescriptor.java("java/domain-service.ftl", ".domain.service", "DomainService");
        GeneratorDescriptor controller = GeneratorDescriptor.java("java/controller.ftl", ".adapter.web", "Controller");

        assertEquals(".domain.model", entity.packageSuffix());
        assertEquals("Repository", repository.classNameSuffix());
        assertEquals("DomainService", domainService.classNameSuffix());
        assertEquals(".adapter.web", controller.packageSuffix());
    }
}
