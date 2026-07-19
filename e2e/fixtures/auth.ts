import { Page } from '@playwright/test'

const TEST_USER = {
  username: 'demouser',
  password: 'Demo@123',
  email: 'demo@test.com',
}

/**
 * 确保测试用户存在并登录，将 token 写入 localStorage。
 * 必须先在当前页面 navigate 到同源页面后才能写 localStorage。
 */
export async function loginViaApi(page: Page, gotoAfterLogin?: string) {
  // 先导航到登录页（确保同源，localStorage 可写）
  await page.goto('/login')

  // 尝试登录
  let response = await page.request.post('/api/user/login', {
    headers: { 'Content-Type': 'application/json' },
    data: { username: TEST_USER.username, password: TEST_USER.password },
  })
  let body = await response.json()

  // 用户不存在则先注册
  if (body.code === 1001) {
    await page.request.post('/api/user/register', {
      headers: { 'Content-Type': 'application/json' },
      data: {
        username: TEST_USER.username,
        password: TEST_USER.password,
        email: TEST_USER.email,
        nickname: 'DemoUser',
      },
    })
    response = await page.request.post('/api/user/login', {
      headers: { 'Content-Type': 'application/json' },
      data: { username: TEST_USER.username, password: TEST_USER.password },
    })
    body = await response.json()
  }

  const token = body.data?.accessToken
  if (!token) throw new Error(`Login failed: ${JSON.stringify(body)}`)

  // page 已在 /login 页面，localStorage 可写
  await page.evaluate(({ token, username, userId }) => {
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify({ username, nickname: 'DemoUser', userId }))
  }, { token, username: TEST_USER.username, userId: body.data.userId })

  if (gotoAfterLogin) {
    await page.goto(gotoAfterLogin)
  }
}

/**
 * 注册新用户
 */
export async function registerViaApi(page: Page, username: string, password: string, email: string) {
  const response = await page.request.post('/api/user/register', {
    headers: { 'Content-Type': 'application/json' },
    data: { username, password, email, nickname: username },
  })
  return await response.json()
}
