import { http, HttpResponse } from 'msw'

export const authHandlers = [
  http.post('/api/user/register', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: null })
  }),

  http.post('/api/user/login', async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string }

    if (body.username === 'duplicate') {
      return HttpResponse.json({ code: 1002, msg: '用户名已存在', data: null })
    }

    return HttpResponse.json({
      code: 0,
      msg: 'success',
      data: {
        accessToken: 'mock-access-token-for-testing',
        refreshToken: 'mock-refresh-token',
        userId: 1,
        username: body.username,
        nickname: 'TestUser',
      },
    })
  }),

  http.get('/api/user/profile', ({ request }) => {
    const userId = request.headers.get('X-User-Id')
    if (!userId) {
      return HttpResponse.json({ code: 401, msg: '未登录', data: null })
    }
    return HttpResponse.json({
      code: 0,
      msg: 'success',
      data: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        avatarUrl: null,
        level: 1,
        points: 100,
        createdAt: '2025-01-01T00:00:00',
      },
    })
  }),
]
