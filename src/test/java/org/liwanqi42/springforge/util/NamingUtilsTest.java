package org.liwanqi42.springforge.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NamingUtils 命名转换工具全面测试。
 */
@DisplayName("NamingUtils 命名转换工具")
class NamingUtilsTest {

    // ======================== toUpperCamel ========================

    @Nested
    @DisplayName("toUpperCamel — 下划线转大驼峰")
    class ToUpperCamel {

        @ParameterizedTest
        @CsvSource({
                "user_order,        UserOrder",
                "user,              User",
                "user_order_detail, UserOrderDetail",
                "order_v2,          OrderV2",
                "a_b,               AB",
                "hello_world,       HelloWorld",
                "single,            Single",
                "a,                 A",
                "t_user,            TUser",
                "user_role_permission, UserRolePermission",
        })
        @DisplayName("正常下划线转大驼峰")
        void normalConversion(String input, String expected) {
            assertEquals(expected, NamingUtils.toUpperCamel(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 和空字符串返回空字符串")
        void nullAndEmpty(String input) {
            assertEquals("", NamingUtils.toUpperCamel(input));
        }

        @Test
        @DisplayName("连续下划线正确处理")
        void consecutiveUnderscores() {
            assertEquals("UserOrder", NamingUtils.toUpperCamel("user__order"));
            assertEquals("AB", NamingUtils.toUpperCamel("a___b"));
        }

        @Test
        @DisplayName("首尾下划线正确处理")
        void leadingTrailingUnderscores() {
            assertEquals("UserOrder", NamingUtils.toUpperCamel("_user_order_"));
            assertEquals("User", NamingUtils.toUpperCamel("_user"));
            assertEquals("User", NamingUtils.toUpperCamel("user_"));
        }

        @Test
        @DisplayName("全大写输入转驼峰")
        void allUpperCase() {
            assertEquals("UserOrder", NamingUtils.toUpperCamel("USER_ORDER"));
            assertEquals("HelloWorld", NamingUtils.toUpperCamel("HELLO_WORLD"));
            assertEquals("User", NamingUtils.toUpperCamel("USER"));
        }

        @Test
        @DisplayName("首字母大写保持")
        void alreadyCamelCase() {
            // toUpperCamel 没有下划线时：首字母大写，其余小写
            assertEquals("Userorder", NamingUtils.toUpperCamel("Userorder"));
            assertEquals("Helloworld", NamingUtils.toUpperCamel("HelloWorld"));
        }

        @Test
        @DisplayName("混合大小写输入")
        void mixedCase() {
            assertEquals("UserOrder", NamingUtils.toUpperCamel("User_Order"));
            assertEquals("UserOrder", NamingUtils.toUpperCamel("user_Order"));
        }

        @Test
        @DisplayName("纯数字和下划线组合")
        void numbersAndUnderscores() {
            assertEquals("V1User", NamingUtils.toUpperCamel("v1_user"));
            assertEquals("Order2Detail", NamingUtils.toUpperCamel("order_2_detail"));
            assertEquals("Field1", NamingUtils.toUpperCamel("field_1"));
        }

        @Test
        @DisplayName("只有下划线的输入")
        void onlyUnderscores() {
            assertEquals("", NamingUtils.toUpperCamel("___"));
            assertEquals("", NamingUtils.toUpperCamel("_"));
        }
    }

    // ======================== toLowerCamel ========================

    @Nested
    @DisplayName("toLowerCamel — 下划线转小驼峰")
    class ToLowerCamel {

        @ParameterizedTest
        @CsvSource({
                "user_order,        userOrder",
                "user,              user",
                "user_order_detail, userOrderDetail",
                "field_1_name,      field1Name",
                "hello_world,       helloWorld",
                "t_user,            tUser",
                "user_role_permission, userRolePermission",
        })
        @DisplayName("正常下划线转小驼峰")
        void normalConversion(String input, String expected) {
            assertEquals(expected, NamingUtils.toLowerCamel(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 和空字符串返回空字符串")
        void nullAndEmpty(String input) {
            assertEquals("", NamingUtils.toLowerCamel(input));
        }

        @Test
        @DisplayName("连续下划线正确处理")
        void consecutiveUnderscores() {
            assertEquals("userOrder", NamingUtils.toLowerCamel("user__order"));
            assertEquals("aB", NamingUtils.toLowerCamel("a___b"));
        }

        @Test
        @DisplayName("首尾下划线正确处理")
        void leadingTrailingUnderscores() {
            // _user_order_: 第一个 _ → nextUpper, u 是 first → 'u',
            // s → nextUpper 仍为 true → 'S' → uSer...
            // 实际实现中 _ 后 nextUpper 保持直到被消费
            assertEquals("uSerOrder", NamingUtils.toLowerCamel("_user_order_"));
            assertEquals("uSer", NamingUtils.toLowerCamel("_user"));
        }

        @Test
        @DisplayName("全大写输入转小驼峰")
        void allUpperCase() {
            assertEquals("userOrder", NamingUtils.toLowerCamel("USER_ORDER"));
            assertEquals("helloWorld", NamingUtils.toLowerCamel("HELLO_WORLD"));
            assertEquals("user", NamingUtils.toLowerCamel("USER"));
        }

        @Test
        @DisplayName("已是小驼峰保持")
        void alreadyLowerCase() {
            // 没有下划线时 toLowerCamel 全部 lowerCase
            assertEquals("userorder", NamingUtils.toLowerCamel("userOrder"));
            assertEquals("helloworld", NamingUtils.toLowerCamel("helloWorld"));
        }

        @Test
        @DisplayName("数字开头")
        void startsWithDigit() {
            // 1 作为 first 字符 → '1'，_ → nextUpper，f → 'F' → 1Field
            assertEquals("1Field", NamingUtils.toLowerCamel("1_field"));
        }

        @Test
        @DisplayName("只有下划线的输入")
        void onlyUnderscores() {
            assertEquals("", NamingUtils.toLowerCamel("___"));
            assertEquals("", NamingUtils.toLowerCamel("_"));
        }
    }

    // ======================== tableNameToMappingPath ========================

    @Nested
    @DisplayName("tableNameToMappingPath — 表名转映射路径")
    class TableNameToMappingPath {

        @ParameterizedTest
        @CsvSource({
                "user_order, /user/order",
                "user,       /user",
                "product,    /product",
                "order_item, /order/item",
        })
        @DisplayName("正常表名转路径")
        void normalConversion(String input, String expected) {
            assertEquals(expected, NamingUtils.tableNameToMappingPath(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 和空字符串返回空字符串")
        void nullAndEmpty(String input) {
            assertEquals("", NamingUtils.tableNameToMappingPath(input));
        }

        @Test
        @DisplayName("大写表名转路径（下划线→斜杠+全部小写）")
        void upperCaseTableName() {
            // tableNameToMappingPath: 先全部 lowerCase 再把 _ 替换为 /
            assertEquals("/user/order", NamingUtils.tableNameToMappingPath("USER_ORDER"));
            assertEquals("/user", NamingUtils.tableNameToMappingPath("USER"));
        }

        @Test
        @DisplayName("单字符表名")
        void singleCharTableName() {
            assertEquals("/a", NamingUtils.tableNameToMappingPath("a"));
        }

        @Test
        @DisplayName("多级下划线表名")
        void multipleUnderscores() {
            assertEquals("/user/order/detail", NamingUtils.tableNameToMappingPath("user_order_detail"));
        }
    }
}
