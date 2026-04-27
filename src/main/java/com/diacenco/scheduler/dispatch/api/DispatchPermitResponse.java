package com.diacenco.scheduler.dispatch.api;

import com.diacenco.scheduler.ratelimit.domain.DispatchPermit;

public record DispatchPermitResponse(
        boolean allowed,
        long currentHits,
        long limit,
        long windowSeconds
) {
    public static DispatchPermitResponse from(DispatchPermit permit) {
        return new DispatchPermitResponse(
                permit.allowed(),
                permit.currentHits(),
                permit.limit(),
                permit.windowSeconds()
        );
    }
}
