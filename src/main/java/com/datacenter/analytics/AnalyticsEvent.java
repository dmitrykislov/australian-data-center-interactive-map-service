package com.datacenter.analytics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an analytics event for tracking page views and API calls.
 * Immutable data class that does not capture PII.
 */
public class AnalyticsEvent {

  private final String eventType;
  private final Instant timestamp;
  private final Map<String, String> properties;

  /**
   * Creates an analytics event.
   *
   * @param eventType the type of event (page_view, api_call, error)
   * @param timestamp the event timestamp
   * @param properties event-specific properties (no PII)
   */
  public AnalyticsEvent(String eventType, Instant timestamp, Map<String, String> properties) {
    this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
    this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    this.properties = new HashMap<>(Objects.requireNonNull(properties, "Properties cannot be null"));
  }

  public String getEventType() {
    return eventType;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public Map<String, String> getProperties() {
    return new HashMap<>(properties);
  }

  @Override
  public String toString() {
    return "AnalyticsEvent{"
        + "eventType='"
        + eventType
        + '\''
        + ", timestamp="
        + timestamp
        + ", properties="
        + properties
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnalyticsEvent that = (AnalyticsEvent) o;
    return Objects.equals(eventType, that.eventType)
        && Objects.equals(timestamp, that.timestamp)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventType, timestamp, properties);
  }
}