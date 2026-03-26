package com.datacenter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents geographic coordinates (latitude, longitude) for a data center location.
 * Coordinates are validated to ensure they fall within valid geographic ranges:
 * - Latitude: [-90, 90]
 * - Longitude: [-180, 180]
 */
public class Coordinates {

  private static final double MIN_LATITUDE = -90.0;
  private static final double MAX_LATITUDE = 90.0;
  private static final double MIN_LONGITUDE = -180.0;
  private static final double MAX_LONGITUDE = 180.0;

  private final double latitude;
  private final double longitude;

  @JsonCreator
  public Coordinates(
      @JsonProperty("latitude") double latitude,
      @JsonProperty("longitude") double longitude) {
    validateLatitude(latitude);
    validateLongitude(longitude);
    this.latitude = latitude;
    this.longitude = longitude;
  }

  private static void validateLatitude(double latitude) {
    if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
      throw new IllegalArgumentException(
          String.format(
              "Latitude must be between %.1f and %.1f, got %.6f",
              MIN_LATITUDE, MAX_LATITUDE, latitude));
    }
  }

  private static void validateLongitude(double longitude) {
    if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
      throw new IllegalArgumentException(
          String.format(
              "Longitude must be between %.1f and %.1f, got %.6f",
              MIN_LONGITUDE, MAX_LONGITUDE, longitude));
    }
  }

  @JsonProperty("latitude")
  public double getLatitude() {
    return latitude;
  }

  @JsonProperty("longitude")
  public double getLongitude() {
    return longitude;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Coordinates that = (Coordinates) o;
    return Double.compare(that.latitude, latitude) == 0
        && Double.compare(that.longitude, longitude) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }

  @Override
  public String toString() {
    return String.format("Coordinates{latitude=%.6f, longitude=%.6f}", latitude, longitude);
  }
}