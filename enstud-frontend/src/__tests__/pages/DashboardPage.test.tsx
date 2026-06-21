import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect } from 'vitest'
import DashboardPage from '../../pages/DashboardPage'

function renderDashboard() {
  return render(<BrowserRouter><DashboardPage /></BrowserRouter>)
}

describe('DashboardPage', () => {
  it('renders learning stats after API load', async () => {
    renderDashboard()
    await waitFor(() => {
      expect(screen.getByText('学习仪表盘')).toBeInTheDocument()
    })
    // Ant Design Statistic renders value as text in the component
    expect(screen.getByText('15')).toBeInTheDocument() // todayLearnedWords
    expect(screen.getByText('250')).toBeInTheDocument() // totalLearnedWords
  })

  it('displays memory level distribution labels', async () => {
    renderDashboard()
    await waitFor(() => {
      expect(screen.getByText('单词记忆等级分布')).toBeInTheDocument()
    })
    expect(screen.getByText('未学习')).toBeInTheDocument()
    expect(screen.getByText('模糊')).toBeInTheDocument()
    expect(screen.getByText('精通')).toBeInTheDocument()
  })

  it('renders streak days indicator', async () => {
    renderDashboard()
    await waitFor(() => {
      expect(screen.getByText('8')).toBeInTheDocument() // streakDays
      expect(screen.getByText('连续学习')).toBeInTheDocument()
    })
  })
})
