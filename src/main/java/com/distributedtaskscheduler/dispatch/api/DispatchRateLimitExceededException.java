package com.distributedtaskscheduler.dispatch.api;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;

public final class DispatchRateLimitExceededException extends RuntimeException {

    private final DispatchPermit permit;

    public DispatchRateLimitExceededException(DispatchPermit permit) {
        super("Dispatch rate limit exceeded");
        this.permit = permit;
    }

    public DispatchPermit permit() {
        return permit;
    }
}
