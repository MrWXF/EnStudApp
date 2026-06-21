import { http, HttpResponse } from 'msw'

export const wordHandlers = [
  http.get('/api/word/wordbooks', () => {
    return HttpResponse.json({
      code: 0,
      msg: 'success',
      data: [
        { id: 1, name: 'CET-4 核心词汇', description: '大学英语四级', wordCount: 200, category: 'CET4', difficulty: 1, isOfficial: true },
        { id: 2, name: 'CET-6 进阶词汇', description: '大学英语六级', wordCount: 300, category: 'CET6', difficulty: 2, isOfficial: true },
      ],
    })
  }),

  http.post('/api/word/study', () => {
    return HttpResponse.json({
      code: 0,
      msg: 'success',
      data: [
        { id: 1, word: 'abandon', phoneticUs: '/əˈbændən/', definitionCn: '放弃，抛弃', definitionEn: 'to leave someone forever', exampleSentence: 'They had to abandon the sinking ship.', partOfSpeech: 'v', masteryLevel: 0, status: 'NEW' },
        { id: 2, word: 'brilliant', phoneticUs: '/ˈbrɪliənt/', definitionCn: '杰出的，明亮的', exampleSentence: 'She has a brilliant future.', partOfSpeech: 'adj', masteryLevel: 0, status: 'NEW' },
        { id: 3, word: 'curious', phoneticUs: '/ˈkjʊəriəs/', definitionCn: '好奇的', exampleSentence: 'The curious child asked many questions.', partOfSpeech: 'adj', masteryLevel: 0, status: 'NEW' },
      ],
    })
  }),

  http.post('/api/word/review', () => {
    return HttpResponse.json({ code: 0, msg: 'success', data: null })
  }),

  http.get('/api/word/memory-level/distribution', () => {
    return HttpResponse.json({
      code: 0,
      msg: 'success',
      data: { notLearned: 50, fuzzy: 30, familiar: 20, basic: 15, proficient: 10, mastered: 5 },
    })
  }),
]
