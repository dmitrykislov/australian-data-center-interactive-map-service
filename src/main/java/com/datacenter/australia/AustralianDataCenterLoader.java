package com.datacenter.australia;

import com.datacenter.model.DataCenter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Loader for Australian data centers from JSON file.
 * Loads and validates data centers from the data-centers.json resource file.
 */
public class AustralianDataCenterLoader {

  private final ObjectMapper objectMapper;
  private final AustralianDataCenterValidator validator;

  public AustralianDataCenterLoader() {
    this.objectMapper = new ObjectMapper();
    this.validator = new AustralianDataCenterValidator();
  }

  /**
   * Loads data centers from the classpath resource file.
   *
   * @return list of validated DataCenter objects
   * @throws IOException if the file cannot be read
   * @throws IllegalArgumentException if validation fails
   */
  public List<DataCenter> loadFromClasspath() throws IOException {
    InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream("data-centers.json");
    if (inputStream == null) {
      throw new IOException("data-centers.json not found in classpath");
    }

    JsonNode rootNode = objectMapper.readTree(inputStream);
    return loadFromJsonNode(rootNode);
  }

  /**
   * Loads data centers from a file path.
   *
   * @param filePath the path to the JSON file
   * @return list of validated DataCenter objects
   * @throws IOException if the file cannot be read
   * @throws IllegalArgumentException if validation fails
   */
  public List<DataCenter> loadFromFile(Path filePath) throws IOException {
    Objects.requireNonNull(filePath, "File path cannot be null");

    if (!Files.exists(filePath)) {
      throw new IOException("File not found: " + filePath);
    }

    JsonNode rootNode = objectMapper.readTree(Files.newInputStream(filePath));
    return loadFromJsonNode(rootNode);
  }

  /**
   * Loads data centers from a JSON string.
   *
   * @param jsonString the JSON string
   * @return list of validated DataCenter objects
   * @throws IOException if JSON parsing fails
   * @throws IllegalArgumentException if validation fails
   */
  public List<DataCenter> loadFromString(String jsonString) throws IOException {
    Objects.requireNonNull(jsonString, "JSON string cannot be null");

    JsonNode rootNode = objectMapper.readTree(jsonString);
    return loadFromJsonNode(rootNode);
  }

  private List<DataCenter> loadFromJsonNode(JsonNode rootNode) {
    validator.clearErrors();

    if (!validator.validateDataCenters(rootNode)) {
      List<String> errors = validator.getValidationErrors();
      throw new IllegalArgumentException(
          String.format(
              "Data center validation failed with %d errors: %s",
              errors.size(), String.join("; ", errors)));
    }

    List<DataCenter> dataCenters = new ArrayList<>();
    for (JsonNode dataCenterNode : rootNode) {
      DataCenter dc = validator.toDataCenter(dataCenterNode);
      dataCenters.add(dc);
    }

    return dataCenters;
  }

  /**
   * Gets validation errors from the last load operation.
   *
   * @return list of error messages
   */
  public List<String> getValidationErrors() {
    return validator.getValidationErrors();
  }
}