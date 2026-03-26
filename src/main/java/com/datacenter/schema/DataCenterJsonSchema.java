package com.datacenter.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Validates DataCenter JSON data against the schema.
 * Provides methods to validate raw data files and ensure they conform to the expected structure.
 */
public class DataCenterJsonSchema {

  private static final String SCHEMA_RESOURCE = "/schema/datacenter-schema.json";
  private final JsonSchema schema;
  private final ObjectMapper objectMapper;

  public DataCenterJsonSchema() {
    this.objectMapper = new ObjectMapper();
    this.schema = loadSchema();
  }

  private JsonSchema loadSchema() {
    try (InputStream schemaStream = getClass().getResourceAsStream(SCHEMA_RESOURCE)) {
      if (schemaStream == null) {
        throw new IllegalStateException(
            String.format("Schema resource not found: %s", SCHEMA_RESOURCE));
      }
      JsonNode schemaNode = objectMapper.readTree(schemaStream);
      JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
      return factory.getSchema(schemaNode);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load DataCenter JSON schema", e);
    }
  }

  /**
   * Validates a JSON string against the DataCenter schema.
   *
   * @param jsonString the JSON string to validate
   * @return a set of validation messages (empty if valid)
   * @throws IOException if the JSON cannot be parsed
   */
  public Set<ValidationMessage> validate(String jsonString) throws IOException {
    JsonNode jsonNode = objectMapper.readTree(jsonString);
    return validate(jsonNode);
  }

  /**
   * Validates a JsonNode against the DataCenter schema.
   *
   * @param jsonNode the JSON node to validate
   * @return a set of validation messages (empty if valid)
   */
  public Set<ValidationMessage> validate(JsonNode jsonNode) {
    return schema.validate(jsonNode);
  }

  /**
   * Checks if a JSON string is valid according to the schema.
   *
   * @param jsonString the JSON string to validate
   * @return true if valid, false otherwise
   */
  public boolean isValid(String jsonString) {
    try {
      JsonNode jsonNode = objectMapper.readTree(jsonString);
      return schema.validate(jsonNode).isEmpty();
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Gets the underlying JsonSchema object for advanced validation scenarios.
   *
   * @return the JsonSchema instance
   */
  public JsonSchema getSchema() {
    return schema;
  }
}