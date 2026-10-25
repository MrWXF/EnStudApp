import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Card, Input, Button, List, Avatar, Select, Space, message, Tag, Drawer, Grid } from 'antd';
import {
  SendOutlined, PlusOutlined, UserOutlined, RobotOutlined,
  DeleteOutlined, ThunderboltOutlined, MenuOutlined,
} from '@ant-design/icons';
import { createSession, getSessions, sendMessage, getMessages, deleteSession, startReviewSession } from '../api';
import { getLearningWords } from '../api/word';
import type { ChatSession, MessageDTO } from '../types';
import type { LearningWordDTO } from '../api/word';

const { useBreakpoint } = Grid;

export default function ChatPage() {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const [searchParams] = useSearchParams();
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [activeSession, setActiveSession] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageDTO[]>([]);
  const [input, setInput] = useState('');
  const [scenario, setScenario] = useState('FREE');
  const [loading, setLoading] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);
  const reviewTriggered = useRef(false);
  const [learningWords, setLearningWords] = useState<LearningWordDTO[]>([]);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const loadSessions = async () => {
    try { const res = await getSessions(); setSessions(res.data || []); } catch { /* 静默处理 */ }
  };
  const loadMessages = async (id: number) => {
    try { const res = await getMessages(id); setMessages(res.data || []); } catch { /* 静默处理 */ }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadSessions();
    getLearningWords(10).then((res) => setLearningWords(res.data || [])).catch(() => {});
  }, []);
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (activeSession) loadMessages(activeSession);
  }, [activeSession]);
  useEffect(() => { listRef.current?.scrollTo(0, listRef.current.scrollHeight); }, [messages]);

  const handleNewSession = async () => {
    try {
      const res = await createSession(scenario);
      setActiveSession(res.data.id);
      setMessages([]);
      loadSessions();
      setDrawerOpen(false);
      message.success('新会话已创建');
    } catch {
      message.error('创建失败');
    }
  };

  const handleReviewSession = async () => {
    try {
      setLoading(true);
      const res = await startReviewSession();
      if (res.data) {
        await loadSessions();
        const sessionsRes = await getSessions();
        if (sessionsRes.data && sessionsRes.data.length > 0) {
          setActiveSession(sessionsRes.data[0].id);
          setMessages([res.data.aiMessage]);
        }
        setDrawerOpen(false);
        message.success('复习对话已创建，AI 将围绕你正在学习的单词进行练习');
      }
    } catch {
      message.error('创建复习对话失败');
    } finally {
      setLoading(false);
    }
  };

  // 从首页「AI 用词对话」跳转（?review=1）时，自动开始复习对话
  useEffect(() => {
    if (searchParams.get('review') === '1' && !reviewTriggered.current) {
      reviewTriggered.current = true;
      handleReviewSession();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSend = async () => {
    if (!input.trim() || !activeSession) return;
    const text = input;
    setInput('');
    setLoading(true);
    setMessages((prev) => [...prev, { role: 'USER', content: text } as MessageDTO]);
    try {
      const res = await sendMessage(activeSession, text);
      const { aiMessage } = res.data;
      setMessages((prev) => [...prev, { role: 'USER', content: text } as MessageDTO, aiMessage]);
    } catch {
      message.error('发送失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteSession(id);
      loadSessions();
      if (activeSession === id) setActiveSession(null);
    } catch {
      message.error('删除失败');
    }
  };

  const handleSelectSession = (id: number) => {
    setActiveSession(id);
    setDrawerOpen(false);
  };

  const renderItem = (s: ChatSession) => (
    <List.Item
      style={{ cursor: 'pointer', background: activeSession === s.id ? '#e6f4ff' : undefined }}
      onClick={() => handleSelectSession(s.id)}
      actions={[
        <Button
          size="small" type="text" danger icon={<DeleteOutlined />}
          onClick={(e) => { e.stopPropagation(); handleDelete(s.id); }}
        />,
      ]}
    >
      <List.Item.Meta title={s.title} description={new Date(s.createdAt).toLocaleDateString()} />
    </List.Item>
  );

  /** 会话列表面板内容 */
  const sessionPanel = (
    <Space direction="vertical" style={{ width: '100%' }}>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        <Select value={scenario} onChange={setScenario} size="small" style={{ width: 100 }}
          options={[
            { label: '自由', value: 'FREE' },
            { label: '日常', value: 'DAILY' },
            { label: '商务', value: 'BUSINESS' },
            { label: '旅行', value: 'TRAVEL' },
          ]} />
        <Button type="primary" size="small" icon={<PlusOutlined />} onClick={handleNewSession} />
        <Button size="small" icon={<ThunderboltOutlined />} onClick={handleReviewSession} title="AI 复习对话">
          复习
        </Button>
      </div>
      <List size="small" dataSource={sessions} renderItem={renderItem} />
    </Space>
  );

  return (
    <div style={{ display: 'flex', gap: 16, height: isMobile ? 'calc(100vh - 180px)' : 'calc(100vh - 140px)' }}>
      {/* 桌面端：侧边栏 */}
      {!isMobile && (
        <Card style={{ width: 240, overflow: 'auto' }} size="small" title="会话列表">
          {sessionPanel}
        </Card>
      )}

      {/* 移动端：浮动按钮打开 Drawer */}
      {isMobile && (
        <Button
          icon={<MenuOutlined />}
          onClick={() => setDrawerOpen(true)}
          style={{ position: 'fixed', bottom: 80, left: 16, zIndex: 100, borderRadius: '50%', width: 48, height: 48 }}
          type="primary"
          shape="circle"
        />
      )}

      {/* 移动端：Drawer 会话列表 */}
      {isMobile && (
        <Drawer
          title="会话列表"
          placement="left"
          onClose={() => setDrawerOpen(false)}
          open={drawerOpen}
          width={300}
        >
          {sessionPanel}
        </Drawer>
      )}

      <Card style={{ flex: 1, display: 'flex', flexDirection: 'column' }} bodyStyle={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 0 }}>
        {/* 学习上下文提示条 */}
        {learningWords.length > 0 && (
          <div style={{
            padding: '8px 16px',
            background: '#f6ffed',
            borderBottom: '1px solid #d9f7be',
            fontSize: 12,
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            flexWrap: 'wrap',
          }}>
            <span>💡</span>
            <span style={{ color: '#52c41a', fontWeight: 500 }}>
              AI 正在融入你学习的 {learningWords.length} 个单词
            </span>
            <span style={{ color: '#8c8c8c' }}>
              （{learningWords.slice(0, 3).map((w) => w.word).join(', ')}{learningWords.length > 3 ? '...' : ''}）
            </span>
          </div>
        )}
        <div ref={listRef} style={{ flex: 1, overflow: 'auto', padding: isMobile ? 12 : 16 }}>
          {messages.map((m, i) => (
            <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 16, flexDirection: m.role === 'USER' ? 'row-reverse' : 'row' }}>
              <Avatar icon={m.role === 'USER' ? <UserOutlined /> : <RobotOutlined />}
                style={{ background: m.role === 'USER' ? '#1677ff' : '#52c41a', flexShrink: 0 }} />
              <div style={{
                maxWidth: isMobile ? '85%' : '70%',
                background: m.role === 'USER' ? '#1677ff' : '#f0f0f0',
                color: m.role === 'USER' ? '#fff' : '#000',
                padding: '8px 16px', borderRadius: 12, whiteSpace: 'pre-wrap',
                fontSize: isMobile ? 14 : 15,
              }}>
                {m.content}
                {m.grammarIssues && m.grammarIssues.length > 0 && (
                  <div style={{ marginTop: 8 }}>
                    {m.grammarIssues.map((g, j) => (
                      <Tag key={j} color="orange" style={{ marginTop: 4 }}>{g.error} → {g.correction}</Tag>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
          {messages.length === 0 && (
            <div style={{ textAlign: 'center', color: '#ccc', marginTop: 100 }}>
              {isMobile ? '点击左下角按钮选择会话' : '开始一段新对话吧'}
            </div>
          )}
        </div>
        <div style={{ padding: isMobile ? 12 : 16, borderTop: '1px solid #f0f0f0' }}>
          <Input.Search value={input} onChange={(e) => setInput(e.target.value)} onSearch={handleSend}
            enterButton={<Button type="primary" icon={<SendOutlined />} loading={loading}>发送</Button>}
            placeholder="输入英语，开始聊天..." disabled={!activeSession} />
        </div>
      </Card>
    </div>
  );
}
