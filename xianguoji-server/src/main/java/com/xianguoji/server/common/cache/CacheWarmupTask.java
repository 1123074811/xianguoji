package com.xianguoji.server.common.cache;

import com.xianguoji.server.module.catalog.service.CatalogService;
import com.xianguoji.server.module.shop.service.ShopService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmupTask {

    private final CatalogService catalogService;
    private final ShopService shopService;
    private final PickupGeoService pickupGeoService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Bean 初始化时立即清除旧格式缓存，避免应用启动后、warmup 前的请求读到脏数据
     */
    @PostConstruct
    public void clearOldCache() {
        clearCacheEntries();
    }

    /**
     * 应用启动完成后预热首页热点数据
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnStartup() {
        log.info("[CacheWarmup] 开始预热首页数据...");
        try {
            warmupAll();
            log.info("[CacheWarmup] 预热完成");
        } catch (Exception e) {
            log.error("[CacheWarmup] 预热失败", e);
        }
    }

    /**
     * 每天早8点定时刷新预热
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void warmupScheduled() {
        log.info("[CacheWarmup] 定时预热开始...");
        try {
            warmupAll();
            log.info("[CacheWarmup] 定时预热完成");
        } catch (Exception e) {
            log.error("[CacheWarmup] 定时预热失败", e);
        }
    }

    private void warmupAll() {
        // 分类树
        catalogService.getCategoryTree();
        // Banner
        catalogService.getBannerList();
        // 热门搜索
        catalogService.getHotSearchList();
        // 店主推荐
        catalogService.getRecommendProducts();
        // 店铺信息
        try { shopService.getShopInfo(); } catch (Exception e) { log.warn("[CacheWarmup] shopInfo 预热跳过: {}", e.getMessage()); }
        // GEO 自提点索引
        try { pickupGeoService.rebuildGeoIndex(); } catch (Exception e) { log.warn("[CacheWarmup] GEO 重建失败: {}", e.getMessage()); }

        // 预热标记，供健康检查
        stringRedisTemplate.opsForValue().set(
                "xgj:cache:warmup:last",
                String.valueOf(System.currentTimeMillis()),
                Duration.ofHours(25));
    }

    private void clearCacheEntries() {
        // 通过 StringRedisTemplate 扫描并删除 xgj:cache:* 旧格式缓存
        var keys = stringRedisTemplate.keys("xgj:cache:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("[CacheWarmup] 清除旧缓存 {} 条", keys.size());
        }
    }
}
