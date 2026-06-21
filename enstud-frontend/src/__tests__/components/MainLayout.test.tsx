import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect, beforeEach } from 'vitest'
import MainLayout from '../../components/MainLayout'

function renderLayout() {
  return render(
    <BrowserRouter>
      <MainLayout />
    </BrowserRouter>,
  )
}

describe('MainLayout', () => {
  beforeEach(() => {
    localStorage.setItem('user', JSON.stringify({ username: 'testuser', nickname: 'TestUser', userId: 1 }))
    localStorage.setItem('token', 'mock-token')
  })

  it('renders navigation menu items', () => {
    renderLayout()
    expect(screen.getByText('EnStudApp')).toBeInTheDocument()
    expect(screen.getByText('首页')).toBeInTheDocument()
    expect(screen.getByText('单词学习')).toBeInTheDocument()
    expect(screen.getByText('AI 对话')).toBeInTheDocument()
    expect(screen.getByText('写作练习')).toBeInTheDocument()
    expect(screen.getByText('翻译工具')).toBeInTheDocument()
    expect(screen.getByText('学习论坛')).toBeInTheDocument()
  })

  it('shows current user display name', () => {
    renderLayout()
    expect(screen.getByText('TestUser')).toBeInTheDocument()
  })
})
