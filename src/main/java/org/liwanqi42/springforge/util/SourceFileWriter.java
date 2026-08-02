package org.liwanqi42.springforge.util;

import org.liwanqi42.springforge.exception.GenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 源代码文件写入工具。
 *
 * <p>统一处理 UTF-8 编码、目录创建、覆写控制。</p>
 */
public final class SourceFileWriter {

    private static final Logger logger = LoggerFactory.getLogger(SourceFileWriter.class);

    private SourceFileWriter() {
    }

    /**
     * 写入 Java 源文件。
     *
     * @param outputDir   输出根目录
     * @param packageName 包名
     * @param className   类名
     * @param content     文件内容
     * @param overwrite   是否覆盖已有文件
     */
    public static void writeJavaFile(Path outputDir, String packageName,
                                     String className, String content, boolean overwrite) {
        String packagePath = packageName.replace('.', '/');
        Path dir = outputDir.resolve("src/main/java").resolve(packagePath);
        writeFile(dir, className + ".java", content, overwrite);
    }

    /**
     * 写入资源文件（如 Mapper XML、properties 等）。
     */
    public static void writeResourceFile(Path outputDir, String relativePath,
                                         String fileName, String content, boolean overwrite) {
        Path dir = outputDir.resolve("src/main/resources").resolve(relativePath);
        writeFile(dir, fileName, content, overwrite);
    }

    /**
     * 写入任意文件。
     */
    public static void writeFile(Path dir, String fileName, String content, boolean overwrite) {
        File dirFile = dir.toFile();
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            throw new GenerationException("无法创建目录：" + dir);
        }
        File file = new File(dirFile, fileName);
        if (file.exists() && !overwrite) {
            logger.info("跳过已存在文件：{}", file.getAbsolutePath());
            return;
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new GenerationException("写入文件失败：" + file.getAbsolutePath(), e);
        }
        logger.info("已生成：{}", file.getAbsolutePath());
    }

    /**
     * 写入 pom.xml 文件（委托 {@link #writeFile}）。
     */
    public static void writePomXml(Path moduleDir, String content, boolean overwrite) {
        writeFile(moduleDir, "pom.xml", content, overwrite);
    }
}
