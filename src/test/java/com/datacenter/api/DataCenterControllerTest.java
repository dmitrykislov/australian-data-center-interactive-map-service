package com.datacenter.api;

import com.datacenter.search.SearchService;
import com.datacenter.validation.InputValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for DataCenterController.
 * Tests REST API endpoints for data center queries.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DataCenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchService searchService;

    @Autowired
    private InputValidator inputValidator;

    @Test
    public void testGetAllDataCenters() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataCenters").isArray());
    }

    @Test
    public void testGetAllDataCentersInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters")
                .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDataCenterById() throws Exception {
        // Test with a valid UUID format that doesn't exist
        mockMvc.perform(get("/api/v1/datacenters/550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDataCenterByInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDataCenterNotFound() throws Exception {
        // Test with a valid UUID that doesn't exist
        mockMvc.perform(get("/api/v1/datacenters/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFilterByOperator() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters/filter/operator")
                .param("operator", "Equinix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataCenters").isArray());
    }

    @Test
    public void testFilterByOperatorInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters/filter/operator")
                .param("operator", "<script>alert('xss')</script>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters/filter/status")
                .param("status", "operational"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataCenters").isArray());
    }

    @Test
    public void testFilterByStatusInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/datacenters/filter/status")
                .param("status", "invalid_status"))
                .andExpect(status().isBadRequest());
    }
}