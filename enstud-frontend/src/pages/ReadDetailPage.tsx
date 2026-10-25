import { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card, Typography, Button, Tag, Space, Divider, message,
  Row, Col, Segmented, Skeleton, Spin, Tooltip,
} from 'antd';
import {
  ArrowLeftOutlined, StarOutlined, StarFilled,
  TranslationOutlined, LinkOutlined, FireOutlined,
  UserOutlined, ClockCircleOutlined, SendOutlined, BookOutlined,
  BulbOutlined,
} from '@ant-design/icons';
import {
  getArticleDetail, getArticleTranslation,
  toggleBookmark, wordLookup,
} from '../api';
import { getLearningWords } from '../api/word';
import type { ArticleDetailDTO, WordLookupResponse } from '../types';
import type { LearningWordDTO } from '../api/word';

const { Title, Paragraph, Text } = Typography;

/** 将文本中的学习单词高亮渲染 */
function HighlightedContent({
  text,
  learningWords,
}: {
  text: string;
  learningWords: LearningWordDTO[];
}) {
  if (!learningWords.length || !text) {
    return <>{text}</>;
  }

  // 构建正则：匹配所有学习单词（不区分大小写，整词匹配）
  const words = learningWords.map((w) => w.word).filter(Boolean);
  if (words.length === 0) return <>{text}</>;

  const escaped = words.map((w) => w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'));
  const regex = new RegExp(`\\b(${escaped.join('|')})\\b`, 'gi');

  const wordMap = new Map(
    learningWords.map((w) => [w.word.toLowerCase(), w]),
  );

  const parts: Array<{ text: string; isMatch: boolean; word?: LearningWordDTO }> = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ text: text.slice(lastIndex, match.index), isMatch: false });
    }
    const matchedWord = wordMap.get(match[1].toLowerCase());
    parts.push({ text: match[1], isMatch: true, word: matchedWord });
    lastIndex = regex.lastIndex
  }
  if (lastIndex < text.length) {
    parts.push({ text: text.slice(lastIndex), isMatch: false });
  }

  return (
    <>
      {parts.map((part, i) =>
        part.isMatch && part.word ? (
          <Tooltip
            key={i}
            title={
              <span>
                <strong>{part.word.word}</strong>
                {part.word.definitionCn && (
                  <> — {part.word.definitionCn}</>
                )}
              </span>
            }
          >
            <mark
              style={{
                background: '#e6f4ff',
                color: '#1677ff',
                padding: '1px 3px',
                borderRadius: 3,
                textDecoration: 'underline',
                textDecorationColor: '#91caff',
                cursor: 'help',
              }}
            >
              {part.text}
            </mark>
          </Tooltip>
        ) : (
          <span key={i}>{part.text}</span>
        ),
      )}
    </>
  );
}

export default function ReadDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [article, setArticle] = useState<ArticleDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [translating, setTranslating] = useState(false);
  const [translated, setTranslated] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const [showOriginal, setShowOriginal] = useState(true);

  // 学习单词高亮状态
  const [learningWords, setLearningWords] = useState<LearningWordDTO[]>([]);

  // 划词查词状态
  const [popupVisible, setPopupVisible] = useState(false);
  const [popupPos, setPopupPos] = useState({ x: 0, y: 0 });
  const [selectedText, setSelectedText] = useState('');
  const [lookupResult, setLookupResult] = useState<WordLookupResponse | null>(null);
  const [lookupLoading, setLookupLoading] = useState(false);
  const popupRef = useRef<HTMLDivElement>(null);

  // 加载文章 + 学习单词
  useEffect(() => {
    if (!id) return;
    const articleId = parseInt(id);

    setLoading(true);
    Promise.all([
      getArticleDetail(articleId).then((res) => res.data),
      getLearningWords(50).then((res) => res.data || []).catch(() => []),
    ])
      .then(([data, words]) => {
        setArticle(data);
        setLearningWords(words);
        setBookmarked(false);
      })
      .finally(() => setLoading(false));
  }, [id]);

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

  // 计算文章中包含的学习单词数
  const matchedWordsInArticle = useMemo(() => {
    if (!article?.content || learningWords.length === 0) return [];
    const content = article.content.toLowerCase();
    return learningWords.filter((w) =>
      content.includes(w.word.toLowerCase()),
    );
  }, [article, learningWords]);

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
    if (start > 0) {
      const sentenceStart = ctx.indexOf('. ');
      if (sentenceStart > 0 && sentenceStart < 20) ctx = ctx.substring(sentenceStart + 2);
    }
    return ctx.trim();
  };

  // 划词翻译
  const handleTextSelection = useCallback(() => {
    const selection = window.getSelection();
    const text = selection?.toString().trim();
    if (!text || text.length === 0 || text.length > 100) {
      setPopupVisible(false);
      return;
    }

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

    const articleId = parseInt(id || '0');
    if (articleId > 0) {
      setLookupLoading(true);
      const contextSentence = getContextSentence(text);
      wordLookup({ selectedText: text, articleId, contextSentence })
        .then((res) => {
          setLookupResult(res.data);
        })
        .catch(() => {
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

  const handleAddToWordbook = async () => {
    if (!lookupResult) return;
    const articleId = parseInt(id || '0');
    if (articleId === 0) return;

    if (lookupResult.addedToWordbook) {
      message.success('已添加到生词本');
      setPopupVisible(false);
      return;
    }

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

  // 点击外部关闭 Popup
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
      {/* 学习单词提示条 */}
      {matchedWordsInArticle.length > 0 && showOriginal && (
        <Card
          size="small"
          style={{ marginBottom: 12, background: '#f6ffed', borderColor: '#b7eb8f' }}
          styles={{ body: { padding: '8px 16px' } }}
        >
          <Space wrap>
            <BulbOutlined style={{ color: '#52c41a' }} />
            <Text strong style={{ color: '#52c41a' }}>
              本文包含 {matchedWordsInArticle.length} 个你正在学习的单词
            </Text>
            <span style={{ color: '#8c8c8c', fontSize: 12 }}>
              （{matchedWordsInArticle.slice(0, 5).map((w) => w.word).join(', ')}
              {matchedWordsInArticle.length > 5 ? '...' : ''}）
            </span>
          </Space>
        </Card>
      )}

      {/* Top bar */}
      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '12px 24px' } }}
      >
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/read')}>
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
                原文
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
                翻译
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Content card */}
      <Card>
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

        {/* Content with word highlighting */}
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
                {showOriginal ? (
                  <HighlightedContent text={line} learningWords={learningWords} />
                ) : (
                  line || ' '
                )}
              </Paragraph>
            ))
          ) : (
            <Text type="secondary">暂无内容，点击「原文链接」查看原文</Text>
          )}
        </div>
      </Card>

      {/* 划词查词 Popover */}
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
