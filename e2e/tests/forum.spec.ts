import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('Forum', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaApi(page)
    await page.goto('/forum')
  })

  test('forum page shows category buttons and posts', async ({ page }) => {
    await expect(page.getByText('学习论坛')).toBeVisible()
    // Category buttons should appear
    await expect(page.getByText(/学习经验/).first()).toBeVisible()
  })

  test('create a new post', async ({ page }) => {
    // Click "发帖" button
    await page.getByText(/发帖/).click()
    await expect(page).toHaveURL(/\/forum\/new/)

    // Fill in post form
    await page.getByPlaceholder(/标题/).first().fill(`E2E Test Post ${Date.now()}`)
    await page.getByPlaceholder(/内容/).first().fill('This is an E2E test post content.')
    await page.getByText(/发布/).click()

    // Should navigate back to forum or post detail
    await page.waitForTimeout(2000)
  })
})
