/**
 * Leaflet map initialization and marker management for data center mapping.
 * Handles map setup, marker clustering, and popup display on marker click.
 */

import { createPopup } from './popup.js';
import { initializeMarkerKeyboardNavigation } from './marker-keyboard-navigation.js';
import { initializeAttribution, validateAttributions } from './attribution.js';
import { trackPageView, trackApiCall } from './analytics.js';

const MAP_CONFIG = {
    center: [20, 0],
    zoom: 2,
    minZoom: 1,
    maxZoom: 18
};

const MARKER_COLORS = {
    operational: '#28a745',
    maintenance: '#ffc107',
    planned: '#17a2b8',
    decommissioned: '#6c757d'
};

let map;
let markerClusterGroup;
let dataCenters = [];
let markers = {};

/**
 * Initializes the Leaflet map and loads data centers.
 */
export async function initializeMap() {
    try {
        // Initialize map
        map = L.map('map').setView(MAP_CONFIG.center, MAP_CONFIG.zoom);
        
        // Add tile layer
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: MAP_CONFIG.maxZoom,
            minZoom: MAP_CONFIG.minZoom
        }).addTo(map);
        
        // Initialize marker cluster group
        markerClusterGroup = L.markerClusterGroup({
            maxClusterRadius: 80,
            disableClusteringAtZoom: 15
        });
        map.addLayer(markerClusterGroup);
        
        // Load data centers
        await loadDataCenters();
        
        // Add markers to map
        addMarkersToMap();
        
        // Fit bounds if data centers exist
        if (dataCenters.length > 0) {
            fitMapBounds();
        }
        
        // Initialize marker keyboard navigation
        initializeMarkerKeyboardNavigation(map, markers, dataCenters, showFacilityPopup);
        
        // Initialize attribution
        initializeAttribution(map);
        
        // Track page view
        trackPageView('/');
        
        // Validate attribution is present
        if (!validateAttributions()) {
            console.warn('Attribution validation failed - some sources may not be properly credited');
        }
        
        console.log(`Map initialized with ${dataCenters.length} data centers`);
    } catch (error) {
        console.error('Failed to initialize map:', error);
        showErrorMessage('Failed to initialize map. Please refresh the page.');
    }
}

/**
 * Loads data centers from the backend API.
 */
async function loadDataCenters() {
    try {
        const response = await fetch('/api/v1/datacenters');
        trackApiCall('/api/v1/datacenters', response.status);
        if (!response.ok) {
            throw new Error(`API returned status ${response.status}`);
        }
        const data = await response.json();
        dataCenters = data.dataCenters || [];
    } catch (error) {
        console.error('Failed to load data centers:', error);
        dataCenters = [];
    }
}

/**
 * Adds markers for all data centers to the map.
 */
function addMarkersToMap() {
    dataCenters.forEach(dc => {
        if (!dc.coordinates || dc.coordinates.latitude === undefined || dc.coordinates.longitude === undefined) {
            console.warn(`Data center ${dc.id} has invalid coordinates`);
            return;
        }
        
        const marker = createMarker(dc);
        markers[dc.id] = marker;
        markerClusterGroup.addLayer(marker);
    });
}

/**
 * Creates a marker for a data center.
 * @param {Object} dc - Data center object
 * @returns {L.Marker} Leaflet marker
 */
function createMarker(dc) {
    const { latitude, longitude } = dc.coordinates;
    const status = dc.status.toLowerCase();
    const openFacilityPopup = () => {
        showFacilityPopup(dc);
    };

    // Create marker with valid Leaflet options.
    // Keyboard navigation is implemented via marker-keyboard-navigation.js
    const marker = L.marker([latitude, longitude], {
        title: dc.name,
        alt: `${dc.name} - ${status}`,
        className: `marker-${status}`,
        keyboard: true
    });
    
    // Add activation handlers for desktop and touch interactions
    marker.on('click', openFacilityPopup);
    marker.on('touchend', openFacilityPopup);

    // Add hover tooltip
    marker.bindTooltip(dc.name, {
        permanent: false,
        direction: 'top',
        offset: [0, -10]
    });
    
    return marker;
}

/**
 * Displays facility information popup on marker click.
 * Measures performance to ensure 500ms SLA.
 * @param {Object} facility - Data center object
 */
function showFacilityPopup(facility) {
    const clickStartTime = performance.now();
    
    const container = document.getElementById('popup-container');
    
    // Clear any existing popup
    container.innerHTML = '';
    
    try {
        createPopup(facility, container, () => {
            console.log('Popup closed');
        });
        
        const clickEndTime = performance.now();
        const responseTime = clickEndTime - clickStartTime;
        
        // Log performance metrics
        console.log(`Popup displayed in ${responseTime.toFixed(2)}ms`);
        
        if (responseTime > 500) {
            console.warn(`Popup display exceeded 500ms SLA: ${responseTime.toFixed(2)}ms`);
        }
        
        // Update performance indicator
        updatePerformanceIndicator(responseTime);
    } catch (error) {
        console.error('Failed to display popup:', error);
        showErrorMessage('Failed to display facility information.');
    }
}

/**
 * Fits map bounds to show all markers.
 */
function fitMapBounds() {
    try {
        const group = new L.featureGroup(Object.values(markers));
        if (group.getLayers().length > 0) {
            map.fitBounds(group.getBounds(), { padding: [50, 50] });
        }
    } catch (error) {
        console.error('Failed to fit map bounds:', error);
    }
}

/**
 * Shows an error message to the user.
 * @param {string} message - Error message to display
 */
function showErrorMessage(message) {
    const container = document.getElementById('popup-container');
    container.innerHTML = `
        <div class="popup-overlay"></div>
        <div class="popup-visible popup-dialog" style="text-align: center; padding: 40px;">
            <h3 style="color: #d32f2f; margin-bottom: 10px;">Error</h3>
            <p style="color: #666;">${escapeHtml(message)}</p>
            <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Reload Page
            </button>
        </div>
    `;
    container.classList.remove('popup-hidden');
    container.classList.add('popup-container-visible');
}

/**
 * Updates performance indicator display.
 * @param {number} responseTime - Response time in milliseconds
 */
function updatePerformanceIndicator(responseTime) {
    let indicator = document.getElementById('perf-indicator');
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'perf-indicator';
        indicator.className = 'perf-indicator';
        document.body.appendChild(indicator);
    }
    
    const status = responseTime <= 500 ? '✓' : '⚠';
    const color = responseTime <= 500 ? '#4caf50' : '#ff9800';
    
    indicator.textContent = `${status} ${responseTime.toFixed(0)}ms`;
    indicator.style.backgroundColor = `rgba(0, 0, 0, 0.7)`;
    indicator.style.color = color;
    indicator.classList.add('visible');
    
    // Hide after 3 seconds
    setTimeout(() => {
        indicator.classList.remove('visible');
    }, 3000);
}

/**
 * Escapes HTML special characters.
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, char => map[char]);
}

// Initialize map when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeMap);
} else {
    initializeMap();
}