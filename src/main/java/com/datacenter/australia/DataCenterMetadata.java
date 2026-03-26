package com.datacenter.australia;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Metadata for a data center including source references and confirmation status.
 * Tracks whether a data center has been validated against online sources.
 */
public class DataCenterMetadata {

  private final String sourceReference;
  private final String confirmationStatus; // "confirmed" or "unconfirmed"
  private final String sourceUrl;
  private final String lastVerifiedDate;
  private final String region; // Australian state/territory
  private final String city;
  private final String comments;

  @JsonCreator
  public DataCenterMetadata(
      @JsonProperty("sourceReference") String sourceReference,
      @JsonProperty("confirmationStatus") String confirmationStatus,
      @JsonProperty("sourceUrl") String sourceUrl,
      @JsonProperty("lastVerifiedDate") String lastVerifiedDate,
      @JsonProperty("region") String region,
      @JsonProperty("city") String city,
      @JsonProperty("comments") String comments) {
    this.sourceReference = validateSourceReference(sourceReference);
    this.confirmationStatus = validateConfirmationStatus(confirmationStatus);
    this.sourceUrl = sourceUrl;
    this.lastVerifiedDate = lastVerifiedDate;
    this.region = validateRegion(region);
    this.city = validateCity(city);
    this.comments = comments;
  }

  private static String validateSourceReference(String sourceReference) {
    if (sourceReference == null || sourceReference.trim().isEmpty()) {
      throw new IllegalArgumentException("Source reference cannot be null or empty");
    }
    return sourceReference;
  }

  private static String validateConfirmationStatus(String status) {
    if (status == null || status.trim().isEmpty()) {
      throw new IllegalArgumentException("Confirmation status cannot be null or empty");
    }
    String normalized = status.toLowerCase();
    if (!normalized.equals("confirmed") && !normalized.equals("unconfirmed")) {
      throw new IllegalArgumentException(
          "Confirmation status must be 'confirmed' or 'unconfirmed', got: " + status);
    }
    return normalized;
  }

  private static String validateRegion(String region) {
    if (region == null || region.trim().isEmpty()) {
      throw new IllegalArgumentException("Region cannot be null or empty");
    }
    return region;
  }

  private static String validateCity(String city) {
    if (city == null || city.trim().isEmpty()) {
      throw new IllegalArgumentException("City cannot be null or empty");
    }
    return city;
  }

  @JsonProperty("sourceReference")
  public String getSourceReference() {
    return sourceReference;
  }

  @JsonProperty("confirmationStatus")
  public String getConfirmationStatus() {
    return confirmationStatus;
  }

  @JsonProperty("sourceUrl")
  public String getSourceUrl() {
    return sourceUrl;
  }

  @JsonProperty("lastVerifiedDate")
  public String getLastVerifiedDate() {
    return lastVerifiedDate;
  }

  @JsonProperty("region")
  public String getRegion() {
    return region;
  }

  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  @JsonProperty("comments")
  public String getComments() {
    return comments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataCenterMetadata that = (DataCenterMetadata) o;
    return Objects.equals(sourceReference, that.sourceReference)
        && Objects.equals(confirmationStatus, that.confirmationStatus)
        && Objects.equals(region, that.region)
        && Objects.equals(city, that.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceReference, confirmationStatus, region, city);
  }

  @Override
  public String toString() {
    return String.format(
        "DataCenterMetadata{region='%s', city='%s', status='%s', source='%s'}",
        region, city, confirmationStatus, sourceReference);
  }
}