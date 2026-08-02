<#-- ======================= Spring-Forge application.yml 模板 ======================= -->

spring:
<#if hasJdbc>
  datasource:
    url: ${jdbcUrl}
    username: ${jdbcUsername!"root"}
    password: ${jdbcPassword!"root"}
    driver-class-name: com.mysql.cj.jdbc.Driver
<#else>
  # ↓ 默认使用 H2 内嵌数据库，零配置即可启动
  # ↓ 切换 MySQL 时，把下面注释掉，改为上面的 MySQL 配置
  datasource:
    url: jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  sql:
    init:
      mode: always
</#if>
  jackson:
    date-format: ${dateFormat!"yyyy-MM-dd HH:mm:ss"}
    time-zone: GMT+8

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ${idType!"ASSIGN_ID"}
<#if logicDeleteField?? && logicDeleteField != "">
      logic-delete-field: ${logicDeleteField}
      logic-delete-value: ${logicDeleteValue!"1"}
      logic-not-delete-value: ${logicNotDeleteValue!"0"}
</#if>
<#if tablePrefix?? && tablePrefix != "">
      table-prefix: ${tablePrefix}
</#if>
  mapper-locations: classpath*:/mapper/**/*.xml

server:
  port: 8080
