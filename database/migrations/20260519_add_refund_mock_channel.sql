SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'refund'
    AND COLUMN_NAME = 'refund_channel'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `refund` ADD COLUMN `refund_channel` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''退款通道 MOCK/wechat/alipay'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'refund'
    AND COLUMN_NAME = 'refund_transaction_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `refund` ADD COLUMN `refund_transaction_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''退款流水号'' AFTER `refund_channel`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `refund`
SET `refund_channel` = 'MOCK',
    `refund_transaction_id` = CONCAT('MOCK_REFUND_', `refund_no`)
WHERE `status` = 3
  AND `refund_channel` IS NULL;
