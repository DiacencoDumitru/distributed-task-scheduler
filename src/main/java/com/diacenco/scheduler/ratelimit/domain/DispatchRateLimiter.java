package com.diacenco.scheduler.ratelimit.domain;

public interface DispatchRateLimiter {

    DispatchPermit tryAcquire(String limiterKey);
}
