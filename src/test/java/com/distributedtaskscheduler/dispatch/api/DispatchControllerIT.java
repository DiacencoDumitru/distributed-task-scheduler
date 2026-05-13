package com.distributedtaskscheduler.dispatch.api;

import com.distributedtaskscheduler.support.RedisIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "scheduler.dispatch.rate-limit.limit=1",
        "scheduler.dispatch.rate-limit.window-seconds=60",
        "scheduler.dispatch.rate-limit.key-prefix=test:dts:ratelimit:dispatch"
})
class DispatchControllerIT extends RedisIntegrationTestBase {
    private static final String GLOBAL_KEY = "test:dts:ratelimit:dispatch:global";
    private static final String WORKER_A_KEY = "test:dts:ratelimit:dispatch:worker-a";
    private static final String WORKER_B_KEY = "test:dts:ratelimit:dispatch:worker-b";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clear() {
        redisTemplate.delete(GLOBAL_KEY);
        redisTemplate.delete(WORKER_A_KEY);
        redisTemplate.delete(WORKER_B_KEY);
    }

    @Test
    void shouldReturnTooManyRequestsWhenDispatchLimitExceeded() throws Exception {
        mockMvc.perform(post("/api/v1/dispatch/permit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.currentHits").value(1))
                .andExpect(jsonPath("$.limit").value(1));

        mockMvc.perform(post("/api/v1/dispatch/permit"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.title").value("Too many dispatch requests"))
                .andExpect(jsonPath("$.detail").value("Dispatch rate limit exceeded"));
    }

    @Test
    void shouldApplyRateLimitIndependentlyForEachWorker() throws Exception {
        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Worker-Id", "worker-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.currentHits").value(1))
                .andExpect(jsonPath("$.limit").value(1));

        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Worker-Id", "worker-b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.currentHits").value(1))
                .andExpect(jsonPath("$.limit").value(1));

        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Worker-Id", "worker-a"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"));
    }

    @Test
    void shouldReturnBadRequestForInvalidWorkerId() throws Exception {
        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Worker-Id", "worker with spaces"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid worker id"))
                .andExpect(jsonPath("$.detail").value("Worker id must match ^[A-Za-z0-9_-]{1,64}$"));
    }
}
