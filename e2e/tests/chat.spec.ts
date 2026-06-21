import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('AI Chat', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaApi(page)
    await page.goto('/chat')
  })

  test('chat page renders new session UI', async ({ page }) => {
    await expect(page.getByText(/对话/).first()).toBeVisible()
  })
})
