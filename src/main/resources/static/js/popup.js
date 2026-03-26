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
    
    const statusClass = facility.status.toLowerCase();
    const statusDisplay = facility.status.toLowerCase().charAt(0).toUpperCase() + facility.status.toLowerCase().slice(1);
    
    let html = `
        <div class="popup-header">
            <h2 class="popup-title" id="popup-title">${escapeHtml(facility.name)}</h2>
            <button class="popup-close-btn" aria-label="Close facility details popup" title="Close (Escape key)">×</button>
        </div>
        <div class="popup-content">
            <div class="popup-section">
                <div class="popup-field">
                    <span class="popup-field-label">Operator</span>
                    <span class="popup-field-value">${escapeHtml(facility.operator)}</span>
                </div>
                <div class="popup-field">
                    <span class="popup-field-label">Address</span>
                    <span class="popup-field-value">${escapeHtml(facility.address || 'N/A')}</span>
                </div>
                <div class="popup-field">
                    <span class="popup-field-label">City</span>
                    <span class="popup-field-value">${escapeHtml(facility.city || 'N/A')}</span>
                </div>
                <div class="popup-field">
                    <span class="popup-field-label">Status</span>
                    <span class="popup-status ${statusClass}">${statusDisplay}</span>
                </div>
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
    popup.className = 'popup-visible';
    popup.innerHTML = generatePopupContent(facility);
    
    // Append to container
    container.appendChild(overlay);
    container.appendChild(popup);
    container.classList.remove('popup-hidden');
    container.classList.add('popup-visible');
    
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
    
    // Prevent closing when clicking inside popup
    popup.addEventListener('click', (e) => {
        e.stopPropagation();
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
        container.classList.remove('popup-visible');
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