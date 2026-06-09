import { useState } from 'react';
import { Card, Input, Button, Select, Spin, message } from 'antd';
import { SwapOutlined } from '@ant-design/icons';
import { translateText } from '../api';
import type { ApiResponse, TranslateResponse } from '../types';

const { TextArea } = Input;

export default function TranslatePage() {
  const [text, setText] = useState('');
  const [result, setResult] = useState('');
  const [from, setFrom] = useState<string>('auto');
  const [to, setTo] = useState('zh');
  const [loading, setLoading] = useState(false);

  const handleTranslate = async () => {
    if (!text.trim()) return;
    setLoading(true);
    try {
      const res: ApiResponse<TranslateResponse> = await translateText({
        text,
        from: from === 'auto' ? undefined : from,
        to,
      });
      setResult(res.data.translatedText);
    } catch {
      message.error('翻译失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSwap = () => {
    if (from === 'auto') return;
    const tmp = from;
    setFrom(to);
    setTo(tmp);
    setText(result);
    setResult('');
  };

  return (
    <Card title="翻译工具">
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <div style={{ marginBottom: 8 }}>
            <Select value={from} onChange={setFrom} style={{ width: 100 }}
              options={[{ label: '自动检测', value: 'auto' }, { label: '中文', value: 'zh' }, { label: '英文', value: 'en' }]} />
          </div>
          <TextArea rows={8} value={text} onChange={(e) => setText(e.target.value)} placeholder="输入要翻译的文本..." />
        </div>
        <Button icon={<SwapOutlined />} onClick={handleSwap} style={{ marginTop: 32 }} />
        <div style={{ flex: 1 }}>
          <div style={{ marginBottom: 8 }}>
            <Select value={to} onChange={setTo} style={{ width: 100 }}
              options={[{ label: '中文', value: 'zh' }, { label: '英文', value: 'en' }]} />
          </div>
          <Spin spinning={loading}>
            <TextArea rows={8} value={result} readOnly placeholder="翻译结果..." style={{ background: '#fafafa' }} />
          </Spin>
        </div>
      </div>
      <Button type="primary" onClick={handleTranslate} loading={loading} style={{ marginTop: 16 }} block>
        翻译
      </Button>
    </Card>
  );
}
