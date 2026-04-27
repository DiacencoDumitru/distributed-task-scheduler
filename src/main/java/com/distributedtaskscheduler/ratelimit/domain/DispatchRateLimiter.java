package com.distributedtaskscheduler.ratelimit.domain;

public interface DispatchRateLimiter {

    DispatchPermit tryAcquire(String limiterKey);
}
