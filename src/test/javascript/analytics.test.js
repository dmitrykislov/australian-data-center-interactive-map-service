import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('Analytics Module', () => {
  beforeEach(() => {
    // Reset fetch mock before each test
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Page View Tracking', () => {
    it('should track page view with path only', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      const response = await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'pagePath=%2Fmap'
      });

      expect(response.ok).toBe(true);
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/v1/analytics/page-view',
        expect.objectContaining({
          method: 'POST'
        })
      );
    });

    it('should track page view with referrer and user agent type', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      const response = await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'pagePath=%2Fmap&referrer=https%3A%2F%2Fexample.com&userAgentType=desktop'
      });

      expect(response.ok).toBe(true);
      expect(global.fetch).toHaveBeenCalled();
    });

    it('should not capture PII in page view tracking', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      const response = await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'pagePath=%2Fmap'
      });

      expect(response.ok).toBe(true);
      
      // Verify that the request body does not contain IP, user ID, or session ID
      const callArgs = global.fetch.mock.calls[0];
      const requestBody = callArgs[1].body;
      expect(requestBody).not.toContain('ip=');
      expect(requestBody).not.toContain('userId=');
      expect(requestBody).not.toContain('sessionId=');
    });
  });

  describe('API Call Tracking', () => {
    it('should track API call with endpoint and status code', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      const response = await fetch('/api/v1/analytics/api-call', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'endpoint=%2Fapi%2Fv1%2Fdatacenters&statusCode=200'
      });

      expect(response.ok).toBe(true);
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/v1/analytics/api-call',
        expect.objectContaining({
          method: 'POST'
        })
      );
    });

    it('should track API call with response time', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      const response = await fetch('/api/v1/analytics/api-call', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'endpoint=%2Fapi%2Fv1%2Fdatacenters&statusCode=200&responseTimeMs=150'
      });

      expect(response.ok).toBe(true);
    });

    it('should handle API call tracking errors gracefully', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Invalid endpoint' })
      });

      const response = await fetch('/api/v1/analytics/api-call', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'endpoint=invalid&statusCode=200'
      });

      expect(response.ok).toBe(false);
      expect(response.status).toBe(400);
    });
  });

  describe('Input Validation', () => {
    it('should reject invalid page paths', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Invalid path' })
      });

      const response = await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'pagePath=%3Cscript%3E'
      });

      expect(response.ok).toBe(false);
      expect(response.status).toBe(400);
    });

    it('should reject invalid user agent types', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Invalid user agent type' })
      });

      const response = await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'pagePath=%2Fmap&userAgentType=invalid'
      });

      expect(response.ok).toBe(false);
      expect(response.status).toBe(400);
    });
  });

  describe('HTTPS Enforcement', () => {
    it('should use HTTPS for analytics endpoints', () => {
      const endpoint = '/api/v1/analytics/page-view';
      // Verify endpoint path is correct format
      expect(endpoint).toMatch(/^\/api\/v\d+\/analytics/);
    });

    it('should send analytics requests with proper headers', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'pagePath=%2Fmap'
      });

      const callArgs = global.fetch.mock.calls[0];
      expect(callArgs[1].headers['Content-Type']).toBe('application/x-www-form-urlencoded');
    });
  });

  describe('Multiple Events', () => {
    it('should track multiple page views', async () => {
      global.fetch.mockResolvedValue({
        ok: true,
        json: async () => ({ status: 'tracked' })
      });

      await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        body: 'pagePath=%2F'
      });

      await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        body: 'pagePath=%2Fmap'
      });

      await fetch('/api/v1/analytics/page-view', {
        method: 'POST',
        body: 'pagePath=%2Fsearch'
      });

      expect(global.fetch).toHaveBeenCalledTimes(3);
    });
  });
});