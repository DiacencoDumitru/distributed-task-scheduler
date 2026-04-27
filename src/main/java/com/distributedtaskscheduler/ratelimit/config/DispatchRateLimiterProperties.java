package com.distributedtaskscheduler.ratelimit.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "scheduler.dispatch.rate-limit")
public record DispatchRateLimiterProperties(
        @Min(1) long limit,
        @Min(1) long windowSeconds,
        String keyPrefix
) {

    public DispatchRateLimiterProperties {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            keyPrefix = "dts:ratelimit:dispatch";
        }
    }
}
