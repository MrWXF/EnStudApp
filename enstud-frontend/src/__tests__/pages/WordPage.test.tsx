import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import WordPage from '../../pages/WordPage'

function renderWord() {
  return render(<BrowserRouter><WordPage /></BrowserRouter>)
}

/** Helper: select an Ant Design Select option by clicking the selector div */
async function selectAntdOption(placeholderText: string, optionText: string) {
  // Find the placeholder span, then click its parent selector (which is clickable)
  const placeholder = screen.getByText(placeholderText)
  const selectTrigger = placeholder.closest('.ant-select-selector') || placeholder.parentElement!
  await userEvent.click(selectTrigger)
  // Options appear in the dropdown (portal), find and click the desired one
  const option = await screen.findByText(optionText)
  await userEvent.click(option)
}

describe('WordPage', () => {
  it('loads and displays wordbook options in select dropdown', async () => {
    renderWord()
    // Ant Design Select renders the placeholder as text inside the trigger area
    await waitFor(() => {
      expect(screen.getByText('选择词库')).toBeInTheDocument()
    })
  })

  it('starts study and renders word card', async () => {
    renderWord()
    // Wait for wordbook data to load
    await waitFor(() => expect(screen.getByText('选择词库')).toBeInTheDocument())

    // Select CET-4 wordbook
    await selectAntdOption('选择词库', 'CET-4 核心词汇 (200词)')

    // Click "开始学习"
    const startBtn = screen.getByText('开始学习')
    await userEvent.click(startBtn)

    // Should show the first word
    await waitFor(() => {
      expect(screen.getByText('abandon')).toBeInTheDocument()
      expect(screen.getByText('显示释义')).toBeInTheDocument()
    })
  })

  it('shows definition and review buttons after clicking show answer', async () => {
    renderWord()
    await waitFor(() => expect(screen.getByText('选择词库')).toBeInTheDocument())
    await selectAntdOption('选择词库', 'CET-4 核心词汇 (200词)')
    await userEvent.click(screen.getByText('开始学习'))

    await screen.findByText('abandon')
    await userEvent.click(screen.getByText('显示释义'))

    await waitFor(() => {
      expect(screen.getByText('放弃，抛弃')).toBeInTheDocument()
      expect(screen.getByText('忘记')).toBeInTheDocument()
      expect(screen.getByText('记得')).toBeInTheDocument()
      expect(screen.getByText('熟练')).toBeInTheDocument()
    })
  })

  it('moves to next word after submitting review', async () => {
    renderWord()
    await waitFor(() => expect(screen.getByText('选择词库')).toBeInTheDocument())
    await selectAntdOption('选择词库', 'CET-4 核心词汇 (200词)')
    await userEvent.click(screen.getByText('开始学习'))

    await screen.findByText('abandon')
    await userEvent.click(screen.getByText('显示释义'))
    await userEvent.click(screen.getByText('熟练'))

    // Should move to next word
    await waitFor(() => {
      expect(screen.getByText('brilliant')).toBeInTheDocument()
    })
  })
})
