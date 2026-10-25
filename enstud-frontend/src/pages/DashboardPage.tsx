import { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Spin, Typography, Progress, Button, Tag, Space } from 'antd';
import {
  BookOutlined, ReadOutlined, EditOutlined,
  MessageOutlined, RiseOutlined, FireOutlined,
  RobotOutlined, ThunderboltOutlined, ExperimentOutlined, ShareAltOutlined,
} from '@ant-design/icons';
import { getUserStats, getAiUsage } from '../api/dashboard';
import { getRecommendedArticles } from '../api/reading';
import type { AiUsageResponse, AiUsageDetail } from '../api/dashboard';
import type { UserStats, ArticleDTO } from '../types';
import { useDashboardStore } from '../stores';
import { useNavigate } from 'react-router-dom';
import ShareCardModal from '../components/ShareCardModal';

const { Title, Text } = Typography;

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
  const [shareOpen, setShareOpen] = useState(false);
  const [aiUsage, setAiUsage] = useState<AiUsageResponse | null>(null);
  const [recommendedArticles, setRecommendedArticles] = useState<ArticleDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { setStats: setStoreStats } = useDashboardStore();

  useEffect(() => {
    Promise.all([
      getUserStats().then((res: { data: UserStats }) => res.data),
      getAiUsage().then((res: { data: AiUsageResponse }) => res.data).catch(() => null),
      getRecommendedArticles(6).then((res: { data: ArticleDTO[] }) => res.data).catch(() => []),
    ])
      .then(([statsData, usageData, articles]) => {
        setStats(statsData);
        setAiUsage(usageData);
        setRecommendedArticles(articles);
        if (statsData) {
          setStoreStats({
            wordsToday: statsData.todayLearnedWords,
            wordsTotal: statsData.totalLearnedWords,
            articlesRead: statsData.totalReadArticles,
            chatCount: statsData.totalChats,
            writingCount: statsData.totalWritings,
            streak: statsData.streakDays,
          });
        }
      })
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

      {/* 快捷工具：词汇量测试 / 分享卡片 */}
      <div style={{ marginBottom: 24 }}>
        <Space>
          <Button icon={<ExperimentOutlined />} onClick={() => navigate('/vocab-test')}>
            词汇量测试
          </Button>
          <Button icon={<ShareAltOutlined />} onClick={() => setShareOpen(true)}>
            生成分享卡片
          </Button>
        </Space>
      </div>

      <ShareCardModal open={shareOpen} onClose={() => setShareOpen(false)} variant="stats" />

      {/* 顶部统计卡片 — 四维度学习进度 */}
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
          <Card
            hoverable
            style={stats.streakDays >= 7 ? { borderColor: '#ff4d4f', background: '#fff2f0' } : {}}
          >
            <Statistic
              title={
                <span>
                  <FireOutlined style={{ color: stats.streakDays >= 7 ? '#ff4d4f' : '#faad14' }} />{' '}
                  连续学习
                </span>
              }
              value={stats.streakDays}
              suffix="天"
              valueStyle={{ color: stats.streakDays >= 7 ? '#ff4d4f' : '#faad14', fontWeight: 700 }}
              prefix={<FireOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 学习闭环进度 */}
      <Card style={{ marginTop: 24 }} styles={{ body: { padding: '20px 24px' } }}>
        <Title level={5} style={{ marginBottom: 16 }}>
          <ThunderboltOutlined /> 学习闭环
        </Title>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flexWrap: 'wrap', gap: 8 }}>
          {/* 阅读 */}
          <div
            onClick={() => navigate('/read')}
            style={{ textAlign: 'center', cursor: 'pointer', padding: '8px 16px', borderRadius: 8, transition: 'background 0.2s' }}
            onMouseEnter={(e) => (e.currentTarget.style.background = '#f0f5ff')}
            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
          >
            <div style={{ fontSize: 28 }}>📖</div>
            <div style={{ fontWeight: 600, fontSize: 14 }}>阅读文章</div>
            <div style={{ color: '#1677ff', fontSize: 20, fontWeight: 700 }}>{stats.totalReadArticles}</div>
            <div style={{ color: '#8c8c8c', fontSize: 11 }}>篇</div>
          </div>
          <div style={{ fontSize: 20, color: '#d9d9d9' }}>→</div>
          {/* 积累 */}
          <div
            onClick={() => navigate('/word')}
            style={{ textAlign: 'center', cursor: 'pointer', padding: '8px 16px', borderRadius: 8, transition: 'background 0.2s' }}
            onMouseEnter={(e) => (e.currentTarget.style.background = '#f6ffed')}
            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
          >
            <div style={{ fontSize: 28 }}>📚</div>
            <div style={{ fontWeight: 600, fontSize: 14 }}>积累单词</div>
            <div style={{ color: '#52c41a', fontSize: 20, fontWeight: 700 }}>{stats.totalLearnedWords}</div>
            <div style={{ color: '#8c8c8c', fontSize: 11 }}>词</div>
          </div>
          <div style={{ fontSize: 20, color: '#d9d9d9' }}>→</div>
          {/* 复习 */}
          <div
            onClick={() => navigate('/word')}
            style={{ textAlign: 'center', cursor: 'pointer', padding: '8px 16px', borderRadius: 8, transition: 'background 0.2s' }}
            onMouseEnter={(e) => (e.currentTarget.style.background = '#fff7e6')}
            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
          >
            <div style={{ fontSize: 28 }}>🧠</div>
            <div style={{ fontWeight: 600, fontSize: 14 }}>复习巩固</div>
            <div style={{ color: '#faad14', fontSize: 20, fontWeight: 700 }}>
              {Object.entries(dist).reduce((sum, [key, val]: [string, number]) => {
                const cfg = LEVEL_CONFIG.find((c) => c.key === key);
                return cfg && ['notLearned', 'fuzzy', 'familiar'].includes(cfg.key) ? sum + val : sum;
              }, 0)}
            </div>
            <div style={{ color: '#8c8c8c', fontSize: 11 }}>待复习</div>
          </div>
          <div style={{ fontSize: 20, color: '#d9d9d9' }}>→</div>
          {/* 对话 */}
          <div
            onClick={() => navigate('/chat')}
            style={{ textAlign: 'center', cursor: 'pointer', padding: '8px 16px', borderRadius: 8, transition: 'background 0.2s' }}
            onMouseEnter={(e) => (e.currentTarget.style.background = '#f9f0ff')}
            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
          >
            <div style={{ fontSize: 28 }}>💬</div>
            <div style={{ fontWeight: 600, fontSize: 14 }}>对话运用</div>
            <div style={{ color: '#722ed1', fontSize: 20, fontWeight: 700 }}>{stats.totalChats}</div>
            <div style={{ color: '#8c8c8c', fontSize: 11 }}>次</div>
          </div>
        </div>
      </Card>

      {/* AI 每日用量 + 学习闭环快捷入口 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        {/* AI 用量进度条 */}
        {aiUsage && (
          <Col xs={24} md={12}>
            <Card title={<><RobotOutlined /> AI 今日用量</>} size="small">
              <Space direction="vertical" style={{ width: '100%' }}>
                {Object.entries(aiUsage.features).map(([key, detail]: [string, AiUsageDetail]) => (
                  <div key={key}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                      <Text>
                        {key === 'chat' ? '💬 对话' : key === 'writing' ? '✍️ 写作' : '🌐 翻译'}
                      </Text>
                      <Text type="secondary">{detail.usedToday} / {detail.dailyLimit}</Text>
                    </div>
                    <Progress
                      percent={detail.dailyLimit > 0 ? Math.round((detail.usedToday / detail.dailyLimit) * 100) : 0}
                      strokeColor={detail.usedToday >= detail.dailyLimit ? '#ff4d4f' : '#1677ff'}
                      size="small"
                    />
                  </div>
                ))}
              </Space>
            </Card>
          </Col>
        )}

        {/* 学习闭环快捷入口 */}
        <Col xs={24} md={aiUsage ? 12 : 24}>
          <Card title={<><ThunderboltOutlined /> 快捷入口</>} size="small">
            <Space wrap>
              <Button type="primary" icon={<RobotOutlined />} onClick={() => navigate('/chat')}>
                🤖 AI 复习对话
              </Button>
              <Button icon={<ReadOutlined />} onClick={() => navigate('/read')}>
                📖 推荐文章
              </Button>
              <Button icon={<EditOutlined />} onClick={() => navigate('/writing')}>
                ✍️ 开始写作
              </Button>
              <Button icon={<BookOutlined />} onClick={() => navigate('/word')}>
                📚 背单词
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 推荐文章（学习闭环） */}
      {recommendedArticles.length > 0 && (
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col span={24}>
            <Card
              title="📖 为你推荐 — 包含你正在学习的单词"
              extra={<Button type="link" onClick={() => navigate('/read')}>查看全部</Button>}
            >
              <Row gutter={[12, 12]}>
                {recommendedArticles.map((article) => (
                  <Col xs={24} sm={12} md={8} key={article.id}>
                    <Card
                      hoverable
                      size="small"
                      onClick={() => navigate(`/read/${article.id}`)}
                      style={{ height: '100%' }}
                    >
                      <Card.Meta
                        title={article.title}
                        description={
                          <div>
                            <Text type="secondary" ellipsis style={{ fontSize: 12 }}>
                              {article.summary}
                            </Text>
                            <div style={{ marginTop: 8 }}>
                              <Tag color="blue">{article.source}</Tag>
                              {article.bookmarked && <Tag color="gold">已收藏</Tag>}
                            </div>
                          </div>
                        }
                      />
                    </Card>
                  </Col>
                ))}
              </Row>
            </Card>
          </Col>
        </Row>
      )}

      {/* 记忆等级分布 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
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
