# Attribution Implementation Guide

## Overview

This document describes how attribution is implemented for tile providers and data sources in the Australian Data Centers Mapping application.

## Attribution Providers

### Tile Providers

Tile providers supply the map tiles displayed in the background of the map.

#### OpenStreetMap
- **Name**: OpenStreetMap
- **Type**: tile_provider
- **URL**: https://www.openstreetmap.org
- **Attribution**: © OpenStreetMap contributors
- **License**: ODbL (Open Data Commons Open Database License)
- **Description**: Free and open-source map data

### Data Sources

Data sources provide the information about Australian data centers.

#### Australian Data Centers Dataset
- **Name**: Australian Data Centers
- **Type**: data_source
- **URL**: https://example.com/data
- **Attribution**: Australian Data Center Information
- **License**: Custom
- **Description**: Comprehensive database of Australian data centers

## Implementation

### AttributionService

The `AttributionService` class manages all attribution providers:

```java
@Service
public class AttributionService {
  public List<AttributionProvider> getAllProviders()
  public List<AttributionProvider> getProvidersByType(String type)
}
```

### AttributionProvider

The `AttributionProvider` class represents a single attribution provider:

```java
public class AttributionProvider {
  private String name;
  private String type;
  private String url;
  private String attribution;
}
```

### API Endpoint

The `/api/v1/attribution/providers` endpoint returns all attribution providers:

```
GET /api/v1/attribution/providers
```

**Response:**
```json
{
  "providers": [
    {
      "name": "OpenStreetMap",
      "type": "tile_provider",
      "url": "https://www.openstreetmap.org",
      "attribution": "© OpenStreetMap contributors"
    },
    {
      "name": "Australian Data Centers",
      "type": "data_source",
      "url": "https://example.com/data",
      "attribution": "Australian Data Center Information"
    }
  ]
}
```

## Frontend Display

### Attribution Container

Attribution is displayed in a dedicated container at the bottom of the map:

```html
<div id="attribution-container">
  <div class="attribution-item" data-type="tile_provider">
    <a href="https://www.openstreetmap.org" target="_blank" rel="noopener noreferrer">
      OpenStreetMap
    </a>
    <span class="attribution-text">© OpenStreetMap contributors</span>
  </div>
  <div class="attribution-item" data-type="data_source">
    <a href="https://example.com/data" target="_blank" rel="noopener noreferrer">
      Australian Data Centers
    </a>
    <span class="attribution-text">Australian Data Center Information</span>
  </div>
</div>
```

### CSS Styling

Attribution items are styled with:
- Horizontal layout
- Proper spacing and padding
- Readable font size
- Accessible color contrast
- Responsive design for mobile

### JavaScript Implementation

Attribution is loaded and displayed using JavaScript:

```javascript
async function loadAttribution() {
  const response = await fetch('/api/v1/attribution/providers');
  const data = await response.json();
  displayAttribution(data.providers);
}

function displayAttribution(providers) {
  const container = document.getElementById('attribution-container');
  const html = providers.map(provider => `
    <div class="attribution-item" data-type="${provider.type}">
      <a href="${provider.url}" target="_blank" rel="noopener noreferrer">
        ${provider.name}
      </a>
      <span class="attribution-text">${provider.attribution}</span>
    </div>
  `).join('');
  container.innerHTML = html;
}
```

## Security Considerations

### XSS Prevention

Attribution text is properly escaped to prevent XSS attacks:
- HTML special characters are escaped
- User input is never directly inserted into HTML
- Content Security Policy restricts inline scripts

### Link Security

Attribution links use security attributes:
- `target="_blank"` - Opens in new tab
- `rel="noopener noreferrer"` - Prevents window.opener access
- HTTPS URLs only - Ensures secure connections

### Input Validation

Attribution provider data is validated:
- Names and URLs validated on backend
- Attribution text sanitized
- Invalid data rejected

## Accessibility

### ARIA Labels

Attribution items include ARIA labels:
```html
<div class="attribution-item" role="contentinfo" aria-label="Map attribution">
  ...
</div>
```

### Keyboard Navigation

Attribution links are keyboard accessible:
- Tab key navigates between links
- Enter key opens links
- Focus indicators visible

### Screen Reader Support

Attribution is properly announced to screen readers:
- Semantic HTML structure
- Descriptive link text
- Role attributes for context

## Testing

### Unit Tests

`AttributionServiceTest` verifies:
- All providers are returned
- Providers can be filtered by type
- Providers are immutable
- Required fields are present

### Integration Tests

Frontend tests verify:
- Attribution loads from API
- Attribution displays correctly
- Links have proper attributes
- HTML is properly escaped

## Adding New Attribution Providers

To add a new attribution provider:

1. **Update AttributionService**:
```java
private List<AttributionProvider> initializeProviders() {
  providers.add(new AttributionProvider(
    "New Provider",
    "tile_provider",
    "https://example.com",
    "Attribution text"
  ));
}
```

2. **Test the change**:
```java
@Test
void testNewProviderExists() {
  List<AttributionProvider> providers = attributionService.getAllProviders();
  assertTrue(providers.stream().anyMatch(p -> "New Provider".equals(p.getName())));
}
```

3. **Verify frontend display**:
- Check attribution container
- Verify link works
- Test on mobile

## Best Practices

### Attribution Accuracy
- Keep attribution text accurate and up-to-date
- Include proper copyright symbols (©)
- Link to official provider websites
- Update when provider information changes

### User Experience
- Display attribution prominently
- Make attribution links accessible
- Ensure attribution doesn't obscure map
- Responsive design for all screen sizes

### Legal Compliance
- Respect license requirements
- Include required attribution text
- Link to license documents
- Update when licenses change

### Performance
- Load attribution asynchronously
- Cache attribution data
- Minimize DOM updates
- Optimize CSS for attribution display

## References

- [OpenStreetMap Attribution](https://www.openstreetmap.org/copyright)
- [ODbL License](https://opendatacommons.org/licenses/odbl/)
- [Web Accessibility Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [OWASP XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)