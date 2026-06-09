import { useState, useEffect } from 'react';
import { Card, Form, Input, Button, Select, Tabs, List, Tag, message } from 'antd';
import { SendOutlined } from '@ant-design/icons';
import { submitWriting, getWritingHistory, getModels } from '../api';
import type { CorrectionDTO, WritingDTO, ModelEssayDTO, ApiResponse } from '../types';

const { TextArea } = Input;

export default function WritingPage() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<CorrectionDTO | null>(null);
  const [history, setHistory] = useState<WritingDTO[]>([]);
  const [models, setModels] = useState<ModelEssayDTO[]>([]);
  const [form] = Form.useForm();

  useEffect(() => { loadHistory(); loadModels(); }, []);

  const loadHistory = async () => {
    try {
      const res: ApiResponse<WritingDTO[]> = await getWritingHistory();
      setHistory(res.data || []);
    } catch {}
  };
  const loadModels = async () => {
    try {
      const res: ApiResponse<ModelEssayDTO[]> = await getModels();
      setModels(res.data || []);
    } catch {}
  };

  const handleSubmit = async (values: { title: string; content: string; topicType?: string }) => {
    setLoading(true);
    setResult(null);
    try {
      const res: ApiResponse<CorrectionDTO> = await submitWriting(values);
      setResult(res.data);
      loadHistory();
      message.success('批改完成');
    } catch (err: unknown) {
      message.error((err as { msg?: string })?.msg || '提交失败');
    } finally {
      setLoading(false);
    }
  };

  const renderHistoryItem = (w: WritingDTO) => (
    <List.Item extra={w.score != null ? <Tag color="blue">得分: {w.score}</Tag> : null}>
      <List.Item.Meta title={w.title} description={`${w.wordCount} 词 · ${new Date(w.createdAt).toLocaleDateString()}`} />
    </List.Item>
  );

  const renderModelItem = (m: ModelEssayDTO) => (
    <Card style={{ marginBottom: 16 }} title={m.title} extra={<Tag>{m.topicType}</Tag>}>
      <p style={{ whiteSpace: 'pre-wrap' }}>{m.content}</p>
      <Card size="small" style={{ background: '#f6ffed', marginTop: 8 }}>{m.comment}</Card>
    </Card>
  );

  return (
    <Tabs items={[
      {
        key: 'submit', label: '提交作文',
        children: (
          <div style={{ display: 'flex', gap: 16 }}>
            <Card style={{ flex: 1 }}>
              <Form form={form} onFinish={handleSubmit} layout="vertical">
                <Form.Item name="title" rules={[{ required: true, message: '请输入作文标题' }]}>
                  <Input placeholder="作文标题" />
                </Form.Item>
                <Form.Item name="topicType" label="题目类型">
                  <Select options={[
                    { label: '议论文', value: 'ESSAY' },
                    { label: '书信', value: 'LETTER' },
                    { label: '摘要', value: 'SUMMARY' },
                  ]} />
                </Form.Item>
                <Form.Item name="content" rules={[{ required: true, message: '请输入作文内容' }]}>
                  <TextArea rows={12} placeholder="在此输入英语作文..." />
                </Form.Item>
                <Button type="primary" htmlType="submit" icon={<SendOutlined />} loading={loading} block>
                  提交批改
                </Button>
              </Form>
            </Card>
            {result && (
              <Card style={{ flex: 1 }} title={`批改结果 — 得分: ${result.score}`}>
                <p style={{ color: '#8c8c8c', fontStyle: 'italic' }}>{result.overallComment}</p>
                {result.items?.map((item, i) => (
                  <Card key={i} size="small" style={{ marginTop: 8 }}>
                    <Tag color="orange">{item.type}</Tag>
                    <p><del style={{ color: '#ff4d4f' }}>{item.original}</del> → <b style={{ color: '#52c41a' }}>{item.suggestion}</b></p>
                    <p style={{ color: '#8c8c8c', fontSize: 12 }}>{item.explanation}</p>
                  </Card>
                ))}
              </Card>
            )}
          </div>
        ),
      },
      {
        key: 'history', label: '写作历史',
        children: <List dataSource={history} renderItem={renderHistoryItem} />,
      },
      {
        key: 'models', label: '范文参考',
        children: <List dataSource={models} renderItem={renderModelItem} />,
      },
    ]} />
  );
}
