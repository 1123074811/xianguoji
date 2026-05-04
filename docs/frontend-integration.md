# 鲜果记 前后端联调实施文档（Frontend Integration Spec）

> 面向 AI 编程工具的可执行规约。与 [backend-spec.md](./backend-spec.md) 同级定位：后端定义"接口契约"，本文定义"前端如何消费契约 + 联调流程"。AI 必须按本文逐条落实，不得自由发挥。

---

## 0. 阅读对象与执行原则

- **读者**：AI 编程助手 / 前端开发同学（用户端 uni-app + 商家端 Vite-Vue3）
- **执行原则**：
  1. 接口路径、入参、出参以 [backend-spec.md](./backend-spec.md) 与 Knife4j（`http://127.0.0.1:8080/doc.html`）的实际 Controller 为准。**两者冲突时以代码为准**（因为 spec 可能滞后于 commit）
  2. **禁止自创接口**——前端发现缺接口时，先在 `docs/frontend-integration.md` 第 9 节"联调缺口"追加一行 TODO，再去后端补；不得在前端造假数据掩盖问题
  3. **Mock 必须可一键关闭**——所有 mock 走 `VITE_USE_MOCK=true` 开关，生产构建必须为 `false`
  4. **类型契约不可偷换**——所有 VO/DTO 在前端有对应 TS interface（位于 `src/api/types/`），命名与后端类一致（如 `ProductVO`、`OrderSubmitDto`）
  5. **每个模块联调完成后必须自测 8 项验收清单（见 §11）**——通过后才能在 PR/Commit 中标记"模块已联调"
  6. 写代码时若遇未实现/外部依赖未到位，方法体留 `// TODO: 联调-xxx 接口待后端补` 并 `console.warn`，禁止静默吞错或塞假数据

---

## 1. 联调总览

### 1.1 系统拓扑

```
┌─────────────────────┐       ┌───────────────────────────┐       ┌──────────────┐
│ user-front          │       │ xianguoji-server          │       │  MySQL 9.x   │
│ (uni-app/H5/小程序) │  ───► │ Spring Boot 3.2 :8080     │  ───► │  xianguoji   │
└─────────────────────┘       │                           │       └──────────────┘
                              │ • /api/pub/**  公共       │       ┌──────────────┐
┌─────────────────────┐       │ • /api/u/**    用户端     │  ───► │  Redis :6379 │
│ merchant-front      │  ───► │ • /api/admin/**商家端     │       └──────────────┘
│ (Vite + Vue3)       │       │ • /static/**   文件       │
└─────────────────────┘       └───────────────────────────┘
```

### 1.2 端 / URL 矩阵

| 端 | 框架 | 开发端口 | 接口 BASE_URL（dev） | 接口 BASE_URL（prod） | Token 存储键 |
|---|---|---|---|---|---|
| 用户端 user-front | uni-app + Vue3 | 5173（H5） / 微信开发者工具 | `http://127.0.0.1:8080` | `https://api.xianguoji.com` | `uni.storage: token / userInfo` |
| 商家端 merchant-front | Vite + Vue3 | 5174 | `http://127.0.0.1:8080` | `https://api.xianguoji.com` | `localStorage: admin_token / admin_info` |

> 商家端开发端口必须改为 5174（vite.config.ts 中 `server.port: 5174`），避免与用户端冲突。

### 1.3 后端鉴权与 CORS（已在后端实现，前端只需配合）

- 拦截器：`/api/u/**` 与 `/api/admin/**` 必须带 `Authorization: Bearer <token>`
- CORS：[WebMvcConfig.java:25](../xianguoji-server/src/main/java/com/xianguoji/server/common/config/WebMvcConfig.java#L25) 已放开 `http://localhost:*` 与 `http://127.0.0.1:*`，部署时如需新域名，**改后端 CORS 白名单，不要在前端绕过**
- Token 失效：服务端返回 `401 + code 4010/4011`，前端必须**清本地存储并跳转登录页**

### 1.4 统一响应壳

所有接口返回（除文件流外）：

```json
{ "code": 0, "msg": "ok", "data": <T>, "ts": 1714723200000 }
```

- `code === 0` → 成功，使用 `data`
- `code !== 0` → 失败，按 §1.5 错误码处理

### 1.5 错误码总表（与 [ResultCode.java](../xianguoji-server/src/main/java/com/xianguoji/server/common/result/ResultCode.java) 一一对应）

| code | 含义 | 前端处理策略 |
|---|---|---|
| 0 | 成功 | 取 `data` |
| 4000 | 参数错误 | toast `msg`，**不上报** |
| 4001 | 业务校验失败 | toast `msg` |
| 4010 | 未登录/Token失效 | 清 token → 跳登录页（用户端 `/pages/login/login`，商家端 `/login`） |
| 4011 | Token 已过期 | 同 4010 |
| 4030 | 无权限 | toast "无权访问"，留在原页 |
| 4040 | 资源不存在 | toast `msg`，可选返回上一页 |
| 4090 | 资源冲突（重复领券、库存不足） | toast `msg`，刷新当前列表 |
| 4290 | 接口限流 | toast "操作过于频繁，请稍后再试" |
| 5000 | 服务器内部错误 | toast "服务异常" + 上报埋点 |
| 5001 | 第三方依赖异常 | toast `msg` |
| 6001 | 库存不足 | 弹窗提示 + 刷新购物车 |
| 6002 | 订单状态非法流转 | toast + 刷新订单详情 |
| 6003 | 优惠券不可用 | 移除已选优惠券，重算金额 |
| 6004 | 拼团已满/已结束 | 跳商品详情，提示"活动已结束" |
| 6005 | 商品已下架 | 移除购物车中该项 |

> 4010/4011 必须在 request 拦截器统一处理；6001/6005 在购物车/下单流程中按场景处理。

---

## 2. 工程结构（强制目录）

### 2.1 用户端 user-front 目标结构

```
user-front/src/
├── api/
│   ├── request.ts              # 请求底层封装（已存在，需重写）
│   ├── interceptors.ts         # 鉴权 + 错误码拦截
│   ├── env.ts                  # 环境变量与 BASE_URL
│   ├── types/                  # 与后端 VO/DTO 一一对应的 TS 类型（新增）
│   │   ├── common.ts           # R<T>、PageVO<T>、PageQry
│   │   ├── auth.ts             # LoginVO、SmsLoginDto...
│   │   ├── user.ts             # UserProfileVO、AddressVO...
│   │   ├── catalog.ts          # ProductVO、ProductDetailVO、CategoryTreeVO、BannerVO
│   │   ├── cart.ts             # CartItemVO、CartListVO
│   │   ├── order.ts            # OrderVO、OrderPreviewVO、OrderSubmitDto、RefundVO
│   │   ├── promo.ts            # CouponVO、UserCouponVO、GroupBuyActivityVO
│   │   ├── shop.ts             # ShopVO、PickupPointVO、DeliverySettingVO
│   │   ├── review.ts           # ReviewVO、ReviewSummaryVO
│   │   └── message.ts          # MessageVO、FeedbackVO
│   └── modules/                # 业务 API 模块（新增）
│       ├── auth.ts
│       ├── user.ts
│       ├── catalog.ts
│       ├── cart.ts
│       ├── order.ts
│       ├── promo.ts
│       ├── shop.ts
│       ├── review.ts
│       └── message.ts
├── stores/                     # Pinia（已有，需要按模块拆分扩展）
│   ├── user.ts                 # 已存在，需对接真实登录
│   ├── cart.ts                 # 已存在，需要全部改为调用接口
│   └── app.ts                  # 新增：店铺信息、配送设置等全局静态数据
├── mock/                       # 仅本地开发降级使用（可保留，受 VITE_USE_MOCK 控制）
└── pages/ ...                  # 业务页面
```

### 2.2 商家端 merchant-front 目标结构

```
merchant-front/src/
├── api/
│   ├── request.ts              # 基于 axios，全新建
│   ├── env.ts
│   ├── types/                  # 与后端 admin 端 VO/DTO 对齐
│   └── modules/
│       ├── auth.ts
│       ├── shop.ts
│       ├── catalog.ts          # 商品/分类/Banner 后台管理
│       ├── order.ts            # 订单后台
│       ├── promo.ts            # 优惠券、满减、拼团后台
│       ├── stat.ts             # 工作台、客户管理
│       ├── staff.ts            # 员工
│       └── message.ts          # 通知设置、用户反馈
├── stores/
│   ├── admin.ts                # 当前管理员 + token + 权限
│   └── app.ts                  # 全局菜单、配置
├── router/                     # 已存在，需加路由守卫
└── views/ ...
```

> 商家端必须新增依赖：`axios`。命令：`pnpm add axios` 或 `npm i axios`。

---

## 3. 请求层规约（强制实现，双端都要落地）

### 3.1 用户端 request.ts（重写 [user-front/src/api/request.ts](../user-front/src/api/request.ts)）

```typescript
// user-front/src/api/request.ts
import { useUserStore } from '@/stores/user';

const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';

export interface R<T = any> {
  code: number;
  msg: string;
  data: T;
  ts: number;
}

export interface PageVO<T> {
  total: number;
  list: T[];
  page: number;
  size: number;
}

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: any;
  params?: Record<string, any>;
  header?: Record<string, string>;
  /** 不需要 Toast 失败提示时设 true */
  silent?: boolean;
  /** 不需要登录态也不会 401 跳转 */
  anonymous?: boolean;
}

export function request<T = any>(opts: RequestOptions): Promise<T> {
  const userStore = useUserStore();
  const token = userStore.token;

  // GET 参数拼接
  let url = opts.url.startsWith('http') ? opts.url : `${BASE_URL}${opts.url}`;
  if (opts.params && Object.keys(opts.params).length) {
    const qs = Object.entries(opts.params)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join('&');
    url += (url.includes('?') ? '&' : '?') + qs;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: opts.method || 'GET',
      data: opts.data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...opts.header,
      },
      success: (res) => {
        // 1. HTTP 层异常
        if (res.statusCode < 200 || res.statusCode >= 300) {
          if ((res.statusCode === 401) && !opts.anonymous) handleAuthFail();
          !opts.silent && uni.showToast({ title: `网络异常 ${res.statusCode}`, icon: 'none' });
          return reject(res);
        }
        // 2. 业务码
        const body = res.data as R<T>;
        if (body.code === 0) return resolve(body.data);
        if ((body.code === 4010 || body.code === 4011) && !opts.anonymous) {
          handleAuthFail();
          return reject(body);
        }
        !opts.silent && uni.showToast({ title: body.msg || '请求失败', icon: 'none' });
        reject(body);
      },
      fail: (err) => {
        !opts.silent && uni.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      },
    });
  });
}

function handleAuthFail() {
  const userStore = useUserStore();
  userStore.logout();
  uni.showToast({ title: '请重新登录', icon: 'none' });
  setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 800);
}
```

**强制约定**：
- 所有业务模块**只能 import `request`**，不得直接调用 `uni.request`
- `silent` 仅用于"埋点上报、心跳、轮询"等不需要打扰用户的请求
- `anonymous` 仅用于 `/api/pub/**` 接口

### 3.2 商家端 request.ts（基于 axios 新建）

```typescript
// merchant-front/src/api/request.ts
import axios, { AxiosError, AxiosRequestConfig } from 'axios';
import { useAdminStore } from '@/stores/admin';

const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';

export interface R<T = any> { code: number; msg: string; data: T; ts: number; }
export interface PageVO<T> { total: number; list: T[]; page: number; size: number; }

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' },
});

instance.interceptors.request.use((config) => {
  const store = useAdminStore();
  if (store.token) config.headers.Authorization = `Bearer ${store.token}`;
  return config;
});

instance.interceptors.response.use(
  (res) => {
    const body = res.data as R<any>;
    if (body.code === 0) return body.data;
    if (body.code === 4010 || body.code === 4011) {
      handleAuthFail();
      return Promise.reject(body);
    }
    showError(body.msg);
    return Promise.reject(body);
  },
  (err: AxiosError) => {
    if (err.response?.status === 401) handleAuthFail();
    showError(err.message);
    return Promise.reject(err);
  },
);

export function request<T = any>(config: AxiosRequestConfig & { silent?: boolean }): Promise<T> {
  return instance.request<any, T>(config);
}

function handleAuthFail() {
  useAdminStore().logout();
  // router 跳转 /login
  window.location.href = '/login';
}

function showError(msg?: string) {
  // 接入 ElMessage / 自研 Toast，此处用 console 占位
  console.error('[API ERROR]', msg);
}
```

### 3.3 环境变量

**用户端**：在 `user-front/` 下新增 `.env.development` / `.env.production`：

```
# .env.development
VITE_API_BASE=http://127.0.0.1:8080
VITE_USE_MOCK=false

# .env.production
VITE_API_BASE=https://api.xianguoji.com
VITE_USE_MOCK=false
```

**商家端**：同上，置于 `merchant-front/`。

---

## 4. 类型契约（必须新建）

> 一律在 `src/api/types/*.ts` 下定义，**字段名与后端 VO/DTO 严格一致**（驼峰），数值用 `number`，金额用 `string`（避免 BigDecimal 精度丢失，由前端做 `Number()` 展示）。

### 4.1 通用类型 `types/common.ts`

```typescript
export interface R<T = any> { code: number; msg: string; data: T; ts: number; }
export interface PageVO<T> { total: number; list: T[]; page: number; size: number; }
export interface PageQry { page?: number; size?: number; }
```

### 4.2 模块类型示例（节选，完整版按 §6 接口清单写齐）

```typescript
// types/auth.ts
export interface LoginVO {
  token: string;
  expireAt: string;          // yyyy-MM-dd HH:mm:ss
  userInfo: UserInfoVO;
}
export interface UserInfoVO {
  id: number;
  nickname: string;
  avatar: string;
  phone: string;
  role: 'user' | 'staff';
  staffRole?: 'owner' | 'admin' | 'packer' | 'courier';
}
export interface SmsSendDto { phone: string; }
export interface SmsLoginDto { phone: string; code: string; }
export interface WechatLoginDto { code: string; }
export interface AdminLoginDto { username: string; password: string; }

// types/catalog.ts
export interface ProductVO {
  id: number;
  name: string;
  subtitle: string;
  categoryId: number;
  mainImage: string;
  minPrice: string;
  maxPrice: string;
  totalStock: number;
  sales: number;
  isRecommend: 0 | 1;
  supportDelivery: 0 | 1;
  supportPickup: 0 | 1;
}
export interface ProductDetailVO extends ProductVO {
  videoUrl?: string;
  description: string;
  skuList: SkuVO[];
  carouselImages: string[];
  detailImages: string[];
  reviewSummary: { totalCount: number; avgRating: number; withImageCount: number; };
}
export interface SkuVO {
  id: number;
  specName: string;
  price: string;
  originalPrice: string;
  stock: number;
  isDefault: 0 | 1;
}
export interface CategoryTreeVO {
  id: number; name: string; icon: string; sort: number;
  children: CategoryTreeVO[];
}
export interface BannerVO { id: number; title: string; image: string; linkType: number; linkValue: string; }

// types/cart.ts
export interface CartItemVO {
  id: number; productId: number; skuId: number;
  productName: string; mainImage: string; specName: string;
  price: string; originalPrice: string;
  stock: number; productStatus: number;
  quantity: number; selected: 0 | 1; subtotal: string;
}
export interface CartListVO {
  items: CartItemVO[];
  totalAmount: string;
  discountAmount: string;
  promotionTip: string;
}

// types/order.ts
export interface OrderSubmitDto {
  addressId?: number;
  pickupPointId?: number;
  deliveryType: 1 | 2;       // 1=配送 2=自提
  deliveryTime?: string;
  cartItemIds?: number[];
  userCouponId?: number;
  userRemark?: string;
  payMethod: 'wechat' | 'alipay';
  groupBuyActivityId?: number;
  groupBuyInstanceId?: number;
}
// OrderPreviewVO / OrderVO 略，按后端 VO 直接镜像
```

> 后端字段如有改动，前端 types **必须同步更新**；可考虑后续接入 OpenAPI codegen 自动生成（见 §10 后续优化）。

---

## 5. API 模块写法（强制模板）

### 5.1 模板：`api/modules/<module>.ts`

```typescript
// user-front/src/api/modules/catalog.ts
import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type {
  ProductVO, ProductDetailVO, CategoryTreeVO, BannerVO,
} from '@/api/types/catalog';

export const catalogApi = {
  categoryTree: () =>
    request<CategoryTreeVO[]>({ url: '/api/pub/category/tree', anonymous: true }),

  bannerList: () =>
    request<BannerVO[]>({ url: '/api/pub/banner/list', anonymous: true }),

  hotSearch: () =>
    request<string[]>({ url: '/api/pub/hot-search/list', anonymous: true }),

  productPage: (params: {
    page?: number; size?: number;
    categoryId?: number; keyword?: string;
    sort?: 'sales' | 'price_asc' | 'price_desc';
  }) =>
    request<PageVO<ProductVO>>({ url: '/api/pub/product/page', params, anonymous: true }),

  recommend: () =>
    request<ProductVO[]>({ url: '/api/pub/product/recommend', anonymous: true }),

  productDetail: (id: number) =>
    request<ProductDetailVO>({ url: `/api/pub/product/${id}`, anonymous: true }),

  recordSearch: (keyword: string) =>
    request<void>({ url: '/api/u/search/record', method: 'POST', data: { keyword }, silent: true }),

  searchHistory: () =>
    request<string[]>({ url: '/api/u/search/history' }),

  clearSearchHistory: () =>
    request<void>({ url: '/api/u/search/history', method: 'DELETE' }),
};
```

**强制约定**：
- 一个模块对外暴露一个对象（`xxxApi`），方法名贴合接口语义
- 不在 API 模块里做 toast / 路由跳转，那是页面/store 的职责
- 入参用对象传，避免位置参数歧义

---

## 6. 接口对接清单（按业务模块）

> 列出**前端必调**的接口与对应页面，AI 实现某个模块时按表逐条替换 mock。完整接口看 [backend-spec.md §7](./backend-spec.md) 与 Knife4j。

### 6.1 auth — 登录/鉴权（用户端）

| 业务 | METHOD | 路径 | 前端调用点 | 备注 |
|---|---|---|---|---|
| 发送验证码 | POST | `/api/pub/auth/sms/send` | [pages/login/login.vue:95](../user-front/src/pages/login/login.vue#L95) | 60s 倒计时由前端控制；返回 ok 即可 |
| 验证码登录 | POST | `/api/pub/auth/login/sms` | [pages/login/login.vue:111](../user-front/src/pages/login/login.vue#L111) | 拿 `LoginVO` → `userStore.setToken/setUserInfo` |
| 微信登录 | POST | `/api/pub/auth/login/wechat` | [pages/login/login.vue:123](../user-front/src/pages/login/login.vue#L123) | uni.login 拿 code → 提交 |
| 退出登录 | POST | `/api/u/auth/logout` | [pagesC/settings](../user-front/src/pagesC/settings/) | 后端无状态，调一下做日志 |

### 6.2 user — 用户中心 / 地址 / 收藏 / 足迹

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 我的资料 | GET | `/api/u/user/profile` | [pages/profile/profile.vue](../user-front/src/pages/profile/profile.vue) |
| 修改资料 | PUT | `/api/u/user/profile` | profile 编辑页 |
| 地址列表 | GET | `/api/u/address/list` | `pagesC/address/index` |
| 地址增/改/删/设默认 | POST/PUT/DELETE/PUT | `/api/u/address/**` | `pagesC/address/edit` |
| 足迹分页 | GET | `/api/u/footprint/page` | `pagesC/footprint` |
| 清空足迹 | DELETE | `/api/u/footprint` | 同上 |
| 收藏分页 | GET | `/api/u/favorite/page` | `pagesC/favorite` |
| 收藏/取消 | POST/DELETE | `/api/u/favorite/{productId}` | 商品详情 + 收藏页 |

### 6.3 shop — 店铺/自提点/配送（公共）

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 店铺信息 | GET | `/api/pub/shop/info` | App 启动 / 首页头部 |
| 自提点列表 | GET | `/api/pub/pickup-point/list` | 结算页选自提点 |
| 配送设置 | GET | `/api/pub/delivery-setting` | 结算页计算运费 |

### 6.4 catalog — 分类/商品/搜索

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 分类树 | GET | `/api/pub/category/tree` | [pages/category/category.vue:101](../user-front/src/pages/category/category.vue#L101) |
| Banner | GET | `/api/pub/banner/list` | [pages/index/index.vue:32](../user-front/src/pages/index/index.vue#L32) |
| 热门搜索 | GET | `/api/pub/hot-search/list` | `pagesA/search/index` |
| 商品分页 | GET | `/api/pub/product/page` | [pages/category/category.vue:105](../user-front/src/pages/category/category.vue#L105)、`pagesA/search-result` |
| 店主推荐 | GET | `/api/pub/product/recommend` | [pages/index/index.vue:134](../user-front/src/pages/index/index.vue#L134) |
| 商品详情 | GET | `/api/pub/product/{id}` | `pagesA/goods-detail/index` |
| 搜索记录/历史/清空 | POST/GET/DELETE | `/api/u/search/**` | `pagesA/search/index` |

### 6.5 cart — 购物车（**全部走接口，禁止本地存储**）

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 购物车列表 | GET | `/api/u/cart/list` | [pages/cart/cart.vue](../user-front/src/pages/cart/cart.vue) |
| 加车 | POST | `/api/u/cart` | 商品详情 + 列表"加号"按钮 |
| 改数量 | PUT | `/api/u/cart/{id}/quantity` | 购物车页 |
| 批量勾选 | PUT | `/api/u/cart/selected` | 购物车页 |
| 删除 | DELETE | `/api/u/cart/{id}` | 购物车页 |
| 清空 | DELETE | `/api/u/cart/clear` | 购物车页 |
| 角标数量 | GET | `/api/u/cart/count` | 全局 tabBar 角标，App 启动 + 加车后刷新 |

> [stores/cart.ts](../user-front/src/stores/cart.ts) 现状是纯前端假数据，**整个文件需重写**：state 仅缓存最近一次拉到的 `CartListVO`，所有操作 await 后端、然后重新 `getCart()` 同步 state。

### 6.6 order — 订单/售后

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 结算预览 | POST | `/api/u/order/preview` | `pagesB/checkout/index` 进入即调 |
| 提交订单 | POST | `/api/u/order/submit` | `pagesB/checkout` 点"提交订单" |
| 发起支付 | POST | `/api/u/order/{orderNo}/pay` | 提交后立即调，拿到 wxpay 参数后 `uni.requestPayment` |
| 订单分页 | GET | `/api/u/order/page` | [pages/order/order.vue](../user-front/src/pages/order/order.vue) |
| 订单详情 | GET | `/api/u/order/{orderNo}` | `pagesB/order-detail/index` |
| 取消/确认/提醒/再下单 | POST | `/api/u/order/{orderNo}/{cancel|confirm|remind|repurchase}` | 订单详情 + 订单列表 |
| 申请售后 | POST | `/api/u/refund` | `pagesB/order-detail` 内"申请售后" |
| 售后详情 | GET | `/api/u/refund/{refundNo}` | `pagesC/feedback` 或售后详情页 |

### 6.7 promo — 优惠券/拼团

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 可领券列表 | GET | `/api/pub/coupon/list` | 首页券位 + `pagesC/coupons` |
| 领券 | POST | `/api/u/coupon/{couponId}/claim` | 首页券位 [index.vue:122](../user-front/src/pages/index/index.vue#L122) |
| 我的券 | GET | `/api/u/coupon/my` | `pagesC/coupons` |
| 拼团活动列表 | GET | `/api/pub/group-buy/list` | `pagesC/group-buy/index` |
| 拼团详情 | GET | `/api/pub/group-buy/{id}` | 商品详情拼团入口 |
| 开团/参团 | POST | `/api/u/group-buy/start`、`/join` | 商品详情、拼团详情页 |

### 6.8 review — 评价

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 商品评价分页 | GET | `/api/pub/review/page` | 商品详情评价区 |
| 评价摘要 | GET | `/api/pub/review/summary/{productId}` | 商品详情头部 |
| 我的评价 | GET | `/api/u/review/my` | `pagesC/evaluation` |
| 提交评价 | POST | `/api/u/review` | `pagesB/evaluation` |

### 6.9 message — 消息/反馈

| 业务 | METHOD | 路径 | 前端调用点 |
|---|---|---|---|
| 消息分页 | GET | `/api/u/message/page` | `pagesC/message` |
| 已读 | PUT | `/api/u/message/{id}/read` | 消息页点击 |
| 提交反馈 | POST | `/api/u/feedback` | `pagesC/feedback` |

---

## 7. 商家端 API 清单（merchant-front）

> 路径前缀 `/api/admin/**`，鉴权 Header 同 [§3.2](#32-商家端-requestts基于-axios-新建)。Token TTL 12h，过期跳 `/login`。

| 模块 | 主要接口 | 前端视图 |
|---|---|---|
| auth | `POST /api/pub/admin/login`、`GET /api/admin/auth/me`、`POST /api/admin/auth/logout` | [Login.vue](../merchant-front/src/views/Login.vue) |
| dashboard / stat | `GET /api/admin/stat/dashboard`、`/stat/sales`、`/stat/order` | [Dashboard.vue](../merchant-front/src/views/Dashboard.vue)、[BusinessAnalysis.vue](../merchant-front/src/views/BusinessAnalysis.vue) |
| 商品 | `GET/POST/PUT/DELETE /api/admin/product/**`、SKU/分类/Banner CRUD | [Goods.vue](../merchant-front/src/views/Goods.vue)、[GoodsEdit.vue](../merchant-front/src/views/GoodsEdit.vue) |
| 订单 | `GET /api/admin/order/page`、`/{orderNo}`、`/{orderNo}/ship`、`/refund/**` | [Orders.vue](../merchant-front/src/views/Orders.vue)、[OrderDetail.vue](../merchant-front/src/views/OrderDetail.vue)、[Shipping.vue](../merchant-front/src/views/Shipping.vue) |
| 客户 | `GET /api/admin/customer/page`、`/{id}` | [Customers.vue](../merchant-front/src/views/Customers.vue) |
| 营销 | `GET/POST /api/admin/promo/coupon/**`、`/group-buy/**` | [Campaign.vue](../merchant-front/src/views/Campaign.vue) |
| 评价 | `GET /api/admin/review/page`、`POST /api/admin/review/{id}/reply` | [Reviews.vue](../merchant-front/src/views/Reviews.vue) |
| 消息 | `GET /api/admin/feedback/page`、`/api/admin/notify-setting/**` | [Messages.vue](../merchant-front/src/views/Messages.vue) |
| 设置 | `PUT /api/admin/shop/info`、`/open-status`、`/delivery-setting` | [Settings.vue](../merchant-front/src/views/Settings.vue) |
| 员工 | `GET/POST/PUT/DELETE /api/admin/staff/**` | Settings 子页 |
| 导出 | `GET /api/admin/export/order`（流式 EasyExcel） | [ReportExport.vue](../merchant-front/src/views/ReportExport.vue) |

> 商家端**所有路由必须挂全局守卫**：未登录或 token 过期跳 `/login`；按 `staffRole` 隐藏菜单（`packer` 看不到营销/统计、`courier` 仅看订单配送）。

---

## 8. Pinia Store 改造规范

### 8.1 user store（[user-front/src/stores/user.ts](../user-front/src/stores/user.ts)）

需新增：

```typescript
async function smsLogin(phone: string, code: string) {
  const vo = await authApi.smsLogin({ phone, code });
  setToken(vo.token);
  setUserInfo(vo.userInfo);
  // 登录成功后预加载购物车角标
  await useCartStore().refreshCount();
}
async function fetchProfile() {
  const profile = await userApi.profile();
  setUserInfo({ ...userInfo.value, ...profile });
}
```

并删除 [login.vue:116-117](../user-front/src/pages/login/login.vue#L116) 中的 `'mock-token'` 与假 userInfo。

### 8.2 cart store（[user-front/src/stores/cart.ts](../user-front/src/stores/cart.ts)）

**整个文件重写**：

```typescript
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { cartApi } from '@/api/modules/cart';
import type { CartItemVO, CartListVO } from '@/api/types/cart';

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItemVO[]>([]);
  const totalAmount = ref('0.00');
  const discountAmount = ref('0.00');
  const count = ref(0);

  const totalCount = computed(() => count.value);
  const selectedItems = computed(() => items.value.filter(i => i.selected === 1));

  async function refreshList() {
    const data = await cartApi.list();
    items.value = data.items;
    totalAmount.value = data.totalAmount;
    discountAmount.value = data.discountAmount;
    count.value = data.items.reduce((s, i) => s + i.quantity, 0);
  }
  async function refreshCount() {
    count.value = await cartApi.count();
  }
  async function addToCart(skuId: number, quantity = 1) {
    await cartApi.add({ skuId, quantity });
    await refreshCount();
  }
  async function updateQty(id: number, quantity: number) {
    await cartApi.updateQuantity(id, quantity);
    await refreshList();
  }
  async function setSelected(ids: number[], selected: 0 | 1) {
    await cartApi.updateSelected({ ids, selected });
    await refreshList();
  }
  async function remove(id: number) {
    await cartApi.delete(id);
    await refreshList();
  }
  async function clear() {
    await cartApi.clear();
    await refreshList();
  }

  return {
    items, totalAmount, discountAmount, count,
    totalCount, selectedItems,
    refreshList, refreshCount, addToCart, updateQty, setSelected, remove, clear,
  };
});
```

### 8.3 admin store（merchant-front 新建 `stores/admin.ts`）

字段：`token / staffInfo / staffRole / permissions[]`；方法：`login(username,password)`、`fetchMe()`、`logout()`、`hasPermission(key)`。

---

## 9. 联调环境准备清单

### 9.1 后端启动前置

1. MySQL 9.x 已建库 `xianguoji`，导入 [database/schema.sql](../database/schema.sql)
2. Redis 6+ 启动，地址/密码与 [application.yml:27](../xianguoji-server/src/main/resources/application.yml#L27) 对齐
3. 准备测试账号：
   - 用户端：手机号 `13800000000`，验证码取 Redis `sms:code:13800000000` 或开发期看后端日志
   - 商家端：种子 SQL 中的 owner 账户（如 `admin / admin123`，具体看 `database/migrations` 里种子数据，没有就先在 schema 里 INSERT 一条）
4. `cd xianguoji-server && mvn spring-boot:run`，验证 `http://127.0.0.1:8080/doc.html` 可访问

### 9.2 前端启动

```bash
# 用户端
cd user-front
npm i
npm run dev:h5             # H5 调试，浏览器打开 http://localhost:5173
# 或 npm run dev:mp-weixin  # 用微信开发者工具打开 dist/dev/mp-weixin

# 商家端
cd merchant-front
npm i
npm run dev                # http://localhost:5174
```

### 9.3 联调缺口登记表（联调过程中持续维护）

> 发现"前端需要但后端没有"的接口时在此追加：

| 序号 | 端 | 期望接口 | 当前状态 | 责任人 | 备注 |
|---|---|---|---|---|---|
| 1 | 用户端 | （示例）`GET /api/pub/coupon/list` 公共可领券列表 | 待后端补 | - | [index.vue:116](../user-front/src/pages/index/index.vue#L116) 首页券位需要 |
| ... | | | | | |

---

## 10. 联调里程碑与排期建议

| 阶段 | 内容 | 工时（人日） | 验收 |
|---|---|---|---|
| M1 基础设施 | request 重写、env、types 框架、Pinia user/cart 新接口、商家端 axios 接入与登录 | 1 | 用户端可登录拿真 token；商家端可登录进 dashboard |
| M2 公共域 | shop info、category tree、banner、热门搜索、商品分页、商品详情 | 1 | 首页/分类/详情全部走真接口，无 getMockData |
| M3 用户域 | profile、address CRUD、favorite、footprint | 1 | 个人中心全部联通 |
| M4 购物车 | cart 全套 + 全局角标 | 0.5 | 加车→购物车→改数量→勾选→删除链路通 |
| M5 下单支付 | order preview / submit / pay / 列表 / 详情 / 取消确认 | 1.5 | 下单→支付→订单状态流转完整跑通（支付可先 mock） |
| M6 营销 + 评价 + 售后 + 消息 | 优惠券、拼团、评价、refund、message、feedback | 1 | 各功能页可用 |
| M7 商家端 | 商品 CRUD、订单管理、客户、营销、统计、设置、员工、导出 | 3 | 全后台可用 |
| M8 联调收尾 | 错误码走查、异常路径回归、CORS/部署联调 | 0.5 | §11 验收清单全部 PASS |

> 总计约 9.5 人日（仅前端联调工作量；不含后端补缺口、UI 微调）。

---

## 11. 模块联调验收清单（每个模块上线前**必过**）

每个 PR / commit 自测以下 8 项：

- [ ] **接口路径正确**：与 Knife4j 完全一致，含 `/api/u`、`/api/pub`、`/api/admin` 前缀
- [ ] **Token 行为**：未登录访问 `/api/u/**` 返回 4010，前端正确跳登录
- [ ] **加载态**：所有列表/详情有 loading；按钮在 await 期间 disabled，避免重复提交
- [ ] **空态**：列表为 0 条时有"暂无数据"占位，不是空白页
- [ ] **错误码**：手动构造 6001/6003/4090 等业务异常，前端 toast 与 UI 表现符合 §1.5
- [ ] **下拉/上拉**：列表页支持下拉刷新与触底加载，分页参数正确递增
- [ ] **类型契约**：TS 编译 0 报错；接口返回字段与 `types/*.ts` 一致（用浏览器 Network 抽查 1 条）
- [ ] **Mock 已下线**：本模块代码内不再 import `getMockData`、不再有写死的 picsum/unsplash 图

---

## 12. 已知前端待清理的"假数据/写死项"清单

> AI 联调时按此清单逐条替换：

| 文件 | 行 | 问题 | 替换为 |
|---|---|---|---|
| [user-front/src/api/request.ts](../user-front/src/api/request.ts) | 1 | `BASE_URL = 'https://api-mock.example.com'` | env 注入 |
| [user-front/src/api/request.ts](../user-front/src/api/request.ts) | 9 | `Authorization: token` 缺 `Bearer ` 前缀 | `Bearer ${token}` |
| [user-front/src/pages/login/login.vue](../user-front/src/pages/login/login.vue) | 95-109 | `getCode()` 仅本地倒计时 | 调 `authApi.sendSms` |
| [user-front/src/pages/login/login.vue](../user-front/src/pages/login/login.vue) | 111-129 | `'mock-token'` 假登录 | 调 `userStore.smsLogin()` |
| [user-front/src/pages/index/index.vue](../user-front/src/pages/index/index.vue) | 32 | banner 写死 picsum | `catalogApi.bannerList()` |
| [user-front/src/pages/index/index.vue](../user-front/src/pages/index/index.vue) | 105-114 | shortcuts 写死 8 项 | `catalogApi.categoryTree()` 取前 8 |
| [user-front/src/pages/index/index.vue](../user-front/src/pages/index/index.vue) | 116-120 | coupons 写死 3 张 | `promoApi.couponList()` |
| [user-front/src/pages/index/index.vue](../user-front/src/pages/index/index.vue) | 122-124 | `claimCoupon` 仅 toast | `promoApi.claimCoupon(id)` |
| [user-front/src/pages/index/index.vue](../user-front/src/pages/index/index.vue) | 134 | `getMockData('goods.json')` | `catalogApi.recommend()` |
| [user-front/src/pages/category/category.vue](../user-front/src/pages/category/category.vue) | 101-105 | `getMockData('categories.json' / 'goods.json')` | `catalogApi.categoryTree() + productPage()` |
| [user-front/src/stores/cart.ts](../user-front/src/stores/cart.ts) | 5-22 | 假数据 + 纯本地状态 | 全文件按 §8.2 重写 |
| [user-front/src/pagesB/payment-result/index.vue](../user-front/src/pagesB/payment-result/index.vue) | 71 | `getMockData('goods.json')` 推荐位 | `catalogApi.recommend()` |
| [user-front/src/mock/](../user-front/src/mock/) | - | 整个目录 | M2 完成后**删除**或挂在 `if(VITE_USE_MOCK)` 下 |
| [merchant-front/src/](../merchant-front/src/) | 全部 views | 静态 UI、无请求层 | 按 §7 接接口 |

---

## 13. 后续优化（非阻塞，可在联调完成后做）

1. **OpenAPI codegen**：用 `openapi-typescript` 从 `http://127.0.0.1:8080/v3/api-docs` 自动生成 `types/`，杜绝字段漂移
2. **请求重试 / 弱网降级**：超时 1 次重试，弱网下显示骨架屏
3. **接口缓存**：分类树、店铺信息、配送设置等低频接口走 5 分钟内存缓存
4. **埋点**：在 request 拦截器统一上报接口耗时与失败率
5. **CI 守卫**：PR 时跑 `tsc --noEmit` 与 `eslint`，类型 / 静态检查不过禁止合并

---

## 14. 文档维护规则

- 本文与 [backend-spec.md](./backend-spec.md) 同步演进；后端新增/修改接口时，**前后端都必须同 commit 改本文 §6 / §7**
- §9.3「联调缺口」表是活的，发现一条加一条，闭环后划线删除
- 字段类型变化必须在 §4 类型契约同步更新
