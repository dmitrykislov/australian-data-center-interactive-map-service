package com.datacenter.attribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service for managing attribution information for tile providers and data sources.
 */
@Service
public class AttributionService {

  private final List<AttributionProvider> providers;

  public AttributionService() {
    this.providers = initializeProviders();
  }

  /**
   * Gets all attribution providers.
   *
   * @return an unmodifiable list of attribution providers
   */
  public List<AttributionProvider> getAllProviders() {
    return Collections.unmodifiableList(providers);
  }

  /**
   * Gets attribution providers by type.
   *
   * @param type the type of provider ("tile_provider" or "data_source")
   * @return a list of matching providers
   */
  public List<AttributionProvider> getProvidersByType(String type) {
    return providers.stream().filter(p -> p.getType().equals(type)).toList();
  }

  private List<AttributionProvider> initializeProviders() {
    List<AttributionProvider> list = new ArrayList<>();

    // Tile providers
    list.add(
        new AttributionProvider(
            "OpenStreetMap",
            "https://www.openstreetmap.org/",
            "© OpenStreetMap contributors",
            "tile_provider"));

    list.add(
        new AttributionProvider(
            "Stadia Maps",
            "https://stadiamaps.com/",
            "© Stadia Maps, © OpenMapTiles, © OpenStreetMap contributors",
            "tile_provider"));

    // Data sources
    list.add(
        new AttributionProvider(
            "Australian Data Centers",
            "https://github.com/datacenter/australia",
            "Data compiled from public sources and industry databases",
            "data_source"));

    return list;
  }
}