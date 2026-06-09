import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Button, Input, List, Tag, Space, message, Avatar, Popconfirm, Tooltip } from 'antd';
import { LikeOutlined, LikeFilled, ArrowLeftOutlined, DeleteOutlined, UserOutlined, EyeOutlined } from '@ant-design/icons';
import { getPostDetail, replyPost, toggleLike, deletePost } from '../api';
import { formatRelativeTime, formatDateTime } from '../utils/format';
import { getCurrentUserId, getUserAvatar } from '../utils/user';
import type { PostDetail, ForumReply, ApiResponse } from '../types';

export default function ForumDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [post, setPost] = useState<PostDetail | null>(null);
  const [reply, setReply] = useState('');
  const [liked, setLiked] = useState(false);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const currentUserId = getCurrentUserId();

  useEffect(() => { loadPost(); }, [id]);

  const loadPost = async () => {
    setLoading(true);
    try {
      const res: ApiResponse<PostDetail> = await getPostDetail(Number(id));
      setPost(res.data);
    } catch {
      // 错误已在 client 统一处理
    } finally {
      setLoading(false);
    }
  };

  const handleLike = async () => {
    try {
      await toggleLike('POST', Number(id));
      setLiked(prev => !prev);
      loadPost();
    } catch {
      // 错误已在 client 统一处理
    }
  };

  const handleReplyLike = async (replyId: number) => {
    try {
      await toggleLike('REPLY', replyId);
      loadPost();
    } catch {
      // 错误已在 client 统一处理
    }
  };

  const handleReply = async () => {
    if (!reply.trim()) return;
    try {
      await replyPost(Number(id), reply);
      setReply('');
      loadPost();
      message.success('回复成功');
    } catch {
      // 错误已在 client 统一处理
    }
  };

  const handleDelete = async () => {
    try {
      await deletePost(Number(id));
      message.success('帖子已删除');
      navigate('/forum');
    } catch {
      // 错误已在 client 统一处理
    }
  };

  if (loading) return <Card loading />;

  if (!post) return <Card><div style={{ textAlign: 'center', padding: 48 }}>帖子不存在或已被删除</div></Card>;

  const isAuthor = currentUserId === post.authorId;

  const renderReplyItem = (r: ForumReply) => (
    <List.Item
      style={{ padding: '12px 0' }}
      actions={[
        <Tooltip key="like" title="点赞">
          <Button
            type="text"
            size="small"
            icon={<LikeOutlined />}
            onClick={() => handleReplyLike(r.id)}
          >
            {r.likeCount || 0}
          </Button>
        </Tooltip>,
      ]}
    >
      <List.Item.Meta
        avatar={
          <Avatar size="small" style={{ backgroundColor: '#87d068' }}>
            {getUserAvatar(r.authorName)}
          </Avatar>
        }
        title={r.authorName || `用户${r.authorId}`}
        description={
          <>
            <div style={{ lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>{r.content}</div>
            <div style={{ fontSize: 12, color: '#ccc', marginTop: 4 }}>{formatRelativeTime(r.createdAt)}</div>
          </>
        }
      />
    </List.Item>
  );

  return (
    <div style={{ maxWidth: 800 }}>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/forum')}>返回论坛</Button>
        {isAuthor && (
          <Popconfirm title="确定删除这篇帖子吗？" onConfirm={handleDelete} okText="删除" cancelText="取消" okButtonProps={{ danger: true }}>
            <Button danger icon={<DeleteOutlined />}>删除帖子</Button>
          </Popconfirm>
        )}
      </Space>

      {/* 帖子内容 */}
      <Card>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
          <Avatar
            icon={<UserOutlined />}
            style={{ backgroundColor: '#1677ff', marginRight: 12, width: 40, height: 40, fontSize: 18 }}
          >
            {getUserAvatar(post.authorName)}
          </Avatar>
          <div>
            <div style={{ fontWeight: 600 }}>{post.authorName || '匿名用户'}</div>
            <div style={{ color: '#8c8c8c', fontSize: 12 }}>{formatRelativeTime(post.createdAt)} · {formatDateTime(post.createdAt)}</div>
          </div>
        </div>

        <h2 style={{ marginTop: 0, marginBottom: 12 }}>{post.title}</h2>

        {post.tags && (
          <div style={{ marginBottom: 12 }}>
            {post.tags.split(',').filter(Boolean).map((t, i) => (
              <Tag key={i} color="blue" style={{ borderRadius: 12 }}>#{t.trim()}</Tag>
            ))}
          </div>
        )}

        <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8, fontSize: 15, color: '#333' }}>{post.content}</div>

        <div style={{ marginTop: 24, paddingTop: 16, borderTop: '1px solid #f0f0f0', display: 'flex', alignItems: 'center', gap: 16 }}>
          <Space>
            <Tag>{post.categoryName}</Tag>
          </Space>
          <Space size="large" style={{ marginLeft: 'auto' }}>
            <span style={{ color: '#8c8c8c' }}><EyeOutlined /> {post.viewCount}</span>
            <Tooltip title={liked ? '取消点赞' : '点赞'}>
              <Button
                type={liked ? 'primary' : 'text'}
                icon={liked ? <LikeFilled /> : <LikeOutlined />}
                onClick={handleLike}
              >
                {post.likeCount || 0}
              </Button>
            </Tooltip>
          </Space>
        </div>
      </Card>

      {/* 回复列表 */}
      <Card title={`回复 (${post.replies?.length || 0})`} style={{ marginTop: 16 }}>
        {post.replies && post.replies.length > 0 ? (
          <List
            dataSource={post.replies}
            renderItem={renderReplyItem}
          />
        ) : (
          <div style={{ textAlign: 'center', color: '#8c8c8c', padding: 32 }}>暂无回复，快来抢沙发吧</div>
        )}

        {/* 回复输入框 */}
        <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #f0f0f0' }}>
          <Input.TextArea
            rows={3}
            value={reply}
            onChange={(e) => setReply(e.target.value)}
            placeholder="写下你的回复..."
            maxLength={2000}
            showCount
          />
          <Button type="primary" onClick={handleReply} style={{ marginTop: 8 }} disabled={!reply.trim()}>
            发表回复
          </Button>
        </div>
      </Card>
    </div>
  );
}
