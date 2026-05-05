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

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("shop", defaultCfg.entryTtl(Duration.ofHours(2)));
        perCache.put("category", defaultCfg.entryTtl(Duration.ofHours(6)));
        perCache.put("product", defaultCfg.entryTtl(Duration.ofMinutes(10)));
        perCache.put("banner", defaultCfg.entryTtl(Duration.ofMinutes(30)));
        perCache.put("hotSearch", defaultCfg.entryTtl(Duration.ofHours(1)));
        perCache.put("recommend", defaultCfg.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(perCache)
                .build();
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
