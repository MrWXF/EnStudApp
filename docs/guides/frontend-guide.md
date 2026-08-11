# 前端开发指南

> EnStudApp 前端开发规范与实践指南

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| React | 19.x | UI 框架 |
| TypeScript | 6.x | 类型系统 |
| Vite | 8.x | 构建工具 |
| Ant Design | 6.x | UI 组件库 |
| React Router | 7.x | 路由管理 |
| Axios | 1.x | HTTP 客户端 |
| Vitest | - | 单元测试 |

## 目录结构

```
enstud-frontend/
├── src/
│   ├── api/              # API 请求封装（按模块拆分）
│   │   ├── auth.ts       # 认证相关
│   │   ├── user.ts       # 用户相关
│   │   ├── word.ts       # 单词相关
│   │   ├── chat.ts       # 聊天相关
│   │   ├── writing.ts    # 写作相关
│   │   ├── translate.ts  # 翻译相关
│   │   ├── forum.ts      # 论坛相关
│   │   ├── reading.ts    # 阅读相关
│   │   ├── dashboard.ts  # 仪表盘相关
│   │   ├── client.ts     # Axios 实例配置
│   │   └── index.ts      # 统一导出
│   │
│   ├── pages/            # 页面组件
│   │   ├── HomePage.tsx           # 首页
│   │   ├── LoginPage.tsx          # 登录页
│   │   ├── DashboardPage.tsx      # 仪表盘
│   │   ├── WordPage.tsx           # 单词学习
│   │   ├── ChatPage.tsx           # AI 聊天
│   │   ├── WritingPage.tsx        # 写作批改
│   │   ├── TranslatePage.tsx      # 翻译
│   │   ├── ReadPage.tsx           # 阅读列表
│   │   ├── ReadDetailPage.tsx     # 阅读详情
│   │   ├── ForumPage.tsx          # 论坛列表
│   │   ├── ForumDetailPage.tsx    # 帖子详情
│   │   └── CreatePostPage.tsx     # 发布帖子
│   │
│   ├── components/       # 通用组件
│   │   ├── MainLayout.tsx         # 主布局
│   │   └── ...
│   │
│   ├── types/            # TypeScript 类型定义
│   │   ├── index.ts
│   │   └── ...
│   │
│   ├── utils/            # 工具函数
│   │   ├── storage.ts    # LocalStorage 封装
│   │   ├── format.ts     # 格式化工具
│   │   └── ...
│   │
│   ├── assets/           # 静态资源
│   │   ├── images/
│   │   └── styles/
│   │
│   ├── App.tsx           # 根组件
│   └── main.tsx          # 入口文件
│
├── public/               # 公共静态资源
├── index.html            # HTML 模板
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
└── package.json          # 依赖配置
```

## 快速开始

### 安装依赖

```bash
cd enstud-frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:5173

### 构建生产版本

```bash
npm run build
```

构建产物在 `dist/` 目录

### 代码检查

```bash
# ESLint 检查
npm run lint

# 类型检查（build 时自动执行）
npx tsc --noEmit
```

### 运行测试

```bash
# 运行所有测试
npm run test

# 监听模式
npm run test:watch
```

## 开发规范

### 1. 禁止使用 `any` 类型

```typescript
// ❌ 错误
const data: any = response.data;

// ✅ 正确
interface UserData {
  id: number;
  username: string;
  email: string;
}
const data: UserData = response.data;
```

### 2. 组件命名

- 使用 PascalCase（大驼峰）
- 文件名与组件名一致

```typescript
// ✅ 正确
// WordPage.tsx
export default function WordPage() {
  return <div>...</div>;
}

// ❌ 错误
// wordPage.tsx
export default function wordPage() {
  return <div>...</div>;
}
```

### 3. Props 类型定义

```typescript
// ✅ 使用 interface
interface ButtonProps {
  text: string;
  onClick: () => void;
  disabled?: boolean;
}

export default function Button({ text, onClick, disabled = false }: ButtonProps) {
  return <button onClick={onClick} disabled={disabled}>{text}</button>;
}
```

### 4. 使用函数组件和 Hooks

```typescript
// ✅ 推荐
import { useState, useEffect } from 'react';

export default function WordList() {
  const [words, setWords] = useState<Word[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // 加载数据
  }, []);

  return <div>...</div>;
}

// ❌ 避免使用 Class 组件
```

### 5. API 请求封装

在 `src/api/` 目录下按模块创建 API 文件：

```typescript
// src/api/word.ts
import client from './client';
import type { Word, WordListResponse } from '../types';

/**
 * 获取单词列表
 */
export const getWordList = async (bookId: number, limit = 10): Promise<WordListResponse> => {
  const response = await client.get('/api/word/list', {
    params: { bookId, limit },
  });
  return response.data;
};

/**
 * 提交学习记录
 */
export const submitLearnRecord = async (wordId: number, result: 'CORRECT' | 'WRONG') => {
  const response = await client.post('/api/word/learn', {
    wordId,
    result,
  });
  return response.data;
};
```

### 6. 统一错误处理

```typescript
// src/api/client.ts
import axios from 'axios';
import { message } from 'antd';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
});

// 请求拦截器
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
client.interceptors.response.use(
  (response) => {
    const { code, message: msg, data } = response.data;
    if (code !== 0) {
      message.error(msg || '请求失败');
      return Promise.reject(new Error(msg));
    }
    return data;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期，跳转登录
      localStorage.removeItem('token');
      window.location.href = '/login';
    } else {
      message.error(error.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export default client;
```

### 7. 路由配置

```typescript
// src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainLayout from './components/MainLayout';
import HomePage from './pages/HomePage';
import WordPage from './pages/WordPage';
import ChatPage from './pages/ChatPage';
import LoginPage from './pages/LoginPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<MainLayout />}>
          <Route index element={<HomePage />} />
          <Route path="word" element={<WordPage />} />
          <Route path="chat" element={<ChatPage />} />
          {/* 其他路由 */}
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

### 8. 使用 Ant Design 组件

```typescript
import { Button, Input, Table, message, Modal } from 'antd';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons';

export default function WordList() {
  const handleAdd = () => {
    message.success('添加成功');
  };

  return (
    <div>
      <Input
        prefix={<SearchOutlined />}
        placeholder="搜索单词"
      />
      <Button
        type="primary"
        icon={<PlusOutlined />}
        onClick={handleAdd}
      >
        添加单词
      </Button>
      <Table dataSource={[]} columns={[]} />
    </div>
  );
}
```

## 状态管理

优先使用 React 内置的状态管理方案：

### 局部状态

```typescript
const [count, setCount] = useState(0);
```

### 全局状态（Context）

```typescript
// src/contexts/UserContext.tsx
import { createContext, useContext, useState, ReactNode } from 'react';

interface User {
  id: number;
  username: string;
}

interface UserContextType {
  user: User | null;
  setUser: (user: User | null) => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  return (
    <UserContext.Provider value={{ user, setUser }}>
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser must be used within UserProvider');
  }
  return context;
}
```

## 样式规范

### 使用 CSS Modules

```typescript
// Button.module.css
.button {
  padding: 8px 16px;
  border-radius: 4px;
}

.primary {
  background-color: #1890ff;
  color: white;
}
```

```typescript
// Button.tsx
import styles from './Button.module.css';

export default function Button({ type = 'primary' }) {
  return (
    <button className={`${styles.button} ${styles[type]}`}>
      按钮
    </button>
  );
}
```

### 或使用内联样式

```typescript
const buttonStyle: React.CSSProperties = {
  padding: '8px 16px',
  borderRadius: '4px',
};
```

## 性能优化

### 1. 组件懒加载

```typescript
import { lazy, Suspense } from 'react';

const WordPage = lazy(() => import('./pages/WordPage'));

function App() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <WordPage />
    </Suspense>
  );
}
```

### 2. 使用 useMemo 和 useCallback

```typescript
import { useMemo, useCallback } from 'react';

export default function WordList({ words }) {
  // 缓存计算结果
  const sortedWords = useMemo(() => {
    return [...words].sort((a, b) => a.word.localeCompare(b.word));
  }, [words]);

  // 缓存回调函数
  const handleClick = useCallback((wordId: number) => {
    console.log('Clicked:', wordId);
  }, []);

  return <div>...</div>;
}
```

### 3. 图片懒加载

```typescript
<img
  src="image.jpg"
  loading="lazy"
  alt="描述"
/>
```

## 常见问题

### Q: 如何配置环境变量？

创建 `.env` 文件：

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=EnStudApp
```

在代码中使用：

```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL;
```

### Q: 如何处理跨域？

在 `vite.config.ts` 中配置代理：

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

### Q: 如何调试？

使用 React DevTools 浏览器扩展，或使用 `console.log`：

```typescript
console.log('Data:', data);
debugger; // 断点调试
```

## 相关文档

- [React 文档](https://react.dev/)
- [TypeScript 文档](https://www.typescriptlang.org/)
- [Ant Design 文档](https://ant.design/)
- [Vite 文档](https://vitejs.dev/)
- [编码规范](./coding-standards.md)
