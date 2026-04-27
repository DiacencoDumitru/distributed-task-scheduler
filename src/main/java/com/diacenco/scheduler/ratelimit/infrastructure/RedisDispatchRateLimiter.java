package com.diacenco.scheduler.ratelimit.infrastructure;

import com.diacenco.scheduler.ratelimit.config.DispatchRateLimiterProperties;
import com.diacenco.scheduler.ratelimit.domain.DispatchPermit;
import com.diacenco.scheduler.ratelimit.domain.DispatchRateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class RedisDispatchRateLimiter implements DispatchRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> luaScript;
    private final DispatchRateLimiterProperties properties;

    public RedisDispatchRateLimiter(
            StringRedisTemplate redisTemplate,
            RedisScript<List> dispatchRateLimiterLuaScript,
            DispatchRateLimiterProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.luaScript = dispatchRateLimiterLuaScript;
        this.properties = properties;
    }

    @Override
    public DispatchPermit tryAcquire(String limiterKey) {
        String redisKey = "%s:%s".formatted(properties.keyPrefix(), limiterKey);
        List result = redisTemplate.execute(
                luaScript,
                List.of(redisKey),
                String.valueOf(properties.limit()),
                String.valueOf(properties.windowSeconds())
        );

        if (result == null || result.size() < 2) {
            throw new IllegalStateException("Redis script returned invalid rate-limit response");
        }

        long allowedFlag = toLong(result.getFirst());
        long currentHits = toLong(result.get(1));
        boolean allowed = allowedFlag == 1;

        return new DispatchPermit(allowed, currentHits, properties.limit(), properties.windowSeconds());
    }

    private long toLong(Object raw) {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(Objects.toString(raw));
    }
}
