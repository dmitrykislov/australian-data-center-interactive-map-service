package com.datacenter.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for security headers configuration.
 * Verifies that OWASP-recommended security headers are properly set.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHSTSHeaderPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    @Test
    public void testXFrameOptionsHeaderPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    public void testXFrameOptionsValueDeny() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("X-Frame-Options", "DENY"));
    }

    @Test
    public void testXContentTypeOptionsHeaderPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"));
    }

    @Test
    public void testXXSSProtectionHeaderPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-XSS-Protection"));
    }

    @Test
    public void testReferrerPolicyHeaderPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    public void testPermissionsPolicyHeaderPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Permissions-Policy"));
    }
}