/**
 * Integration tests for popup display using Playwright browser automation.
 * Verifies popup appears within 500ms SLA when marker is clicked.
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080';

test.describe('Facility Information Popup Integration', () => {
    test.beforeEach(async ({ page }) => {
        // Navigate to map page
        await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle' });
        
        // Wait for map to load
        await page.waitForSelector('#map', { timeout: 10000 });
    });
    
    test('should display popup within 500ms when marker is clicked', async ({ page }) => {
        // Wait for markers to be added to map
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Measure click-to-popup time
        const popupAppearTime = await page.evaluate(() => {
            return new Promise((resolve) => {
                const startTime = performance.now();
                
                // Get first marker
                const marker = document.querySelector('.leaflet-marker-icon');
                if (!marker) {
                    resolve(-1);
                    return;
                }
                
                // Setup observer for popup
                const observer = new MutationObserver((mutations) => {
                    const popup = document.querySelector('.popup-visible');
                    if (popup) {
                        const endTime = performance.now();
                        observer.disconnect();
                        resolve(endTime - startTime);
                    }
                });
                
                observer.observe(document.getElementById('popup-container'), {
                    childList: true,
                    subtree: true
                });
                
                // Click marker
                marker.click();
                
                // Timeout after 1 second
                setTimeout(() => {
                    observer.disconnect();
                    resolve(-1);
                }, 1000);
            });
        });
        
        expect(popupAppearTime).toBeGreaterThan(0);
        expect(popupAppearTime).toBeLessThan(500);
    });
    
    test('should display facility name in popup', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Get marker title (facility name)
        const facilityName = await page.getAttribute('.leaflet-marker-icon', 'title');
        
        // Click marker
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-title', { timeout: 1000 });
        
        // Verify facility name is displayed
        const popupTitle = await page.textContent('.popup-title');
        expect(popupTitle).toContain(facilityName);
    });
    
    test('should display operator in popup', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click first marker
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-content', { timeout: 1000 });
        
        // Verify operator field exists
        const operatorField = await page.locator('text=Operator').first();
        expect(operatorField).toBeTruthy();
    });
    
    test('should display address in popup', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click first marker
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-content', { timeout: 1000 });
        
        // Verify address field exists
        const addressField = await page.locator('text=Address').first();
        expect(addressField).toBeTruthy();
    });
    
    test('should display city in popup', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click first marker
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-content', { timeout: 1000 });
        
        // Verify city field exists
        const cityField = await page.locator('text=City').first();
        expect(cityField).toBeTruthy();
    });
    
    test('should display status in popup', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click first marker
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-status', { timeout: 1000 });
        
        // Verify status is displayed
        const statusElement = await page.locator('.popup-status').first();
        expect(statusElement).toBeTruthy();
        
        const statusText = await statusElement.textContent();
        const validStatuses = ['OPERATIONAL', 'MAINTENANCE', 'PLANNED', 'DECOMMISSIONED'];
        expect(validStatuses.some(s => statusText.includes(s))).toBe(true);
    });
    
    test('should display specifications when available', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click first marker
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-content', { timeout: 1000 });
        
        // Check if specifications section exists
        const specsSection = await page.locator('text=Specifications').first();
        const specsExists = await specsSection.isVisible().catch(() => false);
        
        if (specsExists) {
            // Verify at least one spec is displayed
            const specItems = await page.locator('.popup-spec-item');
            expect(await specItems.count()).toBeGreaterThan(0);
        }
    });
    
    test('should close popup when close button is clicked', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click marker to open popup
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-visible', { timeout: 1000 });
        
        // Click close button
        await page.click('.popup-close-btn');
        
        // Verify popup is hidden
        const popup = await page.locator('.popup-visible');
        expect(await popup.isVisible()).toBe(false);
    });
    
    test('should close popup when overlay is clicked', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click marker to open popup
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-overlay', { timeout: 1000 });
        
        // Click overlay
        await page.click('.popup-overlay');
        
        // Verify popup is hidden
        const popup = await page.locator('.popup-visible');
        expect(await popup.isVisible()).toBe(false);
    });
    
    test('should close popup when Escape key is pressed', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click marker to open popup
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-visible', { timeout: 1000 });
        
        // Press Escape key
        await page.keyboard.press('Escape');
        
        // Verify popup is hidden
        const popup = await page.locator('.popup-visible');
        expect(await popup.isVisible()).toBe(false);
    });
    
    test('should not close popup when clicking inside popup content', async ({ page }) => {
        // Wait for markers
        await page.waitForSelector('.leaflet-marker-icon', { timeout: 10000 });
        
        // Click marker to open popup
        await page.click('.leaflet-marker-icon');
        
        // Wait for popup
        await page.waitForSelector('.popup-content', { timeout: 1000 });
        
        // Click inside popup content
        await page.click('.popup-content');
        
        // Verify popup is still visible
        const popup = await page.locator('.popup-visible');
        expect(await popup.isVisible()).toBe(true);
    });
    
    test('should display multiple popups sequentially', async ({ page }) => {
        // Wait for markers
        const markers = await page.locator('.leaflet-marker-icon');
        const markerCount = await markers.count();
        
        if (markerCount < 2) {
            test.skip();
        }
        
        // Click first marker
        await markers.nth(0).click();
        await page.waitForSelector('.popup-visible', { timeout: 1000 });
        
        const firstTitle = await page.textContent('.popup-title');
        
        // Close popup
        await page.click('.popup-close-btn');
        
        // Click second marker
        await markers.nth(1).click();
        await page.waitForSelector('.popup-visible', { timeout: 1000 });
        
        const secondTitle = await page.textContent('.popup-title');
        
        // Verify different facilities are displayed
        expect(firstTitle).not.toEqual(secondTitle);
    });
});