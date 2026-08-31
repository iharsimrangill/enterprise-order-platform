package com.portfolio.orders.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orders.domain.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisOrderCache {

    private static final String PREFIX = "order:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisOrderCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.cache.order-ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public Optional<Order> get(UUID orderId) {
        String payload = redisTemplate.opsForValue().get(key(orderId));

        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(payload, Order.class));
        } catch (JsonProcessingException exception) {
            redisTemplate.delete(key(orderId));
            return Optional.empty();
        }
    }

    public void put(Order order) {
        try {
            redisTemplate.opsForValue().set(
                    key(order.id()),
                    objectMapper.writeValueAsString(order),
                    ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize order for Redis cache", exception);
        }
    }

    public void evict(UUID orderId) {
        redisTemplate.delete(key(orderId));
    }

    private String key(UUID orderId) {
        return PREFIX + orderId;
    }
}
