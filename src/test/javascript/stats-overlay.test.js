/**
 * Unit tests for the stats overlay module.
 * Tests pure computation logic and DOM rendering separately.
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import {
    computeStats,
    createStatsOverlay,
    updateStatsOverlay,
} from '../../main/resources/static/js/stats-overlay.js';

// ── Shared fixtures ────────────────────────────────────────────────────────

function makeDC(overrides = {}) {
    return {
        id: 'dc-' + Math.random(),
        operator: 'TestCorp',
        status: 'operational',
        metadata: { region: 'NSW' },
        ...overrides,
    };
}

const SAMPLE_DATA = [
    makeDC({ operator: 'NextDC',  status: 'operational', metadata: { region: 'NSW' } }),
    makeDC({ operator: 'NextDC',  status: 'operational', metadata: { region: 'NSW' } }),
    makeDC({ operator: 'Equinix', status: 'operational', metadata: { region: 'VIC' } }),
    makeDC({ operator: 'Equinix', status: 'planned',     metadata: { region: 'QLD' } }),
    makeDC({ operator: 'AWS',     status: 'operational', metadata: { region: 'QLD' } }),
];

// ── computeStats ───────────────────────────────────────────────────────────

describe('computeStats', () => {
    describe('empty / invalid input', () => {
        it('returns zero total for empty array', () => {
            expect(computeStats([]).total).toBe(0);
        });

        it('returns empty byStatus for empty array', () => {
            expect(computeStats([])).toMatchObject({ byStatus: {}, byRegion: {} });
        });

        it('returns zero operators for empty array', () => {
            expect(computeStats([]).distinctOperators).toBe(0);
        });

        it('handles null gracefully', () => {
            expect(computeStats(null).total).toBe(0);
        });

        it('handles undefined gracefully', () => {
            expect(computeStats(undefined).total).toBe(0);
        });

        it('handles non-array gracefully', () => {
            expect(computeStats('bad').total).toBe(0);
        });
    });

    describe('totals', () => {
        it('counts all data centers', () => {
            expect(computeStats(SAMPLE_DATA).total).toBe(5);
        });

        it('counts a single data center', () => {
            expect(computeStats([makeDC()]).total).toBe(1);
        });
    });

    describe('status breakdown', () => {
        it('groups operational and planned separately', () => {
            const stats = computeStats(SAMPLE_DATA);
            expect(stats.byStatus.operational).toBe(4);
            expect(stats.byStatus.planned).toBe(1);
        });

        it('normalises status to lowercase', () => {
            const stats = computeStats([makeDC({ status: 'OPERATIONAL' })]);
            expect(stats.byStatus['operational']).toBe(1);
        });

        it('handles maintenance status', () => {
            const stats = computeStats([makeDC({ status: 'maintenance' })]);
            expect(stats.byStatus.maintenance).toBe(1);
        });

        it('handles decommissioned status', () => {
            const stats = computeStats([makeDC({ status: 'decommissioned' })]);
            expect(stats.byStatus.decommissioned).toBe(1);
        });

        it('handles unknown status without crashing', () => {
            const stats = computeStats([makeDC({ status: undefined })]);
            expect(stats.byStatus['unknown']).toBe(1);
        });

        it('sums across all four statuses correctly', () => {
            const mixed = [
                makeDC({ status: 'operational' }),
                makeDC({ status: 'planned' }),
                makeDC({ status: 'maintenance' }),
                makeDC({ status: 'decommissioned' }),
            ];
            const stats = computeStats(mixed);
            const sum = Object.values(stats.byStatus).reduce((a, b) => a + b, 0);
            expect(sum).toBe(4);
        });
    });

    describe('distinct operators', () => {
        it('deduplicates the same operator name', () => {
            const dcs = [
                makeDC({ operator: 'Equinix' }),
                makeDC({ operator: 'Equinix' }),
                makeDC({ operator: 'Equinix' }),
            ];
            expect(computeStats(dcs).distinctOperators).toBe(1);
        });

        it('counts all unique operators', () => {
            expect(computeStats(SAMPLE_DATA).distinctOperators).toBe(3); // NextDC, Equinix, AWS
        });

        it('ignores entries with no operator field', () => {
            const dcs = [makeDC({ operator: undefined }), makeDC({ operator: null })];
            expect(computeStats(dcs).distinctOperators).toBe(0);
        });

        it('treats operator names case-sensitively', () => {
            const dcs = [makeDC({ operator: 'equinix' }), makeDC({ operator: 'Equinix' })];
            expect(computeStats(dcs).distinctOperators).toBe(2);
        });
    });

    describe('region breakdown', () => {
        it('groups by metadata.region', () => {
            const stats = computeStats(SAMPLE_DATA);
            expect(stats.byRegion['NSW']).toBe(2);
            expect(stats.byRegion['VIC']).toBe(1);
            expect(stats.byRegion['QLD']).toBe(2);
        });

        it('uses "Other" for entries with no region', () => {
            const stats = computeStats([makeDC({ metadata: {} })]);
            expect(stats.byRegion['Other']).toBe(1);
        });

        it('uses "Other" for entries with no metadata', () => {
            const dc = { id: '1', operator: 'X', status: 'operational' };
            expect(computeStats([dc]).byRegion['Other']).toBe(1);
        });

        it('accumulates counts for the same region', () => {
            const dcs = Array.from({ length: 5 }, () => makeDC({ metadata: { region: 'NSW' } }));
            expect(computeStats(dcs).byRegion['NSW']).toBe(5);
        });

        it('handles all 8 Australian states', () => {
            const regions = ['NSW', 'VIC', 'QLD', 'WA', 'SA', 'TAS', 'ACT', 'NT'];
            const dcs = regions.map(r => makeDC({ metadata: { region: r } }));
            const stats = computeStats(dcs);
            regions.forEach(r => expect(stats.byRegion[r]).toBe(1));
        });
    });
});

// ── createStatsOverlay ─────────────────────────────────────────────────────

describe('createStatsOverlay', () => {
    function cleanup() {
        document.getElementById('stats-overlay')?.remove();
    }

    beforeEach(cleanup);
    afterEach(cleanup);

    it('appends the overlay to document.body', () => {
        createStatsOverlay(SAMPLE_DATA);
        expect(document.getElementById('stats-overlay')).not.toBeNull();
    });

    it('overlay has role="complementary"', () => {
        createStatsOverlay(SAMPLE_DATA);
        expect(document.getElementById('stats-overlay').getAttribute('role')).toBe('complementary');
    });

    it('overlay has an aria-label', () => {
        createStatsOverlay(SAMPLE_DATA);
        const label = document.getElementById('stats-overlay').getAttribute('aria-label');
        expect(label).toBeTruthy();
        expect(label.length).toBeGreaterThan(0);
    });

    it('shows the correct total facility count', () => {
        createStatsOverlay(SAMPLE_DATA);
        const overlay = document.getElementById('stats-overlay');
        const totalRow = overlay.querySelector('.stats-row--total');
        expect(totalRow.querySelector('.stats-value').textContent).toBe('5');
    });

    it('shows the Operational row with count 4', () => {
        createStatsOverlay(SAMPLE_DATA);
        const overlay = document.getElementById('stats-overlay');
        expect(overlay.textContent).toContain('Operational');
        // value next to Operational label
        const rows = [...overlay.querySelectorAll('.stats-row')];
        const opRow = rows.find(r => r.textContent.includes('Operational'));
        expect(opRow.querySelector('.stats-value').textContent).toBe('4');
    });

    it('shows the Planned row with count 1', () => {
        createStatsOverlay(SAMPLE_DATA);
        const overlay = document.getElementById('stats-overlay');
        const rows = [...overlay.querySelectorAll('.stats-row')];
        const row = rows.find(r => r.textContent.includes('Planned'));
        expect(row.querySelector('.stats-value').textContent).toBe('1');
    });

    it('shows the correct distinct operator count', () => {
        createStatsOverlay(SAMPLE_DATA);
        const overlay = document.getElementById('stats-overlay');
        const rows = [...overlay.querySelectorAll('.stats-row')];
        const row = rows.find(r => r.textContent.includes('Operators'));
        expect(row.querySelector('.stats-value').textContent).toBe('3');
    });

    it('shows region breakdown labels', () => {
        createStatsOverlay(SAMPLE_DATA);
        const overlay = document.getElementById('stats-overlay');
        expect(overlay.textContent).toContain('NSW');
        expect(overlay.textContent).toContain('VIC');
        expect(overlay.textContent).toContain('QLD');
    });

    it('shows "By State / Territory" section title', () => {
        createStatsOverlay(SAMPLE_DATA);
        expect(document.getElementById('stats-overlay').textContent).toContain('By State');
    });

    it('has a status dot element for each status that has data', () => {
        createStatsOverlay(SAMPLE_DATA);
        const dots = document.querySelectorAll('#stats-overlay .stats-dot');
        // SAMPLE_DATA has operational + planned → 2 dots
        expect(dots.length).toBe(2);
    });

    it('renders status dots with inline background colors', () => {
        createStatsOverlay(SAMPLE_DATA);
        const dots = [...document.querySelectorAll('#stats-overlay .stats-dot')];
        dots.forEach(dot => {
            expect(dot.getAttribute('style')).toContain('background');
        });
    });

    describe('toggle behaviour', () => {
        it('has a toggle button with aria-expanded="true" by default', () => {
            createStatsOverlay(SAMPLE_DATA);
            const btn = document.querySelector('#stats-overlay .stats-toggle');
            expect(btn).not.toBeNull();
            expect(btn.getAttribute('aria-expanded')).toBe('true');
        });

        it('shows "−" when expanded', () => {
            createStatsOverlay(SAMPLE_DATA);
            expect(document.querySelector('.stats-toggle').textContent).toBe('−');
        });

        it('collapses body on first toggle click', () => {
            createStatsOverlay(SAMPLE_DATA);
            document.querySelector('.stats-toggle').click();
            expect(document.querySelector('.stats-body').classList.contains('stats-body--collapsed')).toBe(true);
        });

        it('shows "+" when collapsed', () => {
            createStatsOverlay(SAMPLE_DATA);
            document.querySelector('.stats-toggle').click();
            expect(document.querySelector('.stats-toggle').textContent).toBe('+');
        });

        it('sets aria-expanded="false" when collapsed', () => {
            createStatsOverlay(SAMPLE_DATA);
            document.querySelector('.stats-toggle').click();
            expect(document.querySelector('.stats-toggle').getAttribute('aria-expanded')).toBe('false');
        });

        it('re-expands on second toggle click', () => {
            createStatsOverlay(SAMPLE_DATA);
            const btn = document.querySelector('.stats-toggle');
            btn.click();
            btn.click();
            expect(document.querySelector('.stats-body').classList.contains('stats-body--collapsed')).toBe(false);
            expect(btn.textContent).toBe('−');
        });

        it('toggle button has aria-label', () => {
            createStatsOverlay(SAMPLE_DATA);
            const btn = document.querySelector('.stats-toggle');
            expect(btn.getAttribute('aria-label')).toBeTruthy();
        });
    });

    it('shows "No data" gracefully for empty array', () => {
        createStatsOverlay([]);
        const overlay = document.getElementById('stats-overlay');
        expect(overlay.textContent).toContain('0');
    });
});

// ── updateStatsOverlay ─────────────────────────────────────────────────────

describe('updateStatsOverlay', () => {
    function cleanup() {
        document.getElementById('stats-overlay')?.remove();
    }

    beforeEach(cleanup);
    afterEach(cleanup);

    it('updates the total when data changes', () => {
        const overlay = createStatsOverlay(SAMPLE_DATA);
        const extended = [...SAMPLE_DATA, makeDC({ operator: 'Google', metadata: { region: 'SA' } })];
        updateStatsOverlay(overlay, extended);
        expect(overlay.querySelector('.stats-row--total .stats-value').textContent).toBe('6');
    });

    it('updates operator count when data changes', () => {
        const overlay = createStatsOverlay(SAMPLE_DATA);
        const extended = [...SAMPLE_DATA, makeDC({ operator: 'Google', metadata: { region: 'SA' } })];
        updateStatsOverlay(overlay, extended);
        const rows = [...overlay.querySelectorAll('.stats-row')];
        const opRow = rows.find(r => r.textContent.includes('Operators'));
        expect(opRow.querySelector('.stats-value').textContent).toBe('4');
    });

    it('preserves collapsed state across updates', () => {
        const overlay = createStatsOverlay(SAMPLE_DATA);
        overlay.querySelector('.stats-toggle').click(); // collapse
        updateStatsOverlay(overlay, SAMPLE_DATA);
        expect(overlay.querySelector('.stats-body').classList.contains('stats-body--collapsed')).toBe(true);
        expect(overlay.querySelector('.stats-toggle').textContent).toBe('+');
    });

    it('keeps expanded state across updates when not collapsed', () => {
        const overlay = createStatsOverlay(SAMPLE_DATA);
        updateStatsOverlay(overlay, SAMPLE_DATA);
        expect(overlay.querySelector('.stats-body').classList.contains('stats-body--collapsed')).toBe(false);
    });

    it('re-wires the toggle after update', () => {
        const overlay = createStatsOverlay(SAMPLE_DATA);
        updateStatsOverlay(overlay, SAMPLE_DATA);
        overlay.querySelector('.stats-toggle').click();
        expect(overlay.querySelector('.stats-body').classList.contains('stats-body--collapsed')).toBe(true);
    });

    it('handles null overlay without throwing', () => {
        expect(() => updateStatsOverlay(null, SAMPLE_DATA)).not.toThrow();
    });

    it('handles undefined overlay without throwing', () => {
        expect(() => updateStatsOverlay(undefined, SAMPLE_DATA)).not.toThrow();
    });
});

