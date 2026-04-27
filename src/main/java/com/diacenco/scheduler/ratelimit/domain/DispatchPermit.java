package com.diacenco.scheduler.ratelimit.domain;

public record DispatchPermit(boolean allowed, long currentHits, long limit, long windowSeconds) {
}
