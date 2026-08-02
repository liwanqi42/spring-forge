package org.liwanqi42.springforge.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlTypeMapper JDBC→Java 类型映射全面测试。
 */
@DisplayName("SqlTypeMapper SQL 类型映射")
class SqlTypeMapperTest {

    // ======================== getJavaType ========================

    @Nested
    @DisplayName("getJavaType — JDBC 类型转 Java 类型")
    class GetJavaType {

        @ParameterizedTest
        @CsvSource({
                "VARCHAR,     java.lang.String,   String,   String",
                "CHAR,        java.lang.String,   String,   String",
                "TEXT,        java.lang.String,   String,   String",
                "MEDIUMTEXT,  java.lang.String,   String,   String",
                "LONGTEXT,    java.lang.String,   String,   String",
                "TINYINT,     java.lang.Integer,  Integer,  Integer",
                "SMALLINT,    java.lang.Integer,  Integer,  Integer",
                "INT,         java.lang.Integer,  Integer,  Integer",
                "INTEGER,     java.lang.Integer,  Integer,  Integer",
                "BIGINT,      java.lang.Long,     Long,     Long",
                "FLOAT,       java.math.BigDecimal, BigDecimal, BigDecimal",
                "DOUBLE,      java.math.BigDecimal, BigDecimal, BigDecimal",
                "DECIMAL,     java.math.BigDecimal, BigDecimal, BigDecimal",
                "DATE,        java.time.LocalDate, LocalDate, LocalDate",
                "TIME,        java.time.LocalTime, LocalTime, LocalTime",
                "DATETIME,    java.time.LocalDateTime, LocalDateTime, LocalDateTime",
                "TIMESTAMP,   java.time.LocalDateTime, LocalDateTime, LocalDateTime",
                "BIT,         java.lang.Boolean,  Boolean,  Boolean",
                "BOOLEAN,     java.lang.Boolean,  Boolean,  Boolean",
                "BLOB,        byte[],             byte[],    byte[]",
                "LONGBLOB,    byte[],             byte[],    byte[]",
        })
        @DisplayName("所有已知类型正确映射")
        void knownTypes(String jdbcType, String fullyQualified, String shortName, String boxedName) {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType(jdbcType);
            assertNotNull(info);
            assertEquals(fullyQualified, info.fullyQualified(), "fullyQualified 不匹配: " + jdbcType);
            assertEquals(shortName, info.shortName(), "shortName 不匹配: " + jdbcType);
            assertEquals(boxedName, info.boxedName(), "boxedName 不匹配: " + jdbcType);
        }

        @Test
        @DisplayName("null 输入返回默认 String 类型")
        void nullInput() {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType(null);
            assertEquals("java.lang.String", info.fullyQualified());
            assertEquals("String", info.shortName());
        }

        @Test
        @DisplayName("未知类型返回默认 String 类型")
        void unknownType() {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType("UNKNOWN_CUSTOM_TYPE");
            assertEquals("java.lang.String", info.fullyQualified());
            assertEquals("String", info.shortName());
        }

        @ParameterizedTest
        @ValueSource(strings = {"VARCHAR(255)", "VARCHAR(50)", "VARCHAR(1000)"})
        @DisplayName("带参数的 VARCHAR(n) 正确映射为 String")
        void varcharWithSize(String jdbcType) {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType(jdbcType);
            assertEquals("java.lang.String", info.fullyQualified());
            assertEquals("String", info.shortName());
        }

        @ParameterizedTest
        @ValueSource(strings = {"BIGINT(20)", "INT(11)", "TINYINT(4)", "DECIMAL(10,2)"})
        @DisplayName("带参数的数值类型正确映射")
        void numericWithParams(String jdbcType) {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType(jdbcType);
            assertNotNull(info);
            assertNotNull(info.shortName());
        }

        @Test
        @DisplayName("UNSIGNED 修饰符正确处理")
        void unsignedTypes() {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType("BIGINT UNSIGNED");
            assertEquals("java.lang.Long", info.fullyQualified());

            info = SqlTypeMapper.getJavaType("INT UNSIGNED");
            assertEquals("java.lang.Integer", info.fullyQualified());

            info = SqlTypeMapper.getJavaType("TINYINT UNSIGNED");
            assertEquals("java.lang.Integer", info.fullyQualified());
        }

        @ParameterizedTest
        @ValueSource(strings = {"varchar", "VARCHAR", "Varchar"})
        @DisplayName("大小写不敏感")
        void caseInsensitive(String jdbcType) {
            SqlTypeMapper.JavaTypeInfo info = SqlTypeMapper.getJavaType(jdbcType);
            assertEquals("java.lang.String", info.fullyQualified());
        }

        @Test
        @DisplayName("JavaTypeInfo record 相等性")
        void javaTypeInfoEquality() {
            SqlTypeMapper.JavaTypeInfo a = new SqlTypeMapper.JavaTypeInfo("a", "b", "c");
            SqlTypeMapper.JavaTypeInfo b = new SqlTypeMapper.JavaTypeInfo("a", "b", "c");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }
    }

    // ======================== getXmlJdbcType ========================

    @Nested
    @DisplayName("getXmlJdbcType — JDBC 类型转 MyBatis XML jdbcType")
    class GetXmlJdbcType {

        @ParameterizedTest
        @CsvSource({
                "VARCHAR,     VARCHAR",
                "CHAR,        VARCHAR",
                "TEXT,        LONGVARCHAR",
                "TINYINT,     TINYINT",
                "SMALLINT,    SMALLINT",
                "INT,         INTEGER",
                "INTEGER,     INTEGER",
                "BIGINT,      BIGINT",
                "FLOAT,       FLOAT",
                "DOUBLE,      DOUBLE",
                "DECIMAL,     DECIMAL",
                "DATE,        DATE",
                "TIME,        TIME",
                "DATETIME,    TIMESTAMP",
                "TIMESTAMP,   TIMESTAMP",
                "BIT,         BOOLEAN",
                "BOOLEAN,     BOOLEAN",
                "BLOB,        BLOB",
        })
        @DisplayName("所有已知类型的 XML JDBC 映射")
        void knownXmlTypes(String jdbcType, String expectedXmlType) {
            assertEquals(expectedXmlType, SqlTypeMapper.getXmlJdbcType(jdbcType));
        }

        @Test
        @DisplayName("null 输入返回默认 VARCHAR")
        void nullInput() {
            assertEquals("VARCHAR", SqlTypeMapper.getXmlJdbcType(null));
        }

        @Test
        @DisplayName("未知类型返回默认 VARCHAR")
        void unknownType() {
            assertEquals("VARCHAR", SqlTypeMapper.getXmlJdbcType("CUSTOM_ENUM_TYPE"));
        }

        @Test
        @DisplayName("带参数的 JDBC 类型正确处理")
        void withParams() {
            assertEquals("INTEGER", SqlTypeMapper.getXmlJdbcType("INT(11)"));
            assertEquals("VARCHAR", SqlTypeMapper.getXmlJdbcType("VARCHAR(255)"));
            assertEquals("DECIMAL", SqlTypeMapper.getXmlJdbcType("DECIMAL(10,2)"));
        }

        @Test
        @DisplayName("小写输入正确映射")
        void lowerCase() {
            assertEquals("INTEGER", SqlTypeMapper.getXmlJdbcType("int"));
            assertEquals("VARCHAR", SqlTypeMapper.getXmlJdbcType("varchar"));
            assertEquals("BIGINT", SqlTypeMapper.getXmlJdbcType("bigint"));
        }
    }
}
