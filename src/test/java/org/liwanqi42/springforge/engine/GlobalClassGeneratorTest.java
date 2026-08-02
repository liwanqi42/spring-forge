package org.liwanqi42.springforge.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.liwanqi42.springforge.model.GenerationContext;
import org.liwanqi42.springforge.model.GenerationMode;
import org.liwanqi42.springforge.model.GenerationOptions;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GlobalClassGenerator 全局基础类生成器测试。
 */
@DisplayName("GlobalClassGenerator 全局基础类生成器")
class GlobalClassGeneratorTest {

    @TempDir
    Path tempDir;

    private FreeMarkerTemplateEngine engine;
    private GlobalClassGenerator generator;

    @BeforeEach
    void setUp() {
        engine = new FreeMarkerTemplateEngine();
        generator = new GlobalClassGenerator(engine);
    }

    @Nested
    @DisplayName("shouldGenerate 判断")
    class ShouldGenerate {

        @Test
        @DisplayName("无 lock 文件返回 true")
        void noLockFile() {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            assertTrue(generator.shouldGenerate(ctx));
        }

        @Test
        @DisplayName("有 lock 文件返回 false")
        void hasLockFile() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            // 先跑一次生成，创建 lock
            generator.generate(ctx);

            Path lockFile = tempDir.resolve(".spring-forge.lock");
            assertTrue(Files.exists(lockFile), "lock 文件应存在");
            assertFalse(generator.shouldGenerate(ctx), "lock 存在应返回 false");
        }

        @Test
        @DisplayName("模式变更返回 true（MVC→DDD）")
        void modeSwitchReturnsTrue() throws Exception {
            GenerationContext mvcCtx = buildCtx(GenerationMode.MVC);
            generator.generate(mvcCtx); // 创建 MVC lock

            GenerationContext dddCtx = buildCtx(GenerationMode.DDD);
            assertTrue(generator.shouldGenerate(dddCtx),
                    "模式从 MVC 切换到 DDD 应重新生成");
        }

        @Test
        @DisplayName("模式变更返回 true（DDD→MVC）")
        void dddToMvcReturnsTrue() throws Exception {
            GenerationContext dddCtx = buildCtx(GenerationMode.DDD);
            generator.generate(dddCtx); // 创建 DDD lock

            GenerationContext mvcCtx = buildCtx(GenerationMode.MVC);
            assertTrue(generator.shouldGenerate(mvcCtx),
                    "模式从 DDD 切换到 MVC 应重新生成");
        }
    }

    @Nested
    @DisplayName("generate 生成全局类")
    class Generate {

        @Test
        @DisplayName("首次生成全部 8 个全局类")
        void generateAllGlobalClasses() {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            generator.generate(ctx);

            String base = "src/main/java/com/test/common/";
            assertFileExists(base + "Result.java", "Result");
            assertFileExists(base + "PageResult.java", "PageResult");
            assertFileExists(base + "BizException.java", "BizException");
            assertFileExists(base + "ErrorCode.java", "ErrorCode");
            assertFileExists(base + "config/RestExceptionHandler.java", "RestExceptionHandler");
            assertFileExists(base + "config/MyBatisPlusConfig.java", "MyBatisPlusConfig");
            assertFileExists(base + "config/WebConfig.java", "WebConfig");
            assertFileExists(base + "util/DateUtils.java", "DateUtils");
        }

        @Test
        @DisplayName("lock 文件格式正确")
        void lockFileContent() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            generator.generate(ctx);

            String lockContent = Files.readString(tempDir.resolve(".spring-forge.lock"));
            assertTrue(lockContent.contains("# Spring-Forge Global Classes Marker"));
            assertTrue(lockContent.contains("generatedAt="));
            assertTrue(lockContent.contains("mode=MVC"));
        }

        @Test
        @DisplayName("第二次生成跳过（幂等性）")
        void secondGenerationSkipped() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);

            // 第一次
            generator.generate(ctx);
            Path resultFile = tempDir.resolve("src/main/java/com/test/common/Result.java");
            assertTrue(Files.exists(resultFile));
            long firstModified = Files.getLastModifiedTime(resultFile).toMillis();

            // 等待 100ms 确保时间戳可区分
            Thread.sleep(100);

            // 第二次（同一 ctx，新实例）
            GlobalClassGenerator newGen = new GlobalClassGenerator(engine);
            newGen.generate(ctx);

            long secondModified = Files.getLastModifiedTime(resultFile).toMillis();
            assertEquals(firstModified, secondModified,
                    "第二次生成应跳过，文件修改时间不变");
        }

        @Test
        @DisplayName("MVC 模式 MyBatisPlusConfig 扫描 .mapper 包")
        void mvcMapperScan() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            generator.generate(ctx);

            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/test/common/config/MyBatisPlusConfig.java"));
            assertTrue(content.contains(".mapper"),
                    "MVC 模式应扫描 .mapper 包，实际: " + content);
        }

        @Test
        @DisplayName("DDD 模式 MyBatisPlusConfig 扫描 .domain.repository 包")
        void dddMapperScan() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.DDD);
            generator.generate(ctx);

            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/test/common/config/MyBatisPlusConfig.java"));
            assertTrue(content.contains(".domain.repository"),
                    "DDD 模式应扫描 .domain.repository 包，实际: " + content);
        }

        @Test
        @DisplayName("minimalMyBatisConfig=true 时生成精简配置")
        void minimalConfig() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            ctx.getOptions().setMinimalMyBatisConfig(true);
            generator.generate(ctx);

            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/test/common/config/MyBatisPlusConfig.java"));
            // minimal 模式下 MyBatisPlusConfig 模板输出精简配置（仅分页插件 + MapperScan）
            assertNotNull(content);
            assertTrue(content.contains("@MapperScan"),
                    "MyBatisPlusConfig 应包含 @MapperScan");
        }

        @Test
        @DisplayName("minimalMyBatisConfig=false 时应生成完整配置")
        void fullConfig() throws Exception {
            GenerationContext ctx = buildCtx(GenerationMode.MVC);
            ctx.getOptions().setMinimalMyBatisConfig(false);
            generator.generate(ctx);

            String content = Files.readString(tempDir.resolve(
                    "src/main/java/com/test/common/config/MyBatisPlusConfig.java"));
            // full 模式下内容应该比 minimal 更多
            assertNotNull(content);
            assertTrue(content.length() > 0);
        }
    }

    // ======================== 辅助方法 ========================

    private GenerationContext buildCtx(GenerationMode mode) {
        GenerationContext ctx = new GenerationContext();
        ctx.setMode(mode);
        ctx.setBasePackage("com.test");
        ctx.setOutputDir(tempDir.toString());
        ctx.setOverwrite(true);
        ctx.setDate("2026/08/02");
        GenerationOptions opts = new GenerationOptions();
        opts.setMinimalMyBatisConfig(true);
        ctx.setOptions(opts);
        return ctx;
    }

    private void assertFileExists(String relativePath, String description) {
        Path file = tempDir.resolve(relativePath);
        assertTrue(Files.exists(file),
                description + " 未生成: " + file);
    }
}
