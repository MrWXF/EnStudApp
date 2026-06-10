import { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Spin, Typography, Progress } from 'antd';
import {
  BookOutlined, ReadOutlined, EditOutlined,
  MessageOutlined, TeamOutlined, RiseOutlined,
} from '@ant-design/icons';
import { getUserStats } from '../api';
import type { UserStats } from '../types';

const { Title } = Typography;

/** 记忆等级色板 */
const LEVEL_CONFIG = [
  { label: '未学习', color: '#d9d9d9', key: 'notLearned' as const },
  { label: '模糊', color: '#ff7a45', key: 'fuzzy' as const },
  { label: '有印象', color: '#faad14', key: 'familiar' as const },
  { label: '基本掌握', color: '#52c41a', key: 'basic' as const },
  { label: '熟练', color: '#1677ff', key: 'proficient' as const },
  { label: '精通', color: '#722ed1', key: 'mastered' as const },
];

export default function DashboardPage() {
  const [stats, setStats] = useState<UserStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getUserStats()
      .then((res: any) => setStats(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 120 }} />;
  if (!stats) return <Typography.Text type="danger">加载统计失败</Typography.Text>;

  const dist = stats.memoryLevelDistribution;
  const totalWords = Object.values(dist).reduce((a: number, b: number) => a + b, 0);

  return (
    <div style={{ padding: 24 }}>
      <Title level={3} style={{ marginBottom: 24 }}>
        <RiseOutlined /> 学习仪表盘
      </Title>

      {/* 顶部统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={8} lg={4}>
          <Card hoverable>
            <Statistic title="今日学习" value={stats.todayLearnedWords} prefix={<BookOutlined />} suffix="词" />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <Card hoverable>
            <Statistic title="累计单词" value={stats.totalLearnedWords} prefix={<BookOutlined />} suffix="词" />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <Card hoverable>
            <Statistic title="阅读文章" value={stats.totalReadArticles} prefix={<ReadOutlined />} suffix="篇" />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <Card hoverable>
            <Statistic title="作文" value={stats.totalWritings} prefix={<EditOutlined />} suffix="篇" />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <Card hoverable>
            <Statistic title="对话" value={stats.totalChats} prefix={<MessageOutlined />} suffix="次" />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <Card hoverable>
            <Statistic title="帖子" value={stats.totalPosts} prefix={<TeamOutlined />} suffix="条" />
          </Card>
        </Col>
      </Row>

      {/* 第二行：写作均分 + 记忆等级分布 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        {/* 写作均分 */}
        <Col xs={24} md={8}>
          <Card title="写作平均得分">
            <div style={{ textAlign: 'center', padding: '24px 0' }}>
              <Progress
                type="circle"
                percent={Math.round(stats.avgWritingScore * 20)}
                format={() => `${stats.avgWritingScore.toFixed(1)}`}
                size={160}
              />
              <div style={{ marginTop: 12, color: '#888' }}>/ 5.0</div>
            </div>
          </Card>
        </Col>

        {/* 记忆等级分布 */}
        <Col xs={24} md={16}>
          <Card title="单词记忆等级分布">
            <Row gutter={[12, 12]}>
              {LEVEL_CONFIG.map((cfg) => {
                const val = dist[cfg.key] ?? 0;
                const pct = totalWords > 0 ? (val / totalWords) * 100 : 0;
                return (
                  <Col xs={12} sm={8} key={cfg.key}>
                    <div style={{ marginBottom: 8 }}>
                      <span
                        style={{
                          display: 'inline-block',
                          width: 12, height: 12,
                          borderRadius: 3,
                          background: cfg.color,
                          marginRight: 8,
                        }}
                      />
                      <span style={{ fontSize: 13 }}>{cfg.label}</span>
                      <span style={{ float: 'right', fontWeight: 600 }}>{val}</span>
                    </div>
                    <Progress
                      percent={Math.round(pct * 10) / 10}
                      strokeColor={cfg.color}
                      trailColor="#f0f0f0"
                      size="small"
                      showInfo={false}
                    />
                  </Col>
                );
              })}
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
