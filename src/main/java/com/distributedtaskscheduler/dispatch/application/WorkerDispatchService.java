package com.distributedtaskscheduler.dispatch.application;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import com.distributedtaskscheduler.ratelimit.domain.DispatchRateLimiter;
import org.springframework.stereotype.Service;

@Service
public class WorkerDispatchService {

    private static final String GLOBAL_DISPATCH_LIMITER_KEY = "global";

    private final DispatchRateLimiter dispatchRateLimiter;

    public WorkerDispatchService(DispatchRateLimiter dispatchRateLimiter) {
        this.dispatchRateLimiter = dispatchRateLimiter;
    }

    public DispatchPermit requestDispatchPermit() {
        return dispatchRateLimiter.tryAcquire(GLOBAL_DISPATCH_LIMITER_KEY);
    }
}
