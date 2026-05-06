-- P1-5: 补充高频查询索引
-- 执行前请确认索引不存在，避免重复创建

-- 订单表：用户订单列表（按状态+时间排序）
ALTER TABLE `order` ADD INDEX idx_user_status_created (`user_id`, `status`, `created_at` DESC);

-- 订单表：商家按状态查单
ALTER TABLE `order` ADD INDEX idx_status_created (`status`, `created_at` DESC);

-- 订单表：订单号查询
ALTER TABLE `order` ADD INDEX idx_order_no (`order_no`);

-- 订单项：按订单ID查商品
ALTER TABLE `order_item` ADD INDEX idx_order_id (`order_id`);

-- 退款表：按订单查退款
ALTER TABLE `refund` ADD INDEX idx_order_id (`order_id`);

-- 商品表：上架+排序
ALTER TABLE `product` ADD INDEX idx_on_sale_sort (`on_sale`, `sort_order` DESC);

-- 商品表：分类+上架
ALTER TABLE `product` ADD INDEX idx_category_on_sale (`category_id`, `on_sale`);

-- 评价表：按商品查评价
ALTER TABLE `review` ADD INDEX idx_product_created (`product_id`, `created_at` DESC);

-- 优惠券领取记录：防重复
ALTER TABLE `user_coupon` ADD INDEX idx_user_coupon (`user_id`, `coupon_id`);
