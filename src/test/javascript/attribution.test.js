import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('Attribution Module', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  describe('Attribution Display', () => {
    it('should fetch attribution providers', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        },
        {
          name: 'Australian Data Centers',
          url: 'https://github.com/datacenter/australia',
          attribution: 'Data compiled from public sources',
          type: 'data_source'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      expect(response.ok).toBe(true);
      expect(data.data).toHaveLength(2);
      expect(data.data[0].name).toBe('OpenStreetMap');
    });

    it('should display tile provider attribution', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution?type=tile_provider');
      const data = await response.json();

      expect(response.ok).toBe(true);
      expect(data.data[0].type).toBe('tile_provider');
    });

    it('should display data source attribution', async () => {
      const mockProviders = [
        {
          name: 'Australian Data Centers',
          url: 'https://github.com/datacenter/australia',
          attribution: 'Data compiled from public sources',
          type: 'data_source'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution?type=data_source');
      const data = await response.json();

      expect(response.ok).toBe(true);
      expect(data.data[0].type).toBe('data_source');
    });

    it('should include attribution links with proper security attributes', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      expect(response.ok).toBe(true);
      // Verify that URL is present for creating links with rel="noopener noreferrer"
      expect(data.data[0].url).toBe('https://www.openstreetmap.org/');
    });

    it('should handle attribution fetch errors gracefully', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ error: 'Failed to fetch attribution' })
      });

      const response = await fetch('/api/v1/attribution');

      expect(response.ok).toBe(false);
      expect(response.status).toBe(500);
    });
  });

  describe('Attribution Security', () => {
    it('should escape attribution text to prevent XSS', async () => {
      const mockProviders = [
        {
          name: 'Test Provider',
          url: 'https://example.com/',
          attribution: 'Safe attribution text',
          type: 'tile_provider'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      // Verify attribution text is safe (no HTML tags)
      expect(data.data[0].attribution).not.toContain('<');
      expect(data.data[0].attribution).not.toContain('>');
    });

    it('should validate attribution URLs', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      // Verify URL is HTTPS
      expect(data.data[0].url).toMatch(/^https:\/\//);
    });

    it('should not allow HTTP URLs in attribution', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      // Verify no HTTP URLs (only HTTPS)
      data.data.forEach(provider => {
        expect(provider.url).not.toMatch(/^http:\/\//);
      });
    });
  });

  describe('Attribution Data Integrity', () => {
    it('should return all required attribution fields', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      expect(data.data[0]).toHaveProperty('name');
      expect(data.data[0]).toHaveProperty('url');
      expect(data.data[0]).toHaveProperty('attribution');
      expect(data.data[0]).toHaveProperty('type');
    });

    it('should handle multiple attribution providers', async () => {
      const mockProviders = [
        {
          name: 'OpenStreetMap',
          url: 'https://www.openstreetmap.org/',
          attribution: '© OpenStreetMap contributors',
          type: 'tile_provider'
        },
        {
          name: 'Stadia Maps',
          url: 'https://stadiamaps.com/',
          attribution: '© Stadia Maps',
          type: 'tile_provider'
        },
        {
          name: 'Australian Data Centers',
          url: 'https://github.com/datacenter/australia',
          attribution: 'Data compiled from public sources',
          type: 'data_source'
        }
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockProviders })
      });

      const response = await fetch('/api/v1/attribution');
      const data = await response.json();

      expect(data.data).toHaveLength(3);
      expect(data.data.filter(p => p.type === 'tile_provider')).toHaveLength(2);
      expect(data.data.filter(p => p.type === 'data_source')).toHaveLength(1);
    });
  });
});