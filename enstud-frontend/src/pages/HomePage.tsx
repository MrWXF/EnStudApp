import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Typography, Statistic, Button, Tag, Space, Skeleton } from 'antd';
import {
  BookOutlined, ReadOutlined, MessageOutlined, FireOutlined,
  ThunderboltOutlined, RocketOutlined,
} from '@ant-design/icons';
import { getUserStats } from '../api/dashboard';
import { getRecommendedArticles } from '../api/reading';
import type { UserStats, ArticleDTO } from '../types';

const { Title, Text } = Typography;

export default function HomePage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [stats, setStats] = useState<UserStats | null>(null);
  const [articles, setArticles] = useState<ArticleDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getUserStats().then((res) => res.data).catch(() => null),
      getRecommendedArticles(6).then((res) => res.data || []).catch(() => []),
    ]).then(([statsData, articlesData]) => {
      setStats(statsData);
      setArticles(articlesData);
    }).finally(() => setLoading(false));
  }, []);

  // 计算待复习单词数（记忆等级 0-2 的单词）
  const wordsToReview = stats
    ? (stats.memoryLevelDistribution['notLearned'] ?? 0)
      + (stats.memoryLevelDistribution['fuzzy'] ?? 0)
      + (stats.memoryLevelDistribution['familiar'] ?? 0)
    : 0;

  const hour = new Date().getHours();
  const greeting = hour < 6 ? '夜深了' : hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好';

  if (loading) {
    return (
      <div>
        <Skeleton active paragraph={{ rows: 6 }} />
      </div>
    );
  }

  return (
    <div>
      {/* 问候语 + 连续打卡 */}
      <div style={{ marginBottom: 20 }}>
        <Title level={4} style={{ marginBottom: 4 }}>
          {greeting}，{user.nickname || user.username || '同学'} 👋
        </Title>
        {stats && stats.streakDays > 0 ? (
          <Text type="secondary">
            <FireOutlined style={{ color: '#faad14' }} />{' '}
            你已连续学习 <Text strong style={{ color: '#faad14' }}>{stats.streakDays}</Text> 天，继续加油！
          </Text>
        ) : (
          <Text type="secondary">开始今天的学习之旅吧 🚀</Text>
        )}
      </div>

      {/* 今日学习概览 — 4 个 mini 统计卡 */}
      <Row gutter={[12, 12]} style={{ marginBottom: 20 }}>
        <Col xs={12} sm={6}>
          <Card size="small" hoverable onClick={() => navigate('/word')}>
            <Statistic title="今日学词" value={stats?.todayLearnedWords ?? 0} suffix="词"
              valueStyle={{ fontSize: 24, color: '#1677ff' }} prefix={<BookOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small" hoverable onClick={() => navigate('/word')}>
            <Statistic title="待复习" value={wordsToReview} suffix="词"
              valueStyle={{ fontSize: 24, color: wordsToReview > 0 ? '#ff7a45' : '#52c41a' }}
              prefix={<ThunderboltOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small" hoverable onClick={() => navigate('/read')}>
            <Statistic title="阅读文章" value={stats?.totalReadArticles ?? 0} suffix="篇"
              valueStyle={{ fontSize: 24, color: '#52c41a' }} prefix={<ReadOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small" hoverable onClick={() => navigate('/chat')}>
            <Statistic title="AI 对话" value={stats?.totalChats ?? 0} suffix="次"
              valueStyle={{ fontSize: 24, color: '#722ed1' }} prefix={<MessageOutlined />} />
          </Card>
        </Col>
      </Row>

      {/* 学习闭环入口 — 核心 CTA */}
      <Card
        style={{ marginBottom: 20, background: 'linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%)', border: '1px solid #d6e4ff' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <div style={{ marginBottom: 12 }}>
          <RocketOutlined style={{ color: '#1677ff', fontSize: 18 }} />
          <Text strong style={{ marginLeft: 8, fontSize: 15 }}>学习闭环</Text>
          <Text type="secondary" style={{ marginLeft: 8, fontSize: 12 }}>阅读 → 积累 → 复习 → 对话</Text>
        </div>
        <Row gutter={[12, 12]}>
          <Col xs={24} sm={8}>
            <Button block size="large" icon={<BookOutlined />} onClick={() => navigate('/word')}
              style={{ height: 52, textAlign: 'left' }}>
              📚 继续背单词
              {stats && stats.todayLearnedWords > 0 && (
                <Tag color="blue" style={{ marginLeft: 8 }}>{stats.todayLearnedWords} 词</Tag>
              )}
            </Button>
          </Col>
          <Col xs={24} sm={8}>
            <Button block size="large" icon={<ReadOutlined />} onClick={() => navigate('/read')}
              style={{ height: 52, textAlign: 'left' }}>
              📖 推荐阅读
              {articles.length > 0 && (
                <Tag color="green" style={{ marginLeft: 8 }}>{articles.length} 篇</Tag>
              )}
            </Button>
          </Col>
          <Col xs={24} sm={8}>
            <Button block size="large" type="primary" icon={<MessageOutlined />} onClick={() => navigate('/chat')}
              style={{ height: 52, textAlign: 'left' }}>
              💬 AI 复习对话
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 推荐文章 — 横向滚动卡片 */}
      {articles.length > 0 && (
        <Card
          title={<><ReadOutlined /> 为你推荐 — 包含你正在学习的单词</>}
          extra={<Button type="link" onClick={() => navigate('/read')}>查看全部</Button>}
          style={{ marginBottom: 20 }}
        >
          <div style={{ display: 'flex', gap: 12, overflowX: 'auto', paddingBottom: 8 }}>
            {articles.map((article) => (
              <Card
                key={article.id}
                hoverable
                size="small"
                onClick={() => navigate(`/read/${article.id}`)}
                style={{ minWidth: 240, maxWidth: 280, flexShrink: 0 }}
              >
                <Card.Meta
                  title={
                    <span style={{ fontSize: 14 }}>
                      <Tag color="blue" style={{ marginRight: 4 }}>{article.source}</Tag>
                      {article.titleCn || article.title}
                    </span>
                  }
                  description={
                    <Text type="secondary" ellipsis style={{ fontSize: 12 }}>
                      {article.summary || article.title}
                    </Text>
                  }
                />
              </Card>
            ))}
          </div>
        </Card>
      )}

      {/* 累计学习数据 */}
      {stats && (
        <Card title="累计学习" size="small">
          <Row gutter={[16, 8]}>
            <Col xs={12} sm={6}>
              <Space>
                <BookOutlined style={{ color: '#1677ff' }} />
                <Text type="secondary">累计单词</Text>
                <Text strong>{stats.totalLearnedWords}</Text>
              </Space>
            </Col>
            <Col xs={12} sm={6}>
              <Space>
                <ReadOutlined style={{ color: '#52c41a' }} />
                <Text type="secondary">阅读文章</Text>
                <Text strong>{stats.totalReadArticles}</Text>
              </Space>
            </Col>
            <Col xs={12} sm={6}>
              <Space>
                <MessageOutlined style={{ color: '#722ed1' }} />
                <Text type="secondary">对话次数</Text>
                <Text strong>{stats.totalChats}</Text>
              </Space>
            </Col>
            <Col xs={12} sm={6}>
              <Space>
                <FireOutlined style={{ color: '#faad14' }} />
                <Text type="secondary">最长连续</Text>
                <Text strong>{stats.streakDays} 天</Text>
              </Space>
            </Col>
          </Row>
        </Card>
      )}
    </div>
  );
}
