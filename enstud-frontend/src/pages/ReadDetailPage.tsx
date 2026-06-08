import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card, Spin, Typography, Button, Tag, Space, Divider, message,
  Tooltip, Row, Col, Segmented, Skeleton, Drawer, Input,
} from 'antd';
import {
  ArrowLeftOutlined, StarOutlined, StarFilled,
  TranslationOutlined, LinkOutlined, FireOutlined,
  UserOutlined, ClockCircleOutlined, SendOutlined,
} from '@ant-design/icons';
import {
  getArticleDetail, getArticleTranslation,
  toggleBookmark,
} from '../api';

const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

interface ArticleDetail {
  id: number;
  title: string;
  titleCn: string | null;
  url: string;
  source: string;
  sourceIcon: string | null;
  author: string | null;
  coverUrl: string | null;
  content: string | null;
  contentCn: string | null;
  summary: string | null;
  summaryCn: string | null;
  score: number;
  sourceScore: number | null;
  publishedAt: string | null;
}

const activeWordSet = new Set<string>(); // 模拟高亮单词

export default function ReadDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [article, setArticle] = useState<ArticleDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [translating, setTranslating] = useState(false);
  const [translated, setTranslated] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [showOriginal, setShowOriginal] = useState(true);
  const [selectedText, setSelectedText] = useState('');
  const [showWordDrawer, setShowWordDrawer] = useState(false);
  const [transWord, setTransWord] = useState('');

  useEffect(() => {
    if (id) loadArticle(parseInt(id));
  }, [id]);

  const loadArticle = async (articleId: number) => {
    setLoading(true);
    try {
      const res = await getArticleDetail(articleId);
      const data = res.data?.data;
      setArticle(data);
      setBookmarked(data?.bookmarked ?? false);
    } catch {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTranslate = async () => {
    if (!id) return;
    setTranslating(true);
    try {
      const res = await getArticleTranslation(parseInt(id));
      setArticle(res.data?.data);
      setTranslated(true);
      message.success('翻译完成');
    } catch {
      message.error('翻译失败');
    } finally {
      setTranslating(false);
    }
  };

  const handleBookmark = async () => {
    if (!id) return;
    try {
      const res = await toggleBookmark(parseInt(id));
      setBookmarked(res.data?.data);
      message.success(res.data?.data ? '已收藏' : '已取消收藏');
    } catch {
      message.error('操作失败');
    }
  };

  // 划词翻译
  const handleTextSelection = useCallback(() => {
    const selection = window.getSelection();
    const text = selection?.toString().trim();
    if (text && text.length > 0 && text.length < 100) {
      setSelectedText(text);
      setShowWordDrawer(true);
      // 模拟翻译
      setTransWord(`（${text} 的翻译）`);
    }
  }, []);

  const formatTime = (time: string | null) => {
    if (!time) return '';
    return new Date(time).toLocaleDateString('zh-CN', {
      year: 'numeric', month: 'long', day: 'numeric',
    });
  };

  if (loading) {
    return (
      <Card>
        <Skeleton active paragraph={{ rows: 8 }} />
      </Card>
    );
  }

  if (!article) {
    return (
      <Card>
        <Space direction="vertical" align="center" style={{ width: '100%', padding: 40 }}>
          <Text type="secondary">文章不存在</Text>
          <Button onClick={() => navigate('/read')}>返回列表</Button>
        </Space>
      </Card>
    );
  }

  const mainContent = showOriginal || !translated ? article.content : article.contentCn;
  const mainTitle = showOriginal || !translated ? (article.titleCn || article.title) : article.title;

  return (
    <div>
      {/* Top bar */}
      <Card
        style={{ marginBottom: 16 }}
        bodyStyle={{ padding: '12px 24px' }}
      >
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <Button
                icon={<ArrowLeftOutlined />}
                onClick={() => navigate('/read')}
              >
                返回
              </Button>
              <Tag color="orange">{article.source}</Tag>
              {article.sourceScore != null && (
                <Text type="secondary">
                  <FireOutlined /> {article.sourceScore.toLocaleString()}
                </Text>
              )}
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<LinkOutlined />}
                onClick={() => window.open(article.url, '_blank')}
              >
                原文链接
              </Button>
              <Button
                icon={bookmarked ? <StarFilled /> : <StarOutlined />}
                onClick={handleBookmark}
                type={bookmarked ? 'primary' : 'default'}
              >
                {bookmarked ? '已收藏' : '收藏'}
              </Button>
              <Button
                icon={<TranslationOutlined />}
                onClick={handleTranslate}
                loading={translating}
                type={translated ? 'primary' : 'default'}
              >
                翻译全文
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Content card */}
      <Card>
        {/* Title */}
        <Title level={3}>
          {showOriginal && article.titleCn ? article.titleCn : article.title}
        </Title>
        {showOriginal && article.titleCn && (
          <Text type="secondary" style={{ fontSize: 14, display: 'block', marginBottom: 12 }}>
            {article.title}
          </Text>
        )}
        <Space style={{ marginBottom: 16 }}>
          {article.author && <Text><UserOutlined /> {article.author}</Text>}
          {article.publishedAt && (
            <Text type="secondary">
              <ClockCircleOutlined /> {formatTime(article.publishedAt)}
            </Text>
          )}
        </Space>

        <Divider />

        {/* Language toggle (only when translated) */}
        {translated && (
          <div style={{ marginBottom: 16 }}>
            <Segmented
              value={showOriginal ? 'original' : 'translated'}
              onChange={(val) => setShowOriginal(val === 'original')}
              options={[
                { value: 'original', label: 'English' },
                { value: 'translated', label: '中文' },
              ]}
            />
          </div>
        )}

        {/* Content with word selection */}
        <div
          onMouseUp={handleTextSelection}
          style={{
            lineHeight: 1.8,
            fontSize: 15,
            color: '#333',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            userSelect: 'text',
          }}
        >
          {mainContent ? (
            mainContent.split('\n').map((line, i) => (
              <Paragraph key={i} style={{ marginBottom: 8 }}>
                {line || '\u00A0'}
              </Paragraph>
            ))
          ) : (
            <Text type="secondary">暂无内容，点击「原文链接」查看原文</Text>
          )}
        </div>
      </Card>

      {/* Word translation drawer */}
      <Drawer
        title="划词翻译"
        placement="bottom"
        height={180}
        open={showWordDrawer}
        onClose={() => { setShowWordDrawer(false); setSelectedText(''); }}
      >
        {selectedText && (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong style={{ fontSize: 16 }}>{selectedText}</Text>
            <Text style={{ fontSize: 14, color: '#1677ff' }}>{transWord}</Text>
            <Divider style={{ margin: '8px 0' }} />
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={() => {
                message.success('已添加到生词本');
                setShowWordDrawer(false);
              }}
            >
              添加到生词本
            </Button>
          </Space>
        )}
      </Drawer>
    </div>
  );
}
