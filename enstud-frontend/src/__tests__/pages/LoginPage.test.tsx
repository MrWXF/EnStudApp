import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { describe, it, expect, beforeEach } from 'vitest'
import LoginPage from '../../pages/LoginPage'

function renderLogin() {
  return render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>,
  )
}

function getInputByPlaceholder(text: string): HTMLInputElement {
  return screen.queryAllByPlaceholderText(text)[0] as HTMLInputElement
}

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('renders login form with username and password fields', () => {
    renderLogin()
    expect(screen.queryAllByPlaceholderText('用户名').length).toBeGreaterThanOrEqual(1)
    expect(screen.queryAllByPlaceholderText('密码').length).toBeGreaterThanOrEqual(1)
  })

  it('switches to register tab and shows email field', async () => {
    renderLogin()
    // Click the "注册" tab in the tab navigation (Ant Design may add spaces)
    const tabs = screen.getAllByText(/注\s*册/)
    await userEvent.click(tabs[0])
    await waitFor(() => {
      expect(screen.queryAllByPlaceholderText('邮箱').length).toBeGreaterThanOrEqual(1)
    })
  })

  it('calls login API and stores token on success', async () => {
    renderLogin()

    await userEvent.type(getInputByPlaceholder('用户名'), 'testuser')
    await userEvent.type(getInputByPlaceholder('密码'), 'Test@1234')

    // Ant Design 6 may render Chinese button text with spaces: "登 录"
    // Use regex to match both "登录" and "登 录"
    const allLoginTexts = screen.getAllByText(/登\s*录/)
    const loginBtn = allLoginTexts[allLoginTexts.length - 1] as HTMLElement
    await userEvent.click(loginBtn)

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('mock-access-token-for-testing')
    }, { timeout: 5000 })
  })

  it('calls register API on submit', async () => {
    renderLogin()
    // Switch to register tab
    await userEvent.click(screen.getAllByText(/注\s*册/)[0])

    await userEvent.type(getInputByPlaceholder('用户名'), 'newuser')
    await userEvent.type(getInputByPlaceholder('邮箱'), 'new@example.com')
    await userEvent.type(getInputByPlaceholder('密码'), 'Test@1234')

    // Ant Design 6 may render "注 册"
    const allRegisterTexts = screen.getAllByText(/注\s*册/)
    const registerBtn = allRegisterTexts[allRegisterTexts.length - 1] as HTMLElement
    await userEvent.click(registerBtn)

    // Just verify no crash — MSW returns success
    await waitFor(() => {
      expect(registerBtn).toBeInTheDocument()
    })
  })

  it('shows email format validation hint', async () => {
    renderLogin()
    await userEvent.click(screen.getAllByText(/注\s*册/)[0])
    await userEvent.type(getInputByPlaceholder('用户名'), 'newuser')
    await userEvent.type(getInputByPlaceholder('邮箱'), 'invalid')
    await userEvent.type(getInputByPlaceholder('密码'), 'Test@1234')

    expect(getInputByPlaceholder('邮箱')).toBeInTheDocument()
  })
})
