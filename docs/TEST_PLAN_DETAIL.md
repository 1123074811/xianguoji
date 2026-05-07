# 鲜果记 完整测试明细（TEST_PLAN_DETAIL）

> 配套 [TEST_PLAN.md](TEST_PLAN.md) 使用。本文按 **每个接口 / 每个页面 / 每个能力点** 列出全部测试用例，目标：覆盖项目所有功能点，AI 拿来直接编码。
>
> - 接口前缀约定：`/api/pub/**` 公开匿名 / `/api/u/**` 用户 token / `/api/admin/**` 商家或员工 token。
> - 通用断言（**所有接口默认包含**，下文不再重复）：
>   - `A0` 无 token 调 `/api/u/**` 或 `/api/admin/**` → 401
>   - `A1` 用错角色 token（用户 token 调 admin / 反之）→ 403
>   - `A2` 篡改 / 过期 / 黑名单 token → 401
>   - `A3` `Content-Type` 不合法 → 415
>   - `A4` 必填参数缺失或类型错误 → 400 + 字段级错误信息
>   - `A5` 响应统一结构 `{code, msg, data, traceId}`，金额 BigDecimal 字符串化、时间 ISO-8601
>   - `A6` 接口耗时 P95 ≤ 800ms（详情/列表 ≤ 500ms，写接口 ≤ 1500ms）
> - 用例编号：`TC-{LAYER}-{MODULE}-{ENDPOINT}-{SEQ}`，LAYER ∈ UT/API/WS/E2E/FE/PERF/SEC/COMP。
> - 每个用例字段：场景 / 输入 / 预期（HTTP+业务码+副作用）/ 优先级。

---

## 目录

1. 后端接口测试（按 Controller / 端点）
   - 1.1 Auth & Captcha
   - 1.2 User & Address & Footprint/Favorite
   - 1.3 Catalog（C 端 + Admin）
   - 1.4 Cart
   - 1.5 Order（用户 + 管理 + 导出）
   - 1.6 Promo（Coupon / GroupBuy / AdminPromo / PromotionRule）
   - 1.7 Review
   - 1.8 Message & Feedback & Broadcast
   - 1.9 Chat（HTTP + WebSocket）
   - 1.10 Shop / PickupPoint / Delivery / NotifySetting / Notification
   - 1.11 Staff
   - 1.12 Help
   - 1.13 Stat / Customer
   - 1.14 Report
2. 后端单元测试（Service / Util / 状态机 / 并发）
3. 前端单元 & 组件测试（用户端 + 商家端）
4. 端到端测试（E2E）
5. 性能 / 安全 / 兼容性 / 数据迁移 / 灾备
6. 测试夹具与执行规范

---

# 1. 后端接口测试

## 1.1 Auth & Captcha 模块

### 1.1.1 `GET /api/pub/captcha`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-CAP-001 | 正常获取图形验证码 | — | 200，返回 `captchaId + base64 图`；Redis 写入 5min TTL 的 key | P0 |
| TC-API-AUTH-CAP-002 | 同 IP 1s 内 10 次 | 高频 | 第 N 次后 429 | P0 |
| TC-API-AUTH-CAP-003 | 验证码内容字符集 | 多次取样 | 不出现易混淆字符（0/O、1/l/I） | P2 |
| TC-API-AUTH-CAP-004 | 响应不包含明文 code | — | body 无明文，仅服务端 Redis 持有 | P0 |

### 1.1.2 `POST /api/pub/auth/sms/send`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-SMS-001 | 手机号格式错 | `13` | 400 | P0 |
| TC-API-AUTH-SMS-002 | 验证码图未过校验 | 错 captchaCode | 业务码 CAPTCHA_INVALID | P0 |
| TC-API-AUTH-SMS-003 | 正常发送 | 合法手机+图形码 | 200，Redis 存 6 位短信码 5min | P0 |
| TC-API-AUTH-SMS-004 | 同号 60s 内重复 | 同号再次请求 | 业务码 SMS_TOO_FREQUENT | P0 |
| TC-API-AUTH-SMS-005 | 同号 1 天 ≥10 次 | 第 11 次 | 业务码 SMS_DAILY_LIMIT | P0 |
| TC-API-AUTH-SMS-006 | 同 IP 1 小时 ≥30 次 | 多号轮询 | 限流 429 | P0 |
| TC-API-AUTH-SMS-007 | 短信网关失败回滚 | mock 网关 5xx | 业务码 SMS_SEND_FAIL，Redis 不留残留 | P1 |
| TC-API-AUTH-SMS-008 | scene 区分（登录/绑定/重置） | scene=bindPhone | key 命名空间隔离，不互通 | P1 |

### 1.1.3 `POST /api/pub/auth/login/sms`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-SMSL-001 | 短信码错误 | wrong | CAPTCHA_INVALID，Redis 失败计数+1 | P0 |
| TC-API-AUTH-SMSL-002 | 短信码连错 5 次 | — | 短信码失效，要求重发 | P0 |
| TC-API-AUTH-SMSL-003 | 新手机号自动注册 | 未注册 | 200，建用户，返回 token + needCompleteProfile=true | P0 |
| TC-API-AUTH-SMSL-004 | 已注册用户 | 已存在 | 200，token + userInfo | P0 |
| TC-API-AUTH-SMSL-005 | 用户已被封禁 | status=disabled | 业务码 USER_DISABLED | P0 |
| TC-API-AUTH-SMSL-006 | 一次性消费 | 同短信码二次使用 | 拒绝 | P0 |
| TC-API-AUTH-SMSL-007 | token 内容 | — | 含 userId/role/exp，不含手机号原文 | P0 |
| TC-API-AUTH-SMSL-008 | refreshToken 同时下发 | — | 长 TTL，存 Redis | P1 |

### 1.1.4 `POST /api/pub/auth/login/wechat`、`POST /api/pub/auth/login/wechat/quick`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-WX-001 | code 失效 | mock 微信返回 40029 | 业务码 WX_CODE_INVALID | P0 |
| TC-API-AUTH-WX-002 | 首次登录无手机号 | 仅 openid | 200，needBindPhone=true | P0 |
| TC-API-AUTH-WX-003 | 已绑定手机号 | 老用户 | 200，token | P0 |
| TC-API-AUTH-WX-004 | quick 登录（已授权） | 带 unionid 直登 | 200 | P1 |
| TC-API-AUTH-WX-005 | openid 与已存在手机用户合并 | 同手机 + 新 openid | 合并到既有用户，不新建 | P0 |
| TC-API-AUTH-WX-006 | 微信网关超时 | 5s | 网关错误码 + 不创建脏用户 | P1 |

### 1.1.5 `POST /api/pub/auth/refresh`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-RFR-001 | 正常刷新 | 有效 refresh | 新 access + 新 refresh（旋转） | P0 |
| TC-API-AUTH-RFR-002 | refresh 过期 | 过期 | 401 | P0 |
| TC-API-AUTH-RFR-003 | refresh 已被吊销 | 用过的旧 refresh | 401 + 风控告警 | P0 |
| TC-API-AUTH-RFR-004 | 并发刷新 | 同 token 并发两次 | 仅一次成功 | P1 |

### 1.1.6 `POST /api/u/auth/logout`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-LO-001 | 正常登出 | — | 200，token 加入黑名单至原 exp | P0 |
| TC-API-AUTH-LO-002 | 登出后再用 | 同 token | 401 | P0 |
| TC-API-AUTH-LO-003 | refresh 同步失效 | — | refresh 也吊销 | P0 |

### 1.1.7 商家登录与重置 `POST /api/pub/admin/login`、`POST /api/pub/admin/reset-password`、`POST /api/admin/auth/logout`、`GET /api/admin/auth/me`
| 编号 | 场景 | 输入 | 预期 | P |
|---|---|---|---|---|
| TC-API-AUTH-ADM-001 | 账号不存在 | — | 业务码 ACCOUNT_NOT_FOUND（防枚举可统一返回 INVALID_CREDENTIALS） | P0 |
| TC-API-AUTH-ADM-002 | 密码错误 | — | INVALID_CREDENTIALS，失败计数+1 | P0 |
| TC-API-AUTH-ADM-003 | 错 5 次锁 30min | — | 业务码 ACCOUNT_LOCKED | P0 |
| TC-API-AUTH-ADM-004 | 正常登录 | — | 200，token，role ∈ {OWNER, MANAGER, CLERK} | P0 |
| TC-API-AUTH-ADM-005 | reset-password 缺验证 | 仅 newPassword | 必须邮箱/短信验证 | P0 |
| TC-API-AUTH-ADM-006 | 密码强度不足 | 6 位纯数字 | 拒绝 | P0 |
| TC-API-AUTH-ADM-007 | 同密码复用 | 与历史密码相同 | 拒绝（最近 3 次） | P1 |
| TC-API-AUTH-ADM-008 | logout 黑名单 | — | 同上用户黑名单 | P0 |
| TC-API-AUTH-ADM-009 | /me 返回 | — | 含 staffId/shopId/role/permissions[]，不含密码哈希 | P0 |

---

## 1.2 User / Address / Footprint / Favorite

### 1.2.1 `GET /api/u/user/profile`
| TC-API-USR-PRO-001 | 正常 | — | 返回 nickname、avatar、gender、birthday、phone（脱敏 138****1234）、createdAt | P0 |
| TC-API-USR-PRO-002 | 已注销账户 token | — | 401 | P1 |

### 1.2.2 `PUT /api/u/user/profile`
| TC-API-USR-PRO-003 | 昵称含敏感词 | "***" | 拒绝 | P1 |
| TC-API-USR-PRO-004 | 头像非白名单域 | `http://evil.com/x.jpg` | 拒绝 | P1 |
| TC-API-USR-PRO-005 | 生日 > 今天 | 2099-1-1 | 400 | P2 |
| TC-API-USR-PRO-006 | 字段长度边界 | nickname 32 字 vs 33 字 | 32 通过、33 拒绝 | P1 |
| TC-API-USR-PRO-007 | 仅修改部分字段 | 仅 nickname | 其它保留 | P1 |

### 1.2.3 `POST /api/u/user/bind-phone`
| TC-API-USR-BPH-001 | 短信码错 | — | CAPTCHA_INVALID | P0 |
| TC-API-USR-BPH-002 | 手机号已被他人占用 | — | PHONE_OCCUPIED；不覆盖 | P0 |
| TC-API-USR-BPH-003 | 同手机号重复绑定 | 已是当前用户的手机号 | 幂等 200 | P1 |
| TC-API-USR-BPH-004 | 跨账户合并触发 | 微信用户绑定一个已存在手机号用户 | 走合并策略并审计日志 | P0 |

### 1.2.4 地址 `GET/POST /api/u/address`、`PUT/DELETE /api/u/address/{id}`、`PUT /api/u/address/{id}/default`
| TC-API-USR-ADR-001 | 列表分页/排序 | — | 默认地址置顶，按 updatedAt desc | P1 |
| TC-API-USR-ADR-002 | 新增首条自动置默认 | — | isDefault=true | P0 |
| TC-API-USR-ADR-003 | 超过上限 20 条 | 第 21 条 | 业务码 ADDRESS_LIMIT | P1 |
| TC-API-USR-ADR-004 | 修改他人地址（IDOR） | id 属于他人 | 403/404 | P0 |
| TC-API-USR-ADR-005 | 删除默认地址 | — | 自动选 updatedAt 最新一条置默认 | P1 |
| TC-API-USR-ADR-006 | 设置默认 | 切换 | 旧默认置 false，事务一致 | P0 |
| TC-API-USR-ADR-007 | 字段校验 | 手机非法、收件人空、详细地址>120 | 400 | P1 |
| TC-API-USR-ADR-008 | 经纬度可空 | — | 允许 | P2 |

### 1.2.5 足迹 `GET /api/u/footprint/page`、`POST /api/u/footprint/{productId}`、`DELETE /api/u/footprint`
| TC-API-USR-FP-001 | 写入幂等 | 同商品 1h 多次 | 仅更新 viewedAt，不增条目 | P1 |
| TC-API-USR-FP-002 | 商品已下架 | — | 列表标记 unavailable | P2 |
| TC-API-USR-FP-003 | 批量删除 | ids[] | 仅删自己的 | P0（越权） |
| TC-API-USR-FP-004 | 清空 | ids 为空 | 清空当前用户 | P1 |
| TC-API-USR-FP-005 | 分页 | size=20, total=55 | 三页正确 | P1 |

### 1.2.6 收藏 `GET /api/u/favorite/page`、`POST/DELETE /api/u/favorite/{productId}`
| TC-API-USR-FV-001 | 重复收藏 | 同商品二次 | 幂等，不重复 | P1 |
| TC-API-USR-FV-002 | 取消收藏 | 未收藏过 | 200 幂等 | P2 |
| TC-API-USR-FV-003 | 收藏商品已下架 | 列表显示 | 标记不可购 | P2 |
| TC-API-USR-FV-004 | 收藏数量上限 | 1000+ | 性能仍 ≤ 500ms | P2 |

---

## 1.3 Catalog（C 端 + 管理端）

### 1.3.1 `GET /api/pub/category/tree`
| TC-API-CAT-TREE-001 | 仅显示启用分类 | — | enabled=true 的递归树 | P0 |
| TC-API-CAT-TREE-002 | 排序 | — | 按 sort asc | P1 |
| TC-API-CAT-TREE-003 | 缓存命中 | 二次调用 | 命中 Caffeine + Redis L2 | P1 |
| TC-API-CAT-TREE-004 | 缓存失效 | Admin 改分类后 | 主动 evict，下次取新 | P0 |

### 1.3.2 `GET /api/pub/banner/list`、`/hot-search/list`
| TC-API-CAT-BNR-001 | 时间窗 | startAt/endAt 过滤 | 仅生效中 | P0 |
| TC-API-CAT-BNR-002 | 排序、置顶 | — | sort asc | P1 |
| TC-API-CAT-HOT-001 | 默认 10 条 | — | 长度 ≤10，按热度 | P1 |

### 1.3.3 `GET /api/pub/product/page`
| TC-API-CAT-PRD-001 | 分类筛 | categoryId | 仅含该分类 | P0 |
| TC-API-CAT-PRD-002 | 关键字 | keyword | 模糊匹配 name/tag | P0 |
| TC-API-CAT-PRD-003 | 价格区间 | minPrice/maxPrice | 含端点 | P1 |
| TC-API-CAT-PRD-004 | 排序 | sort=salesDesc / priceAsc / newest | 各自正确 | P1 |
| TC-API-CAT-PRD-005 | 仅显示上架 | status=ON | offline 不出现 | P0 |
| TC-API-CAT-PRD-006 | 分页 | page/size 边界、size>100 | 截断 100 | P1 |
| TC-API-CAT-PRD-007 | SQL 注入尝试 | keyword `'OR 1=1--` | 安全转义 | P0 |
| TC-API-CAT-PRD-008 | 性能 | 1000 并发 | P95 < 500ms | P0 |

### 1.3.4 `GET /api/pub/product/recommend`
| TC-API-CAT-REC-001 | 默认 N | — | 长度=配置值 | P1 |
| TC-API-CAT-REC-002 | 已下架不推 | — | 过滤 | P1 |

### 1.3.5 `GET /api/pub/product/{id}`
| TC-API-CAT-DTL-001 | 不存在 | id=0 | GOODS_NOT_FOUND | P0 |
| TC-API-CAT-DTL-002 | 已下架商品 | — | 仍可访问但 status 标记 OFFLINE，不可加购 | P0 |
| TC-API-CAT-DTL-003 | 含规格/SKU/库存/折扣 | — | 字段齐全 | P0 |
| TC-API-CAT-DTL-004 | 富文本 XSS 防御 | 详情含 `<script>` | 输出转义/白名单 | P0 |
| TC-API-CAT-DTL-005 | 缓存 + 库存实时性 | — | 详情缓存命中但库存接近实时 | P1 |

### 1.3.6 搜索历史 `GET /api/u/search/history`、`DELETE`、`POST /api/u/search/record`
| TC-API-CAT-SH-001 | 记录上限 20 | 超过 | 滚动淘汰最早 | P1 |
| TC-API-CAT-SH-002 | 去重 | 重复关键字 | 提到顶 | P1 |
| TC-API-CAT-SH-003 | 清空 | — | 仅当前用户 | P1 |
| TC-API-CAT-SH-004 | 关键字脱敏存储 | — | 不存超长内容（限 32） | P2 |

### 1.3.7 Admin 分类 `GET/POST/PUT/DELETE /api/admin/category[*]`
| TC-API-CAT-ADM-001 | 普通员工无权限 | role=CLERK | 403 | P0 |
| TC-API-CAT-ADM-002 | 删除含商品的分类 | 有商品 | 拒绝并返回数量 | P0 |
| TC-API-CAT-ADM-003 | 父子层级 | 自己作父级 | 拒绝循环 | P1 |
| TC-API-CAT-ADM-004 | 排序更新 | sort | 列表顺序变化 | P2 |
| TC-API-CAT-ADM-005 | 名称重复 | 同父级下重名 | 拒绝 | P1 |

### 1.3.8 Admin 商品 `GET /admin/product/page`、`POST /admin/product`、`PUT /admin/product/{id}`、`PUT /admin/product/{id}/status`、`POST /admin/product/{id}/copy`
| TC-API-CAT-ADM-006 | 列表筛选 | status/keyword/categoryId | 多条件 AND | P0 |
| TC-API-CAT-ADM-007 | 创建必填校验 | 缺 categoryId/price | 400 | P0 |
| TC-API-CAT-ADM-008 | 价格 ≤ 0 | -1 | 拒绝 | P0 |
| TC-API-CAT-ADM-009 | 库存负数 | -1 | 拒绝 | P0 |
| TC-API-CAT-ADM-010 | SKU 列表 | 含规格 | 价格/库存按 SKU 维度落库 | P0 |
| TC-API-CAT-ADM-011 | 上下架 | status toggle | C 端列表实时反映（缓存 evict） | P0 |
| TC-API-CAT-ADM-012 | 复制商品 | — | 名字加 "(副本)"，状态 OFFLINE，SKU 复制完整 | P1 |
| TC-API-CAT-ADM-013 | 改价审计 | — | 写商品价格变更日志 | P1 |
| TC-API-CAT-ADM-014 | 富文本图片白名单域 | externalsrc | 拒绝/重写 | P1 |
| TC-API-CAT-ADM-015 | 并发上下架 | 同 id 两请求 | 最终状态确定 | P1 |

### 1.3.9 Banner / HotSearch（Admin）
| TC-API-CAT-ADM-016 | 创建 banner 缺图 | — | 400 | P1 |
| TC-API-CAT-ADM-017 | banner 时间区间不合法 | start>end | 400 | P1 |
| TC-API-CAT-ADM-018 | 删除 banner 后 C 端立即消失 | — | 缓存失效 | P0 |
| TC-API-CAT-ADM-019 | 热搜词重复 | 同词 | 拒绝 | P2 |

---

## 1.4 Cart `/api/u/cart/*`

### 1.4.1 `GET /list`
| TC-API-CART-001 | 空购物车 | — | items=[]，summary=0 | P0 |
| TC-API-CART-002 | 含已下架 | — | 标记 unavailable=true，不计入合计 | P0 |
| TC-API-CART-003 | 价格变化 | 加购后涨价 | 标记 priceChanged=true，使用最新价 | P0 |
| TC-API-CART-004 | 库存变化 | 库存 < 选购数量 | 标记 stockShortage，可购数=stock | P0 |
| TC-API-CART-005 | 选中合计 | selected | summary 仅累加选中 | P0 |

### 1.4.2 `POST /` 加购、`PUT /{id}/quantity`、`PUT /selected`、`DELETE /{id}`、`DELETE /clear`、`GET /count`
| TC-API-CART-ADD-001 | 同 SKU 累加 | 已有 2 + 加 3 | qty=5 | P0 |
| TC-API-CART-ADD-002 | 数量超 SKU 限购 | 限购 3，加 5 | 截断到 3 + 提示 | P0 |
| TC-API-CART-ADD-003 | SKU 已下架/不存在 | — | 拒绝 | P0 |
| TC-API-CART-ADD-004 | 数量 ≤ 0 | 0 / -1 | 400 | P0 |
| TC-API-CART-QTY-001 | 改 0 等同删除？ | 0 | 按规格定义（推荐拒绝并要求 DELETE） | P1 |
| TC-API-CART-SEL-001 | 全选/反选/部分 | 全选 ids | 全部 selected=true | P1 |
| TC-API-CART-DEL-001 | 删除非己 | id 属于他人 | 403/404 | P0 |
| TC-API-CART-CLR-001 | 清空 | — | 当前用户全删 | P1 |
| TC-API-CART-CNT-001 | 红点数量 | — | 总件数（数量累加） | P1 |
| TC-API-CART-PERF | 100 商品购物车 | — | list ≤ 400ms | P1 |

---

## 1.5 Order

### 1.5.1 `POST /api/u/order/preview`
| TC-API-ORD-PREV-001 | 全选购物车 + 默认地址 + 自动最优券 | — | 返回商品行 / 运费 / 优惠 / 实付 | P0 |
| TC-API-ORD-PREV-002 | 选定 couponId 不可用 | 不满门槛 | 业务码 COUPON_UNUSABLE | P0 |
| TC-API-ORD-PREV-003 | 含已下架/库存不足 | — | unavailable 列表，不影响其它 | P0 |
| TC-API-ORD-PREV-004 | 自提 vs 配送 | type=PICKUP/DELIVERY | 运费规则正确（自提为 0） | P0 |
| TC-API-ORD-PREV-005 | 拼团模式 | groupBuyId | 单价取拼团价 | P0 |
| TC-API-ORD-PREV-006 | 满减叠加规则 | 多条 promotionRule | 与 PromotionRuleEngine 单测一致 | P0 |
| TC-API-ORD-PREV-007 | 配送范围外地址 | — | 拒绝并提示 | P0 |
| TC-API-ORD-PREV-008 | 营业时间外 | 商家 closed | 业务码 SHOP_CLOSED | P0 |
| TC-API-ORD-PREV-009 | 金额计算精度 | 0.1*3 | BigDecimal 精确 | P0 |

### 1.5.2 `POST /api/u/order/submit`
| TC-API-ORD-SBM-001 | 正常下单 | 与 preview 一致 | 200，订单号生成，库存扣减、券核销、购物车移除选中、新订单消息推商家 | P0 |
| TC-API-ORD-SBM-002 | 幂等键重复 | 同 idempotencyKey | 返回首次结果 | P0 |
| TC-API-ORD-SBM-003 | 提交时金额与 preview 不一致（前端篡改） | 改 totalAmount | 后端重算并以服务端为准 | P0 |
| TC-API-ORD-SBM-004 | 库存被并发抢光 | 100 并发，库存 50 | 50 成功 + 50 失败，无超卖 | P0 |
| TC-API-ORD-SBM-005 | 用券并发 | 同券并发使用 | 仅 1 次成功 | P0 |
| TC-API-ORD-SBM-006 | 配送到非范围 | — | 拒绝 | P0 |
| TC-API-ORD-SBM-007 | 自提点已停用 | — | 拒绝 | P1 |
| TC-API-ORD-SBM-008 | 订单超时未支付 | 15min | 自动取消、回滚库存与券 | P0 |
| TC-API-ORD-SBM-009 | 备注 / 期望送达时间 | 含特殊字符 | 转义存储 | P1 |

### 1.5.3 `POST /api/u/order/{orderNo}/pay`
| TC-API-ORD-PAY-001 | 状态机：未支付→支付中 | — | 200，返回支付参数 | P0 |
| TC-API-ORD-PAY-002 | 已支付重复支付 | — | 拒绝 | P0 |
| TC-API-ORD-PAY-003 | 已取消订单支付 | — | 拒绝 | P0 |
| TC-API-ORD-PAY-004 | 订单非己 | — | 403 | P0 |
| TC-API-ORD-PAY-005 | 微信支付回调成功 | mock notify | 状态→已支付，触发新订单通知给商家 WS | P0 |
| TC-API-ORD-PAY-006 | 回调签名错误 | — | 拒绝 | P0 |
| TC-API-ORD-PAY-007 | 回调重放 | 同 transaction_id 二次 | 幂等 | P0 |

### 1.5.4 `GET /api/u/order/page`、`GET /api/u/order/{orderNo}`
| TC-API-ORD-LST-001 | 状态筛选 | status=ALL/PENDING_PAY/PENDING_SHIP/SHIPPED/COMPLETED/CANCELED/REFUNDING/REFUNDED | 各分类正确 | P0 |
| TC-API-ORD-LST-002 | 分页 | — | OK | P1 |
| TC-API-ORD-LST-003 | 详情非己 | — | 403/404 | P0 |
| TC-API-ORD-LST-004 | 详情字段 | — | 含商品行、地址、券、退款、物流、时间轴 | P0 |
| TC-API-ORD-LST-005 | 时间轴顺序 | — | 严格升序 | P1 |

### 1.5.5 取消 / 确认 / 提醒 / 再来一单
| TC-API-ORD-CAN-001 | 未支付取消 | — | 释放库存/券 | P0 |
| TC-API-ORD-CAN-002 | 已支付未发货取消 | — | 进入退款审核 | P0 |
| TC-API-ORD-CAN-003 | 已发货取消 | — | 拒绝 | P0 |
| TC-API-ORD-CFM-001 | 已发货确认收货 | — | 状态→COMPLETED，可评价 | P0 |
| TC-API-ORD-CFM-002 | 自动收货定时 | 14 天 | 自动 COMPLETED | P1 |
| TC-API-ORD-RMD-001 | 提醒发货 | 同单 5min 内多次 | 限频，仅推送一次 | P1 |
| TC-API-ORD-REP-001 | 再来一单 | 部分商品下架 | 跳过下架并提示 | P1 |

### 1.5.6 退款 `POST /api/u/refund`、`GET /api/u/refund/{refundNo}`
| TC-API-ORD-RF-001 | 仅退款（未发货）/ 退货退款（已发货） | type | 状态机分支正确 | P0 |
| TC-API-ORD-RF-002 | 上传图片 ≤ 9 张 | 10 | 拒绝 | P1 |
| TC-API-ORD-RF-003 | 同单重复申请 | 进行中再申 | 拒绝 | P0 |
| TC-API-ORD-RF-004 | 退款金额 > 实付 | — | 拒绝 | P0 |
| TC-API-ORD-RF-005 | 部分退（按行） | 商品行金额 | 总额≤行小计 | P1 |
| TC-API-ORD-RF-006 | 商家拒绝后用户再申 | — | 允许 | P1 |

### 1.5.7 Admin 订单
| TC-API-ORD-ADM-001 | 列表筛选 | 多条件 | OK | P0 |
| TC-API-ORD-ADM-002 | accept/reject | — | 状态机正确 | P0 |
| TC-API-ORD-ADM-003 | ship 必填物流单号或自配送 | 缺单号 + 配送=快递 | 400 | P0 |
| TC-API-ORD-ADM-004 | pickup-verify | 用户错误核销码 | 拒绝 | P0 |
| TC-API-ORD-ADM-005 | complete 已完成 | — | 拒绝 | P1 |
| TC-API-ORD-ADM-006 | print 小票 | — | 返回小票数据/打印任务入队 | P2 |
| TC-API-ORD-ADM-007 | new-count | 待处理订单数 | 与 SQL 计数一致 | P1 |
| TC-API-ORD-ADM-008 | refund 同意 | — | 调三方退款 + 状态→REFUNDED + 站内信 | P0 |
| TC-API-ORD-ADM-009 | refund 拒绝 | — | 状态→拒绝 + 备注 | P0 |
| TC-API-ORD-ADM-010 | 越权（CLERK 退款） | — | 403 | P0 |

### 1.5.8 `GET /api/admin/order/export`
| TC-API-ORD-EXP-001 | 区间 + 状态筛 | 1 万行 | 200，xlsx 流式，不 OOM | P1 |
| TC-API-ORD-EXP-002 | 100 万行 | — | 不超时（10 min），文件可用 | P1 |
| TC-API-ORD-EXP-003 | 字段齐全 | — | 订单号/用户/手机脱敏/金额/状态/时间/物流/SKU 列表 | P1 |

---

## 1.6 Promo

### 1.6.1 优惠券（C 端）
- `GET /api/pub/coupon/list`
- `POST /api/u/coupon/{id}/receive`
- `GET /api/u/coupon/list`
- `POST /api/u/coupon/usable`

| TC-API-PRM-CP-001 | 公开列表仅显示可领 | — | enabled + remain>0 + within time | P0 |
| TC-API-PRM-CP-002 | 领取限领 1 张 | 二次领 | COUPON_LIMIT_REACHED | P0 |
| TC-API-PRM-CP-003 | 抢券 1000 并发 100 张 | — | 恰好 100 张发出 | P0 |
| TC-API-PRM-CP-004 | 我的列表 | filter=usable/used/expired | 三类正确 | P1 |
| TC-API-PRM-CP-005 | usable（结算可用） | 入参订单金额 + 商品列表 | 仅返回适配券 | P0 |
| TC-API-PRM-CP-006 | 过期券不可用 | — | 不返回 | P1 |

### 1.6.2 拼团
- `GET /api/pub/group-buy/page`
- `GET /api/pub/group-buy/by-product/{productId}`
- `GET /api/pub/group-buy/share/{shareCode}`
- `POST /api/u/group-buy/launch`
- `POST /api/u/group-buy/{instanceId}/join`
- `GET /api/u/group-buy/{instanceId}`

| TC-API-PRM-GB-001 | 列表仅含进行中 | — | OK | P0 |
| TC-API-PRM-GB-002 | 分享码无效 | 错码 | SHARE_CODE_INVALID | P0 |
| TC-API-PRM-GB-003 | 开团成功 | — | 创建 instance + 生成 shareCode + 占位订单（待支付） | P0 |
| TC-API-PRM-GB-004 | 重复开团（同活动同用户进行中） | — | 拒绝/复用 | P1 |
| TC-API-PRM-GB-005 | 参团已满 | size=N，再 join | GROUP_FULL | P0 |
| TC-API-PRM-GB-006 | 参团已过期 | — | GROUP_EXPIRED | P0 |
| TC-API-PRM-GB-007 | 同用户重复参团 | — | 拒绝 | P0 |
| TC-API-PRM-GB-008 | 自动成团 | 满员瞬间 | 状态 SUCCESS，所有占位订单转入 PENDING_SHIP | P0 |
| TC-API-PRM-GB-009 | 到期未成团（cron） | — | 状态 FAIL，全员退款 + 通知 | P0 |
| TC-API-PRM-GB-010 | 详情 | — | 含成员头像/倒计时/差几人 | P1 |

### 1.6.3 Admin 优惠券 `/api/admin/coupon/*`
| TC-API-PRM-ADM-001 | 创建：金额/折扣类型 | 无门槛/满减/折扣 | 各类型存储正确 | P0 |
| TC-API-PRM-ADM-002 | 适用商品/分类范围 | 全场/部分 | 持久化并参与 usable 计算 | P0 |
| TC-API-PRM-ADM-003 | 删除已被领取的券 | 有占用 | 软删除，不撤销已领 | P0 |
| TC-API-PRM-ADM-004 | stats / status-counts | — | 与 SQL 实时一致 | P1 |
| TC-API-PRM-ADM-005 | 改券改适用范围 | 已发出 | 仅影响新领的 / 或全量？需明确策略并断言 | P0 |

### 1.6.4 Admin 满减规则 `/api/admin/promotion-rule/*`
| TC-API-PRM-ADM-006 | 多档满减 | 满 100-10、200-30 | 引擎正确选最优 | P0 |
| TC-API-PRM-ADM-007 | 与优惠券叠加策略 | 同时使用 | 按规则（建议券后再算满减或仅一项） | P0 |
| TC-API-PRM-ADM-008 | 时间窗 | start>end | 拒绝 | P1 |

### 1.6.5 Admin 拼团 `/api/admin/group-buy/*`
| TC-API-PRM-ADM-009 | 创建拼团 | 起团人数/价/有效时长 | 入库 | P0 |
| TC-API-PRM-ADM-010 | 商品已绑定其它进行中拼团 | — | 拒绝 | P1 |
| TC-API-PRM-ADM-011 | 删除进行中拼团 | 有 instance | 拒绝 | P0 |
| TC-API-PRM-ADM-012 | stats | — | 与实时一致 | P1 |

---

## 1.7 Review

### 1.7.1 用户侧 `GET /api/u/review/pending`、`/my`、`POST /api/u/review`
| TC-API-REV-001 | 待评列表 | 已收货未评价订单行 | OK | P0 |
| TC-API-REV-002 | 提交评价 | rating 1-5 / 文字 / 图≤9 / 视频 | 入库 + 反写订单已评 | P0 |
| TC-API-REV-003 | 同行重复评 | — | 拒绝 | P0 |
| TC-API-REV-004 | 评价超时（确认收货 30 天后）| — | 拒绝 | P1 |
| TC-API-REV-005 | 内容含违禁词（mock 审核） | — | 进入待审或拒绝 | P1 |
| TC-API-REV-006 | rating 越界 | 0 / 6 | 400 | P0 |
| TC-API-REV-007 | 仅评分不评文字 | — | 允许 | P1 |
| TC-API-REV-008 | 我的评价分页 | — | OK | P2 |

### 1.7.2 公开 `/api/pub/review/product/{id}` + `summary`
| TC-API-REV-009 | 列表筛选 | hasImage / rating | 正确 | P1 |
| TC-API-REV-010 | summary 含分布 | — | 平均分 + 1-5 星数 | P1 |
| TC-API-REV-011 | 隐藏的评价 | hidden=true | 不出现 | P0 |

### 1.7.3 Admin `/api/admin/review/page`、`/{id}/reply`、`/{id}/hidden`
| TC-API-REV-012 | 回复评价 | — | reply 字段写入并向用户推送通知 | P1 |
| TC-API-REV-013 | 显隐切换 | hidden | C 端联动 | P0 |
| TC-API-REV-014 | 越权（CLERK 隐藏） | — | 403 | P0 |

---

## 1.8 Message / Feedback / Broadcast

### 1.8.1 用户侧 `GET /api/u/message/page`、`PUT /{id}/read`、`PUT /read-all`、`GET /unread-count`
| TC-API-MSG-001 | 列表分类 | type=order/promo/system | 各分支 | P0 |
| TC-API-MSG-002 | 未读数 | — | 与 SQL 一致 | P0 |
| TC-API-MSG-003 | 单条已读 | 非己 id | 403 | P0 |
| TC-API-MSG-004 | 全部已读 | — | 仅当前用户 | P1 |

### 1.8.2 反馈 `POST /api/u/feedback`、Admin `/api/admin/feedback/page`、`/{id}/handle`
| TC-API-MSG-FB-001 | 文本+图片提交 | OSS URL 列表 | 入库 | P0 |
| TC-API-MSG-FB-002 | 图片非白名单域 | 外部 URL | 拒绝 | P0 |
| TC-API-MSG-FB-003 | 频控 | 1min 5 次 | 第 6 次 429 | P1 |
| TC-API-MSG-FB-004 | Admin 列表筛 | status/type | OK | P1 |
| TC-API-MSG-FB-005 | handle 状态机 | NEW→HANDLING→DONE | 不可回退 | P1 |

### 1.8.3 `POST /api/admin/message/broadcast`
| TC-API-MSG-BC-001 | 全员广播 | 100w 用户 | 异步派发 + 不阻塞接口 | P1 |
| TC-API-MSG-BC-002 | 定向（标签/最近活跃） | 条件 | 命中范围正确 | P1 |
| TC-API-MSG-BC-003 | 仅 OWNER 可发 | — | 其他角色 403 | P0 |
| TC-API-MSG-BC-004 | 内容 XSS | `<script>` | 转义 | P0 |

---

## 1.9 Chat（HTTP + WebSocket）

### 1.9.1 HTTP
- `POST /api/u/chat/send`
- `GET /api/u/chat/messages`
- `GET /api/u/chat/unread-count`
- `PUT /api/u/chat/read-all`
- `GET /api/admin/chat/users`
- `GET /api/admin/chat/messages`
- `POST /api/admin/chat/reply`

| TC-API-CHAT-001 | 用户发送文字 | — | 入库，触发 WS 推送给在线客服 | P0 |
| TC-API-CHAT-002 | 内容长度 ≤ 1000 | 1001 | 400 | P1 |
| TC-API-CHAT-003 | 图片消息（OSS URL） | — | URL 校验 + 通过 | P1 |
| TC-API-CHAT-004 | 同会话历史分页 | beforeMsgId | 倒序游标分页 | P1 |
| TC-API-CHAT-005 | unread-count | — | 正确 | P1 |
| TC-API-CHAT-006 | read-all 仅当前用户 | — | OK | P1 |
| TC-API-CHAT-007 | senderAvatar：用户消息 | senderType=0，用户有 avatar | ChatMessageVO.senderAvatar = 用户 avatar URL | P0 |
| TC-API-CHAT-008 | senderAvatar：用户无头像 | senderType=0，用户 avatar=null | ChatMessageVO.senderAvatar = null | P1 |
| TC-API-CHAT-009 | senderAvatar：商家消息 | senderType=1，Staff 有 avatar | ChatMessageVO.senderAvatar = Staff avatar URL（通过 createBy 查 staff） | P0 |
| TC-API-CHAT-010 | senderAvatar：商家无头像 | senderType=1，Staff avatar=null | ChatMessageVO.senderAvatar = null | P1 |
| TC-API-CHAT-ADM-001 | 客服会话列表 | — | 按最近活跃倒序 | P1 |
| TC-API-CHAT-ADM-002 | 客服回复用户 | userId | 推送 WS + 入库 | P0 |
| TC-API-CHAT-ADM-003 | 客服越权回复别 shop | — | 拒绝 | P0 |

### 1.9.2 WebSocket（`AdminWebSocketHandler` + `WebSocketAuthInterceptor`）
| TC-WS-001 | 握手无 token | — | 401 关闭 | P0 |
| TC-WS-002 | 用户连接拿到自己消息 | A→B | B 在线收到 | P0 |
| TC-WS-003 | 离线消息 | B 不在线 | 入库未读，B 上线下发 | P0 |
| TC-WS-004 | 多端 | 同用户 2 连接 | 双端同步 | P1 |
| TC-WS-005 | 心跳 | ping/pong 30s | 缺失关闭 | P1 |
| TC-WS-006 | 顺序保证 | 同会话连发 10 条 | 顺序到达 | P1 |
| TC-WS-007 | 客户端重连 | 断线重连 | 拉取断线期间消息 | P1 |
| TC-WS-008 | 鉴权失效（token 过期）| — | 服务端关闭并要求重连 | P0 |
| TC-WS-009 | 推送范围 | 跨 shop 推送 | 隔离 | P0 |
| TC-WS-010 | 大量连接稳定性 | 5000 长连 | 内存不泄漏 | P1 |

---

## 1.10 Shop / PickupPoint / Delivery / NotifySetting / Notification

### 1.10.1 公开 `/api/pub/shop/info`、`/pickup-point/list`、`/pickup-point/geo`、`/delivery-setting`
| TC-API-SHP-PUB-001 | 店铺基本信息 | — | name/logo/营业时间/公告 | P0 |
| TC-API-SHP-PUB-002 | 自提点列表 | — | 仅启用 | P0 |
| TC-API-SHP-PUB-003 | geo 距离排序 | lng/lat | 升序 + km 字段 | P1 |
| TC-API-SHP-PUB-004 | 配送设置 | — | 起送/配送费/范围 | P0 |

### 1.10.2 Admin 店铺 / 自提点 / 配送
| TC-API-SHP-ADM-001 | 改店铺资料 | 营业时间格式 | 校验 HH:mm-HH:mm | P0 |
| TC-API-SHP-ADM-002 | 切换营业 | open/close | C 端立即可见 | P0 |
| TC-API-SHP-ADM-003 | 自提点 CRUD | — | 经纬度必填 | P1 |
| TC-API-SHP-ADM-004 | 删除有未完成订单的自提点 | — | 拒绝/置停用 | P0 |
| TC-API-SHP-ADM-005 | 配送设置改起送 | — | 校验非负 | P1 |

### 1.10.3 通知设置 `/api/admin/notify-setting/*`
| TC-API-SHP-ADM-006 | 列表 | — | 含微信/短信/站内三通道开关 | P1 |
| TC-API-SHP-ADM-007 | 改通道 | enabled=false | 后续不发该通道 | P1 |

### 1.10.4 通知中心 `/api/admin/notification/*`
| TC-API-SHP-ADM-008 | 列表分页 | — | OK | P1 |
| TC-API-SHP-ADM-009 | 单条已读 / 全部已读 / 未读数 | — | OK | P1 |
| TC-API-SHP-ADM-010 | 系统通知（OWNER 发布） | broadcast 系统级 | 全员可见 | P1 |
| TC-API-SHP-ADM-011 | 隔离 shop | 仅本店 | 不互通 | P0 |

---

## 1.11 Staff `/api/admin/staff/*`

| TC-API-STF-001 | /me/security | — | 含登录历史 / 二次认证状态 | P1 |
| TC-API-STF-002 | 员工分页 | — | 仅本店 | P0 |
| TC-API-STF-003 | 修改员工 | role 升降 | 仅 OWNER 可改 OWNER；MANAGER 不可创建 OWNER | P0 |
| TC-API-STF-004 | 删除员工 | — | 软删，token 立即失效 | P0 |
| TC-API-STF-005 | 重置员工密码 | — | 强密码 + 通知员工 | P1 |
| TC-API-STF-006 | 改自己密码 | 旧密错 | 拒绝 | P0 |
| TC-API-STF-007 | 改自己密码成功 | — | 强制重新登录其它端 | P1 |
| TC-API-STF-008 | 越权（CLERK 删 MANAGER） | — | 403 | P0 |

---

## 1.12 Help `/api/admin/help/*`

| TC-API-HLP-001 | FAQ 列表 | — | OK | P2 |
| TC-API-HLP-002 | 指南列表 | — | OK | P2 |
| TC-API-HLP-003 | 商家提交反馈 `/feedback` | — | 入库 + 通知 | P1 |
| TC-API-HLP-004 | 限频 | 1min 多次 | 429 | P2 |

---

## 1.13 Stat & Customer

### 1.13.1 Stat（仪表盘 / 业务分析）
- `GET /api/admin/stat/dashboard`
- `GET /api/admin/stat/order-trend`
- `GET /api/admin/stat/order-status`
- `GET /api/admin/stat/top-products`
- `GET /api/admin/stat/todo`
- `GET /api/admin/stat/analysis/kpi`
- `GET /api/admin/stat/analysis/revenue-trend`
- `GET /api/admin/stat/analysis/category-distribution`
- `GET /api/admin/stat/analysis/customer-segments`
- `GET /api/admin/stat/analysis/product-performance`
- `GET /api/admin/stat/analysis/hourly-heatmap`

| TC-API-STAT-001 | dashboard 数字 | 今日订单/营业额/待处理 | 与 SQL 直查一致 | P0 |
| TC-API-STAT-002 | order-trend 7d/30d/自定义 | start/end | 区间 + 粒度（day/hour）正确 | P0 |
| TC-API-STAT-003 | top-products | size=10 | 排序按销量 desc | P1 |
| TC-API-STAT-004 | todo | 待处理订单/退款/低库存 | 数量准确 | P1 |
| TC-API-STAT-005 | KPI 同环比 | — | 公式正确：(本-上)/上 | P0 |
| TC-API-STAT-006 | revenue-trend | 时间格式 | 无空洞日期填 0 | P1 |
| TC-API-STAT-007 | category-distribution | 占比之和=100% | 浮点容差 | P1 |
| TC-API-STAT-008 | customer-segments | 新客/老客/沉睡 | 阈值与 SQL 一致 | P1 |
| TC-API-STAT-009 | product-performance | 排序、转化率 | 公式正确 | P1 |
| TC-API-STAT-010 | hourly-heatmap | 24×7 | 矩阵尺寸正确 | P1 |
| TC-API-STAT-011 | 越权（CLERK 看分析） | — | 403 | P0 |
| TC-API-STAT-012 | 区间过大 | 365d | 服务端限制最大区间或聚合粒度切换 | P1 |
| TC-API-STAT-013 | SQL 注入 | sort 字段 | 白名单 | P0 |

### 1.13.2 Customer `/api/admin/customer/*`
| TC-API-CUS-001 | 分页 + 多筛 | 名/手机/标签 | OK | P1 |
| TC-API-CUS-002 | 详情 | id | 含消费汇总、最近订单、收货地址 | P1 |
| TC-API-CUS-003 | 发券 | couponId | 写一条 user_coupon | P1 |
| TC-API-CUS-004 | 不存在用户 | — | 404 | P2 |
| TC-API-CUS-005 | 隐私脱敏 | 手机号 | 列表脱敏，详情可解（按权限） | P0 |

---

## 1.14 Report `/api/admin/report/*`

| TC-API-RPT-001 | 提交导出任务 | type/range | 200，taskId | P0 |
| TC-API-RPT-002 | 任务列表 | — | 状态 PENDING/RUNNING/DONE/FAILED | P1 |
| TC-API-RPT-003 | 下载 DONE 任务 | — | 200 文件流 | P0 |
| TC-API-RPT-004 | 下载 PENDING/FAILED | — | 拒绝 | P1 |
| TC-API-RPT-005 | 删除任务 | DONE/PENDING | DONE 同时删除 OSS 文件 | P1 |
| TC-API-RPT-006 | 任务非己（跨店） | — | 403 | P0 |
| TC-API-RPT-007 | 大量数据 | 50w | 异步 + 流式 | P1 |
| TC-API-RPT-008 | 文件保留期 | 7d | 过期清理 | P2 |

---

# 2. 后端单元测试（与 §1 互补，关注内部逻辑）

> 镜像目录：`xianguoji-server/src/test/java/com/xianguoji/server/module/{m}/service/...`

## 2.1 通用 / 工具
- `MoneyUtil`：加减乘除、四舍五入 HALF_UP、千分位字符串化（含 0.1+0.2 等浮点经典）。
- `JwtUtil`：签发/解析/篡改/过期/算法降级（alg=none 拒绝）。
- `IdGenerator`：分布式 ID 不重复、订单号格式 `YYYYMMDDhhmmss + 6 位随机` 唯一。
- `RedisLockTemplate`：可重入、超时释放、释放误删保护（lua）。
- `RateLimiter`：滑动窗口/令牌桶不超额。

## 2.2 Auth
- `AuthService.loginBySms` 全分支：新建/已存在/封禁/锁定。
- `AuthService.loginByWechat`：合并、未绑定手机、code 失败。
- `CaptchaService`：图形+短信，一次性、TTL、计数。
- `JwtFilter`：黑名单命中、refresh 旋转。

## 2.3 Catalog
- `CategoryService.tree`：递归与排序。
- `ProductService.create/update/copy`：SKU 维护一致性。
- `BannerService.activeNow`：时间过滤。
- `SearchHistoryService`：滚动淘汰。

## 2.4 Cart
- `CartService.add`：累加 / 截断 / 下架拒绝。
- `CartService.summary`：合计、运费占位、券占位（不计算最终价，由 OrderPreview 决定）。

## 2.5 Order（最高优先级）
- `OrderPreviewService.calc`（参数化）：商品 + 运费 + 满减 + 券 + 拼团 多维组合断言（≥30 组）。
- `OrderSubmitService`：库存原子扣减、券核销、购物车清理、幂等键、并发不超卖（CountDownLatch 100 线程）。
- `OrderStateMachine.transit`：所有 (from,to) 合法/非法对，覆盖 9 个状态。
- `OrderTimeoutJob`：15min 未支付自动取消。
- `RefundService`：仅退款 / 退货退款 金额边界；分批退款；微信退款回调幂等。
- `OrderQueryService`：列表过滤 SQL 与 Mapper 测试（Testcontainers）。
- `OrderExportService`：流式生成 xlsx，10w 行内存峰值断言。

## 2.6 Promo
- `CouponService.claim`：库存原子扣减、限领、并发。
- `CouponService.usable`：门槛/范围/时间。
- `PromotionRuleEngine`：多档满减选最优、与券叠加策略。
- `GroupBuyService.launch/join/settle/expire`：成团/失败/部分退款。

## 2.7 Review / Message / Chat
- `ReviewService.submit`：去重、违禁词、超时。
- `MessageDispatcher`：站内 + 微信模板 + 短信通道开关。
- `ChatService`：会话定位、未读计数、跨 shop 隔离。
- `ChatService.toVO`：senderAvatar 填充——senderType=0 查 User.avatar、senderType=1 查 Staff.avatar（通过 createBy → StaffMapper.selectById）、用户/商家无头像时返回 null。

## 2.8 Shop / Staff / Help / Stat / Report
- `DeliveryRangeService.inRange`：圆形/多边形配送范围 + 边界点。
- `StaffService.role`：角色矩阵（每角色 × 每操作）。
- `StatService` 各方法：与黄金 SQL 比对（fixture 数据 + 期望快照）。
- `ReportTaskExecutor`：失败重试、超时熔断、文件落 OSS。

---

# 3. 前端测试

## 3.1 用户端 `user-front`（uni-app + Vue3）

### 3.1.1 单元 / 组件
| 编号 | 对象 | 用例 | P |
|---|---|---|---|
| TC-FE-U-CART-001 | 购物车 store（pinia） | 增/减/选/合计 / 持久化 | P0 |
| TC-FE-U-PRICE-001 | 下单金额组合（与 preview 一致） | 全分支 | P0 |
| TC-FE-U-FORM-001 | 手机号 / 短信码 / 收件人表单校验 | 正反例 | P1 |
| TC-FE-U-UPLD-001 | 图片上传（OSS 直传签名）组件 | 大小/类型/网络错重试 | P1 |
| TC-FE-U-WS-001 | 客服 WS 客户端 | 重连/心跳/消息合并 | P1 |
| TC-FE-U-RTR-001 | 路由守卫 | 未登录跳 splash/login | P0 |
| TC-FE-U-I18N-001 | 文案占位 | 缺 key 不崩溃 | P2 |

### 3.1.2 页面级（每页关键交互）

#### `pages/splash`
- TC-FE-U-SPL-001 启动加载（首次/二次） → 首页或登录。
- TC-FE-U-SPL-002 网络断开 → 友好提示 + 重试。
- TC-FE-U-SPL-003 Logo 显示：logo-circle 内显示 logo.png 图片，非 SVG 占位符。

#### `pages/login`
- TC-FE-U-LOG-001 短信登录全流程；TC-002 微信一键登录；TC-003 协议未勾选拦截。
- TC-FE-U-LOG-004 Logo 显示：logo-box 内显示 logo.png 图片，非 SVG 占位符。

#### `pages/index`（首页）
- TC-FE-U-IDX-001 banner 轮播；TC-002 分类 tab；TC-003 推荐流上拉加载；TC-004 搜索入口；TC-005 营业状态条幅；TC-006 公告。

#### `pages/category`
- TC-FE-U-CAT-001 二级联动；TC-002 价格区间筛；TC-003 排序切换；TC-004 加购 toast。

#### `pages/cart`
- TC-FE-U-CRT-001 增减/批选/全选；TC-002 失效商品分组；TC-003 价格变化提示；TC-004 去结算携带 selected。

#### `pages/order`（订单中心）
- TC-FE-U-ORD-001 状态 tab；TC-002 详情时间线；TC-003 取消/确认/再来一单/提醒发货按钮可见性矩阵。

#### `pages/profile`
- TC-FE-U-PRF-001 入口可见性（登录/未登录）；TC-002 退出登录清理 token+缓存。

#### `pagesC/profile-complete`
- TC-FE-U-PRC-001 必填校验；TC-002 微信绑手机号成功后跳回。

#### `pagesC/address`
- TC-FE-U-ADR-001 列表/新增/编辑/删除/设默认；TC-002 微信定位/地图选点；TC-003 上限提示。

#### `pagesC/coupons`
- TC-FE-U-COU-001 三 tab 切换；TC-002 立即使用跳列表；TC-003 倒计时刷新。

#### `pagesC/evaluation`
- TC-FE-U-EVA-001 待评/已评 tab；TC-002 评价表单（星级/文字/图）提交。

#### `pagesC/favorite`
- TC-FE-U-FAV-001 列表/取消收藏；TC-002 已下架灰显。

#### `pagesC/feedback`
- TC-FE-U-FBK-001 表单 + 图片上传完整链路；TC-002 提交失败重试；TC-003 频控提示。

#### `pagesC/footprint`
- TC-FE-U-FTP-001 列表/批量删除/清空；TC-002 失效商品分组。

#### `pagesC/group-buy`（index/detail）
- TC-FE-U-GB-001 列表；TC-002 详情倒计时；TC-003 开团/参团 CTA；TC-004 分享码深链拉起 detail。

#### `pagesC/message`
- TC-FE-U-MSG-001 类目 tab；TC-002 单条已读 / 全部已读；TC-003 红点同步。

#### `pagesC/pickup-point`
- TC-FE-U-PUP-001 地图列表；TC-002 距离排序；TC-003 选中回填下单页。

#### `pagesC/settings`
- TC-FE-U-SET-001 通知开关持久化；TC-002 清缓存；TC-003 注销账号二次确认。

#### `pagesC/chat`
- TC-FE-U-CHT-001 进入会话拉历史；TC-002 文字/图发送；TC-003 WS 离线重连补拉；TC-004 未读数同步。
- TC-FE-U-CHT-005 商家消息头像：senderAvatar 有值时显示实际头像，无值时 fallback 到 logo.png。
- TC-FE-U-CHT-006 用户消息头像：senderAvatar 有值时显示实际头像，无值时 fallback 到 default-avatar.png。
- TC-FE-U-CHT-007 欢迎提示头像：无消息时显示 logo.png 作为客服头像。
- TC-FE-U-CHT-008 商品卡片消息：点击跳转商品详情页。

## 3.2 商家端 `merchant-front`（Vue3 + Vite）

### 3.2.1 通用
| TC-FE-M-RTR-001 | 路由守卫：未登录、Token 失效、403 跳转 | P0 |
| TC-FE-M-AXIOS-001 | 请求拦截：401 自动登出、429 提示、5xx 友好 | P0 |
| TC-FE-M-WS-001 | 后台 WS 弹窗"新订单"通知与跳转 | P0 |
| TC-FE-M-PERM-001 | 权限指令 v-permission 隐藏按钮 | P0 |

### 3.2.2 视图级
- `Login` / `ForgotPassword`：表单、错误提示、忘记密码邮箱/短信链路；Logo 显示——PC 端 glass-panel 和移动端 header 均使用 logo.png，非 material icon 占位符。
- `Dashboard`：四卡数据 + 趋势 + 待办 + 最新订单；空数据态。
- `Goods` / `GoodsEdit`：列表筛选；编辑表单（必填、图片、SKU 行编辑、富文本）；上下架/复制。
- `Orders` / `OrderDetail`：筛选、批量操作、发货抽屉（物流单号校验）、退款审批、打印。
- `Shipping`：发货流水；按物流公司筛。
- `Reviews`：列表、回复、隐藏。
- `Customers`：列表、详情、发券。
- `Campaign` / `CouponCreate`：创建券（满减/折扣/无门槛）；时间窗；适用范围多选。
- `Messages`：客服会话面板；未读切会话；输入框防抖；表情/图片。
- `Chat`：消息头像渲染——senderAvatar 有值时显示 img，无值时 fallback 到 material icon（senderType=1 → store 图标，senderType=0 → person 图标）。
- `ReportExport`：发起任务、列表、下载、删除。
- `BusinessAnalysis`：KPI、收入趋势、品类、客群、商品、热力图；区间切换。
- `HelpCenter`：FAQ/指南；提交反馈。
- `Settings`：店铺资料、营业状态、自提点、配送、通知设置、员工管理（按角色）；版本信息区域显示 logo.png，非 material icon 占位。
- `DefaultLayout`：侧边栏顶部 logo.png + "鲜果记" 品牌名，非纯文字。

每个视图至少 5 条用例（首屏渲染 / 主操作成功 / 主操作失败 / 权限隐藏 / 空态 + 加载态）。

---

# 4. 端到端（E2E）

> 工具：Playwright（H5 + 商家 Web） + 微信开发者工具 CLI（小程序）。脚本目录：`tests/e2e/`。

## 4.1 用户端核心链路（每条 = 1 个 spec）
1. TC-E2E-U-FLOW-001 注册 + 完善资料（短信 / 微信 / 绑手机）
2. TC-E2E-U-FLOW-002 浏览 → 详情 → 加购 → 结算（用券）→ 微信支付（mock）→ 商家发货 → 确认收货 → 评价
3. TC-E2E-U-FLOW-003 自提订单 → 到店核销
4. TC-E2E-U-FLOW-004 拼团：开团 → 分享码邀请 → 成团/未成团两路径
5. TC-E2E-U-FLOW-005 优惠券：领取 → 结算自动选 → 取消还原 → 二次复用
6. TC-E2E-U-FLOW-006 退款：未发货仅退款；已收货退货退款
7. TC-E2E-U-FLOW-007 客服：H5 ↔ 商家端 双向消息 + 未读
8. TC-E2E-U-FLOW-008 反馈（含 OSS 上传）
9. TC-E2E-U-FLOW-009 收藏/足迹/地址/设置 全 CRUD 回归
10. TC-E2E-U-FLOW-010 多端登录、退出、重新登录会话恢复
11. TC-E2E-U-FLOW-011 客服头像渲染：用户端聊天 → 商家消息显示 senderAvatar 或 fallback logo.png；商家端聊天 → 用户消息显示 senderAvatar 或 fallback 图标
12. TC-E2E-U-FLOW-012 Logo 统一验证：用户端 splash / login / chat 欢迎页 / favicon 均使用 logo.png

## 4.2 商家端
1. TC-E2E-M-FLOW-001 登录 → 仪表盘加载
2. TC-E2E-M-FLOW-002 商品上架（含 SKU/图片）→ C 端立即可见
3. TC-E2E-M-FLOW-003 接单 → 发货（填单号）→ 用户端联动
4. TC-E2E-M-FLOW-004 自提订单核销
5. TC-E2E-M-FLOW-005 退款审批
6. TC-E2E-M-FLOW-006 创建优惠券 / 满减规则 / 拼团活动
7. TC-E2E-M-FLOW-007 客服回复
8. TC-E2E-M-FLOW-008 报表导出 + 下载
9. TC-E2E-M-FLOW-009 员工管理（角色矩阵）
10. TC-E2E-M-FLOW-010 店铺资料 / 营业 / 自提点 / 配送 / 通知 设置变更生效
11. TC-E2E-M-FLOW-011 客服聊天头像：商家端 Chat.vue → 用户消息显示 senderAvatar，无 avatar 时 fallback 到 person 图标
12. TC-E2E-M-FLOW-012 Logo 统一验证：Login / ForgotPassword / DefaultLayout sidebar / Settings 版本信息 / favicon 均使用 logo.png

## 4.3 端到端联动（Cross）
- TC-E2E-X-001 商品下架 → 用户端列表 / 详情 / 购物车 / 收藏 即时反馈
- TC-E2E-X-002 商家库存改 0 → 用户端不可加购
- TC-E2E-X-003 拼团到期未成团 → 双端通知 + 退款到账
- TC-E2E-X-004 客服会话 ↔ 站内信 ↔ WS 三通道一致
- TC-E2E-X-005 商家"营业关闭" → 用户端下单受阻
- TC-E2E-X-006 客服头像双向同步：用户修改头像 → 商家端聊天消息头像更新；商家 Staff 修改头像 → 用户端聊天消息头像更新
- TC-E2E-X-007 Logo 统一：用户端 + 商家端所有 logo 位置均使用 logo.png，无残留 SVG/图标占位

---

# 5. 性能 / 安全 / 兼容性 / 数据迁移 / 灾备

## 5.1 性能（k6 / JMeter，脚本 `xianguoji-server/scripts/perf/`）

| 编号 | 场景 | 模型 | 阈值 |
|---|---|---|---|
| TC-PERF-001 | 首页 / 分类 / 详情 | 1k 并发，5min | P95 < 500ms / 600ms / 600ms，错误<0.1% |
| TC-PERF-002 | 加购 / 改数量 | 200 并发 | P95 < 400ms |
| TC-PERF-003 | 下单 | 100 并发，库存 50 | 不超卖；P95 < 1.5s |
| TC-PERF-004 | 抢券 | 1k 并发 / 100 张 | 仅 100 张发出 |
| TC-PERF-005 | 拼团参团 | 200 并发 | 不超员 |
| TC-PERF-006 | 报表导出 | 50w 行 | 不 OOM；< 5min |
| TC-PERF-007 | WebSocket | 5k 长连接 / 1msg/s | CPU < 70%，无连接泄漏 |
| TC-PERF-008 | 数据库慢 SQL | EXPLAIN 全部上线 SQL | 无全表扫描；关键字段有索引 |
| TC-PERF-009 | Redis 命中率 | 详情 / 列表 | ≥ 95% |
| TC-PERF-010 | 限流验证 | 触发各接口限流 | 准确返回 429 |

## 5.2 安全

| 编号 | 类别 | 用例 | P |
|---|---|---|---|
| TC-SEC-AUTHZ-001 | IDOR | 跨用户访问订单/地址/收藏/退款/评价/反馈 | P0 |
| TC-SEC-AUTHZ-002 | 角色越权（CLERK/MANAGER/OWNER 矩阵） | 每写接口跑一遍 | P0 |
| TC-SEC-AUTH-001 | JWT 篡改 / alg=none / 过期 / 黑名单 | — | P0 |
| TC-SEC-AUTH-002 | 验证码：枚举 / 重放 / 跨场景 | — | P0 |
| TC-SEC-INJ-001 | SQL 注入：列表关键字、排序、报表 | 自动 fuzz | P0 |
| TC-SEC-INJ-002 | XSS：评价、富文本商品详情、广播、帮助 | — | P0 |
| TC-SEC-INJ-003 | NoSQL/Redis 命令注入 | key 拼接 | P1 |
| TC-SEC-CSRF-001 | 商家端关键写接口 | — | P1 |
| TC-SEC-FILE-001 | 上传：扩展名/MIME/大小/魔术字节/路径穿越 | — | P0 |
| TC-SEC-FILE-002 | OSS 直传签名：过期、跨用户、目录绑定 | — | P0 |
| TC-SEC-RATE-001 | 登录/短信/领券/下单 限流 | — | P0 |
| TC-SEC-PRICE-001 | 价格篡改：preview/submit 金额对照 | — | P0 |
| TC-SEC-REPLAY-001 | 支付回调签名 + 单号去重 | — | P0 |
| TC-SEC-LOG-001 | 敏感日志：密码、token、手机号、地址 | grep | P0 |
| TC-SEC-DEP-001 | OWASP Dependency-Check | — | P0 |
| TC-SEC-CORS-001 | 商家端域白名单 | — | P1 |
| TC-SEC-HEADER-001 | 安全头：HSTS / CSP / X-Frame / X-CT-Options | — | P1 |
| TC-SEC-PRIV-001 | PII 脱敏：列表脱敏，详情按权限 | — | P0 |

## 5.3 兼容性

| 端 | 设备/版本 | 关注点 | P |
|---|---|---|---|
| H5 | iOS Safari 15/16/17、Android Chrome 100+、微信内置 | 布局、支付跳转、键盘遮挡 | P0 |
| 小程序 | 基础库 ≥2.27 | 接口、滚动、订阅消息授权 | P0 |
| 商家 Web | Chrome/Edge 最新两版 | 大表格滚动、富文本 | P0 |
| 屏幕 | 320 / 375 / 414 / 1280 / 1920 | 响应式 | P1 |
| 弱网 | 3G / 丢包 20% | 重试、loading、超时 | P1 |
| 多语言 | 中文 / 拼写 | 文案完整 | P2 |

## 5.4 数据迁移 & 灾备

| 编号 | 场景 | 预期 | P |
|---|---|---|---|
| TC-DATA-MIG-001 | flyway 全量从空库初始化 | 通过且幂等 | P0 |
| TC-DATA-MIG-002 | 增量脚本 v1→v2 兼容 | 不丢字段 | P0 |
| TC-DATA-IDX-001 | 唯一索引：订单号 / refund_no / coupon_user(user,coupon) / pay_callback(transaction_id) | 防重 | P0 |
| TC-DATA-BAK-001 | 每日备份恢复演练 | RPO ≤ 1h | P0 |
| TC-DATA-BAK-002 | 表删除误操作回滚 | binlog 恢复可行 | P1 |
| TC-DATA-CACHE-001 | Redis 宕机 | 接口降级到 DB，不全站雪崩 | P0 |

---

# 6. 测试夹具与执行规范

## 6.1 后端工程结构（建议生成）

```
xianguoji-server/src/test/
├── java/com/xianguoji/server/
│   ├── AbstractApiTest.java          // @SpringBootTest + Testcontainers + MockMvc + 登录夹具
│   ├── AbstractServiceTest.java       // 单元测试基类（Mockito）
│   ├── support/
│   │   ├── TestUserFactory.java       // 注册并颁发 token
│   │   ├── TestStaffFactory.java
│   │   ├── TestProductFactory.java
│   │   ├── TestOrderFactory.java
│   │   └── ResponseAssertions.java    // {code,msg,data} 通用断言
│   └── module/
│       ├── auth/...
│       ├── catalog/...
│       └── ...                        // 与 main 镜像
└── resources/
    ├── application-test.yml           // H2 / Testcontainers + mock 外部
    ├── sql/schema.sql
    ├── sql/data.sql
    ├── fixtures/order/preview-cases.csv
    └── fixtures/promo/rule-cases.json
```

- 数据库 / Redis 用 Testcontainers，每方法 `@Sql(executionPhase=AFTER_TEST_METHOD)` 清理或事务回滚。
- 外部依赖 mock：`WechatClient`、`SmsClient`、`OssClient`、`PayClient`，统一在 `application-test.yml` 切换 bean。
- 时间用 `Clock` 注入，便于断言 cron / TTL。

## 6.2 前端工程结构

```
user-front/
├── vitest.config.ts
├── src/__tests__/                     // 单元
└── tests/e2e/                         // Playwright（H5）/ MP CLI（小程序）

merchant-front/
├── vitest.config.ts
├── src/__tests__/
└── tests/e2e/                         // Playwright
```

## 6.3 命名 / CI / 报告

- 测试方法：`{被测}_should_{预期}_when_{条件}`。
- CI（GitHub Actions / 自建）：
  - PR：单元 + 接口 + 前端单元 + 关键 E2E（≤10 min）
  - Nightly：全量 E2E + 性能（基准） + 安全扫描 + 兼容性
- 覆盖率：JaCoCo（后端）+ vitest coverage（前端），上传 Codecov；后端整体 ≥70%，order/cart/promo/auth ≥85%。
- 失败必附：日志、截图（前端）、HTTP 报文、SQL 与 Redis 当时状态快照。

## 6.4 质量门（Go/No-Go）

- [ ] P0 用例 100% 通过；P1 ≥ 95%
- [ ] 行覆盖率达标
- [ ] 性能阈值通过
- [ ] OWASP 高危 = 0
- [ ] 数据迁移幂等可回滚
- [ ] 监控/告警/灰度/回滚就绪

---

> 全文完。AI 实现时按编号生成测试，覆盖到本文每一条；如发现接口/页面遗漏请反馈补编号，**不要自行新增未列功能的测试**。
