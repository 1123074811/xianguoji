# xianguoji-server

鲜果记 单商户精品生鲜小程序后端

## 技术栈

- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- MySQL 9.x
- Redis
- JWT 双端鉴权
- Knife4j (OpenAPI 文档)

## 启动

```bash
# 1. 先执行 database/schema.sql 初始化数据库

# 2. 启动 Redis

# 3. 启动应用
mvn spring-boot:run
```

## 接口文档

启动后访问: http://localhost:8080/doc.html

## 配置

- `application.yml` - 主配置
- `application-dev.yml` - 开发环境
- `application-prod.yml` - 生产环境

## 项目结构

按业务模块划分，每个模块包含 controller/service/mapper/entity/dto/vo 六个子包。
