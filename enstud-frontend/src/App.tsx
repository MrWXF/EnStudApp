import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import MainLayout from './components/MainLayout';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import WordPage from './pages/WordPage';
import ChatPage from './pages/ChatPage';
import WritingPage from './pages/WritingPage';
import TranslatePage from './pages/TranslatePage';
import ForumPage from './pages/ForumPage';
import ForumDetailPage from './pages/ForumDetailPage';
import CreatePostPage from './pages/CreatePostPage';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/login" />;
}

export default function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{ token: { colorPrimary: '#1677ff', borderRadius: 8 } }}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
            <Route index element={<HomePage />} />
            <Route path="word" element={<WordPage />} />
            <Route path="chat" element={<ChatPage />} />
            <Route path="writing" element={<WritingPage />} />
            <Route path="translate" element={<TranslatePage />} />
            <Route path="forum" element={<ForumPage />} />
            <Route path="forum/:id" element={<ForumDetailPage />} />
            <Route path="forum/new" element={<CreatePostPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}
