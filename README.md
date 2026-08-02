# Spring-Forge

> 定义一张表 → 还你一个项目。不连数据库，一个 JSON 就搞定。

---

## 快速开始

### ① 加依赖

在新 Spring Boot 项目的 `pom.xml` 中加入：

```xml
<dependency>
    <groupId>io.github.liwanqi42</groupId>
    <artifactId>spring-forge</artifactId>
    <version>2.0.2</version>
</dependency>
```

spring-forge 会**自动传递**所有必需的运行时依赖：Spring Web、MyBatis-Plus、H2 数据库、参数校验。你不需要额外添加任何东西。

### ② 写一个 JSON

在项目根目录创建 `spring-forge-config.json`：

```jsonc
{
  "project": {
    "name": "my-project",
    "groupId": "com.example",
    "basePackage": "com.example.demo",
    "mode": "MVC",
    "outputDir": ".",
    "author": "CodeGenerator"
  },
  "tables": [
    {
      "name": "user",
      "comment": "用户表",
      "columns": [
        { "name": "id", "type": "BIGINT", "comment": "主键ID", "primaryKey": true, "autoIncrement": true },
        { "name": "username", "type": "VARCHAR", "length": 50, "comment": "用户名", "nullable": false },
        { "name": "password", "type": "VARCHAR", "length": 255, "comment": "密码", "nullable": false },
        { "name": "email", "type": "VARCHAR", "length": 100, "comment": "邮箱", "nullable": true },
        { "name": "phone", "type": "VARCHAR", "length": 20, "comment": "手机号", "nullable": true },
        { "name": "status", "type": "TINYINT", "comment": "状态：1启用 0禁用", "defaultValue": "1" },
        { "name": "create_time", "type": "DATETIME", "comment": "创建时间" },
        { "name": "update_time", "type": "DATETIME", "comment": "更新时间" }
      ]
    }
  ],
  "options": {
    "generateDdl": true,
    "logicDeleteField": "deleted",
    "optimisticLockField": "version",
    "idType": "ASSIGN_ID",
    "tablePrefix": "",
    "useLombok": true,
    "minimalMyBatisConfig": true
  }
}

```
> 项目自带 [`spring-forge-config.json`](spring-forge-config.json) 模板，改一下包名和表结构就能用。

### ③ 启动（两次）

```bash
# 第一次：生成代码（看到 "生成完成" 后 Ctrl+C 停掉）
mvn clean spring-boot:run

# 第二次：真正启动（代码已生成，这次会编译并运行）
mvn clean spring-boot:run
```

**完了。** 打开浏览器访问 `http://localhost:8080/user/list` 就能看到接口。

> **为什么需要两次？** 代码生成发生在 Spring Boot 启动之后，`.java` 文件来不及被当次 Maven 编译。第二次启动时 Maven 会编译所有生成的代码，应用正常运行。后续每次只需要一次 `mvn spring-boot:run`。

---


## 常用选项

### project 节点

```jsonc
"project": {
  "name": "my-project",
  "groupId": "com.example",
  "basePackage": "com.example.demo",  // 必填
  "mode": "MVC",                      // MVC | DDD
  "outputDir": ".",
  "author": "CodeGenerator",
  "overwrite": false                  // 是否覆盖已存在的业务代码
}
```

### options 节点

```jsonc
"options": {
  "generateDdl": true,              // 生成 schema.sql，启动时自动建表（建议开启）
  "logicDeleteField": "deleted",    // 逻辑删除字段（空字符串 = 禁用）
  "optimisticLockField": "version", // 乐观锁字段（空字符串 = 禁用）
  "idType": "ASSIGN_ID",           // 主键策略：AUTO / ASSIGN_ID（雪花）
  "tablePrefix": "t_",              // 表前缀（生成实体名时自动去除）
  "useLombok": true,               // 是否用 Lombok 替代 getter/setter
  "minimalMyBatisConfig": true      // 精简 MyBatis 配置（信任 Spring Boot 自动配置）
}
```

### columns 节点

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 列名（必填） |
| `type` | string | JDBC 类型：VARCHAR / BIGINT / INT / TINYINT / DATETIME 等 |
| `comment` | string | 列注释 |
| `primaryKey` | bool | 是否主键 |
| `autoIncrement` | bool | 是否自增 |
| `nullable` | bool | 是否可为空（默认 false） |
| `length` | number | 列长度 |
| `defaultValue` | string | 默认值 |
| `javaType` | string | 自定义 Java 类型（如 `java.math.BigDecimal`），不填则自动映射 |

---

## 进阶：直连 MySQL 数据库

如果你已经有 MySQL 数据库，Spring-Forge 可以直连读取表结构——连字段都不用写。

在 JSON 中加入 `database` 节点，**并在 pom.xml 中添加 MySQL 驱动**：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

```jsonc
{
  "project": { "name": "...", "groupId": "...", "basePackage": "..." },
  "database": {
    "jdbcUrl": "jdbc:mysql://localhost:3306/你的库名?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
    "username": "root",
    "password": "你的密码"
  }
}
```

也可以只读指定表：

```jsonc
{
  "tables": ["user", "order"]   // 字符串数组 = 只读取这两张表
}
```

---


## Controller 接口

```
POST   /user                      新增
PUT    /user                      修改
DELETE /user/{id}                 删除
GET    /user/{id}                 查详情
GET    /user/list?status=1        条件列表
GET    /user/page?pageNum=1&pageSize=10   分页
```

返回格式：

```json
// 成功
{"code":200, "message":"操作成功", "data":{...}, "success":true}

// 分页
{"total":100, "pages":10, "current":1, "size":10, "records":[...]}

// 失败
{"code":400, "message":"用户名不能为空", "data":null, "success":false}
```

---

## 重复执行

- **全局基础类**：只生成一次，删掉 `.spring-forge.lock` 可强制重新生成
- **业务代码**：默认不覆盖已有文件，设置 `"overwrite": true` 可强制覆盖
- **application.yml / schema.sql**：同上，默认不覆盖
- **新增表后**重新启动，增量生成新表代码

---

## 热修复（已有项目）

如果想在已生成代码的项目中继续使用 spring-forge（不删除依赖），不会影响正常运行——spring-forge 检测到 `.spring-forge.lock` 后会自动退出代码生成模式，应用正常启动。

---

## MyBatis 配置模式

`minimalMyBatisConfig` 控制生成的 `MyBatisPlusConfig` 风格：

| 模式 | 值 | DataSource | SqlSessionFactory | 适用场景 |
|------|-----|-----------|-------------------|---------|
| **精简**（默认） | `true` | Spring Boot 自动配置 | Spring Boot 自动配置 | 标准项目，通过 `application.yml` 调参 |
| **完整** | `false` | 手动 HikariCP | 手动 MybatisSqlSessionFactoryBean | 非标环境，需完全掌控 Bean 创建 |

精简模式下生成的配置文件仅保留 `@MapperScan` + `MybatisPlusInterceptor`（含分页插件），其余全部由 `mybatis-plus-spring-boot3-starter` 自动配置接管。

---

## 项目结构

```
templates/                    ← FreeMarker 模板（生成代码的蓝图）
├── global/                   ← 全局基础类模板（Result、BizException 等）
├── shared/                   ← MVC/DDD 共享模板（Entity、Mapper、Controller）
├── mvc/                      ← MVC 专属模板（DTO/VO/Converter）
├── ddd/core/                 ← DDD 专属模板（DomainService）
├── common/                   ← 脚手架模板（pom.xml、application.yml、DDL）
└── macros.ftl                ← 共享 FreeMarker 宏
```

修改生成代码的行为：直接编辑对应 `.ftl` 模板，无需改动 Java 代码。

---

## 常见问题

**Q: 启动报 "Failed to configure a DataSource"？**

A: 确认 `spring-forge-config.json` 中 options 有 `"generateDdl": true`。

**Q: 第二次启动编译报错？**

A: 确保用的是最新版 spring-forge。如果升级了 Spring Boot 大版本，删掉 `.spring-forge.lock` 和 `src/main/java/.../common/` 重新生成。

**Q: 如何从 H2 切换到 MySQL？**

A: 在 pom.xml 中加入 `mysql-connector-j`，修改 `application.yml` 中的 `spring.datasource.*` 配置，移除 H2 依赖即可。
