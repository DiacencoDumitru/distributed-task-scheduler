package com.distributedtaskscheduler.dispatch.application;

import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import com.distributedtaskscheduler.support.RedisIntegrationTestBase;
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

    @Autowired
    private WorkerDispatchService workerDispatchService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clear() {
        redisTemplate.delete("test:dts:ratelimit:dispatch:global");
    }

    @Test
    void shouldAllowOnlyConfiguredNumberOfDispatchesPerWindow() {
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
    }
}
