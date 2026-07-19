import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('Forum', () => {
  test('forum page loads and shows UI elements', async ({ page }) => {
    await loginViaApi(page, '/forum')
    await expect(page.getByText(/论坛/).first()).toBeVisible({ timeout: 10_000 })
  })
})
