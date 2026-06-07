import { useState, useEffect } from 'react';
import { Card, Select, Button, Spin, Progress, message, Empty, Space } from 'antd';
import { PlayCircleOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { getWordbooks, startStudy, submitReview } from '../api';

export default function WordPage() {
  const [wordbooks, setWordbooks] = useState<any[]>([]);
  const [selectedBook, setSelectedBook] = useState<number | null>(null);
  const [words, setWords] = useState<any[]>([]);
  const [currentIdx, setCurrentIdx] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [loading, setLoading] = useState(false);
  const [studying, setStudying] = useState(false);

  useEffect(() => { getWordbooks().then((res: any) => setWordbooks(res.data || [])); }, []);

  const handleStart = async () => {
    if (!selectedBook) return;
    setLoading(true);
    try {
      const res: any = await startStudy(selectedBook, 10);
      setWords(res.data || []);
      setCurrentIdx(0);
      setShowAnswer(false);
      setStudying(true);
    } catch { message.error('获取单词失败'); }
    finally { setLoading(false); }
  };

  const handleReview = async (quality: number) => {
    const word = words[currentIdx];
    if (!word) return;
    try {
      await submitReview(word.id, quality);
      if (currentIdx < words.length - 1) {
        setCurrentIdx(currentIdx + 1);
        setShowAnswer(false);
      } else {
        message.success('本轮学习完成！');
        setStudying(false);
        setWords([]);
      }
    } catch { message.error('提交失败'); }
  };

  if (studying && words.length > 0) {
    const word = words[currentIdx];
    const progress = Math.round(((currentIdx + 1) / words.length) * 100);
    return (
      <Card title="单词学习">
        <Progress percent={progress} style={{ marginBottom: 16 }} />
        <Card style={{ background: '#fafafa', textAlign: 'center', minHeight: 200 }}>
          <h1 style={{ fontSize: 36, margin: '16px 0' }}>{word.word}</h1>
          {word.phoneticUs && <p style={{ color: '#8c8c8c' }}>/ {word.phoneticUs} /</p>}
          {showAnswer ? (
            <div style={{ marginTop: 16 }}>
              <p style={{ fontSize: 18 }}>{word.definitionCn}</p>
              {word.exampleSentence && <p style={{ color: '#8c8c8c', fontStyle: 'italic' }}>"{word.exampleSentence}"</p>}
              <Space style={{ marginTop: 24 }} size="large">
                <Button size="large" icon={<CloseCircleOutlined />} danger onClick={() => handleReview(1)}>忘记</Button>
                <Button size="large" icon={<CheckCircleOutlined />} onClick={() => handleReview(4)}>记得</Button>
                <Button size="large" icon={<CheckCircleOutlined />} type="primary" onClick={() => handleReview(5)}>熟练</Button>
              </Space>
            </div>
          ) : (
            <div style={{ marginTop: 32 }}>
              <Button size="large" type="primary" icon={<PlayCircleOutlined />} onClick={() => setShowAnswer(true)}>
                显示释义
              </Button>
            </div>
          )}
        </Card>
      </Card>
    );
  }

  return (
    <Card title="单词学习">
      <Spin spinning={loading}>
        <Space>
          <Select
            style={{ width: 240 }}
            placeholder="选择词库"
            value={selectedBook}
            onChange={setSelectedBook}
            options={wordbooks.map((b: any) => ({ label: `${b.name} (${b.wordCount}词)`, value: b.id }))}
          />
          <Button type="primary" icon={<PlayCircleOutlined />} disabled={!selectedBook} onClick={handleStart}>
            开始学习
          </Button>
        </Space>
        {wordbooks.length === 0 && <Empty style={{ marginTop: 48 }} description="暂无词库，请先导入数据" />}
      </Spin>
    </Card>
  );
}
