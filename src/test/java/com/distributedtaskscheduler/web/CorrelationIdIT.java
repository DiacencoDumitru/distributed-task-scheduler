package com.distributedtaskscheduler.web;

import com.distributedtaskscheduler.support.RedisIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorrelationIdIT extends RedisIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldEchoProvidedCorrelationId() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "req-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "req-123"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, matchesPattern(
                        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
                )));
    }
}
