import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('Word Learning', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaApi(page)
    await page.goto('/word')
  })

  test('word page shows wordbook selector', async ({ page }) => {
    await expect(page.getByText('单词学习')).toBeVisible()
    await expect(page.getByText('选择词库')).toBeVisible()
  })

  test('select wordbook and start learning flow', async ({ page }) => {
    // Click the select to open dropdown
    await page.locator('.ant-select-selector').first().click()
    // Select first wordbook option
    await page.locator('.ant-select-item-option').first().click()
    // Click "开始学习"
    await page.getByText(/开始学习/).click()

    // Wait for word card to appear
    await expect(page.getByText(/./)).toBeVisible()
  })
})
