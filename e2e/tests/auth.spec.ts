import { test, expect } from '@playwright/test'
import { loginViaApi, registerViaApi } from '../fixtures/auth'

test.describe('Authentication', () => {
  test('login page renders login form', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByPlaceholder('用户名').first()).toBeVisible()
    await expect(page.getByPlaceholder('密码').first()).toBeVisible()
  })

  test('register via API creates new user', async ({ page }) => {
    const ts = Date.now()
    const result = await registerViaApi(page, `e2e_${ts}`, 'Test@1234', `e2e_${ts}@test.com`)
    expect(result.code).toBe(0)
  })

  test('duplicate username registration is rejected', async ({ page }) => {
    const ts = Date.now()
    const username = `dupe2e_${ts}`
    await registerViaApi(page, username, 'Test@1234', `${username}@test.com`)
    const result = await registerViaApi(page, username, 'Test@1234', `other_${ts}@test.com`)
    expect(result.code).toBe(1002)
  })

  test('login via API — token persists across page navigation', async ({ page }) => {
    await loginViaApi(page, '/word')
    await page.waitForTimeout(2000)
    // Should stay on word page (not redirect to login)
    expect(page.url()).toContain('/word')
  })

  test('protected route redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/word')
    await page.waitForURL('**/login**')
    expect(page.url()).toContain('/login')
  })
})
