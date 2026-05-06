package com.xianguoji.server.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@EnableCaching
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        om.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(om, Object.class);

        RedisCacheConfiguration defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(SerializationPair.fromSerializer(StringRedisSerializer.UTF_8))
                .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer))
                .computePrefixWith(name -> "xgj:cache:" + name + ":");

        // 空值缓存：防穿透，60s TTL
        RedisCacheConfiguration nullValueCfg = defaultCfg
                .entryTtl(Duration.ofSeconds(60));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("shop", withJitter(defaultCfg, Duration.ofHours(2)));
        perCache.put("category", withJitter(defaultCfg, Duration.ofHours(6)));
        perCache.put("product", withJitter(defaultCfg, Duration.ofMinutes(10)));
        perCache.put("banner", withJitter(defaultCfg, Duration.ofMinutes(30)));
        perCache.put("hotSearch", withJitter(defaultCfg, Duration.ofHours(1)));
        perCache.put("recommend", withJitter(defaultCfg, Duration.ofMinutes(5)));
        // 空值缓存区域
        perCache.put("nullValues", nullValueCfg);

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    /**
     * 为 TTL 添加 ±60s 抖动，防止缓存雪崩
     */
    private RedisCacheConfiguration withJitter(RedisCacheConfiguration base, Duration ttl) {
        long jitterSeconds = ThreadLocalRandom.current().nextLong(-60, 60);
        return base.entryTtl(ttl.plusSeconds(jitterSeconds));
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheSpecification("maximumSize=500,expireAfterWrite=30s");
        return manager;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                try {
                    cache.evict(key);
                } catch (RuntimeException ignored) {
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                throw exception;
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                throw exception;
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                throw exception;
            }
        };
    }
}
