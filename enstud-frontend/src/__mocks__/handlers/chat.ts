import { http, HttpResponse } from 'msw'

export const chatHandlers = [
  http.post('/api/chat/sessions', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: { id: 1, title: '新对话', scenario: 'FREE', createdAt: new Date().toISOString(), messageCount: 0 },
    })
  }),

  http.get('/api/chat/sessions', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [{ id: 1, title: '日常聊天', scenario: 'FREE', createdAt: new Date().toISOString(), messageCount: 3 }],
    })
  }),

  http.post('/api/chat/sessions/:id/messages', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: {
        userMessage: { id: 1, role: 'USER', content: 'Hello', createdAt: new Date().toISOString() },
        aiMessage: { id: 2, role: 'AI', content: 'Hi there! How can I help you practice English today?', createdAt: new Date().toISOString() },
        grammarIssues: [],
      },
    })
  }),

  http.get('/api/chat/sessions/:id/messages', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [
        { id: 1, role: 'USER', content: 'Hello', createdAt: new Date().toISOString() },
        { id: 2, role: 'AI', content: 'Hi there! How can I help you practice English today?', createdAt: new Date().toISOString() },
      ],
    })
  }),

  http.delete('/api/chat/sessions/:id', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: null })
  }),
]
