# 鲜果记 - 用户端小程序

基于 Stitch 导出的 HTML + PNG 原型图，1:1 高保真复刻的 uni-app 跨端小程序项目。

## 技术栈

- **框架**：uni-app (Vue 3 + Composition API)
- **语言**：TypeScript
- **构建工具**：Vite
- **状态管理**：Pinia
- **样式**：SCSS + 设计 Token
- **UI 组件库**：wot-design-uni

## 项目结构

```
src/
├── pages/                  # 主包页面（首页、分类、购物车、订单、我的）
├── pagesA/                 # 商品相关（详情、搜索）
├── pagesB/                 # 订单相关（结算、支付结果、订单详情、评价）
├── pagesC/                 # 营销与个人中心子页（拼团、地址）
├── components/             # 全局通用组件
├── stores/                 # Pinia 状态管理
├── styles/                 # 全局样式与变量
├── mock/                   # 本地 Mock 数据
└── static/                 # 静态资源
```

## 启动命令

```bash
# 安装依赖
pnpm install

# 运行微信小程序
pnpm dev:mp-weixin

# 运行 H5 端预览
pnpm dev:h5
```

## Mock 数据说明

Mock 数据位于 `src/mock/` 目录下，通过 `src/mock/index.ts` 中的 `getMockData` 函数统一调用，模拟网络延迟效果。

## 复刻说明

- **1:1 还原**：严格遵守设计 Token（`variables.scss`），包括颜色、字号、间距、圆角等。
- **组件化**：复用性高的元素（如商品卡片）已提取为独立组件。
- **交互**：所有按钮均有 `:active` 反馈，加入购物车有 Toast 提示。
- **适配**：已处理 iOS 安全区（Safe Area）适配。
```
