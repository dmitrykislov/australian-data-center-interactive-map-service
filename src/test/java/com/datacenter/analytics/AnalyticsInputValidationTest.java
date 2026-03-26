package com.datacenter.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Input validation tests for analytics endpoints.
 * Verifies that all input parameters are properly validated.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AnalyticsInputValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testMissingPagePath() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("referrer", "http://example.com")
                .param("userAgentType", "desktop"))
                .andExpect(status().isOk());
    }

    @Test
    public void testEmptyPagePath() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "")
                .param("referrer", "http://example.com")
                .param("userAgentType", "desktop"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidPagePathVariations() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/map"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/search/results"))
                .andExpect(status().isOk());
    }

    @Test
    public void testMissingEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("statusCode", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMissingStatusCode() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidStatusCodeFormat() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testStatusCodeTooLow() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testStatusCodeTooHigh() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "600"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidStatusCodeVariations() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "200"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/search")
                .param("statusCode", "404"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "500"))
                .andExpect(status().isOk());
    }
}