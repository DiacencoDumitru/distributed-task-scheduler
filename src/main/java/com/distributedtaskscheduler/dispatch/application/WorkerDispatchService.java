package com.distributedtaskscheduler.dispatch.application;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import com.distributedtaskscheduler.ratelimit.domain.DispatchRateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class WorkerDispatchService {

    private static final String GLOBAL_DISPATCH_LIMITER_KEY = "global";
    private static final String DISPATCH_PERMITS_METRIC = "dispatch.permits";

    private final DispatchRateLimiter dispatchRateLimiter;
    private final MeterRegistry meterRegistry;

    public WorkerDispatchService(DispatchRateLimiter dispatchRateLimiter, MeterRegistry meterRegistry) {
        this.dispatchRateLimiter = dispatchRateLimiter;
        this.meterRegistry = meterRegistry;
    }

    public DispatchPermit requestDispatchPermit() {
        DispatchPermit permit = dispatchRateLimiter.tryAcquire(GLOBAL_DISPATCH_LIMITER_KEY);
        meterRegistry.counter(
                DISPATCH_PERMITS_METRIC,
                "result",
                permit.allowed() ? "granted" : "throttled"
        ).increment();
        return permit;
    }
}
