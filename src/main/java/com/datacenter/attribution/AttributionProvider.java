package com.datacenter.attribution;

import java.util.Objects;

/**
 * Represents an attribution provider for tile sources or data sources.
 * Immutable data class for attribution information.
 */
public class AttributionProvider {

  private final String name;
  private final String url;
  private final String attribution;
  private final String type;

  /**
   * Creates an attribution provider.
   *
   * @param name the provider name
   * @param url the provider URL
   * @param attribution the attribution text
   * @param type the provider type ("tile_provider" or "data_source")
   */
  public AttributionProvider(String name, String url, String attribution, String type) {
    this.name = Objects.requireNonNull(name, "Name cannot be null");
    this.url = Objects.requireNonNull(url, "URL cannot be null");
    this.attribution = Objects.requireNonNull(attribution, "Attribution cannot be null");
    this.type = Objects.requireNonNull(type, "Type cannot be null");
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getAttribution() {
    return attribution;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "AttributionProvider{"
        + "name='"
        + name
        + '\''
        + ", url='"
        + url
        + '\''
        + ", attribution='"
        + attribution
        + '\''
        + ", type='"
        + type
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttributionProvider that = (AttributionProvider) o;
    return Objects.equals(name, that.name)
        && Objects.equals(url, that.url)
        && Objects.equals(attribution, that.attribution)
        && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url, attribution, type);
  }
}