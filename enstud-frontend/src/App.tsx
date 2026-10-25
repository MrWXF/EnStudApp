import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import MainLayout from './components/MainLayout';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import DashboardPage from './pages/DashboardPage';
import WordPage from './pages/WordPage';
import ChatPage from './pages/ChatPage';
import WritingPage from './pages/WritingPage';
import TranslatePage from './pages/TranslatePage';
import ForumPage from './pages/ForumPage';
import ForumDetailPage from './pages/ForumDetailPage';
import CreatePostPage from './pages/CreatePostPage';
import ReadPage from './pages/ReadPage';
import ReadDetailPage from './pages/ReadDetailPage';
import { useAuthStore } from './stores';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  // 兼容：store 无数据时回退检查 localStorage
  const hasToken = isAuthenticated || !!localStorage.getItem('token');
  return hasToken ? <>{children}</> : <Navigate to="/login" />;
}

export default function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#1677ff', borderRadius: 8 } }}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
            <Route index element={<HomePage />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="word" element={<WordPage />} />
            <Route path="chat" element={<ChatPage />} />
            <Route path="writing" element={<WritingPage />} />
            <Route path="translate" element={<TranslatePage />} />
            <Route path="forum" element={<ForumPage />} />
            <Route path="forum/:id" element={<ForumDetailPage />} />
            <Route path="forum/new" element={<CreatePostPage />} />
            <Route path="read" element={<ReadPage />} />
            <Route path="read/:id" element={<ReadDetailPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}
