/**
 * Marker keyboard navigation implementation for WCAG 2.1 AA compliance.
 * Enables Tab key cycling through markers, Enter/Space activation, and visible focus indicators.
 */

let currentMarkerIndex = -1;
let markerArray = [];
let markerMap = {};
let escapeHandler = null;

/**
 * Initializes keyboard navigation for markers.
 * @param {L.Map} map - Leaflet map instance
 * @param {Object} markers - Object mapping data center IDs to marker instances
 * @param {Array} dataCenters - Array of data center objects
 * @param {Function} showPopupCallback - Callback to show facility popup
 */
export function initializeMarkerKeyboardNavigation(map, markers, dataCenters, showPopupCallback) {
    // Build marker array in order of data centers
    markerArray = dataCenters
        .map(dc => markers[dc.id])
        .filter(marker => marker !== undefined);

    // Create reverse lookup map
    markerMap = { ...markers };

    // Add keyboard event listener to map container
    const mapContainer = map.getContainer();
    mapContainer.setAttribute('tabindex', '0');
    mapContainer.setAttribute('role', 'application');
    mapContainer.setAttribute('aria-label', 'Interactive data center map. Use Tab to navigate markers, Enter or Space to view details.');

    // Handle keyboard events
    mapContainer.addEventListener('keydown', (e) => {
        handleMarkerKeyboardEvent(e, map, showPopupCallback);
    });

    // Handle Escape key to deselect marker
    escapeHandler = (e) => {
        if (e.key === 'Escape') {
            clearMarkerFocus();
        }
    };

    document.addEventListener('keydown', escapeHandler);
}

/**
 * Handles keyboard events for marker navigation.
 * @param {KeyboardEvent} event - The keyboard event
 * @param {L.Map} map - Leaflet map instance
 * @param {Function} showPopupCallback - Callback to show facility popup
 */
function handleMarkerKeyboardEvent(event, map, showPopupCallback) {
    if (markerArray.length === 0) {
        return;
    }

    switch (event.key) {
        case 'Tab':
            event.preventDefault();
            navigateToNextMarker(event.shiftKey);
            break;
        case 'Enter':
        case ' ':
            event.preventDefault();
            if (currentMarkerIndex >= 0 && currentMarkerIndex < markerArray.length) {
                activateCurrentMarker(showPopupCallback);
            }
            break;
        case 'ArrowRight':
        case 'ArrowDown':
            event.preventDefault();
            navigateToNextMarker(false);
            break;
        case 'ArrowLeft':
        case 'ArrowUp':
            event.preventDefault();
            navigateToNextMarker(true);
            break;
    }
}

/**
 * Navigates to the next or previous marker.
 * @param {boolean} reverse - If true, navigate backwards; otherwise forward
 */
function navigateToNextMarker(reverse = false) {
    if (markerArray.length === 0) {
        return;
    }

    // Clear previous focus
    if (currentMarkerIndex >= 0 && currentMarkerIndex < markerArray.length) {
        const previousMarker = markerArray[currentMarkerIndex];
        removeFocusFromMarker(previousMarker);
    }

    // Calculate next index
    if (currentMarkerIndex === -1) {
        currentMarkerIndex = reverse ? markerArray.length - 1 : 0;
    } else {
        if (reverse) {
            currentMarkerIndex = (currentMarkerIndex - 1 + markerArray.length) % markerArray.length;
        } else {
            currentMarkerIndex = (currentMarkerIndex + 1) % markerArray.length;
        }
    }

    // Apply focus to new marker
    const currentMarker = markerArray[currentMarkerIndex];
    applyFocusToMarker(currentMarker);
}

/**
 * Applies focus styling to a marker.
 * @param {L.Marker} marker - The marker to focus
 */
function applyFocusToMarker(marker) {
    if (!marker) {
        return;
    }

    const markerElement = marker.getElement();
    if (markerElement) {
        markerElement.classList.add('marker-focused');
        markerElement.setAttribute('tabindex', '0');
        markerElement.setAttribute('role', 'button');
        markerElement.setAttribute('aria-pressed', 'false');
        markerElement.focus();

        // Announce to screen readers
        announceMarkerFocus(marker);

        // Pan map to show marker
        const map = marker._map;
        if (map) {
            map.panTo(marker.getLatLng(), { animate: true });
        }
    }
}

/**
 * Removes focus styling from a marker.
 * @param {L.Marker} marker - The marker to unfocus
 */
function removeFocusFromMarker(marker) {
    if (!marker) {
        return;
    }

    const markerElement = marker.getElement();
    if (markerElement) {
        markerElement.classList.remove('marker-focused');
        markerElement.setAttribute('aria-pressed', 'false');
    }
}

/**
 * Clears all marker focus.
 */
function clearMarkerFocus() {
    if (currentMarkerIndex >= 0 && currentMarkerIndex < markerArray.length) {
        removeFocusFromMarker(markerArray[currentMarkerIndex]);
    }
    currentMarkerIndex = -1;
}

/**
 * Activates the current marker (opens popup).
 * @param {Function} showPopupCallback - Callback to show facility popup
 */
function activateCurrentMarker(showPopupCallback) {
    if (currentMarkerIndex >= 0 && currentMarkerIndex < markerArray.length) {
        const marker = markerArray[currentMarkerIndex];
        const markerElement = marker.getElement();
        if (markerElement) {
            markerElement.setAttribute('aria-pressed', 'true');
        }

        // Trigger marker click to show popup
        marker.fire('click');
    }
}

/**
 * Announces marker focus to screen readers.
 * @param {L.Marker} marker - The focused marker
 */
function announceMarkerFocus(marker) {
    // Create or update live region for screen reader announcements
    let liveRegion = document.getElementById('marker-focus-announcement');
    if (!liveRegion) {
        liveRegion = document.createElement('div');
        liveRegion.id = 'marker-focus-announcement';
        liveRegion.className = 'sr-only';
        liveRegion.setAttribute('role', 'status');
        liveRegion.setAttribute('aria-live', 'polite');
        document.body.appendChild(liveRegion);
    }

    const markerTitle = marker.options.title || 'Data center marker';
    const markerIndex = currentMarkerIndex + 1;
    const totalMarkers = markerArray.length;

    liveRegion.textContent = `${markerTitle}, marker ${markerIndex} of ${totalMarkers}. Press Enter or Space to view details.`;
}

/**
 * Cleans up keyboard navigation on map destruction.
 */
export function cleanupMarkerKeyboardNavigation() {
    if (escapeHandler) {
        document.removeEventListener('keydown', escapeHandler);
    }
    clearMarkerFocus();
    markerArray = [];
    markerMap = {};
    currentMarkerIndex = -1;
}