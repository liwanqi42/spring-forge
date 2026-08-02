package org.liwanqi42.springforge.engine;

/**
 * 输出目标类型，区分 Java 源文件和资源文件。
 *
 * @since 2.0.0
 */
public enum OutputTarget {
    /** Java 源文件，写入 src/main/java 下 */
    JAVA,
    /** 资源文件，写入 src/main/resources 下 */
    RESOURCE
}
