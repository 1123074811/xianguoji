-- 测试数据库初始化脚本
-- 此脚本用于Testcontainers MySQL容器启动时初始化测试数据

-- 创建测试用户
INSERT INTO user (id, phone, nickname, avatar, gender, birthday, status, created_at, updated_at, deleted)
VALUES 
(1, '13800138001', '测试用户1', 'https://example.com/avatar1.jpg', 1, '1990-01-01', 1, NOW(), NOW(), 0),
(2, '13800138002', '测试用户2', NULL, 0, '1995-05-15', 1, NOW(), NOW(), 0),
(999, '13800999001', '被封禁用户', NULL, 1, '1988-08-08', 0, NOW(), NOW(), 0);

-- 创建测试地址
INSERT INTO address (id, user_id, receiver, phone, province, city, district, detail, is_default, latitude, longitude, created_at, updated_at, deleted)
VALUES 
(1, 1, '张三', '13800138001', '广东省', '深圳市', '南山区', '科技园南区123号', 1, 22.5311, 113.9344, NOW(), NOW(), 0),
(2, 1, '李四', '13800138001', '广东省', '深圳市', '福田区', '中心区456号', 0, 22.5470, 114.0859, NOW(), NOW(), 0);

-- 创建测试分类
INSERT INTO category (id, parent_id, name, icon, sort, enabled, created_at, updated_at, deleted)
VALUES 
(1, 0, '时令热销', 'icon1', 1, 1, NOW(), NOW(), 0),
(2, 0, '进口鲜果', 'icon2', 2, 1, NOW(), NOW(), 0),
(3, 0, '精品礼盒', 'icon3', 3, 1, NOW(), NOW(), 0),
(11, 1, '草莓', 'icon11', 1, 1, NOW(), NOW(), 0),
(12, 1, '樱桃', 'icon12', 2, 1, NOW(), NOW(), 0);

-- 创建测试商品
INSERT INTO product (id, category_id, name, main_image, images, detail, status, sort, created_at, updated_at, deleted)
VALUES 
(1, 11, '丹东草莓', 'https://example.com/strawberry.jpg', '[]', '<p>新鲜丹东草莓</p>', 1, 1, NOW(), NOW(), 0),
(2, 12, '智利车厘子', 'https://example.com/cherry.jpg', '[]', '<p>进口智利车厘子</p>', 1, 2, NOW(), NOW(), 0),
(3, 11, '下架商品', 'https://example.com/offline.jpg', '[]', '<p>已下架</p>', 0, 3, NOW(), NOW(), 0);

-- 创建测试SKU
INSERT INTO sku (id, product_id, spec, price, stock, sales, created_at, updated_at, deleted)
VALUES 
(1, 1, '500g', 29.90, 100, 50, NOW(), NOW(), 0),
(2, 1, '1kg', 49.90, 50, 30, NOW(), NOW(), 0),
(3, 2, '2kg', 89.00, 20, 10, NOW(), NOW(), 0),
(4, 3, '500g', 19.90, 0, 0, NOW(), NOW(), 0);

-- 创建测试商家员工
INSERT INTO shop (id, name, logo, phone, address, open_time, close_time, status, created_at, updated_at, deleted)
VALUES (1, '鲜果记测试店', 'https://example.com/shop_logo.jpg', '13900139000', '测试地址', '08:00', '22:00', 1, NOW(), NOW(), 0);

INSERT INTO staff (id, shop_id, username, password, name, role, phone, status, created_at, updated_at, deleted)
VALUES 
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '店长', 'OWNER', '13900139001', 1, NOW(), NOW(), 0),
(2, 1, 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '经理', 'MANAGER', '13900139002', 1, NOW(), NOW(), 0),
(3, 1, 'clerk', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '店员', 'CLERK', '13900139003', 1, NOW(), NOW(), 0);
-- 密码均为: test123456

-- 创建测试优惠券
INSERT INTO coupon (id, name, type, discount, min_amount, total, per_limit, start_time, end_time, status, created_at, updated_at, deleted)
VALUES 
(1, '新人优惠券', 'MONEY', 10.00, 50.00, 1000, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW(), 0),
(2, '满减券', 'MONEY', 20.00, 100.00, 500, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW(), 0);

-- 创建测试拼团活动
INSERT INTO group_buy (id, product_id, sku_id, group_size, group_price, start_time, end_time, status, created_at, updated_at, deleted)
VALUES 
(1, 1, 1, 3, 25.90, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, NOW(), NOW(), 0);
