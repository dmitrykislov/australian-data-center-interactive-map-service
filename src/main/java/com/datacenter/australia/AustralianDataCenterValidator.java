package com.datacenter.australia;

import com.datacenter.australia.DataCenterMetadata;
import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import com.datacenter.schema.DataCenterJsonSchema;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Validator for Australian data centers.
 * Validates data center entries against the DataCenter schema and business rules.
 * Ensures all required fields are present and valid, and checks for duplicates.
 */
public class AustralianDataCenterValidator {

  private final DataCenterJsonSchema jsonSchema;
  private final ObjectMapper objectMapper;
  private final List<String> validationErrors;
  private final Set<String> seenIds;
  private final Set<String> seenNames;

  public AustralianDataCenterValidator() {
    this.jsonSchema = new DataCenterJsonSchema();
    this.objectMapper = new ObjectMapper();
    this.validationErrors = new ArrayList<>();
    this.seenIds = new HashSet<>();
    this.seenNames = new HashSet<>();
  }

  /**
   * Validates a single data center entry.
   *
   * @param dataCenterJson the JSON node representing a data center
   * @return true if valid, false otherwise
   */
  public boolean validateDataCenter(JsonNode dataCenterJson) {
    Objects.requireNonNull(dataCenterJson, "Data center JSON cannot be null");

    // Validate against JSON schema
    var validationMessages = jsonSchema.validate(dataCenterJson);
    if (validationMessages != null && !validationMessages.isEmpty()) {
      validationErrors.add(
          String.format(
              "Data center failed schema validation: %s",
              dataCenterJson.get("name")));
      return false;
    }

    // Extract and validate individual fields
    String id = dataCenterJson.get("id").asText();
    String name = dataCenterJson.get("name").asText();

    // Check for duplicate IDs
    if (seenIds.contains(id)) {
      validationErrors.add(String.format("Duplicate data center ID: %s", id));
      return false;
    }
    seenIds.add(id);

    // Check for duplicate names
    if (seenNames.contains(name)) {
      validationErrors.add(String.format("Duplicate data center name: %s", name));
      return false;
    }
    seenNames.add(name);

    // Validate coordinates are within Australia bounds
    if (!validateAustralianCoordinates(dataCenterJson)) {
      validationErrors.add(
          String.format(
              "Data center '%s' coordinates are outside Australia: %s",
              name, dataCenterJson.get("coordinates")));
      return false;
    }

    // Validate capacity is reasonable
    long capacity = dataCenterJson.get("capacity").asLong();
    if (capacity < 1 || capacity > 10000) {
      validationErrors.add(
          String.format(
              "Data center '%s' has unrealistic capacity: %d MW", name, capacity));
      return false;
    }

    return true;
  }

  /**
   * Validates that coordinates fall within Australia's geographic bounds.
   * Australia: latitude [-43.6, -10.0], longitude [113.0, 154.0]
   */
  private boolean validateAustralianCoordinates(JsonNode dataCenterJson) {
    try {
      JsonNode coordNode = dataCenterJson.get("coordinates");
      double latitude = coordNode.get("latitude").asDouble();
      double longitude = coordNode.get("longitude").asDouble();

      // Australia bounds (approximate)
      double minLat = -43.6;
      double maxLat = -10.0;
      double minLon = 113.0;
      double maxLon = 154.0;

      return latitude >= minLat
          && latitude <= maxLat
          && longitude >= minLon
          && longitude <= maxLon;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Validates the entire data centers array.
   *
   * @param dataCentersJson the JSON array of data centers
   * @return true if all data centers are valid, false otherwise
   */
  public boolean validateDataCenters(JsonNode dataCentersJson) {
    Objects.requireNonNull(dataCentersJson, "Data centers JSON cannot be null");

    if (!dataCentersJson.isArray()) {
      validationErrors.add("Root element must be an array");
      return false;
    }

    boolean allValid = true;
    for (JsonNode dataCenterNode : dataCentersJson) {
      if (!validateDataCenter(dataCenterNode)) {
        allValid = false;
      }
    }

    return allValid;
  }

  /**
   * Converts a validated JSON node to a DataCenter object.
   * Assumes the JSON has already been validated by validateDataCenter() or validateDataCenters().
   *
   * @param dataCenterJson the JSON node
   * @return DataCenter object
   * @throws IllegalArgumentException if required fields are missing
   */
  public DataCenter toDataCenter(JsonNode dataCenterJson) {
    Objects.requireNonNull(dataCenterJson, "Data center JSON cannot be null");
    
    // Validate required fields exist
    if (!dataCenterJson.has("id") || !dataCenterJson.has("name") 
        || !dataCenterJson.has("operator") || !dataCenterJson.has("coordinates")
        || !dataCenterJson.has("capacity") || !dataCenterJson.has("status")) {
      throw new IllegalArgumentException("Invalid data center JSON: missing required fields");
    }

    String id = dataCenterJson.get("id").asText();
    String name = dataCenterJson.get("name").asText();
    String operator = dataCenterJson.get("operator").asText();

    JsonNode coordNode = dataCenterJson.get("coordinates");
    Coordinates coordinates =
        new Coordinates(
            coordNode.get("latitude").asDouble(), coordNode.get("longitude").asDouble());

    long capacity = dataCenterJson.get("capacity").asLong();
    DataCenterStatus status =
        DataCenterStatus.fromValue(dataCenterJson.get("status").asText());

    String description = null;
    if (dataCenterJson.has("description") && !dataCenterJson.get("description").isNull()) {
      description = dataCenterJson.get("description").asText();
    }

    String tags = null;
    if (dataCenterJson.has("tags") && !dataCenterJson.get("tags").isNull()) {
      tags = dataCenterJson.get("tags").asText();
    }

    String confirmationStatus = null;
    if (dataCenterJson.has("confirmationStatus") && !dataCenterJson.get("confirmationStatus").isNull()) {
      confirmationStatus = dataCenterJson.get("confirmationStatus").asText();
    }

    DataCenterMetadata metadata = null;
    if (dataCenterJson.has("metadata") && !dataCenterJson.get("metadata").isNull()) {
      JsonNode metadataNode = dataCenterJson.get("metadata");
      String sourceReference = metadataNode.has("sourceReference") ? metadataNode.get("sourceReference").asText() : null;
      String sourceUrl = metadataNode.has("sourceUrl") ? metadataNode.get("sourceUrl").asText() : null;
      String lastVerifiedDate = metadataNode.has("lastVerifiedDate") ? metadataNode.get("lastVerifiedDate").asText() : null;
      String region = metadataNode.has("region") ? metadataNode.get("region").asText() : null;
      String city = metadataNode.has("city") ? metadataNode.get("city").asText() : null;
      String comments = metadataNode.has("comments") ? metadataNode.get("comments").asText() : null;
      
      if (sourceReference != null && region != null && city != null) {
        metadata = new DataCenterMetadata(sourceReference, confirmationStatus, sourceUrl, lastVerifiedDate, region, city, comments);
      }
    }
    return new DataCenter(id, name, operator, coordinates, capacity, status, description, tags, confirmationStatus, metadata);
  }

  /**
   * Gets all validation errors accumulated during validation.
   *
   * @return list of error messages
   */
  public List<String> getValidationErrors() {
    return new ArrayList<>(validationErrors);
  }

  /**
   * Clears all validation errors and state.
   */
  public void clearErrors() {
    validationErrors.clear();
    seenIds.clear();
    seenNames.clear();
  }

  /**
   * Gets the count of validation errors.
   *
   * @return number of errors
   */
  public int getErrorCount() {
    return validationErrors.size();
  }
}