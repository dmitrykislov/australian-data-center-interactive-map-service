/**
 * Attribution Module
 * Manages display of attribution for tile providers and data sources
 */

const ATTRIBUTION_DATA = {
  tileProviders: [
    {
      name: 'OpenStreetMap',
      url: 'https://www.openstreetmap.org/',
      attribution: '© OpenStreetMap contributors',
    },
    {
      name: 'Stadia Maps',
      url: 'https://stadiamaps.com/',
      attribution: '© Stadia Maps, © OpenMapTiles, © OpenStreetMap contributors',
    },
  ],
  dataSources: [
    {
      name: 'Australian Data Centers',
      url: 'https://github.com/datacenter/australia',
      attribution: 'Data compiled from public sources and industry databases',
    },
  ],
};

/**
 * Initialize attribution data
 * @returns {Object} Attribution data with tile providers and data sources
 */
export function initializeAttribution() {
  return {
    tileProviders: ATTRIBUTION_DATA.tileProviders,
    dataSources: ATTRIBUTION_DATA.dataSources,
  };
}

/**
 * Generate HTML for attribution providers
 * @param {string} type - Type of providers ('tile_provider' or 'data_source')
 * @returns {string} HTML string with attribution links
 */
export function getAttributionHtml(type) {
  const providers = type === 'tile_provider' ? ATTRIBUTION_DATA.tileProviders : ATTRIBUTION_DATA.dataSources;

  return providers
    .map((provider) => {
      const escapedName = escapeHtml(provider.name);
      const escapedAttribution = escapeHtml(provider.attribution);
      const escapedUrl = escapeHtml(provider.url);

      return `<a href="${escapedUrl}" target="_blank" rel="noopener noreferrer">${escapedName}</a>: ${escapedAttribution}`;
    })
    .join(' | ');
}

/**
 * Add attribution control to Leaflet map
 * @param {L.Map} map - Leaflet map instance
 */
export function addAttributionControl(map) {
  if (!map) {
    return;
  }

  try {
    const tileProviderAttribution = getAttributionHtml('tile_provider');
    const dataSourceAttribution = getAttributionHtml('data_source');
    const fullAttribution = `${tileProviderAttribution} | ${dataSourceAttribution}`;

    const control = L.control.attribution({ prefix: false }).setPosition('bottomright');

    control.addAttribution(fullAttribution);
    control.addTo(map);
  } catch (error) {
    console.error('Error adding attribution control:', error);
  }
}

/**
 * Escape HTML special characters to prevent XSS
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
  if (!text) {
    return '';
  }

  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  };

  return text.replace(/[&<>"']/g, (char) => map[char]);
}

/**
 * Display attribution in a modal or sidebar
 * @param {string} containerId - ID of container element
 */
export function displayAttributionModal(containerId) {
  const container = document.getElementById(containerId);
  if (!container) {
    return;
  }

  const html = `
    <div class="attribution-modal">
      <h3>Data Attribution</h3>
      <div class="attribution-section">
        <h4>Tile Providers</h4>
        <p>${getAttributionHtml('tile_provider')}</p>
      </div>
      <div class="attribution-section">
        <h4>Data Sources</h4>
        <p>${getAttributionHtml('data_source')}</p>
      </div>
    </div>
  `;

  container.innerHTML = html;
}