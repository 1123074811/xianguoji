-- P2-1: 为业务表添加 create_by / update_by 字段，配合 AutoFill 自动填充操作人

ALTER TABLE `order` ADD COLUMN `create_by` BIGINT UNSIGNED COMMENT '创建人ID' AFTER `updated_at`,
                   ADD COLUMN `update_by` BIGINT UNSIGNED COMMENT '更新人ID' AFTER `create_by`;

ALTER TABLE `refund` ADD COLUMN `create_by` BIGINT UNSIGNED COMMENT '创建人ID' AFTER `updated_at`,
                     ADD COLUMN `update_by` BIGINT UNSIGNED COMMENT '更新人ID' AFTER `create_by`;

ALTER TABLE `user_coupon` ADD COLUMN `create_by` BIGINT UNSIGNED COMMENT '创建人ID' AFTER `expire_at`,
                         ADD COLUMN `update_by` BIGINT UNSIGNED COMMENT '更新人ID' AFTER `create_by`;

ALTER TABLE `group_buy_instance` ADD COLUMN `create_by` BIGINT UNSIGNED COMMENT '创建人ID' AFTER `updated_at`,
                                ADD COLUMN `update_by` BIGINT UNSIGNED COMMENT '更新人ID' AFTER `create_by`;

ALTER TABLE `review` ADD COLUMN `create_by` BIGINT UNSIGNED COMMENT '创建人ID' AFTER `updated_at`,
                    ADD COLUMN `update_by` BIGINT UNSIGNED COMMENT '更新人ID' AFTER `create_by`;

ALTER TABLE `feedback` ADD COLUMN `create_by` BIGINT UNSIGNED COMMENT '创建人ID' AFTER `created_at`,
                      ADD COLUMN `update_by` BIGINT UNSIGNED COMMENT '更新人ID' AFTER `create_by`;
