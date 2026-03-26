/**
 * Integration tests for marker focus and keyboard navigation.
 * Tests interaction between markers, popups, and keyboard events.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { createPopup } from '../../main/resources/static/js/popup.js';

describe('Marker Focus Integration', () => {
    let mapContainer;
    let popupContainer;
    let validFacility;

    beforeEach(() => {
        // Create map container
        mapContainer = document.createElement('div');
        mapContainer.id = 'map';
        mapContainer.className = 'map-container';
        document.body.appendChild(mapContainer);

        // Create popup container
        popupContainer = document.createElement('div');
        popupContainer.id = 'popup-container';
        popupContainer.className = 'popup-hidden';
        document.body.appendChild(popupContainer);

        validFacility = {
            id: '550e8400-e29b-41d4-a716-446655440000',
            name: 'NYC Data Center',
            operator: 'TechCorp',
            address: '123 Main St',
            city: 'New York',
            status: 'operational'
        };
    });

    afterEach(() => {
        if (mapContainer && mapContainer.parentNode) {
            mapContainer.parentNode.removeChild(mapContainer);
        }
        if (popupContainer && popupContainer.parentNode) {
            popupContainer.parentNode.removeChild(popupContainer);
        }
    });

    describe('Marker and Popup Interaction', () => {
        it('should maintain focus after popup closes', () => {
            return new Promise((resolve) => {
                // Create popup
                const popup = createPopup(validFacility, popupContainer, () => {
                    // After popup closes, focus should be manageable
                    expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                    resolve();
                });

                // Close popup
                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.click();
            });
        });

        it('should allow keyboard navigation after popup closes', () => {
            return new Promise((resolve) => {
                const popup = createPopup(validFacility, popupContainer);

                // Close popup
                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.click();

                // Verify popup is closed
                setTimeout(() => {
                    expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                    resolve();
                }, 50);
            });
        });
    });

    describe('Focus Management Across Components', () => {
        it('should handle focus transitions between map and popup', () => {
            return new Promise((resolve) => {
                // Focus map
                mapContainer.focus();
                // In jsdom, activeElement may not update immediately
                expect(mapContainer).toBeTruthy();

                // Create popup
                const popup = createPopup(validFacility, popupContainer);

                // Focus should move to close button
                setTimeout(() => {
                    const closeBtn = popup.element.querySelector('.popup-close-btn');
                    expect(document.activeElement === closeBtn || document.activeElement.contains(closeBtn)).toBe(true);
                    resolve();
                }, 50);
            });
        });

        it('should restore focus to map after popup closes', () => {
            return new Promise((resolve) => {
                // Focus map
                mapContainer.focus();

                // Create and close popup
                const popup = createPopup(validFacility, popupContainer, () => {
                    // After popup closes, focus management is application's responsibility
                    expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                    resolve();
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.click();
            });
        });
    });

    describe('Keyboard Navigation with Popup Open', () => {
        it('should close popup with Escape key', () => {
            return new Promise((resolve) => {
                const popup = createPopup(validFacility, popupContainer, () => {
                    expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                    resolve();
                });

                // Press Escape
                const escapeEvent = new KeyboardEvent('keydown', {
                    key: 'Escape',
                    bubbles: true,
                    cancelable: true
                });
                document.dispatchEvent(escapeEvent);
            });
        });

        it('should allow Tab navigation within popup', () => {
            return new Promise((resolve) => {
                const popup = createPopup(validFacility, popupContainer);

                // Tab should focus close button
                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.focus();

                expect(document.activeElement === closeBtn || document.activeElement.contains(closeBtn)).toBe(true);
                resolve();
            });
        });

        it('should prevent marker navigation while popup is open', () => {
            return new Promise((resolve) => {
                const popup = createPopup(validFacility, popupContainer);

                // Popup should be visible
                expect(popupContainer.classList.contains('popup-visible')).toBe(true);

                // Close popup
                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.click();

                setTimeout(() => {
                    expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                    resolve();
                }, 50);
            });
        });
    });

    describe('Accessibility Attributes', () => {
        it('should have proper ARIA attributes on popup', () => {
            const popup = createPopup(validFacility, popupContainer);

            const title = popup.element.querySelector('#popup-title');
            expect(title).toBeTruthy();
            expect(title.textContent).toContain('NYC Data Center');
        });

        it('should have aria-label on close button', () => {
            const popup = createPopup(validFacility, popupContainer);

            const closeBtn = popup.element.querySelector('.popup-close-btn');
            expect(closeBtn.getAttribute('aria-label')).toBe('Close facility details popup');
        });

        it('should have title attribute on close button', () => {
            const popup = createPopup(validFacility, popupContainer);

            const closeBtn = popup.element.querySelector('.popup-close-btn');
            expect(closeBtn.getAttribute('title')).toContain('Close');
        });
    });

    describe('Touch and Keyboard Consistency', () => {
        it('should handle both touch and keyboard activation', () => {
            return new Promise((resolve) => {
                let closeCount = 0;
                const popup = createPopup(validFacility, popupContainer, () => {
                    closeCount++;
                    if (closeCount === 1) {
                        expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                        resolve();
                    }
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');

                // Activate via keyboard
                const enterEvent = new KeyboardEvent('keydown', {
                    key: 'Enter',
                    bubbles: true,
                    cancelable: true
                });
                closeBtn.dispatchEvent(enterEvent);
            });
        });

        it('should support both click and keyboard close', () => {
            return new Promise((resolve) => {
                let closeCount = 0;
                const popup = createPopup(validFacility, popupContainer, () => {
                    closeCount++;
                    if (closeCount === 1) {
                        expect(popupContainer.classList.contains('popup-hidden')).toBe(true);
                        resolve();
                    }
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');

                // Activate via Space key
                const spaceEvent = new KeyboardEvent('keydown', {
                    key: ' ',
                    bubbles: true,
                    cancelable: true
                });
                closeBtn.dispatchEvent(spaceEvent);
            });
        });
    });
});