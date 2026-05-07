# 鲜果记 生产前测试方案（Test Plan）

> 范围：`xianguoji-server`（Spring Boot 3 / Java 17 / MyBatis-Plus / Redis / WebSocket）、`user-front`（uni-app + Vue3，用户端小程序/H5）、`merchant-front`（Vue3 + Vite，商家管理台）。
> 目标：在进入生产前，对单商户生鲜电商核心链路（注册登录 → 浏览下单 → 支付配送 → 售后/评价 → 商家后台运营）完成单元、接口、集成、端到端、性能、安全、兼容性 7 类测试，达到上线质量门槛。

---

## 0. 文档使用说明（给执行编码的 AI）

- **每个测试用例**都包含：编号 / 模块 / 类型 / 前置条件 / 步骤 / 输入 / 预期 / 优先级 / 实现位置（建议路径）。
- **编号规则**：`TC-{层级}-{模块}-{序号}`，层级取值 `UT`(单元) / `API`(接口) / `INT`(集成) / `E2E` / `PERF` / `SEC` / `COMP`。
- **优先级**：P0=阻断上线，P1=必须修，P2=建议修，P3=可选。
- **覆盖率门槛**：后端单元测试行覆盖率 ≥ 70%，核心模块（order/cart/promo/auth）≥ 85%；接口测试 P0/P1 用例 100% 通过。
- **技术栈建议**：
  - 后端单元 / 接口：JUnit 5 + Mockito + Spring Boot Test + MockMvc + Testcontainers（MySQL / Redis）
  - 后端性能：JMeter 或 k6
  - 前端单元：Vitest + @vue/test-utils
  - 端到端：Playwright（Web） / Appium 或微信开发者工具自动化（小程序）
  - 安全：OWASP ZAP + 依赖项扫描（已有 `dependency-check-suppressions.xml`）
- **测试数据**：所有种子数据集中放在 `xianguoji-server/src/test/resources/sql/` 与 `*/fixtures/`，禁止硬编码生产数据。

---

## 1. 测试环境

| 环境 | 用途 | 数据 | 配置 |
|---|---|---|---|
| `local` | 开发自测 | docker-compose 起 MySQL+Redis | `application-dev.yml` |
| `test` | CI 自动化 | Testcontainers 临时实例 | `application-test.yml`（新建） |
| `staging` | 预发，业务回归 | 生产脱敏快照 | `application-staging.yml` |
| `prod` | 生产 | — | 仅冒烟 |

**新建配置项**：
- `xianguoji-server/src/test/resources/application-test.yml`：禁用外部依赖（OSS、短信、微信支付）走 mock。
- `xianguoji-server/src/test/resources/sql/schema.sql` + `data.sql`：建表 + 基础字典/账号。

---

## 2. 模块清单与责任矩阵

后端模块（`com.xianguoji.server.module.*`）：

| 模块 | 关键能力 | 风险点 |
|---|---|---|
| auth | 登录、注册、JWT、验证码、微信登录 | 越权、Token 伪造、验证码绕过 |
| user | 个人资料、收货地址、足迹/收藏 | PII 泄漏、越权 |
| catalog | 分类、商品、规格、库存 | 库存超卖、价格篡改 |
| cart | 购物车增删改查 | 数量/SKU 一致性 |
| order | 下单、支付、发货、售后 | 重复下单、超卖、金额计算、状态机 |
| promo | 优惠券、拼团、分享码 | 优惠叠加、并发抢券、拼团成团判定 |
| review | 评价、晒单 | 内容审核、重复评价 |
| message / chat | 站内信、客服 IM（WebSocket） | 鉴权、消息丢失 |
| shop / staff | 店铺资料、员工权限 | 角色越权 |
| report / stat | 报表导出、客户分析 | 大数据量导出超时、SQL 注入 |
| help | 帮助中心 | XSS |

前端模块：
- 用户端：splash/login、首页(`pages/index`)、分类、商品详情、购物车、下单/支付、订单中心、个人资料、地址、优惠券、足迹、收藏、评价、反馈、消息、拼团、自提点、设置。
- 商家端：Login/ForgotPassword、Dashboard、Goods/GoodsEdit、Orders/OrderDetail、Shipping、Reviews、Customers、Campaign/CouponCreate、Messages、ReportExport、BusinessAnalysis、HelpCenter、Settings。

---

## 3. 单元测试（Unit Test）

实现位置：`xianguoji-server/src/test/java/com/xianguoji/server/module/{模块}/...`，与 `main` 包结构镜像。

### 3.1 通用约定
- 一个公共方法至少 1 条正向 + 1 条边界 + 1 条异常用例。
- Service 用 Mockito mock Mapper 与外部 Client；工具类用纯 JUnit。
- 命名：`{被测方法}_should_{预期}_when_{条件}`。

### 3.2 用例清单（节选高优先级）

| 编号 | 模块 | 被测对象 | 用例 | 预期 | 优先级 |
|---|---|---|---|---|---|
| TC-UT-AUTH-001 | auth | `JwtUtil.generate/parse` | 正常签发并解析 | 用户ID/角色/过期时间一致 | P0 |
| TC-UT-AUTH-002 | auth | `JwtUtil.parse` | 篡改签名 | 抛 `InvalidTokenException` | P0 |
| TC-UT-AUTH-003 | auth | `JwtUtil.parse` | 已过期 token | 抛 `ExpiredTokenException` | P0 |
| TC-UT-AUTH-004 | auth | `CaptchaService.verify` | 验证码大小写 / 一次性 | 二次校验失败 | P1 |
| TC-UT-AUTH-005 | auth | `AuthService.loginByPassword` | 密码错误连续 5 次 | 锁定账户 15 分钟 | P0 |
| TC-UT-AUTH-006 | auth | `AuthService.loginByWechat` | 微信 code 失效 | 返回业务错误码 | P1 |
| TC-UT-CART-001 | cart | `CartService.addItem` | 同 SKU 累加，超过上限取上限 | 数量=上限 | P0 |
| TC-UT-CART-002 | cart | `CartService.addItem` | SKU 已下架 | 抛 `SkuOfflineException` | P0 |
| TC-UT-CART-003 | cart | `CartService.checkout` | 部分商品库存不足 | 仅冻结可用项，返回不可用列表 | P0 |
| TC-UT-ORDER-001 | order | `OrderCalc.calcAmount` | 商品+运费+优惠券+满减组合 | 与表格断言一致（参数化） | P0 |
| TC-UT-ORDER-002 | order | `OrderCalc.calcAmount` | 优惠后金额 < 0 | 取 0，不允许负数 | P0 |
| TC-UT-ORDER-003 | order | `OrderStateMachine.transit` | 已取消单再发货 | 抛非法状态 | P0 |
| TC-UT-ORDER-004 | order | `StockService.deduct` | 并发扣减 | 乐观锁/Redis 原子操作不超卖（用 `CountDownLatch` 模拟 100 线程） | P0 |
| TC-UT-ORDER-005 | order | `OrderService.refund` | 已发货前/后退款金额 | 前=全额，后=按规则 | P1 |
| TC-UT-PROMO-001 | promo | `CouponService.claim` | 限领 1 张，重复领取 | 第二次拒绝 | P0 |
| TC-UT-PROMO-002 | promo | `CouponService.use` | 不满门槛 | 拒绝 | P0 |
| TC-UT-PROMO-003 | promo | `GroupBuyService.join` | 团已满 / 已过期 | 拒绝并返回错误码 | P0 |
| TC-UT-PROMO-004 | promo | `GroupBuyService.settle` | 到期未成团 | 全员自动退款 | P0 |
| TC-UT-CATALOG-001 | catalog | `SkuService.updateStock` | 入库为负数 | 抛参数异常 | P1 |
| TC-UT-USER-001 | user | `AddressService.setDefault` | 切换默认地址 | 旧默认置 false | P1 |
| TC-UT-REVIEW-001 | review | `ReviewService.submit` | 同订单同 SKU 二次评价 | 拒绝 | P1 |
| TC-UT-REPORT-001 | report | `ReportExportService.buildExcel` | 10w 行流式导出 | 不 OOM，文件可解析 | P1 |
| TC-UT-COMMON-001 | common | `MoneyUtil.add/sub/mul` | 浮点精度 | 用 BigDecimal，2 位小数 HALF_UP | P0 |

> 完整列表：每个 Service 公共方法逐一展开（约 180+ 条）。AI 实现时按"模块 → Service → 方法 → 用例"四级生成。

---

## 4. 接口/集成测试（API + Integration）

实现位置：`xianguoji-server/src/test/java/.../api/{模块}ApiTest.java`，统一基类 `AbstractApiTest`（启动 `@SpringBootTest(webEnvironment=RANDOM_PORT)` + Testcontainers）。

### 4.1 通用规则
- 每个 Controller 一个测试类。
- 每个端点至少：未登录 401、越权 403、参数校验失败 400、正常 200、业务规则失败业务码。
- 数据库脏数据通过 `@Transactional` 或 `@Sql(scripts=..., executionPhase=AFTER_TEST_METHOD)` 清理。
- 响应断言：HTTP 状态码 + `code`/`msg`/`data` 三段；金额、时间字段格式断言。

### 4.2 端点用例（按 Controller 列出代表用例）

#### 4.2.1 AuthController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-001 | POST /auth/register | 手机号已注册 | 业务码 USER_EXISTS | P0 |
| TC-API-AUTH-002 | POST /auth/register | 验证码错误 | 业务码 CAPTCHA_INVALID | P0 |
| TC-API-AUTH-003 | POST /auth/login | 正常 | 返回 token + 过期时间 | P0 |
| TC-API-AUTH-004 | POST /auth/login | 5 次错误后 | 锁定 | P0 |
| TC-API-AUTH-005 | POST /auth/wechat-login | 新用户首次 | 自动建号并返回 token | P0 |
| TC-API-AUTH-006 | POST /auth/bind-phone | 已绑定其他账号 | 拒绝 | P1 |
| TC-API-AUTH-007 | POST /auth/refresh | refreshToken 过期 | 401 强制重登 | P0 |
| TC-API-AUTH-008 | POST /auth/logout | 登出后 token 立即失效（黑名单） | 第二次请求 401 | P0 |

#### 4.2.2 CartController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-CART-001 | POST /cart/items | 正常添加 | 返回购物车快照 | P0 |
| TC-API-CART-002 | PUT /cart/items/{id} | 改数量超限 | 截断到上限并提示 | P1 |
| TC-API-CART-003 | DELETE /cart/items | 批量删除部分 ID 不属于当前用户 | 仅删除自己的，返回拒绝列表 | P0（越权） |
| TC-API-CART-004 | GET /cart | 含已下架 SKU | 标记 unavailable | P1 |

#### 4.2.3 CatalogController / AdminCatalogController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-CAT-001 | GET /catalog/goods | 分页+分类过滤 | 数据正确、page/size 边界 | P0 |
| TC-API-CAT-002 | GET /catalog/goods/{id} | 不存在 ID | 业务码 GOODS_NOT_FOUND | P1 |
| TC-API-CAT-003 | POST /admin/catalog/goods | 普通用户调用 | 403 | P0 |
| TC-API-CAT-004 | PUT /admin/catalog/goods/{id} | 上下架并发 | 最终状态一致 | P1 |

#### 4.2.4 OrderController / AdminOrderController / OrderExportController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-ORD-001 | POST /orders/preview | 含优惠券+运费 | 金额与单元测试一致 | P0 |
| TC-API-ORD-002 | POST /orders | 正常下单 | 生成订单 + 扣库存 + 清理购物车 | P0 |
| TC-API-ORD-003 | POST /orders | 同幂等键重复 | 第二次返回首次结果，不重复创建 | P0 |
| TC-API-ORD-004 | POST /orders | 库存不足 | 整单失败回滚 | P0 |
| TC-API-ORD-005 | POST /orders/{id}/pay-callback | 重复回调 | 幂等不重复发货 | P0 |
| TC-API-ORD-006 | POST /orders/{id}/cancel | 已支付未发货 | 触发退款流程 | P0 |
| TC-API-ORD-007 | POST /orders/{id}/refund | 仅退款 vs 退货退款 | 不同状态机分支 | P1 |
| TC-API-ORD-008 | POST /admin/orders/{id}/ship | 缺物流单号 | 400 | P1 |
| TC-API-ORD-009 | GET /admin/orders/export | 大量数据 | 流式导出，HTTP 200 + 附件头 | P1 |

#### 4.2.5 PromoController（Coupon / GroupBuy / AdminPromo）
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-PRM-001 | POST /coupons/{id}/claim | 库存 1 张 / 100 并发 | 仅一人成功 | P0 |
| TC-API-PRM-002 | GET /coupons/mine | 含已过期 | 标记 expired | P2 |
| TC-API-PRM-003 | POST /group-buy/{id}/join | 通过分享码进入 | 计入团队 | P1 |
| TC-API-PRM-004 | POST /group-buy/{id}/join | 团已满 | 拒绝 | P0 |
| TC-API-PRM-005 | (cron) 拼团到期任务 | 未成团 | 自动退款 + 站内信 | P0 |

#### 4.2.6 ReviewController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-REV-001 | POST /reviews | 上传 9 张图 + 视频 | 成功 | P1 |
| TC-API-REV-002 | POST /reviews | 含违禁词（mock 审核） | 拒绝并提示 | P1 |

#### 4.2.7 MessageController + WebSocket（chat）
| 编号 | 场景 | 预期 | P |
|---|---|---|---|
| TC-API-MSG-001 | 未带 token 连接 ws | 握手失败 | P0 |
| TC-API-MSG-002 | A→B 发消息，B 离线 | 入库未读，B 上线收到 | P0 |
| TC-API-MSG-003 | 同一用户多端登录 | 多端同步收消息 | P1 |
| TC-API-MSG-004 | 心跳超时 | 服务端关闭连接 | P2 |
| TC-API-MSG-005 | ChatMessageVO.senderAvatar：用户消息 | senderType=0 时 senderAvatar = 用户 avatar | P0 |
| TC-API-MSG-006 | ChatMessageVO.senderAvatar：商家消息 | senderType=1 时 senderAvatar = Staff avatar（通过 createBy 查 staff） | P0 |
| TC-API-MSG-007 | ChatMessageVO.senderAvatar：用户无头像 | senderAvatar = null，前端 fallback 到默认头像 | P1 |
| TC-API-MSG-008 | ChatMessageVO.senderAvatar：商家无头像 | senderAvatar = null，前端 fallback 到图标 | P1 |

#### 4.2.8 UserController / FootprintFavoriteController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-USR-001 | PUT /user/profile | 头像 URL 非白名单域 | 拒绝 | P1 |
| TC-API-USR-002 | POST /user/address | 超过上限 20 条 | 拒绝 | P2 |
| TC-API-USR-003 | GET /user/footprints | 分页 + 自动去重 | 同商品取最新 | P2 |

#### 4.2.9 ShopController / StaffController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-SHP-001 | PUT /admin/shop | 普通员工调用 | 403 | P0 |
| TC-API-STF-001 | POST /admin/staff | 角色越权（店员建店长） | 403 | P0 |

#### 4.2.10 ReportController / StatController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-RPT-001 | GET /admin/stat/overview | 7/30/自定义区间 | 数据与 SQL 直查一致 | P1 |
| TC-API-RPT-002 | GET /admin/report/sales/export | 100 万条 | 不超时不 OOM | P1 |

#### 4.2.11 HelpController
| 编号 | 端点 | 场景 | 预期 | P |
|---|---|---|---|---|
| TC-API-HLP-001 | GET /help/articles | 含 HTML | 输出已转义/白名单 | P1（XSS） |

---

## 5. 端到端测试（E2E）

### 5.1 用户端关键流程
| 编号 | 场景 | 步骤 | 预期 | P |
|---|---|---|---|---|
| TC-E2E-U-001 | 新用户注册→完善资料→登录 | splash → 微信授权 → 绑手机 → 完善资料 → 进首页 | 全程无报错，token 有效 | P0 |
| TC-E2E-U-002 | 浏览→加购→下单→支付→收货→评价 | 首页选品 → 详情加购 → 购物车结算 → 选地址/券 → 模拟支付成功 → 商家发货 → 用户确认 → 评价 | 订单状态机全流转，金额一致 | P0 |
| TC-E2E-U-003 | 拼团完整链路 | 开团 → 分享码邀请 → 第二人参团成团 → 发货 → 收货 | 两单成团；未成团则全退 | P0 |
| TC-E2E-U-004 | 优惠券领取与使用 | 领券 → 下单选择 → 计算金额 → 支付 | 金额扣减与预览一致 | P0 |
| TC-E2E-U-005 | 申请退款 | 已支付未发货 → 申请仅退款 → 商家同意 → 退款到账 | 状态/通知正确 | P0 |
| TC-E2E-U-006 | 自提点下单 | 下单选自提 → 到点核销码 | 核销后状态置已完成 | P1 |
| TC-E2E-U-007 | 反馈提交（含图片上传 OSS） | 反馈页填表 + 上传图 → 提交 | OSS URL 入库，列表可见 | P1 |
| TC-E2E-U-008 | 客服会话 | 进入会话 → 发文字/图 → 商家回复 → 关闭 | 双端消息一致，未读数正确 | P1 |
| TC-E2E-U-009 | 客服头像渲染 | 用户端进入聊天 → 查看商家消息头像 | 商家消息显示 senderAvatar（或 fallback logo.png） | P0 |
| TC-E2E-U-010 | Logo 统一验证 | 依次检查 splash / login / chat 欢迎页 / favicon | 均使用 logo.png，无 SVG 占位符或旧图标 | P1 |

### 5.2 商家端关键流程
| 编号 | 场景 | 预期 | P |
|---|---|---|---|
| TC-E2E-M-001 | 登录→Dashboard 数据加载 | 概览数字与后端一致 | P0 |
| TC-E2E-M-002 | 商品上下架 + 编辑 SKU | 用户端立即生效 | P0 |
| TC-E2E-M-003 | 订单发货（填物流单号） | 用户端订单状态切换 | P0 |
| TC-E2E-M-004 | 优惠券创建 + 投放 | 用户端可领 | P1 |
| TC-E2E-M-005 | 报表导出 Excel | 文件可打开，列齐全 | P1 |
| TC-E2E-M-006 | 客服回复 | 用户端实时收到 | P1 |
| TC-E2E-M-007 | 忘记密码找回 | 邮箱/短信验证后改密成功 | P1 |
| TC-E2E-M-008 | 客服聊天用户头像 | 商家端聊天消息列表 → 查看用户消息头像 | 用户消息显示 senderAvatar（或 fallback 图标） | P0 |
| TC-E2E-M-009 | Logo 统一验证 | 依次检查 Login / ForgotPassword / DefaultLayout sidebar / Settings 版本信息 / favicon | 均使用 logo.png，无 material icon 占位 | P1 |

### 5.3 实现建议
- 用户端 H5 用 Playwright；小程序用微信开发者工具 CLI + 自动化脚本。
- 商家端用 Playwright，目录 `merchant-front/tests/e2e/`。
- 测试数据通过后端"测试夹具接口"（仅 staging 启用）一键重置。

---

## 6. 前端单元/组件测试

实现位置：
- `user-front/src/__tests__/`
- `merchant-front/src/__tests__/`

| 编号 | 对象 | 用例 | P |
|---|---|---|---|
| TC-FE-U-001 | 购物车 store | 增/减/清空/选中价格汇总 | P0 |
| TC-FE-U-002 | 下单页金额计算 composable | 与后端 preview 接口同输入同输出 | P0 |
| TC-FE-U-003 | 表单校验（手机/验证码/地址） | 正/反例 | P1 |
| TC-FE-U-004 | 图片上传组件 | 超大/格式错/网络错 | P1 |
| TC-FE-M-001 | GoodsEdit 表单 | 必填/价格/库存边界 | P1 |
| TC-FE-M-002 | OrderDetail 状态展示 | 各状态枚举映射 | P1 |
| TC-FE-M-003 | 路由守卫 | 未登录跳 Login，权限不足跳 403 | P0 |

---

## 7. 性能测试（Performance）

工具：JMeter 或 k6，脚本目录 `xianguoji-server/scripts/perf/`。

| 编号 | 场景 | 模型 | 通过标准 | P |
|---|---|---|---|---|
| TC-PERF-001 | 首页商品列表 | 500 并发持续 5 min | P95 < 500ms，错误率 < 0.1% | P0 |
| TC-PERF-002 | 商品详情 | 1000 并发 | P95 < 600ms | P0 |
| TC-PERF-003 | 加购 | 200 并发 | P95 < 400ms | P1 |
| TC-PERF-004 | 下单 | 100 并发，热门 SKU 库存 50 | 不超卖，P95 < 1.5s | P0 |
| TC-PERF-005 | 抢券 | 1000 并发抢 100 张 | 恰好 100 张发出，无超发 | P0 |
| TC-PERF-006 | 拼团参团 | 200 并发 | 团人数严格不超员 | P0 |
| TC-PERF-007 | 报表导出 | 单请求 100 万行 | 内存峰值 < 512MB，10 min 内完成 | P1 |
| TC-PERF-008 | WebSocket | 5000 长连接，1 msg/s | 服务端 CPU < 70% | P1 |

容量基线：登录在线用户 ≥ 1 万，QPS ≥ 2000（读）/ 200（写）。

---

## 8. 安全测试（Security）

| 编号 | 类别 | 用例 | 预期 | P |
|---|---|---|---|---|
| TC-SEC-001 | 鉴权 | 不带 token 访问受保护接口 | 401 | P0 |
| TC-SEC-002 | 越权（IDOR） | 用户 A 用 token 调 `GET /orders/{B 的 id}` | 403/404 | P0 |
| TC-SEC-003 | 越权（角色） | 普通员工调店长接口 | 403 | P0 |
| TC-SEC-004 | SQL 注入 | 列表接口 `keyword='OR 1=1--`、排序字段注入 | 参数化保护，无异常 | P0 |
| TC-SEC-005 | XSS | 评价/帮助文章/商品详情富文本 | 输出转义/白名单 | P0 |
| TC-SEC-006 | CSRF | 商家端关键写接口 | 非同源/无 token 拒绝 | P1 |
| TC-SEC-007 | 文件上传 | 改后缀的 .jsp/.exe、超大文件 | 拒绝；类型/大小白名单 | P0 |
| TC-SEC-008 | OSS 直传签名 | 重放、跨用户上传 | 签名带过期 + path 前缀绑定用户 | P0 |
| TC-SEC-009 | 验证码 | 验证码可枚举（短/无频控） | 频控 + 一次性 | P0 |
| TC-SEC-010 | JWT | 改 alg=none、改 payload | 拒绝 | P0 |
| TC-SEC-011 | 速率限制 | 登录、领券、下单 | 阈值触发 429 | P0 |
| TC-SEC-012 | 敏感日志 | 日志中不出现密码、token、手机号明文 | grep 校验 | P0 |
| TC-SEC-013 | 依赖漏洞 | OWASP Dependency-Check | 高危=0 | P0 |
| TC-SEC-014 | 价格篡改 | 下单时前端改 price | 后端以 SKU 主数据为准 | P0 |
| TC-SEC-015 | 重放 | 支付回调签名 + 单号防重放 | 第二次拒绝 | P0 |
| TC-SEC-016 | CORS | 商家端域名白名单 | 非白名单 origin 拒绝 | P1 |

---

## 9. 兼容性测试（Compatibility）

| 编号 | 端 | 设备/平台 | 关注 | P |
|---|---|---|---|---|
| TC-COMP-001 | 用户端 H5 | iOS Safari 15+/16/17、微信内置浏览器、Android Chrome 100+ | 布局、支付跳转 | P0 |
| TC-COMP-002 | 用户端小程序 | 微信小程序基础库 ≥ 2.27 | 接口可用、滚动、输入法 | P0 |
| TC-COMP-003 | 商家端 | Chrome/Edge 最新 2 个版本 | 核心页面 | P0 |
| TC-COMP-004 | 屏幕 | 320 / 375 / 414 / 1280 / 1920 | 不溢出不重叠 | P1 |
| TC-COMP-005 | 弱网 | 3G/丢包 20% | 接口超时友好提示、重试 | P1 |

---

## 10. 数据 / 回归 / 上线前清单

### 10.1 数据库
- 所有迁移脚本在 `database/` 目录，按版本号排序；测试 `flyway/liquibase` 重复执行幂等。
- 关键表 `order`/`order_item`/`stock_log`/`coupon_user` 必须有唯一索引防重。

### 10.2 回归用例集
- 把 P0 接口测试 + P0 E2E 抽成 `nightly` 与 `pre-release` 两套。
- 每次发布前 staging 全量执行。

### 10.3 上线前 Go/No-Go 检查
- [ ] 所有 P0/P1 用例通过
- [ ] 行覆盖率达标
- [ ] 性能基线通过
- [ ] 安全扫描无高危
- [ ] 日志/监控/报警接入（错误率、P95、订单失败率、库存异常）
- [ ] 灰度方案与回滚脚本就绪
- [ ] 备份/恢复演练通过

---

## 11. 实现路线（建议给 AI 的执行顺序）

1. **脚手架**：建 `src/test` 目录与 `application-test.yml`、Testcontainers 基类、统一断言工具、登录夹具（颁发 token）。
2. **公共工具单元测试**（MoneyUtil/JwtUtil/CaptchaService）— 快速回正。
3. **领域核心单元**：order 计算、状态机、stock 扣减、promo 抢券/拼团结算。
4. **接口层**：按本文 §4 顺序，每个 Controller 一类。
5. **WebSocket 集成**（chat / message）。
6. **前端单元 + 路由守卫**。
7. **E2E**（用户端→商家端关键链路各 1 条主路径打通后再扩展）。
8. **性能 + 安全 + 兼容性**。
9. **CI 接入**：GitHub Actions / 自建 — `mvn verify` + 前端 `pnpm test` + Playwright，覆盖率上传。

---

## 12. 附：测试用例模板（供 AI 复用）

```yaml
id: TC-API-ORD-002
module: order
type: api
priority: P0
preconditions:
  - 用户 U1 已登录
  - SKU S1 库存 = 10
  - 购物车含 S1 数量 2
steps:
  - POST /orders   body: { addressId, couponId: null, remark: '' }
expected:
  http: 200
  body.code: 0
  side_effects:
    - order 表新增 1 行，状态 = PENDING_PAY
    - stock 表 S1 剩余 = 8
    - cart 中 S1 已移除
implementation_path: src/test/java/com/xianguoji/server/module/order/api/OrderApiTest.java
```

> 文档结束。AI 编码时严格按本文编号生成测试类与方法，缺失场景请反馈补充而非自行假设。
