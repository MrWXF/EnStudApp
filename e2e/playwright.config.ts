import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  retries: 0,
  reporter: [['html'], ['list']],
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  // Backend and frontend must be started externally:
  // 1. docker-compose -f e2e/docker-compose.e2e.yml up -d
  // 2. Initialize DB: mysql ... < sql/init/*.sql
  // 3. Start all microservices (see scripts/start-backend.sh)
  // 4. cd enstud-frontend && npm run dev
  // 5. cd e2e && npx playwright test
})
