package com.distributedtaskscheduler.dispatch.application;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import com.distributedtaskscheduler.ratelimit.domain.DispatchRateLimiter;
import org.springframework.stereotype.Service;

@Service
public class WorkerDispatchService {

    private final DispatchRateLimiter dispatchRateLimiter;

    public WorkerDispatchService(DispatchRateLimiter dispatchRateLimiter) {
        this.dispatchRateLimiter = dispatchRateLimiter;
    }

    public DispatchPermit requestDispatchPermit(String limiterKey) {
        return dispatchRateLimiter.tryAcquire(limiterKey);
    }
}
