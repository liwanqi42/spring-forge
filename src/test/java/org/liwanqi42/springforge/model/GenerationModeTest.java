package org.liwanqi42.springforge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerationMode 枚举测试。
 */
@DisplayName("GenerationMode 生成模式枚举")
class GenerationModeTest {

    @Test
    @DisplayName("枚举包含 MVC 和 DDD 两个值")
    void containsBothModes() {
        GenerationMode[] values = GenerationMode.values();
        assertEquals(2, values.length);
        assertTrue(contains(values, GenerationMode.MVC));
        assertTrue(contains(values, GenerationMode.DDD));
    }

    @Test
    @DisplayName("valueOf(\"MVC\") 正确解析")
    void valueOfMvc() {
        assertEquals(GenerationMode.MVC, GenerationMode.valueOf("MVC"));
    }

    @Test
    @DisplayName("valueOf(\"DDD\") 正确解析")
    void valueOfDdd() {
        assertEquals(GenerationMode.DDD, GenerationMode.valueOf("DDD"));
    }

    @Test
    @DisplayName("valueOf 大小写敏感（非法值抛异常）")
    void valueOfCaseSensitive() {
        assertThrows(IllegalArgumentException.class, () -> GenerationMode.valueOf("mvc"));
        assertThrows(IllegalArgumentException.class, () -> GenerationMode.valueOf("ddd"));
        assertThrows(IllegalArgumentException.class, () -> GenerationMode.valueOf("Mvc"));
        assertThrows(IllegalArgumentException.class, () -> GenerationMode.valueOf("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> GenerationMode.valueOf(""));
    }

    @Test
    @DisplayName("valueOf(null) 抛出 NullPointerException")
    void valueOfNullThrowsNpe() {
        // Enum.valueOf 在 name 为 null 时抛出 NullPointerException
        assertThrows(NullPointerException.class, () -> GenerationMode.valueOf(null));
    }

    @Test
    @DisplayName("MVC ordinal 为 0（默认）")
    void mvcOrdinal() {
        assertEquals(0, GenerationMode.MVC.ordinal());
    }

    @Test
    @DisplayName("DDD ordinal 为 1")
    void dddOrdinal() {
        assertEquals(1, GenerationMode.DDD.ordinal());
    }

    @Test
    @DisplayName("name() 返回 \"MVC\" 和 \"DDD\"")
    void nameValues() {
        assertEquals("MVC", GenerationMode.MVC.name());
        assertEquals("DDD", GenerationMode.DDD.name());
    }

    private boolean contains(GenerationMode[] values, GenerationMode target) {
        for (GenerationMode v : values) {
            if (v == target) return true;
        }
        return false;
    }
}
