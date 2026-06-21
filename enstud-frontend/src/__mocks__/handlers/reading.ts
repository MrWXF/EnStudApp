import { http, HttpResponse } from 'msw'

export const readingHandlers = [
  http.get('/api/read/hot', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [
        { id: 1, title: 'The Future of AI', url: 'https://example.com/ai', source: 'HN', sourceIcon: '🔶', summary: 'AI is transforming every industry...', score: 120, author: 'John Doe', publishedAt: new Date().toISOString(), bookmarked: false },
        { id: 2, title: 'TypeScript 5.0 Released', url: 'https://example.com/ts5', source: 'GitHub', sourceIcon: '🐙', summary: 'Major new features in TypeScript...', score: 95, author: 'MS', publishedAt: new Date().toISOString(), bookmarked: true },
      ],
    })
  }),

  http.get('/api/read/:id', ({ params }) => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: {
        id: Number(params.id), title: 'The Future of AI', url: 'https://example.com/ai', source: 'HN', sourceIcon: '🔶',
        content: 'Full article content about AI...', summary: 'AI is transforming every industry...', score: 120, sourceScore: 100, author: 'John Doe',
        publishedAt: new Date().toISOString(),
      },
    })
  }),

  http.post('/api/read/:id/bookmark', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: true })
  }),

  http.get('/api/read/bookmarks', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: [] })
  }),

  http.get('/api/read/sources', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [
        { id: 'HN', name: 'Hacker News', icon: '🔶', activeCount: 10 },
        { id: 'GitHub', name: 'GitHub Trending', icon: '🐙', activeCount: 5 },
      ],
    })
  }),
]
