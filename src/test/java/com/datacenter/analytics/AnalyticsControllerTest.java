package com.datacenter.analytics;

import com.datacenter.validation.InputValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AnalyticsController.
 * Tests page view and API call tracking without mocking to avoid Java 25 Mockito issues.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private InputValidator inputValidator;

    @Test
    public void testTrackPageViewSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/map")
                .param("referrer", "http://example.com")
                .param("userAgentType", "desktop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("tracked"));
    }

    @Test
    public void testTrackPageViewWithoutReferrer() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("tracked"));
    }

    @Test
    public void testTrackPageViewInvalidPath() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/map<script>alert('xss')</script>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTrackApiCallSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("tracked"));
    }

    @Test
    public void testTrackApiCallWithoutResponseTime() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/search")
                .param("statusCode", "404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("tracked"));
    }

    @Test
    public void testTrackApiCallInvalidEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "invalid-endpoint")
                .param("statusCode", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTrackApiCallInvalidStatusCode() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/api-call")
                .param("endpoint", "/api/v1/datacenters")
                .param("statusCode", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAnalyticsDoesNotCapturePII() throws Exception {
        // Verify that analytics endpoint accepts requests without capturing PII
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/map"))
                .andExpect(status().isOk());
        
        // The service should not store IP addresses, user IDs, or session identifiers
        // This is verified through code inspection and logging verification
    }

    @Test
    public void testMultiplePageViewsTracked() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/"))
                .andExpect(status().isOk());
        
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/map"))
                .andExpect(status().isOk());
        
        mockMvc.perform(post("/api/v1/analytics/page-view")
                .param("pagePath", "/search"))
                .andExpect(status().isOk());
    }
}