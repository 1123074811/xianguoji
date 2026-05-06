-- 拼团分享/邀请：为 group_buy_instance 增加 share_code 字段
ALTER TABLE `group_buy_instance`
  ADD COLUMN `share_code` VARCHAR(16) NULL COMMENT '分享/邀请码' AFTER `success_at`,
  ADD UNIQUE KEY `uk_share_code` (`share_code`);
