import { Page } from '@playwright/test'

/**
 * 登录辅助函数：通过 API 直接登录并将 token 写入 localStorage
 * 比 UI 登录更快更稳定
 */
export async function loginViaApi(page: Page, username = 'testuser', password = 'Test@1234') {
  const response = await page.request.post('/api/user/login', {
    data: { username, password },
  })
  const body = await response.json()
  const token = body.data?.accessToken
  if (!token) throw new Error(`Login failed: ${JSON.stringify(body)}`)

  // 将 token 和 user 信息写入 localStorage
  await page.evaluate(({ token, username }) => {
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify({ username, nickname: 'TestUser', userId: 1 }))
  }, { token, username })
}

/**
 * 注册新用户
 */
export async function registerViaApi(page: Page, username: string, password: string, email: string) {
  const response = await page.request.post('/api/user/register', {
    data: { username, password, email },
  })
  return await response.json()
}
