import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import TranslatePage from '../../pages/TranslatePage'

function renderTranslate() {
  return render(<BrowserRouter><TranslatePage /></BrowserRouter>)
}

describe('TranslatePage', () => {
  it('renders translate form with text areas', () => {
    renderTranslate()
    // Card title
    expect(screen.getByText('翻译工具')).toBeInTheDocument()
    // TextArea inputs
    expect(screen.getByPlaceholderText('输入要翻译的文本...')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('翻译结果...')).toBeInTheDocument()
  })

  it('translates text when clicking translate button', async () => {
    renderTranslate()
    await userEvent.type(screen.getByPlaceholderText('输入要翻译的文本...'), 'hello')

    // Ant Design 6 inserts spaces: "翻 译". Use exact /^翻\s*译$/ to avoid matching "翻译工具"
    const translateBtn = screen.getByText(/^翻\s*译$/)
    await userEvent.click(translateBtn)

    await waitFor(() => {
      const result = screen.getByPlaceholderText('翻译结果...') as HTMLTextAreaElement
      expect(result.value).toBeTruthy()
    })
  })
})
