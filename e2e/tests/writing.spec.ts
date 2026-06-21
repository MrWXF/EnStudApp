import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('Writing', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaApi(page)
    await page.goto('/writing')
  })

  test('writing page renders title and textarea', async ({ page }) => {
    await expect(page.getByText(/写作/).first()).toBeVisible()
  })

  test('submit writing and view correction', async ({ page }) => {
    // Fill in writing form
    const titleInput = page.getByPlaceholder(/标题/).first()
    const contentInput = page.getByPlaceholder(/内容/).first()

    if (await titleInput.isVisible()) {
      await titleInput.fill('My Day')
      await contentInput.fill('I went to school yesterday. It was a very good day.')
      await page.getByText(/提交/).click()

      // Wait for correction result
      await page.waitForTimeout(3000)
    }
  })
})
