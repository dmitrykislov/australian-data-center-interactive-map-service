/**
 * Playwright configuration for integration tests.
 * Configures browser automation for popup display testing.
 */

export default {
    testDir: './src/test/javascript',
    testMatch: '**/*.integration.test.js',
    fullyParallel: false,
    forbidOnly: false,
    retries: 1,
    workers: 1,
    reporter: 'html',
    use: {
        baseURL: 'http://localhost:8080',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure'
    },
    webServer: {
        command: 'mvn spring-boot:run',
        url: 'http://localhost:8080',
        reuseExistingServer: false,
        timeout: 120000
    },
    projects: [
        {
            name: 'chromium',
            use: { ...require('@playwright/test').chromium }
        }
    ]
};