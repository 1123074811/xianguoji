package com.xianguoji.server.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xianguoji.server.common.config.OssProperties;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.catalog.entity.Banner;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductImage;
import com.xianguoji.server.module.catalog.mapper.BannerMapper;
import com.xianguoji.server.module.catalog.mapper.ProductImageMapper;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.entity.Refund;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import com.xianguoji.server.module.order.mapper.RefundMapper;
import com.xianguoji.server.module.review.entity.Review;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import com.xianguoji.server.module.shop.entity.Shop;
import com.xianguoji.server.module.shop.mapper.ShopMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 一次性迁移端点：将本地 seed 图片上传到 OSS，并更新数据库中所有引用。
 * 调用一次即可，之后可删除此 Controller。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
@ConditionalOnBean(OSS.class)
public class SeedMigrationController {

    private final OSS ossClient;
    private final OssProperties ossProps;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final BannerMapper bannerMapper;
    private final ShopMapper shopMapper;
    private final UserMapper userMapper;
    private final OrderItemMapper orderItemMapper;
    private final ReviewMapper reviewMapper;
    private final RefundMapper refundMapper;

    private static final String SEED_BASE_DIR = "D:/xianguoji/upload/seed";
    private static final String OSS_SEED_PREFIX = "seed";

    @PostMapping("/migrate-to-oss")
    public R<Map<String, Object>> migrateToOss() {
        log.info("===== 开始 seed 图片迁移到 OSS =====");

        // 1. 扫描本地 seed 文件
        Map<String, String> urlMapping = new LinkedHashMap<>(); // /static/seed/xxx.jpg -> https://oss/seed/xxx.jpg
        List<String> uploadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(SEED_BASE_DIR))) {
            List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            log.info("发现 {} 个本地 seed 文件", files.size());

            for (Path file : files) {
                String relativePath = Paths.get(SEED_BASE_DIR).relativize(file).toString().replace('\\', '/');
                String ossKey = OSS_SEED_PREFIX + "/" + relativePath;
                String oldUrl = "/static/seed/" + relativePath;
                String newUrl = buildOssUrl(ossKey);

                try {
                    uploadToOss(file, ossKey);
                    urlMapping.put(oldUrl, newUrl);
                    uploadedFiles.add(relativePath);
                    log.info("  [OK] {} -> {}", relativePath, newUrl);
                } catch (Exception e) {
                    failedFiles.add(relativePath);
                    log.error("  [FAIL] {} : {}", relativePath, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("扫描 seed 目录失败", e);
            return R.fail(500, "扫描 seed 目录失败: " + e.getMessage());
        }

        if (urlMapping.isEmpty()) {
            return R.fail(404, "没有找到可迁移的 seed 文件");
        }

        // 2. 更新数据库
        int productCount = updateProductImages(urlMapping);
        int productImageCount = updateProductImageTable(urlMapping);
        int bannerCount = updateBannerImages(urlMapping);
        int shopCount = updateShopLogo(urlMapping);
        int userCount = updateUserAvatars(urlMapping);
        int orderItemCount = updateOrderItemImages(urlMapping);
        int reviewCount = updateReviewImages(urlMapping);
        int refundCount = updateRefundImages(urlMapping);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uploadedFiles", uploadedFiles.size());
        result.put("failedFiles", failedFiles);
        result.put("urlMappingSize", urlMapping.size());
        result.put("dbUpdates", Map.of(
                "product", productCount,
                "product_image", productImageCount,
                "banner", bannerCount,
                "shop", shopCount,
                "user", userCount,
                "order_item", orderItemCount,
                "review", reviewCount,
                "refund", refundCount
        ));
        result.put("sampleMapping", urlMapping.entrySet().stream().limit(3).collect(Collectors.toList()));

        log.info("===== seed 图片迁移完成: {} 个文件上传, DB 更新: product={}, product_image={}, banner={}, shop={}, user={}, order_item={}, review={}, refund={} =====",
                uploadedFiles.size(), productCount, productImageCount, bannerCount, shopCount, userCount, orderItemCount, reviewCount, refundCount);

        return R.ok(result);
    }

    private void uploadToOss(Path localFile, String ossKey) throws IOException {
        String fileName = localFile.getFileName().toString();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";
        String contentType = switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(Files.size(localFile));
        meta.setContentType(contentType);
        // 设置缓存 1 年，seed 图片不会变
        meta.setCacheControl("max-age=31536000, public");

        try (FileInputStream fis = new FileInputStream(localFile.toFile())) {
            ossClient.putObject(ossProps.getBucket(), ossKey, fis, meta);
        }
    }

    private String buildOssUrl(String ossKey) {
        if (ossProps.getDomain() != null && !ossProps.getDomain().isBlank()) {
            return ossProps.getDomain().replaceAll("/+$", "") + "/" + ossKey;
        }
        return "https://" + ossProps.getBucket() + "." + ossProps.getEndpoint() + "/" + ossKey;
    }

    // ---- 数据库更新 ----

    private int updateProductImages(Map<String, String> mapping) {
        int count = 0;
        List<Product> list = productMapper.selectList(
                new LambdaQueryWrapper<Product>().likeRight(Product::getMainImage, "/static/seed/"));
        for (Product p : list) {
            String newUrl = mapping.get(p.getMainImage());
            if (newUrl != null) {
                p.setMainImage(newUrl);
                productMapper.updateById(p);
                count++;
            }
        }
        return count;
    }

    private int updateProductImageTable(Map<String, String> mapping) {
        int count = 0;
        List<ProductImage> list = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>().likeRight(ProductImage::getUrl, "/static/seed/"));
        for (ProductImage pi : list) {
            String newUrl = mapping.get(pi.getUrl());
            if (newUrl != null) {
                pi.setUrl(newUrl);
                productImageMapper.updateById(pi);
                count++;
            }
        }
        return count;
    }

    private int updateBannerImages(Map<String, String> mapping) {
        int count = 0;
        List<Banner> list = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().likeRight(Banner::getImage, "/static/seed/"));
        for (Banner b : list) {
            String newUrl = mapping.get(b.getImage());
            if (newUrl != null) {
                b.setImage(newUrl);
                bannerMapper.updateById(b);
                count++;
            }
        }
        return count;
    }

    private int updateShopLogo(Map<String, String> mapping) {
        int count = 0;
        List<Shop> list = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>().likeRight(Shop::getLogo, "/static/seed/"));
        for (Shop s : list) {
            String newUrl = mapping.get(s.getLogo());
            if (newUrl != null) {
                s.setLogo(newUrl);
                shopMapper.updateById(s);
                count++;
            }
        }
        return count;
    }

    private int updateUserAvatars(Map<String, String> mapping) {
        int count = 0;
        List<User> list = userMapper.selectList(
                new LambdaQueryWrapper<User>().likeRight(User::getAvatar, "/static/seed/"));
        for (User u : list) {
            String newUrl = mapping.get(u.getAvatar());
            if (newUrl != null) {
                u.setAvatar(newUrl);
                userMapper.updateById(u);
                count++;
            }
        }
        return count;
    }

    private int updateOrderItemImages(Map<String, String> mapping) {
        int count = 0;
        List<OrderItem> list = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().likeRight(OrderItem::getImage, "/static/seed/"));
        for (OrderItem oi : list) {
            String newUrl = mapping.get(oi.getImage());
            if (newUrl != null) {
                oi.setImage(newUrl);
                orderItemMapper.updateById(oi);
                count++;
            }
        }
        return count;
    }

    private int updateReviewImages(Map<String, String> mapping) {
        int count = 0;
        List<Review> list = reviewMapper.selectList(null);
        for (Review r : list) {
            if (r.getImages() == null || r.getImages().isEmpty()) continue;
            boolean changed = false;
            List<String> newImages = new ArrayList<>();
            for (String img : r.getImages()) {
                String newUrl = mapping.get(img);
                if (newUrl != null) {
                    newImages.add(newUrl);
                    changed = true;
                } else {
                    newImages.add(img);
                }
            }
            if (changed) {
                r.setImages(newImages);
                reviewMapper.updateById(r);
                count++;
            }
        }
        return count;
    }

    private int updateRefundImages(Map<String, String> mapping) {
        int count = 0;
        List<Refund> list = refundMapper.selectList(null);
        for (Refund rf : list) {
            if (rf.getImages() == null || rf.getImages().isEmpty()) continue;
            boolean changed = false;
            List<String> newImages = new ArrayList<>();
            for (String img : rf.getImages()) {
                String newUrl = mapping.get(img);
                if (newUrl != null) {
                    newImages.add(newUrl);
                    changed = true;
                } else {
                    newImages.add(img);
                }
            }
            if (changed) {
                rf.setImages(newImages);
                refundMapper.updateById(rf);
                count++;
            }
        }
        return count;
    }
}
