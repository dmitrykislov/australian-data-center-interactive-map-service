package com.datacenter.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests for Content Security Policy (CSP) header configuration.
 * Verifies that CSP headers are properly set by SecurityHeadersFilter.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigCspTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testContentSecurityPolicyHeaderPresent() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    public void testCSPContainsDefaultSrc() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }

    @Test
    public void testCSPContainsScriptSrc() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("script-src")));
    }

    @Test
    public void testCSPContainsStyleSrc() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("style-src")));
    }

    @Test
    public void testCSPContainsFrameAncestors() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")));
    }

    @Test
    public void testCSPContainsFormAction() throws Exception {
        mockMvc.perform(get("/").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("form-action 'self'")));
    }
}