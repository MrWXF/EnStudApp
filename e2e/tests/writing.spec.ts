import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('Writing', () => {
  test('writing page renders after login', async ({ page }) => {
    await loginViaApi(page, '/writing')
    await expect(page.getByText(/写作/).first()).toBeVisible({ timeout: 10_000 })
  })
})
