package org.liwanqi42.springforge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerationOptions 生成选项模型测试。
 */
@DisplayName("GenerationOptions 生成选项")
class GenerationOptionsTest {

    @Test
    @DisplayName("所有默认值正确")
    void defaultValues() {
        GenerationOptions opt = new GenerationOptions();

        assertFalse(opt.isGenerateDdl(), "默认不生成 DDL");
        assertFalse(opt.isGenerateTests(), "默认不生成测试");
        assertEquals("deleted", opt.getLogicDeleteField(), "默认逻辑删除字段");
        assertEquals("1", opt.getLogicDeleteValue(), "默认逻辑删除值");
        assertEquals("0", opt.getLogicNotDeleteValue(), "默认逻辑未删除值");
        assertEquals("version", opt.getOptimisticLockField(), "默认乐观锁字段");
        assertEquals("createTime,createBy", opt.getAutoFillInsertFields(), "默认插入填充字段");
        assertEquals("updateTime,updateBy", opt.getAutoFillUpdateFields(), "默认更新填充字段");
        assertEquals("ASSIGN_ID", opt.getIdType(), "默认 ID 类型");
        assertEquals("", opt.getTablePrefix(), "默认无表前缀");
        assertFalse(opt.isUseLombok(), "默认不使用 Lombok");
        assertEquals("yyyy-MM-dd HH:mm:ss", opt.getDateFormat(), "默认日期格式");
        assertTrue(opt.isMinimalMyBatisConfig(), "默认精简 MyBatis 配置");
    }

    @Test
    @DisplayName("完整属性设置和读取")
    void fullPropertyAccess() {
        GenerationOptions opt = new GenerationOptions();

        opt.setGenerateDdl(true);
        opt.setGenerateTests(true);
        opt.setLogicDeleteField("is_deleted");
        opt.setLogicDeleteValue("Y");
        opt.setLogicNotDeleteValue("N");
        opt.setOptimisticLockField("rev");
        opt.setAutoFillInsertFields("createdAt");
        opt.setAutoFillUpdateFields("updatedAt");
        opt.setIdType("AUTO");
        opt.setTablePrefix("t_");
        opt.setUseLombok(true);
        opt.setDateFormat("yyyy-MM-dd");
        opt.setMinimalMyBatisConfig(false);

        assertTrue(opt.isGenerateDdl());
        assertTrue(opt.isGenerateTests());
        assertEquals("is_deleted", opt.getLogicDeleteField());
        assertEquals("Y", opt.getLogicDeleteValue());
        assertEquals("N", opt.getLogicNotDeleteValue());
        assertEquals("rev", opt.getOptimisticLockField());
        assertEquals("createdAt", opt.getAutoFillInsertFields());
        assertEquals("updatedAt", opt.getAutoFillUpdateFields());
        assertEquals("AUTO", opt.getIdType());
        assertEquals("t_", opt.getTablePrefix());
        assertTrue(opt.isUseLombok());
        assertEquals("yyyy-MM-dd", opt.getDateFormat());
        assertFalse(opt.isMinimalMyBatisConfig());
    }

    @Test
    @DisplayName("逻辑删除禁用场景：logicDeleteField 设为空字符串")
    void logicDeleteDisabled() {
        GenerationOptions opt = new GenerationOptions();
        opt.setLogicDeleteField("");

        assertEquals("", opt.getLogicDeleteField());
    }

    @Test
    @DisplayName("乐观锁禁用场景：optimisticLockField 设为空字符串")
    void optimisticLockDisabled() {
        GenerationOptions opt = new GenerationOptions();
        opt.setOptimisticLockField("");

        assertEquals("", opt.getOptimisticLockField());
    }

    @Test
    @DisplayName("ID 类型设为 ASSIGN_ID（雪花算法）")
    void idTypeSnowflake() {
        GenerationOptions opt = new GenerationOptions();
        opt.setIdType("ASSIGN_ID");

        assertEquals("ASSIGN_ID", opt.getIdType());
    }
}
