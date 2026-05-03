-- =====================================================================
-- 鲜果记（Xianguoji）单商户精品生鲜小程序  数据库 Schema
-- 引擎: InnoDB    字符集: utf8mb4    校对: utf8mb4_unicode_ci
-- 适用范围: 用户端 + 商家端
-- =====================================================================

DROP DATABASE IF EXISTS `xianguoji`;
CREATE DATABASE `xianguoji` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xianguoji`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. 店铺信息（单商户：仅一条记录）
-- ---------------------------------------------------------------------
CREATE TABLE `shop` (
  `id`              INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `name`            VARCHAR(64)  NOT NULL                        COMMENT '店铺名称',
  `logo`            VARCHAR(255)                                 COMMENT '店铺Logo',
  `description`     VARCHAR(255)                                 COMMENT '店铺简介',
  `phone`           VARCHAR(20)                                  COMMENT '联系电话',
  `address`         VARCHAR(255)                                 COMMENT '营业地址',
  `business_hours`  VARCHAR(64)                                  COMMENT '营业时间，如 08:00-22:00',
  `is_open`         TINYINT      NOT NULL DEFAULT 1              COMMENT '0已打烊 1营业中',
  `auto_accept`     TINYINT      NOT NULL DEFAULT 0              COMMENT '自动接单开关',
  `voice_notify`    TINYINT      NOT NULL DEFAULT 1              COMMENT '来单语音播报',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='店铺信息（单商户）';

-- ---------------------------------------------------------------------
-- 2. 商家员工（店主、管理员、打包员、配送员）
-- ---------------------------------------------------------------------
CREATE TABLE `staff` (
  `id`            BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `username`      VARCHAR(32)  NOT NULL UNIQUE                   COMMENT '登录账号',
  `password_hash` VARCHAR(128) NOT NULL                          COMMENT 'bcrypt 密码哈希',
  `name`          VARCHAR(32)  NOT NULL                          COMMENT '姓名',
  `phone`         VARCHAR(20)                                    COMMENT '手机号',
  `avatar`        VARCHAR(255),
  `role`          VARCHAR(16)  NOT NULL DEFAULT 'admin'          COMMENT 'owner店主 / admin管理员 / packer打包员 / courier配送员',
  `permissions`   JSON                                           COMMENT '权限点 JSON 数组',
  `status`        TINYINT      NOT NULL DEFAULT 1                COMMENT '0禁用 1正常',
  `last_login_at` DATETIME,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_role` (`role`)
) ENGINE=InnoDB COMMENT='商家员工';

-- ---------------------------------------------------------------------
-- 3. 用户端用户
-- ---------------------------------------------------------------------
CREATE TABLE `user` (
  `id`               BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `phone`            VARCHAR(20)                                  COMMENT '手机号（登录）',
  `nickname`         VARCHAR(64),
  `avatar`           VARCHAR(255),
  `gender`           TINYINT NOT NULL DEFAULT 0                   COMMENT '0未知 1男 2女',
  `birthday`         DATE,
  `wx_openid`        VARCHAR(64)                                  COMMENT '微信 openid',
  `wx_unionid`       VARCHAR(64),
  `tag`              VARCHAR(16) NOT NULL DEFAULT 'new'           COMMENT 'new新客 / regular老客 / silent沉默',
  `status`           TINYINT NOT NULL DEFAULT 1                   COMMENT '0禁用 1正常',
  `register_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login_time`  DATETIME,
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_wx_openid` (`wx_openid`),
  KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB COMMENT='用户表';

-- ---------------------------------------------------------------------
-- 4. 收货地址
-- ---------------------------------------------------------------------
CREATE TABLE `user_address` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `consignee`  VARCHAR(32)  NOT NULL                              COMMENT '收货人',
  `phone`      VARCHAR(20)  NOT NULL,
  `province`   VARCHAR(32)  NOT NULL,
  `city`       VARCHAR(32)  NOT NULL,
  `district`   VARCHAR(32)  NOT NULL,
  `detail`     VARCHAR(128) NOT NULL                              COMMENT '详细地址',
  `tag`        VARCHAR(16)                                        COMMENT '家/公司/学校',
  `is_default` TINYINT      NOT NULL DEFAULT 0,
  `longitude`  DECIMAL(10,7),
  `latitude`   DECIMAL(10,7),
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='用户收货地址';

-- ---------------------------------------------------------------------
-- 5. 自提点
-- ---------------------------------------------------------------------
CREATE TABLE `pickup_point` (
  `id`             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `name`           VARCHAR(64)  NOT NULL,
  `address`        VARCHAR(255) NOT NULL,
  `phone`          VARCHAR(20),
  `business_hours` VARCHAR(64)                                    COMMENT '如 09:00-21:00',
  `longitude`      DECIMAL(10,7),
  `latitude`       DECIMAL(10,7),
  `status`         TINYINT NOT NULL DEFAULT 1                     COMMENT '0停用 1启用',
  `sort`           INT NOT NULL DEFAULT 0,
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='自提点';

-- ---------------------------------------------------------------------
-- 6. 商品分类（支持二级）
-- ---------------------------------------------------------------------
CREATE TABLE `category` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `parent_id`  BIGINT UNSIGNED NOT NULL DEFAULT 0                 COMMENT '0为一级分类',
  `name`       VARCHAR(32) NOT NULL,
  `icon`       VARCHAR(255)                                       COMMENT '一级分类图标',
  `sort`       INT NOT NULL DEFAULT 0,
  `status`     TINYINT NOT NULL DEFAULT 1                         COMMENT '0隐藏 1显示',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB COMMENT='商品分类';

-- ---------------------------------------------------------------------
-- 7. 商品（SPU）
-- ---------------------------------------------------------------------
CREATE TABLE `product` (
  `id`              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `name`            VARCHAR(128) NOT NULL,
  `subtitle`        VARCHAR(128)                                  COMMENT '简短卖点',
  `category_id`     BIGINT UNSIGNED NOT NULL                      COMMENT '二级分类id',
  `main_image`      VARCHAR(255) NOT NULL                         COMMENT '主图',
  `video_url`       VARCHAR(255),
  `description`     MEDIUMTEXT                                    COMMENT '商品介绍 富文本HTML',
  `min_price`       DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '最低售价（多规格冗余）',
  `max_price`       DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '最高售价',
  `total_stock`     INT NOT NULL DEFAULT 0                        COMMENT '总库存（冗余）',
  `sales`           INT NOT NULL DEFAULT 0                        COMMENT '累计销量',
  `is_recommend`    TINYINT NOT NULL DEFAULT 0                    COMMENT '店主推荐',
  `support_delivery` TINYINT NOT NULL DEFAULT 1                   COMMENT '支持同城配送',
  `support_pickup`   TINYINT NOT NULL DEFAULT 1                   COMMENT '支持自提',
  `status`          TINYINT NOT NULL DEFAULT 1                    COMMENT '0已下架 1在售 2回收站',
  `stock_warn_threshold` INT NOT NULL DEFAULT 5                   COMMENT '库存预警阈值',
  `sort`            INT NOT NULL DEFAULT 0,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_sales` (`sales`)
) ENGINE=InnoDB COMMENT='商品 SPU';

-- ---------------------------------------------------------------------
-- 8. 商品规格（SKU）
-- ---------------------------------------------------------------------
CREATE TABLE `product_sku` (
  `id`             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `product_id`     BIGINT UNSIGNED NOT NULL,
  `spec_name`      VARCHAR(64)  NOT NULL                          COMMENT '规格名 如 500g装 / 1kg装',
  `sku_code`       VARCHAR(64)                                    COMMENT 'SKU编码',
  `price`          DECIMAL(10,2) NOT NULL                         COMMENT '售价',
  `original_price` DECIMAL(10,2)                                  COMMENT '划线原价',
  `cost_price`     DECIMAL(10,2)                                  COMMENT '成本价',
  `stock`          INT NOT NULL DEFAULT 0,
  `sales`          INT NOT NULL DEFAULT 0,
  `is_default`     TINYINT NOT NULL DEFAULT 0                     COMMENT '默认规格',
  `status`         TINYINT NOT NULL DEFAULT 1                     COMMENT '0禁用 1启用',
  `sort`           INT NOT NULL DEFAULT 0,
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB COMMENT='商品规格 SKU';

-- ---------------------------------------------------------------------
-- 9. 商品图片（多图轮播 / 详情图）
-- ---------------------------------------------------------------------
CREATE TABLE `product_image` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `url`        VARCHAR(255) NOT NULL,
  `type`       TINYINT NOT NULL DEFAULT 1                         COMMENT '1轮播主图 2详情图',
  `sort`       INT NOT NULL DEFAULT 0,
  KEY `idx_product_id_type` (`product_id`, `type`)
) ENGINE=InnoDB COMMENT='商品图片';

-- ---------------------------------------------------------------------
-- 10. 购物车
-- ---------------------------------------------------------------------
CREATE TABLE `cart_item` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `sku_id`     BIGINT UNSIGNED NOT NULL,
  `quantity`   INT NOT NULL DEFAULT 1,
  `selected`   TINYINT NOT NULL DEFAULT 1                         COMMENT '是否勾选',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='购物车';

-- ---------------------------------------------------------------------
-- 11. 优惠券模板
-- ---------------------------------------------------------------------
CREATE TABLE `coupon` (
  `id`             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `name`           VARCHAR(64) NOT NULL,
  `type`           TINYINT NOT NULL DEFAULT 1                     COMMENT '1满减 2折扣',
  `amount`         DECIMAL(10,2) NOT NULL                         COMMENT '面额（折扣类型为折扣率0-1）',
  `min_amount`     DECIMAL(10,2) NOT NULL DEFAULT 0               COMMENT '使用门槛',
  `total`          INT NOT NULL DEFAULT 0                         COMMENT '发放总量 0不限',
  `received_count` INT NOT NULL DEFAULT 0                         COMMENT '已领取',
  `used_count`     INT NOT NULL DEFAULT 0                         COMMENT '已核销',
  `per_user_limit` INT NOT NULL DEFAULT 1                         COMMENT '每人限领',
  `valid_type`     TINYINT NOT NULL DEFAULT 1                     COMMENT '1绝对日期 2领取后N天',
  `start_time`     DATETIME,
  `end_time`       DATETIME,
  `valid_days`     INT                                            COMMENT '领取后有效天数',
  `scope`          TINYINT NOT NULL DEFAULT 1                     COMMENT '1全部商品 2指定商品',
  `scope_product_ids` JSON                                        COMMENT '指定商品ID数组',
  `status`         TINYINT NOT NULL DEFAULT 1                     COMMENT '0未开始 1进行中 2已结束',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='优惠券模板';

-- ---------------------------------------------------------------------
-- 12. 用户领取的优惠券
-- ---------------------------------------------------------------------
CREATE TABLE `user_coupon` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `coupon_id`  BIGINT UNSIGNED NOT NULL,
  `status`     TINYINT NOT NULL DEFAULT 0                         COMMENT '0未使用 1已使用 2已过期',
  `order_id`   BIGINT UNSIGNED                                    COMMENT '使用订单',
  `received_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `used_at`    DATETIME,
  `expire_at`  DATETIME,
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB COMMENT='用户优惠券';

-- ---------------------------------------------------------------------
-- 13. 订单
-- ---------------------------------------------------------------------
CREATE TABLE `order` (
  `id`              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `order_no`        VARCHAR(32) NOT NULL UNIQUE                   COMMENT '订单编号',
  `user_id`         BIGINT UNSIGNED NOT NULL,
  -- 状态
  `status`          TINYINT NOT NULL DEFAULT 0
    COMMENT '0待付款 1待接单 2备货中 3配送中 4待自提 5已完成 6已取消 7退款中 8已退款',
  `pay_status`      TINYINT NOT NULL DEFAULT 0                    COMMENT '0未支付 1已支付 2已退款',
  -- 配送 / 自提
  `delivery_type`   TINYINT NOT NULL                              COMMENT '1同城配送 2到店自提',
  `delivery_time`   VARCHAR(64)                                   COMMENT '预约时段，如 今日18:00-20:00',
  `address_id`      BIGINT UNSIGNED                               COMMENT '配送地址',
  `consignee`       VARCHAR(32)                                   COMMENT '冗余',
  `consignee_phone` VARCHAR(20),
  `consignee_address` VARCHAR(255),
  `pickup_point_id` BIGINT UNSIGNED                               COMMENT '自提点',
  `pickup_code`     VARCHAR(8)                                    COMMENT '自提码',
  -- 金额
  `goods_amount`    DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '商品总价',
  `coupon_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '优惠券抵扣',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '满减优惠',
  `delivery_fee`    DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '配送费',
  `pay_amount`      DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '实付款',
  `user_coupon_id`  BIGINT UNSIGNED                               COMMENT '使用的用户优惠券',
  -- 支付
  `pay_method`      VARCHAR(16)                                   COMMENT 'wechat / alipay',
  `pay_time`        DATETIME,
  `pay_trade_no`    VARCHAR(64)                                   COMMENT '第三方流水号',
  -- 备注
  `user_remark`     VARCHAR(255)                                  COMMENT '用户备注',
  `cancel_reason`   VARCHAR(255),
  -- 拼团
  `group_buy_instance_id` BIGINT UNSIGNED                         COMMENT '所属拼团',
  -- 配送
  `courier_name`    VARCHAR(32),
  `courier_phone`   VARCHAR(20),
  `delivered_at`    DATETIME                                      COMMENT '送达 / 自提完成时间',
  `finished_at`     DATETIME                                      COMMENT '订单完成时间',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_pickup_code` (`pickup_code`)
) ENGINE=InnoDB COMMENT='订单';

-- ---------------------------------------------------------------------
-- 14. 订单商品明细
-- ---------------------------------------------------------------------
CREATE TABLE `order_item` (
  `id`             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `order_id`       BIGINT UNSIGNED NOT NULL,
  `product_id`     BIGINT UNSIGNED NOT NULL,
  `sku_id`         BIGINT UNSIGNED NOT NULL,
  -- 下单时快照
  `product_name`   VARCHAR(128) NOT NULL,
  `spec_name`      VARCHAR(64)  NOT NULL,
  `image`          VARCHAR(255) NOT NULL,
  `price`          DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2),
  `quantity`       INT NOT NULL,
  `subtotal`       DECIMAL(10,2) NOT NULL,
  `is_reviewed`    TINYINT NOT NULL DEFAULT 0                     COMMENT '是否已评价',
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB COMMENT='订单商品';

-- ---------------------------------------------------------------------
-- 15. 订单状态日志（操作日志时间轴）
-- ---------------------------------------------------------------------
CREATE TABLE `order_status_log` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `order_id`   BIGINT UNSIGNED NOT NULL,
  `from_status` TINYINT,
  `to_status`  TINYINT NOT NULL,
  `operator_type` TINYINT NOT NULL                                COMMENT '1用户 2商家 3系统',
  `operator_id` BIGINT UNSIGNED,
  `remark`     VARCHAR(255),
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB COMMENT='订单状态流转日志';

-- ---------------------------------------------------------------------
-- 16. 退款 / 售后申请
-- ---------------------------------------------------------------------
CREATE TABLE `refund` (
  `id`            BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `refund_no`     VARCHAR(32) NOT NULL UNIQUE,
  `order_id`      BIGINT UNSIGNED NOT NULL,
  `user_id`       BIGINT UNSIGNED NOT NULL,
  `type`          TINYINT NOT NULL DEFAULT 1                     COMMENT '1仅退款 2退货退款',
  `amount`        DECIMAL(10,2) NOT NULL,
  `reason`        VARCHAR(255),
  `images`        JSON                                           COMMENT '凭证图片URL数组',
  `status`        TINYINT NOT NULL DEFAULT 0                     COMMENT '0待审核 1已同意 2已拒绝 3已退款',
  `reject_reason` VARCHAR(255),
  `handled_by`    BIGINT UNSIGNED                                COMMENT '处理员工ID',
  `handled_at`    DATETIME,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='退款 / 售后';

-- ---------------------------------------------------------------------
-- 17. 拼团活动（针对某 SKU 的拼团配置）
-- ---------------------------------------------------------------------
CREATE TABLE `group_buy_activity` (
  `id`               BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `product_id`       BIGINT UNSIGNED NOT NULL,
  `sku_id`           BIGINT UNSIGNED NOT NULL,
  `group_price`      DECIMAL(10,2) NOT NULL                       COMMENT '拼团价',
  `group_size`       INT NOT NULL DEFAULT 3                       COMMENT '成团人数',
  `valid_hours`      INT NOT NULL DEFAULT 24                      COMMENT '拼团时效（小时）',
  `start_time`       DATETIME NOT NULL,
  `end_time`         DATETIME NOT NULL,
  `total_join_count` INT NOT NULL DEFAULT 0                       COMMENT '参团人次',
  `success_count`    INT NOT NULL DEFAULT 0                       COMMENT '成团数',
  `status`           TINYINT NOT NULL DEFAULT 1                   COMMENT '0已结束 1进行中',
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='拼团活动配置';

-- ---------------------------------------------------------------------
-- 18. 拼团实例（一次开团）
-- ---------------------------------------------------------------------
CREATE TABLE `group_buy_instance` (
  `id`           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `activity_id`  BIGINT UNSIGNED NOT NULL,
  `leader_id`    BIGINT UNSIGNED NOT NULL                          COMMENT '团长用户ID',
  `current_size` INT NOT NULL DEFAULT 1,
  `target_size`  INT NOT NULL,
  `status`       TINYINT NOT NULL DEFAULT 1                        COMMENT '1拼团中 2已成团 3已失败',
  `expire_at`    DATETIME NOT NULL,
  `success_at`   DATETIME,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='拼团实例';

-- ---------------------------------------------------------------------
-- 19. 拼团参与者
-- ---------------------------------------------------------------------
CREATE TABLE `group_buy_participant` (
  `id`          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `instance_id` BIGINT UNSIGNED NOT NULL,
  `user_id`     BIGINT UNSIGNED NOT NULL,
  `order_id`    BIGINT UNSIGNED NOT NULL,
  `is_leader`   TINYINT NOT NULL DEFAULT 0,
  `joined_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_instance_user` (`instance_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='拼团参与者';

-- ---------------------------------------------------------------------
-- 20. 评价
-- ---------------------------------------------------------------------
CREATE TABLE `review` (
  `id`              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `order_id`        BIGINT UNSIGNED NOT NULL,
  `order_item_id`   BIGINT UNSIGNED NOT NULL,
  `product_id`      BIGINT UNSIGNED NOT NULL,
  `user_id`         BIGINT UNSIGNED NOT NULL,
  `rating`          TINYINT NOT NULL                              COMMENT '总评 1-5',
  `freshness_rating` TINYINT                                      COMMENT '新鲜度',
  `value_rating`    TINYINT                                       COMMENT '性价比',
  `package_rating`  TINYINT                                       COMMENT '包装',
  `content`         VARCHAR(1000),
  `images`          JSON                                          COMMENT '图片URL数组',
  `is_anonymous`    TINYINT NOT NULL DEFAULT 0,
  `is_hidden`       TINYINT NOT NULL DEFAULT 0                    COMMENT '商家隐藏',
  `merchant_reply`  VARCHAR(500),
  `replied_at`      DATETIME,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_rating` (`rating`)
) ENGINE=InnoDB COMMENT='商品评价';

-- ---------------------------------------------------------------------
-- 21. 收藏
-- ---------------------------------------------------------------------
CREATE TABLE `favorite` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB COMMENT='商品收藏';

-- ---------------------------------------------------------------------
-- 22. 浏览足迹
-- ---------------------------------------------------------------------
CREATE TABLE `footprint` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `viewed_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_viewed` (`user_id`, `viewed_at`)
) ENGINE=InnoDB COMMENT='浏览足迹';

-- ---------------------------------------------------------------------
-- 23. 消息中心
-- ---------------------------------------------------------------------
CREATE TABLE `message` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL                            COMMENT '0为全员广播',
  `type`       TINYINT NOT NULL                                    COMMENT '1系统通知 2订单消息 3优惠活动 4拼团动态',
  `title`      VARCHAR(128) NOT NULL,
  `content`    VARCHAR(500),
  `link_url`   VARCHAR(255)                                        COMMENT '跳转地址',
  `is_read`    TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_type` (`user_id`, `type`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB COMMENT='用户消息';

-- ---------------------------------------------------------------------
-- 24. 轮播图 / Banner
-- ---------------------------------------------------------------------
CREATE TABLE `banner` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `title`      VARCHAR(64),
  `image`      VARCHAR(255) NOT NULL,
  `link_type`  TINYINT NOT NULL DEFAULT 0                          COMMENT '0无 1商品 2分类 3活动 4外链',
  `link_value` VARCHAR(255),
  `sort`       INT NOT NULL DEFAULT 0,
  `status`     TINYINT NOT NULL DEFAULT 1,
  `start_time` DATETIME,
  `end_time`   DATETIME,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB COMMENT='首页轮播';

-- ---------------------------------------------------------------------
-- 25. 搜索历史 / 热门搜索
-- ---------------------------------------------------------------------
CREATE TABLE `search_history` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `keyword`    VARCHAR(64) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='搜索历史';

CREATE TABLE `hot_search` (
  `id`      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `keyword` VARCHAR(64) NOT NULL,
  `sort`    INT NOT NULL DEFAULT 0,
  `status`  TINYINT NOT NULL DEFAULT 1,
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB COMMENT='热门搜索';

-- ---------------------------------------------------------------------
-- 26. 用户反馈
-- ---------------------------------------------------------------------
CREATE TABLE `feedback` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id`    BIGINT UNSIGNED NOT NULL,
  `type`       VARCHAR(32)                                         COMMENT '反馈类型',
  `content`    VARCHAR(1000) NOT NULL,
  `images`     JSON,
  `contact`    VARCHAR(64),
  `status`     TINYINT NOT NULL DEFAULT 0                          COMMENT '0待处理 1已处理',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='用户反馈';

-- ---------------------------------------------------------------------
-- 27. 配送费规则
-- ---------------------------------------------------------------------
CREATE TABLE `delivery_setting` (
  `id`             INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0              COMMENT '起送价',
  `base_fee`       DECIMAL(10,2) NOT NULL DEFAULT 0                COMMENT '基础配送费',
  `free_amount`    DECIMAL(10,2) NOT NULL DEFAULT 0                COMMENT '满X元免运费',
  `time_slots`     JSON                                            COMMENT '可选时段 [{label,start,end}]',
  `service_area`   JSON                                            COMMENT '配送范围 GeoJSON',
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='配送设置';

-- ---------------------------------------------------------------------
-- 28. 满减促销规则
-- ---------------------------------------------------------------------
CREATE TABLE `promotion_rule` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `name`       VARCHAR(64) NOT NULL,
  `min_amount` DECIMAL(10,2) NOT NULL                              COMMENT '满X元',
  `discount`   DECIMAL(10,2) NOT NULL                              COMMENT '减Y元',
  `start_time` DATETIME,
  `end_time`   DATETIME,
  `status`     TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='满减规则';

-- ---------------------------------------------------------------------
-- 29. 通知设置（商家端：哪些事件触发通知）
-- ---------------------------------------------------------------------
CREATE TABLE `notify_setting` (
  `id`             INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `event_key`      VARCHAR(32) NOT NULL UNIQUE                     COMMENT '事件键 new_order/refund/stock_warn',
  `event_name`     VARCHAR(64) NOT NULL,
  `enable_voice`   TINYINT NOT NULL DEFAULT 1,
  `enable_sms`     TINYINT NOT NULL DEFAULT 0,
  `enable_app`     TINYINT NOT NULL DEFAULT 1,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='商家通知设置';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 初始化数据 / 种子数据
-- =====================================================================

-- 店铺信息
INSERT INTO `shop` (`name`, `description`, `phone`, `address`, `business_hours`, `is_open`)
VALUES ('鲜果记', '新鲜直达，每日精选', '0571-12345678', '杭州市西湖区文三路 100 号', '08:00-22:00', 1);

-- 默认店主账号  密码: 123456 （bcrypt 占位 hash，正式上线请替换）
INSERT INTO `staff` (`username`, `password_hash`, `name`, `phone`, `role`) VALUES
('admin',  '$2b$10$.Xv/qEaxR5uW/.xIgcvbw.z3uf/0m78Lvq3Pk/cJi7T6QE7mjemo6', '张老板', '13800000000', 'owner'),
('packer', '$2b$10$.Xv/qEaxR5uW/.xIgcvbw.z3uf/0m78Lvq3Pk/cJi7T6QE7mjemo6', '小王',   '13800000001', 'packer');

-- 一级分类
INSERT INTO `category` (`id`, `parent_id`, `name`, `icon`, `sort`) VALUES
(1, 0, '叶菜',   '/static/cat/leaf.png',     1),
(2, 0, '水果',   '/static/cat/fruit.png',    2),
(3, 0, '根茎',   '/static/cat/root.png',     3),
(4, 0, '菌菇',   '/static/cat/mushroom.png', 4),
(5, 0, '肉禽蛋', '/static/cat/meat.png',     5),
(6, 0, '水产',   '/static/cat/seafood.png',  6),
(7, 0, '豆制品', '/static/cat/bean.png',     7),
(8, 0, '烘焙',   '/static/cat/bakery.png',   8);

-- 二级分类
INSERT INTO `category` (`id`, `parent_id`, `name`, `sort`) VALUES
(101, 1, '叶菜类', 1),
(201, 2, '当季鲜果', 1),
(202, 2, '进口水果', 2),
(203, 2, '礼盒装',   3),
(301, 3, '根茎类',   1),
(401, 4, '鲜菌',     1),
(501, 5, '鲜肉',     1),
(502, 5, '禽类',     2),
(503, 5, '蛋品',     3);

-- 自提点
INSERT INTO `pickup_point` (`name`, `address`, `phone`, `business_hours`, `longitude`, `latitude`) VALUES
('文三路总店',   '杭州市西湖区文三路 100 号', '0571-12345678', '08:00-22:00', 120.1234567, 30.2876543),
('滨江分点',     '杭州市滨江区江南大道 88 号',  '0571-87654321', '09:00-21:00', 120.2098765, 30.2056789);

-- 商品
INSERT INTO `product` (`id`, `name`, `subtitle`, `category_id`, `main_image`, `min_price`, `max_price`, `total_stock`, `sales`, `is_recommend`) VALUES
(1, '阳光玫瑰葡萄',         '当日新摘 现剪现发', 201, '/static/p/grape.jpg',   29.90, 89.00, 200, 350, 1),
(2, '智利进口车厘子 JJJ级',  '空运直达 颗颗甜爽', 202, '/static/p/cherry.jpg',  168.00, 288.00, 80, 120, 1),
(3, '四川蒲江红心猕猴桃',    '果香浓郁 软糯香甜', 201, '/static/p/kiwi.jpg',    29.90, 59.00, 150, 220, 1),
(4, '有机菜心',              '当日采摘 清甜脆嫩', 101, '/static/p/caixin.jpg',  4.50, 8.50, 300, 1200, 1),
(5, '正宗散养土鸡蛋',        '柴火灶喂养 蛋黄红润', 503, '/static/p/egg.jpg',     18.90, 35.00, 120, 300, 1),
(6, '新鲜红富士苹果',        '口感清甜 脆爽多汁', 201, '/static/p/apple.jpg',   12.80, 22.80, 250, 500, 0),
(7, '云南松茸',              '高原野生 限量新到', 401, '/static/p/songrong.jpg',128.00, 268.00, 30, 45, 0),
(8, '黑猪五花肉',            '当日鲜屠 肥瘦相间', 501, '/static/p/pork.jpg',    36.80, 69.00, 60, 180, 0);

-- SKU
INSERT INTO `product_sku` (`product_id`, `spec_name`, `sku_code`, `price`, `original_price`, `cost_price`, `stock`, `is_default`) VALUES
(1, '500g装',     'SKU-1-1', 29.90, 35.80, 18.00, 100, 1),
(1, '1kg装',      'SKU-1-2', 56.00, 65.00, 35.00,  80, 0),
(1, '2kg礼盒装',  'SKU-1-3', 89.00, 99.00, 60.00,  20, 0),
(2, '1kg礼盒',    'SKU-2-1', 168.00, 198.00, 110.00, 50, 1),
(2, '2.5kg礼盒',  'SKU-2-2', 288.00, 328.00, 200.00, 30, 0),
(3, '15枚装',     'SKU-3-1', 39.90, 49.00, 22.00, 100, 1),
(3, '30枚装礼盒', 'SKU-3-2', 59.00, 79.00, 38.00,  50, 0),
(4, '250g/份',    'SKU-4-1',  4.50,  5.90,  2.00, 300, 1),
(4, '500g/份',    'SKU-4-2',  8.50, 10.50,  4.00, 200, 0),
(5, '10枚装',     'SKU-5-1', 18.90, 22.00, 12.00, 80, 1),
(5, '20枚装',     'SKU-5-2', 35.00, 42.00, 24.00, 40, 0),
(6, '500g装',     'SKU-6-1', 12.80, 15.80,  7.00, 150, 1),
(6, '1kg装',      'SKU-6-2', 22.80, 26.80, 14.00, 100, 0),
(7, '250g',       'SKU-7-1', 128.00, 158.00, 80.00, 20, 1),
(7, '500g礼盒',   'SKU-7-2', 268.00, 298.00, 180.00, 10, 0),
(8, '500g',       'SKU-8-1', 36.80, 42.00, 22.00, 40, 1),
(8, '1kg',        'SKU-8-2', 69.00, 78.00, 42.00, 20, 0);

-- 商品图（每个商品一张主图样例）
INSERT INTO `product_image` (`product_id`, `url`, `type`, `sort`) VALUES
(1, '/static/p/grape.jpg',   1, 1),
(2, '/static/p/cherry.jpg',  1, 1),
(3, '/static/p/kiwi.jpg',    1, 1),
(4, '/static/p/caixin.jpg',  1, 1),
(5, '/static/p/egg.jpg',     1, 1),
(6, '/static/p/apple.jpg',   1, 1),
(7, '/static/p/songrong.jpg',1, 1),
(8, '/static/p/pork.jpg',    1, 1);

-- 优惠券模板
INSERT INTO `coupon` (`name`, `type`, `amount`, `min_amount`, `total`, `per_user_limit`, `valid_type`, `start_time`, `end_time`, `scope`, `status`) VALUES
('新人满 39 减 5',  1,  5.00, 39.00, 1000, 1, 1, '2026-01-01', '2026-12-31', 1, 1),
('满 69 减 10',     1, 10.00, 69.00, 2000, 5, 1, '2026-01-01', '2026-12-31', 1, 1),
('满 129 减 20',    1, 20.00,129.00, 1000, 5, 1, '2026-01-01', '2026-12-31', 1, 1);

-- 满减规则（与配送页满减提示对齐）
INSERT INTO `promotion_rule` (`name`, `min_amount`, `discount`, `status`) VALUES
('满 39 减 5',   39.00,  5.00, 1),
('满 99 减 15',  99.00, 15.00, 1);

-- 拼团活动（土鸡蛋 拼团）
INSERT INTO `group_buy_activity` (`product_id`, `sku_id`, `group_price`, `group_size`, `valid_hours`, `start_time`, `end_time`, `status`)
VALUES (5, 10, 15.90, 3, 24, '2026-05-01 00:00:00', '2026-06-30 23:59:59', 1);

-- 配送设置
INSERT INTO `delivery_setting` (`min_order_amount`, `base_fee`, `free_amount`, `time_slots`)
VALUES (20.00, 5.00, 39.00, JSON_ARRAY(
  JSON_OBJECT('label','今日 18:00-20:00','start','18:00','end','20:00'),
  JSON_OBJECT('label','明日上午','start','09:00','end','12:00'),
  JSON_OBJECT('label','明日下午','start','14:00','end','18:00')
));

-- 商家通知设置
INSERT INTO `notify_setting` (`event_key`, `event_name`, `enable_voice`, `enable_app`) VALUES
('new_order',  '新订单',     1, 1),
('refund',     '退款申请',   1, 1),
('stock_warn', '库存预警',   0, 1);

-- 热门搜索
INSERT INTO `hot_search` (`keyword`, `sort`) VALUES
('车厘子', 1), ('葡萄', 2), ('土鸡蛋', 3), ('松茸', 4), ('菜心', 5);

-- Banner
INSERT INTO `banner` (`title`, `image`, `link_type`, `link_value`, `sort`, `status`) VALUES
('夏日西瓜节',         '/static/banner/summer.jpg', 3, 'group-buy',  1, 1),
('新会员立减 10 元',   '/static/banner/newuser.jpg', 0, NULL,         2, 1);

-- 测试用户
INSERT INTO `user` (`id`, `phone`, `nickname`, `avatar`, `tag`) VALUES
(1, '13900000001', '小张', '/static/avatar/u1.jpg', 'new'),
(2, '13900000002', '老李', '/static/avatar/u2.jpg', 'regular');

-- 测试地址
INSERT INTO `user_address` (`user_id`, `consignee`, `phone`, `province`, `city`, `district`, `detail`, `tag`, `is_default`) VALUES
(1, '小张', '13900000001', '浙江省', '杭州市', '西湖区', '文三路 200 号 1 幢 502', '家',   1),
(1, '小张', '13900000001', '浙江省', '杭州市', '西湖区', '文一路阿里巴巴 1 号楼',   '公司', 0);

-- 用户已领取的优惠券
INSERT INTO `user_coupon` (`user_id`, `coupon_id`, `status`, `expire_at`) VALUES
(1, 1, 0, '2026-12-31 23:59:59'),
(1, 2, 0, '2026-12-31 23:59:59');

-- 同步商品冗余字段
UPDATE `product` p
SET `min_price` = (SELECT MIN(price) FROM `product_sku` WHERE product_id = p.id),
    `max_price` = (SELECT MAX(price) FROM `product_sku` WHERE product_id = p.id),
    `total_stock` = (SELECT COALESCE(SUM(stock),0) FROM `product_sku` WHERE product_id = p.id);
