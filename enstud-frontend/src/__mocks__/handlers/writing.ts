import { http, HttpResponse } from 'msw'

export const writingHandlers = [
  http.post('/api/writing/submit', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: {
        writingId: 1,
        score: 85,
        overallComment: 'Good job! Pay attention to verb tenses.',
        items: [
          { type: 'GRAMMAR', original: 'I go to school yesterday', suggestion: 'I went to school yesterday', explanation: 'Use past tense for yesterday.' },
        ],
      },
    })
  }),

  http.get('/api/writing/history', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [{ id: 1, title: 'My Day', content: 'I went to school...', wordCount: 50, topicType: 'ESSAY', score: 85, createdAt: new Date().toISOString() }],
    })
  }),

  http.get('/api/writing/models', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: [
        { title: 'Sample Essay', content: 'Model content here...', topicType: 'ESSAY', analysis: 'Well structured.' },
      ],
    })
  }),
]
