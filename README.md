# 鲜果记

鲜果记是一个面向精品水果零售场景的前后端分离电商系统，覆盖用户端小程序/H5、商家端后台管理和 Spring Boot 后端服务。项目支持商品浏览、分类检索、购物车、下单支付流程、地址/自提点、优惠券、拼团、评价、消息、商家商品与订单管理、统计报表等核心业务。

## 项目概览

| 项目 | 说明 |
|---|---|
| 项目名称 | 鲜果记 |
| 业务定位 | 单商户精品生鲜/水果电商系统 |
| 用户端 | uni-app + Vue 3，支持微信小程序与 H5 |
| 商家端 | Vite + Vue 3 + Element Plus 的 PC 管理后台 |
| 后端服务 | Spring Boot 3.2 单体应用，提供 REST API |
| 数据库 | MySQL，库名 `xianguoji` |
| 缓存/限流/锁 | Redis + Redisson |
| 接口文档 | Knife4j / OpenAPI |
| 部署方式 | 本地启动、Docker Compose、Linux 一键部署脚本 |

## 系统架构

```text
┌─────────────────────┐       ┌───────────────────────────┐       ┌──────────────┐
│ user-front          │       │ xianguoji-server          │       │ MySQL        │
│ uni-app / H5 / 小程序│ ───► │ Spring Boot :8080         │ ───► │ xianguoji    │
└─────────────────────┘       │                           │       └──────────────┘
                              │ /api/pub/**    公共接口    │       ┌──────────────┐
┌─────────────────────┐       │ /api/u/**      用户端接口  │ ───► │ Redis        │
│ merchant-front      │ ───► │ /api/admin/**  商家端接口  │       └──────────────┘
│ Vite + Vue3         │       │ /static/**     静态资源    │
└─────────────────────┘       └───────────────────────────┘
```

## 仓库目录结构

```text
.
├── xianguoji-server/              # 后端服务：Spring Boot + MyBatis-Plus
│   ├── src/main/java/com/xianguoji/server/
│   │   ├── common/                # 通用基础设施：结果封装、异常、安全、配置、切面等
│   │   └── module/                # 业务模块
│   ├── src/main/resources/        # application.yml、mapper XML、日志配置等
│   ├── Dockerfile                 # 后端 Docker 镜像文件
│   └── pom.xml                    # Maven 项目配置
├── user-front/                    # 用户端：uni-app 微信小程序/H5
│   ├── src/pages/                 # 主包页面：首页、分类、购物车、订单、我的
│   ├── src/pagesA/                # 商品详情、搜索等子包页面
│   ├── src/pagesB/                # 结算、订单详情、支付结果、评价等子包页面
│   ├── src/pagesC/                # 地址、拼团、消息等子包页面
│   ├── src/components/            # 通用组件
│   └── package.json
├── merchant-front/                # 商家端：Vite + Vue3 管理后台
│   ├── src/                       # 页面、路由、接口、状态管理等
│   └── package.json
├── database/                      # 数据库脚本
│   ├── schema.sql                 # 数据库初始化结构与基础数据
│   └── migrations/                # 数据库迁移脚本
├── docs/                          # 项目文档
│   ├── backend-spec.md            # 后端开发规约与接口约定
│   ├── frontend-integration.md    # 前后端联调规约
│   ├── ops-guide.md               # 运维部署指南
│   ├── TEST_PLAN.md               # 测试计划
│   └── TEST_PLAN_DETAIL.md        # 详细测试计划
├── scripts/                       # 安全、性能测试等脚本
├── docker-compose.yml             # Docker Compose 编排文件
├── deploy.sh                      # Ubuntu 服务器一键部署脚本
└── README.md                      # 当前文档
```

## 核心功能

### 用户端功能

- **账号与鉴权**：微信小程序登录、Token 登录态维护。
- **首页与分类**：Banner、分类导航、热门商品、分类商品列表。
- **商品详情**：商品信息、规格 SKU、库存、价格、评价、拼团入口。
- **购物车**：加入购物车、数量调整、勾选结算、库存校验。
- **订单流程**：确认订单、优惠券选择、配送/自提、提交订单、支付结果、订单详情。
- **地址与自提点**：收货地址管理、自提点选择、定位相关能力。
- **营销活动**：优惠券、满减、拼团活动。
- **个人中心**：订单入口、收藏、足迹、评价、消息、反馈等。

### 商家端功能

- **后台登录与权限**：商家端独立 Token 和后台接口路径。
- **商品管理**：商品新增/编辑、SKU、图片、上下架、分类维护。
- **订单管理**：订单列表、订单详情、状态流转、退款/售后相关处理。
- **营销管理**：优惠券、满减、拼团活动配置。
- **店铺配置**：店铺信息、自提点、配送设置、通知设置。
- **客户与评价**：用户信息、评价管理、消息反馈。
- **数据统计**：工作台统计、销售与运营数据展示。

### 后端模块

后端业务模块位于 `xianguoji-server/src/main/java/com/xianguoji/server/module/`：

| 模块 | 说明 |
|---|---|
| `auth` | 用户端/商家端鉴权、验证码、微信登录 |
| `user` | 用户、地址、足迹、收藏等 |
| `catalog` | 分类、商品、SKU、图片、Banner、搜索 |
| `cart` | 购物车 |
| `order` | 订单、订单项、状态流转、退款相关能力 |
| `promo` | 优惠券、满减、拼团 |
| `review` | 商品评价 |
| `shop` | 店铺信息、自提点、配送和通知设置 |
| `message` | 消息中心、用户反馈 |
| `staff` | 商家端员工管理 |
| `stat` | 统计数据 |
| `chat` | 聊天/客服相关能力 |
| `help` | 帮助中心 |
| `report` | 报表相关能力 |

## 技术栈

### 后端 `xianguoji-server`

| 技术 | 版本/说明 |
|---|---|
| JDK | 17 |
| Spring Boot | 3.2.5 |
| MyBatis-Plus | 3.5.5 |
| MySQL Driver | MySQL Connector/J |
| Druid | 1.2.22，连接池与监控 |
| Redis | Spring Data Redis + Lettuce |
| Redisson | 3.27.2，分布式锁 |
| Caffeine | 本地二级缓存 |
| JWT | jjwt 0.12.5 |
| Knife4j | 4.5.0，接口文档 |
| EasyExcel | 4.0.2，导入导出 |
| OkHttp | 4.12.0，第三方 HTTP 调用 |
| Aliyun OSS | 3.17.4，文件上传存储 |
| Hutool | 5.8.27 |
| Lombok | 简化 Java 样板代码 |
| Testcontainers | 集成测试支持 |

### 用户端 `user-front`

| 技术 | 说明 |
|---|---|
| 框架 | uni-app + Vue 3 |
| 语言 | TypeScript |
| 构建工具 | Vite |
| 状态管理 | Pinia |
| UI 组件 | wot-design-uni |
| 样式 | SCSS + 全局设计 Token |
| 目标平台 | 微信小程序、H5 |
| 微信 AppID | `wxfbb4085f2c6b2992` |

### 商家端 `merchant-front`

| 技术 | 说明 |
|---|---|
| 框架 | Vue 3 |
| 语言 | TypeScript |
| 构建工具 | Vite 7 |
| UI 组件 | Element Plus |
| 状态管理 | Pinia |
| 样式 | Tailwind CSS 4 + PostCSS |
| HTTP 客户端 | Axios |
| 测试 | Vitest + Vue Test Utils + happy-dom |

## 环境要求

本地开发建议安装以下环境：

- **JDK**：17+
- **Maven**：3.8+
- **Node.js**：建议 18+；商家端依赖较新，建议 20+
- **npm**：项目包含 `package-lock.json`，推荐使用 `npm install`
- **MySQL**：8.x/9.x 均可，数据库名 `xianguoji`
- **Redis**：7.x 推荐
- **微信开发者工具**：运行和预览用户端微信小程序时需要
- **Docker / Docker Compose**：使用容器化部署时需要

## 环境变量配置

后端通过 `.env` 加载敏感配置，模板位于：

```text
xianguoji-server/.env.example
```

本地后端启动前，在 `xianguoji-server/` 下创建 `.env`：

```bash
cp xianguoji-server/.env.example xianguoji-server/.env
```

Windows PowerShell 可使用：

```powershell
Copy-Item xianguoji-server/.env.example xianguoji-server/.env
```

常用变量说明：

| 变量 | 说明 |
|---|---|
| `MYSQL_USER` | MySQL 用户名，默认可为 `root` |
| `MYSQL_PASSWORD` | MySQL 密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `DRUID_USER` | Druid 监控登录用户名 |
| `DRUID_PASSWORD` | Druid 监控登录密码 |
| `JWT_SECRET` / `JWT_SECRET_V1` | JWT 密钥，建议至少 32 字节 |
| `WECHAT_APPID` | 微信小程序 AppID |
| `WECHAT_SECRET` | 微信小程序 Secret |
| `ALIYUN_OSS_ENDPOINT` | 阿里云 OSS Endpoint |
| `ALIYUN_OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey ID |
| `ALIYUN_OSS_ACCESS_KEY_SECRET` | 阿里云 OSS AccessKey Secret |
| `ALIYUN_OSS_BUCKET` | 阿里云 OSS Bucket |

> 注意：`.env` 已被 `.gitignore` 排除，不要提交真实密钥、数据库密码、微信密钥或 OSS 密钥。

## 本地启动

### 1. 初始化数据库

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS xianguoji
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

导入初始化脚本：

```bash
mysql -uroot -p xianguoji < database/schema.sql
```

Windows PowerShell 如遇到重定向兼容问题，可使用：

```powershell
cmd /c "mysql -uroot -p xianguoji < database\schema.sql"
```

### 2. 启动 Redis

确保 Redis 已启动，并且密码与 `.env` 中的 `REDIS_PASSWORD` 一致。开发环境也支持空密码，对应 `application-dev.yml` 中的默认配置。

### 3. 启动后端服务

```bash
cd xianguoji-server
mvn spring-boot:run
```

启动成功后：

- **后端服务**：`http://127.0.0.1:8080`
- **Knife4j 文档**：`http://127.0.0.1:8080/doc.html`
- **OpenAPI JSON**：`http://127.0.0.1:8080/v3/api-docs`
- **静态资源**：`http://127.0.0.1:8080/static/**`

### 4. 启动用户端

```bash
cd user-front
npm install
npm run dev:h5
```

运行微信小程序：

```bash
npm run dev:mp-weixin
```

微信小程序构建产物通常位于：

```text
user-front/dist/dev/mp-weixin
```

使用微信开发者工具导入该目录即可预览。

### 5. 启动商家端

```bash
cd merchant-front
npm install
npm run dev
```

商家端开发服务端口为：

```text
http://127.0.0.1:5174
```

## 常用命令

### 后端命令

```bash
cd xianguoji-server

# 启动开发环境
mvn spring-boot:run

# 运行测试
mvn test

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 用户端命令

```bash
cd user-front

# 安装依赖
npm install

# H5 开发
npm run dev:h5

# H5 构建
npm run build:h5

# 微信小程序开发
npm run dev:mp-weixin

# 微信小程序构建
npm run build:mp-weixin

# 单元测试
npm run test

# 样式检查
npm run lint:css
```

### 商家端命令

```bash
cd merchant-front

# 安装依赖
npm install

# 开发启动
npm run dev

# 生产构建
npm run build

# 本地预览构建产物
npm run preview

# 单元测试
npm run test

# 样式检查
npm run lint:css
```

## 接口约定

后端接口按调用方划分路径：

| 路径前缀 | 说明 | 鉴权 |
|---|---|---|
| `/api/pub/**` | 公共接口，如公开商品、分类、Banner 等 | 不一定需要登录 |
| `/api/u/**` | 用户端接口，如购物车、下单、地址、用户中心等 | 需要用户 Token |
| `/api/admin/**` | 商家端后台接口，如商品、订单、营销、统计管理等 | 需要商家端 Token |
| `/static/**` | 上传文件和静态资源访问 | 公开访问 |

统一响应格式：

```json
{
  "code": 0,
  "msg": "ok",
  "data": {},
  "ts": 1714723200000
}
```

常见错误码：

| code | 含义 |
|---|---|
| `0` | 成功 |
| `4000` | 参数错误 |
| `4001` | 业务校验失败 |
| `4010` | 未登录或 Token 失效 |
| `4011` | Token 已过期 |
| `4030` | 无权限 |
| `4040` | 资源不存在 |
| `4090` | 资源冲突 |
| `4290` | 接口限流 |
| `5000` | 服务器内部错误 |
| `5001` | 第三方依赖异常 |
| `6001` | 库存不足 |
| `6002` | 订单状态非法流转 |
| `6003` | 优惠券不可用 |
| `6004` | 拼团已满或已结束 |
| `6005` | 商品已下架 |

更多接口与联调规范见：

- `docs/backend-spec.md`
- `docs/frontend-integration.md`
- 后端启动后的 `http://127.0.0.1:8080/doc.html`

## 数据库说明

数据库脚本位于 `database/`：

```text
database/
├── schema.sql                 # 初始化结构与基础数据
├── migrations/                # 增量迁移脚本
└── download-seed-images.ps1   # 种子图片下载脚本
```

后端 ORM 使用 MyBatis-Plus，Mapper XML 位于：

```text
xianguoji-server/src/main/resources/mapper/
```

重要约定：

- 数据库字符集统一使用 `utf8mb4`。
- 数据库名为 `xianguoji`。
- 表结构以 `database/schema.sql` 和 `database/migrations/` 为准。
- 修改表结构后，应同步更新数据库脚本，避免本地、部署和 Docker 初始化环境不一致。

## Docker Compose 部署

根目录提供 `docker-compose.yml`，包含：

- `app`：后端应用，端口 `8080`
- `mysql`：MySQL，初始化执行 `database/schema.sql`
- `redis`：Redis 7 Alpine，开启 AOF 持久化
- `mysql_data`、`redis_data`、`upload_data`、`app_logs` 数据卷

使用前需要在根目录准备 `.env`，至少包含：

```env
MYSQL_PASSWORD=your_mysql_password
REDIS_PASSWORD=your_redis_password
DRUID_USER=druid
DRUID_PASSWORD=your_druid_password
JWT_SECRET=your_jwt_secret_at_least_32_bytes
WECHAT_APPID=your_wechat_appid
WECHAT_SECRET=your_wechat_secret
```

后端镜像依赖已打包的 JAR：

```bash
cd xianguoji-server
mvn clean package -DskipTests
```

然后在项目根目录启动：

```bash
docker compose up -d --build
```

查看服务状态：

```bash
docker compose ps
```

查看后端日志：

```bash
docker compose logs -f app
```

停止服务：

```bash
docker compose down
```

如需同时删除数据卷，请谨慎执行：

```bash
docker compose down -v
```

## 服务器部署

根目录提供 `deploy.sh`，用于 Ubuntu 24.04 服务器部署/更新，脚本中默认：

| 配置 | 默认值 |
|---|---|
| 应用目录 | `/opt/xianguoji` |
| 后端端口 | `8080` |
| 域名 | `oujincong.xyz` |
| API 域名 | `api.oujincong.xyz` |
| 用户端域名 | `m.oujincong.xyz` |

执行前请确认：

- 已准备好服务器基础环境或允许脚本安装依赖。
- 域名已解析到服务器。
- `.env` 中的数据库、Redis、JWT、微信、OSS 等配置完整。
- 生产环境不要提交或泄露任何密钥。

更多运维配置见：

```text
docs/ops-guide.md
```

## 测试与质量保障

项目包含测试计划与脚本：

| 位置 | 说明 |
|---|---|
| `docs/TEST_PLAN.md` | 测试计划 |
| `docs/TEST_PLAN_DETAIL.md` | 详细测试用例与执行说明 |
| `scripts/perf/perf-test.js` | 性能测试脚本 |
| `scripts/security-pentest.sh` | 安全测试脚本 |
| `scripts/nuclei-templates/` | Nuclei 安全扫描模板 |

常用测试命令：

```bash
# 后端测试
cd xianguoji-server
mvn test

# 用户端测试
cd user-front
npm run test

# 商家端测试
cd merchant-front
npm run test
```

## 开发规范

### 后端规范

- Controller 只负责参数接收、校验和响应，不写复杂业务逻辑。
- 业务逻辑放在 Service 层，数据库访问通过 Mapper / MyBatis-Plus 完成。
- 接口统一返回响应壳，不直接返回裸对象。
- 用户端和商家端鉴权路径分离：`/api/u/**` 与 `/api/admin/**`。
- 参数校验使用 Jakarta Validation。
- 业务异常使用统一异常处理机制。
- 缓存、限流、幂等、分布式锁等基础能力优先复用 `common/` 下已有实现。

### 前端规范

- 接口路径、入参、出参以后端 Controller 和 Knife4j 为准。
- 不自创接口；缺接口时先记录联调缺口，再补后端。
- 用户端 Token 存储在 `uni.storage`。
- 商家端 Token 存储在 `localStorage`。
- `4010` / `4011` 需要统一清理登录态并跳转登录页。
- 页面样式优先复用已有变量、混入和组件，保持视觉一致性。

## 常见问题

### 1. 后端启动时报数据库连接失败

请检查：

- MySQL 是否启动。
- 是否已创建 `xianguoji` 数据库。
- 是否已导入 `database/schema.sql`。
- `.env` 中 `MYSQL_USER`、`MYSQL_PASSWORD` 是否正确。
- `application-dev.yml` 中数据库地址是否与本地一致。

### 2. 后端启动时报 Redis 连接失败

请检查：

- Redis 是否启动。
- Redis 端口是否为 `6379`。
- `.env` 中 `REDIS_PASSWORD` 是否与 Redis 实际密码一致。
- 开发环境如 Redis 无密码，可确认 `application-dev.yml` 是否允许空密码。

### 3. Knife4j 访问地址是什么

后端启动后访问：

```text
http://127.0.0.1:8080/doc.html
```

### 4. 商家端端口为什么是 5174

用户端 H5 常用 Vite 默认端口 `5173`，商家端在 `merchant-front/vite.config.ts` 中配置为 `5174`，避免两个前端项目同时启动时端口冲突。

### 5. 微信小程序如何预览

执行：

```bash
cd user-front
npm run dev:mp-weixin
```

然后使用微信开发者工具导入：

```text
user-front/dist/dev/mp-weixin
```

### 6. 文件上传访问路径是什么

开发环境默认上传目录配置在后端 `application.yml`：

```yaml
xianguoji:
  upload:
    base-dir: D:/xianguoji/upload
    domain: http://127.0.0.1:8080/static
```

也就是说，上传文件可通过 `/static/**` 路径访问。生产环境可结合 Nginx 或 OSS 配置访问域名。

## 相关文档

- `xianguoji-server/README.md`：后端简要说明
- `user-front/README.md`：用户端说明
- `merchant-front/README.md`：商家端说明
- `docs/backend-spec.md`：后端开发规约
- `docs/frontend-integration.md`：前后端联调规约
- `docs/ops-guide.md`：部署与运维说明
- `docs/TEST_PLAN.md`：测试计划
- `docs/TEST_PLAN_DETAIL.md`：详细测试计划
- `docs/OPTIMIZATION_PLAN.md`：优化计划
- `docs/PRODUCTION_OPTIMIZATION_PLAN.md`：生产优化计划

## 安全提醒

- 不要提交 `.env`、密钥、数据库密码、微信密钥、OSS 密钥。
- 生产环境必须使用强 JWT 密钥。
- 生产环境建议启用 HTTPS、Nginx 限流、安全响应头和日志监控。
- 上传文件应限制大小、类型并避免可执行脚本上传。
- 管理后台接口必须走 `/api/admin/**` 并校验商家端 Token。

## License

本项目为课程/实训/内部开发项目。如需开源或商用，请先补充明确的 License 文件和版权说明。
