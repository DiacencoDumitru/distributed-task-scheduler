package com.distributedtaskscheduler.dispatch.application;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import com.distributedtaskscheduler.ratelimit.domain.DispatchRateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class WorkerDispatchService {

    private static final String GLOBAL_DISPATCH_LIMITER_KEY = "global";
    private static final String DISPATCH_PERMITS_METRIC = "dispatch.permits";
    private static final Pattern WORKER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final DispatchRateLimiter dispatchRateLimiter;
    private final MeterRegistry meterRegistry;

    public WorkerDispatchService(DispatchRateLimiter dispatchRateLimiter, MeterRegistry meterRegistry) {
        this.dispatchRateLimiter = dispatchRateLimiter;
        this.meterRegistry = meterRegistry;
    }

    public DispatchPermit requestDispatchPermit() {
        return requestDispatchPermit(null);
    }

    public DispatchPermit requestDispatchPermit(String workerId) {
        String limiterKey = normalizeWorkerId(workerId);
        DispatchPermit permit = dispatchRateLimiter.tryAcquire(limiterKey);
        meterRegistry.counter(
                DISPATCH_PERMITS_METRIC,
                "result",
                permit.allowed() ? "granted" : "throttled"
        ).increment();
        return permit;
    }

    private String normalizeWorkerId(String workerId) {
        if (!StringUtils.hasText(workerId)) {
            return GLOBAL_DISPATCH_LIMITER_KEY;
        }
        if (!WORKER_ID_PATTERN.matcher(workerId).matches()) {
            throw new InvalidWorkerIdException();
        }
        return workerId;
    }
}
