package com.diacenco.scheduler.ratelimit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
@EnableConfigurationProperties(DispatchRateLimiterProperties.class)
public class DispatchRateLimiterConfig {

    @Bean
    RedisScript<List> dispatchRateLimiterLuaScript() {
        String script = """
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
                end
                if current <= tonumber(ARGV[1]) then
                    return {1, current}
                end
                return {0, current}
                """;

        return new DefaultRedisScript<>(script, List.class);
    }
}
