package com.datacenter.attribution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttributionServiceTest {

  private AttributionService attributionService;

  @BeforeEach
  void setUp() {
    attributionService = new AttributionService();
  }

  @Test
  void testGetAllProviders() {
    List<AttributionProvider> providers = attributionService.getAllProviders();
    assertNotNull(providers);
    assertFalse(providers.isEmpty());
  }

  @Test
  void testGetProvidersByTypeTileProvider() {
    List<AttributionProvider> providers = attributionService.getProvidersByType("tile_provider");
    assertNotNull(providers);
    assertFalse(providers.isEmpty());
    assertTrue(providers.stream().allMatch(p -> "tile_provider".equals(p.getType())));
  }

  @Test
  void testGetProvidersByTypeDataSource() {
    List<AttributionProvider> providers = attributionService.getProvidersByType("data_source");
    assertNotNull(providers);
    assertFalse(providers.isEmpty());
    assertTrue(providers.stream().allMatch(p -> "data_source".equals(p.getType())));
  }

  @Test
  void testGetProvidersByTypeInvalid() {
    List<AttributionProvider> providers = attributionService.getProvidersByType("invalid");
    assertNotNull(providers);
    assertTrue(providers.isEmpty());
  }

  @Test
  void testProvidersAreImmutable() {
    List<AttributionProvider> providers = attributionService.getAllProviders();
    assertThrows(UnsupportedOperationException.class, () -> providers.add(null));
  }

  @Test
  void testOpenStreetMapProviderExists() {
    List<AttributionProvider> providers = attributionService.getProvidersByType("tile_provider");
    assertTrue(providers.stream().anyMatch(p -> "OpenStreetMap".equals(p.getName())));
  }

  @Test
  void testAttributionProviderHasRequiredFields() {
    List<AttributionProvider> providers = attributionService.getAllProviders();
    assertTrue(providers.stream().allMatch(p -> p.getName() != null && !p.getName().isEmpty()));
    assertTrue(providers.stream().allMatch(p -> p.getType() != null && !p.getType().isEmpty()));
    assertTrue(providers.stream().allMatch(p -> p.getUrl() != null && !p.getUrl().isEmpty()));
    assertTrue(providers.stream().allMatch(p -> p.getAttribution() != null && !p.getAttribution().isEmpty()));
  }
}