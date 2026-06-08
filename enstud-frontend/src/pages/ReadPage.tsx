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
  toggleBookmark, syncArticles,
} from '../api';

const { Text, Title, Paragraph } = Typography;

interface Article {
  id: number;
  title: string;
  titleCn: string | null;
  url: string;
  source: string;
  sourceIcon: string | null;
  summary: string | null;
  summaryCn: string | null;
  coverUrl: string | null;
  author: string | null;
  score: number;
  publishedAt: string | null;
  bookmarked: boolean;
}

interface Source {
  id: string;
  name: string;
  icon: string | null;
  count: number;
}

const sourceColors: Record<string, string> = {
  HN: 'orange',
  GitHub: '#333',
  Medium: 'green',
  InfoQ: 'blue',
};

const sourceLabels: Record<string, string> = {
  HN: 'Hacker News',
  GitHub: 'GitHub',
  Medium: 'Medium',
  InfoQ: 'InfoQ',
};

export default function ReadPage() {
  const navigate = useNavigate();
  const [articles, setArticles] = useState<Article[]>([]);
  const [sources, setSources] = useState<Source[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [activeSource, setActiveSource] = useState('all');
  const [activeTab, setActiveTab] = useState('hot');

  useEffect(() => {
    loadSources();
    loadArticles('all');
  }, []);

  const loadSources = async () => {
    try {
      const res = await getSources();
      setSources(res.data?.data || []);
    } catch {
      // ignore
    }
  };

  const loadArticles = async (source: string) => {
    setLoading(true);
    try {
      if (activeTab === 'bookmarks') {
        const res = await getBookmarks();
        setArticles(res.data?.data || []);
      } else {
        const res = await getHotArticles(source);
        setArticles(res.data?.data || []);
      }
    } catch (e) {
      console.error('Failed to load articles', e);
    } finally {
      setLoading(false);
    }
  };

  const handleSourceChange = (source: string) => {
    setActiveSource(source);
    loadArticles(source);
  };

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
    if (tab === 'bookmarks') {
      loadArticles(activeSource);
    } else {
      loadArticles(activeSource);
    }
  };

  const handleBookmark = async (articleId: number) => {
    try {
      const res = await toggleBookmark(articleId);
      const newBookmarked = res.data?.data;
      setArticles(prev =>
        prev.map(a => a.id === articleId ? { ...a, bookmarked: newBookmarked } : a)
      );
      message.success(newBookmarked ? '已收藏' : '已取消收藏');
    } catch {
      message.error('操作失败');
    }
  };

  const handleSync = async () => {
    setSyncing(true);
    try {
      const res = await syncArticles();
      message.success(`同步完成，新增 ${res.data?.data || 0} 篇文章`);
      loadArticles(activeSource);
      loadSources();
    } catch {
      message.error('同步失败');
    } finally {
      setSyncing(false);
    }
  };

  const formatTime = (time: string | null) => {
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
                  label: `${s.name} (${s.count})`,
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
                          <Paragraph
                            ellipsis={{ rows: 2 }}
                            style={{ marginBottom: 8, color: '#666' }}
                          >
                            {article.summary}
                          </Paragraph>
                        )}
                        <Row justify="space-between" align="middle">
                          <Col>
                            <Space size={12}>
                              <Text type="secondary" style={{ fontSize: 12 }}>
                                <FireOutlined /> {(article.score || 0).toLocaleString()}
                              </Text>
                              <Text type="secondary" style={{ fontSize: 12 }}>
                                <ClockCircleOutlined /> {formatTime(article.publishedAt)}
                              </Text>
                            </Space>
                          </Col>
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
