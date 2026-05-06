---
description: 排查前端样式问题（布局溢出、容器高度不足、按钮过大过宽、文本换行、z-index冲突等）
---

# 前端样式问题排查工作流

## 适用场景
- 样式覆盖 / 显示不完全
- 容器高度不够（过小）
- 按钮过大过宽
- 文本换行 / 溢出截断
- z-index 层叠冲突
- flex / grid 布局异常
- Uni-App 小程序特有样式问题（rpx、组件多级节点）

## 步骤

### 1. 确认问题现象
- 用户提供截图或描述
- 确认问题出现的平台（H5 / 微信小程序 / App）
- 确认设备/屏幕尺寸（不同宽度可能表现不同）

### 2. 定位目标文件
- 根据截图中的页面，找到对应的 Vue 组件文件
- 用户端页面映射参考：
  - 首页 → `pages/index/index.vue`
  - 分类 → `pages/category/category.vue`
  - 商品详情 → `pagesA/goods-detail/index.vue`
  - 购物车 → `pages/cart/cart.vue`
  - 结算 → `pagesB/checkout/index.vue`
  - 订单列表 → `pages/order/order.vue`
  - 订单详情 → `pagesB/order-detail/index.vue`
  - 个人中心 → `pages/profile/profile.vue`
  - 反馈 → `pagesC/feedback/index.vue`
  - 消息 → `pagesC/message/index.vue`
- 商家端页面在 `merchant-front/src/views/` 下

### 3. 读取并分析源码
- 读取目标 Vue 文件的 `<template>` 和 `<style>` 部分
- 检查以下常见问题模式：

#### 3.1 容器高度不足
- `height: 0` + `padding-bottom` hack → 改用 `aspect-ratio`
- 固定高度缺 `min-height` 兜底
- `overflow: hidden` 截断内容 → 改用 `overflow: auto` 或 `text-overflow: ellipsis`

#### 3.2 按钮过大过宽
- `width: 100%` 在 flex 容器中撑满 → 加 `max-width` 或 `flex-shrink: 0`
- Uni-App `<button>` 默认宽度100% → 改用 `<view>` 自控宽度

#### 3.3 文本换行溢出
- 长文本缺 `overflow-wrap: break-word` / `word-break: break-all`
- 单行截断缺 `white-space: nowrap; overflow: hidden; text-overflow: ellipsis`
- 多行截断缺 `-webkit-line-clamp`

#### 3.4 布局溢出
- flex 子项缺 `flex-shrink: 0` 导致被压缩
- grid 列宽固定值超出容器 → 改用 `1fr` 或 `auto`
- 绝对定位元素超出父容器 → 检查 `position: relative` 边界

#### 3.5 z-index 冲突
- 检查是否创建了新的 stacking context（`transform`, `opacity`, `filter` 等都会创建）
- 建议使用全局 z-index 层级规范

#### 3.6 Uni-App 小程序特有
- 微信小程序自定义组件多一层节点 → `virtualHost` 配置或调整选择器
- `rpx` 在不同屏幕宽度下表现不同 → 检查极端值
- `<button>` 的 `disabled` 会拦截 `@tap` → 改用 `<view>` + CSS 禁用态
- `aspect-ratio` 需基础库 2.19.0+ → 低版本用固定宽高

### 4. 运行 stylelint 静态检查（可选）
```bash
# 在 user-front 目录下
npx stylelint "src/**/*.vue" --allow-empty-input 2>&1 | findstr /V "Unknown rule"
# 或用 npm script
npm run lint:css
# 在 merchant-front 目录下
npx stylelint "src/**/*.vue" --allow-empty-input
npm run lint:css
```
关注 defensive-css 插件报告的规则：
- `require-flex-wrap` — flex 容器缺 flex-wrap（warning 级别）
- `no-accidental-hover` — 触屏设备上的 hover 状态问题
- `no-fixed-sizes` — 固定像素值（已关闭，Uni-App 用 rpx/px 正常）

### 5. 修复并验证
- 最小化修改，只改必要的 CSS 属性
- 优先使用项目已有的 SCSS 变量（`$space-*`, `$font-*`, `$radius-*`, `$color-*`）
- 构建验证：`npx vite build` 确认无编译错误
- 如果是 H5 可用 Playwright 截图对比，小程序需用户在开发者工具中确认

### 6. 提交
- commit message 格式：`fix(front): 修复XX页面XX样式问题`
- 描述根因和修复方式
