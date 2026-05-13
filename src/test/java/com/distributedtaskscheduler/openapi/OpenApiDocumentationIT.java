package com.distributedtaskscheduler.openapi;

import com.distributedtaskscheduler.support.RedisIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIT extends RedisIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeOpenApiDocumentWithDispatchPermitPath() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/dispatch/permit']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/dispatch/permit'].post").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/dispatch/permit'].post.parameters[?(@.name == 'X-Dispatch-Scope')]"
                ).exists());
    }
}
