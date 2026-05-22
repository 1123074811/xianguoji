# 鲜果记 H5 演示版改造方案

> 本文用于指导将当前“鲜果记”用户端从微信小程序优先形态，改造成可用于答辩、演示、内测的 H5 演示版。改造原则是：**不删除现有接口、不删除微信登录/微信支付/小程序专属逻辑，只通过环境开关、条件编译、适配层或注释保留，确保后续可以切回微信小程序正式版**。

---

## 1. 背景与目标

### 1.1 背景

当前项目定位为精品水果电商系统，包含用户端、商家端、后端服务、数据库与缓存。用户端基于 `uni-app + Vue 3 + TypeScript + Pinia` 开发，当前已具备微信小程序与 H5 构建能力。

由于微信小程序平台对个人主体/部分主体的电商类目存在限制，完整的水果商城交易闭环可能无法以个人小程序形式正式上线。因此，短期建议将用户端调整为 **H5 演示版**：保留商品浏览、购物车、订单提交、商家后台、订单管理等核心业务流程，登录与支付采用适合演示的方案。

### 1.2 改造目标

- **保留现有成果**：页面、接口、Pinia 状态、订单流程、商家端管理能力尽量复用。
- **支持 H5 访问**：可通过浏览器访问 `http://localhost:5173` 或部署后的 H5 域名。
- **降低资质依赖**：演示阶段不依赖微信小程序审核、微信小程序登录、微信小程序支付。
- **保留微信能力**：现有 `uni.login`、微信小程序登录接口、微信支付参数、`open-type` 等逻辑不删除，只做条件编译和开关控制。
- **方便日后切换**：未来具备企业/个体主体资质后，可以恢复小程序正式版和微信支付能力。

### 1.3 非目标

本阶段不做以下事情：

- 不删除 `/api/pub/auth/login/wechat`、`/api/pub/auth/login/wechat/quick` 等微信登录接口。
- 不删除 `/api/u/order/{orderNo}/pay` 等支付接口。
- 不删除现有微信小程序配置 `mp-weixin.appid`。
- 不强行改成纯展示型网站。
- 不接入真实 H5 微信支付或公众号网页授权登录。
- 不处理正式商业上线所需的全部资质、备案、支付商户号申请。

---

## 2. 当前项目现状

### 2.1 用户端 H5 支持现状

用户端目录：

```text
user-front/
```

已有脚本：

```json
{
  "dev:h5": "uni -p h5",
  "build:h5": "uni build -p h5",
  "dev:mp-weixin": "uni -p mp-weixin",
  "build:mp-weixin": "uni build -p mp-weixin"
}
```

说明当前工程已经支持 H5 编译，不需要重建项目。

### 2.2 接口基础配置

用户端请求封装位于：

```text
user-front/src/api/request.ts
```

当前 `BASE_URL` 使用：

```text
import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080'
```

这适合 H5 演示版继续使用。后续建议补充 `.env.development`、`.env.production`、`.env.demo`，让接口地址和演示开关可配置化。

### 2.3 登录现状

登录页位于：

```text
user-front/src/pages/login/login.vue
```

已有能力：

- 手机号验证码登录：`authApi.smsLogin`
- 发送验证码：`authApi.sendSms`
- 微信小程序登录：`userStore.wechatLogin`
- 微信小程序静默登录：`userStore.wechatQuickLogin`
- 微信头像昵称授权弹窗

相关接口：

```text
POST /api/pub/auth/sms/send
POST /api/pub/auth/login/sms
POST /api/pub/auth/login/wechat
POST /api/pub/auth/login/wechat/quick
```

H5 演示版应优先使用手机号验证码登录或测试账号一键登录，微信小程序登录逻辑保留在 `MP-WEIXIN` 条件编译块中。

### 2.4 订单与支付现状

确认订单页位于：

```text
user-front/src/pagesB/checkout/index.vue
```

当前普通订单提交使用：

```text
payMethod: 'wechat'
```

订单提交后跳转：

```text
/pagesB/payment-result/index?orderNo=xxx
```

支付结果页位于：

```text
user-front/src/pagesB/payment-result/index.vue
```

当前文案偏向真实支付结果，例如：

- 支付成功
- 支付失败
- 实付金额

H5 演示版建议将支付改为“模拟支付/预约提交成功/到店支付”，但保留原支付接口和字段。

### 2.5 H5 兼容风险点

已发现需要重点适配的能力：

| 类型 | 文件/能力 | H5 风险 | 建议 |
|---|---|---|---|
| 微信小程序登录 | `uni.login({ provider: 'weixin' })` | H5 不可直接使用小程序 code | 用 `MP-WEIXIN` 条件编译保留；H5 使用短信/测试登录 |
| 微信头像授权 | `open-type="chooseAvatar"` | H5 不支持 | 小程序保留；H5 用普通头像上传/默认头像 |
| 微信手机号授权 | `open-type="getPhoneNumber"` | H5 不支持 | 小程序保留；H5 显示“未绑定”或普通绑定流程 |
| 微信地址导入 | `uni.chooseAddress` | H5 不支持 | H5 隐藏按钮或提示手动填写 |
| 小程序分享 | `open-type="share"` | H5 不支持 | H5 改为复制链接/普通分享提示 |
| 支付 | `payMethod: 'wechat'` | H5 无小程序支付环境 | H5 演示使用 `mock`/`offline`/`demo` 支付方法 |
| CORS | 浏览器跨域 | H5 会受同源策略限制 | 后端 CORS 加入 H5 域名 |
| HTTPS | 生产构建请求 HTTP 被拦截 | H5 正式部署需要 HTTPS | 演示本地用 dev，部署用 HTTPS |

---

## 3. 改造总原则

### 3.1 不删除，只隔离

所有微信小程序能力都应保留，但只在微信小程序环境启用。

推荐方式：

```text
// #ifdef MP-WEIXIN
微信小程序专属逻辑
// #endif

// #ifdef H5
H5 演示逻辑
// #endif
```

如果某段逻辑未来需要恢复，但当前 H5 不使用，可以保留函数和接口，只在入口处通过环境变量判断。

### 3.2 使用配置开关，不硬编码演示逻辑

建议新增环境变量：

```text
VITE_API_BASE=http://127.0.0.1:8080
VITE_APP_MODE=demo
VITE_USE_MOCK=false
VITE_ENABLE_WECHAT_LOGIN=false
VITE_ENABLE_WECHAT_PAY=false
VITE_ENABLE_DEMO_LOGIN=true
VITE_DEMO_PAY_METHOD=mock
```

含义：

| 变量 | 建议值 | 说明 |
|---|---|---|
| `VITE_API_BASE` | `http://127.0.0.1:8080` | 后端接口地址 |
| `VITE_APP_MODE` | `demo` | 当前应用模式：`demo` / `prod` |
| `VITE_USE_MOCK` | `false` | 是否使用前端 mock 数据 |
| `VITE_ENABLE_WECHAT_LOGIN` | `false` | H5 演示版关闭微信小程序登录入口 |
| `VITE_ENABLE_WECHAT_PAY` | `false` | H5 演示版关闭真实微信支付 |
| `VITE_ENABLE_DEMO_LOGIN` | `true` | 开启测试账号/演示登录 |
| `VITE_DEMO_PAY_METHOD` | `mock` | 演示支付方式 |

### 3.3 接口保持真实，支付可演示

建议 H5 演示版继续调用真实后端接口完成：

- 商品列表
- 商品详情
- 分类
- 购物车
- 地址管理
- 订单预览
- 订单提交
- 订单列表
- 订单详情
- 评价
- 拼团展示
- 商家端管理

只有微信强绑定能力可演示化：

- 微信登录
- 微信支付
- 微信地址导入
- 微信分享
- 微信客服

### 3.4 前端文案避免误导

如果没有真实支付资质，H5 演示版不建议继续显示“微信支付成功”这类文案。

建议替换为：

| 原文案 | H5 演示版文案 |
|---|---|
| 立即购买 | 立即下单 / 提交订单 |
| 支付成功 | 订单提交成功 / 模拟支付成功 |
| 实付合计 | 预计合计 / 应付合计 |
| 微信快捷登录 | 手机号登录 / 演示登录 |
| 微信地址导入 | 手动填写地址 |

---

## 4. 推荐改造范围

### 4.1 用户端改造范围

#### 4.1.1 登录页

文件：

```text
user-front/src/pages/login/login.vue
```

建议：

1. 保留手机号验证码登录作为 H5 主登录方式。
2. 增加“演示账号一键登录”按钮，适用于答辩和演示。
3. 微信登录按钮仅在 `MP-WEIXIN` 环境显示。
4. H5 环境下不要调用 `uni.login({ provider: 'weixin' })`。
5. 微信授权弹窗保留，但只在小程序环境触发。

推荐交互：

- H5：手机号 + 验证码 / 演示登录
- 微信小程序：手机号 + 验证码 / 微信快捷登录

推荐保留策略：

```text
// #ifdef MP-WEIXIN
显示微信快捷登录按钮，并调用 handleWechatTap
// #endif

// #ifdef H5
显示演示登录按钮或普通登录提示
// #endif
```

#### 4.1.2 用户状态 Store

文件：

```text
user-front/src/stores/user.ts
```

建议：

- 保留 `wechatLogin`、`wechatQuickLogin`。
- 新增 `demoLogin` 或复用 `smsLogin`。
- 如果后端已有固定测试手机号验证码机制，优先调用真实 `smsLogin`。
- 如果后端没有演示接口，可以新增后端演示登录接口，但不要伪造 token。

推荐优先级：

1. 使用真实短信登录接口，后端开发环境固定验证码。
2. 新增后端 `POST /api/pub/auth/login/demo`，仅在 `dev/demo` profile 开启。
3. 不建议前端硬编码 token。

#### 4.1.3 请求封装

文件：

```text
user-front/src/api/request.ts
```

建议：

- `X-Client-Type` 不要固定为 `miniapp`。
- H5 下建议设置为 `h5`，小程序下设置为 `miniapp`。
- 保留生产环境 HTTPS 检查。
- 保持 401 统一跳转登录逻辑。

建议客户端类型：

| 运行端 | `X-Client-Type` |
|---|---|
| H5 | `h5` |
| 微信小程序 | `miniapp` |
| App | `app` |

#### 4.1.4 确认订单页

文件：

```text
user-front/src/pagesB/checkout/index.vue
```

建议：

- 保留 `orderApi.submit`。
- H5 演示版提交订单时 `payMethod` 改为环境变量控制。
- 不删除现有 `payMethod: 'wechat'`，未来小程序正式版继续使用。
- 订单提交成功后跳转到结果页，但结果页文案根据模式显示。

推荐支付方法值：

| 模式 | `payMethod` |
|---|---|
| 小程序正式支付 | `wechat` |
| H5 演示模拟支付 | `mock` |
| 到店支付 | `offline` |
| 预约订单 | `reservation` |

如果后端当前只接受 `wechat`，则建议后端兼容 `mock/offline`，或 H5 仍传 `wechat` 但前端文案显示“模拟支付”。长期建议以后端枚举兼容为准。

#### 4.1.5 支付结果页

文件：

```text
user-front/src/pagesB/payment-result/index.vue
```

建议新增模式化文案：

| 模式 | 标题 | 描述 |
|---|---|---|
| 小程序正式支付 | 支付成功 | 感谢您的信任，果园正快马加鞭为您备货 |
| H5 模拟支付 | 模拟支付成功 | 演示订单已创建，可在订单列表查看流程 |
| 预约订单 | 预约提交成功 | 商家确认后将为您备货或联系自提 |
| 到店支付 | 订单提交成功 | 请到店后完成付款 |

结果页仍然可以加载订单详情和推荐商品，不影响业务展示。

#### 4.1.6 地址页

文件：

```text
user-front/src/pagesC/address/index.vue
```

建议：

- `uni.chooseAddress` 只在 `MP-WEIXIN` 环境显示。
- H5 环境隐藏“微信地址导入”，或按钮改为“手动填写地址”。
- 定位功能如果依赖腾讯地图 key，需要确保 H5 域名在腾讯地图控制台配置白名单。

#### 4.1.7 拼团分享页

相关文件：

```text
user-front/src/pagesC/group-buy/detail.vue
user-front/src/pagesC/group-buy/share.vue
user-front/src/pagesA/goods-detail/index.vue
```

建议：

- `open-type="share"` 在小程序环境保留。
- H5 环境改为“复制分享链接”。
- 分享链接建议使用普通 URL，例如：

```text
https://h5.xianguoji.com/pagesC/group-buy/share?id=123
```

演示阶段可以只提示“链接已复制”。

#### 4.1.8 个人资料完善页

文件：

```text
user-front/src/pagesC/profile-complete/index.vue
```

建议：

- `open-type="chooseAvatar"` 小程序保留。
- `open-type="getPhoneNumber"` 小程序保留。
- H5 使用普通输入/上传能力，或展示“请通过手机号登录绑定”。

### 4.2 后端改造范围

#### 4.2.1 CORS 配置

后端已有 CORS 处理：

```text
xianguoji-server/src/main/java/com/xianguoji/server/common/config/WebMvcConfig.java
```

当前配置读取：

```text
xianguoji.security.cors.allowed-origins
CORS_ALLOWED_ORIGINS
```

H5 本地演示需要允许：

```text
http://localhost:5173
http://127.0.0.1:5173
```

部署演示需要允许：

```text
https://h5.xianguoji.com
```

注意：生产环境不要使用 `*`。

#### 4.2.2 演示登录接口

当前已有短信登录和微信小程序登录。

建议优先方案：

- 开发/演示环境下，固定测试手机号 `13800000000`。
- 验证码可由后端日志或 Redis 获取。
- 登录接口仍走 `POST /api/pub/auth/login/sms`。

可选方案：新增演示登录接口：

```text
POST /api/pub/auth/login/demo
```

限制：

- 只能在 `dev/demo` 环境启用。
- 生产环境必须禁用。
- 返回结构必须与 `LoginVO` 一致。
- 不要绕过正常用户创建/查询逻辑。

#### 4.2.3 支付兼容

当前后端已有：

```text
POST /api/u/order/{orderNo}/pay
```

H5 演示版可以采用两种方式：

方案 A：订单提交后不调用支付接口

- 订单提交成功即进入结果页。
- 结果页显示“订单提交成功/模拟支付成功”。
- 适合答辩演示。

方案 B：新增/兼容模拟支付

- 订单提交后调用支付接口。
- `payMethod=mock` 或 `payMethod=offline`。
- 后端将订单状态推进到对应演示状态。
- 适合完整展示订单状态流转。

推荐：先采用方案 A，减少后端改动；若需要订单状态完整流转，再做方案 B。

### 4.3 商家端改造范围

商家端本身是 PC 管理后台，不受微信小程序运行环境限制。

建议：

- 保留现有商品、订单、营销、评价、库存管理。
- 订单管理中能看到 H5 演示订单。
- 如果订单状态未支付，可允许商家端手动确认/备货/完成。
- 演示时可通过商家后台展示“用户端下单 -> 后台处理 -> 用户端查看订单”的闭环。

---

## 5. 推荐实施步骤

### 阶段一：H5 可运行与环境配置

目标：浏览器能正常打开用户端并访问后端。

任务：

1. 新增用户端环境文件：

```text
user-front/.env.development
user-front/.env.demo
```

2. 配置：

```text
VITE_API_BASE=http://127.0.0.1:8080
VITE_APP_MODE=demo
VITE_ENABLE_WECHAT_LOGIN=false
VITE_ENABLE_WECHAT_PAY=false
VITE_ENABLE_DEMO_LOGIN=true
VITE_DEMO_PAY_METHOD=mock
```

3. 后端 CORS 增加：

```text
http://localhost:5173,http://127.0.0.1:5173
```

4. 启动验证：

```text
后端：http://127.0.0.1:8080/doc.html
用户端：http://localhost:5173
商家端：http://localhost:5174
```

验收：

- H5 首页能打开。
- 商品接口能请求成功。
- 浏览器控制台无跨域错误。

### 阶段二：H5 登录适配

目标：H5 可以登录并进入首页、购物车、订单等页面。

任务：

1. 登录页在 H5 环境隐藏或禁用微信快捷登录。
2. 增加演示登录入口。
3. 优先复用短信登录接口。
4. 小程序微信登录代码保留在 `MP-WEIXIN` 条件编译块。
5. 登录成功后继续刷新购物车角标。

验收：

- H5 登录成功后有 token。
- 刷新页面后登录态仍存在。
- 401 失效后能跳回登录页。
- 微信小程序登录逻辑未被删除。

### 阶段三：H5 订单与模拟支付

目标：H5 能完成从商品到订单的演示闭环。

任务：

1. 确认订单页支付方式改为环境变量控制。
2. H5 演示模式使用 `mock/offline/reservation` 之一。
3. 结果页根据模式显示“模拟支付成功/订单提交成功”。
4. 保留原 `wechat` 支付逻辑用于小程序正式版。
5. 订单列表和订单详情继续调用真实后端。

验收：

- 加入购物车正常。
- 购物车结算正常。
- 确认订单正常。
- 订单提交成功。
- 结果页文案符合演示模式。
- 订单列表能看到新订单。

### 阶段四：小程序专属能力降级

目标：H5 不再触发小程序专属 API 报错。

任务：

1. `uni.chooseAddress` 在 H5 隐藏或降级。
2. `open-type="share"` 在 H5 改为复制链接。
3. `open-type="chooseAvatar"` 在 H5 降级为默认头像/普通上传。
4. `open-type="getPhoneNumber"` 在 H5 隐藏或用手机号登录替代。
5. 小程序版本更新检查只在 `MP-WEIXIN` 执行。

验收：

- 浏览器控制台无 `open-type`/小程序 API 相关错误。
- 地址新增、选择、自提点选择可用。
- 拼团分享在 H5 有可解释交互。

### 阶段五：部署与演示脚本

目标：形成稳定演示流程。

任务：

1. 构建 H5：

```text
npm run build:h5
```

2. 部署 `dist/build/h5` 到静态服务器。
3. 后端配置允许 H5 域名跨域。
4. 准备演示数据：商品、分类、优惠券、测试用户、测试地址、自提点。
5. 准备答辩演示路线。

验收：

- 公网/局域网 H5 地址可访问。
- 接口请求正常。
- 用户端和商家端演示链路完整。

---

## 6. 推荐演示流程

### 6.1 用户端演示流程

1. 打开 H5 首页。
2. 浏览首页轮播、推荐商品、分类入口。
3. 进入分类页筛选水果。
4. 进入商品详情页。
5. 加入购物车。
6. 进入购物车调整数量。
7. 进入确认订单页。
8. 选择配送/自提方式。
9. 选择地址或自提点。
10. 提交订单。
11. 显示“模拟支付成功/订单提交成功”。
12. 查看订单列表和订单详情。

### 6.2 商家端演示流程

1. 商家登录后台。
2. 查看商品管理。
3. 查看刚才用户提交的订单。
4. 修改订单状态或发货/确认。
5. 查看数据统计、评价、营销模块。

### 6.3 答辩说明建议

可说明：

```text
由于微信小程序平台对个人主体电商类目存在限制，本项目当前演示版本采用 H5 形式运行，并使用模拟支付/预约提交方式展示交易流程。系统保留微信小程序登录、微信支付及小程序构建能力，未来在具备企业主体、微信支付商户号和相关经营资质后，可切换到微信小程序正式版上线。
```

---

## 7. 文件级改造清单

| 文件 | 改造内容 | 是否删除旧逻辑 |
|---|---|---|
| `user-front/.env.development` | 新增 H5 本地接口和开关 | 否 |
| `user-front/.env.demo` | 新增演示环境配置 | 否 |
| `user-front/src/api/request.ts` | `X-Client-Type` 区分 H5/小程序 | 否 |
| `user-front/src/pages/login/login.vue` | H5 登录入口、微信登录条件编译 | 否 |
| `user-front/src/stores/user.ts` | 可选新增 demoLogin | 否 |
| `user-front/src/pagesB/checkout/index.vue` | 支付方式按环境开关选择 | 否 |
| `user-front/src/pagesB/payment-result/index.vue` | 结果页文案模式化 | 否 |
| `user-front/src/pagesC/address/index.vue` | 微信地址导入 H5 降级 | 否 |
| `user-front/src/pagesC/group-buy/detail.vue` | 分享按钮 H5 降级 | 否 |
| `user-front/src/pagesC/group-buy/share.vue` | 分享按钮 H5 降级 | 否 |
| `user-front/src/pagesA/goods-detail/index.vue` | 分享按钮 H5 降级 | 否 |
| `user-front/src/pagesC/profile-complete/index.vue` | 微信头像/手机号授权 H5 降级 | 否 |
| `xianguoji-server` CORS 配置 | 增加 H5 访问来源 | 否 |
| 后端支付服务 | 可选兼容 mock/offline 支付 | 否 |

---

## 8. 环境变量建议

### 8.1 `user-front/.env.development`

```text
VITE_API_BASE=http://127.0.0.1:8080
VITE_APP_MODE=dev
VITE_USE_MOCK=false
VITE_ENABLE_WECHAT_LOGIN=false
VITE_ENABLE_WECHAT_PAY=false
VITE_ENABLE_DEMO_LOGIN=true
VITE_DEMO_PAY_METHOD=mock
```

### 8.2 `user-front/.env.demo`

```text
VITE_API_BASE=https://api-demo.xianguoji.com
VITE_APP_MODE=demo
VITE_USE_MOCK=false
VITE_ENABLE_WECHAT_LOGIN=false
VITE_ENABLE_WECHAT_PAY=false
VITE_ENABLE_DEMO_LOGIN=true
VITE_DEMO_PAY_METHOD=mock
```

### 8.3 `user-front/.env.production`

```text
VITE_API_BASE=https://api.xianguoji.com
VITE_APP_MODE=prod
VITE_USE_MOCK=false
VITE_ENABLE_WECHAT_LOGIN=true
VITE_ENABLE_WECHAT_PAY=true
VITE_ENABLE_DEMO_LOGIN=false
VITE_DEMO_PAY_METHOD=wechat
```

说明：生产环境如果实际运行的是小程序正式版，应根据主体资质、微信支付商户号和接口实现再启用微信能力。

---

## 9. H5 部署注意事项

### 9.1 本地演示

适合答辩或开发：

```text
用户端：npm run dev:h5
后端：mvn spring-boot:run
商家端：npm run dev
```

访问：

```text
http://localhost:5173
```

### 9.2 局域网演示

如果手机需要访问电脑上的 H5：

1. 电脑和手机连接同一 Wi-Fi。
2. H5 服务监听局域网地址。
3. 后端 CORS 加入局域网地址。
4. 手机访问：

```text
http://电脑局域网IP:5173
```

注意后端接口也需要使用手机可访问的地址，不能写成手机本地的 `127.0.0.1`。

### 9.3 正式演示服务器

建议：

- H5 使用 Nginx 托管静态文件。
- 后端使用 HTTPS。
- 后端 CORS 精确放开 H5 域名。
- 图片上传和静态资源路径统一使用公网可访问 URL。
- 如果在国内公网长期访问，建议完成域名备案。

---

## 10. 风险与规避

| 风险 | 表现 | 规避 |
|---|---|---|
| 跨域失败 | 浏览器控制台 CORS error | 后端 `CORS_ALLOWED_ORIGINS` 加 H5 域名 |
| 小程序 API 报错 | H5 控制台提示 API 不存在 | 使用 `#ifdef MP-WEIXIN` 隔离 |
| 支付状态不一致 | 下单后订单仍待支付 | 演示时说明模拟支付，或后端兼容 mock 支付 |
| 图片不显示 | 相对路径/本地路径不可访问 | `resolveImageUrl` 统一拼接后端静态资源地址 |
| 生产 HTTP 被拦截 | 构建后请求失败 | H5 正式部署使用 HTTPS |
| 演示登录失效 | 验证码不好获取 | 准备固定测试手机号或 demo 登录接口 |
| 未来切回困难 | H5 改造破坏小程序逻辑 | 所有改造使用条件编译和环境变量，不删除旧接口 |

---

## 11. 验收清单

### 11.1 基础访问

- [ ] `npm run dev:h5` 能启动用户端。
- [ ] 首页可访问。
- [ ] 分类页可访问。
- [ ] 商品详情页可访问。
- [ ] 浏览器控制台无跨域错误。

### 11.2 登录

- [ ] H5 可通过手机号/演示账号登录。
- [ ] 登录后 token 正常保存。
- [ ] 刷新页面后仍保持登录。
- [ ] 退出登录后 token 清除。
- [ ] 微信小程序登录逻辑仍保留。

### 11.3 商品与购物车

- [ ] 首页商品数据正常。
- [ ] 分类筛选正常。
- [ ] 商品详情正常。
- [ ] 加入购物车正常。
- [ ] 购物车数量修改正常。
- [ ] 购物车删除正常。

### 11.4 订单

- [ ] 确认订单页能加载预览。
- [ ] 地址选择正常。
- [ ] 自提点选择正常。
- [ ] 优惠券选择正常。
- [ ] 提交订单正常。
- [ ] 结果页显示 H5 演示文案。
- [ ] 订单列表可看到新订单。
- [ ] 订单详情可打开。

### 11.5 小程序能力降级

- [ ] H5 不显示不可用的微信地址导入，或点击有友好提示。
- [ ] H5 分享不使用 `open-type="share"`。
- [ ] H5 不调用 `uni.login({ provider: 'weixin' })`。
- [ ] H5 不调用微信小程序头像昵称授权。
- [ ] 小程序端相关代码未删除。

### 11.6 商家端联动

- [ ] 商家端可登录。
- [ ] 商家端可查看 H5 用户提交的订单。
- [ ] 商家端可处理订单。
- [ ] 用户端订单状态能同步变化。

---

## 12. 最终推荐路线

建议按以下路线实施：

1. 先做 H5 本地演示环境。
2. 登录先使用手机号/测试账号。
3. 支付先使用模拟支付或订单提交成功模式。
4. 保留微信小程序登录、微信支付、小程序分享等逻辑。
5. 完成用户端下单到商家端处理的闭环演示。
6. 如后续有主体资质，再恢复微信小程序正式版能力。

该路线能最大程度保留当前项目成果，同时规避微信小程序个人主体电商类目限制，适合课程设计、实训、答辩和早期产品演示。
