package com.datacenter.api;

import com.datacenter.attribution.AttributionProvider;
import com.datacenter.attribution.AttributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST API controller for attribution endpoints.
 * Provides attribution information for tile providers and data sources.
 */
@RestController
@RequestMapping("/api/v1/attribution")
public class AttributionController {

  private final AttributionService attributionService;

  /**
   * Creates an AttributionController with the given dependencies.
   *
   * @param attributionService the attribution service
   */
  @Autowired
  public AttributionController(AttributionService attributionService) {
    this.attributionService = Objects.requireNonNull(attributionService);
  }

  /**
   * Gets all attribution providers.
   *
   * @param type optional filter by type (tile_provider or data_source)
   * @return list of attribution providers
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAttributions(
      @RequestParam(name = "type", required = false) String type) {
    List<AttributionProvider> providers;

    if (type != null && !type.trim().isEmpty()) {
      providers = attributionService.getProvidersByType(type);
    } else {
      providers = attributionService.getAllProviders();
    }

    Map<String, Object> response = new HashMap<>();
    response.put("data", providers);
    response.put("total", providers.size());

    return ResponseEntity.ok(response);
  }
}