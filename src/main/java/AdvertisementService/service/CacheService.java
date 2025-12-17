package AdvertisementService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String POPULAR = "popular_ads";
    private static final long TTL_MINUTES = 5;

    public void save(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // увеличиваем «популярность» объявления
    public void viewed(UUID id) {
        redisTemplate.opsForZSet().incrementScore(POPULAR, id.toString(), 1);
    }

    // top N самых популярных id
    public Set<Object> popular(int topN) {
        return redisTemplate.opsForZSet().reverseRange(POPULAR, 0, topN - 1);
    }
}
