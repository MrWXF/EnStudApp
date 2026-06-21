import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import ForumPage from '../../pages/ForumPage'

function renderForum() {
  return render(<BrowserRouter><ForumPage /></BrowserRouter>)
}

describe('ForumPage', () => {
  it('loads and displays categories and posts', async () => {
    renderForum()
    await waitFor(() => {
      expect(screen.queryAllByText('学习经验').length).toBeGreaterThanOrEqual(1)
      expect(screen.getByText('问答求助')).toBeInTheDocument()
      expect(screen.getByText('如何快速背单词？')).toBeInTheDocument()
    })
  })

  it('shows "全部" filter button', async () => {
    renderForum()
    await waitFor(() => {
      // Ant Design 6 may add spaces: "全 部"
      expect(screen.getByText(/全\s*部/)).toBeInTheDocument()
    })
  })

  it('filters posts via search input', async () => {
    renderForum()
    await waitFor(() => expect(screen.getByText('如何快速背单词？')).toBeInTheDocument())
    await userEvent.type(screen.getByPlaceholderText('搜索帖子...'), '语法书')
    await waitFor(() => {
      expect(screen.getByText('推荐一本英语语法书')).toBeInTheDocument()
      expect(screen.queryByText('如何快速背单词？')).not.toBeInTheDocument()
    })
  })

  it('shows create post button', async () => {
    renderForum()
    await waitFor(() => expect(screen.getByText('发帖')).toBeInTheDocument())
  })
})
