package com.xianguoji.server.common.service;

import com.xianguoji.server.module.catalog.dto.ProductQry;
import com.xianguoji.server.module.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * P1-10: 缓存预热服务
 * 应用启动后自动加载热点数据到 Redis 缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmupService {

    private final CatalogService catalogService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        try {
            log.info("[P1-10] 开始缓存预热...");
            long start = System.currentTimeMillis();

            catalogService.getCategoryTree();
            catalogService.getBannerList();
            catalogService.getHotSearchList();
            catalogService.getRecommendProducts();

            ProductQry qry = new ProductQry();
            qry.setPage(1);
            qry.setSize(20);
            catalogService.getProductPage(qry);

            long elapsed = System.currentTimeMillis() - start;
            log.info("[P1-10] 缓存预热完成, elapsed={}ms", elapsed);
        } catch (Exception e) {
            log.warn("[P1-10] 缓存预热失败（不影响启动）: {}", e.getMessage());
        }
    }
}
