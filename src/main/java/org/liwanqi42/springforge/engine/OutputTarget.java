package org.liwanqi42.springforge.engine;

/**
 * 输出目标类型，区分 Java 源文件和资源文件。
 *
 * @author Spring-Forge
 * @date 2026/08/02
 */
public enum OutputTarget {
    /** Java 源文件，写入 src/main/java 下 */
    JAVA,
    /** 资源文件，写入 src/main/resources 下 */
    RESOURCE
}
