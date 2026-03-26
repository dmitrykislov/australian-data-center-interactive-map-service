package com.datacenter.api;

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
 * Integration tests for SearchController.
 * Tests search and autocomplete functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSearch() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("q", "Sydney"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    public void testSearchEmptyQuery() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    public void testSearchWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("q", "data")
                .param("limit", "10")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    public void testSearchNegativeOffset() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("q", "Sydney")
                .param("offset", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                .param("q", "Sydney")
                .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAutocomplete() throws Exception {
        mockMvc.perform(get("/api/v1/search/autocomplete")
                .param("q", "Syd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray());
    }
}