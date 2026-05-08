-- MySQL dump 10.13  Distrib 9.1.0, for Win64 (x86_64)
--
-- Host: localhost    Database: xianguoji
-- ------------------------------------------------------
-- Server version	9.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_notification`
--

DROP TABLE IF EXISTS `admin_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_notification` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL COMMENT '1订单 2库存 3评价 4营销 5系统',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转地址',
  `is_read` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家端通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_notification`
--

LOCK TABLES `admin_notification` WRITE;
/*!40000 ALTER TABLE `admin_notification` DISABLE KEYS */;
INSERT INTO `admin_notification` VALUES (1,1,'催单提醒','用户催促发货，订单 260506160721755369','/orders',1,'2026-05-06 18:42:37'),(2,1,'新订单待处理','您有新的订单 260506184333196974，金额 ¥54.90，配送方式：配送','/orders',1,'2026-05-06 18:43:34'),(3,4,'新拼团开团','「丹东99草莓」拼团已开团（1/3）','/campaign',1,'2026-05-06 19:23:07'),(4,4,'拼团失败','「丹东99草莓」拼团失败，已自动取消订单','/campaign',1,'2026-05-07 19:25:00'),(5,4,'新拼团开团','「hhh」拼团已开团（1/3）','/campaign',0,'2026-05-07 23:08:47'),(6,4,'新拼团开团','「hhh」拼团已开团（1/3）','/campaign',1,'2026-05-07 23:22:22');
/*!40000 ALTER TABLE `admin_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `link_type` tinyint NOT NULL DEFAULT '0' COMMENT '0无 1商品 2分类 3活动 4外链',
  `link_value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页轮播';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` VALUES (1,'夏日西瓜节','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/banner/banner1.jpg',3,'group-buy',1,1,NULL,NULL,'2026-05-04 16:03:17'),(2,'新会员立减 10 元','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/banner/banner2.jpg',0,NULL,2,1,NULL,NULL,'2026-05-04 16:03:17'),(3,'精品水果礼盒','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/banner/banner3.jpg',1,'7',3,1,NULL,NULL,'2026-05-04 16:03:17'),(4,'阳光玫瑰特惠','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/banner/banner4.jpg',1,'1',4,1,NULL,NULL,'2026-05-04 16:03:17'),(5,'限时拼团','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/banner/banner5.jpg',3,'group-buy',5,1,NULL,NULL,'2026-05-04 16:03:17'),(6,'已过期活动','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/banner/banner6.jpg',0,NULL,6,0,NULL,NULL,'2026-05-04 16:03:17');
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `product_id` bigint unsigned NOT NULL,
  `sku_id` bigint unsigned NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否勾选',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`,`sku_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
INSERT INTO `cart_item` VALUES (1,1,1,1,2,1,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(2,1,3,6,1,1,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(3,1,5,10,1,1,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(4,1,4,8,1,0,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(5,1,20,35,1,0,'2026-05-04 16:03:17','2026-05-04 16:03:17');
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '0为一级分类',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '一级分类图标',
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0隐藏 1显示',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=803 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,0,'时令热销','fruit_cherries',1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(2,0,'进口鲜果','leafy_greens',2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(3,0,'精品礼盒','gift',3,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(4,0,'国产优选','eco',4,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(5,0,'果干果仁','bakery_dining',5,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(6,0,'果汁甜点','liquor',6,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(7,0,'有机蔬菜','potted_plant',7,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(8,0,'肉蛋水产','meat_egg',8,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(101,1,'当季鲜果',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(102,1,'热带水果',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(201,2,'进口浆果',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(202,2,'进口核果',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(301,3,'鲜果礼盒',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(302,3,'混合礼盒',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(401,4,'柑橘类',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(402,4,'瓜果类',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(501,5,'果干',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(502,5,'坚果',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(601,6,'鲜榨果汁',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(602,6,'果泥甜点',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(701,7,'叶菜类',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(702,7,'根茎类',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(801,8,'鲜鸡蛋',NULL,1,1,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(802,8,'海鲜水产',NULL,2,1,'2026-05-04 16:03:16','2026-05-04 16:03:16');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `sender_type` tinyint NOT NULL DEFAULT '0' COMMENT '0=用户发送 1=商家回复',
  `msg_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT 'text/product/image',
  `content` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文字内容',
  `product_id` bigint DEFAULT NULL COMMENT '商品卡片关联商品ID',
  `images` json DEFAULT NULL COMMENT '图片URL列表',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '0=未读 1=已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_sender_read` (`user_id`,`sender_type`,`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
INSERT INTO `chat_message` VALUES (1,3,0,'product','商品咨询',3,NULL,1,'2026-05-07 13:55:23',3,3),(2,3,0,'text','你好',NULL,NULL,1,'2026-05-07 13:55:38',3,3),(3,3,1,'text','你好怎么了？',NULL,NULL,1,'2026-05-07 14:01:47',1,1),(4,3,0,'product','商品咨询',15,NULL,1,'2026-05-07 14:28:59',3,3),(5,3,0,'product','商品咨询',24,NULL,1,'2026-05-07 14:36:28',3,3),(6,3,0,'product','商品咨询',24,NULL,1,'2026-05-07 14:37:39',3,3),(7,3,0,'text','你好',NULL,NULL,1,'2026-05-07 14:37:39',3,3),(8,3,0,'image','[图片]',NULL,'[\"https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/user/2026/05/07/0420793e97314915ac0c44e160693a18.jpg\"]',1,'2026-05-07 14:37:52',3,3),(9,3,0,'text','这个怎么和商品描述不一样',NULL,NULL,1,'2026-05-07 14:38:04',3,3),(10,3,1,'text','您稍等',NULL,NULL,1,'2026-05-07 14:44:57',1,1);
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1满减 2折扣',
  `amount` decimal(10,2) NOT NULL COMMENT '面额（折扣类型为折扣率0-1）',
  `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '使用门槛',
  `total` int NOT NULL DEFAULT '0' COMMENT '发放总量 0不限',
  `received_count` int NOT NULL DEFAULT '0' COMMENT '已领取',
  `used_count` int NOT NULL DEFAULT '0' COMMENT '已核销',
  `per_user_limit` int NOT NULL DEFAULT '1' COMMENT '每人限领',
  `valid_type` tinyint NOT NULL DEFAULT '1' COMMENT '1绝对日期 2领取后N天',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `valid_days` int DEFAULT NULL COMMENT '领取后有效天数',
  `scope` tinyint NOT NULL DEFAULT '1' COMMENT '1全部商品 2指定商品',
  `scope_product_ids` json DEFAULT NULL COMMENT '指定商品ID数组',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0未开始 1进行中 2已结束',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (1,'新人满 39 减 5',1,5.00,39.00,1000,2,0,1,1,'2026-01-01 00:00:00','2026-12-31 00:00:00',NULL,1,NULL,1,'2026-05-04 16:03:17','2026-05-06 15:08:52'),(2,'满 69 减 10',1,10.00,69.00,2000,6,0,5,1,'2026-01-01 00:00:00','2026-12-31 00:00:00',NULL,1,NULL,1,'2026-05-04 16:03:17','2026-05-06 15:24:01'),(3,'满 129 减 20',1,20.00,129.00,1000,2,0,5,1,'2026-01-01 00:00:00','2026-12-31 00:00:00',NULL,1,NULL,1,'2026-05-04 16:03:17','2026-05-07 22:39:36'),(4,'全场 9 折',2,0.90,0.00,500,3,0,3,1,'2026-05-01 00:00:00','2026-06-30 00:00:00',NULL,1,NULL,1,'2026-05-04 16:03:17','2026-05-06 15:14:06'),(5,'葡萄专属券 减3',1,3.00,0.00,200,2,0,2,1,'2026-01-01 00:00:00','2026-12-31 00:00:00',NULL,2,'[1]',1,'2026-05-04 16:03:17','2026-05-06 15:14:13'),(6,'领取后7天有效',1,8.00,49.00,300,1,0,2,2,NULL,NULL,7,1,NULL,1,'2026-05-04 16:03:17','2026-05-07 22:39:37'),(7,'已过期满减',1,15.00,99.00,100,0,0,1,1,'2025-01-01 00:00:00','2025-12-31 00:00:00',NULL,1,NULL,2,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(8,'新人优惠',1,10.00,20.00,100,1,0,1,1,'2026-05-04 17:00:00','2026-05-05 17:00:00',NULL,1,NULL,1,'2026-05-04 17:00:56','2026-05-07 22:39:38');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_setting`
--

DROP TABLE IF EXISTS `delivery_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_setting` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `min_order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '起送价',
  `base_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '基础配送费',
  `free_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '满X元免运费',
  `time_slots` json DEFAULT NULL COMMENT '可选时段 [{label,start,end}]',
  `service_area` json DEFAULT NULL COMMENT '配送范围 GeoJSON',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_setting`
--

LOCK TABLES `delivery_setting` WRITE;
/*!40000 ALTER TABLE `delivery_setting` DISABLE KEYS */;
INSERT INTO `delivery_setting` VALUES (1,20.00,5.00,40.00,'[{\"end\": \"20:00\", \"label\": \"今日 18:00-20:00\", \"start\": \"18:00\"}, {\"end\": \"12:00\", \"label\": \"明日上午\", \"start\": \"09:00\"}, {\"end\": \"18:00\", \"label\": \"明日下午\", \"start\": \"14:00\"}]','{\"center\": [120.130202, 30.281797], \"radius\": 15000}','2026-05-04 16:03:17');
/*!40000 ALTER TABLE `delivery_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `product_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
INSERT INTO `favorite` VALUES (1,1,1,'2026-04-28 10:00:00'),(2,1,2,'2026-04-29 11:00:00'),(3,1,3,'2026-04-30 09:00:00'),(4,1,7,'2026-05-01 08:00:00'),(5,2,1,'2026-04-25 10:00:00'),(6,2,4,'2026-04-26 14:00:00'),(7,3,24,'2026-05-06 15:15:54');
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '反馈类型',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `images` json DEFAULT NULL,
  `contact` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待处理 1已处理',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (1,1,'bug','测试反馈提交功能是否正常',NULL,NULL,0,'2026-05-06 19:27:17',1,1),(2,3,'bug','12312412412',NULL,NULL,0,'2026-05-06 19:44:18',3,3);
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `footprint`
--

DROP TABLE IF EXISTS `footprint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `footprint` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `product_id` bigint unsigned NOT NULL,
  `viewed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_viewed` (`user_id`,`viewed_at`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览足迹';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `footprint`
--

LOCK TABLES `footprint` WRITE;
/*!40000 ALTER TABLE `footprint` DISABLE KEYS */;
INSERT INTO `footprint` VALUES (1,1,1,'2026-05-01 10:00:00'),(2,1,2,'2026-05-01 09:30:00'),(3,1,3,'2026-05-01 09:00:00'),(4,1,4,'2026-04-30 20:00:00'),(5,1,5,'2026-04-30 19:00:00'),(6,1,7,'2026-04-30 18:00:00'),(7,1,20,'2026-04-30 17:00:00'),(8,2,1,'2026-04-25 10:00:00'),(9,2,3,'2026-04-25 09:00:00'),(10,2,4,'2026-04-26 14:00:00'),(11,3,24,'2026-05-08 12:23:09'),(12,3,7,'2026-05-06 15:09:19'),(13,3,20,'2026-05-06 18:43:25'),(14,3,3,'2026-05-07 22:44:06'),(15,3,15,'2026-05-07 14:28:30'),(16,3,17,'2026-05-07 22:58:44');
/*!40000 ALTER TABLE `footprint` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_activity`
--

DROP TABLE IF EXISTS `group_buy_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `product_id` bigint unsigned NOT NULL,
  `sku_id` bigint unsigned NOT NULL,
  `group_price` decimal(10,2) NOT NULL COMMENT '拼团价',
  `group_size` int NOT NULL DEFAULT '3' COMMENT '成团人数',
  `valid_hours` int NOT NULL DEFAULT '24' COMMENT '拼团时效（小时）',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `total_join_count` int NOT NULL DEFAULT '0' COMMENT '参团人次',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成团数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0已结束 1进行中',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团活动配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_activity`
--

LOCK TABLES `group_buy_activity` WRITE;
/*!40000 ALTER TABLE `group_buy_activity` DISABLE KEYS */;
INSERT INTO `group_buy_activity` VALUES (1,3,6,29.90,3,24,'2026-05-01 00:00:00','2026-06-30 23:59:59',46,12,1,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(2,1,1,25.90,2,48,'2026-05-01 00:00:00','2026-06-30 23:59:59',80,30,1,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(3,4,8,16.90,3,24,'2026-05-01 00:00:00','2026-06-30 23:59:59',36,10,1,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(4,20,35,49.90,5,24,'2026-04-01 00:00:00','2026-04-30 23:59:59',50,8,0,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(5,24,56,153.00,3,24,'2026-05-07 22:54:54','2026-05-08 22:54:00',2,0,1,'2026-05-07 22:54:54','2026-05-07 22:54:54');
/*!40000 ALTER TABLE `group_buy_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_instance`
--

DROP TABLE IF EXISTS `group_buy_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_instance` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` bigint unsigned NOT NULL,
  `leader_id` bigint unsigned NOT NULL COMMENT '团长用户ID',
  `current_size` int NOT NULL DEFAULT '1',
  `target_size` int NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1拼团中 2已成团 3已失败',
  `expire_at` datetime NOT NULL,
  `success_at` datetime DEFAULT NULL,
  `share_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分享/邀请码',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_code` (`share_code`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团实例';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_instance`
--

LOCK TABLES `group_buy_instance` WRITE;
/*!40000 ALTER TABLE `group_buy_instance` DISABLE KEYS */;
INSERT INTO `group_buy_instance` VALUES (1,1,2,3,3,2,'2026-04-25 10:00:00','2026-04-25 10:30:00',NULL,'2026-04-25 08:00:00','2026-05-04 16:03:17',NULL,NULL),(2,2,1,1,2,3,'2026-05-02 10:00:00',NULL,NULL,'2026-05-01 10:00:00','2026-05-04 16:03:17',NULL,NULL),(3,3,2,2,3,3,'2026-05-02 14:00:00',NULL,NULL,'2026-05-01 14:00:00','2026-05-04 16:03:17',NULL,NULL),(4,1,3,1,3,3,'2026-05-07 19:23:07',NULL,'YBVJU858','2026-05-06 19:23:07','2026-05-07 19:25:00',3,3),(5,5,3,1,3,1,'2026-05-08 23:08:46',NULL,'WHYBNH5L','2026-05-07 23:08:46','2026-05-07 23:08:46',3,3),(6,5,3,1,3,1,'2026-05-08 23:22:22',NULL,'SSXLGART','2026-05-07 23:22:22','2026-05-07 23:22:22',3,3);
/*!40000 ALTER TABLE `group_buy_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_participant`
--

DROP TABLE IF EXISTS `group_buy_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_participant` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `instance_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `order_id` bigint unsigned DEFAULT NULL COMMENT '关联订单ID，未下单时为NULL',
  `is_leader` tinyint NOT NULL DEFAULT '0',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_user` (`instance_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团参与者';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_participant`
--

LOCK TABLES `group_buy_participant` WRITE;
/*!40000 ALTER TABLE `group_buy_participant` DISABLE KEYS */;
INSERT INTO `group_buy_participant` VALUES (1,1,2,10,1,'2026-04-25 08:00:00'),(2,1,1,NULL,0,'2026-04-25 09:00:00'),(3,2,1,NULL,1,'2026-05-01 10:00:00'),(4,3,2,NULL,1,'2026-05-01 14:00:00'),(5,3,1,NULL,0,'2026-05-01 15:00:00'),(6,4,3,17,1,'2026-05-06 19:23:07'),(7,5,3,18,1,'2026-05-07 23:08:46'),(8,6,3,19,1,'2026-05-07 23:22:22');
/*!40000 ALTER TABLE `group_buy_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `help_faq`
--

DROP TABLE IF EXISTS `help_faq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `help_faq` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `section` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属板块 orders/goods/shipping/...',
  `question` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0隐藏 1启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_section` (`section`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助中心常见问题';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `help_faq`
--

LOCK TABLES `help_faq` WRITE;
/*!40000 ALTER TABLE `help_faq` DISABLE KEYS */;
INSERT INTO `help_faq` VALUES (1,'orders','如何处理客户退款申请？','进入订单管理页面，筛选\"退款/售后\"标签页，点击对应订单的\"处理退款\"按钮。您可以选择同意退款、部分退款或拒绝退款。处理时限为48小时，超时系统将自动同意退款。',1,1,'2026-05-04 23:09:35'),(2,'goods','商品库存预警阈值如何设置？','进入商品管理页面，点击商品编辑，在\"库存与价格\"模块中可以设置每个SKU的库存预警阈值。当库存低于该值时，系统会自动推送通知到消息中心。',2,1,'2026-05-04 23:09:35'),(3,'campaign','如何创建拼团活动？','进入营销中心，点击\"创建优惠券\"，选择\"拼团活动\"类型。设置成团人数、拼团折扣和活动时间即可。拼团商品会在用户端展示拼团标签。',3,1,'2026-05-04 23:09:35'),(4,'shipping','配送范围如何调整？','进入配送设置页面，在\"配送服务范围\"模块中，使用地图绘制工具调整配送区域。您可以设置标准配送范围和扩展配送范围，不同范围可配置不同运费。',4,1,'2026-05-04 23:09:35'),(5,'report','如何导出经营数据？','进入报表导出页面，选择报表类型、时间范围和导出格式，点击\"生成并下载\"即可。您也可以使用快捷模板快速生成常用报表，或设置定时自动生成。',5,1,'2026-05-04 23:09:35'),(6,'security','忘记密码怎么办？','在登录页面点击\"忘记密码\"，输入注册手机号获取验证码，验证后即可重置密码。如果手机号已变更，请联系客服400-888-9999进行人工验证。',6,1,'2026-05-04 23:09:35'),(7,'reviews','如何查看客户评价并回复？','进入评价管理页面，可以按\"待回复\"、\"已回复\"、\"差评\"筛选。点击评价卡片上的\"回复\"按钮即可撰写回复。对于差评，系统会提供AI建议回复供参考。',7,1,'2026-05-04 23:09:35'),(8,'settings','店铺状态如何切换？','进入系统设置页面，在\"店铺状态\"模块中切换营业/休息状态。休息状态下用户端将显示\"店铺休息中\"，无法下单。您也可以设置定时营业时间自动切换。',8,1,'2026-05-04 23:09:35');
/*!40000 ALTER TABLE `help_faq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `help_feedback`
--

DROP TABLE IF EXISTS `help_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `help_feedback` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `staff_id` bigint unsigned DEFAULT NULL COMMENT '提交者，可空（未登录）',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待处理 1已读 2已回复',
  `reply` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `help_feedback`
--

LOCK TABLES `help_feedback` WRITE;
/*!40000 ALTER TABLE `help_feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `help_feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `help_guide`
--

DROP TABLE IF EXISTS `help_guide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `help_guide` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `duration` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '约5分钟',
  `url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '指南详情链接',
  `sort` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作指南';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `help_guide`
--

LOCK TABLES `help_guide` WRITE;
/*!40000 ALTER TABLE `help_guide` DISABLE KEYS */;
INSERT INTO `help_guide` VALUES (1,'新手入驻指南','rocket_launch','约10分钟','/help/onboarding',1,'2026-05-04 23:09:35'),(2,'商品发布教程','inventory_2','约8分钟','/help/goods',2,'2026-05-04 23:09:35'),(3,'订单处理流程','receipt_long','约5分钟','/help/orders',3,'2026-05-04 23:09:35'),(4,'营销活动设置','campaign','约6分钟','/help/campaign',4,'2026-05-04 23:09:35'),(5,'数据报表解读','analytics','约7分钟','/help/report',5,'2026-05-04 23:09:35');
/*!40000 ALTER TABLE `help_guide` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hot_search`
--

DROP TABLE IF EXISTS `hot_search`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hot_search` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `keyword` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门搜索';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hot_search`
--

LOCK TABLES `hot_search` WRITE;
/*!40000 ALTER TABLE `hot_search` DISABLE KEYS */;
INSERT INTO `hot_search` VALUES (1,'车厘子',1,1),(2,'葡萄',2,1),(3,'草莓',3,1),(4,'芒果',4,1),(5,'西瓜',5,1),(6,'蓝莓',6,1),(7,'礼盒',7,1),(8,'坚果',8,1),(9,'果汁',9,1),(10,'鸡蛋',10,1);
/*!40000 ALTER TABLE `hot_search` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '0为全员广播',
  `type` tinyint NOT NULL COMMENT '1系统通知 2订单消息 3优惠活动 4拼团动态',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转地址',
  `is_read` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`,`type`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,0,1,'系统维护通知','5月5日凌晨2:00-4:00进行系统维护，届时无法下单',NULL,1,'2026-05-03 18:00:00'),(2,1,1,'欢迎注册鲜果记','新用户首单立减10元，快去选购吧！','/pages/index/index',1,'2026-04-28 10:00:00'),(3,1,2,'订单已接单','您的订单 XG20260501002 商家已接单，正在备货中','/pagesB/order-detail/index?orderNo=XG20260501002',0,'2026-05-01 10:10:00'),(4,1,2,'配送中','您的订单 XG20260430004 已由配送员小王取货出发','/pagesB/order-detail/index?orderNo=XG20260430004',1,'2026-04-30 14:00:00'),(5,1,2,'自提码','您的自提订单 XG20260430005 自提码为 A3F2','/pagesB/order-detail/index?orderNo=XG20260430005',1,'2026-04-30 11:30:00'),(6,0,3,'限时拼团开启','阳光玫瑰葡萄2人团低至25.9元，快来参团！','/pagesC/group-buy/index',1,'2026-05-01 08:00:00'),(7,1,3,'优惠券到账','您有一张9折券即将到期，快去使用吧','/pages/index/index',0,'2026-06-20 10:00:00'),(8,1,4,'拼团成功','您参与的草莓3人团已成功，即将发货','/pagesB/order-detail/index?orderNo=XG20260425010',1,'2026-04-25 10:30:00'),(9,1,4,'还差1人成团','您发起的葡萄2人团还差1人，快邀请好友吧','/pagesC/group-buy/index',0,'2026-05-01 10:05:00'),(10,3,6,'客服回复','您稍等','/pagesC/chat/index',1,'2026-05-07 14:44:57');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notify_setting`
--

DROP TABLE IF EXISTS `notify_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notify_setting` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `event_key` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件键 new_order/refund/stock_warn',
  `event_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enable_voice` tinyint NOT NULL DEFAULT '1',
  `enable_sms` tinyint NOT NULL DEFAULT '0',
  `enable_app` tinyint NOT NULL DEFAULT '1',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `event_key` (`event_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家通知设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notify_setting`
--

LOCK TABLES `notify_setting` WRITE;
/*!40000 ALTER TABLE `notify_setting` DISABLE KEYS */;
INSERT INTO `notify_setting` VALUES (1,'new_order','新订单',1,0,1,'2026-05-04 16:03:17'),(2,'refund','退款申请',1,0,1,'2026-05-04 16:03:17'),(3,'stock_warn','库存预警',0,0,1,'2026-05-04 18:09:04');
/*!40000 ALTER TABLE `notify_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint unsigned NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待付款 1待接单 2备货中 3配送中 4待自提 5已完成 6已取消 7退款中 8已退款',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '0未支付 1已支付 2已退款',
  `delivery_type` tinyint NOT NULL COMMENT '1同城配送 2到店自提',
  `delivery_time` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预约时段，如 今日18:00-20:00',
  `address_id` bigint unsigned DEFAULT NULL COMMENT '配送地址',
  `consignee` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '冗余',
  `consignee_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `consignee_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pickup_point_id` bigint unsigned DEFAULT NULL COMMENT '自提点',
  `pickup_code` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自提码',
  `goods_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品总价',
  `coupon_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '优惠券抵扣',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '满减优惠',
  `delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '配送费',
  `pay_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '实付款',
  `user_coupon_id` bigint unsigned DEFAULT NULL COMMENT '使用的用户优惠券',
  `pay_method` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'wechat / alipay',
  `pay_time` datetime DEFAULT NULL,
  `pay_trade_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方流水号',
  `user_remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户备注',
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `group_buy_instance_id` bigint unsigned DEFAULT NULL COMMENT '所属拼团',
  `courier_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `courier_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delivered_at` datetime DEFAULT NULL COMMENT '送达 / 自提完成时间',
  `finished_at` datetime DEFAULT NULL COMMENT '订单完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_pickup_code` (`pickup_code`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'XG20260601001',1,0,0,1,'今日 18:00-20:00',1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,115.80,0.00,5.00,0.00,110.80,NULL,NULL,NULL,NULL,'请尽快配送',NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-01 10:00:00','2026-05-04 16:03:17',NULL,NULL),(2,'XG20260501002',1,1,1,1,'今日 18:00-20:00',1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,168.00,10.00,0.00,5.00,163.00,2,'wechat','2026-05-01 10:05:00','WX202605010002',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-01 10:00:00','2026-05-04 16:03:17',NULL,NULL),(3,'XG20260501003',1,3,1,1,'明日上午',1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,56.00,0.00,0.00,5.00,61.00,NULL,'wechat','2026-05-01 09:00:00','WX202605010003','多放几个',NULL,NULL,NULL,NULL,'2026-05-04 18:11:08',NULL,'2026-05-01 08:50:00','2026-05-04 16:03:17',NULL,NULL),(4,'XG20260430004',1,3,1,1,'今日 14:00-18:00',1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,89.00,0.00,5.00,0.00,84.00,NULL,'wechat','2026-04-30 13:00:00','WX202604300004',NULL,NULL,NULL,'小王','13800000001',NULL,NULL,'2026-04-30 12:00:00','2026-05-04 16:03:17',NULL,NULL),(5,'XG20260430005',1,4,1,2,NULL,NULL,NULL,NULL,NULL,1,'A3F2',38.00,0.00,0.00,0.00,38.00,NULL,'wechat','2026-04-30 11:00:00','WX202604300005',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-30 10:30:00','2026-05-04 16:03:17',NULL,NULL),(6,'XG20260429006',1,5,1,1,'昨日 18:00-20:00',1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,39.90,5.00,0.00,0.00,34.90,1,'wechat','2026-04-29 17:00:00','WX202604290006',NULL,NULL,NULL,'小王','13800000001','2026-04-29 19:30:00','2026-04-29 19:35:00','2026-04-29 17:00:00','2026-05-04 16:03:17',NULL,NULL),(7,'XG20260428007',1,6,0,1,NULL,1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,288.00,0.00,0.00,5.00,0.00,NULL,NULL,NULL,NULL,NULL,'不想买了',NULL,NULL,NULL,NULL,NULL,'2026-04-28 15:00:00','2026-05-04 16:03:17',NULL,NULL),(8,'XG20260427008',1,7,2,1,NULL,1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,59.00,0.00,0.00,5.00,64.00,NULL,'wechat','2026-04-27 10:00:00','WX202604270008',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-27 10:00:00','2026-05-04 16:03:17',NULL,NULL),(9,'XG20260426009',1,8,2,1,NULL,1,'小张','13900000001','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,29.90,0.00,0.00,5.00,34.90,NULL,'wechat','2026-04-26 09:00:00','WX202604260009',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-26 09:00:00','2026-05-04 16:03:17',NULL,NULL),(10,'XG20260425010',2,5,1,1,NULL,1,'老李','13900000002','浙江省杭州市西湖区文三路 200 号 1 幢 502',NULL,NULL,29.90,0.00,0.00,5.00,34.90,NULL,'wechat','2026-04-25 10:00:00','WX202604250010',NULL,NULL,1,NULL,NULL,'2026-04-25 15:00:00','2026-04-25 15:05:00','2026-04-25 10:00:00','2026-05-04 16:03:17',NULL,NULL),(11,'260505171254716113',3,6,0,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,79.80,0.00,5.00,0.00,74.80,NULL,'wechat',NULL,NULL,NULL,'超时未付款，系统自动取消',NULL,NULL,NULL,NULL,NULL,'2026-05-05 17:12:54','2026-05-05 17:12:54',NULL,NULL),(12,'260505171542982728',3,6,0,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,180.00,20.00,15.00,0.00,145.00,11,'wechat',NULL,NULL,NULL,'超时未付款，系统自动取消',NULL,NULL,NULL,NULL,NULL,'2026-05-05 17:15:42','2026-05-05 17:15:42',NULL,NULL),(13,'260505173117917486',3,5,1,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,180.00,0.00,15.00,0.00,165.00,NULL,'wechat',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-05 18:23:36','2026-05-05 18:31:44','2026-05-05 17:31:18','2026-05-05 17:31:18',NULL,NULL),(14,'260506153749956818',3,5,1,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,180.00,0.00,15.00,0.00,165.00,NULL,'wechat',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 16:06:58','2026-05-06 16:07:00','2026-05-06 15:37:49','2026-05-06 15:37:49',3,3),(15,'260506160721755369',3,1,1,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,180.00,0.00,15.00,0.00,165.00,NULL,'wechat',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 16:07:21','2026-05-06 16:07:21',3,3),(16,'260506184333196974',3,1,1,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,59.90,0.00,5.00,0.00,54.90,NULL,'wechat',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 18:43:34','2026-05-06 18:43:34',3,3),(17,'260506192307453075',3,6,0,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,29.90,0.00,0.00,0.00,29.90,NULL,'wechat',NULL,NULL,NULL,'超时未付款，系统自动取消',4,NULL,NULL,NULL,NULL,'2026-05-06 19:23:07','2026-05-06 19:39:00',3,3),(18,'260507230846480272',3,6,0,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,153.00,0.00,0.00,0.00,153.00,NULL,'wechat',NULL,NULL,'我要大号的','超时未付款，系统自动取消',5,NULL,NULL,NULL,NULL,'2026-05-07 23:08:47','2026-05-07 23:24:00',3,3),(19,'260507232222516703',3,6,0,1,NULL,3,'张三','020-81167888','广东省广州市海珠区新港中路397号',NULL,NULL,153.00,0.00,0.00,0.00,153.00,NULL,'wechat',NULL,NULL,NULL,'超时未付款，系统自动取消',6,NULL,NULL,NULL,NULL,'2026-05-07 23:22:22','2026-05-08 12:10:00',3,3);
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint unsigned NOT NULL,
  `product_id` bigint unsigned NOT NULL,
  `sku_id` bigint unsigned NOT NULL,
  `product_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `spec_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `is_reviewed` tinyint NOT NULL DEFAULT '0' COMMENT '是否已评价',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,1,1,'阳光玫瑰葡萄','500g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',29.90,35.80,2,59.80,0),(2,1,3,6,'丹东99草莓','300g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',39.90,49.00,1,39.90,0),(3,2,2,4,'智利进口车厘子 JJJ级','1kg礼盒','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/cherry.jpg',168.00,198.00,1,168.00,0),(4,3,1,2,'阳光玫瑰葡萄','1kg装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',56.00,65.00,1,56.00,0),(5,4,1,3,'阳光玫瑰葡萄','2kg礼盒装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',89.00,99.00,1,89.00,0),(6,5,14,28,'芒果班戟','4个装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/crepe.jpg',38.00,45.00,1,38.00,0),(7,6,3,6,'丹东99草莓','300g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',39.90,49.00,1,39.90,1),(8,7,2,5,'智利进口车厘子 JJJ级','2.5kg礼盒','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/cherry.jpg',288.00,328.00,1,288.00,0),(9,8,3,7,'丹东99草莓','500g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',59.00,79.00,1,59.00,0),(10,9,1,1,'阳光玫瑰葡萄','500g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',29.90,35.80,1,29.90,0),(11,10,3,6,'丹东99草莓','300g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',29.90,49.00,1,29.90,1),(12,11,4,8,'海南金煌芒果','1kg装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mango.jpg',19.90,25.00,1,19.90,0),(13,11,20,35,'每日坚果混合装 30包','30包箱','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/nuts.jpg',59.90,69.90,1,59.90,0),(14,12,24,46,'hhh','一斤','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',180.00,250.00,1,180.00,0),(15,13,24,46,'hhh','一斤','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',180.00,250.00,1,180.00,0),(16,14,24,46,'hhh','一斤','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',180.00,250.00,1,180.00,0),(17,15,24,46,'hhh','一斤','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',180.00,250.00,1,180.00,0),(18,16,20,35,'每日坚果混合装 30包','30包箱','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/nuts.jpg',59.90,69.90,1,59.90,0),(19,17,3,6,'丹东99草莓','300g装','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',29.90,49.00,1,29.90,0),(20,18,24,56,'hhh','一斤','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',153.00,250.00,1,153.00,0),(21,19,24,56,'hhh','一斤','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',153.00,250.00,1,153.00,0);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_status_log`
--

DROP TABLE IF EXISTS `order_status_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_status_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint unsigned NOT NULL,
  `from_status` tinyint DEFAULT NULL,
  `to_status` tinyint NOT NULL,
  `operator_type` tinyint NOT NULL COMMENT '1用户 2商家 3系统',
  `operator_id` bigint unsigned DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态流转日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_status_log`
--

LOCK TABLES `order_status_log` WRITE;
/*!40000 ALTER TABLE `order_status_log` DISABLE KEYS */;
INSERT INTO `order_status_log` VALUES (1,1,NULL,0,1,1,'用户下单','2026-06-01 10:00:00'),(2,2,NULL,0,1,1,'用户下单','2026-05-01 10:00:00'),(3,2,0,1,1,1,'用户支付','2026-05-01 10:05:00'),(4,3,NULL,0,1,1,'用户下单','2026-05-01 08:50:00'),(5,3,0,1,1,1,'用户支付','2026-05-01 09:00:00'),(6,3,1,2,2,1,'商家接单，开始备货','2026-05-01 09:10:00'),(7,4,NULL,0,1,1,'用户下单','2026-04-30 12:00:00'),(8,4,0,1,1,1,'用户支付','2026-04-30 13:00:00'),(9,4,1,2,2,1,'商家接单','2026-04-30 13:30:00'),(10,4,2,3,2,2,'配送员取货出发','2026-04-30 14:00:00'),(11,5,NULL,0,1,1,'用户下单（自提）','2026-04-30 10:30:00'),(12,5,0,1,1,1,'用户支付','2026-04-30 11:00:00'),(13,5,1,4,2,1,'备货完成，等待自提','2026-04-30 11:30:00'),(14,6,NULL,0,1,1,'用户下单','2026-04-29 17:00:00'),(15,6,0,1,1,1,'用户支付','2026-04-29 17:00:00'),(16,6,1,2,2,1,'商家接单','2026-04-29 17:10:00'),(17,6,2,3,2,2,'配送出发','2026-04-29 18:30:00'),(18,6,3,5,1,1,'用户确认收货','2026-04-29 19:35:00'),(19,7,NULL,0,1,1,'用户下单','2026-04-28 15:00:00'),(20,7,0,6,1,1,'用户取消：不想买了','2026-04-28 15:10:00'),(21,8,NULL,0,1,1,'用户下单','2026-04-27 10:00:00'),(22,8,0,1,1,1,'用户支付','2026-04-27 10:00:00'),(23,8,1,2,2,1,'商家接单','2026-04-27 10:10:00'),(24,8,2,5,2,2,'配送完成','2026-04-27 14:00:00'),(25,8,5,7,1,1,'用户申请退款：水果有损坏','2026-04-27 16:00:00'),(26,9,NULL,0,1,1,'用户下单','2026-04-26 09:00:00'),(27,9,0,1,1,1,'用户支付','2026-04-26 09:00:00'),(28,9,1,5,2,1,'商家接单并完成','2026-04-26 12:00:00'),(29,9,5,7,1,1,'用户申请退款','2026-04-26 14:00:00'),(30,9,7,8,2,1,'商家同意退款','2026-04-26 16:00:00'),(31,10,NULL,0,1,2,'用户下单（拼团）','2026-04-25 10:00:00'),(32,10,0,1,1,2,'用户支付','2026-04-25 10:00:00'),(33,10,1,2,2,1,'商家接单','2026-04-25 10:30:00'),(34,10,2,3,2,2,'配送出发','2026-04-25 14:00:00'),(35,10,3,5,1,2,'用户确认收货','2026-04-25 15:05:00'),(36,3,2,3,2,NULL,'标记出库','2026-05-04 18:11:08'),(37,11,NULL,0,1,3,'下单','2026-05-05 17:12:54'),(38,12,NULL,0,1,3,'下单','2026-05-05 17:15:43'),(39,11,0,6,3,NULL,'超时自动取消','2026-05-05 17:30:00'),(40,12,0,6,3,NULL,'超时自动取消','2026-05-05 17:31:00'),(41,13,NULL,1,1,3,'下单并支付','2026-05-05 17:31:18'),(42,13,1,2,2,NULL,'商家接单','2026-05-05 18:23:31'),(43,13,2,3,2,NULL,'标记出库','2026-05-05 18:23:35'),(44,13,3,5,2,NULL,'商家确认送达','2026-05-05 18:31:43'),(45,14,NULL,1,1,3,'下单并支付','2026-05-06 15:37:49'),(46,14,1,2,2,NULL,'商家接单','2026-05-06 16:06:56'),(47,14,2,3,2,NULL,'标记出库','2026-05-06 16:06:57'),(48,14,3,5,2,NULL,'商家确认送达','2026-05-06 16:07:00'),(49,15,NULL,1,1,3,'下单并支付','2026-05-06 16:07:21'),(50,16,NULL,1,1,3,'下单并支付','2026-05-06 18:43:33'),(51,17,NULL,0,1,3,'拼团下单','2026-05-06 19:23:07'),(52,17,0,6,3,NULL,'超时自动取消','2026-05-06 19:39:00'),(53,18,NULL,0,1,3,'拼团下单','2026-05-07 23:08:46'),(54,19,NULL,0,1,3,'拼团下单','2026-05-07 23:22:22'),(55,18,0,6,3,NULL,'超时自动取消','2026-05-07 23:24:00'),(56,19,0,6,3,NULL,'超时自动取消','2026-05-08 12:10:00');
/*!40000 ALTER TABLE `order_status_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pickup_point`
--

DROP TABLE IF EXISTS `pickup_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pickup_point` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_hours` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '如 09:00-21:00',
  `longitude` decimal(10,7) DEFAULT NULL,
  `latitude` decimal(10,7) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0停用 1启用',
  `sort` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自提点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pickup_point`
--

LOCK TABLES `pickup_point` WRITE;
/*!40000 ALTER TABLE `pickup_point` DISABLE KEYS */;
INSERT INTO `pickup_point` VALUES (1,'文三路总店','杭州市西湖区文三路 100 号','0571-12345678','08:00-22:00',120.1234567,30.2876543,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(2,'滨江分点','杭州市滨江区江南大道 88 号','0571-87654321','09:00-21:00',120.2098765,30.2056789,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16');
/*!40000 ALTER TABLE `pickup_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `subtitle` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简短卖点',
  `category_id` bigint unsigned NOT NULL COMMENT '二级分类id',
  `main_image` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主图',
  `video_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '商品介绍 富文本HTML',
  `min_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最低售价（多规格冗余）',
  `max_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最高售价',
  `total_stock` int NOT NULL DEFAULT '0' COMMENT '总库存（冗余）',
  `sales` int NOT NULL DEFAULT '0' COMMENT '累计销量',
  `is_recommend` tinyint NOT NULL DEFAULT '0' COMMENT '店主推荐',
  `support_delivery` tinyint NOT NULL DEFAULT '1' COMMENT '支持同城配送',
  `support_pickup` tinyint NOT NULL DEFAULT '1' COMMENT '支持自提',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0已下架 1在售 2回收站',
  `stock_warn_threshold` int NOT NULL DEFAULT '5' COMMENT '库存预警阈值',
  `sort` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_sales` (`sales`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SPU';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'阳光玫瑰葡萄','当日新摘 现剪现发',101,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',NULL,NULL,29.90,89.00,200,350,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:35:13'),(2,'智利进口车厘子 JJJ级','空运直达 颗颗甜爽',201,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/cherry.jpg',NULL,NULL,168.00,288.00,80,120,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(3,'丹东99草莓','现摘现发 颗颗饱满',101,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',NULL,NULL,39.90,59.00,150,220,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(4,'海南金煌芒果','树上自然熟 甜过初恋',102,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mango.jpg',NULL,NULL,19.90,49.00,299,1200,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-05 17:12:54'),(5,'赣南脐橙','皮薄多汁 橙香浓郁',401,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/orange.jpg',NULL,NULL,12.80,22.80,230,300,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(6,'新鲜红富士苹果','口感清甜 脆爽多汁',402,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/apple.jpg',NULL,NULL,12.80,22.80,250,500,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(7,'精品水果礼盒','6种鲜果 精美包装',301,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/giftbox.jpg',NULL,NULL,128.00,268.00,30,45,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(8,'小蜜蜂蓝莓','颗粒饱满 花香浓郁',201,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/blueberry.jpg',NULL,NULL,29.90,59.00,100,180,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(9,'水蜜桃','果肉细腻 汁多味甜',102,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/peach.jpg',NULL,NULL,25.00,45.00,130,260,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(10,'黑美人西瓜','沙瓤清甜 消暑首选',402,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/watermelon.jpg',NULL,NULL,15.00,35.00,90,420,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(11,'云南蓝莓大果','颗粒饱满 花香浓郁',201,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/blueberry.jpg',NULL,NULL,29.90,59.00,0,180,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(12,'泰国山竹','果后驾到 细腻清甜',202,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mangosteen.jpg',NULL,NULL,45.00,79.00,60,90,0,1,1,0,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(13,'新西兰奇异果','维C之王 酸甜可口',201,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/kiwi.jpg',NULL,NULL,19.90,39.90,120,260,1,1,0,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(14,'芒果班戟','现做现发 绵密奶油',602,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/crepe.jpg',NULL,NULL,38.00,38.00,15,320,1,0,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:17'),(15,'有机小番茄','农场直供 鲜嫩多汁',701,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/tomato.jpg',NULL,NULL,18.80,18.80,120,640,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(16,'有机胡萝卜','甜脆爽口 营养丰富',702,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/carrot.jpg',NULL,NULL,12.50,12.50,90,280,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(17,'农家土鸡蛋 30枚','散养走地鸡 蛋黄饱满',801,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/egg.jpg',NULL,NULL,32.00,32.00,60,1500,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(18,'鲜活大闸蟹 4两公','阳澄湖直发 鲜活到家',802,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/crab.jpg',NULL,NULL,88.00,128.00,0,420,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(19,'榴莲千层蛋糕','浓郁香软 限量供应',602,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/duriancake.jpg',NULL,NULL,68.00,68.00,3,150,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(20,'每日坚果混合装 30包','7种坚果 营养均衡',502,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/nuts.jpg',NULL,NULL,59.90,89.90,499,4600,1,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-05 17:12:54'),(21,'芒果干大袋装','自然风干 酸甜可口',501,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mangodry.jpg',NULL,NULL,15.90,15.90,400,2100,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(22,'NFC鲜榨橙汁 1L','0添加 原汁原味',601,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/orangejuice.jpg',NULL,NULL,22.90,22.90,200,980,0,1,1,1,5,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(23,'阳光玫瑰葡萄(副本)','当日新摘 现剪现发',101,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',NULL,NULL,29.90,89.00,200,0,1,1,1,0,5,0,'2026-05-04 16:03:16','2026-05-04 16:35:13'),(24,'hhh','你好',1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',NULL,'123',180.00,180.00,7,0,1,1,1,1,5,0,'2026-05-05 11:55:20','2026-05-06 16:07:21'),(25,'hhh(副本)','你好',1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',NULL,'123',180.00,180.00,7,0,1,1,1,0,5,0,'2026-05-05 11:55:20','2026-05-06 16:07:21'),(26,'hhh(副本)','你好',1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',NULL,'123',180.00,180.00,7,0,1,1,1,0,5,0,'2026-05-05 11:55:20','2026-05-06 16:07:21');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_image`
--

DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `product_id` bigint unsigned NOT NULL,
  `url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1轮播主图 2详情图',
  `sort` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_product_id_type` (`product_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_image`
--

LOCK TABLES `product_image` WRITE;
/*!40000 ALTER TABLE `product_image` DISABLE KEYS */;
INSERT INTO `product_image` VALUES (5,2,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/cherry.jpg',1,1),(6,2,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/cherry-2.jpg',1,2),(7,3,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry.jpg',1,1),(8,3,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/strawberry-2.jpg',1,2),(9,4,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mango.jpg',1,1),(10,5,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/orange.jpg',1,1),(11,6,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/apple.jpg',1,1),(12,7,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/giftbox.jpg',1,1),(13,8,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/blueberry.jpg',1,1),(14,9,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/peach.jpg',1,1),(15,10,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/watermelon.jpg',1,1),(16,11,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/blueberry.jpg',1,1),(17,12,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mangosteen.jpg',1,1),(18,13,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/kiwi.jpg',1,1),(19,13,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/kiwi-2.jpg',1,2),(20,14,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/crepe.jpg',1,1),(21,15,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/tomato.jpg',1,1),(22,16,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/carrot.jpg',1,1),(23,17,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/egg.jpg',1,1),(24,18,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/crab.jpg',1,1),(25,19,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/duriancake.jpg',1,1),(26,20,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/nuts.jpg',1,1),(27,21,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/mangodry.jpg',1,1),(28,22,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/orangejuice.jpg',1,1),(29,1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape.jpg',1,0),(30,1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape-2.jpg',1,1),(31,1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape-3.jpg',1,2),(32,1,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/product/grape-d1.jpg',2,3),(59,24,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/8522adea6d9c4e6493f372e5cabab6c0.jpg',1,0),(60,24,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/07/8a532c3250674817ab4b622a1bd9153f.jpg',1,1),(61,24,'https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/admin/2026/05/05/b80932ee156c4f75b5ea4b188f161458.jpg',2,2);
/*!40000 ALTER TABLE `product_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_sku`
--

DROP TABLE IF EXISTS `product_sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_sku` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `product_id` bigint unsigned NOT NULL,
  `spec_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规格名 如 500g装 / 1kg装',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU编码',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '划线原价',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `stock` int NOT NULL DEFAULT '0',
  `sales` int NOT NULL DEFAULT '0',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '默认规格',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1启用',
  `sort` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格 SKU';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_sku`
--

LOCK TABLES `product_sku` WRITE;
/*!40000 ALTER TABLE `product_sku` DISABLE KEYS */;
INSERT INTO `product_sku` VALUES (4,2,'1kg礼盒','SKU-2-1',168.00,198.00,110.00,50,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(5,2,'2.5kg礼盒','SKU-2-2',288.00,328.00,200.00,30,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(6,3,'300g装','SKU-3-1',39.90,49.00,22.00,100,0,1,1,0,'2026-05-04 16:03:16','2026-05-06 19:39:00'),(7,3,'500g装','SKU-3-2',59.00,79.00,38.00,50,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(8,4,'1kg装','SKU-4-1',19.90,25.00,10.00,200,0,1,1,0,'2026-05-04 16:03:16','2026-05-05 17:30:00'),(9,4,'2.5kg装','SKU-4-2',49.00,58.00,25.00,100,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(10,5,'5斤装','SKU-5-1',12.80,16.00,7.00,150,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(11,5,'10斤装','SKU-5-2',22.80,28.00,14.00,80,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(12,6,'500g装','SKU-6-1',12.80,15.80,7.00,150,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(13,6,'1kg装','SKU-6-2',22.80,26.80,14.00,100,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(14,7,'标准礼盒','SKU-7-1',128.00,158.00,80.00,20,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(15,7,'豪华礼盒','SKU-7-2',268.00,298.00,180.00,10,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(16,8,'125g装','SKU-8-1',29.90,35.00,18.00,60,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(17,8,'250g装','SKU-8-2',59.00,69.00,36.00,40,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(18,9,'2个装','SKU-9-1',25.00,32.00,15.00,80,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(19,9,'4个装','SKU-9-2',45.00,58.00,28.00,50,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(20,10,'1个约5斤','SKU-10-1',15.00,20.00,8.00,60,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(21,10,'1个约8斤','SKU-10-2',35.00,42.00,20.00,30,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(22,11,'125g装','SKU-11-1',29.90,35.00,18.00,0,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(23,11,'250g装','SKU-11-2',59.00,69.00,36.00,0,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(24,12,'500g装','SKU-12-1',45.00,58.00,28.00,40,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(25,12,'1kg装','SKU-12-2',79.00,99.00,50.00,20,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(26,13,'6个装','SKU-13-1',19.90,25.00,12.00,80,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(27,13,'12个装','SKU-13-2',39.90,49.00,24.00,40,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(28,14,'4个装','SKU-14-1',38.00,45.00,22.00,15,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(29,15,'500g盒','SKU-15-1',18.80,22.00,10.00,120,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(30,16,'1kg袋','SKU-16-1',12.50,15.00,7.00,90,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(31,17,'30枚盒','SKU-17-1',32.00,38.00,20.00,60,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(32,18,'4两公蟹 2只','SKU-18-1',88.00,108.00,55.00,0,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(33,18,'4两公蟹 4只','SKU-18-2',128.00,158.00,80.00,0,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(34,19,'6寸','SKU-19-1',68.00,78.00,38.00,3,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(35,20,'30包箱','SKU-20-1',59.90,69.90,35.00,299,1,1,1,0,'2026-05-04 16:03:16','2026-05-06 18:43:33'),(36,20,'15包箱','SKU-20-2',89.90,109.00,55.00,200,0,0,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(37,21,'大袋200g','SKU-21-1',15.90,19.90,9.00,400,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(38,22,'1L瓶','SKU-22-1',22.90,28.00,13.00,200,0,1,1,0,'2026-05-04 16:03:16','2026-05-04 16:03:16'),(39,1,'500g装',NULL,29.90,35.80,NULL,100,0,1,1,0,'2026-05-04 16:35:08','2026-05-04 16:35:08'),(40,1,'1kg装',NULL,56.00,65.00,NULL,80,0,0,1,0,'2026-05-04 16:35:08','2026-05-04 16:35:08'),(41,1,'2kg礼盒装',NULL,89.00,99.00,NULL,20,0,0,1,0,'2026-05-04 16:35:08','2026-05-04 16:35:08'),(42,23,'500g装',NULL,29.90,35.80,NULL,100,0,1,1,0,'2026-05-04 16:35:08','2026-05-04 16:35:08'),(43,23,'1kg装',NULL,56.00,65.00,NULL,80,0,0,1,0,'2026-05-04 16:35:08','2026-05-04 16:35:08'),(44,23,'2kg礼盒装',NULL,89.00,99.00,NULL,20,0,0,1,0,'2026-05-04 16:35:08','2026-05-04 16:35:08'),(56,24,'一斤',NULL,180.00,250.00,NULL,7,0,1,1,0,'2026-05-07 22:54:54','2026-05-08 12:10:00'),(57,25,'一斤',NULL,180.00,250.00,NULL,7,0,1,1,0,'2026-05-07 22:54:54','2026-05-08 12:10:00'),(58,26,'一斤',NULL,180.00,250.00,NULL,7,0,1,1,0,'2026-05-07 22:54:54','2026-05-08 12:10:00');
/*!40000 ALTER TABLE `product_sku` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotion_rule`
--

DROP TABLE IF EXISTS `promotion_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotion_rule` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_amount` decimal(10,2) NOT NULL COMMENT '满X元',
  `discount` decimal(10,2) NOT NULL COMMENT '减Y元',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='满减规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotion_rule`
--

LOCK TABLES `promotion_rule` WRITE;
/*!40000 ALTER TABLE `promotion_rule` DISABLE KEYS */;
INSERT INTO `promotion_rule` VALUES (1,'满 39 减 5',39.00,5.00,NULL,NULL,1,'2026-05-04 16:03:17'),(2,'满 99 减 15',99.00,15.00,NULL,NULL,1,'2026-05-04 16:03:17');
/*!40000 ALTER TABLE `promotion_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refund`
--

DROP TABLE IF EXISTS `refund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `refund_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1仅退款 2退货退款',
  `amount` decimal(10,2) NOT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `images` json DEFAULT NULL COMMENT '凭证图片URL数组',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1已同意 2已拒绝 3已退款',
  `reject_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `handled_by` bigint unsigned DEFAULT NULL COMMENT '处理员工ID',
  `handled_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款 / 售后';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refund`
--

LOCK TABLES `refund` WRITE;
/*!40000 ALTER TABLE `refund` DISABLE KEYS */;
INSERT INTO `refund` VALUES (1,'RF20260427001',8,1,1,64.00,'水果有损坏，不新鲜','[\"https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/refund/damage1.jpg\", \"https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/refund/damage2.jpg\"]',0,NULL,NULL,NULL,'2026-04-27 16:00:00','2026-05-04 16:03:17',NULL,NULL),(2,'RF20260426002',9,1,1,34.90,'商品与描述不符','[]',3,NULL,1,'2026-04-26 16:00:00','2026-04-26 14:00:00','2026-05-04 16:03:17',NULL,NULL);
/*!40000 ALTER TABLE `refund` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_export`
--

DROP TABLE IF EXISTS `report_export`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report_export` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `staff_id` bigint unsigned NOT NULL,
  `report_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'sales/inventory/customer/delivery/finance',
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `format` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'xlsx/csv/pdf',
  `file_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint unsigned NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0生成中 1已完成 2失败',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_staff_created` (`staff_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表导出记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_export`
--

LOCK TABLES `report_export` WRITE;
/*!40000 ALTER TABLE `report_export` DISABLE KEYS */;
INSERT INTO `report_export` VALUES (1,1,'sales','2026-04-04','2026-05-04','xlsx','销售报表_20260404_20260504.xlsx',4208,1,'2026-05-04 23:12:10'),(2,1,'delivery','2026-04-05','2026-05-05','xlsx','配送报表_20260405_20260505.xlsx',4238,1,'2026-05-05 12:35:38');
/*!40000 ALTER TABLE `report_export` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint unsigned NOT NULL,
  `order_item_id` bigint unsigned NOT NULL,
  `product_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `rating` tinyint NOT NULL COMMENT '总评 1-5',
  `freshness_rating` tinyint DEFAULT NULL COMMENT '新鲜度',
  `value_rating` tinyint DEFAULT NULL COMMENT '性价比',
  `package_rating` tinyint DEFAULT NULL COMMENT '包装',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `images` json DEFAULT NULL COMMENT '图片URL数组',
  `is_anonymous` tinyint NOT NULL DEFAULT '0',
  `is_hidden` tinyint NOT NULL DEFAULT '0' COMMENT '商家隐藏',
  `merchant_reply` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `replied_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_rating` (`rating`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,6,6,3,1,5,5,4,5,'草莓非常新鲜，甜度也够，下次还会回购！','[\"https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/review/strawberry-r1.jpg\", \"https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/review/strawberry-r2.jpg\"]',0,0,'感谢您的好评，期待再次光临！','2026-04-30 10:00:00','2026-04-29 20:00:00','2026-05-04 16:03:17',NULL,NULL),(2,10,11,3,2,3,3,3,4,'一般般，有几颗不太甜','[]',0,0,NULL,NULL,'2026-04-25 20:00:00','2026-05-04 16:03:17',NULL,NULL),(3,6,6,3,1,4,4,5,4,'包装不错，配送也快','[]',1,0,NULL,NULL,'2026-04-30 08:00:00','2026-05-04 16:03:17',NULL,NULL),(4,6,6,3,1,2,2,2,3,'这次收到的有点磕碰，不太满意','[\"https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/review/damage.jpg\"]',0,0,'抱歉给您带来不好的体验，我们会改进包装','2026-05-01 09:00:00','2026-04-30 22:00:00','2026-05-04 16:03:17',NULL,NULL);
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_history`
--

DROP TABLE IF EXISTS `search_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `keyword` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_history`
--

LOCK TABLES `search_history` WRITE;
/*!40000 ALTER TABLE `search_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop`
--

DROP TABLE IF EXISTS `shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `logo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺Logo',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺简介',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业地址',
  `business_hours` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业时间，如 08:00-22:00',
  `is_open` tinyint NOT NULL DEFAULT '1' COMMENT '0已打烊 1营业中',
  `auto_accept` tinyint NOT NULL DEFAULT '0' COMMENT '自动接单开关',
  `support_phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `voice_notify` tinyint NOT NULL DEFAULT '1' COMMENT '来单语音播报',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺信息（单商户）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop`
--

LOCK TABLES `shop` WRITE;
/*!40000 ALTER TABLE `shop` DISABLE KEYS */;
INSERT INTO `shop` VALUES (1,'鲜果记','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/shop/logo.jpg','新鲜直达，每日精选','0571-12345678','杭州市西湖区文三路 100 号','08:00-22:00',1,0,NULL,1,'2026-05-04 16:03:16','2026-05-04 16:03:16');
/*!40000 ALTER TABLE `shop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password_hash` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'bcrypt 密码哈希',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'admin' COMMENT 'owner店主 / admin管理员 / packer打包员 / courier配送员',
  `permissions` json DEFAULT NULL COMMENT '权限点 JSON 数组',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1正常',
  `last_login_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `password_changed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家员工';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,'admin','$2a$10$drtyzCNCaV7iscs9s19ckehpcEuwyj04xt/0M3QPP38DMTRsUYV8G','张老板','13800000000',NULL,'owner',NULL,1,'2026-05-08 12:12:13','2026-05-04 16:03:16','2026-05-04 16:03:16',NULL),(2,'packer','$2b$10$.Xv/qEaxR5uW/.xIgcvbw.z3uf/0m78Lvq3Pk/cJi7T6QE7mjemo6','小王','13800000001',NULL,'packer',NULL,1,NULL,'2026-05-04 16:03:16','2026-05-04 16:03:16',NULL);
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号（登录）',
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '0未知 1男 2女',
  `birthday` date DEFAULT NULL,
  `wx_openid` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信 openid',
  `wx_unionid` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tag` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'new' COMMENT 'new新客 / regular老客 / silent沉默',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1正常',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_wx_openid` (`wx_openid`),
  KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'13900000001','小张','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/avatar/user1.jpg',0,NULL,NULL,NULL,'new',1,'2026-05-04 16:03:17','2026-05-06 19:27:17','2026-05-04 16:03:17','2026-05-04 16:03:17'),(2,'13900000002','老李','https://ojc-xianguoji.oss-cn-beijing.aliyuncs.com/seed/avatar/user2.jpg',0,NULL,NULL,NULL,'regular',1,'2026-05-04 16:03:17',NULL,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(3,NULL,'阿聪','http://127.0.0.1:8080/static/2026/05/04/c7c7a884b15049d0971d0a82e1b2947d.jpeg',0,NULL,'opclQ1wYH79sM7Iip9maqITAotIo',NULL,'new',1,'2026-05-04 16:09:33','2026-05-08 12:22:59','2026-05-04 16:09:33','2026-05-04 16:09:33');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `consignee` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `tag` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '家/公司/学校',
  `is_default` tinyint NOT NULL DEFAULT '0',
  `longitude` decimal(10,7) DEFAULT NULL,
  `latitude` decimal(10,7) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
INSERT INTO `user_address` VALUES (1,1,'小张','13900000001','浙江省','杭州市','西湖区','文三路 200 号 1 幢 502','家',1,NULL,NULL,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(2,1,'小张','13900000001','浙江省','杭州市','西湖区','文一路阿里巴巴 1 号楼','公司',0,NULL,NULL,'2026-05-04 16:03:17','2026-05-04 16:03:17'),(3,3,'张三','020-81167888','广东省','广州市','海珠区','新港中路397号',NULL,1,NULL,NULL,'2026-05-05 17:09:48','2026-05-05 17:09:48');
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_coupon`
--

DROP TABLE IF EXISTS `user_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_coupon` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `coupon_id` bigint unsigned NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0未使用 1已使用 2已过期',
  `order_id` bigint unsigned DEFAULT NULL COMMENT '使用订单',
  `received_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `used_at` datetime DEFAULT NULL,
  `expire_at` datetime DEFAULT NULL,
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_coupon`
--

LOCK TABLES `user_coupon` WRITE;
/*!40000 ALTER TABLE `user_coupon` DISABLE KEYS */;
INSERT INTO `user_coupon` VALUES (1,1,1,0,NULL,'2026-05-01 10:00:00',NULL,'2026-12-31 23:59:59',NULL,NULL),(2,1,2,0,NULL,'2026-05-01 10:00:00',NULL,'2026-12-31 23:59:59',NULL,NULL),(3,1,4,0,NULL,'2026-05-02 14:00:00',NULL,'2026-06-30 23:59:59',NULL,NULL),(4,1,5,0,NULL,'2026-05-03 09:00:00',NULL,'2026-12-31 23:59:59',NULL,NULL),(5,1,6,0,NULL,'2026-05-04 08:00:00',NULL,'2026-05-11 23:59:59',NULL,NULL),(6,1,7,2,NULL,'2025-06-01 10:00:00',NULL,'2025-12-31 23:59:59',NULL,NULL),(7,2,1,1,10,'2026-04-01 10:00:00','2026-04-25 10:00:00','2026-12-31 23:59:59',NULL,NULL),(8,2,2,0,NULL,'2026-04-05 10:00:00',NULL,'2026-12-31 23:59:59',NULL,NULL),(9,3,1,0,NULL,'2026-05-05 13:33:38',NULL,'2026-12-31 00:00:00',NULL,NULL),(10,3,2,0,NULL,'2026-05-05 13:34:00',NULL,'2026-12-31 00:00:00',NULL,NULL),(11,3,3,0,12,'2026-05-05 13:34:03','2026-05-05 17:15:43','2026-12-31 00:00:00',NULL,NULL),(12,3,1,0,NULL,'2026-05-06 15:08:52',NULL,'2026-12-31 00:00:00',3,3),(13,3,2,0,NULL,'2026-05-06 15:13:46',NULL,'2026-12-31 00:00:00',3,3),(14,3,2,0,NULL,'2026-05-06 15:14:00',NULL,'2026-12-31 00:00:00',3,3),(15,3,4,0,NULL,'2026-05-06 15:14:05',NULL,'2026-06-30 00:00:00',3,3),(16,3,4,0,NULL,'2026-05-06 15:14:06',NULL,'2026-06-30 00:00:00',3,3),(17,3,4,0,NULL,'2026-05-06 15:14:06',NULL,'2026-06-30 00:00:00',3,3),(18,3,5,0,NULL,'2026-05-06 15:14:12',NULL,'2026-12-31 00:00:00',3,3),(19,3,5,0,NULL,'2026-05-06 15:14:13',NULL,'2026-12-31 00:00:00',3,3),(20,3,2,0,NULL,'2026-05-06 15:23:58',NULL,'2026-12-31 00:00:00',3,3),(21,3,2,0,NULL,'2026-05-06 15:24:01',NULL,'2026-12-31 00:00:00',3,3),(22,3,2,0,NULL,'2026-05-06 15:24:01',NULL,'2026-12-31 00:00:00',3,3),(23,3,3,0,NULL,'2026-05-07 22:39:36',NULL,'2026-12-31 00:00:00',3,3),(24,3,6,0,NULL,'2026-05-07 22:39:38',NULL,'2026-05-14 22:39:38',3,3),(25,3,8,0,NULL,'2026-05-07 22:39:38',NULL,'2026-05-05 17:00:00',3,3);
/*!40000 ALTER TABLE `user_coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'xianguoji'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-08 12:26:06
