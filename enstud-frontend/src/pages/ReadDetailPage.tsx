import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card, Typography, Button, Tag, Space, Divider, message,
  Row, Col, Segmented, Skeleton, Spin,
} from 'antd';
import {
  ArrowLeftOutlined, StarOutlined, StarFilled,
  TranslationOutlined, LinkOutlined, FireOutlined,
  UserOutlined, ClockCircleOutlined, SendOutlined, BookOutlined,
} from '@ant-design/icons';
import {
  getArticleDetail, getArticleTranslation,
  toggleBookmark, wordLookup,
} from '../api';
import type { ArticleDetailDTO, WordLookupResponse } from '../types';

const { Title, Paragraph, Text } = Typography;

export default function ReadDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [article, setArticle] = useState<ArticleDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [translating, setTranslating] = useState(false);
  const [translated, setTranslated] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [showOriginal, setShowOriginal] = useState(true);

  // 划词查词状态
  const [popupVisible, setPopupVisible] = useState(false);
  const [popupPos, setPopupPos] = useState({ x: 0, y: 0 });
  const [selectedText, setSelectedText] = useState('');
  const [lookupResult, setLookupResult] = useState<WordLookupResponse | null>(null);
  const [lookupLoading, setLookupLoading] = useState(false);
  const popupRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (id) loadArticle(parseInt(id));
  }, [id]);

  const loadArticle = async (articleId: number) => {
    setLoading(true);
    try {
      const res = await getArticleDetail(articleId);
      const data = res.data;
      setArticle(data);
      // bookmarked 状态不来自 ArticleDetailDTO（只在列表中有），用本地 state 控制
      setBookmarked(false);
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
      if (res.data) {
        setArticle(res.data);
        setTranslated(true);
      }
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
      await toggleBookmark(parseInt(id));
      const newBookmarked = !bookmarked;
      setBookmarked(newBookmarked);
      message.success(newBookmarked ? '已收藏' : '已取消收藏');
    } catch {
      message.error('操作失败');
    }
  };

  // 划词翻译 — 选中文本后自动查询
  const handleTextSelection = useCallback(() => {
    const selection = window.getSelection();
    const text = selection?.toString().trim();
    if (!text || text.length === 0 || text.length > 100) {
      setPopupVisible(false);
      return;
    }

    // 获取选中范围的位置
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0);
      const rect = range.getBoundingClientRect();
      setPopupPos({
        x: rect.left + rect.width / 2,
        y: rect.top + window.scrollY - 10,
      });
    }

    setSelectedText(text);
    setPopupVisible(true);
    setLookupResult(null);

    // 自动调用后端查词 API
    const articleId = parseInt(id || '0');
    if (articleId > 0) {
      setLookupLoading(true);
      // 取选中文本所在段落作为上下文
      const contextSentence = getContextSentence(text);
      wordLookup({ selectedText: text, articleId, contextSentence })
        .then((res) => {
          setLookupResult(res.data);
        })
        .catch(() => {
          // 接口调用失败时，使用简单本地翻译降级
          setLookupResult({
            originalWord: text,
            wordCount: text.split(/\s+/).length,
            translation: `（${text} 的翻译）`,
            phonetic: '',
            partOfSpeech: '',
            addedToWordbook: false,
            wordRecordId: null,
          });
        })
        .finally(() => setLookupLoading(false));
    }
  }, [id]);

  // 从选中文本所在的 Paragraph 元素提取上下文句子
  const getContextSentence = (selected: string): string => {
    const el = document.querySelector('[data-content-area]');
    if (!el) return '';
    const fullText = el.textContent || '';
    const idx = fullText.indexOf(selected);
    if (idx < 0) return '';
    const start = Math.max(0, idx - 80);
    const end = Math.min(fullText.length, idx + selected.length + 80);
    let ctx = fullText.substring(start, end);
    // 尝试找到句子边界
    if (start > 0) {
      const sentenceStart = ctx.indexOf('. ');
      if (sentenceStart > 0 && sentenceStart < 20) ctx = ctx.substring(sentenceStart + 2);
    }
    return ctx.trim();
  };

  // 添加到生词本（如果后端自动添加失败，手动触发）
  const handleAddToWordbook = async () => {
    if (!lookupResult) return;
    const articleId = parseInt(id || '0');
    if (articleId === 0) return;

    // 如果后端已经自动添加成功
    if (lookupResult.addedToWordbook) {
      message.success('已添加到生词本');
      setPopupVisible(false);
      return;
    }

    // 如果自动添加失败，手动调用
    setLookupLoading(true);
    try {
      const contextSentence = getContextSentence(selectedText);
      const res = await wordLookup({
        selectedText, articleId, contextSentence,
      });
      if (res.data?.addedToWordbook || res.data?.wordRecordId != null) {
        setLookupResult(res.data);
        message.success('已添加到生词本');
      } else {
        message.warning('添加失败，请稍后重试');
      }
    } catch {
      message.error('添加失败');
    } finally {
      setLookupLoading(false);
      setPopupVisible(false);
    }
  };

  // 点击页面其他区域关闭 Popup
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (popupRef.current && !popupRef.current.contains(e.target as Node)) {
        setPopupVisible(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
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

  return (
    <div style={{ position: 'relative' }}>
      {/* Top bar */}
      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '12px 24px' } }}
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
              {article.score != null && (
                <Text type="secondary">
                  <FireOutlined /> {article.score.toLocaleString()}
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
          data-content-area
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

      {/* 划词查词 Popover — 浮动在选中文本上方 */}
      {popupVisible && selectedText && (
        <div
          ref={popupRef}
          style={{
            position: 'absolute',
            left: Math.max(16, Math.min(popupPos.x - 120, window.innerWidth - 280)),
            top: popupPos.y,
            zIndex: 1050,
            background: '#fff',
            borderRadius: 12,
            boxShadow: '0 4px 20px rgba(0,0,0,0.12)',
            padding: '12px 16px',
            minWidth: 240,
            maxWidth: 320,
            transform: 'translateY(-100%)',
          }}
        >
          {lookupLoading ? (
            <div style={{ textAlign: 'center', padding: '12px 0' }}>
              <Spin size="small" />
              <Text type="secondary" style={{ marginLeft: 8 }}>查询中…</Text>
            </div>
          ) : lookupResult ? (
            <Space direction="vertical" size={4} style={{ width: '100%' }}>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
                <Text strong style={{ fontSize: 16 }}>{lookupResult.originalWord}</Text>
                {lookupResult.phonetic && (
                  <Text type="secondary" style={{ fontSize: 12 }}>/ {lookupResult.phonetic} /</Text>
                )}
                {lookupResult.partOfSpeech && (
                  <Tag style={{ fontSize: 11, lineHeight: '18px' }}>{lookupResult.partOfSpeech}</Tag>
                )}
              </div>
              <Text style={{ fontSize: 14, color: '#1677ff' }}>{lookupResult.translation}</Text>
              <Divider style={{ margin: '6px 0' }} />
              <Button
                type="primary"
                size="small"
                icon={lookupResult.addedToWordbook ? <BookOutlined /> : <SendOutlined />}
                onClick={handleAddToWordbook}
                disabled={lookupResult.addedToWordbook}
              >
                {lookupResult.addedToWordbook ? '已加入生词本' : '加入生词本'}
              </Button>
            </Space>
          ) : (
            <Text type="secondary">查询失败，请重试</Text>
          )}
        </div>
      )}
    </div>
  );
}
