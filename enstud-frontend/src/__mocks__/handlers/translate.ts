import { http, HttpResponse } from 'msw'

export const translateHandlers = [
  http.post('/api/translate/text', async ({ request }) => {
    const body = (await request.json()) as { text: string; from?: string; to: string }
    const sourceLang = body.from || 'auto'
    const targetLang = body.to || 'en'

    let translatedText: string
    if (body.text.toLowerCase().includes('hello')) {
      translatedText = targetLang === 'zh' ? '你好，你好吗？' : 'Hello, how are you?'
    } else if (body.text.includes('你好')) {
      translatedText = targetLang === 'en' ? 'Hello, how are you?' : '你好，你好吗？'
    } else {
      translatedText = targetLang === 'zh' ? `（翻译结果：${body.text}）` : `(Translation: ${body.text})`
    }

    return HttpResponse.json({
      code: 0, msg: 'success',
      data: { sourceText: body.text, translatedText, from: sourceLang, to: targetLang },
    })
  }),
]
