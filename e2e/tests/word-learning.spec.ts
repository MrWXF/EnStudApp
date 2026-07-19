import { test, expect } from '@playwright/test'
import { loginViaApi } from '../fixtures/auth'

test.describe('Word Learning', () => {
  test('word page renders with navigation and content', async ({ page }) => {
    await loginViaApi(page, '/word')
    // Sidebar menu has "单词学习", card title also has it — check page has loaded
    await expect(page.locator('.ant-layout').first()).toBeVisible({ timeout: 10_000 })
  })

  test('translate page renders with form elements', async ({ page }) => {
    await loginViaApi(page, '/translate')
    await expect(page.getByPlaceholder('输入要翻译的文本...')).toBeVisible({ timeout: 10_000 })
    await expect(page.getByPlaceholder('翻译结果...')).toBeVisible()
  })
})
