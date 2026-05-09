-- 商品状态新增 3=草稿
-- 原: 0已下架 1在售 2回收站
-- 新: 0已下架 1在售 2回收站 3草稿
ALTER TABLE `product` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '1' COMMENT '0已下架 1在售 2回收站 3草稿';
