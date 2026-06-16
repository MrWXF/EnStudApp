import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, List, Tag, Button, Space, Input, Empty, Spin, Avatar } from 'antd';
import { EyeOutlined, LikeOutlined, MessageOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { getCategories, getPosts } from '../api';
import { formatRelativeTime } from '../utils/format';
import { getUserAvatar } from '../utils/user';
import type { ForumCategory, ForumPost } from '../types';

export default function ForumPage() {
  const [categories, setCategories] = useState<ForumCategory[]>([]);
  const [posts, setPosts] = useState<ForumPost[]>([]);
  const [catId, setCatId] = useState<number | undefined>();
  const [cursor, setCursor] = useState<string | undefined>();
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [searchText, setSearchText] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    getCategories().then(res => setCategories(res.data || []));
  }, []);

  const loadPosts = useCallback(async (catIdVal?: number, cursorVal?: string) => {
    setLoading(true);
    try {
      const res = await getPosts(catIdVal, cursorVal);
      const list = res.data || [];
      if (cursorVal) {
        setPosts(prev => [...prev, ...list]);
      } else {
        setPosts(list);
      }
      setHasMore(list.length >= 20);
      if (list.length > 0) {
        setCursor(list[list.length - 1].createdAt);
      } else {
        setCursor(undefined);
      }
    } catch {
      // 错误已在 client 统一处理
    } finally {
      setLoading(false);
    }
  }, []);

  // 切换板块时重新加载
  useEffect(() => {
    setCursor(undefined);
    loadPosts(catId, undefined);
  }, [catId, loadPosts]);

  const handleLoadMore = async () => {
    if (loadingMore || !hasMore || !cursor) return;
    setLoadingMore(true);
    try {
      const res = await getPosts(catId, cursor);
      const list = res.data || [];
      setPosts(prev => [...prev, ...list]);
      setHasMore(list.length >= 20);
      if (list.length > 0) {
        setCursor(list[list.length - 1].createdAt);
      } else {
        setHasMore(false);
      }
    } catch {
      // 错误已在 client 统一处理
    } finally {
      setLoadingMore(false);
    }
  };

  // 前端本地搜索过滤
  const filteredPosts = searchText.trim()
    ? posts.filter(p =>
        p.title.toLowerCase().includes(searchText.toLowerCase()) ||
        (p.summary || '').toLowerCase().includes(searchText.toLowerCase())
      )
    : posts;

  const renderPostItem = (p: ForumPost) => (
    <List.Item
      style={{ cursor: 'pointer', padding: '16px 0' }}
      onClick={() => navigate(`/forum/${p.id}`)}
      actions={[
        <span key="view"><EyeOutlined /> {p.viewCount || 0}</span>,
        <span key="like"><LikeOutlined /> {p.likeCount || 0}</span>,
        <span key="reply"><MessageOutlined /> {p.replyCount || 0}</span>,
      ]}
    >
      <List.Item.Meta
        avatar={<Avatar style={{ backgroundColor: '#1677ff' }}>{getUserAvatar(p.authorName)}</Avatar>}
        title={
          <Space>
            {p.isPinned && <Tag color="red">置顶</Tag>}
            {p.isEssence && <Tag color="gold">精华</Tag>}
            <span style={{ fontSize: 16 }}>{p.title}</span>
          </Space>
        }
        description={
          <Space split={<span style={{ color: '#d9d9d9' }}>·</span>}>
            <span style={{ color: '#1677ff' }}>{p.authorName || '匿名用户'}</span>
            <Tag>{p.categoryName}</Tag>
            <span style={{ color: '#8c8c8c' }}>{formatRelativeTime(p.createdAt)}</span>
          </Space>
        }
      />
      <div style={{ color: '#666', lineHeight: 1.6, paddingLeft: 44 }}>{p.summary}</div>
      {p.tags && (
        <div style={{ paddingLeft: 44, marginTop: 8 }}>
          {p.tags.split(',').filter(Boolean).map((t: string, i: number) => (
            <Tag key={i} style={{ borderRadius: 12 }}>#{t.trim()}</Tag>
          ))}
        </div>
      )}
    </List.Item>
  );

  return (
    <Card
      title="学习论坛"
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/forum/new')}>发帖</Button>}
    >
      {/* 板块分类 + 搜索框 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
        <Space wrap>
          <Button type={!catId ? 'primary' : 'default'} onClick={() => setCatId(undefined)}>全部</Button>
          {categories.map((c) => (
            <Button key={c.id} type={catId === c.id ? 'primary' : 'default'} onClick={() => setCatId(c.id)}>
              {c.icon && <span style={{ marginRight: 4 }}>{c.icon}</span>}{c.name}
            </Button>
          ))}
        </Space>
        <Input
          placeholder="搜索帖子..."
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 220 }}
          allowClear
        />
      </div>

      {/* 帖子列表 */}
      <Spin spinning={loading}>
        {filteredPosts.length === 0 && !loading ? (
          <Empty description={searchText ? '没有找到匹配的帖子' : '暂无帖子，快来发第一帖吧'} />
        ) : (
          <List
            dataSource={filteredPosts}
            itemLayout="vertical"
            renderItem={renderPostItem}
          />
        )}
      </Spin>

      {/* 加载更多 */}
      {!loading && hasMore && filteredPosts.length > 0 && (
        <div style={{ textAlign: 'center', padding: '16px 0' }}>
          <Button onClick={handleLoadMore} loading={loadingMore}>
            {loadingMore ? '加载中...' : '加载更多'}
          </Button>
        </div>
      )}
    </Card>
  );
}
