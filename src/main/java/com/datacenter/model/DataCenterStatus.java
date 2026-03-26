package com.datacenter.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of possible data center operational statuses.
 * Represents the current state of a data center facility.
 */
public enum DataCenterStatus {
  OPERATIONAL("operational"),
  MAINTENANCE("maintenance"),
  PLANNED("planned"),
  DECOMMISSIONED("decommissioned");

  private final String value;

  DataCenterStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  /**
   * Converts a string value to the corresponding DataCenterStatus enum.
   *
   * @param value the string representation of the status
   * @return the corresponding DataCenterStatus
   * @throws IllegalArgumentException if the value does not match any status
   */
  public static DataCenterStatus fromValue(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Status value cannot be null");
    }
    for (DataCenterStatus status : DataCenterStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException(
        String.format("Unknown status value: %s", value));
  }
}