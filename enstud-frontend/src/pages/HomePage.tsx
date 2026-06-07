import { Card, Row, Col } from 'antd';
import { BookOutlined, MessageOutlined, EditOutlined, TranslationOutlined, TeamOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const features = [
  { title: '单词学习', desc: 'SM-2 记忆算法，科学高效背单词', icon: <BookOutlined style={{ fontSize: 32, color: '#1677ff' }} />, path: '/word' },
  { title: 'AI 对话', desc: '场景化英语聊天，实时语法纠正', icon: <MessageOutlined style={{ fontSize: 32, color: '#52c41a' }} />, path: '/chat' },
  { title: '写作练习', desc: '提交作文，智能批改评分', icon: <EditOutlined style={{ fontSize: 32, color: '#faad14' }} />, path: '/writing' },
  { title: '翻译工具', desc: '中英互译，快速准确', icon: <TranslationOutlined style={{ fontSize: 32, color: '#ff4d4f' }} />, path: '/translate' },
  { title: '学习论坛', desc: '交流讨论，共同进步', icon: <TeamOutlined style={{ fontSize: 32, color: '#722ed1' }} />, path: '/forum' },
];

export default function HomePage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  return (
    <div>
      <h2>欢迎回来，{user.nickname || user.username}</h2>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        {features.map((f) => (
          <Col xs={24} sm={12} lg={8} xl={6} key={f.title}>
            <Card hoverable onClick={() => navigate(f.path)}>
              <div style={{ textAlign: 'center' }}>
                {f.icon}
                <h3 style={{ marginTop: 8 }}>{f.title}</h3>
                <p style={{ color: '#8c8c8c' }}>{f.desc}</p>
              </div>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
