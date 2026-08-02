package org.liwanqi42.springforge.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerationException 异常类测试。
 */
@DisplayName("GenerationException 异常")
class GenerationExceptionTest {

    @Test
    @DisplayName("单参数构造器正确设置消息")
    void messageOnlyConstructor() {
        String msg = "生成失败";
        GenerationException ex = new GenerationException(msg);

        assertEquals(msg, ex.getMessage());
        assertNull(ex.getCause());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("双参数构造器正确设置消息和原因")
    void messageAndCauseConstructor() {
        String msg = "模板渲染失败";
        Throwable cause = new IllegalArgumentException("参数无效");
        GenerationException ex = new GenerationException(msg, cause);

        assertEquals(msg, ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("异常链正确传递")
    void exceptionChaining() {
        IOException original = new IOException("文件读取失败");
        GenerationException wrapped = new GenerationException("配置加载失败", original);

        assertEquals(original, wrapped.getCause());
        assertEquals("配置加载失败", wrapped.getMessage());
    }

    @Test
    @DisplayName("空消息构造")
    void emptyMessage() {
        GenerationException ex = new GenerationException("");

        assertEquals("", ex.getMessage());
    }

    @Test
    @DisplayName("null 消息构造")
    void nullMessage() {
        GenerationException ex = new GenerationException(null);

        assertNull(ex.getMessage());
    }

    // 内部异常类用于测试
    private static class IOException extends Exception {
        public IOException(String message) {
            super(message);
        }
    }
}
