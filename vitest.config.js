import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    exclude: [
      'node_modules',
      'dist',
      'target',
      '**/*.integration.test.js'
    ],
    include: [
      'src/test/javascript/**/*.test.js',
      '!src/test/javascript/**/*.integration.test.js'
    ]
  }
});