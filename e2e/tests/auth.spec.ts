import { test, expect } from '@playwright/test'
import { loginViaApi, registerViaApi } from '../fixtures/auth'

test.describe('Authentication', () => {
  test('login page renders login form', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByPlaceholder('用户名').first()).toBeVisible()
    await expect(page.getByPlaceholder('密码').first()).toBeVisible()
  })

  test('register via API creates new user', async ({ page }) => {
    const result = await registerViaApi(page, `e2euser_${Date.now()}`, 'Test@1234', `e2e_${Date.now()}@example.com`)
    expect(result.code).toBe(0)
  })

  test('duplicate username registration is rejected', async ({ page }) => {
    const username = `duptest_${Date.now()}`
    // First registration should succeed
    await registerViaApi(page, username, 'Test@1234', `${username}@example.com`)
    // Duplicate should fail
    const result = await registerViaApi(page, username, 'Test@1234', `other_${username}@example.com`)
    expect(result.code).toBe(1002)
  })

  test('protected route redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/word')
    await page.waitForURL('**/login**')
    expect(page.url()).toContain('/login')
  })
})
