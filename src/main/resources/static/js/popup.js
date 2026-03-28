/**
 * Popup content generator and manager for facility information display.
 * Handles creation, rendering, and lifecycle of facility detail popups.
 */

/**
 * Generates HTML content for a facility popup.
 * @param {Object} facility - The facility data object
 * @param {string} facility.id - Unique identifier
 * @param {string} facility.name - Facility name
 * @param {string} facility.operator - Operating company
 * @param {string} facility.address - Street address
 * @param {string} facility.city - City name
 * @param {string} facility.status - Operational status (operational, maintenance, planned, decommissioned)
 * @param {Object} [facility.specifications] - Optional specifications object
 * @param {number} [facility.specifications.power] - Power capacity in MW
 * @param {number} [facility.specifications.cooling] - Cooling capacity in kW
 * @param {number} [facility.specifications.racks] - Number of racks
 * @param {string} [facility.specifications.tier] - Tier classification
 * @returns {string} HTML string for the popup content
 * @throws {Error} If facility is null/undefined or missing required fields
 */
export function generatePopupContent(facility) {
    validateFacility(facility);

    const metadata = facility.metadata || {};
    const statusClass = facility.status.toLowerCase();
    const statusDisplay = facility.status.toLowerCase().charAt(0).toUpperCase() + facility.status.toLowerCase().slice(1);

    const capacity = formatCapacity(facility.capacity);
    const description = facility.description || 'N/A';
    const coordinates = formatCoordinates(facility.coordinates);
    const coordinatesLink = buildGoogleMapsUrl(facility.coordinates);
    const confirmationStatus = formatConfirmationStatus(
        facility.confirmationStatus || metadata.confirmationStatus
    );
    const sourceUrl = sanitizeUrl(metadata.sourceUrl);
    const capacityHelpText = 'Capacity is the total IT power load the site is designed to support, measured in megawatts (MW). For a rough sense of scale: 1 MW equals 1,000 kilowatts, around 5–10 MW is a modest data center, around 20 MW is large, and 50 MW or more is typically hyperscale-scale.';
    const confirmationHelpText = 'Shows whether this data center record has been officially confirmed or is still based on secondary research.';
    const lastVerifiedHelpText = 'The date when this record was last checked against the source to confirm it still looks accurate.';

    let html = `
        <div class="popup-header">
            <h2 class="popup-title" id="popup-title">${escapeHtml(facility.name)}</h2>
            <button class="popup-close-btn" aria-label="Close facility details popup" title="Close (Escape key)">×</button>
        </div>
        <div class="popup-content">
            <div class="popup-section">
                <div class="popup-section-title">Facility Details</div>
                ${buildFieldRow('Operator', facility.operator)}
                ${buildFieldRow('Status', `<span class="popup-status ${statusClass}">${statusDisplay}</span>`, undefined, true)}
                ${buildFieldRow('Capacity', capacity, capacityHelpText)}
                ${buildFieldRow('Description', description)}
                ${buildFieldRow('Confirmation', confirmationStatus, confirmationHelpText)}
                ${buildFieldRow('Address', facility.address || 'N/A')}
                ${buildFieldRow('City', metadata.city || facility.city || 'N/A')}
                ${buildFieldRow('Region', metadata.region || 'N/A')}
                ${buildFieldRow('Coordinates', renderCoordinatesValue(coordinates, coordinatesLink), undefined, true)}
            </div>
            <div class="popup-section">
                <div class="popup-section-title">Data Provenance</div>
                ${buildFieldRow('Source Reference', metadata.sourceReference || 'N/A')}
                ${buildFieldRow('Source URL', sourceUrl ? `<a class="popup-link" href="${escapeHtml(sourceUrl)}" target="_blank" rel="noopener noreferrer">${escapeHtml(sourceUrl)}</a>` : 'N/A', undefined, true)}
                ${buildFieldRow('Last Verified', metadata.lastVerifiedDate || 'N/A', lastVerifiedHelpText)}
                ${buildFieldRow('Comments', metadata.comments || 'N/A')}
            </div>
    `;
    
    // Add specifications if available
    if (facility.specifications && hasSpecifications(facility.specifications)) {
        html += generateSpecificationsSection(facility.specifications);
    }
    
    html += `</div>`;
    
    return html;
}

/**
 * Validates that facility object has all required fields.
 * @param {Object} facility - The facility object to validate
 * @throws {Error} If facility is invalid
 */
function validateFacility(facility) {
    if (!facility) {
        throw new Error('Facility object cannot be null or undefined');
    }
    
    const requiredFields = ['id', 'name', 'operator', 'status'];
    for (const field of requiredFields) {
        if (!facility[field]) {
            throw new Error(`Facility missing required field: ${field}`);
        }
    }
    
    const validStatuses = ['operational', 'maintenance', 'planned', 'decommissioned'];
    if (!validStatuses.includes(facility.status.toLowerCase())) {
        throw new Error(`Invalid facility status: ${facility.status}`);
    }
}

/**
 * Checks if specifications object has any data.
 * @param {Object} specs - Specifications object
 * @returns {boolean} True if any specification field is present and non-null
 */
function hasSpecifications(specs) {
    if (!specs) return false;
    return !!(specs.power || specs.cooling || specs.racks || specs.tier);
}

function buildFieldRow(label, value, helpText, isHtml = false) {
    const renderedValue = isHtml ? value : escapeHtml(value || 'N/A');
    return `
        <div class="popup-field">
            <span class="popup-field-label">${escapeHtml(label)}</span>
            <span class="popup-field-icon-cell">${renderInfoIcon(helpText)}</span>
            <span class="popup-field-value">${renderedValue}</span>
        </div>
    `;
}

function renderInfoIcon(helpText) {
    if (!helpText) {
        return '';
    }
    return `<span class="info-icon-wrap">` +
        `<button type="button" class="info-icon" data-help="${escapeHtml(helpText)}" aria-label="More information">i</button>` +
        `</span>`;
}

/**
 * Returns (creating if needed) a single shared tooltip element that lives
 * directly on <body> so it is never clipped or mis-positioned by the popup's
 * CSS transform.
 * @returns {HTMLElement}
 */
function getOrCreateSharedTooltip() {
    let tooltip = document.getElementById('popup-info-tooltip');
    if (!tooltip) {
        tooltip = document.createElement('span');
        tooltip.id = 'popup-info-tooltip';
        tooltip.className = 'info-tooltip';
        tooltip.setAttribute('role', 'tooltip');
        tooltip.style.display = 'none';
        document.body.appendChild(tooltip);
    }
    return tooltip;
}

/**
 * Positions a tooltip element using fixed coordinates so it is never
 * clipped by the scrollable popup container.
 * @param {HTMLElement} icon
 * @param {HTMLElement} tooltip
 */
function positionTooltip(icon, tooltip) {
    const TOOLTIP_WIDTH = 220;
    const GAP = 8;

    const rect = icon.getBoundingClientRect();

    tooltip.style.position = 'fixed';
    tooltip.style.width = TOOLTIP_WIDTH + 'px';
    tooltip.style.maxWidth = TOOLTIP_WIDTH + 'px';

    // Horizontal: centred on icon, clamped inside viewport
    let left = rect.left + rect.width / 2 - TOOLTIP_WIDTH / 2;
    left = Math.max(GAP, Math.min(left, (window.innerWidth || 800) - TOOLTIP_WIDTH - GAP));
    tooltip.style.left = left + 'px';

    // Vertical: above icon by default, flip below when too close to top
    if (rect.top - 70 > GAP) {
        tooltip.style.top = (rect.top - GAP) + 'px';
        tooltip.style.transform = 'translateY(-100%)';
        tooltip.dataset.placement = 'top';
    } else {
        tooltip.style.top = (rect.bottom + GAP) + 'px';
        tooltip.style.transform = 'none';
        tooltip.dataset.placement = 'bottom';
    }
}

/**
 * Wires hover, focus, click, and keyboard interactions for every info icon
 * inside the given popup element.  All icons share a single tooltip element
 * that is appended to <body> so that the popup's CSS transform does not
 * mis-position the tooltip.
 * @param {HTMLElement} popupEl
 */
function wireInfoTooltips(popupEl) {
    popupEl.querySelectorAll('.info-icon-wrap').forEach((wrap) => {
        const icon = wrap.querySelector('.info-icon');
        if (!icon) return;
        const helpText = icon.dataset.help || '';

        const show = () => {
            const tooltip = getOrCreateSharedTooltip();
            tooltip.textContent = helpText;
            icon.setAttribute('aria-describedby', tooltip.id);
            tooltip.style.display = 'block';
            positionTooltip(icon, tooltip);
            // Close any other open wrap first
            popupEl.querySelectorAll('.info-icon-wrap.tooltip-open').forEach((w) => {
                if (w !== wrap) w.classList.remove('tooltip-open');
            });
            wrap.classList.add('tooltip-open');
        };

        const hide = () => {
            const tooltip = document.getElementById('popup-info-tooltip');
            if (tooltip) tooltip.style.display = 'none';
            icon.removeAttribute('aria-describedby');
            wrap.classList.remove('tooltip-open');
        };

        const closeAll = () => {
            const tooltip = document.getElementById('popup-info-tooltip');
            if (tooltip) tooltip.style.display = 'none';
            popupEl.querySelectorAll('.info-icon-wrap.tooltip-open').forEach((w) => {
                w.classList.remove('tooltip-open');
            });
        };

        // Hover
        icon.addEventListener('mouseenter', show);
        wrap.addEventListener('mouseleave', hide);

        // Keyboard focus/blur
        icon.addEventListener('focusin', show);
        icon.addEventListener('focusout', hide);

        // Click / tap toggle
        icon.addEventListener('click', (e) => {
            e.stopPropagation();
            if (wrap.classList.contains('tooltip-open')) {
                hide();
            } else {
                closeAll();
                show();
            }
        });

        // Enter / Space activate; Escape dismiss
        icon.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                e.stopPropagation();
                if (wrap.classList.contains('tooltip-open')) {
                    hide();
                } else {
                    closeAll();
                    show();
                }
            }
            if (e.key === 'Escape') {
                hide();
            }
        });
    });
}

function formatCapacity(capacity) {
    if (typeof capacity !== 'number' || Number.isNaN(capacity) || capacity <= 0) {
        return 'N/A';
    }
    return `${capacity} MW`;
}

function formatCoordinates(coordinates) {
    if (!coordinates || typeof coordinates.latitude !== 'number' || typeof coordinates.longitude !== 'number') {
        return 'N/A';
    }

    return `${coordinates.latitude.toFixed(4)}, ${coordinates.longitude.toFixed(4)}`;
}

function renderCoordinatesValue(formattedCoordinates, googleMapsUrl) {
    if (!googleMapsUrl || formattedCoordinates === 'N/A') {
        return escapeHtml(formattedCoordinates || 'N/A');
    }

    return `<a class="popup-link" href="${escapeHtml(googleMapsUrl)}" target="_blank" rel="noopener noreferrer" title="Open coordinates in Google Maps">${escapeHtml(formattedCoordinates)}</a>`;
}

function formatConfirmationStatus(status) {
    if (!status) {
        return 'N/A';
    }

    const normalized = status.toLowerCase();
    return normalized.charAt(0).toUpperCase() + normalized.slice(1);
}

function formatTags(tags) {
    if (!tags || typeof tags !== 'string') {
        return 'N/A';
    }

    const tagList = tags
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0);

    if (tagList.length === 0) {
        return 'N/A';
    }

    return `<span class="popup-tags">${tagList.map((tag) => `<span class="popup-tag">${escapeHtml(tag)}</span>`).join('')}</span>`;
}

function sanitizeUrl(url) {
    if (!url || typeof url !== 'string') {
        return '';
    }
    if (/^https?:\/\//i.test(url)) {
        return url;
    }
    return '';
}

function buildGoogleMapsUrl(coordinates) {
    if (!coordinates || typeof coordinates.latitude !== 'number' || typeof coordinates.longitude !== 'number') {
        return '';
    }

    const latitude = coordinates.latitude;
    const longitude = coordinates.longitude;

    if (Number.isNaN(latitude) || Number.isNaN(longitude)) {
        return '';
    }

    return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${latitude},${longitude}`)}`;
}

/**
 * Generates HTML for specifications section.
 * @param {Object} specs - Specifications object
 * @returns {string} HTML string for specifications section
 */
function generateSpecificationsSection(specs) {
    let html = `
        <div class="popup-section">
            <div class="popup-section-title">Specifications</div>
            <div class="popup-specs-grid">
    `;
    
    if (specs.power) {
        html += `
            <div class="popup-spec-item">
                <div class="popup-spec-label">Power</div>
                <div class="popup-spec-value">${specs.power} MW</div>
            </div>
        `;
    }
    
    if (specs.cooling) {
        html += `
            <div class="popup-spec-item">
                <div class="popup-spec-label">Cooling</div>
                <div class="popup-spec-value">${specs.cooling} kW</div>
            </div>
        `;
    }
    
    if (specs.racks) {
        html += `
            <div class="popup-spec-item">
                <div class="popup-spec-label">Racks</div>
                <div class="popup-spec-value">${specs.racks}</div>
            </div>
        `;
    }
    
    if (specs.tier) {
        html += `
            <div class="popup-spec-item">
                <div class="popup-spec-label">Tier</div>
                <div class="popup-spec-value">${escapeHtml(specs.tier)}</div>
            </div>
        `;
    }
    
    html += `
            </div>
        </div>
    `;
    
    return html;
}

/**
 * Escapes HTML special characters to prevent XSS attacks.
 * @param {string} text - Text to escape
 * @returns {string} Escaped text safe for HTML insertion
 */
function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, char => map[char]);
}

/**
 * Creates and displays a popup for a facility.
 * @param {Object} facility - The facility data object
 * @param {HTMLElement} container - Container element for the popup
 * @param {Function} onClose - Callback function when popup is closed
 * @returns {Object} Popup object with methods for interaction
 */
export function createPopup(facility, container, onClose) {
    if (!container) {
        throw new Error('Container element is required');
    }
    
    const startTime = performance.now();
    
    // Create overlay
    const overlay = document.createElement('div');
    overlay.className = 'popup-overlay';
    
    // Create popup element
    const popup = document.createElement('div');
    popup.className = 'popup-visible popup-dialog';
    popup.innerHTML = generatePopupContent(facility);
    
    // Append to container
    container.appendChild(overlay);
    container.appendChild(popup);
    container.classList.remove('popup-hidden');
    container.classList.add('popup-container-visible');

    // Wire info-icon tooltips (hover / focus / click / keyboard)
    wireInfoTooltips(popup);

    // Handle Escape key - define before closeHandler to ensure it's in scope
    const escapeHandler = (e) => {
        if (e.key === 'Escape') {
            closeHandler();
        }
    };
    
    // Setup close handlers
    const closeBtn = popup.querySelector('.popup-close-btn');
    const closeHandler = () => {
        // Remove Escape key listener on all close paths
        document.removeEventListener('keydown', escapeHandler);
        // Hide shared tooltip
        const sharedTooltip = document.getElementById('popup-info-tooltip');
        if (sharedTooltip) sharedTooltip.style.display = 'none';
        closePopup(container, overlay, popup);
        if (onClose) onClose();
    };
    
    closeBtn.addEventListener('click', closeHandler);
    closeBtn.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            closeHandler();
        }
    });
    overlay.addEventListener('click', closeHandler);
    
    // Prevent popup from closing on internal click; also close stray tooltips
    popup.addEventListener('click', (e) => {
        e.stopPropagation();
        if (!e.target.closest('.info-icon-wrap')) {
            const sharedTooltip = document.getElementById('popup-info-tooltip');
            if (sharedTooltip) sharedTooltip.style.display = 'none';
            popup.querySelectorAll('.info-icon-wrap.tooltip-open').forEach((w) => {
                w.classList.remove('tooltip-open');
            });
        }
    });
    
    // Add Escape key listener
    document.addEventListener('keydown', escapeHandler);
    
    // Focus close button for keyboard navigation
    setTimeout(() => {
        closeBtn.focus();
    }, 0);
    
    const endTime = performance.now();
    const renderTime = endTime - startTime;
    
    return {
        element: popup,
        container: container,
        renderTime: renderTime,
        close: closeHandler
    };
}

/**
 * Closes and removes a popup from the DOM.
 * @param {HTMLElement} container - Container element
 * @param {HTMLElement} overlay - Overlay element
 * @param {HTMLElement} popup - Popup element
 */
function closePopup(container, overlay, popup) {
    if (overlay && overlay.parentNode) {
        overlay.parentNode.removeChild(overlay);
    }
    if (popup && popup.parentNode) {
        popup.parentNode.removeChild(popup);
    }
    if (container) {
        container.classList.remove('popup-container-visible');
        container.classList.add('popup-hidden');
    }
}

/**
 * Measures popup render performance.
 * @param {Function} renderFn - Function that renders the popup
 * @returns {Object} Performance metrics
 */
export function measurePopupPerformance(renderFn) {
    const startTime = performance.now();
    renderFn();
    const endTime = performance.now();
    
    return {
        renderTimeMs: endTime - startTime,
        withinSLA: (endTime - startTime) <= 500
    };
}