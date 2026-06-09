import { useState, useEffect, useRef } from 'react';
import { Card, Input, Button, List, Avatar, Select, Space, message, Tag } from 'antd';
import { SendOutlined, PlusOutlined, UserOutlined, RobotOutlined, DeleteOutlined } from '@ant-design/icons';
import { createSession, getSessions, sendMessage, getMessages, deleteSession } from '../api';
import type { ChatSession, MessageDTO, ApiResponse } from '../types';

export default function ChatPage() {
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [activeSession, setActiveSession] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageDTO[]>([]);
  const [input, setInput] = useState('');
  const [scenario, setScenario] = useState('FREE');
  const [loading, setLoading] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => { loadSessions(); }, []);
  useEffect(() => { if (activeSession) loadMessages(activeSession); }, [activeSession]);
  useEffect(() => { listRef.current?.scrollTo(0, listRef.current.scrollHeight); }, [messages]);

  const loadSessions = async () => {
    try { const res: ApiResponse<ChatSession[]> = await getSessions(); setSessions(res.data || []); } catch {}
  };
  const loadMessages = async (id: number) => {
    try { const res: ApiResponse<MessageDTO[]> = await getMessages(id); setMessages(res.data || []); } catch {}
  };

  const handleNewSession = async () => {
    try {
      const res: ApiResponse<ChatSession> = await createSession(scenario);
      setActiveSession(res.data.id);
      setMessages([]);
      loadSessions();
      message.success('新会话已创建');
    } catch {
      message.error('创建失败');
    }
  };

  const handleSend = async () => {
    if (!input.trim() || !activeSession) return;
    const text = input;
    setInput('');
    setLoading(true);
    setMessages((prev) => [...prev, { role: 'USER', content: text } as MessageDTO]);
    try {
      const res: ApiResponse<{ aiMessage: MessageDTO }> = await sendMessage(activeSession, text);
      const { aiMessage } = res.data;
      setMessages((prev) => [...prev, aiMessage]);
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

  const renderItem = (s: ChatSession) => (
    <List.Item
      style={{ cursor: 'pointer', background: activeSession === s.id ? '#e6f4ff' : undefined }}
      onClick={() => setActiveSession(s.id)}
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

  return (
    <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 140px)' }}>
      <Card style={{ width: 240, overflow: 'auto' }} size="small" title={
        <Space>
          <Select value={scenario} onChange={setScenario} size="small" style={{ width: 100 }}
            options={[
              { label: '自由对话', value: 'FREE' },
              { label: '日常', value: 'DAILY' },
              { label: '商务', value: 'BUSINESS' },
              { label: '旅行', value: 'TRAVEL' },
            ]} />
          <Button type="primary" size="small" icon={<PlusOutlined />} onClick={handleNewSession} />
        </Space>
      }>
        <List size="small" dataSource={sessions} renderItem={renderItem} />
      </Card>
      <Card style={{ flex: 1, display: 'flex', flexDirection: 'column' }} bodyStyle={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 0 }}>
        <div ref={listRef} style={{ flex: 1, overflow: 'auto', padding: 16 }}>
          {messages.map((m, i) => (
            <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 16, flexDirection: m.role === 'USER' ? 'row-reverse' : 'row' }}>
              <Avatar icon={m.role === 'USER' ? <UserOutlined /> : <RobotOutlined />}
                style={{ background: m.role === 'USER' ? '#1677ff' : '#52c41a' }} />
              <div style={{ maxWidth: '70%', background: m.role === 'USER' ? '#1677ff' : '#f0f0f0', color: m.role === 'USER' ? '#fff' : '#000', padding: '8px 16px', borderRadius: 12, whiteSpace: 'pre-wrap' }}>
                {m.content}
                {m.grammarIssues && m.grammarIssues.length > 0 && (
                  <div style={{ marginTop: 8 }}>
                    {m.grammarIssues.map((g, j) => (
                      <Tag key={j} color="orange" style={{ marginTop: 4 }}>{g.original} → {g.suggestion}</Tag>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
          {messages.length === 0 && <div style={{ textAlign: 'center', color: '#ccc', marginTop: 100 }}>开始一段新对话吧</div>}
        </div>
        <div style={{ padding: 16, borderTop: '1px solid #f0f0f0' }}>
          <Input.Search value={input} onChange={(e) => setInput(e.target.value)} onSearch={handleSend}
            enterButton={<Button type="primary" icon={<SendOutlined />} loading={loading}>发送</Button>}
            placeholder="输入英语，开始聊天..." disabled={!activeSession} />
        </div>
      </Card>
    </div>
  );
}
