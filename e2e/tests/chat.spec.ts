import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('AI Chat', () => {
  test('chat page renders after login', async ({ page }) => {
    await loginViaApi(page, '/chat')
    await expect(page.getByText(/对话/).first()).toBeVisible({ timeout: 10_000 })
  })
})
