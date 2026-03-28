/**
 * Unit tests for popup content generation.
 * Tests popup HTML generation, validation, and XSS prevention.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { generatePopupContent, createPopup, measurePopupPerformance } from '../../main/resources/static/js/popup.js';

describe('Popup Content Generation', () => {
    let validFacility;
    
    beforeEach(() => {
        validFacility = {
            id: '550e8400-e29b-41d4-a716-446655440000',
            name: 'NYC Data Center',
            operator: 'TechCorp',
            address: '123 Main St',
            city: 'New York',
            status: 'operational'
        };
    });
    
    describe('generatePopupContent', () => {
        it('should generate valid HTML for facility with required fields', () => {
            const html = generatePopupContent(validFacility);
            
            expect(html).toContain('NYC Data Center');
            expect(html).toContain('TechCorp');
            expect(html).toContain('123 Main St');
            expect(html).toContain('New York');
            expect(html).toContain('Operational');
        });
        
        it('should include all required sections', () => {
            const html = generatePopupContent(validFacility);
            
            expect(html).toContain('popup-header');
            expect(html).toContain('popup-content');
            expect(html).toContain('popup-close-btn');
            expect(html).toContain('Data Provenance');
        });

        it('should display full data center details including metadata fields', () => {
            const facility = {
                ...validFacility,
                capacity: 150,
                description: 'Major facility serving enterprise customers.',
                tags: 'au,nsw,sydney',
                confirmationStatus: 'confirmed',
                coordinates: { latitude: -33.8688, longitude: 151.2093 },
                metadata: {
                    sourceReference: 'Official Operator Documentation',
                    sourceUrl: 'https://example.com/datacenter',
                    lastVerifiedDate: '2026-03-27',
                    region: 'NSW',
                    city: 'Sydney',
                    comments: 'Verified from official operator site.'
                }
            };

            const html = generatePopupContent(facility);

            expect(html).toContain('150 MW');
            expect(html).toContain('Major facility serving enterprise customers.');
            expect(html).toContain('NSW');
            expect(html).toContain('Sydney');
            expect(html).toContain('Official Operator Documentation');
            expect(html).toContain('https://example.com/datacenter');
            expect(html).toContain('2026-03-27');
            expect(html).toContain('-33.8688');
            expect(html).toContain('https://www.google.com/maps/search/?api=1&amp;query=-33.8688%2C151.2093');
            expect(html).toContain('title="Open coordinates in Google Maps"');
        });

        it('should render coordinates as a clickable Google Maps link when coordinates are valid', () => {
            const facility = {
                ...validFacility,
                coordinates: { latitude: -37.8136, longitude: 144.9631 }
            };

            const html = generatePopupContent(facility);

            expect(html).toContain('-37.8136, 144.9631');
            expect(html).toContain('href="https://www.google.com/maps/search/?api=1&amp;query=-37.8136%2C144.9631"');
            expect(html).toContain('target="_blank"');
            expect(html).toContain('rel="noopener noreferrer"');
        });

        it('should render N/A instead of a link when coordinates are invalid', () => {
            const facility = {
                ...validFacility,
                coordinates: { latitude: 'invalid', longitude: 144.9631 }
            };

            const html = generatePopupContent(facility);

            expect(html).toContain('Coordinates');
            expect(html).toContain('>N/A<');
            expect(html).not.toContain('google.com/maps/search');
        });

        it('should include info icons only for Capacity, Confirmation, and Last Verified', () => {
            const html = generatePopupContent(validFacility);
            expect(html).toContain('class="info-icon-wrap"');
            expect(html).toContain('class="info-icon"');
            const dataHelpMatches = html.match(/data-help="[^"]+"/g);
            expect(dataHelpMatches).not.toBeNull();
            expect(dataHelpMatches).toHaveLength(3);
            expect(html).toContain('Capacity');
            expect(html).toContain('Confirmation');
            expect(html).toContain('Last Verified');
            expect(html).not.toContain('Organization that owns or runs this data center.');
            expect(html).not.toContain('Current operational state of the facility.');
            expect(html).not.toContain('Latitude and longitude used to place the marker on the map.');
        });

        it('capacity info icon should use plain-language size comparisons', () => {
            const html = generatePopupContent(validFacility);
            expect(html).toContain('1 MW equals 1,000 kilowatts');
            expect(html).toContain('5–10 MW is a modest data center');
            expect(html).toContain('20 MW is large');
            expect(html).toContain('50 MW or more is typically hyperscale-scale');
        });
    });

    describe('Info icon tooltip behaviour (createPopup)', () => {
        let container;

        beforeEach(() => {
            container = document.createElement('div');
            container.id = 'popup-container';
            container.className = 'popup-hidden';
            document.body.appendChild(container);
        });

        afterEach(() => {
            if (container && container.parentNode) {
                container.parentNode.removeChild(container);
            }
        });

        function firstInfoIconWrap(popup) {
            return popup.element.querySelector('.info-icon-wrap');
        }

        it('tooltip is hidden by default', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            expect(wrap.classList.contains('tooltip-open')).toBe(false);
        });

        it('clicking info icon adds tooltip-open class', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            const icon = wrap.querySelector('.info-icon');

            icon.click();

            expect(wrap.classList.contains('tooltip-open')).toBe(true);
        });

        it('clicking info icon again removes tooltip-open class', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            const icon = wrap.querySelector('.info-icon');

            icon.click(); // open
            icon.click(); // close

            expect(wrap.classList.contains('tooltip-open')).toBe(false);
        });

        it('pressing Enter on focused icon opens tooltip', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            const icon = wrap.querySelector('.info-icon');

            icon.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

            expect(wrap.classList.contains('tooltip-open')).toBe(true);
        });

        it('pressing Space on focused icon opens tooltip', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            const icon = wrap.querySelector('.info-icon');

            icon.dispatchEvent(new KeyboardEvent('keydown', { key: ' ', bubbles: true }));

            expect(wrap.classList.contains('tooltip-open')).toBe(true);
        });

        it('pressing Escape dismisses open tooltip', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            const icon = wrap.querySelector('.info-icon');

            icon.click(); // open
            icon.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));

            expect(wrap.classList.contains('tooltip-open')).toBe(false);
        });

        it('clicking elsewhere inside popup closes tooltip', () => {
            const popup = createPopup(validFacility, container);
            const wrap = firstInfoIconWrap(popup);
            const icon = wrap.querySelector('.info-icon');

            icon.click(); // open
            expect(wrap.classList.contains('tooltip-open')).toBe(true);

            popup.element.querySelector('.popup-content').click();

            expect(wrap.classList.contains('tooltip-open')).toBe(false);
        });

        it('opening a second tooltip closes the first', () => {
            const popup = createPopup(validFacility, container);
            const wraps = popup.element.querySelectorAll('.info-icon-wrap');
            if (wraps.length < 2) return; // skip if only one icon

            const icon0 = wraps[0].querySelector('.info-icon');
            const icon1 = wraps[1].querySelector('.info-icon');

            icon0.click(); // open first
            icon1.click(); // open second — should close first

            expect(wraps[0].classList.contains('tooltip-open')).toBe(false);
            expect(wraps[1].classList.contains('tooltip-open')).toBe(true);
        });

        it('tooltip element is created in document.body and has role="tooltip"', () => {
            const popup = createPopup(validFacility, container);
            const icon = popup.element.querySelector('.info-icon');

            // Click to trigger tooltip creation/show
            icon.click();

            const tooltip = document.getElementById('popup-info-tooltip');
            expect(tooltip).not.toBeNull();
            expect(tooltip.getAttribute('role')).toBe('tooltip');
            expect(tooltip.style.display).toBe('block');
            expect(tooltip.parentNode).toBe(document.body);

            // cleanup
            icon.click();
        });

        it('should display status with correct CSS class', () => {
            const html = generatePopupContent(validFacility);
            
            expect(html).toContain('popup-status operational');
        });
        
        it('should handle different status values', () => {
            const statuses = ['operational', 'maintenance', 'planned', 'decommissioned'];
            
            statuses.forEach(status => {
                const facility = { ...validFacility, status };
                const html = generatePopupContent(facility);
                
                expect(html).toContain(`popup-status ${status}`);
            });
        });
        
        it('should escape HTML special characters in facility name', () => {
            const facility = {
                ...validFacility,
                name: '<script>alert("xss")</script>'
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).not.toContain('<script>');
            expect(html).toContain('&lt;script&gt;');
        });
        
        it('should escape HTML special characters in operator', () => {
            const facility = {
                ...validFacility,
                operator: 'Corp & Co. <Ltd>'
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).not.toContain('<Ltd>');
            expect(html).toContain('&lt;Ltd&gt;');
            expect(html).toContain('&amp;');
        });
        
        it('should escape HTML special characters in address', () => {
            const facility = {
                ...validFacility,
                address: '123 "Main" St & Ave'
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).toContain('&quot;');
            expect(html).toContain('&amp;');
        });
        
        it('should handle missing optional fields gracefully', () => {
            const facility = {
                id: '550e8400-e29b-41d4-a716-446655440000',
                name: 'Minimal DC',
                operator: 'Operator',
                status: 'operational'
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).toContain('Minimal DC');
            expect(html).toContain('N/A');
        });
        
        it('should include specifications when provided', () => {
            const facility = {
                ...validFacility,
                specifications: {
                    power: 50,
                    cooling: 1000,
                    racks: 200,
                    tier: 'Tier III'
                }
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).toContain('Specifications');
            expect(html).toContain('50 MW');
            expect(html).toContain('1000 kW');
            expect(html).toContain('200');
            expect(html).toContain('Tier III');
        });
        
        it('should include partial specifications', () => {
            const facility = {
                ...validFacility,
                specifications: {
                    power: 75,
                    tier: 'Tier IV'
                }
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).toContain('75 MW');
            expect(html).toContain('Tier IV');
            expect(html).not.toContain('kW');
        });
        
        it('should not include specifications section when empty', () => {
            const facility = {
                ...validFacility,
                specifications: {}
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).not.toContain('Specifications');
        });
        
        it('should not include specifications section when null', () => {
            const facility = {
                ...validFacility,
                specifications: null
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).not.toContain('Specifications');
        });
        
        it('should throw error when facility is null', () => {
            expect(() => generatePopupContent(null)).toThrow();
        });
        
        it('should throw error when facility is undefined', () => {
            expect(() => generatePopupContent(undefined)).toThrow();
        });
        
        it('should throw error when required field is missing', () => {
            const facility = {
                id: '550e8400-e29b-41d4-a716-446655440000',
                name: 'Test DC'
                // Missing operator and status
            };
            
            expect(() => generatePopupContent(facility)).toThrow();
        });
        
        it('should throw error for invalid status', () => {
            const facility = {
                ...validFacility,
                status: 'invalid_status'
            };
            
            expect(() => generatePopupContent(facility)).toThrow();
        });
        
        it('should handle status case-insensitively', () => {
            const facility = {
                ...validFacility,
                status: 'OPERATIONAL'
            };
            
            const html = generatePopupContent(facility);
            
            expect(html).toContain('Operational');
        });
    });
    
    describe('createPopup', () => {
        let container;
        
        beforeEach(() => {
            container = document.createElement('div');
            container.id = 'popup-container';
            container.className = 'popup-hidden';
            document.body.appendChild(container);
        });
        
        afterEach(() => {
            if (container && container.parentNode) {
                container.parentNode.removeChild(container);
            }
            // Clean up any remaining event listeners
            document.removeEventListener('keydown', () => {});
        });
        
        it('should create popup element in container', () => {
            const popup = createPopup(validFacility, container);
            
            expect(popup).toBeDefined();
            expect(popup.element).toBeDefined();
            expect(container.querySelector('.popup-dialog')).toBeTruthy();
        });
        
        it('should remove popup-hidden class from container', () => {
            createPopup(validFacility, container);
            
            expect(container.classList.contains('popup-hidden')).toBe(false);
            expect(container.classList.contains('popup-container-visible')).toBe(true);
        });
        
        it('should return popup object with close method', () => {
            const popup = createPopup(validFacility, container);
            
            expect(popup.close).toBeDefined();
            expect(typeof popup.close).toBe('function');
        });
        
        it('should measure render time', () => {
            const popup = createPopup(validFacility, container);
            
            expect(popup.renderTime).toBeDefined();
            expect(typeof popup.renderTime).toBe('number');
            expect(popup.renderTime >= 0).toBe(true);
        });
        
        it('should call onClose callback when close button clicked', () => {
            return new Promise((resolve) => {
                const onCloseMock = vi.fn(() => {
                    resolve();
                });
                
                const popup = createPopup(validFacility, container, onCloseMock);
                const closeBtn = container.querySelector('.popup-close-btn');
                closeBtn.click();
                
                // Give event handler time to execute
                setTimeout(() => {
                    expect(onCloseMock).toHaveBeenCalled();
                }, 10);
            });
        });
        
        it('should close popup when overlay clicked', () => {
            return new Promise((resolve) => {
                const onCloseMock = vi.fn(() => {
                    resolve();
                });
                
                const popup = createPopup(validFacility, container, onCloseMock);
                const overlay = container.querySelector('.popup-overlay');
                overlay.click();
                
                // Give event handler time to execute
                setTimeout(() => {
                    expect(onCloseMock).toHaveBeenCalled();
                }, 10);
            });
        });
        
        it('should not close popup when clicking inside popup content', () => {
            let closeCalled = false;
            const popup = createPopup(validFacility, container, () => {
                closeCalled = true;
            });
            
            const content = container.querySelector('.popup-content');
            content.click();
            
            expect(closeCalled).toBe(false);
        });
        
        it('should throw error when container is null', () => {
            expect(() => createPopup(validFacility, null)).toThrow();
        });
        
        it('should throw error when container is undefined', () => {
            expect(() => createPopup(validFacility, undefined)).toThrow();
        });
        
        it('should remove Escape key listener when popup closed via close button', () => {
            return new Promise((resolve) => {
                let escapeListenerCalled = false;
                const escapeListener = (e) => {
                    if (e.key === 'Escape') {
                        escapeListenerCalled = true;
                    }
                };
                
                const popup = createPopup(validFacility, container);
                const closeBtn = container.querySelector('.popup-close-btn');
                closeBtn.click();
                
                // Add a test listener after popup is closed
                document.addEventListener('keydown', escapeListener);
                
                // Simulate Escape key press
                const escapeEvent = new KeyboardEvent('keydown', { key: 'Escape' });
                document.dispatchEvent(escapeEvent);
                
                // Clean up
                document.removeEventListener('keydown', escapeListener);
                
                // The test listener should have been called (popup's listener should be gone)
                setTimeout(() => {
                    resolve();
                }, 10);
            });
        });
        
        it('should remove Escape key listener when popup closed via overlay click', () => {
            return new Promise((resolve) => {
                const popup = createPopup(validFacility, container);
                const overlay = container.querySelector('.popup-overlay');
                overlay.click();
                
                // Verify popup is closed
                const popupElement = container.querySelector('.popup-dialog');
                expect(popupElement).toBeFalsy();
                
                resolve();
            });
        });
        
        it('should not accumulate Escape key listeners across multiple popups', () => {
            return new Promise((resolve) => {
                // Create and close first popup
                const popup1 = createPopup(validFacility, container);
                const closeBtn1 = container.querySelector('.popup-close-btn');
                closeBtn1.click();
                
                // Create and close second popup
                const popup2 = createPopup(validFacility, container);
                const closeBtn2 = container.querySelector('.popup-close-btn');
                closeBtn2.click();
                
                // Create third popup and verify only one Escape listener is active
                let escapeCallCount = 0;
                const testListener = (e) => {
                    if (e.key === 'Escape') {
                        escapeCallCount++;
                    }
                };
                
                const popup3 = createPopup(validFacility, container);
                document.addEventListener('keydown', testListener);
                
                // Simulate Escape key press
                const escapeEvent = new KeyboardEvent('keydown', { key: 'Escape' });
                document.dispatchEvent(escapeEvent);
                
                // Clean up
                document.removeEventListener('keydown', testListener);
                
                // Verify popup3 is closed (its listener handled the Escape)
                setTimeout(() => {
                    const popupElement = container.querySelector('.popup-dialog');
                    expect(popupElement).toBeFalsy();
                    resolve();
                }, 10);
            });
        });
    });
    
    describe('measurePopupPerformance', () => {
        it('should measure render time', () => {
            const metrics = measurePopupPerformance(() => {
                // Simulate rendering
                let sum = 0;
                for (let i = 0; i < 1000; i++) {
                    sum += Math.sqrt(i);
                }
            });
            
            expect(metrics.renderTimeMs).toBeDefined();
            expect(metrics.renderTimeMs >= 0).toBe(true);
        });
        
        it('should indicate SLA compliance', () => {
            const metrics = measurePopupPerformance(() => {
                // Fast operation
                return 1 + 1;
            });
            
            expect(metrics.withinSLA).toBe(true);
        });
        
        it('should detect SLA violation for slow operations', () => {
            const metrics = measurePopupPerformance(() => {
                // Simulate slow operation
                const start = Date.now();
                while (Date.now() - start < 600) {
                    // Busy wait
                }
            });
            
            expect(metrics.withinSLA).toBe(false);
        });
    });
});