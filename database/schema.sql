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
  `order_id`    BIGINT UNSIGNED COMMENT '关联订单ID，未下单时为NULL',
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

-- ---------------------------------------------------------------------
-- 30. 商家端通知（管理员消息中心）
-- ---------------------------------------------------------------------
CREATE TABLE `admin_notification` (
  `id`          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `type`        TINYINT NOT NULL                                    COMMENT '1订单 2库存 3评价 4营销 5系统',
  `title`       VARCHAR(128) NOT NULL,
  `content`     VARCHAR(500),
  `link_url`    VARCHAR(255)                                        COMMENT '跳转地址',
  `is_read`     TINYINT NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB COMMENT='商家端通知';

-- =====================================================================
-- 初始化数据 / 种子数据
-- =====================================================================

-- 店铺信息
INSERT INTO `shop` (`name`, `logo`, `description`, `phone`, `address`, `business_hours`, `is_open`)
VALUES ('鲜果记', '/static/seed/shop/logo.jpg', '新鲜直达，每日精选', '0571-12345678', '杭州市西湖区文三路 100 号', '08:00-22:00', 1);

-- 默认店主账号  密码: 123456 （bcrypt 占位 hash，正式上线请替换）
INSERT INTO `staff` (`username`, `password_hash`, `name`, `phone`, `role`) VALUES
('admin',  '$2b$10$.Xv/qEaxR5uW/.xIgcvbw.z3uf/0m78Lvq3Pk/cJi7T6QE7mjemo6', '张老板', '13800000000', 'owner'),
('packer', '$2b$10$.Xv/qEaxR5uW/.xIgcvbw.z3uf/0m78Lvq3Pk/cJi7T6QE7mjemo6', '小王',   '13800000001', 'packer');

-- 一级分类（icon 字段对应前端 svg-icon 组件的 name）
INSERT INTO `category` (`id`, `parent_id`, `name`, `icon`, `sort`) VALUES
(1, 0, '时令热销', 'fruit_cherries',  1),
(2, 0, '进口鲜果', 'leafy_greens',    2),
(3, 0, '精品礼盒', 'gift',            3),
(4, 0, '国产优选', 'eco',             4),
(5, 0, '果干果仁', 'bakery_dining',   5),
(6, 0, '果汁甜点', 'liquor',          6),
(7, 0, '有机蔬菜', 'potted_plant',    7),
(8, 0, '肉蛋水产', 'meat_egg',        8);

-- 二级分类
INSERT INTO `category` (`id`, `parent_id`, `name`, `sort`) VALUES
(101, 1, '当季鲜果', 1),
(102, 1, '热带水果', 2),
(201, 2, '进口浆果', 1),
(202, 2, '进口核果', 2),
(301, 3, '鲜果礼盒', 1),
(302, 3, '混合礼盒', 2),
(401, 4, '柑橘类',   1),
(402, 4, '瓜果类',   2),
(501, 5, '果干',     1),
(502, 5, '坚果',     2),
(601, 6, '鲜榨果汁', 1),
(602, 6, '果泥甜点', 2),
(701, 7, '叶菜类',   1),
(702, 7, '根茎类',   2),
(801, 8, '鲜鸡蛋',   1),
(802, 8, '海鲜水产', 2);

-- 自提点
INSERT INTO `pickup_point` (`name`, `address`, `phone`, `business_hours`, `longitude`, `latitude`) VALUES
('文三路总店',   '杭州市西湖区文三路 100 号', '0571-12345678', '08:00-22:00', 120.1234567, 30.2876543),
('滨江分点',     '杭州市滨江区江南大道 88 号',  '0571-87654321', '09:00-21:00', 120.2098765, 30.2056789);

-- 商品
INSERT INTO `product` (`id`, `name`, `subtitle`, `category_id`, `main_image`, `min_price`, `max_price`, `total_stock`, `sales`, `is_recommend`) VALUES
(1, '阳光玫瑰葡萄',         '当日新摘 现剪现发', 101, '/static/seed/product/grape.jpg',      29.90, 89.00, 200, 350, 1),
(2, '智利进口车厘子 JJJ级',  '空运直达 颗颗甜爽', 201, '/static/seed/product/cherry.jpg',    168.00, 288.00, 80, 120, 1),
(3, '丹东99草莓',            '现摘现发 颗颗饱满', 101, '/static/seed/product/strawberry.jpg', 39.90, 79.00, 150, 220, 1),
(4, '海南金煌芒果',          '树上自然熟 甜过初恋', 102, '/static/seed/product/mango.jpg',     19.90, 49.00, 300, 1200, 1),
(5, '赣南脐橙',              '皮薄多汁 橙香浓郁', 401, '/static/seed/product/orange.jpg',    12.80, 35.00, 120, 300, 1),
(6, '新鲜红富士苹果',        '口感清甜 脆爽多汁', 402, '/static/seed/product/apple.jpg',     12.80, 22.80, 250, 500, 0),
(7, '精品水果礼盒',          '6种鲜果 精美包装', 301, '/static/seed/product/giftbox.jpg',   128.00, 268.00, 30, 45, 0),
(8, '小蜜蜂蓝莓',            '颗粒饱满 花香浓郁', 201, '/static/seed/product/blueberry.jpg', 29.90, 59.00, 60, 180, 0),
(9, '水蜜桃',                '果肉细腻 汁多味甜', 102, '/static/seed/product/peach.jpg',     25.00, 45.00, 100, 260, 0),
(10, '黑美人西瓜',            '沙瓤清甜 消暑首选', 402, '/static/seed/product/watermelon.jpg', 15.00, 35.00, 80, 420, 0),
-- ---- 售罄商品（total_stock=0） ----
(11, '云南蓝莓大果',           '颗粒饱满 花香浓郁', 201, '/static/seed/product/blueberry.jpg',  29.90, 59.00, 0, 180, 1),
-- ---- 已下架商品（status=0） ----
(12, '泰国山竹',               '果后驾到 细腻清甜', 202, '/static/seed/product/mangosteen.jpg', 45.00, 79.00, 60, 90, 0),
-- ---- 仅配送（support_pickup=0） ----
(13, '新西兰奇异果',           '维C之王 酸甜可口', 201, '/static/seed/product/kiwi.jpg',       19.90, 39.90, 120, 260, 1),
-- ---- 仅自提（support_delivery=0） ----
(14, '芒果班戟',               '现做现发 绵密奶油', 602, '/static/seed/product/crepe.jpg',      38.00, 38.00, 15, 320, 1),
-- ---- 有机蔬菜 ----
(15, '有机小番茄',             '农场直供 鲜嫩多汁', 701, '/static/seed/product/tomato.jpg',     18.80, 18.80, 120, 640, 1),
(16, '有机胡萝卜',             '甜脆爽口 营养丰富', 702, '/static/seed/product/carrot.jpg',     12.50, 12.50, 90, 280, 0),
-- ---- 肉蛋水产 ----
(17, '农家土鸡蛋 30枚',         '散养走地鸡 蛋黄饱满', 801, '/static/seed/product/egg.jpg',       32.00, 32.00, 60, 1500, 1),
-- ---- 售罄水产 ----
(18, '鲜活大闸蟹 4两公',        '阳澄湖直发 鲜活到家', 802, '/static/seed/product/crab.jpg',     88.00, 128.00, 0, 420, 0),
-- ---- 低库存预警（stock < warn_threshold=5） ----
(19, '榴莲千层蛋糕',            '浓郁香软 限量供应', 602, '/static/seed/product/duriancake.jpg', 68.00, 68.00, 3, 150, 1),
-- ---- 果干坚果 ----
(20, '每日坚果混合装 30包',      '7种坚果 营养均衡', 502, '/static/seed/product/nuts.jpg',       59.90, 89.90, 500, 4600, 1),
(21, '芒果干大袋装',             '自然风干 酸甜可口', 501, '/static/seed/product/mangodry.jpg',   15.90, 15.90, 400, 2100, 0),
-- ---- 鲜榨果汁 ----
(22, 'NFC鲜榨橙汁 1L',          '0添加 原汁原味', 601, '/static/seed/product/orangejuice.jpg', 22.90, 22.90, 200, 980, 0);

-- SKU
INSERT INTO `product_sku` (`product_id`, `spec_name`, `sku_code`, `price`, `original_price`, `cost_price`, `stock`, `is_default`) VALUES
(1, '500g装',     'SKU-1-1', 29.90, 35.80, 18.00, 100, 1),
(1, '1kg装',      'SKU-1-2', 56.00, 65.00, 35.00,  80, 0),
(1, '2kg礼盒装',  'SKU-1-3', 89.00, 99.00, 60.00,  20, 0),
(2, '1kg礼盒',    'SKU-2-1', 168.00, 198.00, 110.00, 50, 1),
(2, '2.5kg礼盒',  'SKU-2-2', 288.00, 328.00, 200.00, 30, 0),
(3, '300g装',     'SKU-3-1', 39.90, 49.00, 22.00, 100, 1),
(3, '500g装',     'SKU-3-2', 59.00, 79.00, 38.00,  50, 0),
(4, '1kg装',      'SKU-4-1', 19.90, 25.00, 10.00, 200, 1),
(4, '2.5kg装',    'SKU-4-2', 49.00, 58.00, 25.00, 100, 0),
(5, '5斤装',      'SKU-5-1', 12.80, 16.00,  7.00, 150, 1),
(5, '10斤装',     'SKU-5-2', 22.80, 28.00, 14.00, 80, 0),
(6, '500g装',     'SKU-6-1', 12.80, 15.80,  7.00, 150, 1),
(6, '1kg装',      'SKU-6-2', 22.80, 26.80, 14.00, 100, 0),
(7, '标准礼盒',   'SKU-7-1', 128.00, 158.00, 80.00, 20, 1),
(7, '豪华礼盒',   'SKU-7-2', 268.00, 298.00, 180.00, 10, 0),
(8, '125g装',     'SKU-8-1', 29.90, 35.00, 18.00, 60, 1),
(8, '250g装',     'SKU-8-2', 59.00, 69.00, 36.00, 40, 0),
(9, '2个装',      'SKU-9-1', 25.00, 32.00, 15.00, 80, 1),
(9, '4个装',      'SKU-9-2', 45.00, 58.00, 28.00, 50, 0),
(10, '1个约5斤',  'SKU-10-1', 15.00, 20.00,  8.00, 60, 1),
(10, '1个约8斤',  'SKU-10-2', 35.00, 42.00, 20.00, 30, 0),
-- ---- 售罄商品 SKU（stock=0） ----
(11, '125g装',    'SKU-11-1', 29.90, 35.00, 18.00, 0, 1),
(11, '250g装',    'SKU-11-2', 59.00, 69.00, 36.00, 0, 0),
-- ---- 已下架 ----
(12, '500g装',    'SKU-12-1', 45.00, 58.00, 28.00, 40, 1),
(12, '1kg装',     'SKU-12-2', 79.00, 99.00, 50.00, 20, 0),
-- ---- 仅配送 ----
(13, '6个装',     'SKU-13-1', 19.90, 25.00, 12.00, 80, 1),
(13, '12个装',    'SKU-13-2', 39.90, 49.00, 24.00, 40, 0),
-- ---- 仅自提 ----
(14, '4个装',     'SKU-14-1', 38.00, 45.00, 22.00, 15, 1),
-- ---- 有机蔬菜 ----
(15, '500g盒',    'SKU-15-1', 18.80, 22.00, 10.00, 120, 1),
(16, '1kg袋',     'SKU-16-1', 12.50, 15.00,  7.00, 90, 1),
-- ---- 肉蛋水产 ----
(17, '30枚盒',    'SKU-17-1', 32.00, 38.00, 20.00, 60, 1),
-- ---- 售罄水产 ----
(18, '4两公蟹 2只', 'SKU-18-1', 88.00, 108.00, 55.00, 0, 1),
(18, '4两公蟹 4只', 'SKU-18-2', 128.00, 158.00, 80.00, 0, 0),
-- ---- 低库存 ----
(19, '6寸',       'SKU-19-1', 68.00, 78.00, 38.00, 3, 1),
-- ---- 果干坚果 ----
(20, '30包箱',    'SKU-20-1', 59.90, 69.90, 35.00, 300, 1),
(20, '15包箱',    'SKU-20-2', 89.90, 109.00, 55.00, 200, 0),
(21, '大袋200g',  'SKU-21-1', 15.90, 19.90,  9.00, 400, 1),
-- ---- 鲜榨果汁 ----
(22, '1L瓶',      'SKU-22-1', 22.90, 28.00, 13.00, 200, 1);

-- 商品图（轮播主图 + 详情图）
INSERT INTO `product_image` (`product_id`, `url`, `type`, `sort`) VALUES
(1, '/static/seed/product/grape.jpg',      1, 1),
(1, '/static/seed/product/grape-2.jpg',    1, 2),
(1, '/static/seed/product/grape-3.jpg',    1, 3),
(1, '/static/seed/product/grape-d1.jpg',   2, 1),
(2, '/static/seed/product/cherry.jpg',     1, 1),
(2, '/static/seed/product/cherry-2.jpg',   1, 2),
(3, '/static/seed/product/strawberry.jpg', 1, 1),
(3, '/static/seed/product/strawberry-2.jpg',1, 2),
(4, '/static/seed/product/mango.jpg',      1, 1),
(5, '/static/seed/product/orange.jpg',     1, 1),
(6, '/static/seed/product/apple.jpg',      1, 1),
(7, '/static/seed/product/giftbox.jpg',    1, 1),
(8, '/static/seed/product/blueberry.jpg',  1, 1),
(9, '/static/seed/product/peach.jpg',      1, 1),
(10,'/static/seed/product/watermelon.jpg', 1, 1),
(11,'/static/seed/product/blueberry.jpg',  1, 1),
(12,'/static/seed/product/mangosteen.jpg', 1, 1),
(13,'/static/seed/product/kiwi.jpg',       1, 1),
(13,'/static/seed/product/kiwi-2.jpg',     1, 2),
(14,'/static/seed/product/crepe.jpg',      1, 1),
(15,'/static/seed/product/tomato.jpg',     1, 1),
(16,'/static/seed/product/carrot.jpg',     1, 1),
(17,'/static/seed/product/egg.jpg',        1, 1),
(18,'/static/seed/product/crab.jpg',       1, 1),
(19,'/static/seed/product/duriancake.jpg', 1, 1),
(20,'/static/seed/product/nuts.jpg',       1, 1),
(21,'/static/seed/product/mangodry.jpg',   1, 1),
(22,'/static/seed/product/orangejuice.jpg',1, 1);

-- 优惠券模板（覆盖：满减/折扣/已结束/指定商品/领取后N天生效）
INSERT INTO `coupon` (`name`, `type`, `amount`, `min_amount`, `total`, `per_user_limit`, `valid_type`, `start_time`, `end_time`, `valid_days`, `scope`, `scope_product_ids`, `status`) VALUES
('新人满 39 减 5',  1,  5.00, 39.00, 1000, 1, 1, '2026-01-01', '2026-12-31', NULL, 1, NULL, 1),
('满 69 减 10',     1, 10.00, 69.00, 2000, 5, 1, '2026-01-01', '2026-12-31', NULL, 1, NULL, 1),
('满 129 减 20',    1, 20.00,129.00, 1000, 5, 1, '2026-01-01', '2026-12-31', NULL, 1, NULL, 1),
('全场 9 折',       2,  0.90,  0.00, 500,  3, 1, '2026-05-01', '2026-06-30', NULL, 1, NULL, 1),
('葡萄专属券 减3',  1,  3.00,  0.00, 200,  2, 1, '2026-01-01', '2026-12-31', NULL, 2, '[1]', 1),
('领取后7天有效',   1,  8.00, 49.00, 300,  2, 2, NULL, NULL, 7, 1, NULL, 1),
('已过期满减',      1, 15.00, 99.00, 100,  1, 1, '2025-01-01', '2025-12-31', NULL, 1, NULL, 2);

-- 满减规则（与配送页满减提示对齐）
INSERT INTO `promotion_rule` (`name`, `min_amount`, `discount`, `status`) VALUES
('满 39 减 5',   39.00,  5.00, 1),
('满 99 减 15',  99.00, 15.00, 1);

-- 拼团活动（覆盖：进行中/已结束/不同人数）
INSERT INTO `group_buy_activity` (`id`, `product_id`, `sku_id`, `group_price`, `group_size`, `valid_hours`, `start_time`, `end_time`, `total_join_count`, `success_count`, `status`) VALUES
(1, 3,  6, 29.90, 3, 24, '2026-05-01 00:00:00', '2026-06-30 23:59:59', 45, 12, 1),
(2, 1,  1, 25.90, 2, 48, '2026-05-01 00:00:00', '2026-06-30 23:59:59', 80, 30, 1),
(3, 4,  8, 16.90, 3, 24, '2026-05-01 00:00:00', '2026-06-30 23:59:59', 36, 10, 1),
(4, 20, 35, 49.90, 5, 24, '2026-04-01 00:00:00', '2026-04-30 23:59:59', 50, 8, 0);

-- ---- 更新商品特殊字段 ----
-- 已下架
UPDATE `product` SET `status` = 0 WHERE `id` = 12;
-- 仅配送（不支持自提）
UPDATE `product` SET `support_pickup` = 0 WHERE `id` = 13;
-- 仅自提（不支持配送）
UPDATE `product` SET `support_delivery` = 0 WHERE `id` = 14;

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
('车厘子', 1), ('葡萄', 2), ('草莓', 3), ('芒果', 4), ('西瓜', 5),
('蓝莓', 6), ('礼盒', 7), ('坚果', 8), ('果汁', 9), ('鸡蛋', 10);

-- Banner（覆盖不同 link_type）
INSERT INTO `banner` (`title`, `image`, `link_type`, `link_value`, `sort`, `status`) VALUES
('夏日西瓜节',       '/static/seed/banner/banner1.jpg', 3, 'group-buy',  1, 1),
('新会员立减 10 元', '/static/seed/banner/banner2.jpg', 0, NULL,         2, 1),
('精品水果礼盒',     '/static/seed/banner/banner3.jpg', 1, '7',          3, 1),
('阳光玫瑰特惠',     '/static/seed/banner/banner4.jpg', 1, '1',          4, 1),
('限时拼团',         '/static/seed/banner/banner5.jpg', 3, 'group-buy',  5, 1),
('已过期活动',       '/static/seed/banner/banner6.jpg', 0, NULL,          6, 0);

-- 测试用户
INSERT INTO `user` (`id`, `phone`, `nickname`, `avatar`, `tag`) VALUES
(1, '13900000001', '小张', '/static/seed/avatar/user1.jpg', 'new'),
(2, '13900000002', '老李', '/static/seed/avatar/user2.jpg', 'regular');

-- 测试地址
INSERT INTO `user_address` (`user_id`, `consignee`, `phone`, `province`, `city`, `district`, `detail`, `tag`, `is_default`) VALUES
(1, '小张', '13900000001', '浙江省', '杭州市', '西湖区', '文三路 200 号 1 幢 502', '家',   1),
(1, '小张', '13900000001', '浙江省', '杭州市', '西湖区', '文一路阿里巴巴 1 号楼',   '公司', 0);

-- 用户已领取的优惠券（覆盖：未使用/已使用/已过期）
INSERT INTO `user_coupon` (`user_id`, `coupon_id`, `status`, `order_id`, `received_at`, `used_at`, `expire_at`) VALUES
(1, 1, 0, NULL, '2026-05-01 10:00:00', NULL, '2026-12-31 23:59:59'),
(1, 2, 0, NULL, '2026-05-01 10:00:00', NULL, '2026-12-31 23:59:59'),
(1, 4, 0, NULL, '2026-05-02 14:00:00', NULL, '2026-06-30 23:59:59'),
(1, 5, 0, NULL, '2026-05-03 09:00:00', NULL, '2026-12-31 23:59:59'),
(1, 6, 0, NULL, '2026-05-04 08:00:00', NULL, '2026-05-11 23:59:59'),
(1, 7, 2, NULL, '2025-06-01 10:00:00', NULL, '2025-12-31 23:59:59'),
(2, 1, 1, 10,  '2026-04-01 10:00:00', '2026-04-25 10:00:00', '2026-12-31 23:59:59'),
(2, 2, 0, NULL, '2026-04-05 10:00:00', NULL, '2026-12-31 23:59:59');

-- =====================================================================
-- 购物车数据（覆盖：选中/未选中/不同商品）
-- =====================================================================
INSERT INTO `cart_item` (`user_id`, `product_id`, `sku_id`, `quantity`, `selected`) VALUES
-- 用户1：选中商品
(1, 1,  1, 2, 1),   -- 阳光玫瑰 500g
(1, 3,  6, 1, 1),   -- 丹东草莓 300g
(1, 5, 10, 1, 1),   -- 赣南脐橙 5斤
-- 用户1：未选中商品
(1, 4,  8, 1, 0),   -- 海南芒果 1kg
(1, 20, 35, 1, 0);  -- 每日坚果 30包箱

-- =====================================================================
-- 订单数据（覆盖全部 9 种状态 0-8）
-- =====================================================================
INSERT INTO `order` (`id`, `order_no`, `user_id`, `status`, `pay_status`, `delivery_type`, `delivery_time`, `address_id`, `consignee`, `consignee_phone`, `consignee_address`, `pickup_point_id`, `pickup_code`, `goods_amount`, `coupon_amount`, `discount_amount`, `delivery_fee`, `pay_amount`, `user_coupon_id`, `pay_method`, `pay_time`, `pay_trade_no`, `user_remark`, `cancel_reason`, `group_buy_instance_id`, `courier_name`, `courier_phone`, `delivered_at`, `finished_at`, `created_at`) VALUES
-- 0: 待付款（日期设为未来，避免被定时任务自动取消）
(1, 'XG20260601001', 1, 0, 0, 1, '今日 18:00-20:00', 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 115.80, 0, 5.00, 0.00, 110.80, NULL, NULL, NULL, NULL, '请尽快配送', NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-01 10:00:00'),
-- 1: 待接单
(2, 'XG20260501002', 1, 1, 1, 1, '今日 18:00-20:00', 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 168.00, 10.00, 0, 5.00, 163.00, 2, 'wechat', '2026-05-01 10:05:00', 'WX202605010002', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-01 10:00:00'),
-- 2: 备货中
(3, 'XG20260501003', 1, 2, 1, 1, '明日上午', 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 56.00, 0, 0, 5.00, 61.00, NULL, 'wechat', '2026-05-01 09:00:00', 'WX202605010003', '多放几个', NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-01 08:50:00'),
-- 3: 配送中
(4, 'XG20260430004', 1, 3, 1, 1, '今日 14:00-18:00', 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 89.00, 0, 5.00, 0.00, 84.00, NULL, 'wechat', '2026-04-30 13:00:00', 'WX202604300004', NULL, NULL, NULL, '小王', '13800000001', NULL, NULL, '2026-04-30 12:00:00'),
-- 4: 待自提
(5, 'XG20260430005', 1, 4, 1, 2, NULL, NULL, NULL, NULL, NULL, 1, 'A3F2', 38.00, 0, 0, 0.00, 38.00, NULL, 'wechat', '2026-04-30 11:00:00', 'WX202604300005', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-30 10:30:00'),
-- 5: 已完成
(6, 'XG20260429006', 1, 5, 1, 1, '昨日 18:00-20:00', 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 39.90, 5.00, 0, 0.00, 34.90, 1, 'wechat', '2026-04-29 17:00:00', 'WX202604290006', NULL, NULL, NULL, '小王', '13800000001', '2026-04-29 19:30:00', '2026-04-29 19:35:00', '2026-04-29 17:00:00'),
-- 6: 已取消
(7, 'XG20260428007', 1, 6, 0, 1, NULL, 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 288.00, 0, 0, 5.00, 0.00, NULL, NULL, NULL, NULL, NULL, '不想买了', NULL, NULL, NULL, NULL, NULL, '2026-04-28 15:00:00'),
-- 7: 退款中
(8, 'XG20260427008', 1, 7, 2, 1, NULL, 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 59.00, 0, 0, 5.00, 64.00, NULL, 'wechat', '2026-04-27 10:00:00', 'WX202604270008', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-27 10:00:00'),
-- 8: 已退款
(9, 'XG20260426009', 1, 8, 2, 1, NULL, 1, '小张', '13900000001', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 29.90, 0, 0, 5.00, 34.90, NULL, 'wechat', '2026-04-26 09:00:00', 'WX202604260009', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-26 09:00:00'),
-- 用户2：已完成订单（拼团）
(10, 'XG20260425010', 2, 5, 1, 1, NULL, 1, '老李', '13900000002', '浙江省杭州市西湖区文三路 200 号 1 幢 502', NULL, NULL, 29.90, 0, 0, 5.00, 34.90, NULL, 'wechat', '2026-04-25 10:00:00', 'WX202604250010', NULL, NULL, 1, NULL, NULL, '2026-04-25 15:00:00', '2026-04-25 15:05:00', '2026-04-25 10:00:00');

-- =====================================================================
-- 订单商品明细
-- =====================================================================
INSERT INTO `order_item` (`order_id`, `product_id`, `sku_id`, `product_name`, `spec_name`, `image`, `price`, `original_price`, `quantity`, `subtotal`, `is_reviewed`) VALUES
-- 订单1（待付款）
(1, 1, 1, '阳光玫瑰葡萄', '500g装', '/static/seed/product/grape.jpg', 29.90, 35.80, 2, 59.80, 0),
(1, 3, 6, '丹东99草莓',   '300g装', '/static/seed/product/strawberry.jpg', 39.90, 49.00, 1, 39.90, 0),
-- 订单2（待接单）
(2, 2, 4, '智利进口车厘子 JJJ级', '1kg礼盒', '/static/seed/product/cherry.jpg', 168.00, 198.00, 1, 168.00, 0),
-- 订单3（备货中）
(3, 1, 2, '阳光玫瑰葡萄', '1kg装', '/static/seed/product/grape.jpg', 56.00, 65.00, 1, 56.00, 0),
-- 订单4（配送中）
(4, 1, 3, '阳光玫瑰葡萄', '2kg礼盒装', '/static/seed/product/grape.jpg', 89.00, 99.00, 1, 89.00, 0),
-- 订单5（待自提）
(5, 14, 28, '芒果班戟', '4个装', '/static/seed/product/crepe.jpg', 38.00, 45.00, 1, 38.00, 0),
-- 订单6（已完成）
(6, 3, 6, '丹东99草莓', '300g装', '/static/seed/product/strawberry.jpg', 39.90, 49.00, 1, 39.90, 1),
-- 订单7（已取消）
(7, 2, 5, '智利进口车厘子 JJJ级', '2.5kg礼盒', '/static/seed/product/cherry.jpg', 288.00, 328.00, 1, 288.00, 0),
-- 订单8（退款中）
(8, 3, 7, '丹东99草莓', '500g装', '/static/seed/product/strawberry.jpg', 59.00, 79.00, 1, 59.00, 0),
-- 订单9（已退款）
(9, 1, 1, '阳光玫瑰葡萄', '500g装', '/static/seed/product/grape.jpg', 29.90, 35.80, 1, 29.90, 0),
-- 订单10（拼团已完成）
(10, 3, 6, '丹东99草莓', '300g装', '/static/seed/product/strawberry.jpg', 29.90, 49.00, 1, 29.90, 1);

-- =====================================================================
-- 订单状态流转日志
-- =====================================================================
INSERT INTO `order_status_log` (`order_id`, `from_status`, `to_status`, `operator_type`, `operator_id`, `remark`, `created_at`) VALUES
-- 订单1
(1, NULL, 0, 1, 1, '用户下单', '2026-06-01 10:00:00'),
-- 订单2
(2, NULL, 0, 1, 1, '用户下单', '2026-05-01 10:00:00'),
(2, 0, 1, 1, 1, '用户支付', '2026-05-01 10:05:00'),
-- 订单3
(3, NULL, 0, 1, 1, '用户下单', '2026-05-01 08:50:00'),
(3, 0, 1, 1, 1, '用户支付', '2026-05-01 09:00:00'),
(3, 1, 2, 2, 1, '商家接单，开始备货', '2026-05-01 09:10:00'),
-- 订单4
(4, NULL, 0, 1, 1, '用户下单', '2026-04-30 12:00:00'),
(4, 0, 1, 1, 1, '用户支付', '2026-04-30 13:00:00'),
(4, 1, 2, 2, 1, '商家接单', '2026-04-30 13:30:00'),
(4, 2, 3, 2, 2, '配送员取货出发', '2026-04-30 14:00:00'),
-- 订单5
(5, NULL, 0, 1, 1, '用户下单（自提）', '2026-04-30 10:30:00'),
(5, 0, 1, 1, 1, '用户支付', '2026-04-30 11:00:00'),
(5, 1, 4, 2, 1, '备货完成，等待自提', '2026-04-30 11:30:00'),
-- 订单6
(6, NULL, 0, 1, 1, '用户下单', '2026-04-29 17:00:00'),
(6, 0, 1, 1, 1, '用户支付', '2026-04-29 17:00:00'),
(6, 1, 2, 2, 1, '商家接单', '2026-04-29 17:10:00'),
(6, 2, 3, 2, 2, '配送出发', '2026-04-29 18:30:00'),
(6, 3, 5, 1, 1, '用户确认收货', '2026-04-29 19:35:00'),
-- 订单7
(7, NULL, 0, 1, 1, '用户下单', '2026-04-28 15:00:00'),
(7, 0, 6, 1, 1, '用户取消：不想买了', '2026-04-28 15:10:00'),
-- 订单8
(8, NULL, 0, 1, 1, '用户下单', '2026-04-27 10:00:00'),
(8, 0, 1, 1, 1, '用户支付', '2026-04-27 10:00:00'),
(8, 1, 2, 2, 1, '商家接单', '2026-04-27 10:10:00'),
(8, 2, 5, 2, 2, '配送完成', '2026-04-27 14:00:00'),
(8, 5, 7, 1, 1, '用户申请退款：水果有损坏', '2026-04-27 16:00:00'),
-- 订单9
(9, NULL, 0, 1, 1, '用户下单', '2026-04-26 09:00:00'),
(9, 0, 1, 1, 1, '用户支付', '2026-04-26 09:00:00'),
(9, 1, 5, 2, 1, '商家接单并完成', '2026-04-26 12:00:00'),
(9, 5, 7, 1, 1, '用户申请退款', '2026-04-26 14:00:00'),
(9, 7, 8, 2, 1, '商家同意退款', '2026-04-26 16:00:00'),
-- 订单10
(10, NULL, 0, 1, 2, '用户下单（拼团）', '2026-04-25 10:00:00'),
(10, 0, 1, 1, 2, '用户支付', '2026-04-25 10:00:00'),
(10, 1, 2, 2, 1, '商家接单', '2026-04-25 10:30:00'),
(10, 2, 3, 2, 2, '配送出发', '2026-04-25 14:00:00'),
(10, 3, 5, 1, 2, '用户确认收货', '2026-04-25 15:05:00');

-- =====================================================================
-- 退款 / 售后
-- =====================================================================
INSERT INTO `refund` (`id`, `refund_no`, `order_id`, `user_id`, `type`, `amount`, `reason`, `images`, `status`, `reject_reason`, `handled_by`, `handled_at`, `created_at`) VALUES
(1, 'RF20260427001', 8, 1, 1, 64.00, '水果有损坏，不新鲜', '["/static/seed/refund/damage1.jpg","/static/seed/refund/damage2.jpg"]', 0, NULL, NULL, NULL, '2026-04-27 16:00:00'),
(2, 'RF20260426002', 9, 1, 1, 34.90, '商品与描述不符', '[]', 3, NULL, 1, '2026-04-26 16:00:00', '2026-04-26 14:00:00');

-- =====================================================================
-- 拼团实例 + 参与者
-- =====================================================================
INSERT INTO `group_buy_instance` (`id`, `activity_id`, `leader_id`, `current_size`, `target_size`, `status`, `expire_at`, `success_at`, `created_at`) VALUES
(1, 1, 2, 3, 3, 2, '2026-04-25 10:00:00', '2026-04-25 10:30:00', '2026-04-25 08:00:00'),
(2, 2, 1, 1, 2, 1, '2026-05-02 10:00:00', NULL, '2026-05-01 10:00:00'),
(3, 3, 2, 2, 3, 1, '2026-05-02 14:00:00', NULL, '2026-05-01 14:00:00');

INSERT INTO `group_buy_participant` (`instance_id`, `user_id`, `order_id`, `is_leader`, `joined_at`) VALUES
(1, 2, 10, 1, '2026-04-25 08:00:00'),
(1, 1, NULL, 0, '2026-04-25 09:00:00'),
(2, 1, NULL, 1, '2026-05-01 10:00:00'),
(3, 2, NULL, 1, '2026-05-01 14:00:00'),
(3, 1, NULL, 0, '2026-05-01 15:00:00');

-- =====================================================================
-- 评价（覆盖：好评/差评/匿名/有图/商家回复）
-- =====================================================================
INSERT INTO `review` (`order_id`, `order_item_id`, `product_id`, `user_id`, `rating`, `freshness_rating`, `value_rating`, `package_rating`, `content`, `images`, `is_anonymous`, `is_hidden`, `merchant_reply`, `replied_at`, `created_at`) VALUES
-- 好评 + 有图
(6, 6, 3, 1, 5, 5, 4, 5, '草莓非常新鲜，甜度也够，下次还会回购！', '["/static/seed/review/strawberry-r1.jpg","/static/seed/review/strawberry-r2.jpg"]', 0, 0, '感谢您的好评，期待再次光临！', '2026-04-30 10:00:00', '2026-04-29 20:00:00'),
-- 中评
(10, 11, 3, 2, 3, 3, 3, 4, '一般般，有几颗不太甜', '[]', 0, 0, NULL, NULL, '2026-04-25 20:00:00'),
-- 匿名好评
(6, 6, 3, 1, 4, 4, 5, 4, '包装不错，配送也快', '[]', 1, 0, NULL, NULL, '2026-04-30 08:00:00'),
-- 差评
(6, 6, 3, 1, 2, 2, 2, 3, '这次收到的有点磕碰，不太满意', '["/static/seed/review/damage.jpg"]', 0, 0, '抱歉给您带来不好的体验，我们会改进包装', '2026-05-01 09:00:00', '2026-04-30 22:00:00');

-- =====================================================================
-- 收藏 + 浏览足迹
-- =====================================================================
INSERT INTO `favorite` (`user_id`, `product_id`, `created_at`) VALUES
(1, 1, '2026-04-28 10:00:00'),
(1, 2, '2026-04-29 11:00:00'),
(1, 3, '2026-04-30 09:00:00'),
(1, 7, '2026-05-01 08:00:00'),
(2, 1, '2026-04-25 10:00:00'),
(2, 4, '2026-04-26 14:00:00');

INSERT INTO `footprint` (`user_id`, `product_id`, `viewed_at`) VALUES
(1, 1, '2026-05-01 10:00:00'),
(1, 2, '2026-05-01 09:30:00'),
(1, 3, '2026-05-01 09:00:00'),
(1, 4, '2026-04-30 20:00:00'),
(1, 5, '2026-04-30 19:00:00'),
(1, 7, '2026-04-30 18:00:00'),
(1, 20, '2026-04-30 17:00:00'),
(2, 1, '2026-04-25 10:00:00'),
(2, 3, '2026-04-25 09:00:00'),
(2, 4, '2026-04-26 14:00:00');

-- =====================================================================
-- 消息（覆盖：系统通知/订单消息/优惠活动/拼团动态 + 已读/未读）
-- =====================================================================
INSERT INTO `message` (`user_id`, `type`, `title`, `content`, `link_url`, `is_read`, `created_at`) VALUES
-- 系统通知
(0, 1, '系统维护通知', '5月5日凌晨2:00-4:00进行系统维护，届时无法下单', NULL, 0, '2026-05-03 18:00:00'),
(1, 1, '欢迎注册鲜果记', '新用户首单立减10元，快去选购吧！', '/pages/index/index', 1, '2026-04-28 10:00:00'),
-- 订单消息
(1, 2, '订单已接单', '您的订单 XG20260501002 商家已接单，正在备货中', '/pagesB/order-detail/index?orderNo=XG20260501002', 0, '2026-05-01 10:10:00'),
(1, 2, '配送中', '您的订单 XG20260430004 已由配送员小王取货出发', '/pagesB/order-detail/index?orderNo=XG20260430004', 1, '2026-04-30 14:00:00'),
(1, 2, '自提码', '您的自提订单 XG20260430005 自提码为 A3F2', '/pagesB/order-detail/index?orderNo=XG20260430005', 1, '2026-04-30 11:30:00'),
-- 优惠活动
(0, 3, '限时拼团开启', '阳光玫瑰葡萄2人团低至25.9元，快来参团！', '/pagesC/group-buy/index', 0, '2026-05-01 08:00:00'),
(1, 3, '优惠券到账', '您有一张9折券即将到期，快去使用吧', '/pages/index/index', 0, '2026-06-20 10:00:00'),
-- 拼团动态
(1, 4, '拼团成功', '您参与的草莓3人团已成功，即将发货', '/pagesB/order-detail/index?orderNo=XG20260425010', 1, '2026-04-25 10:30:00'),
(1, 4, '还差1人成团', '您发起的葡萄2人团还差1人，快邀请好友吧', '/pagesC/group-buy/index', 0, '2026-05-01 10:05:00');

-- 同步商品冗余字段
UPDATE `product` p
SET `min_price` = (SELECT MIN(price) FROM `product_sku` WHERE product_id = p.id),
    `max_price` = (SELECT MAX(price) FROM `product_sku` WHERE product_id = p.id),
    `total_stock` = (SELECT COALESCE(SUM(stock),0) FROM `product_sku` WHERE product_id = p.id);

-- =====================================================================
-- 商家端通知种子数据
-- =====================================================================
INSERT INTO `admin_notification` (`type`, `title`, `content`, `link_url`, `is_read`, `created_at`) VALUES
(1, '新订单待处理', '客户下单了精品富士苹果和泰国榴莲，订单金额 ¥458.00，请在2小时内确认接单。', '/orders', 0, '2026-05-04 17:00:00'),
(1, '订单超时提醒', '订单 XG20260501002 已超过备货时限（30分钟），请尽快处理以避免客户投诉。', '/orders', 0, '2026-05-04 16:45:00'),
(2, '库存预警', '榴莲千层蛋糕当前库存仅剩 3 件，低于安全库存阈值，建议尽快补货。', '/goods', 0, '2026-05-04 16:00:00'),
(3, '收到差评待回复', '客户对有机蓝莓给出了2星评价，建议尽快回复以维护店铺口碑。', '/reviews', 0, '2026-05-04 15:00:00'),
(5, '系统维护通知', '系统将于今晚 02:00-04:00 进行例行维护升级，届时部分功能可能暂时不可用。', NULL, 1, '2026-05-04 14:00:00'),
(4, '优惠券即将到期', '「已过期满减」优惠券已过期，当前核销率仅 64.8%，建议推送提醒。', '/campaign', 1, '2026-05-04 12:00:00'),
(1, '退款申请待处理', '客户申请退款 ¥89.90，原因：商品与描述不符。请在48小时内处理。', '/orders', 0, '2026-05-04 11:00:00'),
(3, '新好评提醒', '客户对精品富士苹果给出了5星好评："苹果非常新鲜，包装精美！"', '/reviews', 1, '2026-05-04 09:00:00');

-- =====================================================================
-- 31. 帮助中心：FAQ / 操作指南 / 用户反馈 / 报表导出记录
-- =====================================================================
CREATE TABLE `help_faq` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `section`    VARCHAR(32) NOT NULL                                  COMMENT '所属板块 orders/goods/shipping/...',
  `question`   VARCHAR(255) NOT NULL,
  `answer`     TEXT NOT NULL,
  `sort`       INT NOT NULL DEFAULT 0,
  `status`     TINYINT NOT NULL DEFAULT 1                            COMMENT '0隐藏 1启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_section` (`section`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB COMMENT='帮助中心常见问题';

CREATE TABLE `help_guide` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `title`      VARCHAR(128) NOT NULL,
  `icon`       VARCHAR(64),
  `duration`   VARCHAR(32)                                            COMMENT '约5分钟',
  `url`        VARCHAR(255)                                           COMMENT '指南详情链接',
  `sort`       INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB COMMENT='操作指南';

CREATE TABLE `help_feedback` (
  `id`         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `staff_id`   BIGINT UNSIGNED                                        COMMENT '提交者，可空（未登录）',
  `content`    VARCHAR(1000) NOT NULL,
  `contact`    VARCHAR(64)                                            COMMENT '联系方式',
  `status`     TINYINT NOT NULL DEFAULT 0                             COMMENT '0待处理 1已读 2已回复',
  `reply`      VARCHAR(1000),
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='用户反馈';

CREATE TABLE `report_export` (
  `id`           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `staff_id`     BIGINT UNSIGNED NOT NULL,
  `report_type`  VARCHAR(32) NOT NULL                                 COMMENT 'sales/inventory/customer/delivery/finance',
  `start_date`   DATE NOT NULL,
  `end_date`     DATE NOT NULL,
  `format`       VARCHAR(8) NOT NULL                                  COMMENT 'xlsx/csv/pdf',
  `file_name`    VARCHAR(128) NOT NULL,
  `file_size`    BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `status`       TINYINT NOT NULL DEFAULT 1                           COMMENT '0生成中 1已完成 2失败',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_staff_created` (`staff_id`, `created_at`)
) ENGINE=InnoDB COMMENT='报表导出记录';

-- 帮助中心种子数据
INSERT INTO `help_faq` (`section`, `question`, `answer`, `sort`) VALUES
('orders', '如何处理客户退款申请？', '进入订单管理页面，筛选"退款/售后"标签页，点击对应订单的"处理退款"按钮。您可以选择同意退款、部分退款或拒绝退款。处理时限为48小时，超时系统将自动同意退款。', 1),
('goods', '商品库存预警阈值如何设置？', '进入商品管理页面，点击商品编辑，在"库存与价格"模块中可以设置每个SKU的库存预警阈值。当库存低于该值时，系统会自动推送通知到消息中心。', 2),
('campaign', '如何创建拼团活动？', '进入营销中心，点击"创建优惠券"，选择"拼团活动"类型。设置成团人数、拼团折扣和活动时间即可。拼团商品会在用户端展示拼团标签。', 3),
('shipping', '配送范围如何调整？', '进入配送设置页面，在"配送服务范围"模块中，使用地图绘制工具调整配送区域。您可以设置标准配送范围和扩展配送范围，不同范围可配置不同运费。', 4),
('report', '如何导出经营数据？', '进入报表导出页面，选择报表类型、时间范围和导出格式，点击"生成并下载"即可。您也可以使用快捷模板快速生成常用报表，或设置定时自动生成。', 5),
('security', '忘记密码怎么办？', '在登录页面点击"忘记密码"，输入注册手机号获取验证码，验证后即可重置密码。如果手机号已变更，请联系客服400-888-9999进行人工验证。', 6),
('reviews', '如何查看客户评价并回复？', '进入评价管理页面，可以按"待回复"、"已回复"、"差评"筛选。点击评价卡片上的"回复"按钮即可撰写回复。对于差评，系统会提供AI建议回复供参考。', 7),
('settings', '店铺状态如何切换？', '进入系统设置页面，在"店铺状态"模块中切换营业/休息状态。休息状态下用户端将显示"店铺休息中"，无法下单。您也可以设置定时营业时间自动切换。', 8);

INSERT INTO `help_guide` (`title`, `icon`, `duration`, `url`, `sort`) VALUES
('新手入驻指南',  'rocket_launch', '约10分钟', '/help/onboarding', 1),
('商品发布教程',  'inventory_2',   '约8分钟',  '/help/goods',      2),
('订单处理流程',  'receipt_long',  '约5分钟',  '/help/orders',     3),
('营销活动设置',  'campaign',      '约6分钟',  '/help/campaign',   4),
('数据报表解读',  'analytics',     '约7分钟',  '/help/report',     5);
