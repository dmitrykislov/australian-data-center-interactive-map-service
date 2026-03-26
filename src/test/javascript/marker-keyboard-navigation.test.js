/**
 * Marker keyboard navigation tests.
 * Verifies Tab key cycling, Enter/Space activation, and focus indicators for markers.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { initializeMarkerKeyboardNavigation, cleanupMarkerKeyboardNavigation } from '../../main/resources/static/js/marker-keyboard-navigation.js';

describe('Marker Keyboard Navigation', () => {
    let mapContainer;
    let mockMap;
    let markers;
    let dataCenters;
    let showPopupCallbackMock;

    beforeEach(() => {
        // Create mock map container
        mapContainer = document.createElement('div');
        mapContainer.id = 'map';
        mapContainer.className = 'map-container';
        document.body.appendChild(mapContainer);

        // Create mock map
        mockMap = {
            getContainer: () => mapContainer,
            panTo: vi.fn(),
            getZoom: () => 2
        };

        // Create mock markers
        markers = {
            'dc-1': {
                options: { title: 'NYC Data Center' },
                getElement: () => {
                    let el = document.getElementById('marker-1');
                    if (!el) {
                        el = document.createElement('div');
                        el.id = 'marker-1';
                        el.className = 'leaflet-marker-icon';
                        mapContainer.appendChild(el);
                    }
                    return el;
                },
                getLatLng: () => ({ lat: 40.7128, lng: -74.0060 }),
                _map: mockMap,
                fire: vi.fn(),
                on: vi.fn()
            },
            'dc-2': {
                options: { title: 'LA Data Center' },
                getElement: () => {
                    let el = document.getElementById('marker-2');
                    if (!el) {
                        el = document.createElement('div');
                        el.id = 'marker-2';
                        el.className = 'leaflet-marker-icon';
                        mapContainer.appendChild(el);
                    }
                    return el;
                },
                getLatLng: () => ({ lat: 34.0522, lng: -118.2437 }),
                _map: mockMap,
                fire: vi.fn(),
                on: vi.fn()
            },
            'dc-3': {
                options: { title: 'Chicago Data Center' },
                getElement: () => {
                    let el = document.getElementById('marker-3');
                    if (!el) {
                        el = document.createElement('div');
                        el.id = 'marker-3';
                        el.className = 'leaflet-marker-icon';
                        mapContainer.appendChild(el);
                    }
                    return el;
                },
                getLatLng: () => ({ lat: 41.8781, lng: -87.6298 }),
                _map: mockMap,
                fire: vi.fn(),
                on: vi.fn()
            }
        };

        dataCenters = [
            { id: 'dc-1', name: 'NYC Data Center' },
            { id: 'dc-2', name: 'LA Data Center' },
            { id: 'dc-3', name: 'Chicago Data Center' }
        ];

        showPopupCallbackMock = vi.fn();
    });

    afterEach(() => {
        cleanupMarkerKeyboardNavigation();
        if (mapContainer && mapContainer.parentNode) {
            mapContainer.parentNode.removeChild(mapContainer);
        }
    });

    describe('Tab Key Navigation', () => {
        it('should initialize marker keyboard navigation', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            expect(mapContainer.getAttribute('tabindex')).toBe('0');
            expect(mapContainer.getAttribute('role')).toBe('application');
        });

        it('should focus first marker on Tab key', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });

            mapContainer.dispatchEvent(tabEvent);

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);
        });

        it('should cycle to next marker on subsequent Tab keys', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // First Tab
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            let firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);

            // Second Tab
            tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            let secondMarker = markers['dc-2'].getElement();
            expect(secondMarker.classList.contains('marker-focused')).toBe(true);
            expect(firstMarker.classList.contains('marker-focused')).toBe(false);
        });

        it('should wrap around to first marker after last marker', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Navigate to last marker
            for (let i = 0; i < 3; i++) {
                const tabEvent = new KeyboardEvent('keydown', {
                    key: 'Tab',
                    bubbles: true,
                    cancelable: true
                });
                mapContainer.dispatchEvent(tabEvent);
            }

            let lastMarker = markers['dc-3'].getElement();
            expect(lastMarker.classList.contains('marker-focused')).toBe(true);

            // Tab again should wrap to first
            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            let firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);
            expect(lastMarker.classList.contains('marker-focused')).toBe(false);
        });

        it('should navigate backwards with Shift+Tab', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Navigate to second marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            let secondMarker = markers['dc-2'].getElement();
            expect(secondMarker.classList.contains('marker-focused')).toBe(true);

            // Shift+Tab should go back to first
            tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                shiftKey: true,
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            let firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);
            expect(secondMarker.classList.contains('marker-focused')).toBe(false);
        });
    });

    describe('Enter Key Activation', () => {
        it('should open popup when Enter is pressed on focused marker', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus first marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            // Press Enter
            const enterEvent = new KeyboardEvent('keydown', {
                key: 'Enter',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(enterEvent);

            const firstMarker = markers['dc-1'];
            expect(firstMarker.fire).toHaveBeenCalledWith('click');
        });

        it('should activate marker on Enter key', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            // Press Enter
            const enterEvent = new KeyboardEvent('keydown', {
                key: 'Enter',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(enterEvent);

            const markerElement = markers['dc-1'].getElement();
            expect(markerElement.getAttribute('aria-pressed')).toBe('true');
        });
    });

    describe('Space Key Activation', () => {
        it('should open popup when Space is pressed on focused marker', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus first marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            // Press Space
            const spaceEvent = new KeyboardEvent('keydown', {
                key: ' ',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(spaceEvent);

            const firstMarker = markers['dc-1'];
            expect(firstMarker.fire).toHaveBeenCalledWith('click');
        });

        it('should activate marker on Space key', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            // Press Space
            const spaceEvent = new KeyboardEvent('keydown', {
                key: ' ',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(spaceEvent);

            const markerElement = markers['dc-1'].getElement();
            expect(markerElement.getAttribute('aria-pressed')).toBe('true');
        });
    });

    describe('Arrow Key Navigation', () => {
        it('should navigate to next marker with ArrowRight', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus first marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            // Press ArrowRight
            const arrowEvent = new KeyboardEvent('keydown', {
                key: 'ArrowRight',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(arrowEvent);

            let secondMarker = markers['dc-2'].getElement();
            expect(secondMarker.classList.contains('marker-focused')).toBe(true);
        });

        it('should navigate to next marker with ArrowDown', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus first marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            // Press ArrowDown
            const arrowEvent = new KeyboardEvent('keydown', {
                key: 'ArrowDown',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(arrowEvent);

            let secondMarker = markers['dc-2'].getElement();
            expect(secondMarker.classList.contains('marker-focused')).toBe(true);
        });

        it('should navigate to previous marker with ArrowLeft', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Navigate to second marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);
            mapContainer.dispatchEvent(tabEvent);

            // Press ArrowLeft
            const arrowEvent = new KeyboardEvent('keydown', {
                key: 'ArrowLeft',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(arrowEvent);

            let firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);
        });

        it('should navigate to previous marker with ArrowUp', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Navigate to second marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);
            mapContainer.dispatchEvent(tabEvent);

            // Press ArrowUp
            const arrowEvent = new KeyboardEvent('keydown', {
                key: 'ArrowUp',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(arrowEvent);

            let firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);
        });
    });

    describe('Escape Key Handling', () => {
        it('should clear marker focus on Escape key', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus first marker
            let tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            let firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);

            // Press Escape
            const escapeEvent = new KeyboardEvent('keydown', {
                key: 'Escape',
                bubbles: true,
                cancelable: true
            });
            document.dispatchEvent(escapeEvent);

            expect(firstMarker.classList.contains('marker-focused')).toBe(false);
        });
    });

    describe('Focus Indicators', () => {
        it('should add marker-focused class to focused marker', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(true);
        });

        it('should set tabindex on focused marker', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.getAttribute('tabindex')).toBe('0');
        });

        it('should set role=button on focused marker', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.getAttribute('role')).toBe('button');
        });

        it('should set aria-pressed attribute', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.getAttribute('aria-pressed')).toBe('false');
        });
    });

    describe('Map Container Accessibility', () => {
        it('should set tabindex on map container', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            expect(mapContainer.getAttribute('tabindex')).toBe('0');
        });

        it('should set role=application on map container', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            expect(mapContainer.getAttribute('role')).toBe('application');
        });

        it('should set descriptive aria-label on map container', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const ariaLabel = mapContainer.getAttribute('aria-label');
            expect(ariaLabel).toContain('Tab');
            expect(ariaLabel).toContain('Enter');
            expect(ariaLabel).toContain('Space');
        });
    });

    describe('Screen Reader Announcements', () => {
        it('should create live region for announcements', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const liveRegion = document.getElementById('marker-focus-announcement');
            expect(liveRegion).toBeTruthy();
            expect(liveRegion.getAttribute('role')).toBe('status');
            expect(liveRegion.getAttribute('aria-live')).toBe('polite');
        });

        it('should announce marker name and position', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const liveRegion = document.getElementById('marker-focus-announcement');
            expect(liveRegion.textContent).toContain('NYC Data Center');
            expect(liveRegion.textContent).toContain('marker 1 of 3');
        });
    });

    describe('Cleanup', () => {
        it('should remove event listeners on cleanup', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            cleanupMarkerKeyboardNavigation();

            // After cleanup, Tab should not navigate markers
            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(false);
        });

        it('should clear marker focus on cleanup', () => {
            initializeMarkerKeyboardNavigation(mockMap, markers, dataCenters, showPopupCallbackMock);

            // Focus a marker
            const tabEvent = new KeyboardEvent('keydown', {
                key: 'Tab',
                bubbles: true,
                cancelable: true
            });
            mapContainer.dispatchEvent(tabEvent);

            cleanupMarkerKeyboardNavigation();

            const firstMarker = markers['dc-1'].getElement();
            expect(firstMarker.classList.contains('marker-focused')).toBe(false);
        });
    });
});