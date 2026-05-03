# 鲜果记 后端开发文档（Spring Boot）

> 面向 AI 编程工具的可执行规约。每一节给出**强制约定**与**可量化产物**，AI 应严格按文档生成代码、命名、目录结构与接口契约，不得自由发挥。

---

## 0. 阅读对象与执行原则

- 读者：AI 编程助手 / 后端开发同学
- 执行原则：
  1. **以本文为唯一事实源**；与个人经验冲突时以本文为准
  2. **禁止臆造表 / 字段 / 接口**——所有数据库结构以 `database/schema.sql` 为准；如需扩展，先在 `database/migrations/` 追加 SQL，再改代码
  3. **接口契约不可被偷换**：路径、方法、入参、出参、错误码必须与文档一致
  4. **TODO 必须显式留**：无法实现/外部依赖未到位时，方法体抛 `UnsupportedOperationException("TODO: ...")` 并打 `@Deprecated` 注释，不得静默返回假数据
  5. **代码生成必须能编译并通过启动**——每完成一个模块都要保证 `mvn spring-boot:run` 不报错

---

## 1. 项目概述

| 项 | 值 |
|---|---|
| 项目名 | xianguoji-server |
| 业务定位 | 单商户精品生鲜小程序后端 |
| 服务对象 | ① 用户端小程序 / H5 ② 商家端 PC 后台 ③ 商家端移动简版 |
| 部署形态 | 单体 Spring Boot 应用，前后端分离 |
| 数据库 | MySQL 9.x，库名 `xianguoji`（已建好） |
| 鉴权 | JWT（双端独立 Token），用户端 OpenAPI、商家端 AdminAPI 路径分离 |
| 字符集 | utf8mb4 全栈统一 |

---

## 2. 技术栈（强制版本）

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 启用 `--enable-preview` 关闭 |
| Spring Boot | 3.2.5 | parent 锁版本 |
| Spring Web | with Spring Boot | REST |
| Spring Validation | with Spring Boot | Jakarta Validation |
| MyBatis-Plus | 3.5.5 | ORM；优先使用 Mapper + Service，禁止裸写 JDBC |
| MySQL Driver | 8.4.x | 兼容 MySQL 9 |
| Druid | 1.2.22 | 连接池 + SQL 监控 |
| Redis (Lettuce) | with Spring Boot | 缓存、验证码、限流、分布式锁 |
| Hutool | 5.8.27 | 工具类（DateUtil / IdUtil 等） |
| MapStruct | 1.5.5 | DTO ↔ Entity 转换 |
| Lombok | 1.18.32 | `@Data` `@Builder` `@Slf4j` |
| jjwt | 0.12.5 | JWT |
| knife4j | 4.5.0 | OpenAPI 文档（基于 SpringDoc） |
| Hibernate Validator | with Spring Boot | 校验 |
| Spring AOP | with Spring Boot | 日志 / 限流切面 |
| EasyExcel | 4.0.2 | 订单 / 商品导出 |
| OkHttp | 4.12.0 | 第三方 HTTP（微信、地图） |
| Spring Boot Test + Mockito | with Spring Boot | 测试 |

> 不允许引入未列出的中间件（如 RocketMQ、ES）；如确有需要，先在文档中追加并说明替换方案。

---

## 3. 工程结构（强制目录）

```
xianguoji-server/
├── pom.xml
├── README.md
├── docs/                                   # 文档（本文件、API、错误码表）
├── database/                               # SQL 与迁移
│   ├── schema.sql
│   └── migrations/
└── src/
    ├── main/
    │   ├── java/com/xianguoji/server/
    │   │   ├── XianguojiApplication.java   # 启动类
    │   │   ├── common/                     # 通用基础设施
    │   │   │   ├── result/                 # R / PageR / ResultCode
    │   │   │   ├── exception/              # BizException / GlobalExceptionHandler
    │   │   │   ├── security/               # JWT 工具、拦截器、上下文
    │   │   │   ├── annotation/             # @LoginRequired / @AdminRequired / @RateLimit
    │   │   │   ├── aspect/                 # 日志 / 限流切面
    │   │   │   ├── config/                 # MyBatisPlusConfig / WebMvcConfig / SwaggerConfig / RedisConfig
    │   │   │   ├── constant/               # 常量、枚举
    │   │   │   ├── enums/                  # OrderStatus / DeliveryType ...
    │   │   │   └── util/                   # SnowflakeId / OrderNoUtil / SmsUtil
    │   │   └── module/                     # 业务模块（一模块一包）
    │   │       ├── auth/                   # 鉴权 + 验证码 + 微信登录
    │   │       ├── user/                   # 用户、地址、足迹、收藏
    │   │       ├── shop/                   # 店铺信息、自提点、配送设置、通知设置
    │   │       ├── catalog/                # 分类、商品、SKU、图片、Banner、热门搜索
    │   │       ├── cart/                   # 购物车
    │   │       ├── promo/                  # 优惠券、满减、拼团
    │   │       ├── order/                  # 订单、订单项、状态流转、退款
    │   │       ├── review/                 # 评价
    │   │       ├── message/                # 消息中心、用户反馈
    │   │       ├── staff/                  # 员工管理（商家端）
    │   │       └── stat/                   # 工作台统计、客户管理
    │   └── resources/
    │       ├── application.yml             # 主配置
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── mapper/                     # 各模块 *Mapper.xml
    │       └── logback-spring.xml
    └── test/java/com/xianguoji/server/...
```

**包内每个模块一律 6 子包**：
```
auth/
├── controller/    # 接口入口；只做参数校验、调用 service、组装响应
├── service/       # 业务逻辑
├── mapper/        # MyBatis-Plus Mapper 接口
├── entity/        # 数据库实体（@TableName）
├── dto/           # 请求 DTO（XxxQry / XxxAddDto / XxxUpdDto）
└── vo/            # 响应 VO（XxxVO / XxxDetailVO）
```

> Controller 不直接接收 / 返回 Entity；DTO 与 VO 由 MapStruct 转换。

---

## 4. 通用约定

### 4.1 统一响应

```java
public record R<T>(int code, String msg, T data, long ts) {
    public static <T> R<T> ok(T data)            { return new R<>(0, "ok", data, System.currentTimeMillis()); }
    public static <T> R<T> ok()                  { return ok(null); }
    public static <T> R<T> fail(int c, String m) { return new R<>(c, m, null, System.currentTimeMillis()); }
}
```

```json
{ "code": 0, "msg": "ok", "data": {...}, "ts": 1714723200000 }
```

### 4.2 分页

```java
public record PageQry(@Min(1) Integer page, @Min(1) @Max(100) Integer size) {}
public record PageVO<T>(long total, List<T> list, int page, int size) {}
```

`page` 默认 1，`size` 默认 20；商家端列表最大 100。

### 4.3 错误码（与 `common/result/ResultCode.java` 一一对应）

| code | 含义 | HTTP |
|---|---|---|
| 0 | 成功 | 200 |
| 4000 | 参数错误（@Valid 失败） | 400 |
| 4001 | 业务校验失败 | 400 |
| 4010 | 未登录 / Token 失效 | 401 |
| 4011 | Token 已过期 | 401 |
| 4030 | 无权限 | 403 |
| 4040 | 资源不存在 | 404 |
| 4090 | 资源冲突（重复领券、库存不足等） | 409 |
| 4290 | 接口限流 | 429 |
| 5000 | 服务器内部错误 | 500 |
| 5001 | 第三方依赖异常（支付 / 微信 / 短信） | 502 |
| 6001 | 库存不足 |
| 6002 | 订单状态非法流转 |
| 6003 | 优惠券不可用 |
| 6004 | 拼团已满 / 已结束 |
| 6005 | 商品已下架 |

业务异常一律 `throw new BizException(ResultCode.STOCK_NOT_ENOUGH)`，全局异常处理器统一翻译。

### 4.4 命名

- URL：小写 + 横线，如 `/api/user/address-list`，**不允许动词** 在 path 中（动词放 HTTP 方法）
- DTO：`XxxQry`（查询）、`XxxAddDto`（新增）、`XxxUpdDto`（更新）
- VO：`XxxVO`（列表项）、`XxxDetailVO`（详情）
- Mapper：方法以 `selectXxx / insertXxx / updateXxx / deleteXxx` 开头
- Service 接口：`XxxService`，实现：`XxxServiceImpl`

### 4.5 鉴权路径前缀

| 端 | 前缀 | 鉴权 |
|---|---|---|
| 用户端 | `/api/u/**` | `@LoginRequired` |
| 商家端 | `/api/admin/**` | `@AdminRequired`，按角色细分 |
| 公共 | `/api/pub/**` | 无需登录（首页、分类、商品列表、热门搜索…） |

> 拦截器顺序：CORS → 限流 → JWT 解析 → 权限校验。

### 4.6 ID 与单号

- 表主键：`BIGINT UNSIGNED AUTO_INCREMENT`
- 订单号：`OrderNoUtil.gen()`，格式 `yyMMddHHmmss + 6 位随机`，长度 18
- 退款号：`RFyyMMddHHmmss + 6 位随机`
- 自提码：6 位数字，订单维度唯一

### 4.7 时间

- 所有时间字段用 `LocalDateTime`，序列化格式 `yyyy-MM-dd HH:mm:ss`
- 入参支持 `yyyy-MM-dd` 或 `yyyy-MM-dd HH:mm:ss`
- 时区：`Asia/Shanghai`，`spring.jackson.time-zone=GMT+8`

### 4.8 金额

- 数据库 `DECIMAL(10,2)`，Java 一律 `BigDecimal`，禁止 `Double / float`
- 入参校验：`@DecimalMin("0.00")`，比较使用 `compareTo`

---

## 5. 通用基础设施（必须先写）

### 5.1 配置 `application.yml`（脱敏占位）

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  profiles:
    active: dev
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd HH:mm:ss
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/xianguoji?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: REDACTED_DB_PASSWORD
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      stat-view-servlet:
        enabled: true
        login-username: druid
        login-password: druid
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.xianguoji.server.module.*.entity
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true

xianguoji:
  jwt:
    secret: REDACTED_JWT_SECRET
    user-ttl-hours: 168          # 用户端 7 天
    admin-ttl-hours: 12          # 商家端 12 小时
  upload:
    base-dir: D:/xianguoji/upload
    domain: http://127.0.0.1:8080/static
  sms:
    provider: mock               # mock / aliyun
  wechat:
    appid: <your-appid>
    secret: <your-secret>
    pay:
      mch-id: <your-mchid>
      api-key-v3: <your-apikey>

logging:
  level:
    com.xianguoji: debug
```

### 5.2 全局异常处理器

`common/exception/GlobalExceptionHandler.java` 必须捕获：
1. `BizException` → 返回 R.fail(code, msg)
2. `MethodArgumentNotValidException` → 4000，msg = 第一条字段错误
3. `BindException` → 4000
4. `AccessDeniedException` → 4030
5. `Exception` → 5000，日志打印堆栈

### 5.3 JWT 工具

- 颁发：`JwtUtil.issue(subject, claims, ttlHours)`
- 解析：`JwtUtil.parse(token)` 失败抛 `BizException(TOKEN_INVALID)`
- 用户端 claim：`uid`、`role=user`
- 商家端 claim：`sid`、`role=staff`、`staffRole=owner|admin|packer|courier`

### 5.4 登录上下文

```java
public class LoginContext {
    private static final ThreadLocal<LoginUser> CTX = new ThreadLocal<>();
    public static void set(LoginUser u) { CTX.set(u); }
    public static LoginUser get()       { return CTX.get(); }
    public static Long uid()            { return get() == null ? null : get().getUid(); }
    public static void clear()          { CTX.remove(); }
}
```

拦截器在 `preHandle` 写入，在 `afterCompletion` 清理。

### 5.5 注解

- `@LoginRequired`：方法/类级，缺 token 抛 4010
- `@AdminRequired(roles = {"owner","admin"})`：未指定 roles 时仅判断已登录商家
- `@RateLimit(key, limit, period)`：基于 Redis lua

### 5.6 Knife4j

启动后访问 `http://localhost:8080/doc.html`，按"用户端 / 商家端 / 公共"分组。每个 Controller 必须 `@Tag`，每个方法 `@Operation`。

---

## 6. 数据库映射规则

- 实体类位于 `module/<m>/entity`，类名与表名按驼峰转换（`product_sku` → `ProductSku`）
- 字段映射依赖 `map-underscore-to-camel-case=true`，主键统一 `@TableId(type=AUTO)`
- JSON 字段使用 MyBatis-Plus 的 `@TableField(typeHandler = JacksonTypeHandler.class)`
- `created_at` / `updated_at` 自动填充，统一在 `MetaObjectHandler` 中处理
- 软删除字段：本项目大部分表无 `deleted` 字段——**禁止物理 delete 商品/订单**，使用 `status` 标记下架/取消即可

---

## 7. 模块详细设计

> 每个模块给出：**职责、核心实体、关键状态机、接口清单**。
> 接口清单格式：`METHOD 路径  →  说明 [鉴权]`

### 7.1 auth — 鉴权与登录

**职责**：用户端手机号验证码登录、微信登录；商家端账号密码登录。

**核心点**：
- 验证码：4 位数字，存 Redis `sms:code:{phone}`，TTL 5 分钟；同号 60 秒内不可重复发送，每日上限 10 次（`sms:limit:{phone}:day`）
- 用户首次登录自动注册，`tag=new`
- 商家端密码用 BCrypt 校验

**接口**：

| METHOD | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/pub/auth/sms/send` | 发送验证码 | 公共 |
| POST | `/api/pub/auth/login/sms` | 验证码登录 | 公共 |
| POST | `/api/pub/auth/login/wechat` | 微信小程序 code → openid 登录 | 公共 |
| POST | `/api/u/auth/logout` | 退出登录 | user |
| POST | `/api/pub/admin/login` | 商家账号密码登录 | 公共 |
| POST | `/api/admin/auth/logout` | 商家退出 | admin |
| GET  | `/api/admin/auth/me` | 当前商家信息 | admin |

返回 token 结构：`{ "token": "...", "expireAt": "..." , "userInfo": {...} }`

### 7.2 user — 用户中心 / 地址 / 足迹 / 收藏

**接口（用户端）**：

| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/u/user/profile` | 我的资料（含资产卡片：优惠券/收藏/足迹数量） |
| PUT  | `/api/u/user/profile` | 修改昵称、头像、性别、生日 |
| GET  | `/api/u/address/list` | 地址列表 |
| POST | `/api/u/address` | 新增地址（设默认时同步把其他置 0） |
| PUT  | `/api/u/address/{id}` | 更新 |
| DELETE | `/api/u/address/{id}` | 删除 |
| PUT  | `/api/u/address/{id}/default` | 设默认 |
| GET  | `/api/u/footprint/page` | 分页足迹 |
| DELETE | `/api/u/footprint` | 清空足迹 |
| GET  | `/api/u/favorite/page` | 分页收藏 |
| POST | `/api/u/favorite/{productId}` | 收藏 |
| DELETE | `/api/u/favorite/{productId}` | 取消收藏 |

**接口（商家端）**：客户管理使用 `stat` 模块统计聚合（见 7.13）。

### 7.3 shop — 店铺 / 自提点 / 配送设置

**接口（公共）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET | `/api/pub/shop/info` | 店铺基础信息 + 是否营业 |
| GET | `/api/pub/pickup-point/list` | 启用中的自提点（可按经纬度排序） |
| GET | `/api/pub/delivery-setting` | 配送费规则、可选时段、起送价 |

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| PUT | `/api/admin/shop/info` | 修改店铺信息 |
| PUT | `/api/admin/shop/open-status` | 切换营业状态 |
| GET/POST/PUT/DELETE | `/api/admin/pickup-point/**` | 自提点 CRUD |
| PUT | `/api/admin/delivery-setting` | 修改配送规则 |
| GET/PUT | `/api/admin/notify-setting/**` | 通知开关 |

### 7.4 catalog — 分类 / 商品 / SKU / Banner / 搜索

**核心规则**：
- 商品 `min_price/max_price/total_stock` 由后端在 SKU 增删改时**事务内同步**计算
- 商品状态：`0已下架 1在售 2回收站`；首页 / 分类只展示 `status=1`
- 删除商品 → 改为 `status=2`，30 天后由定时任务清理（先 TODO 占位）
- 搜索：先 LIKE 实现，预留接口可替换 ES

**接口（公共）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET | `/api/pub/category/tree` | 全量两级分类树 |
| GET | `/api/pub/banner/list` | 在线 Banner |
| GET | `/api/pub/hot-search/list` | 热门搜索 |
| GET | `/api/pub/product/page` | 分页商品；支持 `categoryId` `keyword` `sort=comprehensive/sales/priceAsc/priceDesc` |
| GET | `/api/pub/product/recommend` | 店主推荐双列瀑布流 |
| GET | `/api/pub/product/{id}` | 商品详情（含 SKU 列表、详情图、评价摘要） |

**接口（用户端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/u/search/history` | 我的搜索历史 |
| DELETE | `/api/u/search/history` | 清空 |
| POST | `/api/u/search/record` | 记录搜索词（关键字非空时入库） |

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/admin/category/**` | 分类管理 |
| GET | `/api/admin/product/page` | 分页（在售/下架/库存预警/回收站） |
| POST | `/api/admin/product` | 新增（含 SKU 数组、详情图） |
| PUT | `/api/admin/product/{id}` | 编辑 |
| PUT | `/api/admin/product/{id}/status` | 上下架 |
| POST | `/api/admin/product/{id}/copy` | 复制 |
| GET/POST/PUT/DELETE | `/api/admin/banner/**` | Banner 管理 |
| GET/POST/PUT/DELETE | `/api/admin/hot-search/**` | 热门搜索管理 |

### 7.5 cart — 购物车

**核心规则**：
- 唯一键 `(user_id, sku_id)`，加车若存在则数量累加
- 列表返回时联表带出商品/SKU 当前价 + 状态，**已下架/库存为 0 项前端置灰**但仍返回
- 数量变化触发"满减进度"计算，由 `promo` 模块返回

**接口（用户端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/u/cart/list` | 购物车列表 + 满减进度 |
| POST | `/api/u/cart` | 加车 `{skuId, quantity}` |
| PUT  | `/api/u/cart/{id}/quantity` | 改数量 |
| PUT  | `/api/u/cart/selected` | 批量勾选 / 全选 `{ids[], selected}` |
| DELETE | `/api/u/cart/{id}` | 删除 |
| DELETE | `/api/u/cart/clear` | 清空 |
| GET  | `/api/u/cart/count` | 购物车角标数量 |

### 7.6 promo — 优惠券 / 满减 / 拼团

**优惠券核心规则**：
- 领券原子操作：Redis 自增 `coupon:received:{couponId}`，超过 `total` 失败；同时校验 `per_user_limit`
- 计算可用券：传入"商品总价 + 商品 ID 列表 + 用户 ID"，返回可用列表 + 不可用原因
- 订单使用券：`user_coupon` 写 `order_id`、`status=1`、`used_at`

**满减**：在订单提交时由后端按 `promotion_rule` 中 `min_amount` 倒序匹配第一条命中的规则。

**拼团核心规则**：
- 用户开团：创建 `group_buy_instance(status=1, current_size=1, expire_at=now+activity.valid_hours)`，写 `group_buy_participant(is_leader=1)`
- 用户参团：实例必须 `status=1` 且未过期；`current_size++`；达到 `target_size` 时事务内置 `status=2`
- 拼团失败：定时任务扫描 `expire_at < now and status=1` → `status=3`，订单退款（先打 TODO）

**接口（公共/用户端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/pub/coupon/list` | 当前可领券列表 |
| POST | `/api/u/coupon/{id}/receive` | 领取 |
| GET  | `/api/u/coupon/list?status=` | 我的券（未使用/已使用/已过期） |
| POST | `/api/u/coupon/usable` | 给定订单商品计算可用券 |
| GET  | `/api/pub/group-buy/page` | 拼团活动列表 |
| POST | `/api/u/group-buy/launch` | 开团（需绑定订单） |
| POST | `/api/u/group-buy/{instanceId}/join` | 参团 |
| GET  | `/api/u/group-buy/{instanceId}` | 拼团详情 |

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/admin/coupon/**` | 优惠券模板 CRUD |
| GET/POST/PUT/DELETE | `/api/admin/promotion-rule/**` | 满减规则 CRUD |
| GET/POST/PUT/DELETE | `/api/admin/group-buy/**` | 拼团活动 CRUD |

### 7.7 order — 订单（核心模块）

**订单状态机**（与表注释完全一致）：

```
0待付款 ── 支付成功 ──► 1待接单
   │
   └─ 超时未付款 ──► 6已取消

1待接单 ── 商家接单 ──► 2备货中
1待接单 ── 商家拒单 ──► 6已取消（自动退款）

2备货中 ── 标记已出库（配送）──► 3配送中
2备货中 ── 标记已出库（自提）──► 4待自提

3配送中 ── 确认收货 / 自动确认（7天）──► 5已完成
4待自提 ── 商家核销自提码 ──► 5已完成

5已完成 ── 申请售后 ──► 7退款中 ── 商家同意 ──► 8已退款
```

**下单核心流程**（事务）：
1. 校验登录、地址/自提点、商品状态
2. 锁库存：`UPDATE product_sku SET stock = stock - ? WHERE id = ? AND stock >= ?`，影响行 0 → 抛 `STOCK_NOT_ENOUGH`
3. 计算金额：商品总价 → 满减 → 优惠券 → 配送费 → 实付
4. 生成订单号、自提码（自提单）
5. 写 `order` + `order_item` + `order_status_log(0→0)`
6. 标记 `user_coupon.status=1`
7. 拼团单：调用 `promo` 创建/加入 `group_buy_instance`
8. 返回订单号 + 待支付参数（`pay_method=wechat` 时调起微信支付，**先 TODO 占位** 返回 mock）

**退款核心流程**：
1. 校验订单归属、状态合法
2. 写 `refund(status=0)`
3. 商家审核 → `status=1` → 调微信支付退款（**TODO**）→ `status=3`
4. 同步订单 `pay_status=2`，若已发货状态特殊处理

**接口（用户端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| POST | `/api/u/order/preview` | 结算预览（试算金额、可用券、运费） |
| POST | `/api/u/order/submit` | 提交订单 |
| POST | `/api/u/order/{orderNo}/pay` | 发起支付 |
| GET  | `/api/u/order/page?tab=` | 订单列表（all/pending/processing/delivering/done/aftersale） |
| GET  | `/api/u/order/{orderNo}` | 订单详情 |
| POST | `/api/u/order/{orderNo}/cancel` | 取消未支付订单 |
| POST | `/api/u/order/{orderNo}/confirm` | 确认收货 |
| POST | `/api/u/order/{orderNo}/remind` | 提醒发货 |
| POST | `/api/u/order/{orderNo}/repurchase` | 再来一单（生成购物车） |
| POST | `/api/u/refund` | 申请售后 |
| GET  | `/api/u/refund/{refundNo}` | 售后详情 |

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/admin/order/page` | 多条件分页 |
| GET  | `/api/admin/order/{orderNo}` | 订单详情 |
| POST | `/api/admin/order/{orderNo}/accept` | 接单 |
| POST | `/api/admin/order/{orderNo}/reject` | 拒单（带原因） |
| POST | `/api/admin/order/{orderNo}/ship` | 标记已出库 |
| POST | `/api/admin/order/{orderNo}/pickup-verify` | 核销自提码 |
| POST | `/api/admin/order/{orderNo}/print` | 打印小票（返回小票模板数据） |
| GET  | `/api/admin/order/export` | 导出 Excel |
| POST | `/api/admin/refund/{refundNo}/approve` | 同意退款 |
| POST | `/api/admin/refund/{refundNo}/reject` | 拒绝退款 |

**新订单推送**：暂用前端轮询 `/api/admin/order/new-count?since=<ts>` 实现"叮咚提醒"，WebSocket 列入二期。

### 7.8 review — 评价

**接口（用户端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/u/review/pending` | 待评价订单项列表 |
| POST | `/api/u/review` | 提交评价（可批量按 `orderItemId` 数组） |
| GET  | `/api/pub/review/product/{productId}` | 商品评价分页（filter=all/withImage/good/middle/bad） |
| GET  | `/api/pub/review/product/{productId}/summary` | 评价摘要（总评、各维度均分） |

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/admin/review/page` | 全部 / 待回复 / 有图 / 差评 |
| POST | `/api/admin/review/{id}/reply` | 回复 |
| PUT  | `/api/admin/review/{id}/hidden` | 隐藏 / 显示 |

### 7.9 message — 消息中心 / 反馈

**接口（用户端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/u/message/page?type=` | 分页消息（type=system/order/promo/group） |
| PUT  | `/api/u/message/{id}/read` | 标记已读 |
| PUT  | `/api/u/message/read-all` | 全部已读 |
| GET  | `/api/u/message/unread-count` | 未读数 |
| POST | `/api/u/feedback` | 提交反馈 |

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET  | `/api/admin/feedback/page` | 反馈列表 |
| PUT  | `/api/admin/feedback/{id}/handle` | 标记已处理 |
| POST | `/api/admin/message/broadcast` | 发广播消息（user_id=0） |

### 7.10 staff — 员工管理

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/admin/staff/**` | CRUD（仅 owner、admin 可访问） |
| PUT | `/api/admin/staff/{id}/password` | 重置密码 |
| PUT | `/api/admin/staff/me/password` | 改自己密码 |

### 7.11 stat — 工作台 / 客户管理

**接口（商家端）**：
| METHOD | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/stat/dashboard` | 工作台四指标卡 |
| GET | `/api/admin/stat/order-trend?days=7` | 近 N 天订单趋势 |
| GET | `/api/admin/stat/order-status` | 今日订单状态分布 |
| GET | `/api/admin/stat/top-products?limit=10` | 热销 TopN |
| GET | `/api/admin/stat/todo` | 待办（未处理订单/待回复评价/库存预警/待审核退款 数量） |
| GET | `/api/admin/customer/page` | 客户列表（含累计订单数、累计消费、最近下单） |
| GET | `/api/admin/customer/{id}` | 客户详情 |
| POST | `/api/admin/customer/{id}/coupon` | 给指定用户发券 |

### 7.12 文件上传

| METHOD | 路径 | 说明 |
|---|---|---|
| POST | `/api/u/file/upload` | 用户端上传（评价图、反馈图） |
| POST | `/api/admin/file/upload` | 商家端上传（商品图、Banner、店铺Logo） |

- 限制：单文件 ≤ 5 MB，仅 `jpg/jpeg/png/webp`
- 存储：`xianguoji.upload.base-dir` 下按 `yyyy/MM/dd/uuid.ext`
- 返回完整 URL：`http://...domain/yyyy/MM/dd/uuid.ext`

### 7.13 定时任务（Spring `@Scheduled`）

| Cron | 任务 |
|---|---|
| `0 */1 * * * *` | 取消超过 15 分钟未支付订单 |
| `0 */5 * * * *` | 拼团失败扫描（过期未成团） |
| `0 0 3 * * *` | 用户优惠券过期标记 |
| `0 0 4 * * *` | 用户标签更新（30天无下单 → silent） |
| `0 0 1 1 * *` | 每月一日凌晨清理 30 天前的浏览足迹 |

---

## 8. 安全与合规

- 密码 BCrypt cost=10
- 手机号脱敏：列表/评价回包统一 `138****0001`，由 VO 转换处理
- SQL 注入：全程 MyBatis 参数绑定，禁止字符串拼 SQL
- XSS：商品介绍富文本进库前 `Jsoup.clean(html, Safelist.relaxed())`
- 限流：登录/发送验证码 接口 `@RateLimit(key="ip+phone", limit=5, period=60)`
- 跨域：仅放行配置中的域名，不使用 `*`
- 日志：请求/响应 JSON 通过 AOP 打印；敏感字段（密码、token、apiKey）脱敏

---

## 9. 测试要求

- 每个 Service 至少给出**核心成功路径 + 1 个异常路径**的单测
- 关键场景必须覆盖：
  - 下单库存扣减（并发用 `MockMvc + ExecutorService` 模拟 10 并发）
  - 优惠券领取超量
  - 拼团满员触发成团
  - 退款审核流程
- Controller 层用 `MockMvc` 跑接口契约测试

---

## 10. 开发任务拆分（建议迭代顺序）

> 一个迭代 = 一个可运行的里程碑。AI 工具按顺序产出代码并在每个里程碑跑通启动 + Knife4j 接口可见。

### M1 工程基建（0.5 天）
- 初始化 pom、目录、`XianguojiApplication`
- 实现 `R / PageR / ResultCode / BizException / GlobalExceptionHandler`
- 集成 MyBatis-Plus、Druid、Knife4j、Redis
- 写 `MetaObjectHandler`（自动填充 created_at / updated_at）
- 写 `JwtUtil / LoginContext / LoginInterceptor / WebMvcConfig`
- 写 `RateLimit` 注解 + Redis Lua

### M2 鉴权 & 用户基础（1 天）
- module/auth：发送验证码、验证码登录、商家登录、退出
- module/user：profile、address CRUD

### M3 商品域只读（1 天）
- module/shop：店铺信息、自提点列表、配送设置（公共接口先做）
- module/catalog：分类树、Banner、热门搜索、商品分页/详情/推荐、搜索词记录

### M4 购物车 & 促销（1 天）
- module/cart：完整 CRUD + 满减进度计算
- module/promo（用户端）：领券、我的券、可用券计算

### M5 订单核心（2 天）
- module/order：preview / submit / pay（mock）/ list / detail / cancel / confirm / remind
- 库存事务扣减、状态流转日志、自提码生成
- 商家端：accept / reject / ship / pickup-verify / page / detail / export
- 退款：申请、审核

### M6 评价 & 互动（0.5 天）
- module/review：用户提交、商品聚合、商家回复
- module/user：足迹、收藏

### M7 拼团（1 天）
- module/promo（拼团）：开团 / 参团 / 详情 / 列表 / 失败回滚（TODO 退款占位）
- 定时任务：拼团失败扫描

### M8 商家管理域（1.5 天）
- module/staff：员工 CRUD
- module/catalog（商家端）：分类、商品、SKU、Banner、热门搜索 CRUD
- module/promo（商家端）：优惠券、满减、拼团活动 CRUD
- module/shop（商家端）：营业开关、店铺信息修改、自提点 CRUD、配送/通知设置

### M9 工作台 & 客户（1 天）
- module/stat：dashboard / 趋势 / 状态分布 / 热销 / 待办
- 客户管理：列表 / 详情 / 发券

### M10 周边 & 收尾（0.5 天）
- module/message：消息、反馈
- 文件上传
- 定时任务（订单超时、过期券、用户标签）
- 单元测试覆盖核心场景

> 总工时估算：≈ 10 人日。AI 工具按 M1→M10 顺序生成代码，每个里程碑结束后**必须**：(1) 启动应用 (2) 通过 Knife4j 自查接口 (3) 跑通对应单测。

---

## 11. 第三方占位（先打 TODO，不要写假实现）

| 集成 | 占位接口 | 备注 |
|---|---|---|
| 短信发送 | `SmsService.send(phone, code)` | dev 环境 mock 打日志：验证码固定 `1234` |
| 微信小程序登录 | `WxAuthService.code2Session(jsCode)` | 抛 UnsupportedOperationException("TODO") |
| 微信支付下单 | `WxPayService.unifiedOrder(order)` | 同上 |
| 微信支付退款 | `WxPayService.refund(...)` | 同上 |
| 高德地图 | `MapService.distance(...)` | 同上 |

> AI 工具不要使用任何"听起来正确"的假 SDK 凭证，所有外部依赖必须保持显式 TODO。

---

## 12. 验收清单（每个 PR 必须自查）

- [ ] 接口路径、方法、入参、出参与本文一致
- [ ] 入参全部 `@Valid` 校验，无原始 `String` 直接落库
- [ ] 业务异常使用 `BizException + ResultCode`，无 `return null` / `return new R(-1, ...)`
- [ ] 数据库写操作 `@Transactional(rollbackFor = Exception.class)`
- [ ] 涉及金额一律 `BigDecimal`
- [ ] Mapper.xml 中无 `${}` 拼接
- [ ] 新增表/字段已经追加到 `database/migrations/`
- [ ] 单元测试已添加并跑通
- [ ] Knife4j 中能看到新接口并能成功调用
- [ ] 启动日志无 ERROR / 异常堆栈

---

## 13. 交付物

1. 本仓库代码（`xianguoji-server/`）
2. 完整 `docs/api.md`（由 Knife4j 导出 OpenAPI JSON 转 Markdown）
3. `docs/error-codes.md`（错误码字典）
4. `docs/migration-log.md`（按时间倒序的 schema 变更记录）
5. Postman/Apifox 接口集合（导入 OpenAPI 即可）

---

**END** — 开始按 §10 的 M1 执行。
