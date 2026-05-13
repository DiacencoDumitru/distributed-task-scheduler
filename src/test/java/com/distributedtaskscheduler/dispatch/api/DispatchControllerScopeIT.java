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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "scheduler.dispatch.rate-limit.limit=1",
        "scheduler.dispatch.rate-limit.window-seconds=60",
        "scheduler.dispatch.rate-limit.key-prefix=test:dts:ratelimit:dispatch"
})
class DispatchControllerScopeIT extends RedisIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clear() {
        redisTemplate.delete("test:dts:ratelimit:dispatch:global");
        redisTemplate.delete("test:dts:ratelimit:dispatch:tenant-a");
        redisTemplate.delete("test:dts:ratelimit:dispatch:tenant-b");
    }

    @Test
    void shouldRateLimitEachScopeIndependently() throws Exception {
        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Dispatch-Scope", "tenant-a"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Dispatch-Scope", "tenant-a"))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Dispatch-Scope", "tenant-b"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidDispatchScope() throws Exception {
        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Dispatch-Scope", "bad scope"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/dispatch/permit").header("X-Dispatch-Scope", "bad!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDefaultToGlobalScopeWhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/v1/dispatch/permit"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/dispatch/permit"))
                .andExpect(status().isTooManyRequests());
    }
}
