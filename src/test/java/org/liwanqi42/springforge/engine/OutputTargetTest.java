package org.liwanqi42.springforge.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OutputTarget 枚举测试。
 */
@DisplayName("OutputTarget 输出目标枚举")
class OutputTargetTest {

    @Test
    @DisplayName("包含 JAVA 和 RESOURCE 两个值")
    void containsBothValues() {
        OutputTarget[] values = OutputTarget.values();
        assertEquals(2, values.length);
        assertTrue(contains(values, OutputTarget.JAVA));
        assertTrue(contains(values, OutputTarget.RESOURCE));
    }

    @Test
    @DisplayName("valueOf 正确解析")
    void valueOf() {
        assertEquals(OutputTarget.JAVA, OutputTarget.valueOf("JAVA"));
        assertEquals(OutputTarget.RESOURCE, OutputTarget.valueOf("RESOURCE"));
    }

    @Test
    @DisplayName("name 方法返回名称")
    void name() {
        assertEquals("JAVA", OutputTarget.JAVA.name());
        assertEquals("RESOURCE", OutputTarget.RESOURCE.name());
    }

    private boolean contains(OutputTarget[] values, OutputTarget target) {
        for (OutputTarget v : values) {
            if (v == target) return true;
        }
        return false;
    }
}
