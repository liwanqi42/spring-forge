<#import "../macros.ftl" as m>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperPackage}.${entityName}${mapperClassSuffix}">

    <!-- 通用结果映射 -->
    <resultMap id="BaseResultMap" type="${entityPackage}.${entityName}">
<@m.xmlResultMap columns />
    </resultMap>

    <!-- 通用查询列 -->
    <sql id="BaseColumnList">
<@m.xmlBaseColumnList columns />
    </sql>

    <!-- 条件查询片段 -->
    <sql id="QueryCondition">
<@m.xmlQueryCondition queryColumns />
    </sql>

    <!-- 分页条件查询 -->
    <select id="selectPageWithCondition" resultMap="BaseResultMap">
        SELECT <include refid="BaseColumnList"/>
        FROM `${tableName}`
        <include refid="QueryCondition"/>
        ORDER BY ${pkColumn.rawName} DESC
    </select>

    <!-- 根据主键查询 -->
    <select id="selectById" resultMap="BaseResultMap">
        SELECT <include refid="BaseColumnList"/>
        FROM `${tableName}`
        WHERE ${pkColumn.rawName} = ${"#"}{id}
    </select>

    <!-- 新增 -->
    <insert id="insert" parameterType="${entityPackage}.${entityName}">
        INSERT INTO `${tableName}` (
<@m.xmlInsertColumns insertColumns />
        ) VALUES (
<@m.xmlInsertValues insertColumns />
        )
    </insert>

    <!-- 更新 -->
    <update id="update" parameterType="${entityPackage}.${entityName}">
        UPDATE `${tableName}`
        <set>
<@m.xmlUpdateSet updateColumns />
        </set>
        WHERE ${pkColumn.rawName} = ${"#"}{${pkColumn.fieldName}}
    </update>

    <!-- 根据主键删除（物理删除） -->
    <delete id="deleteById">
        DELETE FROM `${tableName}` WHERE ${pkColumn.rawName} = ${"#"}{id}
    </delete>
</mapper>
