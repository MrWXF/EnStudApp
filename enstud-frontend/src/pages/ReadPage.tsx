import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card, List, Tag, Select, Space, Typography, Button, message,
  Spin, Empty, Tooltip, Row, Col, Segmented,
} from 'antd';
import {
  FireOutlined, BookOutlined, StarOutlined, StarFilled,
  ClockCircleOutlined, SyncOutlined, UserOutlined,
} from '@ant-design/icons';
import {
  getHotArticles, getSources, getBookmarks,
  toggleBookmark, syncArticles, getRecommendedArticles,
} from '../api';
import { getLearningWords } from '../api/word';
import type { ArticleDTO, SourceDTO } from '../types';
import type { LearningWordDTO } from '../api/word';

const { Text, Title } = Typography;

const sourceColors: Record<string, string> = {
  HN: 'orange',
  GitHub: '#333',
  Medium: 'green',
  InfoQ: 'blue',
};

export default function ReadPage() {
  const navigate = useNavigate();
  const [articles, setArticles] = useState<ArticleDTO[]>([]);
  const [sources, setSources] = useState<SourceDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [activeSource, setActiveSource] = useState('all');
  const [activeTab, setActiveTab] = useState<string>('hot');
  const [learningWords, setLearningWords] = useState<LearningWordDTO[]>([]);
  const [recommendedIds, setRecommendedIds] = useState<Set<number>>(new Set());

  const loadSources = async () => {
    try {
      const res = await getSources();
      setSources(res.data || []);
    } catch {
      // ignore
    }
  };

  const loadArticles = async (source: string) => {
    setLoading(true);
    try {
      if (activeTab === 'bookmarks') {
        const res = await getBookmarks();
        setArticles(res.data || []);
      } else {
        const res = await getHotArticles(source);
        setArticles(res.data || []);
      }
    } catch (e) {
      console.error('Failed to load articles', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadSources();
    loadArticles('all');
    // 加载学习单词和推荐文章（用于标签显示）
    getLearningWords(50).then((res) => setLearningWords(res.data || [])).catch(() => {});
    getRecommendedArticles(20).then((res) => {
      const ids = new Set((res.data || []).map((a) => a.id));
      setRecommendedIds(ids);
    }).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSourceChange = (source: string) => {
    setActiveSource(source);
    loadArticles(source);
  };

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
    loadArticles(activeSource);
  };

  const handleBookmark = async (articleId: number) => {
    try {
      await toggleBookmark(articleId);
      setArticles(prev =>
        prev.map(a => a.id === articleId ? { ...a, bookmarked: !a.bookmarked } : a)
      );
      const current = articles.find(a => a.id === articleId);
      message.success(current?.bookmarked ? '已取消收藏' : '已收藏');
    } catch {
      message.error('操作失败');
    }
  };

  const handleSync = async () => {
    setSyncing(true);
    try {
      const res = await syncArticles();
      message.success(`同步完成，新增 ${res.data || 0} 篇文章`);
      loadArticles(activeSource);
      loadSources();
    } catch {
      message.error('同步失败');
    } finally {
      setSyncing(false);
    }
  };

  /** 计算文章中包含的学习单词数 */
  const countMatchedWords = (article: ArticleDTO): number => {
    if (learningWords.length === 0) return 0;
    const text = `${article.title} ${article.summary || ''}`.toLowerCase();
    return learningWords.filter((w) =>
      text.includes(w.word.toLowerCase()),
    ).length;
  };

  const formatTime = (time: string | null | undefined) => {
    if (!time) return '';
    const d = new Date(time);
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    const hours = Math.floor(diff / 3600000);
    if (hours < 1) return '刚刚';
    if (hours < 24) return `${hours}小时前`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}天前`;
    return d.toLocaleDateString('zh-CN');
  };

  return (
    <div>
      {/* Header */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Space size="middle" align="center">
            <Title level={4} style={{ margin: 0 }}>🔥 热门阅读</Title>
            <Segmented
              value={activeTab}
              onChange={handleTabChange}
              options={[
                { value: 'hot', label: <><FireOutlined /> 热门文章</> },
                { value: 'bookmarks', label: <><StarOutlined /> 我的收藏</> },
              ]}
            />
          </Space>
        </Col>
        <Col>
          <Space>
            <Select
              value={activeSource}
              onChange={handleSourceChange}
              style={{ width: 160 }}
              options={[
                { value: 'all', label: '全部来源' },
                ...sources.map(s => ({
                  value: s.id,
                  label: `${s.name} (${s.activeCount})`,
                })),
              ]}
            />
            <Button
              icon={<SyncOutlined spin={syncing} />}
              onClick={handleSync}
              loading={syncing}
            >
              同步
            </Button>
          </Space>
        </Col>
      </Row>

      {/* Article List */}
      <Spin spinning={loading}>
        {articles.length === 0 ? (
          <Empty description={activeTab === 'bookmarks' ? '还没有收藏的文章' : '暂无文章，点击同步拉取'} />
        ) : (
          <List
            grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 2 }}
            dataSource={articles}
            renderItem={(article) => (
              <List.Item>
                <Card
                  hoverable
                  onClick={() => navigate(`/read/${article.id}`)}
                  style={{ height: '100%' }}
                  actions={[
                    <Tooltip title={article.bookmarked ? '取消收藏' : '收藏'} key="bookmark">
                      <span onClick={e => { e.stopPropagation(); handleBookmark(article.id); }}>
                        {article.bookmarked
                          ? <StarFilled style={{ color: '#faad14' }} />
                          : <StarOutlined />
                        }
                      </span>
                    </Tooltip>,
                    <Tooltip title="打开原文" key="url">
                      <span onClick={e => { e.stopPropagation(); window.open(article.url, '_blank'); }}>
                        <BookOutlined /> 原文
                      </span>
                    </Tooltip>,
                  ]}
                >
                  <Card.Meta
                    title={
                      <Space direction="vertical" size={2} style={{ width: '100%' }}>
                        <Space>
                          <Tag color={sourceColors[article.source] || 'default'}>
                            {article.source}
                          </Tag>
                          {article.author && (
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              <UserOutlined /> {article.author}
                            </Text>
                          )}
                        </Space>
                        <Text strong style={{ fontSize: 15 }}>
                          {article.titleCn || article.title}
                        </Text>
                        {article.titleCn && (
                          <Text type="secondary" style={{ fontSize: 13 }}>
                            {article.title}
                          </Text>
                        )}
                      </Space>
                    }
                    description={
                      <div>
                        {article.summary && (
                          <Text
                            ellipsis
                            style={{ marginBottom: 8, color: '#666', display: 'block' }}
                          >
                            {article.summary}
                          </Text>
                        )}
                        {recommendedIds.has(article.id) && countMatchedWords(article) > 0 && (
                          <div style={{ marginTop: 8 }}>
                            <Tag color="blue" style={{ fontSize: 11 }}>📖 {countMatchedWords(article)} 个学习词</Tag>
                          </div>
                        )}
                        <Row style={{ marginTop: 4 }}>
                          <Space size={12}>
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              <FireOutlined /> {(article.score || 0).toLocaleString()}
                            </Text>
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              <ClockCircleOutlined /> {formatTime(article.publishedAt)}
                            </Text>
                          </Space>
                        </Row>
                      </div>
                    }
                  />
                </Card>
              </List.Item>
            )}
          />
        )}
      </Spin>
    </div>
  );
}
