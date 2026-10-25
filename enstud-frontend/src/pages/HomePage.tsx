import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Typography, Statistic, Button, Tag, Space, Skeleton, Progress, message } from 'antd';
import {
  BookOutlined, ReadOutlined, MessageOutlined, FireOutlined,
  ThunderboltOutlined, CheckCircleFilled, RightOutlined, CalendarOutlined,
} from '@ant-design/icons';
import { getUserStats, getDailyJourney, checkin } from '../api/dashboard';
import { getRecommendedArticles } from '../api/reading';
import type { UserStats, ArticleDTO, DailyJourney } from '../types';

const { Title, Text } = Typography;

export default function HomePage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [stats, setStats] = useState<UserStats | null>(null);
  const [journey, setJourney] = useState<DailyJourney | null>(null);
  const [articles, setArticles] = useState<ArticleDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [checkinLoading, setCheckinLoading] = useState(false);

  useEffect(() => {
    Promise.all([
      getUserStats().then((res) => res.data).catch(() => null),
      getDailyJourney().then((res) => res.data).catch(() => null),
      getRecommendedArticles(6).then((res) => res.data || []).catch(() => []),
    ]).then(([statsData, journeyData, articlesData]) => {
      setStats(statsData);
      setJourney(journeyData);
      setArticles(articlesData);
    }).finally(() => setLoading(false));
  }, []);

  const refreshJourney = useCallback(async () => {
    const [journeyRes, statsRes] = await Promise.all([
      getDailyJourney().catch(() => null),
      getUserStats().catch(() => null),
    ]);
    if (journeyRes?.data) setJourney(journeyRes.data);
    if (statsRes?.data) setStats(statsRes.data);
  }, []);

  const handleCheckin = async () => {
    setCheckinLoading(true);
    try {
      const res = await checkin();
      const { streakDays, bonusPoints, message: msg } = res.data;
      message.success(bonusPoints > 0 ? `${msg}，+${bonusPoints} 积分，已连续 ${streakDays} 天 🔥` : msg);
      await refreshJourney();
    } catch {
      message.error('打卡失败，请稍后重试');
    } finally {
      setCheckinLoading(false);
    }
  };

  // 今日学习之旅：四项任务完成状态
  const readDone = (journey?.todayReadArticles ?? 0) >= 1;
  const reviewDone = (journey?.todayLearnedWords ?? 0) >= (journey?.dailyWordGoal ?? 20);
  const chatDone = (journey?.todayChats ?? 0) >= 1;
  const checkedIn = journey?.checkedIn ?? false;
  const completedCount = [readDone, reviewDone, chatDone, checkedIn].filter(Boolean).length;
  const reviewPercent = journey
    ? Math.min(100, Math.round((journey.todayLearnedWords / Math.max(journey.dailyWordGoal, 1)) * 100))
    : 0;

  // 计算待复习单词数（记忆等级 0-2 的单词）
  const wordsToReview = stats
    ? (stats.memoryLevelDistribution['notLearned'] ?? 0)
      + (stats.memoryLevelDistribution['fuzzy'] ?? 0)
      + (stats.memoryLevelDistribution['familiar'] ?? 0)
    : 0;

  const hour = new Date().getHours();
  const greeting = hour < 6 ? '夜深了' : hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好';

  /** 任务行 */
  const taskRow = (
    key: string,
    icon: React.ReactNode,
    title: string,
    desc: React.ReactNode,
    done: boolean,
    onClick: () => void,
    extra?: React.ReactNode,
  ) => (
    <div
      key={key}
      onClick={onClick}
      style={{
        display: 'flex', alignItems: 'center', gap: 12, padding: '10px 8px',
        borderRadius: 8, cursor: 'pointer',
      }}
    >
      <div style={{
        width: 36, height: 36, borderRadius: '50%', flexShrink: 0,
        display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16,
        background: done ? '#f6ffed' : '#f0f5ff', color: done ? '#52c41a' : '#1677ff',
      }}>
        {icon}
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <Text strong style={{ fontSize: 14 }}>{title}</Text>
        <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.45)' }}>{desc}</div>
        {extra}
      </div>
      {done
        ? <CheckCircleFilled style={{ color: '#52c41a', fontSize: 18 }} />
        : <RightOutlined style={{ color: 'rgba(0,0,0,0.25)' }} />}
    </div>
  );

  if (loading) {
    return (
      <div>
        <Skeleton active paragraph={{ rows: 8 }} />
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
        {journey && journey.streakDays > 0 ? (
          <Text type="secondary">
            <FireOutlined style={{ color: '#faad14' }} />{' '}
            你已连续学习 <Text strong style={{ color: '#faad14' }}>{journey.streakDays}</Text> 天，继续加油！
          </Text>
        ) : (
          <Text type="secondary">开始今天的学习之旅吧 🚀</Text>
        )}
      </div>

      {/* 今日学习之旅 — 每日任务流 */}
      {journey && (
        <Card
          style={{ marginBottom: 20, background: 'linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%)', border: '1px solid #d6e4ff' }}
          styles={{ body: { padding: '16px 20px' } }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <Space>
              <CalendarOutlined style={{ color: '#1677ff' }} />
              <Text strong style={{ fontSize: 16 }}>今日学习之旅</Text>
              <Text type="secondary" style={{ fontSize: 12 }}>
                {new Date(journey.date).toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })}
              </Text>
            </Space>
            {completedCount >= 4 && <Tag color="success">全部完成 🎉</Tag>}
          </div>

          <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
            {/* 进度环 */}
            <Progress
              type="circle"
              size={100}
              percent={completedCount * 25}
              strokeColor={{ '0%': '#1677ff', '100%': '#52c41a' }}
              format={() => (
                <span style={{ fontSize: 20, fontWeight: 600 }}>{completedCount}<span style={{ fontSize: 12, fontWeight: 400 }}>/4</span></span>
              )}
            />
            {/* 任务列表 */}
            <div style={{ flex: 1, minWidth: 0 }}>
              {taskRow(
                'read',
                <ReadOutlined />,
                '读一篇文章',
                readDone ? `今日已读 ${journey.todayReadArticles} 篇` : '读 1 篇含你学习词的文章',
                readDone,
                () => navigate(articles.length > 0 ? `/read/${articles[0].id}` : '/read'),
              )}
              {taskRow(
                'review',
                <BookOutlined />,
                '复习今日单词',
                <span>
                  今日 {journey.todayLearnedWords}/{journey.dailyWordGoal} 词
                  {journey.dueReviewCount > 0 && <Tag color="orange" style={{ marginLeft: 6 }}>{journey.dueReviewCount} 词到期</Tag>}
                </span>,
                reviewDone,
                () => navigate('/word'),
                !reviewDone && (
                  <Progress percent={reviewPercent} size="small" showInfo={false}
                    strokeColor={{ '0%': '#1677ff', '100%': '#52c41a' }}
                    style={{ margin: '4px 0 0', width: '90%' }} />
                ),
              )}
              {taskRow(
                'chat',
                <MessageOutlined />,
                'AI 用词对话',
                chatDone ? `今日已对话 ${journey.todayChats} 次` : '让 AI 带你用今天的词开口练',
                chatDone,
                () => navigate('/chat?review=1'),
              )}
            </div>
          </div>

          {/* 打卡收尾 */}
          <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px dashed #d6e4ff' }}>
            {checkedIn ? (
              <Space>
                <CheckCircleFilled style={{ color: '#52c41a', fontSize: 18 }} />
                <Text strong style={{ color: '#52c41a' }}>今日已打卡</Text>
                <Text type="secondary">连续 {journey.streakDays} 天，明天继续 🔥</Text>
              </Space>
            ) : (
              <Space>
                <Button type="primary" loading={checkinLoading} onClick={handleCheckin}>
                  完成今日打卡
                </Button>
                <Text type="secondary" style={{ fontSize: 12 }}>完成全部任务后打卡，领取积分奖励</Text>
              </Space>
            )}
          </div>
        </Card>
      )}

      {/* 今日学习概览 — 4 个 mini 统计卡 */}
      <Row gutter={[12, 12]} style={{ marginBottom: 20 }}>
        <Col xs={12} sm={6}>
          <Card size="small" hoverable onClick={() => navigate('/word')}>
            <Statistic title="今日学词" value={journey?.todayLearnedWords ?? stats?.todayLearnedWords ?? 0} suffix="词"
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
