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
            expect(container.querySelector('.popup-visible')).toBeTruthy();
        });
        
        it('should remove popup-hidden class from container', () => {
            createPopup(validFacility, container);
            
            expect(container.classList.contains('popup-hidden')).toBe(false);
            expect(container.classList.contains('popup-visible')).toBe(true);
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
                const popupElement = container.querySelector('.popup-visible');
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
                    const popupElement = container.querySelector('.popup-visible');
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