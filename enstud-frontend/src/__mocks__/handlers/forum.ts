import { http, HttpResponse } from 'msw'

export const forumHandlers = [
  http.get('/api/forum/categories', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [
        { id: 1, name: '学习经验', description: '分享学习方法', icon: '📚' },
        { id: 2, name: '问答求助', description: '问题交流', icon: '❓' },
        { id: 3, name: '英语角', description: '自由讨论', icon: '💬' },
      ],
    })
  }),

  http.get('/api/forum/posts', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [
        { id: 1, title: '如何快速背单词？', summary: '分享一些高效的单词记忆方法...', authorName: 'UserA', authorId: 1, categoryId: 1, categoryName: '学习经验', replyCount: 5, likeCount: 12, viewCount: 100, isPinned: false, isEssence: true, tags: '单词,方法', createdAt: new Date().toISOString() },
        { id: 2, title: '推荐一本英语语法书', summary: '最近发现了一本很好的语法书...', authorName: 'UserB', authorId: 2, categoryId: 1, categoryName: '学习经验', replyCount: 3, likeCount: 8, viewCount: 50, isPinned: false, isEssence: false, createdAt: new Date().toISOString() },
      ],
    })
  }),

  http.get('/api/forum/posts/:id', ({ params }) => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: {
        id: Number(params.id), title: '如何快速背单词？', content: '背单词是英语学习的基础...', authorName: 'UserA', authorId: 1, categoryId: 1, categoryName: '学习经验',
        likeCount: 12, replyCount: 1, viewCount: 100, isPinned: false, isEssence: true,
        replies: [{ id: 1, authorName: 'UserB', authorId: 2, content: '很好的方法！', likeCount: 2, createdAt: new Date().toISOString() }],
        createdAt: new Date().toISOString(),
      },
    })
  }),

  http.post('/api/forum/posts', async ({ request }) => {
    const body = (await request.json()) as { title: string; content: string }
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: { id: 3, title: body.title, summary: body.content.slice(0, 50), authorName: 'TestUser', authorId: 1, categoryId: 1, categoryName: '学习经验', replyCount: 0, likeCount: 0, viewCount: 0, isPinned: false, isEssence: false, createdAt: new Date().toISOString() },
    })
  }),

  http.post('/api/forum/posts/:id/reply', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: { id: 1, content: 'Reply content', authorName: 'TestUser', authorId: 1, createdAt: new Date().toISOString() } })
  }),

  http.post('/api/forum/like', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: null })
  }),

  http.delete('/api/forum/posts/:id', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: null })
  }),
]
