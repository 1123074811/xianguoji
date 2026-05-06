package com.xianguoji.server.common.cache;

import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesRankService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductMapper productMapper;

    private static final String KEY_PREFIX = "xgj:rank:sales:";
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 记录商品销量增量（下单时调用）
     */
    public void incrementSales(Long productId, int quantity) {
        String key = KEY_PREFIX + LocalDate.now().format(F);
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(productId), quantity);
        // 当日 key 48h 过期
        stringRedisTemplate.expire(key, Duration.ofHours(48));
    }

    /**
     * 获取当日销量 Top N
     */
    public List<RankItem> getDailyTop(int topN) {
        String key = KEY_PREFIX + LocalDate.now().format(F);
        return getTop(key, topN);
    }

    /**
     * 获取指定日期销量 Top N
     */
    public List<RankItem> getTopByDate(LocalDate date, int topN) {
        String key = KEY_PREFIX + date.format(F);
        return getTop(key, topN);
    }

    /**
     * 获取当日所有商品的销量增量（用于实时合并 DB sales 展示）
     */
    public Map<Long, Integer> getTodaySalesDelta() {
        String key = KEY_PREFIX + LocalDate.now().format(F);
        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        Map<Long, Integer> delta = new HashMap<>();
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                if (t.getValue() != null && t.getScore() != null) {
                    delta.put(Long.valueOf(t.getValue()), t.getScore().intValue());
                }
            }
        }
        return delta;
    }

    /**
     * 每日凌晨将昨日 ZSet 数据归档到 DB（product.sales 字段）
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void archiveDailySales() {
        String yesterday = LocalDate.now().minusDays(1).format(F);
        String key = KEY_PREFIX + yesterday;
        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        if (tuples == null || tuples.isEmpty()) {
            return;
        }
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            String productId = t.getValue();
            Double score = t.getScore();
            if (productId != null && score != null && score > 0) {
                // 累加到 product.sales
                productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.xianguoji.server.module.catalog.entity.Product>()
                        .eq(com.xianguoji.server.module.catalog.entity.Product::getId, Long.valueOf(productId))
                        .setSql("sales = sales + " + score.intValue()));
            }
        }
        log.info("[SalesRank] 归档 {} 销量数据, 共 {} 条", yesterday, tuples.size());
    }

    private List<RankItem> getTop(String key, int topN) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);
        List<RankItem> list = new ArrayList<>();
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                if (t.getValue() != null && t.getScore() != null) {
                    list.add(new RankItem(Long.valueOf(t.getValue()), t.getScore().intValue()));
                }
            }
        }
        return list;
    }

    public record RankItem(Long productId, int sales) {}
}
