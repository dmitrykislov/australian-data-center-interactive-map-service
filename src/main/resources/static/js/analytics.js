/**
 * Analytics Module
 * Tracks anonymous page views and events without collecting PII
 */

const ANALYTICS_BASE = '/api/v1/analytics';
const SESSION_ID_KEY = 'analyticsSessionId';

/**
 * Initialize analytics
 * Creates or retrieves session ID and tracks initial page view
 */
export function initializeAnalytics() {
  const sessionId = getOrCreateSessionId();
  // Fire and forget - don't block on analytics
  trackPageView().catch(() => {
    // Silently ignore errors
  });
  return { sessionId };
}

/**
 * Get or create anonymous session ID
 * @returns {string} Session ID
 */
function getOrCreateSessionId() {
  let sessionId = localStorage.getItem(SESSION_ID_KEY);

  if (!sessionId) {
    sessionId = generateUUID();
    localStorage.setItem(SESSION_ID_KEY, sessionId);
  }

  return sessionId;
}

/**
 * Get current session ID
 * @returns {string} Current session ID
 */
export function getSessionId() {
  return getOrCreateSessionId();
}

/**
 * Set custom session ID
 * @param {string} sessionId - Session ID to set
 */
export function setSessionId(sessionId) {
  if (sessionId) {
    localStorage.setItem(SESSION_ID_KEY, sessionId);
  }
}

/**
 * Generate UUID v4
 * @returns {string} UUID
 */
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * Track page view
 * @returns {Promise<void>}
 */
export async function trackPageView() {
  const sessionId = getSessionId();
  const event = {
    eventType: 'page_view',
    pageUrl: window.location.pathname,
    referrer: document.referrer || null,
    userAgent: navigator.userAgent,
    sessionId: sessionId,
    timestamp: new Date().toISOString(),
  };

  await sendAnalyticsEvent(event);
}

/**
 * Track custom event
 * @param {string} eventType - Type of event
 * @param {Object} metadata - Optional event metadata (no PII)
 * @returns {Promise<void>}
 */
export async function trackEvent(eventType, metadata = {}) {
  const sessionId = getSessionId();
  const event = {
    eventType: eventType,
    pageUrl: window.location.pathname,
    sessionId: sessionId,
    timestamp: new Date().toISOString(),
    ...metadata,
  };

  await sendAnalyticsEvent(event);
}

/**
 * Send analytics event to backend
 * @param {Object} event - Event object
 * @returns {Promise<void>}
 */
async function sendAnalyticsEvent(event) {
  try {
    let url;
    const params = new URLSearchParams();

    if (event.eventType === 'page_view') {
      url = `${ANALYTICS_BASE}/page-view`;
      params.set('pagePath', event.pageUrl || '/');
      if (event.referrer) params.set('referrer', event.referrer);
      params.set('userAgentType', /Mobi|Android/i.test(navigator.userAgent) ? 'mobile' : 'desktop');
    } else if (event.eventType === 'api_call') {
      url = `${ANALYTICS_BASE}/api-call`;
      params.set('endpoint', event.apiUrl || '/');
      params.set('statusCode', String(event.httpStatus || 200));
    } else {
      // No backend endpoint for other event types – skip silently
      return;
    }

    const response = await fetch(`${url}?${params.toString()}`, { method: 'POST' });
    if (!response.ok) {
      console.warn('Analytics event failed:', response.status);
    }
  } catch (error) {
    // Silently fail - don't disrupt user experience
    console.debug('Analytics error:', error);
  }
}

/**
 * Track filter application
 * @param {string} filterType - Type of filter applied
 * @returns {Promise<void>}
 */
export async function trackFilterApplied(filterType) {
  await trackEvent('filter_applied', { filterType });
}

/**
 * Track search performed
 * @param {string} searchTerm - Search term (sanitized, no PII)
 * @returns {Promise<void>}
 */
export async function trackSearchPerformed(searchTerm) {
  // Only track that search was performed, not the actual search term
  await trackEvent('search_performed', { hasSearchTerm: !!searchTerm });
}

/**
 * Track facility details viewed
 * @param {string} facilityId - Facility ID
 * @returns {Promise<void>}
 */
export async function trackFacilityViewed(facilityId) {
  await trackEvent('facility_viewed', { facilityId });
}

/**
 * Track an API call with its URL and HTTP status code
 * @param {string} url - API endpoint URL
 * @param {number} status - HTTP status code of the response
 * @returns {Promise<void>}
 */
export async function trackApiCall(url, status) {
  await trackEvent('api_call', { apiUrl: url, httpStatus: status });
}
