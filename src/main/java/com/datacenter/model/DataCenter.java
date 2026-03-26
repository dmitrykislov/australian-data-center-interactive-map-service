package com.datacenter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a data center facility with location, specifications, operator, and status.
 * This is the core domain model for the data center mapping system.
 *
 * <p>Required fields: id, name, operator, coordinates, capacity, status
 * Optional fields: description, tags
 */
public class DataCenter {

  private final String id;
  private final String name;
  private final String operator;
  private final Coordinates coordinates;
  private final long capacity; // in MW
  private final DataCenterStatus status;
  private final String description;
  private final String tags;

  @JsonCreator
  public DataCenter(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("operator") String operator,
      @JsonProperty("coordinates") Coordinates coordinates,
      @JsonProperty("capacity") long capacity,
      @JsonProperty("status") DataCenterStatus status,
      @JsonProperty("description") String description,
      @JsonProperty("tags") String tags) {
    this.id = validateId(id);
    this.name = validateName(name);
    this.operator = validateOperator(operator);
    this.coordinates = validateCoordinates(coordinates);
    this.capacity = validateCapacity(capacity);
    this.status = validateStatus(status);
    this.description = description;
    this.tags = tags;
  }

  /**
   * Creates a DataCenter with required fields only. Optional fields are set to null.
   */
  public DataCenter(
      String id,
      String name,
      String operator,
      Coordinates coordinates,
      long capacity,
      DataCenterStatus status) {
    this(id, name, operator, coordinates, capacity, status, null, null);
  }

  private static String validateId(String id) {
    if (id == null || id.trim().isEmpty()) {
      throw new IllegalArgumentException("DataCenter id cannot be null or empty");
    }
    // Validate UUID format and normalize to lowercase
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("DataCenter id must be a valid UUID, got: %s", id), e);
    }
    return id.toLowerCase();
  }

  private static String validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("DataCenter name cannot be null or empty");
    }
    if (name.length() > 255) {
      throw new IllegalArgumentException(
          String.format("DataCenter name must not exceed 255 characters, got %d", name.length()));
    }
    return name;
  }

  private static String validateOperator(String operator) {
    if (operator == null || operator.trim().isEmpty()) {
      throw new IllegalArgumentException("DataCenter operator cannot be null or empty");
    }
    if (operator.length() > 255) {
      throw new IllegalArgumentException(
          String.format(
              "DataCenter operator must not exceed 255 characters, got %d", operator.length()));
    }
    return operator;
  }

  private static Coordinates validateCoordinates(Coordinates coordinates) {
    if (coordinates == null) {
      throw new IllegalArgumentException("DataCenter coordinates cannot be null");
    }
    return coordinates;
  }

  private static long validateCapacity(long capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException(
          String.format("DataCenter capacity must be positive, got %d", capacity));
    }
    return capacity;
  }

  private static DataCenterStatus validateStatus(DataCenterStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("DataCenter status cannot be null");
    }
    return status;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("operator")
  public String getOperator() {
    return operator;
  }

  @JsonProperty("coordinates")
  public Coordinates getCoordinates() {
    return coordinates;
  }

  @JsonProperty("capacity")
  public long getCapacity() {
    return capacity;
  }

  @JsonProperty("status")
  public DataCenterStatus getStatus() {
    return status;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("tags")
  public String getTags() {
    return tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataCenter that = (DataCenter) o;
    return capacity == that.capacity
        && Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(operator, that.operator)
        && Objects.equals(coordinates, that.coordinates)
        && status == that.status
        && Objects.equals(description, that.description)
        && Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, operator, coordinates, capacity, status, description, tags);
  }

  @Override
  public String toString() {
    return String.format(
        "DataCenter{id='%s', name='%s', operator='%s', coordinates=%s, capacity=%d, status=%s}",
        id, name, operator, coordinates, capacity, status);
  }
}