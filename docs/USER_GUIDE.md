# User Guide

## Getting Started

### Accessing the Application

1. Open your web browser
2. Navigate to `https://localhost:8443` (or your server URL)
3. The interactive map will load with all Australian data centers

### Map Features

#### Viewing Data Centers
- **Zoom**: Use mouse wheel or pinch gesture on mobile
- **Pan**: Click and drag to move around the map
- **Markers**: Click on any marker to view facility details
- **Clustering**: Nearby markers automatically cluster for better visibility

#### Facility Information
When you click on a data center marker, a popup displays:
- **Name**: Facility name
- **Operator**: Company operating the facility
- **Location**: City and coordinates
- **Capacity**: Number of racks or power capacity
- **Status**: Operational or Planned
- **Description**: Additional facility information

### Filter Controls

#### Available Filters
1. **Operator Filter**: Select one or more data center operators
2. **Region Filter**: Filter by Australian state or region
3. **Status Filter**: Show operational, planned, or all facilities
4. **Capacity Filter**: Filter by facility capacity range

#### Using Filters
1. Click on the filter icon in the top-left corner
2. Select your filter criteria
3. Results update in real-time
4. Click "Clear Filters" to reset

### Search Functionality

#### Quick Search
1. Click the search box at the top of the map
2. Type facility name or operator name
3. Matching results appear as you type
4. Click a result to jump to that facility

#### Autocomplete
- Search suggestions appear as you type
- Press Enter or click a suggestion to search
- Results are sorted by relevance

### Comparison Tool

#### Comparing Facilities
1. Click on a facility marker
2. Click "Compare" button in the popup
3. Select up to 4 additional facilities to compare
4. View side-by-side comparison of:
   - Distance between facilities
   - Estimated network latency
   - Facility specifications

#### Distance Calculation
- Distances calculated using Haversine formula
- Accurate to within 1 kilometer
- Useful for network planning

### Accessibility Features

#### Keyboard Navigation
- **Tab**: Move between interactive elements
- **Enter/Space**: Activate buttons and links
- **Arrow Keys**: Navigate map and lists
- **Escape**: Close popups and dialogs

#### Screen Reader Support
- All content properly labeled with ARIA attributes
- Semantic HTML for better screen reader compatibility
- Alternative text for all images

#### Visual Accessibility
- **High Contrast Mode**: Automatically enabled on system preference
- **Color Blind Friendly**: Distinct colors for different facility types
- **Large Text**: Zoom to 200% for better readability
- **Reduced Motion**: Respects system motion preferences

### Mobile Experience

#### Touch Gestures
- **Pinch**: Zoom in and out
- **Two-finger pan**: Move around the map
- **Long press**: Open facility details
- **Swipe**: Navigate between views

#### Responsive Design
- Optimized for screens from 320px to 2560px wide
- Touch targets minimum 44x44 pixels
- Readable text at all zoom levels

### Data Attribution

#### Map Tiles
- **Provider**: OpenStreetMap
- **License**: ODbL (Open Data Commons Open Database License)
- **Attribution**: © OpenStreetMap contributors

#### Data Sources
- Australian data center information from multiple sources
- Each facility marked with verification status
- Source references available in facility details

### Analytics

#### What We Track
- Page views (which pages you visit)
- API calls (which features you use)
- Error events (if something goes wrong)

#### What We Don't Track
- Your IP address
- Your identity
- Your session information
- Your personal data

#### Privacy
- All tracking is anonymous
- Data retained for 30 days
- No cookies used
- No third-party tracking

### Troubleshooting

#### Map Not Loading
1. Check your internet connection
2. Ensure you're using HTTPS (not HTTP)
3. Clear browser cache and reload
4. Try a different browser

#### Markers Not Showing
1. Check if filters are too restrictive
2. Zoom out to see more facilities
3. Clear all filters and try again
4. Refresh the page

#### Search Not Working
1. Check spelling of facility name
2. Try searching by operator name
3. Clear search box and try again
4. Ensure JavaScript is enabled

#### Slow Performance
1. Zoom in to reduce number of visible markers
2. Close other browser tabs
3. Clear browser cache
4. Try a different browser

### Tips and Tricks

#### Efficient Navigation
- Use filters to narrow down facilities
- Search for specific operators
- Use comparison tool for network planning
- Bookmark important facilities

#### Best Practices
- Use HTTPS for secure connection
- Enable JavaScript for full functionality
- Allow location access for better experience
- Use modern browser for best compatibility

### Supported Browsers

- **Chrome**: Version 90+
- **Firefox**: Version 88+
- **Safari**: Version 14+
- **Edge**: Version 90+
- **Mobile Safari**: iOS 14+
- **Chrome Mobile**: Android 10+

### Getting Help

#### Documentation
- API Reference: See API_REFERENCE.md
- Security Information: See SECURITY.md
- Technical Details: See SECURITY_IMPLEMENTATION.md

#### Reporting Issues
- Check existing issues on GitHub
- Provide detailed description of problem
- Include browser and OS information
- Attach screenshots if helpful

### Feedback

We welcome your feedback to improve the application:
- Feature requests
- Bug reports
- Usability suggestions
- Performance feedback

Please submit feedback through the GitHub issues page.