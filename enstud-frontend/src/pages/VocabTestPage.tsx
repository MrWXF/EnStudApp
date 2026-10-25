import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card, Typography, Button, Progress, Tag, Statistic, Space, Spin, message,
} from 'antd';
import {
  ExperimentOutlined, CheckOutlined, QuestionOutlined, CloseOutlined,
  ShareAltOutlined, RedoOutlined, HomeOutlined, FireOutlined,
} from '@ant-design/icons';
import { getLatestVocabResult, getVocabTestWords, submitVocabTest } from '../api/word';
import type { VocabTestResult, VocabTestWord } from '../types';
import ShareCardModal from '../components/ShareCardModal';

const { Title, Text, Paragraph } = Typography;

type Stage = 'intro' | 'testing' | 'result';

export default function VocabTestPage() {
  const navigate = useNavigate();
  const [stage, setStage] = useState<Stage>('intro');
  const [words, setWords] = useState<VocabTestWord[]>([]);
  const [index, setIndex] = useState(0);
  const [answers, setAnswers] = useState<{ word: string; knowledge: number }[]>([]);
  const [result, setResult] = useState<VocabTestResult | null>(null);
  const [latest, setLatest] = useState<VocabTestResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [shareOpen, setShareOpen] = useState(false);

  useEffect(() => {
    getLatestVocabResult().then((res) => setLatest(res.data || null)).catch(() => {});
  }, []);

  const startTest = async () => {
    setLoading(true);
    try {
      const res = await getVocabTestWords();
      setWords(res.data || []);
      setAnswers([]);
      setIndex(0);
      setResult(null);
      setStage('testing');
    } catch {
      message.error('获取测试题目失败');
    } finally {
      setLoading(false);
    }
  };

  const answer = async (knowledge: number) => {
    const current = words[index];
    const next = [...answers, { word: current.word, knowledge }];
    setAnswers(next);
    if (index + 1 < words.length) {
      setIndex(index + 1);
    } else {
      // 全部作答完毕，提交
      setLoading(true);
      try {
        const res = await submitVocabTest(next);
        setResult(res.data);
        setStage('result');
        setLatest(res.data);
      } catch {
        message.error('提交失败，请重试');
        setStage('intro');
      } finally {
        setLoading(false);
      }
    }
  };

  const total = words.length;

  if (stage === 'intro') {
    return (
      <div style={{ maxWidth: 640, margin: '0 auto', padding: 24 }}>
        <Card styles={{ body: { padding: 40, textAlign: 'center' } }}>
          <ExperimentOutlined style={{ fontSize: 56, color: '#1677ff' }} />
          <Title level={3} style={{ marginTop: 16 }}>测测你的词汇量</Title>
          <Paragraph type="secondary" style={{ fontSize: 15 }}>
            32 个单词，从初中到进阶 6 个难度频段随机抽取。<br />
            凭第一印象作答，不要查词典，结果更接近真实水平。
          </Paragraph>
          {latest && (
            <Card size="small" style={{ margin: '16px 0', background: '#f6ffed', border: '1px solid #b7eb8f' }}>
              <Space>
                <FireOutlined style={{ color: '#faad14' }} />
                <Text>上次测试：<Text strong>{latest.estimatedSize}</Text> 词</Text>
                <Tag color="blue">{latest.levelLabel}</Tag>
              </Space>
            </Card>
          )}
          <Button type="primary" size="large" loading={loading} onClick={startTest} style={{ height: 48, paddingInline: 48 }}>
            开始测试
          </Button>
        </Card>
      </div>
    );
  }

  if (stage === 'testing') {
    return (
      <div style={{ maxWidth: 640, margin: '0 auto', padding: 24 }}>
        <Progress percent={Math.round((index / Math.max(total, 1)) * 100)} showInfo={false}
          strokeColor={{ '0%': '#1677ff', '100%': '#52c41a' }} />
        <div style={{ textAlign: 'center', margin: '8px 0 24px' }}>
          <Text type="secondary">{index + 1} / {total}</Text>
        </div>
        <Card styles={{ body: { padding: '80px 24px', textAlign: 'center' } }}
          style={{ marginBottom: 24 }}>
          <Title level={1} style={{ marginBottom: 8 }}>{words[index]?.word}</Title>
          <Text type="secondary">认识它吗？凭第一印象选择</Text>
        </Card>
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <Button type="primary" size="large" block icon={<CheckOutlined />}
            loading={loading} onClick={() => answer(2)} style={{ height: 52 }}>
            认识
          </Button>
          <Button size="large" block icon={<QuestionOutlined />}
            loading={loading} onClick={() => answer(1)} style={{ height: 52 }}>
            见过，但不确定
          </Button>
          <Button size="large" block icon={<CloseOutlined />}
            loading={loading} onClick={() => answer(0)} style={{ height: 52 }}>
            不认识
          </Button>
        </Space>
      </div>
    );
  }

  // stage === 'result'
  if (!result) {
    return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;
  }

  return (
    <div style={{ maxWidth: 640, margin: '0 auto', padding: 24 }}>
      <Card styles={{ body: { padding: 32, textAlign: 'center' } }} style={{ marginBottom: 20 }}>
        <Text type="secondary">你的估算词汇量</Text>
        <Statistic value={result.estimatedSize} suffix="词"
          valueStyle={{ fontSize: 64, color: '#1677ff', fontWeight: 700 }} />
        <Tag color="blue" style={{ fontSize: 16, padding: '4px 16px', marginTop: 8 }}>{result.levelLabel}</Tag>
        <Paragraph type="secondary" style={{ marginTop: 16 }}>{result.levelDescription}</Paragraph>
      </Card>

      <Card title="各频段掌握情况" size="small" style={{ marginBottom: 20 }}>
        {result.bands.map((b) => (
          <div key={b.band} style={{ display: 'flex', alignItems: 'center', gap: 12, margin: '10px 0' }}>
            <Text style={{ width: 80, flexShrink: 0 }}>{b.label}</Text>
            <Progress percent={Math.round(b.ratio * 100)} size="small"
              strokeColor={b.ratio >= 0.6 ? '#52c41a' : b.ratio >= 0.3 ? '#faad14' : '#ff7a45'}
              format={() => `${b.known}/${b.known + b.vague + b.unknown}`}
              style={{ flex: 1, margin: 0 }} />
          </div>
        ))}
      </Card>

      <Space style={{ width: '100%', justifyContent: 'center' }} size={12}>
        <Button type="primary" icon={<ShareAltOutlined />} onClick={() => setShareOpen(true)}>
          生成分享卡片
        </Button>
        <Button icon={<RedoOutlined />} onClick={startTest}>再测一次</Button>
        <Button icon={<HomeOutlined />} onClick={() => navigate('/')}>返回首页</Button>
      </Space>

      <ShareCardModal open={shareOpen} onClose={() => setShareOpen(false)} variant="vocab" vocabResult={result} />
    </div>
  );
}
