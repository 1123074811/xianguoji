-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `sender_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0=用户发送 1=商家回复',
  `msg_type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT 'text/product/image',
  `content` VARCHAR(2000) DEFAULT NULL COMMENT '文字内容',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品卡片关联商品ID',
  `images` JSON DEFAULT NULL COMMENT '图片URL列表',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '0=未读 1=已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_sender_read` (`user_id`, `sender_type`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息';
