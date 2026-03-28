/**
 * Statistics overlay for the interactive data center map.
 *
 * Public API
 * ──────────
 *  computeStats(dataCenters)              → plain stats object (pure, testable)
 *  createStatsOverlay(dataCenters)        → builds DOM, appends to body, returns element
 *  updateStatsOverlay(overlay, dataCenters) → refreshes content, preserves collapsed state
 */

/** Ordered list of status keys the overlay knows how to render. */
const STATUS_CONFIG = [
    { key: 'operational',    label: 'Operational',    color: '#28a745' },
    { key: 'planned',        label: 'Planned',        color: '#17a2b8' },
    { key: 'maintenance',    label: 'Maintenance',    color: '#ffc107' },
    { key: 'decommissioned', label: 'Decommissioned', color: '#6c757d' },
];

// ── Public: pure computation ───────────────────────────────────────────────

/**
 * Computes summary statistics from an array of data center objects.
 *
 * @param {Array} dataCenters - Raw array from the API response
 * @returns {{
 *   total: number,
 *   byStatus: Object.<string, number>,
 *   distinctOperators: number,
 *   byRegion: Object.<string, number>
 * }}
 */
export function computeStats(dataCenters) {
    if (!Array.isArray(dataCenters) || dataCenters.length === 0) {
        return { total: 0, byStatus: {}, distinctOperators: 0, byRegion: {} };
    }

    const byStatus = {};
    const operators = new Set();
    const byRegion = {};

    for (const dc of dataCenters) {
        // Status — normalise to lowercase
        const status = (dc.status || 'unknown').toLowerCase();
        byStatus[status] = (byStatus[status] || 0) + 1;

        // Operators — deduplicated by name
        if (dc.operator) {
            operators.add(dc.operator);
        }

        // Region — prefer metadata.region, fall back to top-level or 'Other'
        const region =
            (dc.metadata && dc.metadata.region) ||
            dc.region ||
            'Other';
        byRegion[region] = (byRegion[region] || 0) + 1;
    }

    return {
        total: dataCenters.length,
        byStatus,
        distinctOperators: operators.size,
        byRegion,
    };
}

// ── Public: DOM management ─────────────────────────────────────────────────

/**
 * Builds the stats overlay, appends it to document.body and returns the element.
 *
 * @param {Array} dataCenters
 * @returns {HTMLElement}
 */
export function createStatsOverlay(dataCenters) {
    const overlay = document.createElement('div');
    overlay.id = 'stats-overlay';
    overlay.className = 'stats-overlay';
    overlay.setAttribute('role', 'complementary');
    overlay.setAttribute('aria-label', 'Data center statistics');

    overlay.innerHTML = buildOverlayHtml(computeStats(dataCenters));
    wireToggle(overlay);

    document.body.appendChild(overlay);
    return overlay;
}

/**
 * Refreshes the overlay content with updated data, preserving collapsed state.
 *
 * @param {HTMLElement|null} overlay
 * @param {Array} dataCenters
 */
export function updateStatsOverlay(overlay, dataCenters) {
    if (!overlay) return;

    const wasCollapsed = overlay
        .querySelector('.stats-body')
        ?.classList.contains('stats-body--collapsed') ?? false;

    overlay.innerHTML = buildOverlayHtml(computeStats(dataCenters));

    if (wasCollapsed) {
        overlay.querySelector('.stats-body').classList.add('stats-body--collapsed');
        const btn = overlay.querySelector('.stats-toggle');
        btn.textContent = '+';
        btn.setAttribute('aria-expanded', 'false');
    }

    wireToggle(overlay);
}

// ── Private helpers ────────────────────────────────────────────────────────

function wireToggle(overlay) {
    const btn = overlay.querySelector('.stats-toggle');
    const body = overlay.querySelector('.stats-body');
    if (!btn || !body) return;

    btn.addEventListener('click', () => {
        const nowCollapsed = body.classList.toggle('stats-body--collapsed');
        btn.textContent = nowCollapsed ? '+' : '−';
        btn.setAttribute('aria-expanded', String(!nowCollapsed));
    });
}

function buildOverlayHtml(stats) {
    return `
        <div class="stats-header">
            <span class="stats-title">📊 Summary</span>
            <button class="stats-toggle"
                    aria-label="Toggle statistics panel"
                    aria-expanded="true">−</button>
        </div>
        <div class="stats-body">
            <div class="stats-row stats-row--total">
                <span class="stats-label">Total facilities</span>
                <span class="stats-value">${stats.total}</span>
            </div>
            <div class="stats-divider"></div>
            ${buildStatusRows(stats.byStatus)}
            <div class="stats-divider"></div>
            <div class="stats-row">
                <span class="stats-label">Operators</span>
                <span class="stats-value">${stats.distinctOperators}</span>
            </div>
            <div class="stats-divider"></div>
            <div class="stats-section-title">By State / Territory</div>
            ${buildRegionRows(stats.byRegion)}
        </div>
    `;
}

function buildStatusRows(byStatus) {
    const rows = STATUS_CONFIG.filter(({ key }) => byStatus[key]);
    if (rows.length === 0) return '<div class="stats-row"><span class="stats-label stats-label--muted">No data</span></div>';
    return rows.map(({ key, label, color }) => `
        <div class="stats-row">
            <span class="stats-dot" style="background:${color}" aria-hidden="true"></span>
            <span class="stats-label">${label}</span>
            <span class="stats-value">${byStatus[key]}</span>
        </div>
    `).join('');
}

function buildRegionRows(byRegion) {
    const entries = Object.entries(byRegion)
        .sort(([a, ca], [b, cb]) => cb - ca || a.localeCompare(b));
    if (entries.length === 0) return '<div class="stats-row"><span class="stats-label stats-label--muted">No data</span></div>';
    return entries.map(([region, count]) => `
        <div class="stats-row">
            <span class="stats-label">${region}</span>
            <span class="stats-value">${count}</span>
        </div>
    `).join('');
}

