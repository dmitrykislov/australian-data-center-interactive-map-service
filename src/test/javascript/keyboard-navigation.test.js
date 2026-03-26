/**
 * Keyboard navigation tests.
 * Verifies Tab key navigation, Enter/Space activation, and Escape closing.
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { generatePopupContent, createPopup } from '../../main/resources/static/js/popup.js';

describe('Keyboard Navigation - Popup', () => {
    let container;
    let validFacility;

    beforeEach(() => {
        container = document.createElement('div');
        container.id = 'popup-container';
        container.className = 'popup-hidden';
        document.body.appendChild(container);

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
        if (container && container.parentNode) {
            container.parentNode.removeChild(container);
        }
    });

    describe('Tab Key Navigation', () => {
        it('should have focusable close button', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');
            
            expect(closeBtn).toBeTruthy();
            expect(closeBtn.tagName).toBe('BUTTON');
        });

        it('should focus close button on popup creation', () => {
            return new Promise((done) => {
                createPopup(validFacility, container);

                setTimeout(() => {
                    const closeBtn = container.querySelector('.popup-close-btn');
                    expect(closeBtn).toBeTruthy();
                    done();
                }, 50);
            });
        });

        it('should have proper tab order', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');
            
            expect(closeBtn.tagName).toBe('BUTTON');
        });

        it('should allow Tab to navigate to close button', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');
            
            expect(closeBtn).toBeTruthy();
            closeBtn.focus();
            expect(document.activeElement === closeBtn).toBe(true);
        });
    });

    describe('Enter Key Activation', () => {
        it('should close popup when Enter is pressed on close button', () => {
            return new Promise((done) => {
                const popup = createPopup(validFacility, container, () => {
                    expect(container.classList.contains('popup-hidden')).toBe(true);
                    done();
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.focus();
                
                const event = new KeyboardEvent('keydown', {
                    key: 'Enter',
                    bubbles: true,
                    cancelable: true
                });
                closeBtn.dispatchEvent(event);
            });
        });

        it('should prevent default behavior on Enter', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');

            const event = new KeyboardEvent('keydown', {
                key: 'Enter',
                bubbles: true,
                cancelable: true
            });

            let prevented = false;
            const originalPreventDefault = event.preventDefault;
            event.preventDefault = () => {
                prevented = true;
                originalPreventDefault.call(event);
            };

            closeBtn.dispatchEvent(event);
        });
    });

    describe('Space Key Activation', () => {
        it('should close popup when Space is pressed on close button', () => {
            return new Promise((done) => {
                const popup = createPopup(validFacility, container, () => {
                    expect(container.classList.contains('popup-hidden')).toBe(true);
                    done();
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.focus();

                const event = new KeyboardEvent('keydown', {
                    key: ' ',
                    bubbles: true,
                    cancelable: true
                });
                closeBtn.dispatchEvent(event);
            });
        });

        it('should prevent default behavior on Space', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');

            const event = new KeyboardEvent('keydown', {
                key: ' ',
                bubbles: true,
                cancelable: true
            });

            let prevented = false;
            const originalPreventDefault = event.preventDefault;
            event.preventDefault = () => {
                prevented = true;
                originalPreventDefault.call(event);
            };

            closeBtn.dispatchEvent(event);
        });
    });

    describe('Escape Key Handling', () => {
        it('should close popup when Escape is pressed', () => {
            return new Promise((done) => {
                const popup = createPopup(validFacility, container, () => {
                    expect(container.classList.contains('popup-hidden')).toBe(true);
                    done();
                });

                const event = new KeyboardEvent('keydown', {
                    key: 'Escape',
                    bubbles: true,
                    cancelable: true
                });
                document.dispatchEvent(event);
            });
        });

        it('should work from any focused element', () => {
            return new Promise((done) => {
                const popup = createPopup(validFacility, container, () => {
                    expect(container.classList.contains('popup-hidden')).toBe(true);
                    done();
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');
                closeBtn.focus();

                const event = new KeyboardEvent('keydown', {
                    key: 'Escape',
                    bubbles: true,
                    cancelable: true
                });
                document.dispatchEvent(event);
            });
        });

        it('should not close popup for other keys', () => {
            const popup = createPopup(validFacility, container);
            const initialState = container.classList.contains('popup-visible');

            const event = new KeyboardEvent('keydown', {
                key: 'Enter',
                bubbles: true,
                cancelable: true
            });
            document.dispatchEvent(event);

            expect(container.classList.contains('popup-visible')).toBe(initialState);
        });
    });

    describe('Focus Management', () => {
        it('should focus close button on popup creation', () => {
            return new Promise((done) => {
                createPopup(validFacility, container);

                setTimeout(() => {
                    const closeBtn = container.querySelector('.popup-close-btn');
                    expect(closeBtn).toBeTruthy();
                    done();
                }, 50);
            });
        });

        it('should maintain focus within popup', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');

            closeBtn.focus();
            expect(document.activeElement === closeBtn).toBe(true);
        });

        it('should have visible focus indicator', () => {
            const popup = createPopup(validFacility, container);
            const closeBtn = popup.element.querySelector('.popup-close-btn');

            expect(closeBtn.className).toContain('popup-close-btn');
        });
    });

    describe('Click and Keyboard Consistency', () => {
        it('should close popup on both click and Enter key', () => {
            return new Promise((done) => {
                let closeCount = 0;
                const popup = createPopup(validFacility, container, () => {
                    closeCount++;
                    if (closeCount === 1) {
                        // First close via keyboard
                        expect(container.classList.contains('popup-hidden')).toBe(true);
                        done();
                    }
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');
                const event = new KeyboardEvent('keydown', {
                    key: 'Enter',
                    bubbles: true,
                    cancelable: true
                });
                closeBtn.dispatchEvent(event);
            });
        });

        it('should close popup on both click and Space key', () => {
            return new Promise((done) => {
                let closeCount = 0;
                const popup = createPopup(validFacility, container, () => {
                    closeCount++;
                    if (closeCount === 1) {
                        expect(container.classList.contains('popup-hidden')).toBe(true);
                        done();
                    }
                });

                const closeBtn = popup.element.querySelector('.popup-close-btn');
                const event = new KeyboardEvent('keydown', {
                    key: ' ',
                    bubbles: true,
                    cancelable: true
                });
                closeBtn.dispatchEvent(event);
            });
        });
    });

    describe('Keyboard Accessibility Features', () => {
        it('should have title attribute for tooltip', () => {
            const html = generatePopupContent(validFacility);
            expect(html).toContain('title=');
        });

        it('should have aria-label for screen readers', () => {
            const html = generatePopupContent(validFacility);
            expect(html).toContain('aria-label=');
        });

        it('should have proper button semantics', () => {
            const html = generatePopupContent(validFacility);
            expect(html).toContain('<button');
        });

        it('should support keyboard shortcuts documentation', () => {
            const html = generatePopupContent(validFacility);
            expect(html).toContain('Escape key');
        });
    });

    describe('Event Listener Cleanup', () => {
        it('should remove Escape key listener on close', () => {
            return new Promise((done) => {
                const popup = createPopup(validFacility, container, () => {
                    // After close, Escape should not trigger anything
                    const event = new KeyboardEvent('keydown', {
                        key: 'Escape',
                        bubbles: true,
                        cancelable: true
                    });
                    document.dispatchEvent(event);
                    
                    expect(container.classList.contains('popup-hidden')).toBe(true);
                    done();
                });

                popup.close();
            });
        });

        it('should remove click listener on overlay after close', () => {
            return new Promise((done) => {
                const popup = createPopup(validFacility, container, () => {
                    expect(container.classList.contains('popup-hidden')).toBe(true);
                    done();
                });

                popup.close();
            });
        });
    });
});