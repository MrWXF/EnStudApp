import { http, HttpResponse } from 'msw'

export const dashboardHandlers = [
  http.get('/api/user/stats', () => {
    return HttpResponse.json({
      code: 0, msg: 'success',
      data: {
        todayLearnedWords: 15,
        totalLearnedWords: 250,
        totalReadArticles: 12,
        totalWritings: 5,
        avgWritingScore: 4.2,
        totalChats: 20,
        totalPosts: 3,
        streakDays: 8,
        memoryLevelDistribution: {
          notLearned: 50, fuzzy: 60, familiar: 45, basic: 40, proficient: 35, mastered: 20,
        },
      },
    })
  }),
]
