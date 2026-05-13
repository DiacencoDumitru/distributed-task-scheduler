package com.distributedtaskscheduler.dispatch.application;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import com.distributedtaskscheduler.support.RedisIntegrationTestBase;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "scheduler.dispatch.rate-limit.limit=3",
        "scheduler.dispatch.rate-limit.window-seconds=60",
        "scheduler.dispatch.rate-limit.key-prefix=test:dts:ratelimit:dispatch"
})
class WorkerDispatchServiceIT extends RedisIntegrationTestBase {
    private static final String DISPATCH_PERMITS_METRIC = "dispatch.permits";

    @Autowired
    private WorkerDispatchService workerDispatchService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void clear() {
        redisTemplate.delete("test:dts:ratelimit:dispatch:global");
    }

    @Test
    void shouldAllowOnlyConfiguredNumberOfDispatchesPerWindow() {
        double grantedBefore = counterValue("granted");
        double throttledBefore = counterValue("throttled");

        DispatchPermit first = workerDispatchService.requestDispatchPermit();
        DispatchPermit second = workerDispatchService.requestDispatchPermit();
        DispatchPermit third = workerDispatchService.requestDispatchPermit();
        DispatchPermit fourth = workerDispatchService.requestDispatchPermit();

        assertThat(first.allowed()).isTrue();
        assertThat(second.allowed()).isTrue();
        assertThat(third.allowed()).isTrue();
        assertThat(fourth.allowed()).isFalse();
        assertThat(fourth.currentHits()).isEqualTo(4);
        assertThat(fourth.limit()).isEqualTo(3);
        assertThat(counterValue("granted") - grantedBefore).isEqualTo(3.0);
        assertThat(counterValue("throttled") - throttledBefore).isEqualTo(1.0);
    }

    private double counterValue(String result) {
        var counter = meterRegistry.find(DISPATCH_PERMITS_METRIC)
                .tag("result", result)
                .counter();
        return counter == null ? 0.0 : counter.count();
    }
}
