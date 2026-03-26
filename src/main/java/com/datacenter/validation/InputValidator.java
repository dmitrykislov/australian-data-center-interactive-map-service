package com.datacenter.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Input validator for preventing injection attacks and validating user input.
 * Implements OWASP input validation best practices.
 */
@Component
public class InputValidator {

    // Regex patterns for validation
    private static final Pattern PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9/_-]*$");
    private static final Pattern REFERRER_PATTERN = Pattern.compile("^[a-zA-Z0-9:/?#\\[\\]@!$&'()*+,;=._~-]*$");
    private static final Pattern USER_AGENT_TYPE_PATTERN = Pattern.compile("^(desktop|mobile|tablet|unknown)$");
    private static final Pattern ENDPOINT_PATTERN = Pattern.compile("^/api/v[0-9]+/[a-zA-Z0-9/_-]*$");
    private static final Pattern STATUS_CODE_PATTERN = Pattern.compile("^[1-5][0-9]{2}$");
    private static final Pattern SEARCH_QUERY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._-]{0,255}$");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._-]{0,255}$");
    private static final Pattern REGION_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._-]{0,255}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$", Pattern.CASE_INSENSITIVE);

    /**
     * Validates analytics page path.
     * @param pagePath the page path to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateAnalyticsPath(String pagePath) {
        if (pagePath == null || pagePath.isEmpty()) {
            throw new IllegalArgumentException("Page path cannot be empty");
        }
        if (pagePath.length() > 255) {
            throw new IllegalArgumentException("Page path is too long (max 255 characters)");
        }
        if (!PATH_PATTERN.matcher(pagePath).matches()) {
            throw new IllegalArgumentException("Page path contains invalid characters");
        }
    }

    /**
     * Validates analytics referrer.
     * @param referrer the referrer to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateAnalyticsReferrer(String referrer) {
        if (referrer == null || referrer.isEmpty()) {
            return; // Referrer is optional
        }
        if (referrer.length() > 2048) {
            throw new IllegalArgumentException("Referrer is too long (max 2048 characters)");
        }
        if (!REFERRER_PATTERN.matcher(referrer).matches()) {
            throw new IllegalArgumentException("Referrer contains invalid characters");
        }
    }

    /**
     * Validates analytics user agent type.
     * @param userAgentType the user agent type to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateAnalyticsUserAgentType(String userAgentType) {
        if (userAgentType != null && !userAgentType.isEmpty() && !USER_AGENT_TYPE_PATTERN.matcher(userAgentType).matches()) {
            throw new IllegalArgumentException("User agent type must be one of: desktop, mobile, tablet, unknown");
        }
    }

    /**
     * Validates analytics API endpoint.
     * @param endpoint the endpoint to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateAnalyticsEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("Endpoint cannot be empty");
        }
        if (!ENDPOINT_PATTERN.matcher(endpoint).matches()) {
            throw new IllegalArgumentException("Endpoint contains invalid characters or format");
        }
    }

    /**
     * Validates HTTP status code.
     * @param statusCode the status code to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateStatusCode(int statusCode) {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("Status code must be between 100 and 599");
        }
    }

    /**
     * Validates search query string.
     * @param query the search query to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateSearchQuery(String query) {
        if (query == null) {
            throw new IllegalArgumentException("Search query cannot be null");
        }
        // Empty queries are explicitly allowed - return early
        if (query.trim().isEmpty()) {
            return;
        }
        if (query.length() > 255) {
            throw new IllegalArgumentException("Search query too long (max 255 characters)");
        }
        if (!SEARCH_QUERY_PATTERN.matcher(query).matches()) {
            throw new IllegalArgumentException("Search query contains invalid characters");
        }
    }

    /**
     * Validates operator name.
     * @param operator the operator name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateOperator(String operator) {
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be empty");
        }
        if (operator.length() > 255) {
            throw new IllegalArgumentException("Operator name too long (max 255 characters)");
        }
        if (!OPERATOR_PATTERN.matcher(operator).matches()) {
            throw new IllegalArgumentException("Operator name contains invalid characters");
        }
    }

    /**
     * Validates region name.
     * @param region the region name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region cannot be empty");
        }
        if (region.length() > 255) {
            throw new IllegalArgumentException("Region name too long (max 255 characters)");
        }
        if (!REGION_PATTERN.matcher(region).matches()) {
            throw new IllegalArgumentException("Region name contains invalid characters");
        }
    }

    /**
     * Validates limit parameter for pagination.
     * @param limit the limit value to validate
     * @param maxLimit the maximum allowed limit
     * @throws IllegalArgumentException if validation fails
     */
    public void validateLimit(int limit, int maxLimit) {
        if (limit < 1 || limit > maxLimit) {
            throw new IllegalArgumentException("Limit must be between 1 and " + maxLimit);
        }
    }

    /**
     * Validates offset parameter for pagination.
     * @param offset the offset value to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateOffset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
    }

    /**
     * Validates UUID format.
     * @param uuid the UUID string to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            throw new IllegalArgumentException("UUID cannot be empty");
        }
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            throw new IllegalArgumentException("Invalid UUID format");
        }
    }

    /**
     * Validates data center status.
     * @param status the status to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        String normalizedStatus = status.toLowerCase();
        if (!normalizedStatus.equals("operational") && !normalizedStatus.equals("planned")) {
            throw new IllegalArgumentException("Status must be 'operational' or 'planned'");
        }
    }

    /**
     * Checks if input contains SQL injection patterns.
     * @param input the input to check
     * @return true if SQL injection detected
     */
    public boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        String lower = input.toLowerCase();
        return lower.contains("select ") || lower.contains("insert ") || 
               lower.contains("update ") || lower.contains("delete ") ||
               lower.contains("drop ") || lower.contains("union ") ||
               lower.contains("exec ") || lower.contains("execute ");
    }

    /**
     * Checks if input contains XSS patterns.
     * @param input the input to check
     * @return true if XSS detected
     */
    public boolean containsXss(String input) {
        if (input == null) {
            return false;
        }
        String lower = input.toLowerCase();
        return lower.contains("<script") || lower.contains("javascript:") ||
               lower.contains("onerror=") || lower.contains("onload=") ||
               lower.contains("<iframe") || lower.contains("<object");
    }

    /**
     * Checks if input contains path traversal patterns.
     * @param input the input to check
     * @return true if path traversal detected
     */
    public boolean containsPathTraversal(String input) {
        if (input == null) {
            return false;
        }
        return input.contains("../") || input.contains("..\\") ||
               input.contains("%2e%2e") || input.contains("%252e%252e");
    }

    /**
     * Checks if input contains command injection patterns.
     * @param input the input to check
     * @return true if command injection detected
     */
    public boolean containsCommandInjection(String input) {
        if (input == null) {
            return false;
        }
        return input.contains(";") || input.contains("|") ||
               input.contains("&") || input.contains("`") ||
               input.contains("$") || input.contains("(") ||
               input.contains(")");
    }

    /**
     * Checks if input contains LDAP injection patterns.
     * @param input the input to check
     * @return true if LDAP injection detected
     */
    public boolean containsLdapInjection(String input) {
        if (input == null) {
            return false;
        }
        return input.contains("*") || input.contains("(") ||
               input.contains(")") || input.contains("\\") ||
               input.contains("/");
    }
}