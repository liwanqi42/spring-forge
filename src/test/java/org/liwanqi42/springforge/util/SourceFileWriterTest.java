package org.liwanqi42.springforge.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.liwanqi42.springforge.exception.GenerationException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SourceFileWriter 文件写入工具测试。
 */
@DisplayName("SourceFileWriter 文件写入工具")
class SourceFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("正常写入 Java 源文件")
    void writeJavaFile() throws IOException {
        String content = "package com.test;\npublic class User {\n}";
        SourceFileWriter.writeJavaFile(tempDir, "com.test", "User", content, true);

        Path expectedFile = tempDir.resolve("src/main/java/com/test/User.java");
        assertTrue(Files.exists(expectedFile), "Java 文件应存在");
        assertEquals(content, Files.readString(expectedFile, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("正常写入资源文件")
    void writeResourceFile() throws IOException {
        String content = "<mapper namespace=\"com.test\"></mapper>";
        SourceFileWriter.writeResourceFile(tempDir, "mapper", "UserMapper.xml", content, true);

        Path expectedFile = tempDir.resolve("src/main/resources/mapper/UserMapper.xml");
        assertTrue(Files.exists(expectedFile), "资源文件应存在");
        assertEquals(content, Files.readString(expectedFile));
    }

    @Test
    @DisplayName("overwrite=true 覆盖已存在文件")
    void overwriteFile() throws IOException {
        Path javaFile = tempDir.resolve("src/main/java/com/test/Test.java");
        Files.createDirectories(javaFile.getParent());
        Files.writeString(javaFile, "old content");

        String newContent = "new content";
        SourceFileWriter.writeJavaFile(tempDir, "com.test", "Test", newContent, true);

        assertEquals(newContent, Files.readString(javaFile, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("overwrite=false 跳过已存在文件")
    void skipExistingFile() throws IOException {
        Path javaFile = tempDir.resolve("src/main/java/com/test/Test.java");
        Files.createDirectories(javaFile.getParent());
        String oldContent = "old content";
        Files.writeString(javaFile, oldContent);

        String newContent = "new content";
        SourceFileWriter.writeJavaFile(tempDir, "com.test", "Test", newContent, false);

        assertEquals(oldContent, Files.readString(javaFile, StandardCharsets.UTF_8),
                "overwrite=false 时应保留旧内容");
    }

    @Test
    @DisplayName("pom.xml 写入")
    void writePomXml() throws IOException {
        String content = "<project></project>";
        SourceFileWriter.writePomXml(tempDir, content, true);

        Path pomFile = tempDir.resolve("pom.xml");
        assertTrue(Files.exists(pomFile));
        assertEquals(content, Files.readString(pomFile));
    }

    @Test
    @DisplayName("深层目录自动创建")
    void deepDirectoryCreation() throws IOException {
        String content = "package com.a.b.c.d;\npublic class Deep {\n}";
        SourceFileWriter.writeJavaFile(tempDir, "com.a.b.c.d", "Deep", content, true);

        Path expectedFile = tempDir.resolve("src/main/java/com/a/b/c/d/Deep.java");
        assertTrue(Files.exists(expectedFile));
    }

    @Test
    @DisplayName("UTF-8 中文内容正确写入")
    void utf8ChineseContent() throws IOException {
        String content = "// 用户实体\npublic class 用户 {\n    // 姓名\n    private String 姓名;\n}";
        SourceFileWriter.writeJavaFile(tempDir, "com.test", "用户", content, true);

        Path file = tempDir.resolve("src/main/java/com/test/用户.java");
        assertTrue(Files.exists(file));
        assertEquals(content, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("空内容写入")
    void emptyContent() throws IOException {
        SourceFileWriter.writeJavaFile(tempDir, "com.test", "Empty", "", true);

        Path file = tempDir.resolve("src/main/java/com/test/Empty.java");
        assertTrue(Files.exists(file));
        assertEquals("", Files.readString(file));
    }

    @Test
    @DisplayName("writeFile 写入任意文件")
    void writeFile() throws IOException {
        String content = "hello=world";
        SourceFileWriter.writeFile(tempDir, "test.properties", content, true);

        Path file = tempDir.resolve("test.properties");
        assertTrue(Files.exists(file));
        assertEquals(content, Files.readString(file));
    }

    @Test
    @DisplayName("资源文件写入空路径")
    void resourceFileWithEmptyPath() throws IOException {
        String content = "server.port=8080";
        SourceFileWriter.writeResourceFile(tempDir, "", "application.properties", content, true);

        Path file = tempDir.resolve("src/main/resources/application.properties");
        assertTrue(Files.exists(file));
        assertEquals(content, Files.readString(file));
    }

    @Test
    @DisplayName("outputDir 不存在时自动创建")
    void outputDirAutoCreation() throws IOException {
        Path nonExistent = tempDir.resolve("nonexistent/subdir");
        String content = "package com.test;\npublic class Auto {\n}";
        SourceFileWriter.writeJavaFile(nonExistent, "com.test", "Auto", content, true);

        Path file = nonExistent.resolve("src/main/java/com/test/Auto.java");
        assertTrue(Files.exists(file));
    }
}
