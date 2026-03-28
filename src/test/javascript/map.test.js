import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../../main/resources/static/js/analytics.js', () => ({
  trackPageView: vi.fn(),
  trackApiCall: vi.fn(),
}));

vi.mock('../../main/resources/static/js/attribution.js', () => ({
  initializeAttribution: vi.fn(),
  validateAttributions: vi.fn(() => true),
}));

vi.mock('../../main/resources/static/js/marker-keyboard-navigation.js', () => ({
  initializeMarkerKeyboardNavigation: vi.fn(),
}));

vi.mock('../../main/resources/static/js/stats-overlay.js', () => ({
  createStatsOverlay: vi.fn(),
}));

function setupLeafletMock(createdMarkers) {
  const mapContainer = document.getElementById('map');
  const mapInstance = {
    setView: vi.fn().mockReturnThis(),
    addLayer: vi.fn(),
    fitBounds: vi.fn(),
    getContainer: vi.fn(() => mapContainer),
  };

  global.L = {
    map: vi.fn(() => mapInstance),
    tileLayer: vi.fn(() => ({ addTo: vi.fn() })),
    markerClusterGroup: vi.fn(() => ({ addLayer: vi.fn() })),
    marker: vi.fn((coords, options) => {
      const handlers = {};
      const marker = {
        options,
        _map: mapInstance,
        on: vi.fn((eventName, handler) => {
          handlers[eventName] = handler;
          return marker;
        }),
        fire: (eventName) => {
          if (handlers[eventName]) {
            handlers[eventName]({ type: eventName });
          }
        },
        bindTooltip: vi.fn().mockReturnThis(),
        getElement: vi.fn(() => document.createElement('div')),
        getLatLng: vi.fn(() => ({ lat: coords[0], lng: coords[1] })),
      };
      createdMarkers.push(marker);
      return marker;
    }),
    featureGroup: vi.fn((markers) => ({
      getLayers: vi.fn(() => markers),
      getBounds: vi.fn(() => ({ northEast: {}, southWest: {} })),
    })),
  };
}

describe('Map marker interactions', () => {
  beforeEach(() => {
    vi.resetModules();
    vi.clearAllMocks();

    document.body.innerHTML = `
      <div id="map"></div>
      <div id="popup-container" class="popup-hidden"></div>
    `;

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        dataCenters: [
          {
            id: '550e8400-e29b-41d4-a716-446655440001',
            name: 'Sydney Test DC',
            operator: 'Equinix',
            status: 'operational',
            coordinates: { latitude: -33.86, longitude: 151.21 },
          },
        ],
      }),
    });

    Object.defineProperty(document, 'readyState', {
      configurable: true,
      get: () => 'loading',
    });
  });

  it('opens facility popup when a marker click event is fired', async () => {
    const createdMarkers = [];
    setupLeafletMock(createdMarkers);

    const mapModule = await import('../../main/resources/static/js/map.js');
    await mapModule.initializeMap();

    expect(createdMarkers).toHaveLength(1);

    createdMarkers[0].fire('click');

    expect(document.querySelector('.popup-title')?.textContent).toContain('Sydney Test DC');
  });

  it('opens facility popup when a marker touchend event is fired', async () => {
    const createdMarkers = [];
    setupLeafletMock(createdMarkers);

    const mapModule = await import('../../main/resources/static/js/map.js');
    await mapModule.initializeMap();

    expect(createdMarkers).toHaveLength(1);

    createdMarkers[0].fire('touchend');

    expect(document.querySelector('.popup-title')?.textContent).toContain('Sydney Test DC');
  });
});
